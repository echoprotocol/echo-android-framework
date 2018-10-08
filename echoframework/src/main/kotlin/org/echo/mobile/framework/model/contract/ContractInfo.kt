package org.echo.mobile.framework.model.contract

import org.echo.mobile.framework.model.GrapheneObject

/**
 * Represents info object about contract from blockchain
 *
 * @author Daria Pechkovskaya
 */
class ContractInfo(
    id: String,
    val statistics: String,
    val suicided: Boolean
) : GrapheneObject(id){

    override fun toString(): String =
            "${javaClass.simpleName}(id=$id, statistics=$statistics, suicided=$suicided)"
}


