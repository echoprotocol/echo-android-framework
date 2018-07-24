package com.pixelplex.echoframework.core.mapper.internal

import com.google.gson.Gson
import com.pixelplex.echoframework.core.mapper.MapperCoreComponent


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
}
