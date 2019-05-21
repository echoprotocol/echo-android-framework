package org.echo.mobile.framework.model

/**
 * Enum-type used to specify the different roles of an authority.
 *
 * [Authority details](https://dev-doc.myecho.app/structgraphene_1_1chain_1_1authority.html)
 *
 * @author Dmitriy Bushuev
 */
enum class AuthorityType {

    /**
     * Key that is able to perform normal operations
     */
    ACTIVE,

    /**
     * Key that is able to encrypt\decrypt transaction memo
     */
    MEMO
}
