package org.echo.mobile.framework.facade.internal

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.exception.NotFoundException
import org.echo.mobile.framework.facade.AuthenticationFacade
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AccountOptions
import org.echo.mobile.framework.model.Address
import org.echo.mobile.framework.model.Authority
import org.echo.mobile.framework.model.AuthorityType
import org.echo.mobile.framework.model.FullAccount
import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.model.isEqualsByKey
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.model.operations.AccountUpdateOperation
import org.echo.mobile.framework.model.operations.AccountUpdateOperationBuilder
import org.echo.mobile.framework.processResult
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.service.NetworkBroadcastApiService
import org.echo.mobile.framework.service.RegistrationApiService
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
    private val registrationApiService: RegistrationApiService,
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

        networkBroadcastApiService.broadcastTransaction(transaction).dematerialize()
    }

    override fun register(userName: String, password: String, callback: Callback<Boolean>) {
        val (owner, active, memo) = generateAccountKeys(userName, password)
        val echorandKey = cryptoCoreComponent.getEchorandKey(userName, password)

        registrationApiService.register(
            userName,
            owner,
            active,
            memo,
            echorandKey,
            callback
        )
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
        val (owner, active, memo) = generateAccountKeys(name, newPassword)

        val address = Address(memo, network)

        val ownerAuthority =
            Authority(1, hashMapOf(Address(owner, network).pubKey to 1L), hashMapOf())
        val activeAuthority =
            Authority(1, hashMapOf(Address(active, network).pubKey to 1L), hashMapOf())

        val newOptions = AccountOptions(address.pubKey)
        val account = Account(id)

        return AccountUpdateOperationBuilder()
            .setOptions(newOptions)
            .setAccount(account)
            .setOwner(ownerAuthority)
            .setActive(activeAuthority)
            .build()
    }

    private fun generateAccountKeys(
        name: String,
        password: String
    ): Triple<String, String, String> {
        val ownerKey = cryptoCoreComponent.getAddress(name, password, AuthorityType.OWNER)
        val activeKey =
            cryptoCoreComponent.getAddress(name, password, AuthorityType.ACTIVE)
        val memoKey =
            cryptoCoreComponent.getAddress(name, password, AuthorityType.KEY)

        return Triple(ownerKey, activeKey, memoKey)
    }

    companion object {
        private val LOGGER = LoggerCoreComponent.create(AuthenticationFacadeImpl::class.java.name)
    }

}
