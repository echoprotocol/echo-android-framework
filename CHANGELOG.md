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