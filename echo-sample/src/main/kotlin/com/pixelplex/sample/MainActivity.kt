package com.pixelplex.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.pixelplex.echoframework.AccountListener
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.EchoFramework
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.model.Account
import com.pixelplex.echoframework.model.Balance
import com.pixelplex.echoframework.model.HistoryResponse
import com.pixelplex.echoframework.support.Api
import com.pixelplex.echoframework.support.Settings
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var lib: EchoFramework

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

        btnLogin.setOnClickListener {
            toggleProgress(true)
            lib.isOwnedBy(
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

        btnChangePassword.setOnClickListener {
            toggleProgress(true)
            lib.changePassword(etName.text.toString(),
                etPassword.text.toString(),
                etNewPassword.text.toString(),
                object : Callback<Any> {
                    override fun onSuccess(result: Any) {
                        toggleProgress(false)
                        etName.text.clear()
                        etPassword.text.clear()
                        etNewPassword.text.clear()
                        updateStatus("Password changed successfully")
                    }

                    override fun onError(error: LocalException) {
                        toggleProgress(false)
                        error.printStackTrace()
                        updateStatus("Error ${error.message ?: "empty"}")
                    }
                })
        }
        btnSubscribe.setOnClickListener {
            lib.subscribeOnAccount(etName.text.toString(), object : AccountListener {
                override fun onChange(updatedAccount: Account) {
                    updateStatus(updatedAccount.toString())
                }

            })
        }

        btnUnsubscribe.setOnClickListener {
            lib.unsubscribeFromAccount(etName.text.toString(), object : Callback<Boolean> {
                override fun onSuccess(result: Boolean) {
                    updateStatus("Unsubscribe succeed")
                }

                override fun onError(error: LocalException) {
                    updateStatus("Unsubscribe failed")
                }

            })
        }

        btnUnsubscribeAll.setOnClickListener {
            lib.unsubscribeAll(object : Callback<Boolean> {
                override fun onSuccess(result: Boolean) {
                    updateStatus("Unsubscribe all succeed")
                }

                override fun onError(error: LocalException) {
                    updateStatus("Unsubscribe all failed")
                }

            })
        }

        btnHistory.setOnClickListener {
            lib.getAccountHistory(
                etName.text.toString(),
                "1.11.37878780",
                "1.11.37878741",
                10,
                "1.3.0",
                object : Callback<HistoryResponse> {
                    override fun onSuccess(result: HistoryResponse) {
                        updateStatus("Unsubscribe all succeed")
                    }

                    override fun onError(error: LocalException) {
                        updateStatus(error.message ?: "")
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
                btnChangePassword.visibility = View.VISIBLE
                btnSubscribe.visibility = View.VISIBLE
                btnUnsubscribe.visibility = View.VISIBLE
                btnUnsubscribeAll.visibility = View.VISIBLE
                btnHistory.visibility = View.VISIBLE
                updateStatus("Success initializing")
            }

            override fun onError(error: LocalException) {
                toggleProgress(false)
                btnLogin.visibility = View.INVISIBLE
                btnFind.visibility = View.INVISIBLE
                btnCheck.visibility = View.INVISIBLE
                btnBalances.visibility = View.INVISIBLE
                btnChangePassword.visibility = View.INVISIBLE
                btnSubscribe.visibility = View.INVISIBLE
                btnUnsubscribe.visibility = View.INVISIBLE
                btnUnsubscribeAll.visibility = View.INVISIBLE
                btnHistory.visibility = View.INVISIBLE
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
