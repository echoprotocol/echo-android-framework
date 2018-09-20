package com.pixelplex.echoframework.facade.internal

import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.core.crypto.CryptoCoreComponent
import com.pixelplex.echoframework.core.logger.internal.LoggerCoreComponent
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.exception.NotFoundException
import com.pixelplex.echoframework.facade.AuthenticationFacade
import com.pixelplex.echoframework.model.*
import com.pixelplex.echoframework.model.network.Network
import com.pixelplex.echoframework.model.operations.AccountUpdateOperation
import com.pixelplex.echoframework.model.operations.AccountUpdateOperationBuilder
import com.pixelplex.echoframework.processResult
import com.pixelplex.echoframework.service.DatabaseApiService
import com.pixelplex.echoframework.service.NetworkBroadcastApiService
import com.pixelplex.echoframework.support.dematerialize
import com.pixelplex.echoframework.support.error
import com.pixelplex.echoframework.support.map
import com.pixelplex.echoframework.support.value

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
