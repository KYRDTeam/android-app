package com.kyberswap.android.presentation.main.swap

import android.animation.ObjectAnimator
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.CompoundButton
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jakewharton.rxbinding3.view.focusChanges
import com.jakewharton.rxbinding3.widget.checkedChanges
import com.jakewharton.rxbinding3.widget.textChanges
import com.kyberswap.android.AppExecutors
import com.kyberswap.android.BR
import com.kyberswap.android.R
import com.kyberswap.android.databinding.FragmentSwapBinding
import com.kyberswap.android.domain.SchedulerProvider
import com.kyberswap.android.domain.model.Gas
import com.kyberswap.android.domain.model.NotificationAlert
import com.kyberswap.android.domain.model.Swap
import com.kyberswap.android.domain.model.Wallet
import com.kyberswap.android.presentation.base.BaseFragment
import com.kyberswap.android.presentation.common.DEFAULT_ACCEPT_RATE_PERCENTAGE
import com.kyberswap.android.presentation.common.PendingTransactionNotification
import com.kyberswap.android.presentation.helper.DialogHelper
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.presentation.main.alert.GetAlertState
import com.kyberswap.android.presentation.splash.GetWalletState
import com.kyberswap.android.util.di.ViewModelFactory
import com.kyberswap.android.util.ext.*
import kotlinx.android.synthetic.main.fragment_swap.*
import kotlinx.android.synthetic.main.layout_expanable.*
import net.cachapa.expandablelayout.ExpandableLayout
import org.web3j.utils.Convert
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject


class SwapFragment : BaseFragment(), PendingTransactionNotification {

    private lateinit var binding: FragmentSwapBinding

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var dialogHelper: DialogHelper

    @Inject
    lateinit var appExecutors: AppExecutors

    private var wallet: Wallet? = null

    private var alertNotification: NotificationAlert? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private var sourceAmount: String? = null

    private val displaySourceAmount: String
        get() = sourceAmount.toBigDecimalOrDefaultZero().toDisplayNumber()

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SwapViewModel::class.java)
    }

    private val tokenSources by lazy {
        listOf(binding.imgTokenSource, binding.tvSource)
    }

    private val tokenDests by lazy {
        listOf(binding.imgTokenDest, binding.tvDest)
    }

    private var selectedGasFeeView: CompoundButton? = null

    private var currentFocus: EditText? = null

    private val destLock = AtomicBoolean()

    private val availableAmount: BigDecimal
        get() = binding.swap?.let {
            it.availableAmountForSwap(
                it.tokenSource.currentBalance,
                it.allETHBalanceGasLimit.toBigDecimal(),
                getSelectedGasPrice(it.gas, selectedGasFeeView?.id).toBigDecimalOrDefaultZero()
            )
 ?: BigDecimal.ZERO


    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alertNotification = arguments?.getParcelable(ALERT_PARAM)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSwapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.getSelectedWallet()
        alertNotification?.let { viewModel.getAlert(it) }

        viewModel.getSelectedWalletCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetWalletState.Success -> {
                        if (!state.wallet.isSameWallet(wallet)) {
                            this.wallet = state.wallet
                            binding.walletName = state.wallet.name
                            val promo = wallet?.promo
                            if (wallet?.isPromo == true) {
                                enableTokenSearch(isSourceToken = true, isEnable = false)
                                if (promo?.destinationToken?.isNotEmpty() == true) {
                                    enableTokenSearch(isSourceToken = false, isEnable = false)
                        
                     else {
                                enableTokenSearch(isSourceToken = true, isEnable = true)
                                enableTokenSearch(isSourceToken = false, isEnable = true)
                    

                            viewModel.getSwapData(state.wallet, alertNotification)
                

            
                    is GetWalletState.ShowError -> {

            
        
    
)

        viewModel.getSwapDataCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetSwapState.Success -> {
                        if (!state.swap.isSameTokenPair(binding.swap)) {
                            if (state.swap.tokenSource.tokenSymbol == state.swap.tokenDest.tokenSymbol) {
                                showAlertWithoutIcon(
                                    title = getString(R.string.title_unsupported),
                                    message = getString(R.string.can_not_swap_same_token)
                                )
                    

                            // Token pair change need to reset rate and get the new one
                            binding.swap = state.swap.copy(marketRate = "", expectedRate = "")
                            binding.executePendingBindings()
                            sourceAmount = state.swap.sourceAmount
                            getRate(state.swap)
                            viewModel.getGasPrice()
                
                        viewModel.getGasLimit(wallet, binding.swap)
            
                    is GetSwapState.ShowError -> {

            
        
    
)

        tvAdvanceOption.setOnClickListener {
            expandableLayout.expand()
            tvRevertNotification.text =
                getRevertNotification(rgRate.checkedRadioButtonId)

        imgClose.setOnClickListener {
            expandableLayout.collapse()


        expandableLayout.setOnExpansionUpdateListener { _, state ->
            if (state == ExpandableLayout.State.EXPANDED) {
                val animator = ObjectAnimator.ofInt(
                    binding.scView,
                    "scrollY",
                    tvRevertNotification.bottom
                )

                animator.duration = 300
                animator.interpolator = AccelerateInterpolator()
                animator.start()
    


        imgMenu.setOnClickListener {
            showDrawer(true)



        imgSwap.setOnClickListener {
            resetAmount()
            val swap = binding.swap?.swapToken()
            swap?.let {
                viewModel.saveSwap(swap)
                getRate(it)
    
            binding.setVariable(BR.swap, swap)
            binding.executePendingBindings()



        viewModel.compositeDisposable.add(
            edtSource.textChanges()
                .skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe { text ->
                    if (destLock.get() || currentFocus == binding.edtDest) {
                        return@subscribe
            
                    sourceAmount = text.toString()
                    if (text.isNullOrEmpty()) {
                        binding.edtDest.setText("")
            
                    binding.swap?.let { swap ->
                        if (swap.hasSamePair) {
                            edtDest.setText(text)
                 else {
                            edtDest.setAmount(
                                swap.getExpectedAmount(
                                    binding.tvRate.text.toString(),
                                    text.toString()
                                ).toDisplayNumber()
                            )
                            getRate(swap)
                            wallet?.let {

                                val updatedSwap = swap.copy(
                                    sourceAmount = sourceAmount ?: "",
                                    minAcceptedRatePercent =
                                    getMinAcceptedRatePercent(rgRate.checkedRadioButtonId)
                                )
                                binding.swap = updatedSwap
                                viewModel.saveSwap(updatedSwap)
                    
                
            
        )

        viewModel.compositeDisposable.add(
            binding.edtDest.textChanges()
                .skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe { dstAmount ->
                    if (destLock.get()) {
                        binding.swap?.let { swap ->

                            if ((dstAmount.toBigDecimalOrDefaultZero() * swap.tokenDest.rateEthNow) > 100.toBigDecimal()) {
                                viewModel.estimateAmount(
                                    swap.sourceSymbol, swap.destSymbol, dstAmount.toString()
                                )
                     else {
                                if (swap.rate.toDoubleOrDefaultZero() != 0.0) {

                                    val estSource = dstAmount.toBigDecimalOrDefaultZero()
                                        .divide(
                                            swap.rate.toBigDecimalOrDefaultZero(),
                                            18,
                                            RoundingMode.UP
                                        )
                                    sourceAmount = estSource.toPlainString()
                                    edtSource.setAmount(displaySourceAmount)
                                    viewModel.getExpectedRate(
                                        swap,
                                        estSource.toPlainString()
                                    )
                        
                    
                
            
        
        )


        tokenSources.forEach {
            it.setOnClickListener {
                binding.swap?.let { swap ->
                    viewModel.saveSwap(
                        swap.copy(
                            sourceAmount = sourceAmount ?: "",
                            destAmount = edtDest.text.toString(),
                            minAcceptedRatePercent = getMinAcceptedRatePercent(rgRate.checkedRadioButtonId),
                            gasPrice = getSelectedGasPrice(swap.gas, selectedGasFeeView?.id)
                        )
                    )
        
                navigator.navigateToTokenSearchFromSwapTokenScreen(
                    currentFragment,
                    wallet,
                    true
                )
    



        tokenDests.forEach {
            it.setOnClickListener {
                binding.swap?.let { swap ->
                    viewModel.saveSwap(
                        swap.copy(
                            sourceAmount = sourceAmount ?: "",
                            destAmount = edtDest.text.toString(),
                            minAcceptedRatePercent = getMinAcceptedRatePercent(rgRate.checkedRadioButtonId),
                            gasPrice = getSelectedGasPrice(swap.gas, selectedGasFeeView?.id)
                        )
                    )
        
                navigator.navigateToTokenSearchFromSwapTokenScreen(
                    currentFragment,
                    wallet,
                    false
                )
    


        binding.tvTokenBalanceValue.setOnClickListener {
            binding.swap?.let {
                if (it.tokenSource.isETH) {
                    showAlertWithoutIcon(message = getString(R.string.small_amount_of_eth_transaction_fee))
                    sourceAmount = availableAmount.toDisplayNumber()
                    binding.edtSource.setText(sourceAmount)
         else {
                    sourceAmount = it.tokenSource.currentBalance.toDisplayNumber()
                    binding.edtSource.setText(sourceAmount)
        

    


        viewModel.getExpectedRateCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetExpectedRateState.Success -> {
                        val swap =
                            if (state.list.first().toBigDecimalOrDefaultZero() > BigDecimal.ZERO) {
                                binding.swap?.copy(
                                    expectedRate = state.list[0]
                                )
                     else {
                                binding.swap?.copy(
                                    expectedRate = binding.swap?.marketRate ?: ""
                                )
                    

                        if (swap != null) {
                            if (swap != binding.swap) {
                                binding.swap = swap
                                binding.executePendingBindings()
                    

                            if (destLock.get() || currentFocus == binding.edtDest) {
                                sourceAmount = edtDest.toBigDecimalOrDefaultZero().divide(
                                    swap.expectedRate.toBigDecimalOrDefaultZero(),
                                    18,
                                    RoundingMode.UP
                                ).stripTrailingZeros().toPlainString()
                                edtSource.setAmount(
                                    displaySourceAmount
                                )
                     else {
                                edtDest.setAmount(
                                    binding.swap?.getExpectedAmount(
                                        tvRate.text.toString(),
                                        edtSource.text.toString()
                                    )?.toDisplayNumber()
                                )
                    

                            binding.tvValueInUSD.text =
                                getString(
                                    R.string.dest_balance_usd_format,
                                    binding.swap?.getExpectedDestUsdAmount(
                                        edtSource.toBigDecimalOrDefaultZero(),
                                        swap.tokenDest.rateUsdNow
                                    )?.toDisplayNumber()
                                )

                            tvRevertNotification.text =
                                getRevertNotification(rgRate.checkedRadioButtonId)


                

            
                    is GetExpectedRateState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
            
        
    
)

        viewModel.getGetMarketRateCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetMarketRateState.Success -> {
                        val swap = binding.swap?.copy(
                            marketRate = state.rate
                        )
                        tvRevertNotification.text =
                            getRevertNotification(rgRate.checkedRadioButtonId)
                        if (swap != binding.swap) {
                            binding.swap = swap
                            binding.executePendingBindings()
                
            
                    is GetMarketRateState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
            
        
    
)

        viewModel.getGetGasLimitCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetGasLimitState.Success -> {
                        val swap = binding.swap?.copy(
                            gasLimit = state.gasLimit.toString()
                        )

                        tvRevertNotification.text =
                            getRevertNotification(rgRate.checkedRadioButtonId)

                        if (swap != binding.swap) {
                            binding.swap = swap
                            binding.executePendingBindings()
                
            
                    is GetGasLimitState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
            
        
    
)

        viewModel.compositeDisposable.add(
            rbCustom.checkedChanges().skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    edtCustom.isEnabled = it
                    if (it) {
                        edtCustom.setText("3")
                        edtCustom.requestFocus()
                        edtCustom.setSelection(edtCustom.text.length)
             else {
                        edtCustom.setText("")
            

        )

        viewModel.compositeDisposable.add(
            edtCustom.textChanges()
                .skipInitialValue()
                .observeOn(schedulerProvider.ui())
                .subscribe {
                    edtCustom.isSelected = it.isNullOrEmpty() && rbCustom.isChecked

                    if (it.isNotEmpty() && it.toString().toInt() > 0 && it.toString().toInt() <= 100) {
                        tvRevertNotification.text =
                            getRevertNotification(R.id.rbCustom)
             else if (it.isNotEmpty() && it.toString().toInt() > 100) {
                        val remaining = it.dropLast(1)
                        edtCustom.setText(remaining)
                        edtCustom.setSelection(remaining.length)
            

        
        )

        viewModel.compositeDisposable.add(binding.edtDest.focusChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                destLock.set(it)
                if (it) {
                    currentFocus?.isSelected = false
                    currentFocus = edtDest
                    currentFocus?.isSelected = true
        
    )


        viewModel.compositeDisposable.add(binding.edtSource.focusChanges()
            .skipInitialValue()
            .observeOn(schedulerProvider.ui())
            .subscribe {
                if (it) {
                    currentFocus?.isSelected = false
                    currentFocus = edtSource
                    currentFocus?.isSelected = true
        
    )



        viewModel.compositeDisposable.add(
            rgRate.checkedChanges()
                .observeOn(schedulerProvider.ui())
                .subscribe { id ->
                    tvRevertNotification.text = getRevertNotification(id)
        )

        listOf(rbSuperFast, rbFast, rbRegular, rbSlow).forEach {
            it.setOnCheckedChangeListener { rb, isChecked ->
                if (isChecked) {
                    if (rb != selectedGasFeeView) {
                        selectedGasFeeView?.isChecked = false
                        rb.isSelected = true
                        selectedGasFeeView = rb
                        binding.swap?.let { swap ->
                            viewModel.saveSwap(
                                swap.copy(
                                    gasPrice = getSelectedGasPrice(
                                        swap.gas,
                                        rb.id
                                    )
                                )
                            )
                
            
        

    



        viewModel.getGetGasPriceCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetGasPriceState.Success -> {
                        val swap = binding.swap?.copy(
                            gas = state.gas
                        )
                        if (swap != binding.swap) {
                            binding.swap = swap
                            binding.executePendingBindings()
                
            
                    is GetGasPriceState.ShowError -> {

            
        
    
)

        viewModel.getCapCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == GetCapState.Loading)
                when (state) {
                    is GetCapState.Success -> {

                        if (state.cap.rich) {
                            showAlertWithoutIcon(
                                message =
                                getString(R.string.cap_rich)
                            )
                 else if (state.swap.equivalentETHWithPrecision > state.cap.cap) {
                            val amount = Convert.fromWei(state.cap.cap, Convert.Unit.ETHER)
                            showAlertWithoutIcon(
                                message = String.format(
                                    getString(R.string.cap_reduce_amount),
                                    amount.toDisplayNumber()
                                )
                            )
                 else {
                            viewModel.saveSwap(
                                state.swap, true
                            )
                


            
                    is GetCapState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
            
        
    
)

        viewModel.getAlertState.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetAlertState.Success -> {
                        dialogHelper.showAlertTriggerDialog(state.alert) {

                
            
                    is GetAlertState.ShowError -> {

            
        
    
)

        viewModel.estimateAmountState.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is EstimateAmountState.Success -> {
                        sourceAmount = state.amount
                        edtSource.setAmount(displaySourceAmount)
                        if (binding.swap != null) {
                            getRate(binding.swap!!)
                

            
                    is EstimateAmountState.ShowError -> {

            
        
    
)

        binding.imgInfo.setOnClickListener {
            showAlert(
                String.format(
                    getString(R.string.swap_rate_notification),
                    binding.swap?.ratePercentage
                        .toBigDecimalOrDefaultZero()
                        .abs()
                        .toDisplayNumber()
                ),
                R.drawable.ic_info
            )


        viewModel.saveSwapDataCallback.observe(viewLifecycleOwner, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SaveSwapState.Loading)
                when (state) {
                    is SaveSwapState.Success -> {
                        navigator.navigateToSwapConfirmationScreen(wallet)
            
        
    
)

        binding.tvContinue.setOnClickListener {
            binding.swap?.let { swap ->
                when {
                    edtSource.text.isNullOrEmpty() -> {
                        val errorAmount = getString(R.string.specify_amount)
                        showAlertWithoutIcon(
                            title = getString(R.string.invalid_input),
                            message = errorAmount
                        )
            
                    edtSource.text.toString().toBigDecimalOrDefaultZero() > swap.tokenSource.currentBalance -> {
                        val errorExceedBalance = getString(R.string.exceed_balance)
                        showAlertWithoutIcon(
                            title = getString(R.string.title_amount_too_big),
                            message = errorExceedBalance
                        )
            
                    swap.hasSamePair -> showAlertWithoutIcon(
                        title = getString(R.string.title_unsupported),
                        message = getString(R.string.can_not_swap_same_token)
                    )
                    swap.amountTooSmall(edtSource.text.toString()) -> {
                        val amountError = getString(R.string.swap_amount_small)
                        showAlertWithoutIcon(
                            title = getString(R.string.invalid_amount),
                            message = amountError
                        )
            

                    swap.copy(
                        gasPrice = getSelectedGasPrice(
                            swap.gas,
                            selectedGasFeeView?.id
                        )
                    ).insufficientEthBalance -> showAlertWithoutIcon(
                        getString(R.string.insufficient_eth),
                        getString(R.string.not_enough_eth_blance)
                    )

                    swap.tokenSource.isETH &&
                        availableAmount < edtSource.toBigDecimalOrDefaultZero() -> {
                        showAlertWithoutIcon(
                            getString(R.string.insufficient_eth),
                            getString(R.string.not_enough_eth_blance)
                        )
            

                    rbCustom.isChecked && edtCustom.text.isNullOrEmpty() -> {
                        showAlertWithoutIcon(message = getString(R.string.custom_rate_empty))

            
                    else -> wallet?.let {

                        val data = swap.copy(
                            sourceAmount = (if (sourceAmount.toBigDecimalOrDefaultZero() > BigDecimal.ZERO) sourceAmount else edtSource.text.toString())
                                ?: "",
                            destAmount = edtDest.text.toString(),
                            minAcceptedRatePercent =
                            getMinAcceptedRatePercent(rgRate.checkedRadioButtonId),
                            gasPrice = getSelectedGasPrice(swap.gas, selectedGasFeeView?.id)
                        )

                        if (!((data.tokenSource.isETH && data.tokenDest.isWETH) || (data.tokenSource.isWETH && data.tokenDest.isETH))) {
                            viewModel.getCap(it, data)
                 else {
                            viewModel.saveSwap(
                                data, true
                            )
                
            
        
    



        rbFast.isChecked = true
        rbDefaultRate.isChecked = true

    }

    fun getSelectedWallet() {
        viewModel.getSelectedWallet()
    }

    fun getSwap() {
        wallet?.let {
            viewModel.getSwapData(it)


    }


    private fun resetAmount() {
        edtSource.setText("")
        sourceAmount = ""
        edtDest.setText("")
    }

    private fun enableTokenSearch(isSourceToken: Boolean, isEnable: Boolean) {
        if (isSourceToken) {
            tokenSources.forEach {
                it.isEnabled = isEnable
    
 else {
            tokenDests.forEach {
                it.isEnabled = isEnable
    


        val colorMatrix = ColorMatrix()
        colorMatrix.setSaturation(0f)


        val image = if (isSourceToken) {
            binding.imgTokenSource
 else {
            binding.imgTokenDest


        if (isEnable) {
            colorMatrix.setSaturation(1f)
 else {
            colorMatrix.setSaturation(0.5f)

        val filter = ColorMatrixColorFilter(colorMatrix)
        image.colorFilter = filter

        binding.imgSwap.isEnabled = isEnable
    }


    private fun getRevertNotification(id: Int): String {
        return String.format(
            getString(R.string.rate_revert_notification),
            binding.tvSource.text,
            binding.tvDest.text,
            binding.swap?.rateThreshold(getMinAcceptedRatePercent(id)),
            binding.swap?.combineRate
        )
    }

    private fun getMinAcceptedRatePercent(id: Int): String {
        return when (id) {
            R.id.rbCustom -> {
                edtCustom.text.toString()
    
            else -> DEFAULT_ACCEPT_RATE_PERCENTAGE.toString()

    }

    private fun getSelectedGasPrice(gas: Gas, id: Int?): String {
        return when (id) {
            R.id.rbSuperFast -> gas.superFast
            R.id.rbRegular -> gas.standard
            R.id.rbSlow -> gas.low
            else -> gas.fast

    }

    private fun getRate(swap: Swap) {
        if (swap.hasSamePair) return
        viewModel.getMarketRate(swap)
        viewModel.getExpectedRate(
            swap,
            edtSource.getAmountOrDefaultValue()
        )
    }

    override fun onDestroyView() {
        viewModel.compositeDisposable.clear()
        super.onDestroyView()
    }

    override fun showNotification(showNotification: Boolean) {
        binding.vNotification.visibility = if (showNotification) View.VISIBLE else View.GONE
    }

    companion object {
        private const val ALERT_PARAM = "alert_param"
        fun newInstance(alert: NotificationAlert?) = SwapFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ALERT_PARAM, alert)
    

    }
}