package com.camunda.fox.platform.qa.deployer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 *
 * @author nico.rehwaldt
 */
@Named
@ApplicationScoped
public class TestCdiBean {
  
  protected boolean invoked;
  
  public void invoke() {
    this.invoked = true;    
  }
  
  public boolean isInvoked() {
    return invoked;
  }  
    
  public void setInvoked(boolean invoked) {
    this.invoked = invoked;
  }
}