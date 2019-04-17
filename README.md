## Use MVVM with DataBinding on presentation layer.
### View
* View is the actual user interface in the app. It can be an Activity, a Fragment or any custom Android View. 
### ViewModel
* ViewModel is a model for the View of the app: an abstraction of the View. 
* ViewModel retrieves the necessary data from the Model, applies the UI logic and then exposes relevant data for the View to consume. 
* ViewModel should expose states for the View, rather than just events
* Any possible logic of the View is moved in the ViewModel.
### Model
* The Models hold the entire business logic.
* The Model exposes data easily consumable through event streams. 
* It composes data from multiple sources, like the network layer, database or shared preferences and exposes easily consumable data to whomever needs it. 
## Build based on Clean Architecture
![Clean Architecture](https://8thlight.com/blog/assets/posts/2012-08-13-the-clean-architecture/CleanArchitecture-8d1fe066e8f7fa9c7d8e84c1a6b0e2b74b2c670ff8052828f4a7e73fcbbc698c.jpg)
## Detail Data Flow Implementation
![Data Flow](https://github.com/KyberNetwork/kyberswap-android/blob/develop/images/clean_architecture_flow.svg)

## Folder structure
```
├── data… Data layer
│   ├── api… Packages for each API 
│   ├── mapper… Convert entity to model
│   └── repository… Implementation for Domain layer
│       ├── datasource… Implement local and remote datasource
│       └── storage… Share preferences data
├── domain… Implement business logic
│   ├── model… Business model
│   ├── repository… Interface to communicate with Data Layer
│   └── usecase… Implement business logic
├── presentation… Presentation layer
└── util… Utility functions every where in the app.
    ├── di… Dependencies Injection module
    ├── ext… Implement extension function 

```

## Coding Rule
* Should use Kotlin on new project
* Use Clean Architecture
* We use automatic code review bot (danger) to adopt detekt, AndroidLint static analysis tool
* Use Functional Reactive Style (Observable/Flowable/Single/Compleable) to communicate between Domain/Data/Presentation layer 
* Use DI(Dagger2) to provide instance/object
* Write unit tests for usecase, repository, mapper at least.
* Don't prepare Base class, try to use delegation instead of inheritance as much as possible.	
* BaseActivity/BaseFragmen must not have any responsibility other than the process related with common process for Dagger.
* All object must treat as @NonNull, but if you want to be able to set Null on specific case, use @Nullable annotation.
* If test target method is package private, add @VisibleForTesting.
* Return value of Retrofit API interface must be Single/Completable.
* Use Navigator to navigate between Activity/Fragment
* Use DialogHelper to handle dialog.
* Format code and resolve lint check before committing the code

## Test 
- To run the test
    - `./gradlew test`
