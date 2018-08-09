package com.pixelplex.echoframework.model.contract

import com.pixelplex.echoframework.model.AssetAmount
import com.pixelplex.echoframework.model.ByteSerializable
import com.pixelplex.echoframework.model.GrapheneObject
import com.pixelplex.echoframework.support.toUnsignedByteArray

/**
 * Represents contract_object from blockchain
 *
 * @author Daria Pechkovskaya
 */
class Contract @JvmOverloads constructor(
    id: String,
    var contractCode: String? = null,
    var assetAmount: AssetAmount? = null
) : GrapheneObject(id),
    ByteSerializable {

    override fun toBytes(): ByteArray = this.instance.toUnsignedByteArray()

}
