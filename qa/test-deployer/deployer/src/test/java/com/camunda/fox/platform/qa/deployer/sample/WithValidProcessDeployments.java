package com.camunda.fox.platform.qa.deployer.sample;

import org.activiti.engine.test.Deployment;

/**
 *
 * @author nico.rehwaldt
 */
public class WithValidProcessDeployments {
  
  @Deployment(resources = {
    "processes/CdiResolvingBean.bpmn20.xml"
  })
  public void testFoo() throws Exception {
    
  }
  
  @Deployment(resources = {
    "processes/DelegateExecution.bpmn20.xml"
  })
  public void testFooBar() throws Exception {
    
  }
  
  @Deployment(resources = {
    "processes/SimpleExpressionEvaluation.bpmn20.xml"
  })
  public void testBar() throws Exception {
    
  }
  
  @Deployment(resources = {
    "processes/CdiResolvingBean.bpmn20.xml",
    "processes/DelegateExecution.bpmn20.xml",
    "processes/SimpleExpressionEvaluation.bpmn20.xml"
  })
  public void testCombined() throws Exception {
    
  }
}
