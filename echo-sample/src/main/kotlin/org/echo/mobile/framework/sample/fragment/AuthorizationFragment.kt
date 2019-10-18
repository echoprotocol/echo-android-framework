package org.echo.mobile.framework.sample.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_authorization.*
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.FullAccount
import org.echo.mobile.framework.sample.R

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
                object : Callback<FullAccount> {
                    override fun onSuccess(result: FullAccount) {
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
            lib?.changeKeys(etName.text.toString(),
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