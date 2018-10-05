package org.echo.mobile.framework.facade.internal

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.exception.NotFoundException
import org.echo.mobile.framework.facade.AuthenticationFacade
import org.echo.mobile.framework.model.*
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.model.operations.AccountUpdateOperation
import org.echo.mobile.framework.model.operations.AccountUpdateOperationBuilder
import org.echo.mobile.framework.processResult
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.service.NetworkBroadcastApiService
import org.echo.mobile.framework.support.dematerialize
import org.echo.mobile.framework.support.error
import org.echo.mobile.framework.support.map
import org.echo.mobile.framework.support.value

/**
 * Implementation of [AuthenticationFacade]
 *
 * Delegates API call logic to connected services
 *
 * @author Dmitriy Bushuev
 */
class AuthenticationFacadeImpl(
    private val databaseApiService: DatabaseApiService,
    private val networkBroadcastApiService: NetworkBroadcastApiService,
    private val cryptoCoreComponent: CryptoCoreComponent,
    private val network: Network
) : BaseTransactionsFacade(databaseApiService, cryptoCoreComponent), AuthenticationFacade {

    override fun isOwnedBy(name: String, password: String, callback: Callback<FullAccount>) {
        databaseApiService.getFullAccounts(listOf(name), false)
            .map { accountsMap -> accountsMap[name] }
            .value { account ->
                val address = cryptoCoreComponent.getAddress(name, password, AuthorityType.OWNER)

                val isKeySame =
                    account?.account?.isEqualsByKey(address, AuthorityType.OWNER) ?: false
                if (isKeySame) {
                    callback.onSuccess(account!!)
                    return
                }

                LOGGER.log("No account found owned by $name with specified password")
                callback.onError(NotFoundException("No account found owned by $name with specified password"))
            }
            .error { error ->
                callback.onError(LocalException(error.message, error))
            }
    }

    override fun changePassword(
        name: String,
        oldPassword: String,
        newPassword: String,
        callback: Callback<Any>
    ) = callback.processResult {
        val accountId = getAccountId(name, oldPassword)
        val operation: AccountUpdateOperation =
            buildAccountUpdateOperation(accountId, name, newPassword)

        val blockData = databaseApiService.getBlockData()
        val chainId = getChainId()

        val privateKey =
            cryptoCoreComponent.getPrivateKey(name, oldPassword, AuthorityType.OWNER)
        val fees = getFees(listOf(operation))

        val transaction = Transaction(blockData, listOf(operation), chainId).apply {
            setFees(fees)
            addPrivateKey(privateKey)
        }

        networkBroadcastApiService.broadcastTransactionWithCallback(transaction).dematerialize()
    }

    private fun getAccountId(name: String, password: String): String {
        val accountsResult = databaseApiService.getFullAccounts(listOf(name), false)

        val ownerAddress =
            cryptoCoreComponent.getAddress(name, password, AuthorityType.OWNER)

        val account = accountsResult
            .map { accountsMap -> accountsMap[name] }
            .map { fullAccount -> fullAccount?.account }
            .dematerialize()

        val isKeySame = account?.isEqualsByKey(ownerAddress, AuthorityType.OWNER) ?: false

        return if (isKeySame) {
            account!!.getObjectId()
        } else {
            throw NotFoundException("No account found for specified name and password")
        }
    }

    private fun buildAccountUpdateOperation(
        id: String,
        name: String,
        newPassword: String
    ): AccountUpdateOperation {
        val newOwnerKey = cryptoCoreComponent.getAddress(name, newPassword, AuthorityType.OWNER)
        val newActiveKey =
            cryptoCoreComponent.getAddress(name, newPassword, AuthorityType.ACTIVE)
        val newMemoKey =
            cryptoCoreComponent.getAddress(name, newPassword, AuthorityType.KEY)

        val address = Address(newMemoKey, network)
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

    companion object {
        private val LOGGER = LoggerCoreComponent.create(AuthenticationFacadeImpl::class.java.name)
    }

}
