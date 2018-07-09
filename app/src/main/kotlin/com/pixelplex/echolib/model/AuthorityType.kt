package com.pixelplex.echolib.model

/**
 * Enum-type used to specify the different roles of an authority.
 *
 * @see [Authority](https://bitshares.org/doxygen/authority_8hpp_source.html)
 *
 * @author Dmitriy Bushuev
 */
enum class AuthorityType {
    /**
     * The key that is authorized to change owner, active, and voting keys
     */
    OWNER,

    /**
     *
    the key that is able to perform normal operations
     */
    ACTIVE,

    KEY
}
