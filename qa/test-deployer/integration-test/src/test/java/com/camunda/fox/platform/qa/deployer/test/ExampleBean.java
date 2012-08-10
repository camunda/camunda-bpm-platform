package com.camunda.fox.platform.qa.deployer.test;

import javax.inject.Named;

/**
 *
 * @author nico.rehwaldt
 */
@Named("exampleBean")
public class ExampleBean {
  
  static boolean INVOKED = false;
  
  public void invoke() {
    INVOKED = true;
  }
}
