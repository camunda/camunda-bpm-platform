package com.camunda.fox.platform.qa.deployer.sample;

import org.activiti.engine.test.Deployment;
import javax.inject.Inject;
import org.activiti.engine.ProcessEngine;

/**
 *
 * @author nico.rehwaldt
 */
@Deployment(resources = {
  "processes/DelegateExecution.bpmn20.xml" 
})
public class WithMixedProcessDeployments {
  
  @Inject
  private ProcessEngine processEngine;
  
  @Deployment(resources = {
    "processes/CdiResolvingBean.bpmn20.xml",
  })
  public void testNotExists() throws Exception {
    
  }
}
