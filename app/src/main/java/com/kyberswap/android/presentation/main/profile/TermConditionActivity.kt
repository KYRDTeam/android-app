package com.kyberswap.android.presentation.main.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityTermAndConditionBinding
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.helper.Navigator
import es.voghdev.pdfviewpager.library.RemotePDFViewPager
import es.voghdev.pdfviewpager.library.adapter.PDFPagerAdapter
import es.voghdev.pdfviewpager.library.remote.DownloadFile
import es.voghdev.pdfviewpager.library.util.FileUtil
import javax.inject.Inject

class TermConditionActivity : BaseActivity(), DownloadFile.Listener {

    @Inject
    lateinit var navigator: Navigator

    private lateinit var adapter: PDFPagerAdapter

    private lateinit var remotePDFViewPager: RemotePDFViewPager


    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityTermAndConditionBinding>(
            this,
            R.layout.activity_term_and_condition
        )
    }


    override fun onFailure(e: Exception?) {
        e?.printStackTrace()
    }

    override fun onProgressUpdate(progress: Int, total: Int) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        remotePDFViewPager =
            RemotePDFViewPager(this, "https://files.kyberswap.com/tac.pdf", this)
    }

    override fun onSuccess(url: String?, destinationPath: String?) {
        adapter = PDFPagerAdapter(this, FileUtil.extractFileNameFromURL(url))
        remotePDFViewPager.adapter = adapter
        updateLayout()
    }

    private fun updateLayout() {
        val root = binding.flRoot
        root.removeAllViewsInLayout()
        root.addView(
            remotePDFViewPager,
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT
        )
    }


    companion object {
        fun newIntent(context: Context) =
            Intent(context, TermConditionActivity::class.java)
    }
}