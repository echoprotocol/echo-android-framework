package com.pixelplex.sample.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.model.Account
import com.pixelplex.sample.R
import kotlinx.android.synthetic.main.fragment_authorization.*

/**
 * @author Daria Pechkovskaya
 */
class AuthorizationFragment : BaseFragment() {

    companion object {
        fun newInstance(): AuthorizationFragment {
            return AuthorizationFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_authorization, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btnLogin.setOnClickListener {
            progressListener?.toggle(true)
            lib?.isOwnedBy(
                etName.text.toString(),
                etPassword.text.toString(),
                object : Callback<Account> {
                    override fun onSuccess(result: Account) {
                        clear()
                        progressListener?.toggle(false)
                        updateStatus("Login success!")
                    }

                    override fun onError(error: LocalException) {
                        error.printStackTrace()
                        progressListener?.toggle(false)
                        updateStatus("Error ${error.message ?: "empty"}")
                    }
                })
        }

        btnChangePassword.setOnClickListener {
            progressListener?.toggle(true)
            lib?.changePassword(etName.text.toString(),
                etPassword.text.toString(),
                etNewPassword.text.toString(),
                object : Callback<Any> {
                    override fun onSuccess(result: Any) {
                        clear()
                        progressListener?.toggle(false)
                        updateStatus("Password changed successfully")
                    }

                    override fun onError(error: LocalException) {
                        error.printStackTrace()
                        progressListener?.toggle(false)
                        updateStatus("Error ${error.message ?: "empty"}")
                    }
                })
        }

    }

    override val tvStatus: TextView?
        get() = txtStatus

    override fun clear() {
        etName.text.clear()
        etPassword.text.clear()
        etNewPassword.text.clear()
    }

}