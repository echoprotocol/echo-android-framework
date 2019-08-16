# CHANGELOG 3.4.0

## Removed

# AssetOptions:
* marketFeePercent
* maxMarketFee
* whitelistMarkets
* blacklistMarkets

# BitassetOptions
* forceSettlementDelaySec
* forceSettlementOffsetPercent
* maximumForceSettlementVolume

# CreateAssetOperation
* isPredictionMarket

# Account
* membershipExpirationDate
* referrer
* lifetimeReferrer
* lifetimeReferrerFeePercentage

# FullAccount
* referrerName

# AccountCreateOperation
* referrer
* referrerPercent

# GlobalProperties
* lifetimeReferrer_percent_of_fee
* cashbackVestingThreshold
* countNonMemberVotes
* allowNonMemberWhitelists
* feeLiquidationThreshold

# DynamicGlobalProperties
* recentSlotsFilled

## Added

# Block
* delegate
* prevSignatures
* vmRoot

# SidechainConfig
* erc20WithdrawTopic
* gasPrice

## Updated

# OperationType
Changed operations ordinals 

# Objects ids
* HistoricalTransfer - 1.6.
* ContractInfo - 1.9.
* ContractResult - 1.10.
* Account statistic - 2.5.


# CHANGELOG 3.3.1

## Fixed
Fixed EdDSA random private key generation


# CHANGELOG 3.3.0

## Operations
Replaced password with wif in the following methods:

# Authentication:
* isOwnedBy
* register
* changeKeys

# Assets:
* createAsset
* issueAsset

# Contracts
* createContract
* callContract

# Fee
* getFeeForTransferOperation

# Sidechain
* generateEthereumAddress
* ethWithdraw

# Transfer
* sendTransferOperation

# Added
Added account registration by wif
Added method for account's active and echorand keys changing by wif
Added random account's private key generation

## Tests
Removed tests with password and added required tests with wif

## Removed
Removed ECKey and all connected features


# CHANGELOG 3.2.1

### Fixed
Sidechain history operations parsing

# CHANGELOG 3.1.2

### Changes

* Added Extensions to ContractCallOperation, ContractCreateOperation

### Tests
Updated tests according to changed methods

# CHANGELOG 3.1.1

## Operations
### Added 
* GetAccountWithdrawalsSocketOperation
* GetAccountDepositsSocketOperation


# CHANGELOG 3.1.0

### Changes

* Account - removed owner Authority
* AccountOptions - removed memo
* Public keys prefix - ECHO
* Added new virtual operations to account history


## Operations
Fixed OperationType enum to actual
Removed message from transfer operation
### Added 
* RequiredContractFeesSocketOperation
* SidechainBurnSocketOperation (virtual)
* SidechainIssueSocketOperation (virtual)


## Objects
Fixed GlobalProperties and EthAddress models
### Added 
* ContractFee

## Tests
Updated tests according to changed methods


# CHANGELOG 3.0.9

## Authority
Owner Authority was removed from the network. 
Active Authority key was changed to EdDSA like echorand key.

### Changes

* Account - removed owner Authority
* AuthorityType - removed owner key type
* AccountUpdateOperation - removed owner Authority
* RegisterSocketOperation - removed owner key
* all signatures changed from EcDSA signature to EdDCA


## Framework setup
Added multiplier for fee which will be used for contracts operations

## Methods
### Added 
* getBlock - get block by block number
* subscribeOnContracts - subscribe to contracts changes by contract IDs
* getEthereumAddresses - returns generated ETH addresses by account ID

### Removed
* getAllContracts
* sidechain transfers logic

## Operations
Fixed OperationType enum to actual
Added possibility to create contract with payable constructor
### Added 
* ContractTransferOperation
* GenerateEthereumAddressOperation
* WithdrawEthereumOperation


## Objects
Fixed ObjectType enum to actual
Fixed Block model
### Added 
* EthAddress

## Tests
Updated tests according to added new methods

## Bugs
* Fixed encode/decode bytes for contract bytecode
* Fixed decode contract address from contract bytecode
* Fixed decode address from contract bytecode
* Fixed error parsing logic