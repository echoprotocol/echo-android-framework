package org.echo.mobile.framework.facade.internal

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.bitcoinj.Sha256Hash
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.ECHO_ASSET_ID
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.exception.AccountNotFoundException
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.exception.NotFoundException
import org.echo.mobile.framework.facade.AuthenticationFacade
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AccountOptions
import org.echo.mobile.framework.model.FullAccount
import org.echo.mobile.framework.model.RegistrationResult
import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.model.eddsa.EdAddress
import org.echo.mobile.framework.model.eddsa.EdAuthority
import org.echo.mobile.framework.model.isEqualsByKey
import org.echo.mobile.framework.model.operations.AccountUpdateOperation
import org.echo.mobile.framework.model.operations.AccountUpdateOperationBuilder
import org.echo.mobile.framework.processResult
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.service.NetworkBroadcastApiService
import org.echo.mobile.framework.service.RegistrationApiService
import org.echo.mobile.framework.support.Int64
import org.echo.mobile.framework.support.concurrent.future.FutureTask
import org.echo.mobile.framework.support.concurrent.future.completeCallback
import org.echo.mobile.framework.support.dematerialize
import org.echo.mobile.framework.support.error
import org.echo.mobile.framework.support.map
import org.echo.mobile.framework.support.value
import org.spongycastle.util.encoders.Hex

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
    private val notificationsHelper: NotificationsHelper<RegistrationResult>,
    private val transactionExpirationDelay: Long
) : BaseTransactionsFacade(databaseApiService, cryptoCoreComponent, transactionExpirationDelay), AuthenticationFacade {

    override fun isOwnedBy(nameOrId: String, wif: String, callback: Callback<FullAccount>) {
        databaseApiService.getFullAccounts(listOf(nameOrId), false)
            .map { accountsMap -> accountsMap[nameOrId] }
            .value { account ->
                try {
                    val isOwner = checkOwner(account, wif)

                    if (isOwner) callback.onSuccess(account!!)

                    LOGGER.log("No account found owned by $nameOrId with specified password")
                    callback.onError(AccountNotFoundException("No account found owned by $nameOrId with specified password"))
                } catch (exception: Exception) {
                    LOGGER.log("Error during account'sowner checking")
                    callback.onError(AccountNotFoundException("Error during account'sowner checking"))
                }
            }
            .error { error ->
                callback.onError(LocalException(error.message, error))
            }
    }

    override fun changeKeys(
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

        operation.newOptionsOption.field?.delegatingAccount =
            account[accountId]?.account!!.options.delegatingAccount

        operation.newOptionsOption.field?.delegateShare =
            account[accountId]?.account!!.options.delegateShare

        val privateKey = cryptoCoreComponent.decodeFromWif(oldWif)
        val transaction = configureTransaction(operation, privateKey, ECHO_ASSET_ID)

        networkBroadcastApiService.broadcastTransaction(transaction).dematerialize()
    }

    override fun register(
        userName: String,
        wif: String,
        evmAddress: String?,
        callback: Callback<Boolean>
    ) {
        try {
            val registrationTask = registrationApiService.requestRegistrationTask().dematerialize()
            val nonce = solveTask(
                registrationTask.blockId,
                registrationTask.randNum,
                registrationTask.difficulty
            )

            val privateKeyRaw = cryptoCoreComponent.decodeFromWif(wif)
            val publicKeyRaw = cryptoCoreComponent.deriveEdDSAPublicKeyFromPrivate(privateKeyRaw)

            val active =
                cryptoCoreComponent.getEdDSAAddressFromPublicKey(publicKeyRaw)

            val callId = registrationApiService.submitRegistrationSolution(
                userName,
                active,
                active,
                evmAddress,
                nonce,
                registrationTask.randNum
            ).dematerialize()

            if (callId == -1) {
                callback.onError(NotFoundException("Result of operation not found."))
            } else {
                retrieveTransactionResult(callId.toString(), callback)
            }
        } catch (ex: Exception) {
            callback.onError(LocalException("Can't register account", cause = ex))
        }
    }

    private fun solveTask(blockId: String, randNum: UnsignedLong, difficulty: Int): UnsignedLong {
        var nonce = UnsignedLong.ZERO
        val block = Hex.decode(blockId)
        val rand = Int64.serialize(randNum)
        var hash = Sha256Hash.hash(
            block + rand + Int64.serialize(nonce)
        )

        val (index, value) = getSolutionPair(difficulty)
        while (true) {
            if (hash.take(index)
                    .all { it.toInt() == 0 } && hash[index] > 0 && hash[index] < value
            ) break

            nonce = nonce.plus(UnsignedLong.ONE)
            hash = Sha256Hash.hash(
                block + rand + Int64.serialize(nonce)
            )
        }

        return nonce
    }

    private fun getSolutionPair(difficulty: Int): Pair<Int, Int> {
        val index = difficulty / 8
        val rest = difficulty % 8

        val a = 1 shl (rest - 1)

        return Pair(index, a)
    }

    private fun checkOwner(account: FullAccount?, wif: String): Boolean {
        val privateKey = cryptoCoreComponent.decodeFromWif(wif)
        val publicKey = cryptoCoreComponent.deriveEdDSAPublicKeyFromPrivate(privateKey)

        val address =
            cryptoCoreComponent.getEdDSAAddressFromPublicKey(publicKey)

        return account?.account?.isEqualsByKey(address) ?: false
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

        val isKeySame = account?.isEqualsByKey(address) ?: false

        return if (isKeySame) {
            account!!.getObjectId()
        } else {
            throw AccountNotFoundException("No account found for specified name and password")
        }
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
