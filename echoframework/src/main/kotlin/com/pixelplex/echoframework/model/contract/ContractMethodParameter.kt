package com.pixelplex.echoframework.model.contract

import com.google.gson.annotations.Expose
import java.io.Serializable

/**
 * Model of method parameter for contract
 *
 * @author Daria Pechkovskaya
 */
class ContractMethodParameter
@JvmOverloads constructor(
    @Expose var name: String,
    @Expose var type: String,
    var value: String = ""
) : Serializable
