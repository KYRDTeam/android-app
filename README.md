
  
    
# KyberSwap mobile app    
### Download app on Google Play store [<img src="https://kyberswap.com/app/images/google_play_store.svg">](https://play.google.com/store/apps/details?id=com.kyberswap.android)      
      
### Also available on iOS, download it from App Store [<img src="https://kyberswap.com/app/images/apple_store.svg">](https://itunes.apple.com/us/app/kyberswap/id1453691309)      
      
[iOS source](https://github.com/KyberNetwork/KyberSwap-iOS)      
      
      
KyberSwap DEX is now available as a mobile app. In addition to our popular token swap service, App also features a smart wallet that employs innovative security measures to turn your smartphone into a secure Ethereum wallet.      
      
- Swap      
A decentralized exchange on your phone. Exchange (such as ETH -> KNC, DAI -> ETH, TUSD -> WBTC etc) among 75+ tokens supported by Kyber      
      
- Limit Order      
Swap at your target price, fully control of your tokens, low fees, seamless UX.       
      
- Live prices and charts      
Live prices and charts are available for popular tokens      
      
      
- Price alerts      
Set price alerts to be notified in real time       
      
      
- Gift Cards      
Redeem or unlock gift cards and claim tokens      
      
      
- Transfer      
One click service to transfer tokens from one wallet to another wallet. QR code scanner available.      
      
      
- Import / create wallets      
Import JSON wallet easily in your mobile phone. Don’t understand JSON ? No worry, You can easily create a new wallet.      
      
      
- 12 words and a pen      
In case your device gets lost, you can secure your wallet and coins with just 12 words written on paper. The master phrase can restore your wallet and funds on any other device.      
      
      
- Manage profile and features      
Create and manage your profile directly from app to unlock all features such that price alert.      
      
      
- No ads. No tracking. Always free. Always private.      
No tracking, no ads or privacy concerns. That’s our promise.      
      
# KyberSwap for Android  
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
│   ├── api… Contain all responsed entities from APIs 
│   ├── mapper… Convert a responsed entity to business model 
│   └── repository… The implementation of repositoried interfaces from Domain Layer 
├── domain… Business logic layer 
│   ├── model… Cotain all business models 
│   ├── repository… Interface to communicate with Data Layer 
│   └── usecase… Implement business logic 
├── presentation… Presentation layer, UI and UI logic 
└── util… Utility functions every where in the app.      
    ├── di… Dependencies Injection module     
    ├── ext… Implement extension function 
```