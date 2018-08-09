package com.pixelplex.sample.fragment

import android.content.Context
import android.support.v4.app.Fragment
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.pixelplex.echoframework.EchoFramework
import com.pixelplex.sample.MainActivity
import com.pixelplex.sample.ProgressListener
import com.pixelplex.sample.R

/**
 * @author Daria Pechkovskaya
 */
abstract class BaseFragment : Fragment() {

    protected var lib: EchoFramework? = null
    protected var progressListener: ProgressListener? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is MainActivity) {
            lib = context.lib
            progressListener = context
        } else {
            Toast.makeText(context, "Library was not initialize!", Toast.LENGTH_SHORT).show()
        }
    }

    protected fun updateStatus(currStatus: String, clear: Boolean = false) {
        if (clear) clear()
        progressListener?.toggle(false)
        tvStatus?.text = getString(R.string.status, currStatus)
    }

    protected fun expand(v: LinearLayout, tv: TextView) {
        toggleArrow(tv, true)
        v.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        v.requestLayout()
    }

    protected fun collapse(v: LinearLayout, tv: TextView) {
        toggleArrow(tv, false)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.height = 0
        v.layoutParams = params
        v.requestLayout()
    }

    protected fun toggleArrow(tv: TextView, isExpand: Boolean) {
        val drawableId =
            if (isExpand) R.drawable.ic_arrow_drop_up else R.drawable.ic_arrow_drop_down
        tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableId, 0)
    }

    protected abstract val tvStatus: TextView?

    protected abstract fun clear()

    override fun onDetach() {
        super.onDetach()
        lib = null
        progressListener = null
    }
}