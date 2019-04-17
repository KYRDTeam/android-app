package com.kyberswap.android.util.di.module;

import android.app.Activity;
import com.kyberswap.android.presentation.main.MainActivity;
import com.kyberswap.android.util.di.scope.ActivityScoped;
import dagger.Binds;
import dagger.Module;
import dagger.Subcomponent;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;

@Module(subcomponents = ActivityBindingModule_ContributeMainActivity.MainActivitySubcomponent.class)
public abstract class ActivityBindingModule_ContributeMainActivity {
  private ActivityBindingModule_ContributeMainActivity() {}

  @Binds
  @IntoMap
  @ActivityKey(MainActivity.class)
  abstract AndroidInjector.Factory<? extends Activity> bindAndroidInjectorFactory(
      MainActivitySubcomponent.Builder builder);

  @Subcomponent(modules = MainActivityModule.class)
  @ActivityScoped
  public interface MainActivitySubcomponent extends AndroidInjector<MainActivity> {
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<MainActivity> {}
  }
}
