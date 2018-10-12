package org.echo.mobile.framework.model.contract

import org.echo.mobile.framework.exception.MalformedParameterException
import org.echo.mobile.framework.support.Builder
import org.echo.mobile.framework.support.crypto.Keccak
import org.echo.mobile.framework.support.crypto.Parameter
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
        mMethodParams.addAll(params)
        return this
    }

    override fun build(): String {
        if (methodName == null) {
            throw MalformedParameterException("Contract code requires a name of method to be set")
        }

        codeConverter = ContractCodeConverter(mMethodParams.size)
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
        val hashMethod = keccak.getHash(methodHex, Parameter.KECCAK_256).substring(0, 8)

        return hashMethod + abiParams + appendStringParameters() + appendArrayParameters()
    }

    private fun appendStringParameters(): String {
        var stringParams = ""
        mMethodParams.forEach { param ->
            if (param.type.contains(ContractMethodParameter.TYPE_STRING)) {
                stringParams += codeConverter.appendStringPattern(param.value)
            }
        }

        return stringParams
    }

    private fun appendArrayParameters(): String {
        var stringParams = ""
        mMethodParams.forEach { param ->
            if (codeConverter.parameterIsArray(param)) {
                stringParams += codeConverter.appendArrayParameter(param)
            }
        }

        return stringParams
    }

}
