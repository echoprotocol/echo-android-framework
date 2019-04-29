package org.echo.mobile.framework.facade

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.BlockData
import org.echo.mobile.framework.model.FullAccount
import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.support.Result
import org.echo.mobile.framework.support.concurrent.future.FutureTask
import org.echo.mobile.framework.support.concurrent.future.completeCallback
import org.echo.mobile.framework.support.concurrent.future.wrapResult
import org.echo.mobile.framework.support.dematerialize
import java.util.concurrent.TimeUnit

/**
 * Includes default logic for information assembly
 *
 * @author Daria Pechkovskaya
 */
interface InformationFacadeExtension {

    /**
     * Used for keys processing.
     */
    val cryptoCoreComponent: CryptoCoreComponent

    /**
     * Used for api requests processing.
     */
    val databaseApiService: DatabaseApiService

    /**
     * Fetches accounts associated with private keys in wifs format [wifs]
     *
     * Calls [callback]'s success method with map, contains pairs wif -> accounts list, associated with this wif
     */
    fun getAccountsByWif(wifs: List<String>, callback: Callback<Map<String, List<FullAccount>>>) {
        val keys = wifs.map { wif ->
            val privateKey = cryptoCoreComponent.decodeFromWif(wif)
            val publicKey = cryptoCoreComponent.derivePublicKeyFromPrivate(privateKey)
            cryptoCoreComponent.getAddressFromPublicKey(publicKey)
        }

        val keyReferences = databaseApiService.getKeyReferences(keys).dematerialize()
        val requiredAccounts = keyReferences.values.flatten()
        receiveRequiredAccountsByWifs(wifs, keys, requiredAccounts, keyReferences, callback)
    }

    private fun receiveRequiredAccountsByWifs(
        wifs: List<String>,
        keys: List<String>,
        requiredAccounts: List<String>,
        accountsIdsMap: Map<String, List<String>>,
        callback: Callback<Map<String, List<FullAccount>>>
    ) = try {
        val accounts =
            databaseApiService.getFullAccounts(requiredAccounts, false).dematerialize()
        val resultMap =
            accountsByWifMap(wifs, keys, accountsIdsMap, accounts)

        callback.onSuccess(resultMap)
    } catch (ex: Exception) {
        callback.onSuccess(mapOf())
    }

    private fun accountsByWifMap(
        wifs: List<String>,
        keys: List<String>,
        accountsIdsMap: Map<String, List<String>>,
        receivedAccounts: Map<String, FullAccount>
    ): Map<String, List<FullAccount>> {
        val resultMap = hashMapOf<String, List<FullAccount>>()

        accountsIdsMap.forEach { (key, accountIds) ->
            val wif = wifs[keys.indexOf(key)]
            val accounts = mutableListOf<FullAccount>()

            accountIds.distinct().forEach { id ->
                val account = receivedAccounts[id]
                account?.let { accounts.add(it) }
            }

            resultMap[wif] = accounts
        }

        return resultMap
    }

    /**
     * Fetches accounts associated with private keys in wifs format [wifs]
     *
     * Returns map, contains pairs wif -> accounts list, associated with this wif
     */
    fun getAccountsByWif(wifs: List<String>): Result<LocalException, Map<String, List<FullAccount>>> {
        val accountFuture = FutureTask<Map<String, List<FullAccount>>>()

        getAccountsByWif(wifs, accountFuture.completeCallback())

        return accountFuture.wrapResult()
    }

    /**
     * Fills [accounts] with data.
     *
     * @return [Result] with map, contains pair account id -> filled account, associated with this
     * account id
     */
    fun fillAccounts(accounts: Map<String, FullAccount>): Result<Exception, Map<String, FullAccount>> {
        val futureTask = FutureTask<Map<String, FullAccount>>()
        fillAccounts(accounts, futureTask.completeCallback())
        return futureTask.wrapResult()
    }


    /**
     * Fills [accounts] with data.
     *
     * Callback returns map, contains pair account id -> filled account, associated with this
     * account id
     */
    fun fillAccounts(
        accounts: Map<String, FullAccount>,
        callback: Callback<Map<String, FullAccount>>
    ) {
        val requiredAssets = getRequiredAssets(accounts.values)
        databaseApiService.getAssets(requiredAssets.toList(), object : Callback<List<Asset>> {

            override fun onSuccess(result: List<Asset>) {
                fillAssets(accounts, result, callback)
            }

            override fun onError(error: LocalException) {
                callback.onError(error)
            }

        })
    }

    private fun getRequiredAssets(accounts: Collection<FullAccount>): List<String> {
        val requiredAssets = mutableSetOf<String>()

        accounts.forEach { fullAccount ->
            val balanceAssets =
                fullAccount.balances?.map { it.asset!!.getObjectId() } ?: emptyList()
            val accountAssets = fullAccount.assets?.map { it.getObjectId() } ?: emptyList()

            requiredAssets.addAll(balanceAssets)
            requiredAssets.addAll(accountAssets)
        }

        return requiredAssets.toList()
    }

    private fun fillAssets(
        accounts: Map<String, FullAccount>,
        assets: List<Asset>,
        callback: Callback<Map<String, FullAccount>>
    ) {
        if (assets.isEmpty()) {
            callback.onSuccess(accounts)
            return
        }

        accounts.values.forEach { fullAccount ->
            fullAccount.balances?.forEach { balance ->
                balance.asset =
                    assets.find { asset -> asset.getObjectId() == balance.asset?.getObjectId() }
                        ?: balance.asset
            }

            val filledAssets = mutableListOf<Asset>()
            fullAccount.assets?.forEach { asset ->
                val candidate =
                    assets.find { it.getObjectId() == asset.getObjectId() } ?: asset
                filledAssets.add(candidate)
            }

            fullAccount.assets = filledAssets
        }

        callback.onSuccess(accounts)
    }

}