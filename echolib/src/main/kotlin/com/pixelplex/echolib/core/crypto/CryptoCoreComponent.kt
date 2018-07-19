package com.pixelplex.echolib.core.crypto

import com.pixelplex.bitcoinj.ECKey
import com.pixelplex.echolib.model.AuthorityType

/**
 * Encapsulates logic, associated with keys generation and encryption/decryption processes
 *
 * @author Dmitriy Bushuev
 */
interface CryptoCoreComponent {

    /**
     * Generates unique address string from username and password
     */
    fun getAddress(userName: String, password: String, authorityType: AuthorityType): String

    /**
     * Generates private elliptic curve key for transaction signing
     */
    fun getPrivateKey(userName: String, password: String, authorityType: AuthorityType): ECKey

}
