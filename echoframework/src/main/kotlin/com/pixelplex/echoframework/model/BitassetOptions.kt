package com.pixelplex.echoframework.model

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.pixelplex.bitcoinj.revert
import com.pixelplex.echoframework.support.toUnsignedByteArray

/**
 * Options only available for BitAssets
 * [https://dev-doc.myecho.app/asset__ops_8hpp_source.html]
 *
 * @author Dmitriy Bushuev
 */
class BitassetOptions @JvmOverloads constructor(
    var feedLifetimeSec: Int = -1,
    var minimumFeeds: Int = 0,
    var forceSettlementDelaySec: Int = -1,
    var forceSettlementOffsetPercent: Short = -1,
    var maximumForceSettlementVolume: Short = -1,
    var shortBackingAsset: Asset = Asset("1.3.0")
) : GrapheneSerializable {

    val extensions = Extensions()

    override fun toBytes(): ByteArray {
        val feedLifetimeSecBytes = feedLifetimeSec.revert()
        val minimumFeedsBytes = minimumFeeds.toByte()
        val forceSettlementDelaySecBytes = forceSettlementDelaySec.revert()
        val forceSettlementOffsetPercentBytes = forceSettlementOffsetPercent.revert()
        val maximumForceSettlementVolumeBytes = maximumForceSettlementVolume.revert()
        val shortBackingAssetBytes = shortBackingAsset.instance.toUnsignedByteArray()
        val extensionsBytes = extensions.toBytes()
        return byteArrayOf(1) + feedLifetimeSecBytes + minimumFeedsBytes + forceSettlementDelaySecBytes +
                forceSettlementOffsetPercentBytes + maximumForceSettlementVolumeBytes +
                shortBackingAssetBytes + extensionsBytes
    }

    override fun toJsonString(): String? = toJsonObject().toString()

    override fun toJsonObject(): JsonElement? =
        JsonObject().apply {
            addProperty(FEED_LIFETIME_KEY, feedLifetimeSec)
            addProperty(MINIMUM_FEED_KEY, minimumFeeds)
            addProperty(FORCE_SETTLEMENT_DELAY_KEY, forceSettlementDelaySec)
            addProperty(FORCE_SETTLEMENT_OFFSET_KEY, forceSettlementOffsetPercent)
            addProperty(MAXIMUM_FORCE_SETTLEMENT_VOLUME_KEY, maximumForceSettlementVolume)
            addProperty(MAXIMUM_FORCE_SETTLEMENT_VOLUME_KEY, maximumForceSettlementVolume)
            addProperty(SHORT_BACKING_ASSET_KEY, shortBackingAsset.getObjectId())
            add(EXTENSIONS_KEY, extensions.toJsonObject())
        }

    companion object {
        private const val FEED_LIFETIME_KEY = "feed_lifetime_sec"
        private const val MINIMUM_FEED_KEY = "minimum_feeds"
        private const val FORCE_SETTLEMENT_DELAY_KEY = "force_settlement_delay_sec"
        private const val FORCE_SETTLEMENT_OFFSET_KEY = "force_settlement_offset_percent"
        private const val MAXIMUM_FORCE_SETTLEMENT_VOLUME_KEY = "maximum_force_settlement_volume"
        private const val SHORT_BACKING_ASSET_KEY = "short_backing_asset"
        private const val EXTENSIONS_KEY = "extensions"
    }
}
