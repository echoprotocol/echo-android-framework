package org.echo.mobile.framework.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Represent log model in Graphene Blockchain
 *
 * @author Daria Pechkovskaya
 */
class Log(
    @Expose
    val address: String,
    @SerializedName("log")
    val calledMethodsHashes: List<String>,
    @Expose
    val data: String
)
