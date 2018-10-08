package org.echo.mobile.framework.model

import com.google.common.primitives.Bytes
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import org.echo.mobile.framework.ECHO_ASSET_ID
import org.echo.mobile.framework.support.Uint16
import org.echo.mobile.framework.support.Uint32
import org.echo.mobile.framework.support.Uint8

/**
 * Options only available for BitAssets
 * [https://dev-doc.myecho.app/asset__ops_8hpp_source.html]
 *
 * @author Dmitriy Bushuev
 */
class BitassetOptions @JvmOverloads constructor(
    @SerializedName(FEED_LIFETIME_KEY) var feedLifetimeSec: Int = -1,
    @SerializedName(MINIMUM_FEED_KEY) var minimumFeeds: Int = 0,
    @SerializedName(FORCE_SETTLEMENT_DELAY_KEY) var forceSettlementDelaySec: Int = -1,
    @SerializedName(FORCE_SETTLEMENT_OFFSET_KEY) var forceSettlementOffsetPercent: Short = -1,
    @SerializedName(MAXIMUM_FORCE_SETTLEMENT_VOLUME_KEY) var maximumForceSettlementVolume: Short = -1,
    @SerializedName(SHORT_BACKING_ASSET_KEY) var shortBackingAsset: String = ECHO_ASSET_ID
) : GrapheneSerializable {

    @Transient
    val extensions = Extensions()

    override fun toBytes(): ByteArray {
        val feedLifetimeSecBytes = Uint32.serialize(feedLifetimeSec)
        val minimumFeedsBytes = Uint8.serialize(minimumFeeds)
        val forceSettlementDelaySecBytes = Uint32.serialize(forceSettlementDelaySec)
        val forceSettlementOffsetPercentBytes = Uint16.serialize(forceSettlementOffsetPercent)
        val maximumForceSettlementVolumeBytes = Uint16.serialize(maximumForceSettlementVolume)
        val shortBackingAssetBytes = Uint8.serialize(Asset(shortBackingAsset).instance)
        val extensionsBytes = extensions.toBytes()
        return Bytes.concat(
            feedLifetimeSecBytes, minimumFeedsBytes, forceSettlementDelaySecBytes,
            forceSettlementOffsetPercentBytes, maximumForceSettlementVolumeBytes,
            shortBackingAssetBytes, extensionsBytes
        )
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
            addProperty(SHORT_BACKING_ASSET_KEY, shortBackingAsset)
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
