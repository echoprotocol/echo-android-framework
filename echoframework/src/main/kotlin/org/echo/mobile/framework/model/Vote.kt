package org.echo.mobile.framework.model

import org.echo.mobile.framework.support.checkTrue

/**
 * Represents vote model in graphene blockchain
 *
 * @author Dmitriy Bushuev
 */
class Vote : ByteSerializable {

    private var type: Int = 0
    private var instance: Int = 0

    constructor(vote: String) {
        val parts = parseVote(vote)
        this.type = Integer.valueOf(parts[0])
        this.instance = Integer.valueOf(parts[1])
    }

    constructor(type: Int, instance: Int) {
        this.type = type
        this.instance = instance
    }

    private fun parseVote(vote: String): List<String> {
        val parts = vote.split(VOTE_DELIMITER)
        checkTrue(parts.size == 2, "Invalid vote string: $vote")

        return parts
    }

    override fun toString(): String = "$type:$instance"

    override fun toBytes(): ByteArray {
        return byteArrayOf(instance.toByte(), type.toByte())
    }

    companion object {
        private const val VOTE_DELIMITER = ":"
    }

}
