package com.camunda.fox.platform.test.functional.ejb.local.bean;

import javax.ejb.Local;
import javax.ejb.Stateless;

/**
 * A SLSB with a local business interface 
 * 
 * @author Daniel Meyer
 *
 */
@Stateless
@Local(BusinessInterface.class)
public class LocalSLSBean implements BusinessInterface {

  public boolean doBusiness() {
    return true;
  }

}
