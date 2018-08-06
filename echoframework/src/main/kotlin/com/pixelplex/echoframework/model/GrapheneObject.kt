package com.pixelplex.echoframework.model

import com.google.gson.annotations.Expose

/**
 * Represents base graphene object model
 *
 * Encapsulates logic of parsing object id
 * [http://docs.bitshares.org/development/blockchain/objects.html]
 *
 * @author Dmitriy Bushuev
 */
open class GrapheneObject(
    @Expose
    protected var id: String
) {

    protected var space: Int = 0
    protected var type: Int = 0
    var instance: Long = 0

    init {
        id.split(OBJECT_ID_DELIMITER).takeIf { it.size == OBJECT_ID_PARTS_SIZE }?.let { parts ->
            this.space = parts[0].toInt()
            this.type = parts[1].toInt()
            this.instance = parts[2].toLong()
        }
    }

    /**
     * Returns a String containing the full object apiId in the form {space}.{type}.{instance}
     */
    fun getObjectId(): String = id

    /**
     * Returns the type of this object.
     * @return: Instance of the ObjectType enum.
     */
    fun getObjectType(): ObjectType? = ObjectType.get(space, type)

    companion object {
        const val KEY_ID = "id"
        private const val OBJECT_ID_DELIMITER = "."
        private const val OBJECT_ID_PARTS_SIZE = 3
    }

}
