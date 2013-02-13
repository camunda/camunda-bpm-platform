package com.camunda.fox.cycle.connector.crypt;


public interface EncryptionService {
  
  /**
   * Symmetric key encryption
   */
  public String encryptConnectorPassword(String input);
  public String decryptConnectorPassword(String input);
  /**
   * Asymmetric key encryption
   */
  public String encryptUserPassword(String input);
  public boolean checkUserPassword(String plain, String digest);
  
}
