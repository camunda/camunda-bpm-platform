package org.camunda.bpm.integrationtest.functional.ejb.beans;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import javax.ejb.Stateless;
import javax.inject.Named;

/**
 * A SLSB acting as a {@link JavaDelegate}
 * 
 * @author Daniel Meyer
 *
 */
@Named("SLSBThrowExceptionDelegate")
@Stateless
public class SLSBThrowExceptionDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    throw new MyException("error");
  }

  public static class MyException extends RuntimeException{

    private static final long serialVersionUID = 826202870386719558L;

    public MyException(String string) {
      super(string);
    }
    
  }
}
