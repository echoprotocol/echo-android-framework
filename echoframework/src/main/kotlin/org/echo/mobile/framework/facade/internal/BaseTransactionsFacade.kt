package org.echo.mobile.framework.facade.internal

import org.echo.mobile.framework.ECHO_ASSET_ID
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.exception.AccountNotFoundException
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.BaseOperation
import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.model.contract.ContractFee
import org.echo.mobile.framework.model.isEqualsByKey
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.support.dematerialize
import org.echo.mobile.framework.support.error
import org.echo.mobile.framework.support.value

/**
 * Includes base logic for transactions assembly
 *
 * @author Daria Pechkovskaya
 */
abstract class BaseTransactionsFacade(
    private val databaseApiService: DatabaseApiService,
    private val cryptoCoreComponent: CryptoCoreComponent,
    private val transactionExpirationDelay: Long
) {

    protected fun getChainId(): String = databaseApiService.getChainId().dematerialize()

    protected fun getFees(
        operations: List<BaseOperation>,
        asset: Asset = Asset(ECHO_ASSET_ID)
    ): List<AssetAmount> = databaseApiService.getRequiredFees(operations, asset).dematerialize()

    protected fun getFees(operations: List<BaseOperation>, assetId: String): List<AssetAmount> =
        databaseApiService.getRequiredFees(operations, Asset(assetId)).dematerialize()

    protected fun getContractFees(
        operations: List<BaseOperation>,
        assetId: String
    ): List<ContractFee> =
        databaseApiService.getRequiredContractFees(operations, Asset(assetId)).dematerialize()

    protected fun checkOwnerAccount(wif: String, account: Account) {
        val privateKey = cryptoCoreComponent.decodeFromWif(wif)
        val publicKey = cryptoCoreComponent.deriveEdDSAPublicKeyFromPrivate(privateKey)
        val address = cryptoCoreComponent.getEdDSAAddressFromPublicKey(publicKey)

        val isKeySame = account.isEqualsByKey(address)
        if (!isKeySame) {
            throw LocalException("Owner account checking exception")
        }
    }

    protected fun getParticipantsPair(
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

    protected fun configureTransaction(
        transfer: BaseOperation, privateKey: ByteArray, asset: String, feeAsset: String? = null
    ): Transaction {
        val blockData = databaseApiService.getBlockData()
        val chainId = getChainId()
        val fees = getFees(listOf(transfer), feeAsset ?: asset)

        return Transaction(
            blockData,
            listOf(transfer),
            chainId,
            transactionExpirationDelay
        ).apply {
            setFees(fees)
            addPrivateKey(privateKey)
        }
    }

}
