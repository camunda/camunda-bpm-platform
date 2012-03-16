package com.camunda.fox.platform.test.functional.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named
@ApplicationScoped
public class ExampleBean {
  
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
