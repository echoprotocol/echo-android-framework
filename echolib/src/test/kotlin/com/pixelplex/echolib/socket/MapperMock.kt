package com.pixelplex.echolib.socket

import com.pixelplex.echolib.core.mapper.MapperCoreComponent
import com.pixelplex.echolib.model.SocketResponse

/**
 * Mock for [MapperCoreComponent]
 *
 * <p>
 *     Doesn't parse results.
 *     Expects int in [map] function
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class MapperMock : MapperCoreComponent {

    override fun <T> map(json: String, type: Class<T>): T =
        SocketResponse(json.toInt(), null) as T

    override fun <T> mapSocketResponseResult(json: String, type: Class<T>): T = json as T

}
