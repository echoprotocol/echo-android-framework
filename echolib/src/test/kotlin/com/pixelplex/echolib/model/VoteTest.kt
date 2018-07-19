package com.pixelplex.echolib.model

import org.junit.Assert
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

        Assert.assertEquals(voteParam, vote.toString())
    }

}
