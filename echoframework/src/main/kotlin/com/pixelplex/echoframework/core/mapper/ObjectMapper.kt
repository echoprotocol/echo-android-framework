package com.pixelplex.echoframework.core.mapper

import com.pixelplex.echoframework.model.GrapheneObject

/**
 *  Encapsulates logic, associated with data mapping to [GrapheneObject] based object
 *
 * @author Daria Pechkovskaya
 */
interface ObjectMapper<T : GrapheneObject> : Mapper<T>
