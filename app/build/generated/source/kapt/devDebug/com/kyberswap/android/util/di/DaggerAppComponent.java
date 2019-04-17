// Generated by Dagger (https://google.github.io/dagger).
package com.kyberswap.android.util.di;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.app.Service;
import android.arch.lifecycle.ViewModel;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.Context;
import com.kyberswap.android.KyberSwapApplication;
import com.kyberswap.android.presentation.helper.Navigator;
import com.kyberswap.android.presentation.landing.LandingActivity;
import com.kyberswap.android.presentation.landing.LandingActivity_MembersInjector;
import com.kyberswap.android.presentation.landing.LandingViewModel;
import com.kyberswap.android.presentation.landing.LandingViewModel_Factory;
import com.kyberswap.android.presentation.main.MainActivity;
import com.kyberswap.android.presentation.main.MainActivity_MembersInjector;
import com.kyberswap.android.presentation.main.MainViewModel;
import com.kyberswap.android.presentation.main.MainViewModel_Factory;
import com.kyberswap.android.presentation.splash.SplashActivity;
import com.kyberswap.android.presentation.splash.SplashActivity_MembersInjector;
import com.kyberswap.android.presentation.splash.SplashViewModel;
import com.kyberswap.android.presentation.splash.SplashViewModel_Factory;
import com.kyberswap.android.util.di.module.ActivityBindingModule_ContributeLandingActivity;
import com.kyberswap.android.util.di.module.ActivityBindingModule_ContributeMainActivity;
import com.kyberswap.android.util.di.module.ActivityBindingModule_ContributeSplashActivity;
import com.kyberswap.android.util.di.module.AppModule_ProvideContextFactory;
import com.kyberswap.android.util.di.module.DatabaseModule;
import com.kyberswap.android.util.di.module.NetworkModule;
import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication_MembersInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.DispatchingAndroidInjector_Factory;
import dagger.android.support.DaggerAppCompatActivity_MembersInjector;
import dagger.internal.DoubleCheck;
import dagger.internal.InstanceFactory;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import java.util.Collections;
import java.util.Map;
import javax.inject.Provider;

public final class DaggerAppComponent implements AppComponent {
  private Provider<ActivityBindingModule_ContributeMainActivity.MainActivitySubcomponent.Builder>
      mainActivitySubcomponentBuilderProvider;

  private Provider<
          ActivityBindingModule_ContributeSplashActivity.SplashActivitySubcomponent.Builder>
      splashActivitySubcomponentBuilderProvider;

  private Provider<
          ActivityBindingModule_ContributeLandingActivity.LandingActivitySubcomponent.Builder>
      landingActivitySubcomponentBuilderProvider;

  private Provider<Application> applicationProvider;

  private Provider<Context> provideContextProvider;

  private DaggerAppComponent(Builder builder) {
    initialize(builder);
  }

  public static AppComponent.Builder builder() {
    return new Builder();
  }

  private Map<Class<? extends Activity>, Provider<AndroidInjector.Factory<? extends Activity>>>
      getMapOfClassOfAndProviderOfFactoryOf() {
    return MapBuilder
        .<Class<? extends Activity>, Provider<AndroidInjector.Factory<? extends Activity>>>
            newMapBuilder(3)
        .put(MainActivity.class, (Provider) mainActivitySubcomponentBuilderProvider)
        .put(SplashActivity.class, (Provider) splashActivitySubcomponentBuilderProvider)
        .put(LandingActivity.class, (Provider) landingActivitySubcomponentBuilderProvider)
        .build();
  }

  private DispatchingAndroidInjector<Activity> getDispatchingAndroidInjectorOfActivity() {
    return DispatchingAndroidInjector_Factory.newDispatchingAndroidInjector(
        getMapOfClassOfAndProviderOfFactoryOf());
  }

  private DispatchingAndroidInjector<BroadcastReceiver>
      getDispatchingAndroidInjectorOfBroadcastReceiver() {
    return DispatchingAndroidInjector_Factory.newDispatchingAndroidInjector(
        Collections
            .<Class<? extends BroadcastReceiver>,
                Provider<AndroidInjector.Factory<? extends BroadcastReceiver>>>
                emptyMap());
  }

  private DispatchingAndroidInjector<Fragment> getDispatchingAndroidInjectorOfFragment() {
    return DispatchingAndroidInjector_Factory.newDispatchingAndroidInjector(
        Collections
            .<Class<? extends Fragment>, Provider<AndroidInjector.Factory<? extends Fragment>>>
                emptyMap());
  }

  private DispatchingAndroidInjector<Service> getDispatchingAndroidInjectorOfService() {
    return DispatchingAndroidInjector_Factory.newDispatchingAndroidInjector(
        Collections
            .<Class<? extends Service>, Provider<AndroidInjector.Factory<? extends Service>>>
                emptyMap());
  }

  private DispatchingAndroidInjector<ContentProvider>
      getDispatchingAndroidInjectorOfContentProvider() {
    return DispatchingAndroidInjector_Factory.newDispatchingAndroidInjector(
        Collections
            .<Class<? extends ContentProvider>,
                Provider<AndroidInjector.Factory<? extends ContentProvider>>>
                emptyMap());
  }

  private DispatchingAndroidInjector<android.support.v4.app.Fragment>
      getDispatchingAndroidInjectorOfFragment2() {
    return DispatchingAndroidInjector_Factory.newDispatchingAndroidInjector(
        Collections
            .<Class<? extends android.support.v4.app.Fragment>,
                Provider<AndroidInjector.Factory<? extends android.support.v4.app.Fragment>>>
                emptyMap());
  }

  @SuppressWarnings("unchecked")
  private void initialize(final Builder builder) {
    this.mainActivitySubcomponentBuilderProvider =
        new Provider<
            ActivityBindingModule_ContributeMainActivity.MainActivitySubcomponent.Builder>() {
          @Override
          public ActivityBindingModule_ContributeMainActivity.MainActivitySubcomponent.Builder
              get() {
            return new MainActivitySubcomponentBuilder();
          }
        };
    this.splashActivitySubcomponentBuilderProvider =
        new Provider<
            ActivityBindingModule_ContributeSplashActivity.SplashActivitySubcomponent.Builder>() {
          @Override
          public ActivityBindingModule_ContributeSplashActivity.SplashActivitySubcomponent.Builder
              get() {
            return new SplashActivitySubcomponentBuilder();
          }
        };
    this.landingActivitySubcomponentBuilderProvider =
        new Provider<
            ActivityBindingModule_ContributeLandingActivity.LandingActivitySubcomponent.Builder>() {
          @Override
          public ActivityBindingModule_ContributeLandingActivity.LandingActivitySubcomponent.Builder
              get() {
            return new LandingActivitySubcomponentBuilder();
          }
        };
    this.applicationProvider = InstanceFactory.create(builder.application);
    this.provideContextProvider =
        DoubleCheck.provider(AppModule_ProvideContextFactory.create(applicationProvider));
  }

  @Override
  public void inject(KyberSwapApplication instance) {
    injectKyberSwapApplication(instance);
  }

  private KyberSwapApplication injectKyberSwapApplication(KyberSwapApplication instance) {
    DaggerApplication_MembersInjector.injectActivityInjector(
        instance, getDispatchingAndroidInjectorOfActivity());
    DaggerApplication_MembersInjector.injectBroadcastReceiverInjector(
        instance, getDispatchingAndroidInjectorOfBroadcastReceiver());
    DaggerApplication_MembersInjector.injectFragmentInjector(
        instance, getDispatchingAndroidInjectorOfFragment());
    DaggerApplication_MembersInjector.injectServiceInjector(
        instance, getDispatchingAndroidInjectorOfService());
    DaggerApplication_MembersInjector.injectContentProviderInjector(
        instance, getDispatchingAndroidInjectorOfContentProvider());
    DaggerApplication_MembersInjector.injectSetInjected(instance);
    dagger.android.support.DaggerApplication_MembersInjector.injectSupportFragmentInjector(
        instance, getDispatchingAndroidInjectorOfFragment2());
    return instance;
  }

  private static final class Builder implements AppComponent.Builder {
    private Application application;

    @Override
    public AppComponent build() {
      if (application == null) {
        throw new IllegalStateException(Application.class.getCanonicalName() + " must be set");
      }
      return new DaggerAppComponent(this);
    }

    @Override
    public Builder application(Application application) {
      this.application = Preconditions.checkNotNull(application);
      return this;
    }

    /**
     * This module is declared, but an instance is not used in the component. This method is a
     * no-op. For more, see https://google.github.io/dagger/unused-modules.
     */
    @Override
    public Builder networkModule(NetworkModule networkModule) {
      return this;
    }

    /**
     * This module is declared, but an instance is not used in the component. This method is a
     * no-op. For more, see https://google.github.io/dagger/unused-modules.
     */
    @Override
    public Builder databaseModule(DatabaseModule databaseModule) {
      return this;
    }
  }

  private final class MainActivitySubcomponentBuilder
      extends ActivityBindingModule_ContributeMainActivity.MainActivitySubcomponent.Builder {
    private MainActivity seedInstance;

    @Override
    public ActivityBindingModule_ContributeMainActivity.MainActivitySubcomponent build() {
      if (seedInstance == null) {
        throw new IllegalStateException(MainActivity.class.getCanonicalName() + " must be set");
      }
      return new MainActivitySubcomponentImpl(this);
    }

    @Override
    public void seedInstance(MainActivity arg0) {
      this.seedInstance = Preconditions.checkNotNull(arg0);
    }
  }

  private final class MainActivitySubcomponentImpl
      implements ActivityBindingModule_ContributeMainActivity.MainActivitySubcomponent {
    private MainActivity seedInstance;

    private MainViewModel_Factory mainViewModelProvider;

    private MainActivitySubcomponentImpl(MainActivitySubcomponentBuilder builder) {
      initialize(builder);
    }

    private Navigator getNavigator() {
      return new Navigator(seedInstance);
    }

    private Map<Class<? extends ViewModel>, Provider<ViewModel>>
        getMapOfClassOfAndProviderOfViewModel() {
      return Collections.<Class<? extends ViewModel>, Provider<ViewModel>>singletonMap(
          MainViewModel.class, (Provider) mainViewModelProvider);
    }

    private ViewModelFactory getViewModelFactory() {
      return new ViewModelFactory(getMapOfClassOfAndProviderOfViewModel());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final MainActivitySubcomponentBuilder builder) {
      this.seedInstance = builder.seedInstance;
      this.mainViewModelProvider =
          MainViewModel_Factory.create(DaggerAppComponent.this.provideContextProvider);
    }

    @Override
    public void inject(MainActivity arg0) {
      injectMainActivity(arg0);
    }

    private MainActivity injectMainActivity(MainActivity instance) {
      DaggerAppCompatActivity_MembersInjector.injectSupportFragmentInjector(
          instance, DaggerAppComponent.this.getDispatchingAndroidInjectorOfFragment2());
      DaggerAppCompatActivity_MembersInjector.injectFrameworkFragmentInjector(
          instance, DaggerAppComponent.this.getDispatchingAndroidInjectorOfFragment());
      MainActivity_MembersInjector.injectNavigator(instance, getNavigator());
      MainActivity_MembersInjector.injectViewModelFactory(instance, getViewModelFactory());
      return instance;
    }
  }

  private final class SplashActivitySubcomponentBuilder
      extends ActivityBindingModule_ContributeSplashActivity.SplashActivitySubcomponent.Builder {
    private SplashActivity seedInstance;

    @Override
    public ActivityBindingModule_ContributeSplashActivity.SplashActivitySubcomponent build() {
      if (seedInstance == null) {
        throw new IllegalStateException(SplashActivity.class.getCanonicalName() + " must be set");
      }
      return new SplashActivitySubcomponentImpl(this);
    }

    @Override
    public void seedInstance(SplashActivity arg0) {
      this.seedInstance = Preconditions.checkNotNull(arg0);
    }
  }

  private final class SplashActivitySubcomponentImpl
      implements ActivityBindingModule_ContributeSplashActivity.SplashActivitySubcomponent {
    private SplashActivity seedInstance;

    private SplashActivitySubcomponentImpl(SplashActivitySubcomponentBuilder builder) {
      initialize(builder);
    }

    private Map<Class<? extends ViewModel>, Provider<ViewModel>>
        getMapOfClassOfAndProviderOfViewModel() {
      return Collections.<Class<? extends ViewModel>, Provider<ViewModel>>singletonMap(
          SplashViewModel.class, (Provider) SplashViewModel_Factory.create());
    }

    private ViewModelFactory getViewModelFactory() {
      return new ViewModelFactory(getMapOfClassOfAndProviderOfViewModel());
    }

    private Navigator getNavigator() {
      return new Navigator(seedInstance);
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SplashActivitySubcomponentBuilder builder) {
      this.seedInstance = builder.seedInstance;
    }

    @Override
    public void inject(SplashActivity arg0) {
      injectSplashActivity(arg0);
    }

    private SplashActivity injectSplashActivity(SplashActivity instance) {
      DaggerAppCompatActivity_MembersInjector.injectSupportFragmentInjector(
          instance, DaggerAppComponent.this.getDispatchingAndroidInjectorOfFragment2());
      DaggerAppCompatActivity_MembersInjector.injectFrameworkFragmentInjector(
          instance, DaggerAppComponent.this.getDispatchingAndroidInjectorOfFragment());
      SplashActivity_MembersInjector.injectViewModelFactory(instance, getViewModelFactory());
      SplashActivity_MembersInjector.injectNavigator(instance, getNavigator());
      return instance;
    }
  }

  private final class LandingActivitySubcomponentBuilder
      extends ActivityBindingModule_ContributeLandingActivity.LandingActivitySubcomponent.Builder {
    private LandingActivity seedInstance;

    @Override
    public ActivityBindingModule_ContributeLandingActivity.LandingActivitySubcomponent build() {
      if (seedInstance == null) {
        throw new IllegalStateException(LandingActivity.class.getCanonicalName() + " must be set");
      }
      return new LandingActivitySubcomponentImpl(this);
    }

    @Override
    public void seedInstance(LandingActivity arg0) {
      this.seedInstance = Preconditions.checkNotNull(arg0);
    }
  }

  private final class LandingActivitySubcomponentImpl
      implements ActivityBindingModule_ContributeLandingActivity.LandingActivitySubcomponent {
    private LandingActivity seedInstance;

    private LandingActivitySubcomponentImpl(LandingActivitySubcomponentBuilder builder) {
      initialize(builder);
    }

    private Map<Class<? extends ViewModel>, Provider<ViewModel>>
        getMapOfClassOfAndProviderOfViewModel() {
      return Collections.<Class<? extends ViewModel>, Provider<ViewModel>>singletonMap(
          LandingViewModel.class, (Provider) LandingViewModel_Factory.create());
    }

    private ViewModelFactory getViewModelFactory() {
      return new ViewModelFactory(getMapOfClassOfAndProviderOfViewModel());
    }

    private Navigator getNavigator() {
      return new Navigator(seedInstance);
    }

    @SuppressWarnings("unchecked")
    private void initialize(final LandingActivitySubcomponentBuilder builder) {
      this.seedInstance = builder.seedInstance;
    }

    @Override
    public void inject(LandingActivity arg0) {
      injectLandingActivity(arg0);
    }

    private LandingActivity injectLandingActivity(LandingActivity instance) {
      DaggerAppCompatActivity_MembersInjector.injectSupportFragmentInjector(
          instance, DaggerAppComponent.this.getDispatchingAndroidInjectorOfFragment2());
      DaggerAppCompatActivity_MembersInjector.injectFrameworkFragmentInjector(
          instance, DaggerAppComponent.this.getDispatchingAndroidInjectorOfFragment());
      LandingActivity_MembersInjector.injectViewModelFactory(instance, getViewModelFactory());
      LandingActivity_MembersInjector.injectNavigator(instance, getNavigator());
      return instance;
    }
  }
}
