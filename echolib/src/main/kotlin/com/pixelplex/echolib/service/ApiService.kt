package com.pixelplex.echolib.service

import com.pixelplex.echolib.support.Api

/**
 * Encapsulates login for services of blockchain API
 *
 * @author Daria Pechkovskaya
 */
interface ApiService {

    /**
     * Type of api
     */
    val api: Api
}
