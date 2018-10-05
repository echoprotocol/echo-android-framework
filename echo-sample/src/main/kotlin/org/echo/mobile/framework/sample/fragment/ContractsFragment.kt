package org.echo.mobile.framework.sample.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.ECHO_ASSET_ID
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.contract.ContractInfo
import org.echo.mobile.framework.model.contract.ContractMethodParameter
import org.echo.mobile.framework.model.contract.ContractResult
import org.echo.mobile.framework.sample.R
import kotlinx.android.synthetic.main.fragment_contracts.*
import java.util.regex.Pattern


/**
 * @author Daria Pechkovskaya
 */
class ContractsFragment : BaseFragment() {

    companion object {
        fun newInstance(): ContractsFragment {
            return ContractsFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_contracts, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        tvCreateContract.setOnClickListener {
            collapseAll()
            expand(lytContractCreation, it as TextView)
        }

        tvCallContract.setOnClickListener {
            collapseAll()
            expand(lytCallContract, it as TextView)
        }

        tvQueryContract.setOnClickListener {
            collapseAll()
            expand(lytQueryContract, it as TextView)
        }

        tvResultContract.setOnClickListener {
            collapseAll()
            expand(lytResultContract, it as TextView)
        }

        tvContracts.setOnClickListener {
            collapseAll()
            expand(lytContracts, it as TextView)
        }

        btnCreateContract.setOnClickListener {
            progressListener?.toggle(true)
            lib?.createContract(
                etName.text.toString(),
                etPassword.text.toString(),
                ECHO_ASSET_ID,
                etBytecode.text.toString(),
                object : Callback<Boolean> {
                    override fun onSuccess(result: Boolean) {
                        updateStatus("Contract creation succeed", true)
                    }

                    override fun onError(error: LocalException) {
                        error.printStackTrace()
                        updateStatus(error.message ?: "")
                    }

                })

        }

        btnCallContract.setOnClickListener {
            progressListener?.toggle(true)
            lib?.callContract(
                etNameCall.text.toString(),
                etPasswordCall.text.toString(),
                ECHO_ASSET_ID,
                etContractId.text.toString(),
                etMethodName.text.toString(),
                parseContractParams(etMethodParams.text.toString()),
                object : Callback<Boolean> {
                    override fun onSuccess(result: Boolean) {
                        updateStatus("Contract called succeed", true)
                    }

                    override fun onError(error: LocalException) {
                        error.printStackTrace()
                        updateStatus(error.message ?: "")
                    }
                }
            )
        }

        btnQueryContract.setOnClickListener {
            progressListener?.toggle(true)
            lib?.queryContract(
                etNameQuery.text.toString(),
                ECHO_ASSET_ID,
                etContractQueryId.text.toString(),
                etMethodNameQuery.text.toString(),
                parseContractParams(etMethodParamsQuery.text.toString()),
                object : Callback<String> {
                    override fun onSuccess(result: String) {
                        updateStatus(result, true)
                    }

                    override fun onError(error: LocalException) {
                        error.printStackTrace()
                        updateStatus(error.message ?: "")
                    }
                }
            )
        }

        btnContractResult.setOnClickListener {
            progressListener?.toggle(true)
            lib?.getContractResult(
                etOperationId.text.toString(),
                object : Callback<ContractResult> {
                    override fun onSuccess(result: ContractResult) {
                        updateStatus(
                            "expected: ${result.execRes.excepted}, " +
                                    "output: ${result.execRes.output}", true
                        )
                    }

                    override fun onError(error: LocalException) {
                        error.printStackTrace()
                        updateStatus(error.message ?: "")
                    }
                }
            )
        }

        btnContracts.setOnClickListener {
            progressListener?.toggle(true)
            lib?.getContracts(
                parseContractIds(etContractsIds.text.toString()),
                object : Callback<List<ContractInfo>> {
                    override fun onSuccess(result: List<ContractInfo>) {
                        updateStatus(result.joinToString(), true)
                    }

                    override fun onError(error: LocalException) {
                        error.printStackTrace()
                        updateStatus(error.message ?: "")
                    }
                }
            )
        }
    }

    private fun collapseAll() {
        collapse(lytContractCreation, tvCreateContract)
        collapse(lytCallContract, tvCallContract)
        collapse(lytQueryContract, tvQueryContract)
        collapse(lytResultContract, tvResultContract)
        collapse(lytContracts, tvContracts)
    }

    private fun parseContractParams(params: String): List<ContractMethodParameter> {
        if (params.isEmpty()) return listOf()

        val paramsList = params.trim().split(",").map { item -> item.trim() }
        val contractParams = arrayListOf<ContractMethodParameter>()

        val pattern = Pattern.compile("(.+?) (.+?) = (.+?)$")

        paramsList.forEach {
            val matcher = pattern.matcher(it)
            if (matcher.find()) {
                val type = matcher.group(1)
                val name = matcher.group(2)
                val value = matcher.group(3)
                contractParams.add(ContractMethodParameter(name, type, value))
            } else {
                Toast.makeText(context, "Incorrect params format!", Toast.LENGTH_SHORT).show()
            }
        }
        return contractParams
    }

    private fun parseContractIds(params: String): List<String> =
        params.trim().split(",").map { item -> item.trim() }

    override val tvStatus: TextView?
        get() = txtStatus

    override fun clear() {
        etName.text.clear()
        etPassword.text.clear()
        etBytecode.text.clear()
        etContractId.text.clear()
        etMethodName.text.clear()
        etMethodParams.text.clear()
        etName.text.clear()
        etContractsIds.text.clear()
    }

}