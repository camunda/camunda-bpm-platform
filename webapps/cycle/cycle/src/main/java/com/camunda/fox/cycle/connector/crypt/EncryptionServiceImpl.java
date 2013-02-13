package com.camunda.fox.cycle.connector.crypt;

import java.io.File;
import java.io.FileInputStream;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jasypt.util.password.ConfigurablePasswordEncryptor;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.stereotype.Component;

@Component
public class EncryptionServiceImpl implements EncryptionService {

  private static final Logger log = Logger.getLogger(EncryptionServiceImpl.class.getName());
  
  public final static String DEFAULT_PASSWORD = "change_the_cycle_password";
  
  BasicTextEncryptor textEncryptor;
  
  private String encryptionPassword = null;
  
  private String passwordFilePath = System.getProperty("user.home") + File.separatorChar + "cycle.password";
  
  ConfigurablePasswordEncryptor userPasswordEncryptor;
  
  public EncryptionServiceImpl() {}
  
  public EncryptionServiceImpl(String encryptionPassword) {
    super();
    this.setEncryptionPassword(encryptionPassword);
  }
  
  private void check() {
    if (textEncryptor == null){
      textEncryptor = new BasicTextEncryptor();
      textEncryptor.setPassword(getEncryptionPassword());
    }
    if (userPasswordEncryptor == null) {
      userPasswordEncryptor = new ConfigurablePasswordEncryptor();
      userPasswordEncryptor.setAlgorithm("SHA-1");
      userPasswordEncryptor.setPlainDigest(false);
    }
  }

  @Override
  public String encryptConnectorPassword(String input) {
    check();
    return textEncryptor.encrypt(input);
  }

  @Override
  public String decryptConnectorPassword(String input) {
    check();
    
    if (input == null){
      throw new IllegalArgumentException("Cant decrypt null value, did you try to decrypt an not unset connector password?");
    }
    
    try{
      return textEncryptor.decrypt(input);
    }catch (Exception e) {
      throw new EncryptionException(String.format("Could not decrypt text %s, are you using the correct key?", input), e);
    }
  }

  public String getEncryptionPassword() {
    File passwordFile = new File(getPasswordFilePath());
    
    if (encryptionPassword == null && passwordFile.exists()) {
        try {
          FileInputStream passwordFileStream = new FileInputStream(passwordFile);
          Scanner scanner = new Scanner(passwordFileStream);
          encryptionPassword = scanner.useDelimiter("\\A").next().replace("\n", "");
          passwordFileStream.close();
          scanner.close();
        } catch (Exception e) {
          log.log(Level.WARNING, String.format("Could not read the encryption password from specified path %s, using default password", getPasswordFilePath()), e);
          encryptionPassword = DEFAULT_PASSWORD;
        }
    }else if (encryptionPassword == null) {
      encryptionPassword = DEFAULT_PASSWORD;
    }

    return encryptionPassword;
  }
  
  public static void main(String[] args) {
    System.out.println("Enter the encryption password [press enter to use the default]:");
    Scanner scanner = new Scanner (System.in);
    String password = scanner.nextLine();
    if (password.isEmpty()){
      password = DEFAULT_PASSWORD;
    }
    System.out.println("Enter the text / password to encrypt:");
    String text = scanner.nextLine();
    scanner.close();
    System.out.println("Encrypted Result: "+ new EncryptionServiceImpl(password).encryptConnectorPassword(text) );
  }

  public String getPasswordFilePath() {
    return passwordFilePath;
  }

  public void setPasswordFilePath(String passwordFilePath) {
    this.passwordFilePath = passwordFilePath;
  }

  public void setEncryptionPassword(String encryptionPassword) {
    this.encryptionPassword = encryptionPassword;
  }

  @Override
  public String encryptUserPassword(String input) {
    check();
    return userPasswordEncryptor.encryptPassword(input);
  }

  @Override
  public boolean checkUserPassword(String plain, String digest) {
    check();
    return userPasswordEncryptor.checkPassword(plain, digest);
  }

}
