package com.pixelplex.echoframework.model.network

import com.pixelplex.echoframework.model.Address

/**
 * Contains information about blockchain test network
 *
 * @author Daria Pechkovskaya
 */
class Testnet : Network(Address.TESTNET_PREFIX)
