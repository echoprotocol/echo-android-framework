package com.pixelplex.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.EchoFramework
import com.pixelplex.echolib.exception.LocalException
import com.pixelplex.echolib.support.Api
import com.pixelplex.echolib.support.Settings

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val lib = EchoFramework.create(Settings.Configurator().setApis(Api.DATABASE).configure())

        lib.start(object : Callback<Any> {

            override fun onSuccess(result: Any) {
                Log.d(LOG_TAG, "Success initializing")
            }

            override fun onError(error: LocalException) {
                Log.d(LOG_TAG, "Error occurred during initialization.", error)
            }

        })
    }

    companion object {
        private val LOG_TAG = MainActivity::class.java.simpleName
    }
}
