package com.kyberswap.android.util.di.module;

import android.app.Activity;
import com.kyberswap.android.presentation.splash.SplashActivity;
import com.kyberswap.android.util.di.scope.ActivityScoped;
import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;

@Module(
  subcomponents = ActivityBindingModule_ContributeSplashActivity.SplashActivitySubcomponent.class
)
public abstract class ActivityBindingModule_ContributeSplashActivity {
  private ActivityBindingModule_ContributeSplashActivity() {}

  @Binds
  @IntoMap
  @ActivityKey(SplashActivity.class)
  abstract AndroidInjector.Factory<? extends Activity> bindAndroidInjectorFactory(
      SplashActivitySubcomponent.Builder builder);

  @Subcomponent(modules = SplashActivityModule.class)
  @ActivityScoped
  public interface SplashActivitySubcomponent extends AndroidInjector<SplashActivity> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<SplashActivity> {}
  }
}
