# CHANGELOG 3.1

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