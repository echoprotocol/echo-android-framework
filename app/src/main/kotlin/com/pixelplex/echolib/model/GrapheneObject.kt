package com.pixelplex.echolib.model

import com.google.gson.annotations.Expose

/**
 * Represents base graphene object model
 *
 * <p>
 *     Encapsulates logic of parsing object id
 *     (@see http://docs.bitshares.org/development/blockchain/objects.html)
 * </p>
 *
 * @author Dmitriy Bushuev
 */
open class GrapheneObject(
    @Expose
    protected var id: String
) {

    protected var space: Int = 0
    protected var type: Int = 0
    protected var instance: Long = 0

    init {
        id.split(OBJECT_ID_DELIMITER).takeIf { it.size == OBJECT_ID_PARTS_SIZE }?.let { parts ->
            this.space = Integer.parseInt(parts[0])
            this.type = Integer.parseInt(parts[1])
            this.instance = java.lang.Long.parseLong(parts[2])
        }
    }

    /**
     * Returns a String containing the full object apiId in the form {space}.{type}.{instance}
     */
    fun getObjectId(): String = "$space.$type.$instance"

    companion object {
        const val KEY_ID = "id"
        private const val OBJECT_ID_DELIMITER = "."
        private const val OBJECT_ID_PARTS_SIZE = 3
    }

}
