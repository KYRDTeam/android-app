package com.kyberswap.android.presentation.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentBackupWalletNextBinding
import com.kyberswap.android.domain.model.Word
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.Navigator
import kotlinx.android.synthetic.main.fragment_backup_wallet.*
import javax.inject.Inject

private const val ARG_PARAM = "arg_param"

class BackupWalletFragmentNext : BaseFragment() {

    private lateinit var binding: FragmentBackupWalletNextBinding

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
        binding = FragmentBackupWalletNextBinding.inflate(inflater, container, false)
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
        binding.description = String.format(
            getString(R.string.backup_wallet_instruction),
            words.first().position,
            words.last().position
        )
    }

    companion object {
        fun newInstance(words: List<Word>) =
            BackupWalletFragmentNext().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PARAM, ArrayList(words))
                }
            }
    }
}
