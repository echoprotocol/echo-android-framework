package com.pixelplex.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.EchoFramework
import com.pixelplex.echolib.exception.LocalException
import com.pixelplex.echolib.model.Account
import com.pixelplex.echolib.model.Balance
import com.pixelplex.echolib.support.Api
import com.pixelplex.echolib.support.Settings
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var lib: EchoFramework

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lib = EchoFramework.create(
            Settings.Configurator()
                .setReturnOnMainThread(true)
                .setApis(Api.DATABASE)
                .configure()
        )

        startLib()

        btnLogin.setOnClickListener {
            toggleProgress(true)
            lib.login(
                etName.text.toString(),
                etPassword.text.toString(),
                object : Callback<Account> {
                    override fun onSuccess(result: Account) {
                        toggleProgress(false)
                        etName.text.clear()
                        etPassword.text.clear()
                        updateStatus("Login success!")
                    }

                    override fun onError(error: LocalException) {
                        toggleProgress(false)
                        error.printStackTrace()
                        updateStatus("Error ${error.message ?: "empty"}")
                    }
                })
        }

        btnFind.setOnClickListener {
            toggleProgress(true)
            lib.getAccount(
                etName.text.toString(),
                object : Callback<Account> {
                    override fun onSuccess(result: Account) {
                        toggleProgress(false)
                        etName.text.clear()
                        etPassword.text.clear()
                        updateStatus("Account found!")
                    }

                    override fun onError(error: LocalException) {
                        toggleProgress(false)
                        error.printStackTrace()
                        updateStatus("Error ${error.message ?: "empty"}")
                    }
                })
        }

        btnCheck.setOnClickListener {
            toggleProgress(true)
            lib.checkAccountReserved(
                etName.text.toString(),
                object : Callback<Boolean> {
                    override fun onSuccess(result: Boolean) {
                        toggleProgress(false)
                        etName.text.clear()
                        etPassword.text.clear()
                        updateStatus(if (result) "Account reserved!" else "Account available!")
                    }

                    override fun onError(error: LocalException) {
                        toggleProgress(false)
                        error.printStackTrace()
                        updateStatus("Error ${error.message ?: "empty"}")
                    }
                })
        }

        btnBalances.setOnClickListener {
            lib.getBalance(etName.text.toString(),
                etAsset.text.toString(),
                object : Callback<Balance> {
                    override fun onSuccess(result: Balance) {
                        toggleProgress(false)
                        etName.text.clear()
                        etPassword.text.clear()
                        updateStatus(result.toString())
                    }

                    override fun onError(error: LocalException) {
                        toggleProgress(false)
                        error.printStackTrace()
                        updateStatus("Error ${error.message ?: "empty"}")
                    }
                })
        }
    }

    private fun startLib() {
        toggleProgress(true)
        lib.start(object : Callback<Any> {

            override fun onSuccess(result: Any) {
                toggleProgress(false)
                btnLogin.visibility = View.VISIBLE
                btnFind.visibility = View.VISIBLE
                btnCheck.visibility = View.VISIBLE
                btnBalances.visibility = View.VISIBLE
                updateStatus("Success initializing")

            }

            override fun onError(error: LocalException) {
                toggleProgress(false)
                btnLogin.visibility = View.INVISIBLE
                btnFind.visibility = View.INVISIBLE
                btnCheck.visibility = View.INVISIBLE
                btnBalances.visibility = View.INVISIBLE
                error.printStackTrace()
                updateStatus("Error occurred during initialization.")
            }
        })
    }

    private fun toggleProgress(enable: Boolean) {
        progress.visibility = if (enable) View.VISIBLE else View.GONE
    }

    private fun updateStatus(currStatus: String) {
        txtStatus.text = getString(R.string.status, currStatus)
    }
}
