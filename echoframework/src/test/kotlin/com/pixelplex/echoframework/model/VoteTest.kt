package com.pixelplex.echoframework.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test cases for [Vote]
 *
 * @author Dmitriy Bushuev
 */
class VoteTest {

    @Test
    fun parseVoteTest() {
        val voteParam = "1:2"

        val vote = Vote(voteParam)

        assertEquals(voteParam, vote.toString())
    }

}
