package com.camunda.fox.platform.test.functional.ejb.request.beans;

import javax.enterprise.context.RequestScoped;

/**
 * Simple request scoped bean keeping an invocation count.
 * 
 * @author Daniel Meyer
 *
 */
@RequestScoped
public class InvocationCounter {
  
  private int invocations;
    
  public int incrementAndGet() {
    return ++invocations;
  }
    

}
