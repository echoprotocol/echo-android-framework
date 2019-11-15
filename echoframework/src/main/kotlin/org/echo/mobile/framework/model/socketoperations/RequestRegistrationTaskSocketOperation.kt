package org.echo.mobile.framework.model.socketoperations

import com.google.common.primitives.UnsignedLong
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.RegistrationTask

/**
 * Retrieves registration task
 *
 * @author Dmitriy Bushuev
 */
class RequestRegistrationTaskSocketOperation(
    override val apiId: Int,
    callId: Int,
    callback: Callback<RegistrationTask>
) : SocketOperation<RegistrationTask>(
    SocketMethodType.CALL,
    callId,
    RegistrationTask::class.java,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.REQUEST_REGISTRATION_TASK.key)
            add(JsonArray())
        }

    override fun fromJson(json: String): RegistrationTask? {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        if (!jsonTree.isJsonObject || jsonTree.asJsonObject.get(RESULT_KEY) == null) {
            return null
        }

        try {
            val result = jsonTree.asJsonObject.get(RESULT_KEY)!!.asJsonObject

            val blockId = result.get(BLOCK_ID_KEY).asString
            val randNum = result.get(RAND_NUM_KEY).asString
            val difficulty = result.get(DIFFICULTY_KEY).asInt

            return RegistrationTask(blockId, UnsignedLong.valueOf(randNum), difficulty)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return null
    }

    companion object {
        private const val BLOCK_ID_KEY = "block_id"
        private const val RAND_NUM_KEY = "rand_num"
        private const val DIFFICULTY_KEY = "difficulty"
    }
}
