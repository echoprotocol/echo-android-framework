package com.pixelplex.echoframework.core.socket

import com.pixelplex.echoframework.core.mapper.MapperCoreComponent
import com.pixelplex.echoframework.model.SocketResponse

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

}
