package org.echo.mobile.framework.model

import com.google.common.primitives.Bytes
import com.google.gson.*
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.exception.MalformedAddressException
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.support.hexToBytes
import org.echo.mobile.framework.support.revert
import org.spongycastle.util.encoders.Hex
import java.lang.reflect.Type
import java.math.BigInteger

/**
 * Represents additional transfer operation payload
 * [Memo model details](https://dev-doc.myecho.app/structgraphene_1_1chain_1_1memo__data.html)
 *
 * @author Dmitriy Bushuev
 */
class Memo : ByteSerializable, JsonSerializable {

    var source: Address? = null
    var destination: Address? = null
    var nonce: BigInteger = BigInteger.ZERO
    var byteMessage: ByteArray? = null
    var plaintextMessage: String? = null
        get() = if (field == null) "" else field

    constructor() {
        this.source = null
        this.destination = null
        this.byteMessage = null
    }

    constructor(from: Address, to: Address, nonce: BigInteger, message: ByteArray) {
        this.source = from
        this.destination = to
        this.nonce = nonce
        this.byteMessage = message
    }

    override fun toBytes(): ByteArray {
        if (this.source == null && this.destination == null && this.byteMessage == null) {
            return byteArrayOf(0.toByte())
        } else if (this.source == null && (this.destination == null) and (this.byteMessage != null)) {
            return Bytes.concat(
                byteArrayOf(1),
                byteArrayOf(0.toByte()),
                byteArrayOf(0.toByte()),
                byteArrayOf(0.toByte()),
                byteArrayOf(this.byteMessage!!.size.toByte()),
                this.byteMessage
            )
        } else {
            val paddedNonceBytes = ByteArray(PADDED_NONCE_ARRAY_SIZE)
            val originalNonceBytes = nonce.toByteArray()
            System.arraycopy(
                originalNonceBytes,
                0,
                paddedNonceBytes,
                PADDED_NONCE_ARRAY_SIZE - originalNonceBytes.size,
                originalNonceBytes.size
            )
            val nonceBytes = paddedNonceBytes.revert()

            return Bytes.concat(
                byteArrayOf(1),
                source!!.pubKey.key,
                destination!!.pubKey.key,
                nonceBytes,
                byteArrayOf(this.byteMessage!!.size.toByte()),
                this.byteMessage
            )
        }
    }

    override fun toJsonString(): String? = Gson().toJson(toJsonObject())

    override fun toJsonObject(): JsonElement? =
        JsonObject().apply {
            addProperty(KEY_FROM, source?.toString() ?: "")
            addProperty(KEY_TO, destination?.toString() ?: "")
            addProperty(KEY_NONCE, String.format("%x", nonce))
            addProperty(KEY_MESSAGE, Hex.toHexString(byteMessage))
        }

    /**
     * Class used to deserialize a memo from json string representation
     */
    class MemoDeserializer(private val network: Network) : JsonDeserializer<Memo> {

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): Memo? {
            val jsonObject = json.asJsonObject
            val fromAddress = jsonObject.get(KEY_FROM).asString
            val toAddress = jsonObject.get(KEY_TO).asString

            // The nonce is always coming from the full node as a string containing a decimal number
            val nonce = BigInteger(jsonObject.get(KEY_NONCE).asString, NONCE_RADIX)
            val msg = jsonObject.get(KEY_MESSAGE).asString

            return try {
                Memo(
                    Address(fromAddress, network),
                    Address(toAddress, network),
                    nonce,
                    msg.hexToBytes()
                )
            } catch (e: MalformedAddressException) {
                LOGGER.log("Incorrect address: source = $fromAddress, destination = $toAddress", e)
                null
            }
        }
    }

    companion object {
        private val LOGGER = LoggerCoreComponent.create(Memo::class.java.name)

        private const val NONCE_RADIX = 10
        private const val PADDED_NONCE_ARRAY_SIZE = 8

        private const val KEY_FROM = "from"
        private const val KEY_TO = "to"
        private const val KEY_NONCE = "nonce"
        private const val KEY_MESSAGE = "message"
    }
}
