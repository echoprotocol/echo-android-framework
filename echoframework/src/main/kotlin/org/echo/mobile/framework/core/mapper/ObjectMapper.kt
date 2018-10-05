package org.echo.mobile.framework.core.mapper

import org.echo.mobile.framework.model.GrapheneObject

/**
 *  Encapsulates logic, associated with data mapping to [GrapheneObject] based object
 *
 * @author Daria Pechkovskaya
 */
interface ObjectMapper<T : GrapheneObject> : Mapper<T>
