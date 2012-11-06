package com.camunda.fox.cycle.connector.crypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.logging.Logger;

import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.stereotype.Component;

@Component
public class EncryptionServiceImpl implements EncryptionService {

  private static final long serialVersionUID = 1L;

  BasicTextEncryptor textEncryptor;
  
  Logger log = Logger.getLogger(EncryptionServiceImpl.class.getName());

  public final static String DEFAULT_PASSWORD = "change_the_cycle_password";

  private String encryptionPassword = null;
  
  private String passwordFilePath = System.getProperty("user.home") + File.separatorChar + "cycle.password";
  
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
  }

  @Override
  public String encrypt(String input) {
    check();
    return textEncryptor.encrypt(input);
  }

  @Override
  public String decrypt(String input) {
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
    if (encryptionPassword == null) {
        try {
          encryptionPassword = new Scanner(new FileInputStream(new File(getPasswordFilePath()))).useDelimiter("\\A").next();
        } catch (FileNotFoundException e) {
          log.warning(String.format("Could not read the encryption password from specified path %s, using default password", getPasswordFilePath()));
          encryptionPassword = DEFAULT_PASSWORD;
        }
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
    System.out.println("Encrypted Result: "+ new EncryptionServiceImpl(password).encrypt(text) );
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

}
