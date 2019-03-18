package org.echo.mobile.framework.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.EchoFramework
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.sample.fragment.MainFragment
import org.echo.mobile.framework.support.Api
import org.echo.mobile.framework.support.Settings
import java.security.Signature

class MainActivity : AppCompatActivity(), ProgressListener {

    lateinit var lib: EchoFramework

    companion object {
        const val ECHO_URL = "wss://echo-devnet-node.pixelplex.io/"
        const val new_url = "ws://195.201.164.54:6311"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lib = EchoFramework.create(
            Settings.Configurator()
                .setUrl(new_url)
                .setReturnOnMainThread(true)
                .setApis(Api.DATABASE, Api.NETWORK_BROADCAST, Api.ACCOUNT_HISTORY, Api.REGISTRATION)
                .configure()
        )

        startLib()

        Signature.getInstance("NONEwithEdDSA", "EdDSA")
    }

    private fun startLib() {
        toggle(true)
        lib.start(object : Callback<Any> {

            override fun onSuccess(result: Any) {
                toggle(false)
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, MainFragment.newInstance())
                    .commit()
            }

            override fun onError(error: LocalException) {
                toggle(false)
                error.printStackTrace()
                updateStatus("Error occurred during initialization.")
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        lib.stop()
    }

    override fun toggle(enable: Boolean) {
        progress.visibility = if (enable) View.VISIBLE else View.GONE
    }

    private fun updateStatus(currStatus: String) {
        toggle(false)
        txtStatus.text = getString(R.string.status, currStatus)
    }
}

interface ProgressListener {
    fun toggle(enable: Boolean)
}
