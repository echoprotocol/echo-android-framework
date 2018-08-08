package com.pixelplex.echoframework.model.contract

import com.pixelplex.echoframework.model.GrapheneObject

/**
 * Represents info about contract from blockchain
 *
 * @author Daria Pechkovskaya
 */
class ContractInfo(
    id: String,
    val statistics: String,
    val suicided: Boolean
) : GrapheneObject(id)
