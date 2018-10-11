package org.echo.mobile.framework.sample.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.ECHO_ASSET_ID
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.Balance
import org.echo.mobile.framework.model.FullAccount
import org.echo.mobile.framework.model.HistoryResponse
import org.echo.mobile.framework.sample.R
import kotlinx.android.synthetic.main.fragment_info.*

/**
 * @author Daria Pechkovskaya
 */
class InformationFragment : BaseFragment() {

    companion object {
        fun newInstance(): InformationFragment {
            return InformationFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        tvFindCheckHistory.setOnClickListener {
            collapseAll()
            expand(lytFindCheckHistory, it as TextView)
        }

        tvBalances.setOnClickListener {
            collapseAll()
            expand(lytBalances, it as TextView)
        }

        btnFind.setOnClickListener {
            progressListener?.toggle(true)
            lib?.getAccount(
                etName.text.toString(),
                object : Callback<FullAccount> {
                    override fun onSuccess(result: FullAccount) {
                        progressListener?.toggle(false)
                        updateStatus("Account found!", true)
                    }

                    override fun onError(error: LocalException) {
                        progressListener?.toggle(false)
                        error.printStackTrace()
                        updateStatus("Error ${error.message ?: "empty"}")
                    }
                })
        }

        btnCheck.setOnClickListener {
            progressListener?.toggle(true)
            lib?.checkAccountReserved(
                etName.text.toString(),
                object : Callback<Boolean> {
                    override fun onSuccess(result: Boolean) {
                        progressListener?.toggle(false)
                        updateStatus(
                            if (result) "Account reserved!" else "Account available!", true
                        )
                    }

                    override fun onError(error: LocalException) {
                        progressListener?.toggle(false)
                        error.printStackTrace()
                        updateStatus("Error ${error.message ?: "empty"}")
                    }
                })
        }

        btnBalances.setOnClickListener {
            progressListener?.toggle(true)
            lib?.getBalance(
                etNameBalances.text.toString(),
                etAsset.text.toString(),
                object : Callback<Balance> {
                    override fun onSuccess(result: Balance) {
                        progressListener?.toggle(false)
                        updateStatus(result.toString(), true)
                    }

                    override fun onError(error: LocalException) {
                        progressListener?.toggle(false)
                        error.printStackTrace()
                        updateStatus("Error ${error.message ?: "empty"}")
                    }
                })
        }

        btnHistory.setOnClickListener {
            progressListener?.toggle(true)
            lib?.getAccountHistory(
                etName.text.toString(),
                "1.11.0",
                "1.11.0",
                10,
                object : Callback<HistoryResponse> {
                    override fun onSuccess(result: HistoryResponse) {
                        updateStatus("\n${result.transactions.joinToString(separator = ",\n")}")
                    }

                    override fun onError(error: LocalException) {
                        updateStatus(error.message ?: "")
                    }

                })

        }
    }

    private fun collapseAll() {
        collapse(lytFindCheckHistory, tvFindCheckHistory)
        collapse(lytBalances, tvBalances)
    }

    override val tvStatus: TextView?
        get() = txtStatus

    override fun clear() {
        etName.text.clear()
        etNameBalances.text.clear()
        etAsset.text.clear()
    }
}