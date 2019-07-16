package com.kyberswap.android.presentation.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.andrognito.pinlockview.PinLockListener
import com.kyberswap.android.KyberSwapApplication
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivityPassCodeLockBinding
import com.kyberswap.android.domain.model.PassCode
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class PassCodeLockActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(PassCodeLockViewModel::class.java)
    }

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityPassCodeLockBinding>(
            this,
            R.layout.activity_pass_code_lock
        )
    }

    private val newPinTitle by lazy {
        getString(R.string.pl_set_new_pin)
    }

    private val newPinContent by lazy {
        getString(R.string.pl_access_wallet)
    }

    private val repeatTitle by lazy {
        getString(R.string.pl_repeat_title)
    }

    private val repeatContent by lazy {
        getString(R.string.pl_repeat_content)
    }

    private val verifyAccess by lazy {
        getString(R.string.pl_verify_access)
    }

    private var remainNum = MAX_NUMBER_INPUT

    private var passCode: PassCode? = null

    var type: Int = PASS_CODE_LOCK_TYPE_VERIFY

    private var currentTimePassed: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = intent.getIntExtra(TYPE_PARAM, PASS_CODE_LOCK_TYPE_VERIFY)
        viewModel.getPin()
        binding.pinLockView.attachIndicatorDots(binding.indicatorDots)
        binding.pinLockView.setPinLockListener(object : PinLockListener {
            override fun onComplete(pin: String) {
                if (binding.title == repeatTitle || binding.title == verifyAccess) {
                    Timber.e(remainNum.toString())
                    viewModel.verifyPin(pin, remainNum, System.currentTimeMillis())
         else {
                    viewModel.save(pin)
        
    

            override fun onEmpty() {
    

            override fun onPinChange(pinLength: Int, intermediatePin: String) {

    

)

        binding.pinLockView.pinLength = 6
        binding.pinLockView.enableLayoutShuffling()

        viewModel.savePinCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == SavePinState.Loading)
                when (state) {
                    is SavePinState.Success -> {
                        binding.pinLockView.resetPinLockView()
                        binding.title = repeatTitle
                        binding.content = repeatContent
            
                    is SavePinState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
            
        
    
)

        viewModel.verifyPinCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == VerifyPinState.Loading)
                when (state) {
                    is VerifyPinState.Success -> {
                        if (state.verifyStatus.success) {
                            if (type == PASS_CODE_LOCK_TYPE_CHANGE) {
                                type = PASS_CODE_LOCK_TYPE_VERIFY
                                binding.pinLockView.resetPinLockView()
                                binding.title = newPinTitle
                                binding.content = newPinContent
                                binding.executePendingBindings()

                     else {
                                (applicationContext as KyberSwapApplication).startCounter()
                                finish()
                    
                 else {
                            val shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake)
                            binding.indicatorDots.startAnimation(shakeAnimation)

                            remainNum -= 1
                            if (remainNum > 0) {
                                binding.content =
                                    String.format(getString(R.string.number_of_attempt), remainNum)
                     else {
                                binding.content = ""
                                startCounter()
                    
                            binding.pinLockView.resetPinLockView()
                
            
                    is VerifyPinState.ShowError -> {
                        showAlert(
                            state.message ?: getString(R.string.something_wrong),
                            R.drawable.ic_info_error
                        )
                        binding.title = newPinTitle
                        binding.content = newPinContent
            
        
    
)

        viewModel.getPinCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                showProgress(state == GetPinState.Loading)
                when (state) {
                    is GetPinState.Success -> {
                        if (passCode != state.passCode) {
                            passCode = state.passCode
                            setupInitialView()
                            if (state.passCode.digest.isEmpty()) {
                                binding.title = newPinTitle
                                binding.content = newPinContent
                                binding.executePendingBindings()
                     else {
                                binding.title = verifyAccess
                    
                
            
                    is GetPinState.ShowError -> {
                        binding.title = newPinTitle
                        binding.content = newPinContent
            
        
    
)
    }

    private fun startCounter() {
        binding.pinLockView.enableInput(false)
        viewModel.compositeDisposable.clear()
        viewModel.compositeDisposable.add(
            Observable.interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { interval ->
                    val currentWaitingTime = 60 - interval - currentTimePassed
                    if (currentWaitingTime > 0) {
                        binding.content =
                            String.format(
                                getString(R.string.too_many_attempt),
                                currentWaitingTime.toString()
                            )
             else {
                        remainNum = MAX_NUMBER_INPUT
                        currentTimePassed = 0
                        binding.pinLockView.enableInput(true)
                        binding.content = ""
                        viewModel.compositeDisposable.clear()
            
        
        )
    }

    private fun setupInitialView() {
        passCode?.let {
            if (it.remainNum > 1) {
                remainNum = it.remainNum - 1
                binding.content = String.format(getString(R.string.number_of_attempt), remainNum)
     else if (it.remainNum > 0) {
                if (it.time > 0) {
                    currentTimePassed = (System.currentTimeMillis() - it.time) / 1000
                    startCounter()
        
    


    }

    override fun onBackPressed() {
        finishAffinity()
        super.onBackPressed()
    }

    override fun onDestroy() {
        viewModel.onCleared()
        super.onDestroy()
    }

    companion object {
        private const val MAX_NUMBER_INPUT = 5
        const val PASS_CODE_LOCK_TYPE_VERIFY = 0
        const val PASS_CODE_LOCK_TYPE_CHANGE = 1
        private const val TYPE_PARAM = "type_param"
        fun newIntent(context: Context, type: Int = PASS_CODE_LOCK_TYPE_VERIFY) =
            Intent(context, PassCodeLockActivity::class.java)
                .apply {
                    putExtra(TYPE_PARAM, type)
        
    }

}
