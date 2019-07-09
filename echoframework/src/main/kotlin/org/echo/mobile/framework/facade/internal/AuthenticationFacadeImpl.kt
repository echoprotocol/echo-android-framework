package org.echo.mobile.framework.facade.internal

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.exception.AccountNotFoundException
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.exception.NotFoundException
import org.echo.mobile.framework.facade.AuthenticationFacade
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AccountOptions
import org.echo.mobile.framework.model.AuthorityType
import org.echo.mobile.framework.model.FullAccount
import org.echo.mobile.framework.model.RegistrationResult
import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.model.eddsa.EdAddress
import org.echo.mobile.framework.model.eddsa.EdAuthority
import org.echo.mobile.framework.model.isEqualsByKey
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.model.operations.AccountUpdateOperation
import org.echo.mobile.framework.model.operations.AccountUpdateOperationBuilder
import org.echo.mobile.framework.processResult
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.service.NetworkBroadcastApiService
import org.echo.mobile.framework.service.RegistrationApiService
import org.echo.mobile.framework.support.concurrent.future.FutureTask
import org.echo.mobile.framework.support.concurrent.future.completeCallback
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
    private val network: Network,
    private val notificationsHelper: NotificationsHelper<RegistrationResult>
) : BaseTransactionsFacade(databaseApiService, cryptoCoreComponent), AuthenticationFacade {

    override fun isOwnedBy(name: String, password: String, callback: Callback<FullAccount>) {
        databaseApiService.getFullAccounts(listOf(name), false)
            .map { accountsMap -> accountsMap[name] }
            .value { account ->
                val address =
                    cryptoCoreComponent.getEdDSAAddress(name, password, AuthorityType.ACTIVE)

                val isKeySame =
                    account?.account?.isEqualsByKey(address, AuthorityType.ACTIVE) ?: false
                if (isKeySame) {
                    callback.onSuccess(account!!)
                    return
                }

                LOGGER.log("No account found owned by $name with specified password")
                callback.onError(AccountNotFoundException("No account found owned by $name with specified password"))
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

        val account =
            databaseApiService.getFullAccounts(listOf(accountId), false).dematerialize()

        operation.newOptionsOption.field?.votingAccount =
            account[accountId]?.account!!.options.votingAccount

        operation.newOptionsOption.field?.delegatingAccount =
            account[accountId]?.account!!.options.delegatingAccount

        val blockData = databaseApiService.getBlockData()
        val chainId = getChainId()

        val privateKey =
            cryptoCoreComponent.getEdDSAPrivateKey(name, oldPassword, AuthorityType.ACTIVE)
        val fees = getFees(listOf(operation))

        val transaction = Transaction(blockData, listOf(operation), chainId).apply {
            setFees(fees)
            addPrivateKey(privateKey)
        }

        networkBroadcastApiService.broadcastTransaction(transaction).dematerialize()
    }

    override fun changeWif(
        name: String,
        oldWif: String,
        newWif: String,
        callback: Callback<Any>
    ) = callback.processResult {
        val accountId = getAccountIdByWif(name, oldWif)
        val operation: AccountUpdateOperation =
            buildAccountUpdateOperationWithWif(accountId, newWif)

        val account =
            databaseApiService.getFullAccounts(listOf(accountId), false).dematerialize()

        operation.newOptionsOption.field?.votingAccount =
            account[accountId]?.account!!.options.votingAccount

        operation.newOptionsOption.field?.delegatingAccount =
            account[accountId]?.account!!.options.delegatingAccount

        val blockData = databaseApiService.getBlockData()
        val chainId = getChainId()

        val privateKey = cryptoCoreComponent.decodeFromWif(oldWif)
        val fees = getFees(listOf(operation))

        val transaction = Transaction(blockData, listOf(operation), chainId).apply {
            setFees(fees)
            addPrivateKey(privateKey)
        }

        networkBroadcastApiService.broadcastTransaction(transaction).dematerialize()
    }

    override fun register(userName: String, password: String, callback: Callback<Boolean>) {
        try {
            val active =
                cryptoCoreComponent.getEdDSAAddress(userName, password, AuthorityType.ACTIVE)

            val echorandKey = cryptoCoreComponent.getEchorandKey(userName, password)

            val callId = registrationApiService.register(
                userName,
                active,
                echorandKey
            ).dematerialize().toString()

            retrieveTransactionResult(callId, callback)
        } catch (ex: Exception) {
            callback.onError(LocalException("Can't register account", cause = ex))
        }
    }

    override fun registerByWif(userName: String, wif: String, callback: Callback<Boolean>) {
        try {
            val privateKeyRaw = cryptoCoreComponent.decodeFromWif(wif)
            val publicKeyRaw = cryptoCoreComponent.deriveEdDSAPublicKeyFromPrivate(privateKeyRaw)

            val active =
                cryptoCoreComponent.getEdDSAAddressFromPublicKey(publicKeyRaw)

            val callId = registrationApiService.register(
                userName,
                active,
                active
            ).dematerialize().toString()

            retrieveTransactionResult(callId, callback)
        } catch (ex: Exception) {
            callback.onError(LocalException("Can't register account", cause = ex))
        }
    }

    private fun retrieveTransactionResult(
        callId: String,
        callback: Callback<Boolean>
    ) {
        val future = FutureTask<RegistrationResult>()
        notificationsHelper.subscribeOnResult(
            callId,
            future.completeCallback()
        )

        future.get()
            ?: throw NotFoundException("Result of operation not found.")

        callback.onSuccess(true)
    }

    private fun getAccountId(name: String, password: String): String {
        val address =
            cryptoCoreComponent.getEdDSAAddress(name, password, AuthorityType.ACTIVE)

        return getOwnedAccountId(name, address)
    }

    private fun getAccountIdByWif(name: String, wif: String): String {
        val privateKeyRaw = cryptoCoreComponent.decodeFromWif(wif)
        val publicKeyRaw = cryptoCoreComponent.deriveEdDSAPublicKeyFromPrivate(privateKeyRaw)
        val address = cryptoCoreComponent.getEdDSAAddressFromPublicKey(publicKeyRaw)

        return getOwnedAccountId(name, address)
    }

    private fun getOwnedAccountId(name: String, address: String): String {
        val accountsResult = databaseApiService.getFullAccounts(listOf(name), false)

        val account = accountsResult
            .map { accountsMap -> accountsMap[name] }
            .map { fullAccount -> fullAccount?.account }
            .dematerialize()

        val isKeySame = account?.isEqualsByKey(address, AuthorityType.ACTIVE) ?: false

        return if (isKeySame) {
            account!!.getObjectId()
        } else {
            throw AccountNotFoundException("No account found for specified name and password")
        }
    }

    private fun buildAccountUpdateOperation(
        id: String,
        name: String,
        newPassword: String
    ): AccountUpdateOperation {
        val active = cryptoCoreComponent.getEdDSAAddress(name, newPassword, AuthorityType.ACTIVE)

        val echorandKey = cryptoCoreComponent.getEchorandKey(name, newPassword)

        val activeAuthority =
            EdAuthority(
                1,
                hashMapOf(EdAddress(active).pubKey to 1L),
                hashMapOf()
            )

        val newOptions = AccountOptions()
        val account = Account(id)

        return AccountUpdateOperationBuilder()
            .setOptions(newOptions)
            .setAccount(account)
            .setActive(activeAuthority)
            .setEdKey(echorandKey)
            .build()
    }

    private fun buildAccountUpdateOperationWithWif(
        id: String,
        newWif: String
    ): AccountUpdateOperation {
        val privateKeyRaw = cryptoCoreComponent.decodeFromWif(newWif)
        val publicKeyRaw = cryptoCoreComponent.deriveEdDSAPublicKeyFromPrivate(privateKeyRaw)
        val address = cryptoCoreComponent.getEdDSAAddressFromPublicKey(publicKeyRaw)

        val activeAuthority =
            EdAuthority(
                1,
                hashMapOf(EdAddress(address).pubKey to 1L),
                hashMapOf()
            )

        val newOptions = AccountOptions()
        val account = Account(id)

        return AccountUpdateOperationBuilder()
            .setOptions(newOptions)
            .setAccount(account)
            .setActive(activeAuthority)
            .setEdKey(address)
            .build()
    }

    companion object {
        private val LOGGER = LoggerCoreComponent.create(AuthenticationFacadeImpl::class.java.name)
    }

}
