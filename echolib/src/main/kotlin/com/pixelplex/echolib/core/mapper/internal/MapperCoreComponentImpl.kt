package com.pixelplex.echolib.core.mapper.internal

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pixelplex.echolib.core.mapper.MapperCoreComponent
import com.pixelplex.echolib.model.SocketResponseResult

/**
 * Implementation of [MapperCoreComponent]
 *
 * @author Daria Pechkovskaya
 */
class MapperCoreComponentImpl : MapperCoreComponent {

    val gson = Gson()

    override fun <T> map(json: String, type: Class<T>): T {
        return gson.fromJson<T>(json, type)
    }

    override fun <T> mapSocketResponseResult(json: String, type: Class<T>): T {
        val responseType = object : TypeToken<SocketResponseResult<T>>() {}.type
        val socketResponse = gson.fromJson<SocketResponseResult<T>>(json, responseType)

        return socketResponse.result
    }}
