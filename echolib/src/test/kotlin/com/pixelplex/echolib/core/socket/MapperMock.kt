package com.pixelplex.echolib.core.socket

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

}
