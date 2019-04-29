package org.echo.mobile.framework.facade

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.ECHO_ASSET_ID
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.exception.AccountNotFoundException
import org.echo.mobile.framework.exception.AuthenticationException
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.Address
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.AuthorityType
import org.echo.mobile.framework.model.BaseOperation
import org.echo.mobile.framework.model.BlockData
import org.echo.mobile.framework.model.Memo
import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.model.isEqualsByKey
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.support.dematerialize
import org.echo.mobile.framework.support.error
import org.echo.mobile.framework.support.value
import java.math.BigInteger
import java.util.concurrent.TimeUnit

/**
 * Includes default logic for transactions assembly
 *
 * @author Daria Pechkovskaya
 */
interface TransactionFacadeExtension {

    /**
     * Used for api requests processing.
     */
    val databaseApiService: DatabaseApiService

    /**
     * Used for keys processing.
     */
    val cryptoCoreComponent: CryptoCoreComponent

    /**
     * Retrieves blockchain chain id
     * @return chain id string
     */
    fun getChainId(): String = databaseApiService.getChainId().dematerialize()

    /**
     * Retrieves required fee by asset for each operation
     *
     * @param operations Operations for getting fee
     * @param asset Asset type for fee paying
     *
     * @return [AssetAmount] fees for each operation
     */
    fun getFees(
        operations: List<BaseOperation>,
        asset: Asset = Asset(ECHO_ASSET_ID)
    ): List<AssetAmount> = databaseApiService.getRequiredFees(operations, asset).dematerialize()

    /**
     * Retrieves required fee by asset for each operation
     *
     * @param operations Operations for getting fee
     * @param assetId Id of asset type for fee paying
     *
     * @return [AssetAmount] fees for each operation
     */
    fun getFees(operations: List<BaseOperation>, assetId: String): List<AssetAmount> =
        getFees(operations, Asset(assetId))

    /**
     * Check if [account] is owned by [name] and [password].
     *
     * Throws [AuthenticationException] if account is not owned.
     */
    fun checkOwnerAccount(name: String, password: String, account: Account) {
        val ownerAddress =
            cryptoCoreComponent.getAddress(name, password, AuthorityType.ACTIVE)

        val isKeySame = account.isEqualsByKey(ownerAddress, AuthorityType.ACTIVE)
        if (!isKeySame) {
            throw AuthenticationException("Owner account checking exception")
        }
    }

    /**
     * Check if [account] is owned by [wif].
     *
     * Throws [AuthenticationException] if account is not owned.
     */
    fun checkOwnerAccount(wif: String, account: Account) {
        val privateKey = cryptoCoreComponent.decodeFromWif(wif)
        val publicKey = cryptoCoreComponent.derivePublicKeyFromPrivate(privateKey)
        val address = cryptoCoreComponent.getAddressFromPublicKey(publicKey)

        val isKeySame = account.isEqualsByKey(address, AuthorityType.ACTIVE)
        if (!isKeySame) {
            throw AuthenticationException("Owner account checking exception")
        }
    }

    /**
     * Retrieves memo key by [name] and [password]
     *
     * @return String of memo key
     */
    fun memoKey(name: String, password: String) =
        cryptoCoreComponent.getPrivateKey(name, password, AuthorityType.ACTIVE)

    /**
     * Encrypts [message] by [privateKey] for accessing only for [fromAccount] and [toAccount]
     *
     * @return [Memo] blockchain object
     */
    fun generateMemo(
        privateKey: ByteArray,
        fromAccount: Account,
        toAccount: Account,
        message: String?
    ): Memo {
        if (message != null) {
            val encryptedMessage = cryptoCoreComponent.encryptMessage(
                privateKey,
                toAccount.options.memoKey!!.key,
                BigInteger.ZERO,
                message
            )

            return Memo(
                Address(fromAccount.options.memoKey!!),
                Address(toAccount.options.memoKey!!),
                BigInteger.ZERO,
                encryptedMessage ?: ByteArray(0)
            )
        }

        return Memo()
    }

    /**
     * Retrieves [Account] pair of transaction participants by accounts names or ids
     */
    fun getParticipantsPair(
        fromNameOrId: String,
        toNameOrId: String
    ): Pair<Account, Account> {
        var toAccount: Account? = null
        var fromAccount: Account? = null

        databaseApiService.getFullAccounts(listOf(fromNameOrId, toNameOrId), false)
            .value { accountsMap ->
                fromAccount = accountsMap[fromNameOrId]?.account
                    ?: throw AccountNotFoundException("Unable to find required account $fromNameOrId")
                toAccount = accountsMap[toNameOrId]?.account
                    ?: throw AccountNotFoundException("Unable to find required account $toNameOrId")
            }
            .error { accountsError ->
                throw LocalException("Error occurred during accounts request", accountsError)
            }

        return Pair(fromAccount!!, toAccount!!)
    }

    /**
     * Builds transaction for blockchain [operation] call
     */
    fun configureTransaction(
        operation: BaseOperation, privateKey: ByteArray, asset: String, feeAsset: String?
    ): Transaction {
        val blockData = getBlockData()
        val chainId = getChainId()
        val fees = getFees(listOf(operation), feeAsset ?: asset)

        return Transaction(
            blockData,
            listOf(operation),
            chainId
        ).apply {
            setFees(fees)
            addPrivateKey(privateKey)
        }
    }

    /**
     * Retrieves base block information
     *
     * @return current block data
     */
    fun getBlockData(): BlockData {
        val dynamicProperties = databaseApiService.getDynamicGlobalProperties().dematerialize()
        val expirationTime = TimeUnit.MILLISECONDS.toSeconds(dynamicProperties.date!!.time) +
                Transaction.DEFAULT_EXPIRATION_TIME
        val headBlockId = dynamicProperties.headBlockId
        val headBlockNumber = dynamicProperties.headBlockNumber
        return BlockData(headBlockNumber, headBlockId, expirationTime)
    }

    /**
     * Retrieves base block information
     */
    fun getBlockData(callback: Callback<BlockData>) {
        callback.onSuccess(getBlockData())
    }

}