package com.pixelplex.echoframework.model

/**
 * Represents functionality of entities that fot which
 * it is required to have both json and byte-array representation
 *
 * @author Dmitriy Bushuev
 */
interface GrapheneSerializable : ByteSerializable, JsonSerializable
