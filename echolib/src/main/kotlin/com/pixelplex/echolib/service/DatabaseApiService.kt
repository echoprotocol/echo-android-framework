package com.pixelplex.echolib.service

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.ILLEGAL_ID
import com.pixelplex.echolib.model.FullUserAccount

/**
 * Encapsulates logic, associated with blockchain database API
 *
 * <p>
 *     Graphene blockchain database API:
 *     http://docs.bitshares.org/api/database.html
 * </p>
 *
 * @author Dmitriy Bushuev
 */
interface DatabaseApiService : ApiService, AccountsService {


    companion object {
        /**
         * Actual id for DatabaseApi
         */
        @Volatile
        var id: Int = ILLEGAL_ID
    }
}


interface AccountsService {
    fun getFullAccounts(
        namesOrIds: List<String>,
        subscribe: Boolean,
        callback: Callback<List<FullUserAccount>>
    )
}
