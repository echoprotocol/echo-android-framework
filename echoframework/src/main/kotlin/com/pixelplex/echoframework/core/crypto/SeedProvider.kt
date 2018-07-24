package com.pixelplex.echoframework.core.crypto

import com.pixelplex.echoframework.model.AuthorityType

/**
 * Encapsulates logic of seed generation from user name and password
 *
 * @author Dmitriy Bushuev
 */
interface SeedProvider {

    /**
     * Generates seed from [name] and [password]
     */
    fun provide(name: String, password: String, authorityType: AuthorityType): String

}
