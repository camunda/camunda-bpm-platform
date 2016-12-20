package org.camunda.bpm.integrationtest.functional.ejb.local.bean;

import javax.ejb.Local;
import javax.ejb.Stateless;

import org.camunda.bpm.BpmPlatform;

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
