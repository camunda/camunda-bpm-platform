package com.camunda.fox.platform.qa.deployer.sample;

import org.activiti.engine.test.Deployment;

/**
 *
 * @author nico.rehwaldt
 */
@Deployment(resources = {
  "processes/CdiResolvingBean.bpmn20.xml",
  "processes/DelegateExecution.bpmn20.xml" 
})
public class WithProcessDeploymentsOnClassLevel {
  
  public void testNotExists() throws Exception {
    
  }
}
