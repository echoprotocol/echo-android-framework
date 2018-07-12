package com.pixelplex.echolib.support.model

import com.pixelplex.echolib.DEFAULT_URL
import com.pixelplex.echolib.core.CryptoCoreComponent
import com.pixelplex.echolib.core.CryptoCoreComponentImpl
import com.pixelplex.echolib.core.socket.SocketMessenger
import com.pixelplex.echolib.core.socket.internal.SocketMessengerImpl

/**
 *  Settings for library initialization
 *
 * @author Daria Pechkovskaya
 */
class Settings private constructor(
    val url: String,
    val socketMessenger: SocketMessenger,
    val cryptoComponent: CryptoCoreComponent,
    val apis: Set<Api>
) {

    /**
     * Configures settings for library initialization
     */
    class Configurator {

        private var url: String? = null
        private var socketMessenger: SocketMessenger? = null
        private var cryptoComponent: CryptoCoreComponent? = null
        private var apis: Set<Api>? = null

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
         * Create settings with configurations
         * @return settings for library initialization
         */
        fun configure(): Settings {
            val url = this.url ?: DEFAULT_URL
            val socketMessenger = this.socketMessenger ?: SocketMessengerImpl()
            socketMessenger.setUrl(url)
            val cryptoComponent = this.cryptoComponent ?: CryptoCoreComponentImpl()
            val apis = apis ?: Api.values().toSet()

            return Settings(url, socketMessenger, cryptoComponent, apis)
        }
    }

}
