package com.pixelplex.echolib.model

import com.pixelplex.echolib.support.checkTrue

/**
 * Represents vote model in graphene blockchain
 *
 * @author Dmitriy Bushuev
 */
class Vote {

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

    override fun toString(): String {
        return "$type:$instance"
    }

    companion object {
        private const val VOTE_DELIMITER = ":"
    }

}
