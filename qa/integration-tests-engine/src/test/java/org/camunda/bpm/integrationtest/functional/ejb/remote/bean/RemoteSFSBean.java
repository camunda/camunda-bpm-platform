package org.camunda.bpm.integrationtest.functional.ejb.remote.bean;

import javax.ejb.Remote;
import javax.ejb.Stateful;

/**
 * A SFSB with a remote business interface 
 * 
 * @author Daniel Meyer
 *
 */
@Stateful
@Remote(BusinessInterface.class)
public class RemoteSFSBean implements BusinessInterface {

  public boolean doBusiness() {
    return true;
  }

}
