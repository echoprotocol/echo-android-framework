package com.pixelplex.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.EchoFramework
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.support.Api
import com.pixelplex.echoframework.support.Settings
import com.pixelplex.sample.fragment.MainFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), ProgressListener {

    lateinit var lib: EchoFramework

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lib = EchoFramework.create(
            Settings.Configurator()
                .setReturnOnMainThread(true)
                .setApis(Api.DATABASE, Api.NETWORK_BROADCAST, Api.ACCOUNT_HISTORY)
                .configure()
        )

        startLib()
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
