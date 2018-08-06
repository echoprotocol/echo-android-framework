package com.pixelplex.echoframework.model

import com.pixelplex.echoframework.support.toUnsignedByteArray

/**
 * @author Daria Pechkovskaya
 */
class Contract @JvmOverloads constructor(
    id: String,
    var contractCode: String? = null,
    var assetAmount: AssetAmount? = null
) : GrapheneObject(id), ByteSerializable {

    override fun toBytes(): ByteArray = this.instance.toUnsignedByteArray()

}
