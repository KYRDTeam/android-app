package com.kyberswap.android.presentation.splash

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.kyberswap.android.R
import com.kyberswap.android.databinding.ActivitySplashBinding
import com.kyberswap.android.presentation.base.BaseActivity
import com.kyberswap.android.presentation.helper.Navigator
import com.kyberswap.android.util.di.ViewModelFactory
import javax.inject.Inject


class SplashActivity : BaseActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    private val viewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(SplashViewModel::class.java)
    }

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivitySplashBinding>(this, R.layout.activity_splash)
    }

    private val handler by lazy {
        Handler()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.viewModel = viewModel
        handler.post {
            binding.imageView.setBackgroundResource(R.drawable.progress_animation)
            val frameAnimation = binding.imageView.background as AnimationDrawable
            frameAnimation.start()


        handler.postDelayed({
            viewModel.prepareData()
, 500)

        viewModel.getWalletStateCallback.observe(this, Observer {
            it?.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is GetUserWalletState.Success -> {
                        navigator.navigateToHome(state.wallet, state.userInfo)

            
                    is GetUserWalletState.ShowError -> {
                        navigator.navigateToLandingPage()
            
        
    
)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

}
