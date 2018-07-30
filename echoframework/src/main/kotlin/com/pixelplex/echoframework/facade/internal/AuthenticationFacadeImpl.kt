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
import com.pixelplex.echoframework.service.DatabaseApiService
import com.pixelplex.echoframework.service.NetworkBroadcastApiService
import com.pixelplex.echoframework.support.Result
import com.pixelplex.echoframework.support.error
import com.pixelplex.echoframework.support.map
import com.pixelplex.echoframework.support.value

/**
 * Implementation of [AuthenticationFacade]
 *
 * <p>
 *     Delegates API call logic to connected services
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

    override fun isOwnedBy(name: String, password: String, callback: Callback<Account>) {
        val result = databaseApiService.getFullAccounts(listOf(name), false)

        result
            .map { accountsMap -> accountsMap[name] }
            .value { fullAccount ->
                val address = cryptoCoreComponent.getAddress(name, password, AuthorityType.OWNER)

                val account = fullAccount?.account
                val isKeySame = account?.isEqualsByKey(address, AuthorityType.OWNER) ?: false
                if (isKeySame) {
                    callback.onSuccess(account!!)
                    return
                }

                LOGGER.log("No account found owned by $name with specified password")
                callback.onError(NotFoundException("Account not found."))
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
    ) {
        try {
            val accountId = getAccountId(name, oldPassword)
            val operation: AccountUpdateOperation =
                buildAccountUpdateOperation(accountId, name, newPassword)

            val blockData = databaseApiService.getBlockData()
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

    companion object {
        private val LOGGER = LoggerCoreComponent.create(AuthenticationFacadeImpl::class.java.name)
    }

}
