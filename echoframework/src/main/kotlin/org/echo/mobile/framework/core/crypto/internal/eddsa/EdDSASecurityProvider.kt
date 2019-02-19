package org.echo.mobile.framework.core.crypto.internal.eddsa

import java.security.AccessController
import java.security.PrivilegedAction
import java.security.Provider

/**
 * EdDSA algorithm based security provider
 *
 * Library implementation of key pair generator changed on local one
 *
 * @author Dmitriy Bushuev
 */
class EdDSASecurityProvider :
    Provider(PROVIDER_NAME, 0.3, "str4d EdDSA security provider wrapper") {
    init {
        AccessController.doPrivileged(PrivilegedAction<Any> {
            this@EdDSASecurityProvider.setup()
            null
        })
    }

    private fun setup() {
        this["KeyFactory.EdDSA"] = "jp.co.soramitsu.crypto.ed25519.KeyFactory"
        this["KeyPairGenerator.EdDSA"] =
            "org.echo.mobile.framework.core.crypto.internal.addsa.KeyPairGenerator"
        this["Signature.NONEwithEdDSA"] = "jp.co.soramitsu.crypto.ed25519.EdDSAEngine"
        this["Alg.Alias.KeyFactory.1.3.101.112"] = "EdDSA"
        this["Alg.Alias.KeyFactory.OID.1.3.101.112"] = "EdDSA"
        this["Alg.Alias.KeyPairGenerator.1.3.101.112"] = "EdDSA"
        this["Alg.Alias.KeyPairGenerator.OID.1.3.101.112"] = "EdDSA"
        this["Alg.Alias.Signature.1.3.101.112"] = "NONEwithEdDSA"
        this["Alg.Alias.Signature.OID.1.3.101.112"] = "NONEwithEdDSA"
    }

    companion object {
        private val serialVersionUID = 1210027906682292307L
        val PROVIDER_NAME = "EdDSA"
    }
}