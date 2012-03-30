package com.camunda.fox.cockpit.demo.deployer;

import javax.ejb.ApplicationException;

/**
 * 
 * @author Daniel Meyer
 */
@ApplicationException // get past the EJB container
public class FoxApplicationException extends Exception {

  private static final long serialVersionUID = 1L;

  public FoxApplicationException(String message, Throwable cause) {
    super(message, cause);
  }

  public FoxApplicationException(String message) {
    super(message);
  }

}
