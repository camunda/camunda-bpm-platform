package org.camunda.bpm.integrationtest.functional.ejb.local.bean;

import org.camunda.bpm.BpmPlatform;

import javax.ejb.Local;
import javax.ejb.Stateless;

/**
 * A SLSB with a local business interface
 *
 * @author Daniel Meyer
 *
 */
@Stateless
@Local(StartProcessInterface.class)
public class StartProcessSLSB implements StartProcessInterface {

  public boolean doStartProcess() {

    BpmPlatform.getDefaultProcessEngine()
      .getRuntimeService()
      .startProcessInstanceByKey("callbackProcess");

    return true;
  }

}
