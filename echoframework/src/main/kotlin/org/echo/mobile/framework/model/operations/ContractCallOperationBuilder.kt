package org.echo.mobile.framework.model.operations

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.ECHO_ASSET_ID
import org.echo.mobile.framework.exception.MalformedOperationException
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.contract.Contract
import org.echo.mobile.framework.support.Builder
import org.echo.mobile.framework.support.toUnsignedLong

/**
 * Represents builder for [ContractCreateOperation].
 * Checks required data.
 *
 * @author Daria Pechkovskaya
 */
class ContractCallOperationBuilder : Builder<ContractCallOperation> {

    private var fee: AssetAmount? = null
    private var registrar: Account? = null
    private var callee: Contract? = null
    private var value: AssetAmount? = null
    private var gasPrice: UnsignedLong? = null
    private var gas: UnsignedLong? = null
    private var code: String? = null

    /**
     * Sets actual fee [AssetAmount] for operation
     * @param fee Actual fee [AssetAmount]
     */
    fun setFee(fee: AssetAmount): ContractCallOperationBuilder {
        this.fee = fee
        return this
    }

    /**
     * Sets account working with contract
     * @param registrar Account working with contract
     */
    fun setRegistrar(registrar: Account): ContractCallOperationBuilder {
        this.registrar = registrar
        return this
    }

    /**
     * Sets contract receiver id
     * @param receiverId if of contract receiver
     */
    fun setReceiver(receiverId: String): ContractCallOperationBuilder {
        this.callee = Contract(receiverId)
        return this
    }

    /**
     * Sets pay value for contract operation
     * @param value Pay value for operation
     */
    fun setValue(value: AssetAmount): ContractCallOperationBuilder {
        this.value = value
        return this
    }

    /**
     * Sets gas price for contract operation
     * @param gasPrice Gas price for operation
     */
    fun setGasPrice(gasPrice: UnsignedLong): ContractCallOperationBuilder {
        this.gasPrice = gasPrice
        return this
    }

    /**
     * Sets gas price for contract operation
     * @param gasPrice Gas price for operation
     */
    fun setGasPrice(gasPrice: Long): ContractCallOperationBuilder {
        this.gasPrice = gasPrice.toUnsignedLong()
        return this
    }

    /**
     * Sets gas for contract operation
     * @param gas Gas for operation
     */
    fun setGas(gas: UnsignedLong): ContractCallOperationBuilder {
        this.gas = gas
        return this
    }

    /**
     * Sets gas for contract operation
     * @param gas Gas for operation
     */
    fun setGas(gas: Long): ContractCallOperationBuilder {
        this.gas = gas.toUnsignedLong()
        return this
    }

    /**
     * Sets code for contract if [receiver] is not set
     * @param code Code for contract operation
     */
    fun setContractCode(code: String): ContractCallOperationBuilder {
        this.code = code
        return this
    }

    override fun build(): ContractCallOperation {
        checkRegistrar(registrar)
        checkContractCode(code)

        val value = this.value ?: AssetAmount(UnsignedLong.ZERO, Asset(ECHO_ASSET_ID))
        val gasPrice = this.gasPrice ?: UnsignedLong.ZERO
        val gas = this.gas ?: UnsignedLong.ZERO
        val registrar = this.registrar!!
        val code = this.code!!

        return fee?.let { nullSafeFee ->
            ContractCallOperation(
                registrar,
                callee!!,
                value,
                gasPrice,
                gas,
                code,
                fee = nullSafeFee
            )
        } ?: ContractCallOperation(registrar, callee!!, value, gasPrice, gas, code)
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
