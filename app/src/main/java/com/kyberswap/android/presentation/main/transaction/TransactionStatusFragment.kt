package com.kyberswap.android.presentation.main.transaction

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.daimajia.swipe.SwipeLayout
import com.daimajia.swipe.util.Attributes
import com.google.firebase.analytics.FirebaseAnalytics
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BuildConfig
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentTransactionStatusBinding
import com.kyberswap.android.databinding.LayoutTxSwipeTargetBinding
import com.kyberswap.android.domain.model.Transaction
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.swap.GetGasPriceState
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.FAIL_CANCEL_TX_EVENT
import com.kyberswap.android.util.TRANSACTION_CANCEL
import com.kyberswap.android.util.TRANSACTION_SPEEDUP
import com.kyberswap.android.util.USER_CLICK_SUBMIT_CANCEL_TX_EVENT
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.createEvent
import com.kyberswap.android.util.ext.toBigDecimalOrDefaultZero
import com.kyberswap.android.util.ext.toDisplayNumber
import com.takusemba.spotlight.OnSpotlightListener
import com.takusemba.spotlight.OnTargetListener
import com.takusemba.spotlight.Spotlight
import com.takusemba.spotlight.Target
import com.takusemba.spotlight.shape.Circle
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.web3j.utils.Convert
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TransactionStatusFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var binding: FragmentTransactionStatusBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null
    private var transactionType: Int = Transaction.PENDING

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var transactionStatusAdapter: TransactionStatusAdapter? = null

    @Inject
    lateinit var analytics: FirebaseAnalytics

    private val handler by lazy {
        Handler()
    }

    private val isPending: Boolean
        get() = transactionType == Transaction.PENDING

    private var isShowTutorial: Boolean = false

    private var isViewVisible: Boolean = false

    val compositeDisposable by lazy {
        CompositeDisposable()
    }

    private val emptyTransaction: String
        get() = if (isPending) getString(R.string.empty_pending_transaction) else getString(
            R.string.empty_complete_transaction
        )

    private val viewModel by lazy {
        ViewModelProvider(this, viewModelFactory)
            .get(TransactionStatusViewModel::class.java)
    }

    private var defaultGasPrice: String = 1.toString()

    @Inject
    lateinit var dialogHelper: DialogHelper

    private var spotlight: Spotlight? = null

    private val threshold =
        if (BuildConfig.FLAVOR == "dev" || BuildConfig.FLAVOR == "stg") 1f else 5f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transactionType = arguments?.getInt(TRANSACTION_TYPE) ?: Transaction.PENDING
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTransactionStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.getSelectedWallet()

        wallet?.let {
            getTransactionsByFilter()
        }

        viewModel.getSelectedWalletCallback.observe(viewLifecycleOwner, Observer { event ->
            event?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        if (wallet?.address != state.wallet.address) {
                            wallet = state.wallet
                            getTransactionsByFilter()
                        }
                        binding.wallet = wallet
                    }
                    is GetWalletState.ShowError -> {

                    }
                }
            }
        })

        setupTransactionList()



        context?.let {
            binding.swipeLayout.setColorSchemeColors(
                ContextCompat.getColor(
                    it,
                    R.color.colorAccent
                )
            )
        }
        binding.swipeLayout.setOnRefreshListener(this)

        if (isPending) {
            viewModel.getGasPrice()
            observePendingTransaction()
            binding.root.doOnPreDraw {
                binding.rvTransaction.addOnChildAttachStateChangeListener(object :
                    RecyclerView.OnChildAttachStateChangeListener {
                    override fun onChildViewAttachedToWindow(view: View) {
                        val lastItemPosition = transactionStatusAdapter?.itemCount ?: 0 - 1
                        if (lastItemPosition > 0) {
                            if (binding.rvTransaction.childCount == transactionStatusAdapter?.itemCount) {
                                binding.rvTransaction.removeOnChildAttachStateChangeListener(
                                    this
                                )
                                isViewVisible = true
                            }
                        }
                    }

                    override fun onChildViewDetachedFromWindow(view: View) {}
                })
            }
        }

        viewModel.getTransactionCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetTransactionState.Loading -> {
                        showProgress(true)
                    }
                    is GetTransactionState.Success -> {
                        if (state.isLoaded) {
                            showProgress(false)
                        }

                        if (state.isFilterChanged) {
                            transactionStatusAdapter?.submitList(null)
                        }

                        updateTransactionList(state.transactions)

                        showTutorial()
                    }
                    is GetTransactionState.ShowError -> {
                        showProgress(false)
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                    is GetTransactionState.FilterNotChange -> {
                        showProgress(false)
                        if (transactionStatusAdapter?.itemCount == 0) {
                            binding.emptyTransaction = emptyTransaction
                            spotlight?.finish()
                        } else {
                            binding.emptyTransaction = ""
                        }
                    }
                }
            }
        })

        viewModel.deleteTransactionCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {

                    is DeleteTransactionState.Success -> {
                        showAlert(getString(R.string.delete_transaction_success))
                    }
                    is DeleteTransactionState.ShowError -> {
                        showError(
                            state.message ?: getString(R.string.something_wrong)
                        )
                    }
                }
            }
        })
    }

    private fun setupTransactionList() {
        binding.rvTransaction.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )

        if (transactionStatusAdapter == null) {
            transactionStatusAdapter = TransactionStatusAdapter(appExecutors, isPending, handler, {
                when (it.transactionType) {
                    Transaction.TransactionType.SWAP ->
                        navigator.navigateToSwapTransactionScreen(
                            currentFragment,
                            wallet,
                            it
                        )
                    Transaction.TransactionType.SEND ->
                        navigator.navigateToSendTransactionScreen(
                            currentFragment,
                            wallet,
                            it
                        )
                    Transaction.TransactionType.RECEIVED ->
                        navigator.navigateToReceivedTransactionScreen(
                            currentFragment,
                            wallet,
                            it
                        )
                }

            }, {
                dialogHelper.showConfirmation(
                    getString(R.string.title_delete),
                    getString(R.string.delete_transaction_warning),
                    {
                        viewModel.deleteTransaction(it)
                    })

            }, {
                analytics.logEvent(
                    TRANSACTION_SPEEDUP,
                    Bundle().createEvent()
                )
                navigator.navigateToCustomGas(currentFragment, it)
            }, {

                analytics.logEvent(
                    TRANSACTION_CANCEL,
                    Bundle().createEvent()
                )
                val cancelGasPrice =
                    Convert.toWei(defaultGasPrice.toBigDecimalOrDefaultZero(), Convert.Unit.GWEI)
                        .max(
                            it.gasPrice.toBigDecimalOrDefaultZero() * 1.2.toBigDecimal()
                        )

                dialogHelper.showCancelTransactionDialog(it.getFeeFromWei(cancelGasPrice.toDisplayNumber())) {
                    wallet?.let { it1 ->

                        analytics.logEvent(
                            USER_CLICK_SUBMIT_CANCEL_TX_EVENT,
                            Bundle().createEvent(it.displayTransaction)
                        )
                        viewModel.cancelTransaction(
                            it1,
                            it.copy(gasPrice = cancelGasPrice.toDisplayNumber())
                        )
                    }
                }
            })
        }

        viewModel.cancelTransactionCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SpeedUpTransactionState.Loading)
                when (state) {
                    is SpeedUpTransactionState.Success -> {
                        if (!state.status) {
                            analytics.logEvent(FAIL_CANCEL_TX_EVENT, Bundle().createEvent())
                            showError(getString(R.string.can_not_cancel_transaction))
                        }
                    }
                    is SpeedUpTransactionState.ShowError -> {
                        val error = state.message ?: getString(R.string.something_wrong)
                        if (error.contains(getString(R.string.nonce_too_low), true)) {
                            showError(getString(R.string.can_not_cancel_transaction))
                        } else {
                            showError(error)
                        }
                        analytics.logEvent(
                            FAIL_CANCEL_TX_EVENT,
                            Bundle().createEvent(state.message)
                        )
                    }
                }
            }
        })

        viewModel.getGetGasPriceCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetGasPriceState.Success -> {
                        defaultGasPrice = state.gas.fast
                    }
                    is GetGasPriceState.ShowError -> {

                    }
                }
            }
        })

        viewModel.nonceObserver.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { nonce ->
                transactionStatusAdapter?.smallestNonceHash = nonce
                transactionStatusAdapter?.notifyDataSetChanged()

            }
        })

        transactionStatusAdapter?.mode = Attributes.Mode.Single
        binding.rvTransaction.adapter = transactionStatusAdapter
    }

    private fun observePendingTransaction() {
        var counter = 0L
        compositeDisposable.add(
            Observable.interval(counter, 15, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    counter = it
                    if (((counter * 15) % (threshold * 60).toLong()) == 0L) {
                        showTutorial()
                    }
                }, {
                    it.printStackTrace()
                })
        )
    }

    private fun showTutorial() {
        if (isPending && !isShowTutorial) {
            val oldestTxTimestamp = viewModel.transactionList.filter { !it.isCancel }.minBy {
                it.timeStamp
            }?.timeStamp ?: System.currentTimeMillis() / 1000

            val isShowSpotlight =
                (System.currentTimeMillis() / 1000 - oldestTxTimestamp) / 60f >= threshold
            if (isShowSpotlight && isViewVisible) {
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(
                    {
                        displaySpotLight()
                    }, 250
                )
            }
        }
    }

    private fun getTransactionsByFilter(isForceRefresh: Boolean = false) {
        wallet?.let {
            viewModel.getTransactionFilter(transactionType, it, isForceRefresh)
        }
    }

    override fun onRefresh() {
        getTransactionsByFilter(true)
    }

    private fun updateTransactionList(
        transactions: List<TransactionItem>
    ) {
        transactionStatusAdapter?.submitList(listOf())
        transactionStatusAdapter?.submitList(transactions)

        if (transactions.isEmpty()) {
            binding.emptyTransaction = emptyTransaction
            spotlight?.finish()
            if (isPending) {
                isViewVisible = false
                compositeDisposable.dispose()
            }
        } else {
            binding.emptyTransaction = ""
        }
    }


    private fun displaySpotLight() {
        val overlayTxSwipeTargetBinding =
            DataBindingUtil.inflate<LayoutTxSwipeTargetBinding>(
                LayoutInflater.from(activity), R.layout.layout_tx_swipe_target, null, false
            )

        val targets = ArrayList<Target>()
        transactionStatusAdapter?.itemCount?.let { position ->
            if (position - 1 > 0) {
                val childView =
                    binding.rvTransaction.findViewHolderForLayoutPosition(position - 1)?.itemView

                childView?.let { viewItem ->
                    val swipeLayout =
                        viewItem.findViewById<SwipeLayout>(
                            R.id.swipe
                        )
                    val location = IntArray(2)
                    childView.getLocationInWindow(location)
                    val xSwipe =
                        location[0] + viewItem.width * 3 / 4f
                    val ySwipe =
                        location[1] + viewItem.height / 2f
                    val fifthTarget = Target.Builder()
                        .setAnchor(xSwipe, ySwipe)
                        .setShape(Circle(resources.getDimension(R.dimen.tutorial_120_dp)))
                        .setOverlay(overlayTxSwipeTargetBinding.root)
                        .setOnTargetListener(object : OnTargetListener {
                            override fun onStarted() {
                                swipeLayout?.open(true)
                            }

                            override fun onEnded() {
                                swipeLayout?.close(true)
                            }
                        })
                        .build()

                    targets.add(fifthTarget)
                }
            }

        }

        // create spotlight
        spotlight = Spotlight.Builder(activity!!)
            .setBackgroundColor(R.color.color_tutorial)
            .setTargets(targets)
            .setDuration(1000L)
            .setAnimation(DecelerateInterpolator(2f))
            .setContainer(activity!!.window.decorView.findViewById(android.R.id.content))
            .setOnSpotlightListener(object : OnSpotlightListener {
                override fun onStarted() {
                    isShowTutorial = true
                    compositeDisposable.dispose()
                }

                override fun onEnded() {
                }
            })
            .build()

        spotlight?.start()

        overlayTxSwipeTargetBinding.tvNext.setOnClickListener {
            spotlight?.next()
        }
    }

    override fun showProgress(showProgress: Boolean) {
        handler.post {
            binding.swipeLayout.isRefreshing = showProgress
        }
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        compositeDisposable.dispose()
        isViewVisible = false
        super.onDestroyView()
    }

    companion object {
        private const val TRANSACTION_TYPE = "transaction_type"
        fun newInstance(type: Int) =
            TransactionStatusFragment().apply {
                arguments = Bundle().apply {
                    putInt(TRANSACTION_TYPE, type)
                }
            }
    }
}
