package com.pixelplex.echoframework.model.operations

import com.google.common.primitives.UnsignedLong
import com.pixelplex.echoframework.BITSHARES_ASSET_ID
import com.pixelplex.echoframework.exception.MalformedOperationException
import com.pixelplex.echoframework.model.Account
import com.pixelplex.echoframework.model.Asset
import com.pixelplex.echoframework.model.AssetAmount
import com.pixelplex.echoframework.model.contract.Contract
import com.pixelplex.echoframework.support.Builder
import com.pixelplex.echoframework.support.toUnsignedLong

/**
 * Represents builder for [ContractOperation].
 * Checks required data.
 *
 * @author Daria Pechkovskaya
 */
class ContractOperationBuilder : Builder<ContractOperation> {

    private var fee: AssetAmount? = null
    private var registrar: Account? = null
    private var receiver: Contract? = null
    private var asset: Asset? = null
    private var value: UnsignedLong? = null
    private var gasPrice: UnsignedLong? = null
    private var gas: UnsignedLong? = null
    private var code: String? = null

    /**
     * Sets actual fee [AssetAmount] for operation
     * @param fee Actual fee [AssetAmount]
     */
    fun setFee(fee: AssetAmount): ContractOperationBuilder {
        this.fee = fee
        return this
    }

    /**
     * Sets account working with contract
     * @param registrar Account working with contract
     */
    fun setRegistrar(registrar: Account): ContractOperationBuilder {
        this.registrar = registrar
        return this
    }

    /**
     * Sets contract receiver id
     * @param receiverId if of contract receiver
     */
    fun setReceiver(receiverId: String): ContractOperationBuilder {
        this.receiver = Contract(receiverId)
        return this
    }

    /**
     * Sets contract receiver id
     * @param receiverId if of contract receiver
     */
    fun setReceiver(receiver: Contract): ContractOperationBuilder {
        this.receiver = receiver
        return this
    }

    /**
     * Sets asset for operation
     * @param asset Asset for operation
     */
    fun setAsset(asset: Asset): ContractOperationBuilder {
        this.asset = asset
        return this
    }

    /**
     * Sets asset for operation
     * @param assetId Id of asset for operation
     */
    fun setAsset(assetId: String): ContractOperationBuilder {
        this.asset = Asset(assetId)
        return this
    }

    /**
     * Sets pay value for contract operation
     * @param value Pay value for operation
     */
    fun setValue(value: UnsignedLong): ContractOperationBuilder {
        this.value = value
        return this
    }

    /**
     * Sets pay value for contract operation
     * @param value Pay value for operation
     */
    fun setValue(value: Long): ContractOperationBuilder {
        this.value = value.toUnsignedLong()
        return this
    }

    /**
     * Sets gas price for contract operation
     * @param gasPrice Gas price for operation
     */
    fun setGasPrice(gasPrice: UnsignedLong): ContractOperationBuilder {
        this.gasPrice = gasPrice
        return this
    }

    /**
     * Sets gas price for contract operation
     * @param gasPrice Gas price for operation
     */
    fun setGasPrice(gasPrice: Long): ContractOperationBuilder {
        this.gasPrice = gasPrice.toUnsignedLong()
        return this
    }

    /**
     * Sets gas for contract operation
     * @param gas Gas for operation
     */
    fun setGas(gas: UnsignedLong): ContractOperationBuilder {
        this.gas = gas
        return this
    }

    /**
     * Sets gas for contract operation
     * @param gas Gas for operation
     */
    fun setGas(gas: Long): ContractOperationBuilder {
        this.gas = gas.toUnsignedLong()
        return this
    }

    /**
     * Sets code for contract if [receiver] is not set
     * @param code Code for contract operation
     */
    fun setContractCode(code: String): ContractOperationBuilder {
        this.code = code
        return this
    }

    override fun build(): ContractOperation {
        checkRegistrar(registrar)
        checkContractCode(code)

        val asset = this.asset ?: Asset(BITSHARES_ASSET_ID)
        val value = this.value ?: UnsignedLong.ZERO
        val gasPrice = this.gasPrice ?: UnsignedLong.ZERO
        val gas = this.gas ?: UnsignedLong.ZERO
        val registrar = this.registrar!!
        val code = this.code!!

        return fee?.let { nullSafeFee ->
            ContractOperation(registrar, receiver, asset, value, gasPrice, gas, code, nullSafeFee)
        } ?: ContractOperation(registrar, receiver, asset, value, gasPrice, gas, code)
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
