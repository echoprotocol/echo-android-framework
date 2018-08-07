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
            toggleProgress(true)
            lib.subscribeOnAccount(etName.text.toString(), object : AccountListener {
                override fun onChange(updatedAccount: Account) {
                    updateStatus(updatedAccount.toString())
                }

            })
        }

        btnUnsubscribe.setOnClickListener {
            toggleProgress(true)
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
            toggleProgress(true)
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
            toggleProgress(true)
            lib.getAccountHistory(
                etName.text.toString(),
                "1.11.0",
                "1.11.0",
                10,
                "1.3.0",
                object : Callback<HistoryResponse> {
                    override fun onSuccess(result: HistoryResponse) {
                        updateStatus(result.transactions.joinToString())
                    }

                    override fun onError(error: LocalException) {
                        updateStatus(error.message ?: "")
                    }

                })

        }

        btnCreateContract.setOnClickListener {
            toggleProgress(true)
            lib.createContract(
                etName.text.toString(),
                etPassword.text.toString(),
                "1.3.0",
                "608060405234801561001057600080fd5b506101a2806100206000396000f30060806040" +
                        "5260043610610041576000357c01000000000000000000000000000000000000000000000" +
                        "00000000000900463ffffffff1680630775107014610046575b600080fd5b348015610052" +
                        "57600080fd5b5061005b61005d565b005b60405180807f312e322e35206c69666574696d6" +
                        "55f72656665727265725f6665655f7065726381526020017f656e74616765000000000000" +
                        "0000000000000000000000000000000000000000815250602601905060405180910390bb6" +
                        "00090805190602001906100ce9291906100d1565b50565b82805460018160011615610100" +
                        "0203166002900490600052602060002090601f016020900481019282601f1061011257805" +
                        "160ff1916838001178555610140565b82800160010185558215610140579182015b828111" +
                        "1561013f578251825591602001919060010190610124565b5b50905061014d91906101515" +
                        "65b5090565b61017391905b8082111561016f576000816000905550600101610157565b50" +
                        "90565b905600a165627a7a72305820f15a07ca60484fc3690bf46c388f8330643974e1892" +
                        "5d812c5a73ba93e5c9e400029",
                object : Callback<Boolean> {
                    override fun onSuccess(result: Boolean) {
                        updateStatus("Contract creation succeed")
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
                btnCreateContract.visibility = View.VISIBLE
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
                btnCreateContract.visibility = View.INVISIBLE
                error.printStackTrace()
                updateStatus("Error occurred during initialization.")
            }
        })
    }

    private fun toggleProgress(enable: Boolean) {
        progress.visibility = if (enable) View.VISIBLE else View.GONE
    }

    private fun updateStatus(currStatus: String) {
        toggleProgress(false)
        txtStatus.text = getString(R.string.status, currStatus)
    }
}
