package com.pixelplex.echoframework.model.contract

import com.pixelplex.echoframework.exception.MalformedParameterException
import com.pixelplex.echoframework.support.Builder
import com.pixelplex.echoframework.support.crypto.Keccak
import com.pixelplex.echoframework.support.crypto.Parameters
import org.spongycastle.util.encoders.Hex

/**
 * Builds method of contract with parameters in SHA3 encryption
 *
 * @author Daria Pechkovskaya
 */
class ContractCodeBuilder : Builder<String> {

    private lateinit var codeConverter: ContractCodeConverter
    private lateinit var keccak: Keccak
    private var methodName: String? = null
    private val mMethodParams: ArrayList<ContractMethodParameter> = arrayListOf()

    /**
     * Sets name of contract method
     * @param methodName Name of contract method
     */
    fun setMethodName(methodName: String): ContractCodeBuilder {
        this.methodName = methodName
        return this
    }

    /**
     * Sets parameters to contract method
     * @param params List of method parameters
     */
    fun setMethodParams(params: List<ContractMethodParameter>): ContractCodeBuilder {
        mMethodParams.clear()
        mMethodParams.addAll(params as ArrayList)
        return this
    }

    override fun build(): String {
        if (methodName == null) {
            throw MalformedParameterException("Contract code requires a name of method to be set")
        }

        codeConverter = ContractCodeConverter()
        keccak = Keccak()

        var abiParams = ""
        val parameters = arrayListOf<String>()
        var methodName = methodName!!

        mMethodParams.forEach { parameter ->
            abiParams += codeConverter.convert(parameter)
            parameters.add(parameter.type)
        }

        methodName += parameters.joinToString(separator = ",", prefix = "(", postfix = ")")

        val methodHex = Hex.toHexString(methodName.toByteArray())
        val hashMethod = keccak.getHash(methodHex, Parameters.KECCAK_256).substring(0, 8)

        return hashMethod + abiParams
    }
}
