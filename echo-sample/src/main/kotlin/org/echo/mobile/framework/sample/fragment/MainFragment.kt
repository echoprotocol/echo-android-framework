package org.echo.mobile.framework.sample.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.echo.mobile.framework.sample.R
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * @author Daria Pechkovskaya
 */
class MainFragment : BaseFragment() {

    companion object {
        fun newInstance(): MainFragment {
            return MainFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_main, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        btnAuthorization.setOnClickListener {
            replaceFragment(AuthorizationFragment.newInstance())
        }

        btnInfo.setOnClickListener {
            replaceFragment(InformationFragment.newInstance())
        }

        btnContracts.setOnClickListener {
            replaceFragment(ContractsFragment.newInstance())
        }

        btnSubscription.setOnClickListener {
            replaceFragment(SubscriptionFragment.newInstance())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        activity?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(R.id.fragmentContainer, fragment)
            ?.addToBackStack(null)
            ?.commit()
    }

    override fun clear() {}

    override val tvStatus: TextView? = null
}