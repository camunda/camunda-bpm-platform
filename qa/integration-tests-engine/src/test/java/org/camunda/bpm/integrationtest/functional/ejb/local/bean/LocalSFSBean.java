package org.camunda.bpm.integrationtest.functional.ejb.local.bean;

import javax.ejb.Local;
import javax.ejb.Stateful;

/**
 * A SFSB with a local business interface 
 * 
 * @author Daniel Meyer
 *
 */
@Stateful
@Local(BusinessInterface.class)
public class LocalSFSBean implements BusinessInterface {

  public boolean doBusiness() {
    return true;
  }

}
