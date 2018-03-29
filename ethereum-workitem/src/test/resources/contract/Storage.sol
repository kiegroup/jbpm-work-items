pragma solidity ^0.4.10;
contract Storage {
  event newDataStores(uint256 storedData);

  uint256 storedData;
  function set(uint256 data) public{
    emit newDataStores(data);
    storedData = data;
  }
  function get() public constant returns (uint256) {
    return storedData;
  }
}