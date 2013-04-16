package org.camunda.bpm.integrationtest.functional.ejb.local.bean;

import javax.ejb.Local;
import javax.ejb.Singleton;

/**
 * A SingletonBean with a local business interface 
 * 
 * @author Daniel Meyer
 *
 */
@Singleton
@Local(BusinessInterface.class)
public class LocalSingletonBean implements BusinessInterface {

  public boolean doBusiness() {
    return true;
  }

}
