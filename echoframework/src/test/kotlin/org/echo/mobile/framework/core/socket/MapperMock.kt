package org.echo.mobile.framework.core.socket

import org.echo.mobile.framework.core.mapper.MapperCoreComponent
import org.echo.mobile.framework.model.SocketResponse

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
