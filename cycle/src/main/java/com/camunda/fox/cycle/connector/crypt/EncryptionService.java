package com.camunda.fox.cycle.connector.crypt;


public interface EncryptionService {
  public String encrypt(String input);
  public String decrypt(String input);
}
