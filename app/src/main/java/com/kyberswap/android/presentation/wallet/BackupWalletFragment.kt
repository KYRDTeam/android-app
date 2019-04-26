package com.kyberswap.android.presentation.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.databinding.FragmentBackupWalletBinding
import com.kyberswap.android.domain.model.Word
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import kotlinx.android.synthetic.main.fragment_backup_wallet.*
import javax.inject.Inject

private const val ARG_PARAM = "arg_param"

class BackupWalletFragment : BaseFragment() {

    private lateinit var binding: FragmentBackupWalletBinding

    @Inject
    lateinit var navigator: Navigator

    private lateinit var words: List<Word>

    @Inject
    lateinit var appExecutors: AppExecutors

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        words = arguments?.getParcelableArrayList(ARG_PARAM) ?: listOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBackupWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val wordAdapter = WordAdapter(appExecutors)

        rvWordList.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )
        binding.rvWordList.adapter = wordAdapter
        wordAdapter.submitList(words)
    }

    companion object {
        fun newInstance(words: List<Word>) =
            BackupWalletFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PARAM, ArrayList(words))
                }
            }
    }
}
