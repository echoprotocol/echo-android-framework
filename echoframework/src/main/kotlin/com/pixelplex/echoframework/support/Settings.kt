package com.pixelplex.echoframework.support

import com.pixelplex.echoframework.ECHO_URL
import com.pixelplex.echoframework.core.crypto.CryptoCoreComponent
import com.pixelplex.echoframework.core.crypto.internal.CryptoCoreComponentImpl
import com.pixelplex.echoframework.core.logger.internal.LoggerCoreComponent.LogLevel
import com.pixelplex.echoframework.core.socket.SocketMessenger
import com.pixelplex.echoframework.core.socket.internal.SocketMessengerImpl
import com.pixelplex.echoframework.model.network.Network
import com.pixelplex.echoframework.model.network.Testnet

/**
 * Settings for library initialization
 *
 * @author Daria Pechkovskaya
 */
class Settings private constructor(
    val url: String,
    val socketMessenger: SocketMessenger,
    val cryptoComponent: CryptoCoreComponent,
    val apis: Set<Api>,
    val returnOnMainThread: Boolean,
    val network: Network,
    val logLevel: LogLevel
) {

    /**
     * Configures settings for library initialization
     */
    class Configurator {

        private var url: String? = null
        private var socketMessenger: SocketMessenger? = null
        private var cryptoComponent: CryptoCoreComponent? = null
        private var apis: Set<Api>? = null
        private var returnOnMainThread: Boolean = false
        private var network: Network? = null
        private var logLevel: LogLevel = LogLevel.INFO

        /**
         * Defines url for socket connection.
         *
         * @param url Connection url
         */
        fun setUrl(url: String): Configurator {
            this.url = url
            return this
        }

        /**
         * Defines socket managing functionality
         *
         * @param socketMessenger
         */
        fun setSocketMessenger(socketMessenger: SocketMessenger): Configurator {
            this.socketMessenger = socketMessenger
            return this
        }

        /**
         * Define component for key generation and encryption/decryption processes
         *
         * @param cryptoComponent
         */
        fun setCryptoCoreComponent(cryptoComponent: CryptoCoreComponent): Configurator {
            this.cryptoComponent = cryptoComponent
            return this
        }

        /**
         * Defines apis, needed for connection to blockchain
         *
         * @param apis Set of apis
         */
        fun setApis(vararg apis: Api): Configurator {
            this.apis = setOf(*apis)
            return this
        }

        /**
         * Defines whether callback result should return on main thread
         *
         * @param returnOnMainThread false - result returns on library background thread,
         *                                   true - on android main thread
         */
        fun setReturnOnMainThread(returnOnMainThread: Boolean): Configurator {
            this.returnOnMainThread = returnOnMainThread
            return this
        }

        /**
         * Defines blockchain network for connection
         *
         * @param network Type of blockchain network
         */
        fun setNetworkType(network: Network): Configurator {
            this.network = network
            return this
        }

        /**
         * Defines library log level [LogLevel]
         *
         * @param logLevel Library log level
         */
        fun setLogLevel(logLevel: LogLevel): Configurator {
            this.logLevel = logLevel
            return this
        }

        /**
         * Create settings with configurations
         *
         * @return settings for library initialization
         */
        fun configure(): Settings {
            val url = this.url ?: ECHO_URL
            val socketMessenger = (this.socketMessenger ?: SocketMessengerImpl())
                .apply {
                    setUrl(url)
                }
            val network = network ?: Testnet()
            val cryptoComponent = this.cryptoComponent ?: CryptoCoreComponentImpl(network)
            val apis = apis ?: Api.values().toSet()

            return Settings(
                url,
                socketMessenger,
                cryptoComponent,
                apis,
                returnOnMainThread,
                network,
                logLevel
            )
        }
    }

}
