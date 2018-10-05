package org.echo.mobile.framework.model

/**
 * Describes functionality of entities for which it is required
 * to have specific byte-array representation
 *
 * @author Dmitriy Bushuev
 */
interface ByteSerializable {

    /**
     * Converts entity to byte-array representation
     *
     * @return Json string representation
     */
    fun toBytes(): ByteArray

}
