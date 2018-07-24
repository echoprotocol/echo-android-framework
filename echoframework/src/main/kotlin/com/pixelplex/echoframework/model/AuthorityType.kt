package com.pixelplex.echoframework.model

/**
 * Enum-type used to specify the different roles of an authority.
 *
 * @see [Authority](https://bitshares.org/doxygen/authority_8hpp_source.html)
 *
 * @author Dmitriy Bushuev
 */
enum class AuthorityType {
    /**
     * Key that is authorized to change owner, active, and voting keys
     */
    OWNER,

    /**
     * Hey that is able to perform normal operations
     */
    ACTIVE,

    KEY
}
