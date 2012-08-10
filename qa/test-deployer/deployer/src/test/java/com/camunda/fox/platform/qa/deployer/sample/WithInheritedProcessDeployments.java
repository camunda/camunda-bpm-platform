package com.camunda.fox.platform.qa.deployer.sample;

import org.activiti.engine.test.Deployment;

/**
 *
 * @author nico.rehwaldt
 */
public class WithInheritedProcessDeployments extends AbstractWithProcessDeployments {
  
  @Deployment(resources={ "processes/DelegateExecution.bpmn20.xml" })
  public void testBar() {
    
  }
}
