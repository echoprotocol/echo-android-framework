[ ![Download](https://api.bintray.com/packages/echo/mobile/android-framework-stable/images/download.svg?version=3.0.5) ](https://bintray.com/echo/mobile/android-framework-stable/3.0.5/link)

# echo-android-framework (echo-android-framework)

Pure Kotlin Echo framework for Android mobile development. Can be used to work with accounts, transactions and contracts in Kotlin, and to easily obtain data from the blockchain via public apis.

## Install

This framework can be obtained through gradle dependency:

```
implementation 'org.echo.mobile:echoframework:3.0.5'
```

Or maven dependency:

```
<dependency>
  <groupId>org.echo.mobile</groupId>
  <artifactId>echoframework</artifactId>
  <version>3.0.5</version>
  <type>pom</type>
</dependency>
```

## Setup

For setup framework use this simple code:
```kotlin
// Create and Setup framework main class
fun init() { 
    val echo = EchoFramework.create(
            /** Here you can put your custom settings for our framework
            Example:
            Custom api options which can be used
             */
            Settings.Configurator()
                .setUrl(ECHO_URL)
                .setApis(Api.DATABASE, Api.ACCOUNT_HISTORY, Api.NETWORK_BROADCAST)
                .configure()
            )
    // Start framework. Connect to node and setup public apis
    echo.start(object : Callback<Any> {
                  override fun onError(error: LocalException) { }
    
                  override fun onSuccess(result: Any) {}
            })
}
```

## Usage

There are simple examples of usage framework

### Accounts

#### Login

```kotlin
fun login() {
    echo.isOwnedBy("name", "password", object : Callback<FullAccount> {
                    override fun onSuccess(result: FullAccount) {}
                         
                    override fun onError(error: LocalException) {}
            })
}
```

#### Info

```kotlin
fun getInfo() {
    val name = "some name"
    
    echo.getAccount(name, object : Callback<FullAccount> {
                        override fun onSuccess(result: FullAccount) {}
                        
                        override fun onError(error: LocalException) {}
            })
    
    echo.getBalance(name, "1.3.0", object : Callback<Balance> {
                        override fun onSuccess(result: Balance) {}
                        
                        override fun onError(error: LocalException) {}
            })
    
    echo.isOwnedBy(name, "password", object : Callback<FullAccount> {
                        override fun onSuccess(result: FullAccount) {}
                        
                        override fun onError(error: LocalException) {}
            })
}
```

#### History

```kotlin
fun getHistory() {
    echo.getAccountHistroy(nameOrId = "some id", 
                           startId = "1.11.0", 
                           stopId = "1.11.0", 
                           limit = 100, 
                           object : Callback<HistoryResponce> {
                               override fun onSuccess(result: HistoryResponse) {}
                               
                               override fun onError(error: LocalException) {}
            })
}
```

#### Subscribe to account

```kotlin
fun subscribeToAccount() {
    echo.subscribeOnAccount("account name or id",
                object : AccountListener {
                    override fun onChange(updatedAccount: FullAccount) {}
                }, 
                object : Callback<Boolean> {
                    override fun onSuccess(result: Boolean) {}
                    
                    override fun onError(error: LocalException) {}
            })
}
```

#### Change password

```kotlin
fun changePassword() {
    echo.changePassword(
               "account name or id",
               "old password",
               "new password",
               object : Callback<Boolean> {
                   override fun onSuccess(result: Boolean) {}
               
                   override fun onError(error: LocalException) {}
               }
            )
}
```

#### Fee for transfer

```kotlin
fun feeForTransfer() {
    framework.getFeeForTransferOperation(
               "from name or id",
               "from account password",
               "to name or id",
               amount = "10000",
               asset = "1.3.0",
               feeAsset = "1.3.0", //optional
               "message",          //optional
               object : Callback<String> {
                   override fun onSuccess(result: String) {}
               
                   override fun onError(error: LocalException) {}
               }
            )
}
```

#### Transfer

```kotlin
fun send() {
    echo.sendTransferOperation(fromNameOrId = "some name",
                               password = "some pass",
                               toNameOrId = "some name",
                               amount = "300",
                               asset = "1.3.0",
                               feeAsset = "1.3.0",
                               message = null, // optional
                               object : Callback<Boolean> {
                                    override fun onSuccess(result: Boolean) {}
                                
                                    override fun onError(error: LocalException) {}
                               }
            )
}
```

### Assets

#### Create asset

```kotlin
fun createAsset() {
    val asset = Asset("").apply {
        symbol = "ASSET SYMBOL"
        precision = 4
        issuer = Account("account id")
        setBtsOptions(
            BitassetOptions(
                feedLifetimeSec = 86400,
                minimumFeeds = 7, 
                forceSettlementDelaySec = 86400,
                forceSettlementOffsetPercent = 100, 
                maximumForceSettlementVolume = 2000
            )
        )
    
        predictionMarket = false
    }
    
     
     val price = Price().apply {
                       this.quote = AssetAmount(UnsignedLong.valueOf(1), Asset("1.3.1"))
                       this.base = AssetAmount(UnsignedLong.valueOf(1), Asset("1.3.0"))
             }
     
     val options = AssetOptions(
                     UnsignedLong.valueOf(100000),
                     0.toLong(),
                     UnsignedLong.ZERO,
                     AssetOptions.ALLOW_COMITEE_PROVIDE_FEEDS,
                     AssetOptions.ALLOW_COMITEE_PROVIDE_FEEDS,
                     price,
                     "description"
             )
                 
     asset.assetOptions = options
     
     echo.createAsset(
                "issuer name or id", "password",
                asset,
                object : Callback<Boolean> {
                    override fun onSuccess(result: Boolean) {}
                
                    override fun onError(error: LocalException) {}
                }
             )        
}
```

#### Issue asset

```kotlin
fun issueAsset() {
    framework.issueAsset(
                "issuer account name or id",
                "password",
                "1.3.79",
                "1",
                "target account name or id", 
                "message", // optional
                object : Callback<Boolean> {
                     override fun onSuccess(result: Boolean) {}
                 
                     override fun onError(error: LocalException) {}
                 }
            )
}
```

### Contracts

#### Create contract

```kotlin
fun createContract() {
    val byteCode = "some bytecode"
    
    framework.createContract(
        "registrarNameOrId",
        "password",
        assetId = "1.3.0",
        feeAsset = "1.3.0",     // optional
        byteCode,
        params = listOf(),      // optional
        object : Callback<Boolean> {
            override fun onSuccess(result: Boolean) {}
    
            override fun onError(error: LocalException) {}
        }
    )
}
```

#### Call contract

```kotlin
fun callContract() {
    echo.callContract(
                "account name or id",
                "password",
                assetId = "1.3.0",
                feeAsset = "1.3.0",     //optional
                contractId = "contractId",
                methodName = "incrementCounter",
                methodParams = listOf(),
                value = "1",            //optional
                object : Callback<Boolean> {
                    override fun onSuccess(result: Boolean) {}
            
                    override fun onError(error: LocalException) {}
                }
            )
}
```

#### Query contract

```kotlin
fun queryContract() {
    echo.queryContract(
                "account name or id",
                assetId = "1.3.0",
                contractId = "contractId",
                "getCount",
                methodParams = listOf(),
                object : Callback<String> {
                    override fun onSuccess(result: String) {}
                
                    override fun onError(error: LocalException) {}
                }
            )
}
```