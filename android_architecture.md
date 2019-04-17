# Architecture Guidelines

* Use Dagger2 for Dependencies Injection
* Use Retrofit2 as HttpClient
* Use RxJava/RxAndroid for better performance
* Call REST API by Using RxJava-Retrofit2
* Must use Crashlytic for tracking android crashing
* Database must use Realm
* Using Glide to load Image for better performance
* Use Timber for better logging mechanism
* Use Buddy build for CI/CD
* Use Gson to Parser JSON 
* Use ButterKnife to avoid findViewById for clean code

The architecture of our Android apps is based on the [MVP](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93presenter) (Model View Presenter) pattern.

* __View (UI layer)__: 
    * Define an interface that your View (Activity, Fragment, View) is going to implement. The interface will exposes the method to Presenter to interact with.
    * Your View implement the interface
    * Inject presenter to View
    * Attach View to presenter
    * View only know __HOW__ to display
* __Presenter__: 
    * Implement methods that the View requires to perform the nessary actions.
    * One finish using interface to interact with View
    * Presenter know __WHEN__ display
* __Model (Data Layer)__: 
    * Dispatch request using DataManager
    * Return Observables for Presenter
    * Model know __WHAT__ display

