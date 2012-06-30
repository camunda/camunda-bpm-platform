package com.camunda.fox.platform.test.functional.cdi.beans;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
@Remote(InvocationCounterService.class)
@Local(InvocationCounterServiceLocal.class)
public class InvocationCounterServiceBean implements InvocationCounterService, InvocationCounterServiceLocal {
  
  @Inject
  private InvocationCounter invocationCounter;

  public int getNumOfInvocations() {    
    return invocationCounter.incrementAndGet();
  }

}
