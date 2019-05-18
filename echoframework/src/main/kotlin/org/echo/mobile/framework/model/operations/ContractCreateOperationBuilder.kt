package org.echo.mobile.framework.model.operations

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.ECHO_ASSET_ID
import org.echo.mobile.framework.exception.MalformedOperationException
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.contract.Contract
import org.echo.mobile.framework.support.Builder

/**
 * Represents builder for [ContractCreateOperation].
 * Checks required data.
 *
 * @author Daria Pechkovskaya
 */
class ContractCreateOperationBuilder : Builder<ContractCreateOperation> {

    private var fee: AssetAmount? = null
    private var registrar: Account? = null
    private var receiver: Contract? = null
    private var asset: Asset? = null
    private var value: UnsignedLong? = null
    private var code: String? = null

    /**
     * Sets actual fee [AssetAmount] for operation
     * @param fee Actual fee [AssetAmount]
     */
    fun setFee(fee: AssetAmount): ContractCreateOperationBuilder {
        this.fee = fee
        return this
    }

    /**
     * Sets account working with contract
     * @param registrar Account working with contract
     */
    fun setRegistrar(registrar: Account): ContractCreateOperationBuilder {
        this.registrar = registrar
        return this
    }

    /**
     * Sets contract receiver id
     * @param receiverId if of contract receiver
     */
    fun setReceiver(receiverId: String): ContractCreateOperationBuilder {
        this.receiver = Contract(receiverId)
        return this
    }

    /**
     * Sets asset for operation
     * @param assetId Id of asset for operation
     */
    fun setAsset(assetId: String): ContractCreateOperationBuilder {
        this.asset = Asset(assetId)
        return this
    }

    /**
     * Sets pay value for contract operation
     * @param value Pay value for operation
     */
    fun setValue(value: UnsignedLong): ContractCreateOperationBuilder {
        this.value = value
        return this
    }

    /**
     * Sets code for contract if [receiver] is not set
     * @param code Code for contract operation
     */
    fun setContractCode(code: String): ContractCreateOperationBuilder {
        this.code = code
        return this
    }

    override fun build(): ContractCreateOperation {
        checkRegistrar(registrar)
        checkContractCode(code)

        val finalAsset = asset ?: Asset(ECHO_ASSET_ID)
        val value = this.value ?: UnsignedLong.ZERO
        val payedValue = AssetAmount(value, finalAsset)
        val registrar = this.registrar!!
        val code = this.code!!

        return fee?.let { nullSafeFee ->
            ContractCreateOperation(
                registrar,
                payedValue,
                code,
                fee = nullSafeFee
            )
        } ?: ContractCreateOperation(registrar, payedValue, code)
    }

    private fun checkRegistrar(account: Account?) {
        if (account == null)
            throw MalformedOperationException("This operation requires an account to be set")
    }

    private fun checkContractCode(code: String?) {
        if (code == null) {
            throw MalformedOperationException("This operation requires contract code")
        }
    }
}
