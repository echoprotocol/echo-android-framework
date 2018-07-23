package com.pixelplex.echolib.facade.internal

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.core.crypto.CryptoCoreComponent
import com.pixelplex.echolib.exception.NotFoundException
import com.pixelplex.echolib.exception.LocalException
import com.pixelplex.echolib.facade.AuthenticationFacade
import com.pixelplex.echolib.model.*
import com.pixelplex.echolib.model.network.Network
import com.pixelplex.echolib.model.operations.AccountUpdateOperation
import com.pixelplex.echolib.service.DatabaseApiService
import com.pixelplex.echolib.service.NetworkBroadcastApiService
import com.pixelplex.echolib.support.Result
import com.pixelplex.echolib.support.fold
import com.pixelplex.echolib.support.operationbuilders.AccountUpdateOperationBuilder
import java.util.concurrent.TimeUnit

/**
 * Implementation of [AuthenticationFacade]
 *
 * <p>
 *     Delegates API call logic to [AccountHistoryApiService]
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class AuthenticationFacadeImpl(
    private val databaseApiService: DatabaseApiService,
    private val networkBroadcastApiService: NetworkBroadcastApiService,
    private val cryptoCoreComponent: CryptoCoreComponent,
    private val network: Network
) : AuthenticationFacade {

    override fun login(name: String, password: String, callback: Callback<Account>) {
        val result = databaseApiService.getFullAccounts(listOf(name), false)

        result.fold({ accountsMap ->
            val foundFullAccount = accountsMap[name]
            val address = cryptoCoreComponent.getAddress(name, password, AuthorityType.OWNER)

            val account = foundFullAccount?.account
            val isKeySame = account?.isEqualsByKey(address, AuthorityType.OWNER) ?: false
            if (isKeySame) {
                callback.onSuccess(account!!)
                return
            }

            callback.onError(NotFoundException("Account not found."))
        }, { error ->
            callback.onError(LocalException(error.message, error))
        })
    }

    /**
     * Check account equals by [key] from role [authorityType]
     *
     * @param key Public key from role
     * @param authorityType Role for equals operation
     */
    private fun Account.isEqualsByKey(key: String, authorityType: AuthorityType): Boolean =
        when (authorityType) {
            AuthorityType.OWNER -> isKeyExist(key, owner)
            AuthorityType.ACTIVE -> isKeyExist(key, active)
            AuthorityType.KEY -> {
                options.memoKey?.address == key
            }
        }

    private fun isKeyExist(address: String, authority: Authority): Boolean {
        val foundKey = authority.keyAuthorities.keys.find { pubKey ->
            pubKey.address == address
        }
        return foundKey != null
    }

    override fun changePassword(
        name: String,
        oldPassword: String,
        newPassword: String,
        callback: Callback<Any>
    ) {
        try {
            val accountId = getAccountId(name, oldPassword)
            val operation: AccountUpdateOperation =
                buildAccountUpdateOperation(accountId, name, newPassword)

            val blockData = getBlockData()
            val chainId = getChainId()

            val privateKey =
                cryptoCoreComponent.getPrivateKey(name, oldPassword, AuthorityType.OWNER)
            val transaction = Transaction(privateKey, blockData, listOf(operation), chainId)

            val fees = getFees(listOf(operation))
            transaction.setFees(fees)

            val transactionResult =
                networkBroadcastApiService.broadcastTransactionWithCallback(transaction)
            if (transactionResult is Result.Value) {
                return callback.onSuccess(Any())
            } else {
                throw (transactionResult as Result.Error).error
            }

        } catch (ex: LocalException) {
            return callback.onError(ex)

        } catch (ex: Exception) {
            return callback.onError(LocalException(ex.message, ex))
        }
    }

    private fun getAccountId(name: String, password: String): String {
        val accountsResult = databaseApiService.getFullAccounts(listOf(name), false)

        val accountsMap = if (accountsResult is Result.Value) {
            accountsResult.value
        } else {
            throw (accountsResult as Result.Error).error
        }
        val foundFullAccount = accountsMap[name]
        val ownerAddress =
            cryptoCoreComponent.getAddress(name, password, AuthorityType.OWNER)

        val account = foundFullAccount?.account
        val isKeySame = account?.isEqualsByKey(ownerAddress, AuthorityType.OWNER) ?: false
        return if (isKeySame) {
            account!!.getObjectId()
        } else {
            throw NotFoundException("Account not found.")
        }
    }

    private fun getBlockData(): BlockData {
        val globalPropertiesResult = databaseApiService.getDynamicGlobalProperties()
        val dynamicProperties = if (globalPropertiesResult is Result.Value) {
            globalPropertiesResult.value
        } else {
            throw (globalPropertiesResult as Result.Error).error
        }
        val expirationTime = TimeUnit.MILLISECONDS.toSeconds(dynamicProperties.date!!.time) +
                Transaction.DEFAULT_EXPIRATION_TIME
        val headBlockId = dynamicProperties.headBlockId
        val headBlockNumber = dynamicProperties.headBlockNumber
        return BlockData(headBlockNumber, headBlockId, expirationTime)
    }

    private fun getChainId(): String {
        val chainIdResult = databaseApiService.getChainId()
        return if (chainIdResult is Result.Value) {
            chainIdResult.value
        } else {
            throw (chainIdResult as Result.Error).error
        }
    }

    private fun getFees(operations: List<BaseOperation>): List<AssetAmount> {
        val feesResult =
            databaseApiService.getRequiredFees(operations, Asset("1.3.0"))
        return if (feesResult is Result.Value) {
            feesResult.value
        } else {
            throw (feesResult as Result.Error).error
        }
    }

    private fun buildAccountUpdateOperation(
        id: String,
        name: String,
        newPassword: String
    ): AccountUpdateOperation {
        val newOwnerKey = cryptoCoreComponent.getAddress(name, newPassword, AuthorityType.OWNER)
        val newActiveKey = cryptoCoreComponent.getAddress(name, newPassword, AuthorityType.ACTIVE)
        val address = Address(newActiveKey, network)
        val ownerAuthority =
            Authority(1, hashMapOf(Address(newOwnerKey, network).pubKey to 1L), hashMapOf())
        val activeAuthority =
            Authority(1, hashMapOf(Address(newActiveKey, network).pubKey to 1L), hashMapOf())
        val newOptions = AccountOptions(address.pubKey)
        val account = Account(id)

        return AccountUpdateOperationBuilder()
            .setOptions(newOptions)
            .setAccount(account)
            .setOwner(ownerAuthority)
            .setActive(activeAuthority)
            .build()
    }


}
