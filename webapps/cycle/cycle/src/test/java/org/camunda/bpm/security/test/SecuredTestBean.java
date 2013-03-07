package org.camunda.bpm.security.test;

import javax.annotation.security.RolesAllowed;

/**
 *
 * @author nico.rehwaldt
 */
public class SecuredTestBean {
  
  public static boolean CALLED = false;
  
  @RolesAllowed("admin")
  public void adminAccessFoo() {
    CALLED = true;
  }
  
  @RolesAllowed("user")
  public void normalAccessFoo() {
    CALLED = true;
  }
  
  @RolesAllowed(value={ "admin", "user" })
  public void complexRolesAllowed() {
    CALLED = true;
  }
}
