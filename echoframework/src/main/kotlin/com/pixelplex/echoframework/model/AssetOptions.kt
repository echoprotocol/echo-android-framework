package com.pixelplex.echoframework.model

import com.google.common.primitives.UnsignedLong

/**
 * Contains options available on all assets in the network
 * [AssetOptions details](https://dev-doc.myecho.app/structgraphene_1_1chain_1_1asset__options.html)
 *
 * @author Dmitriy Bushuev
 */
class AssetOptions {

    var maxSupply: UnsignedLong? = null

    var marketFeePercent: Float = 0.toFloat()

    var maxMarketFee: UnsignedLong? = null

    var issuerPermissions: Long = 0

    var flags: Int = 0

    var coreExchangeRate: Price? = null

    var description: String? = null

}
