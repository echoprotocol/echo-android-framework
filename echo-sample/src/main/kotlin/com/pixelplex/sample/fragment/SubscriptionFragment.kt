package com.pixelplex.sample.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.pixelplex.echoframework.AccountListener
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.model.Account
import com.pixelplex.sample.R
import kotlinx.android.synthetic.main.fragment_subscription.*

/**
 * @author Daria Pechkovskaya
 */
class SubscriptionFragment : BaseFragment() {

    companion object {
        fun newInstance(): SubscriptionFragment {
            return SubscriptionFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_subscription, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btnSubscribe.setOnClickListener {
            progressListener?.toggle(true)
            lib?.subscribeOnAccount(etName.text.toString(), object : AccountListener {
                override fun onChange(updatedAccount: Account) {
                    progressListener?.toggle(false)
                    updateStatus(updatedAccount.toString())
                }

            })
        }

        btnUnsubscribe.setOnClickListener {
            progressListener?.toggle(true)
            lib?.unsubscribeFromAccount(etName.text.toString(), object : Callback<Boolean> {
                override fun onSuccess(result: Boolean) {
                    progressListener?.toggle(false)
                    updateStatus("Unsubscribe succeed")
                }

                override fun onError(error: LocalException) {
                    error.printStackTrace()
                    progressListener?.toggle(false)
                    updateStatus("Unsubscribe failed")
                }

            })
        }

        btnUnsubscribeAll.setOnClickListener {
            progressListener?.toggle(true)
            lib?.unsubscribeAll(object : Callback<Boolean> {
                override fun onSuccess(result: Boolean) {
                    progressListener?.toggle(false)
                    updateStatus("Unsubscribe all succeed")
                }

                override fun onError(error: LocalException) {
                    error.printStackTrace()
                    progressListener?.toggle(false)
                    updateStatus("Unsubscribe all failed")
                }

            })
        }
    }

    override val tvStatus: TextView?
        get() = txtStatus

    override fun clear() {
        etName.text.clear()
    }
}