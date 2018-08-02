package com.pixelplex.echoframework.model

/**
 * Represents account model in Graphene blockchain
 * [https://bitshares.org/doxygen/classgraphene_1_1chain_1_1operation__history__object.html]
 *
 * @author Daria Pechkovskaya
 */
class HistoryResult(
    val type: Int,
    val objectId: String
)
