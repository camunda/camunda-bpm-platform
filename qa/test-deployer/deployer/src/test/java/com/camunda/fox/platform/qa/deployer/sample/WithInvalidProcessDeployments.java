package com.camunda.fox.platform.qa.deployer.sample;

import org.activiti.engine.test.Deployment;

/**
 *
 * @author nico.rehwaldt
 */
public class WithInvalidProcessDeployments {
  
  @Deployment(resources = {
    "processes/CdiResolvingBean.bpmn20.xml"
  })
  public void testFoo() throws Exception {
    
  }
  
  @Deployment(resources = {
    "processes/notexists.bpmn20.xml"
  })
  public void testNotExists() throws Exception {
    
  }
}
