package com.camunda.fox.platform.qa.deployer.configuration;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public interface Validatable {
  
  /**
   * Validate the correctness of the receiver
   */
  public void validate();
}
