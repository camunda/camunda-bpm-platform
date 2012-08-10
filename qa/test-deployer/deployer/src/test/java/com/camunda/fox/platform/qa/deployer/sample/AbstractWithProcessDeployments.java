package com.camunda.fox.platform.qa.deployer.sample;

import org.activiti.engine.test.Deployment;

/**
 *
 * @author nico.rehwaldt
 */
public abstract class AbstractWithProcessDeployments {
  
  @Deployment(resources={ "processes/CdiResolvingBean.bpmn20.xml" })
  public void testFoo() {
    
  }
}
