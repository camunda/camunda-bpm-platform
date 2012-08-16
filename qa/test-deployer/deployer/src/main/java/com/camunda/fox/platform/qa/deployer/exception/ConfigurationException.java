package com.camunda.fox.platform.qa.deployer.exception;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class ConfigurationException extends RuntimeException {

  public ConfigurationException() {
  }

  public ConfigurationException(String message) {
    super(message);
  }

  public ConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }

  public ConfigurationException(Throwable cause) {
    super(cause);
  }
}
