package org.camunda.bpm.integrationtest.functional.ejb.remote.bean;

import javax.ejb.Remote;
import javax.ejb.Singleton;

/**
 * A SingletonBean with a remote business interface 
 * 
 * @author Daniel Meyer
 *
 */
@Singleton
@Remote(BusinessInterface.class)
public class RemoteSingletonBean implements BusinessInterface {

  public boolean doBusiness() {
    return true;
  }

}
