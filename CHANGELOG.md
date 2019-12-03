# Changelog 4.0.0 - 2019-12-03

## Added

### ERC20 Sidechaijn
* SidechainERC20BurnSocketOperation
* SidechainERC20DepositSocketOperation
* SidechainERC20IssueSocketOperation
* SidechainERC20RegisterTokenOperation
* GetERC20WithdrawalsSocketOperation
* SidechainERC20RegisterTokenOperation
* GetERC20TokenSocketOperation
* GetERC20DepositsSocketOperation

### BTC Sidechain
* WithdrawBitcoinOperation
* GenerateBitcoinAddressOperation
* GetBitcoinAddressSocketOperation


# Changelog 3.7.0 - 2019-11-15

## Removed
* networkFeePercentage from Account object
* accountsRegisteredThisInterval from DynamicGlobalProperties object
* nextAvailableVoteId from GlobalProperties object
* maximumCommitteeCount, reservePercentOfFee, networkPercentOfFee, maxPredicateOpcode, accountsPerFeeScale, accountFeeScaleBitshifts from GlobalPropertiesParameters
* blockResult from ObjectTypes

## Changed

### GetContractLogsSocketOperation
* Limit changed to toBlock
* Fixed operation JSON

### GetAccountDepositsSocketOperation
* Added type(SidechainType)
* Operation returns array of Deposit

### GetAccountWithdrawalsSocketOperation
* Added type(SidechainType)
* Operation returns array of Withdraw

## Added

### QueryContractSocketOperation
* Added amount field. Fixed operation JSON

### SidechainType
* It contains .eth and .btc value

### BtcDeposit object

### BtcWithdrawal object

### Deposit.
* It contains EthDeposit or BtcDeposit

### Withdraw.
* It contains EthWithdrawal or BtcWithdrawal


# Changelog 3.6.0 - 2019-10-18

## Removed

### DynamicGlobalProperties
* lastRandQuantity

### OperationType
* contractTransferOperation

### AccountOptions
* votingAccount
* numCommittee
* votes

## Added

### SubmitRegistrationSolutionSocketOperation

### OperationType
* committee_member_activate_operation
* committee_member_deactivate_operation
* committee_frozen_balance_deposit_operation
* committee_frozen_balance_withdraw_operation
* contract_internal_create_operation
* contract_internal_call_operation
* contract_selfdestruct_operation
* sidechain_btc_create_intermediate_deposit_operation

### Log
* blockNum
* trxNum
* opNum

### Account
* accumulatedReward

### ObjectType
* committee_frozen_balance_object

## Updated

### SubscribeContractLogsSocketOperation
* Updated input parameters

### GetContractLogsSocketOperation
* Updated input parameters

### GlobalProperties
* Updated activeCommitteeMembers field type

### Registration
* Now registration goes through task solving

# Changelog 3.5.1 - 2019-10-21

## Fixed
Fixed framework login flow

# Changelog 3.5.0 - 2019-10-15

## Removed

### DynamicGlobalProperties
* currentAslot
* recentlyMissedCount

### GlobalProperties
* blockInterval

### ObjectType
* budgetRecord

### Statistics
* pendingFees

### ContractResultEVM
* gasRefunded

### OperationType
* accountTransferOperation
* sidechainChangeConfigOperation

## Added

### AccountOptions
* delegateShare

### DynamicGlobalProperties
* lastRandQuantity

### ObjectType
* frozenBalance
* btcAddress
* btcIntermediateDeposit
* btcDeposit
* btcWithdraw
* btcAggregating

### SidechainConfig
* waitingETHBlocks

## ContractLogEnum
* Added enum which represent evm or x86 contract log
* ContractLogx86

### OperationType
* balanceFreezeOperation
* balanceUnfreezeOperation
* sidechainERC20IssueOperation
* sidechainERC20BurnOperation
* sidechainBTCCreateAddressOperatio
* sidechainBTCIntermediateDepositOperatio
* sidechainBTCDepositOperatio
* sidechainBTCWithdrawOperatio
* sidechainBTCApproveWithdrawOperatio
* sidechainBTCAggregateOperatio
* blockRewardOperation

## Updated

### Signatures
* signer changed to producer

### ContractLogsSocketOperation
* Fixed returns parameter to ContractLog

### SubscribeContractLogsSocketOperation
* Fixed returns parameter to Boolean
* Removed fromBlock and toBlock fields

### Tests
* Updated all tests and constants


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