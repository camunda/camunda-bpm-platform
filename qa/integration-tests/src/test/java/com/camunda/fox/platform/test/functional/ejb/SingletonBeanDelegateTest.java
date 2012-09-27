package com.camunda.fox.platform.test.functional.ejb;

import org.activiti.engine.runtime.ProcessInstance;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.test.functional.ejb.beans.SingletonBeanClientDelegate;
import com.camunda.fox.platform.test.functional.ejb.beans.SingletonBeanDelegate;
import com.camunda.fox.platform.test.util.AbstractFoxPlatformIntegrationTest;


/**
 * Testcase verifying various ways to use a Singleton as a JavaDelegate
 * 
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class SingletonBeanDelegateTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {    
    return initWebArchiveDeployment()
      .addClass(SingletonBeanDelegate.class)
      .addClass(SingletonBeanClientDelegate.class)    
      .addAsResource("com/camunda/fox/platform/test/functional/ejb/SingletonBeanDelegateTest.testBeanResolution.bpmn20.xml")
      .addAsResource("com/camunda/fox/platform/test/functional/ejb/SingletonBeanDelegateTest.testBeanResolutionFromClient.bpmn20.xml");
  }
  
  
  @Test
  public void testBeanResolution() {
    
    // this testcase first resolves the Singleton synchronouly and then from the JobExecutor
    
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testBeanResolution");
    
    Assert.assertEquals(true, runtimeService.getVariable(pi.getId(), SingletonBeanDelegate.class.getName()));
    
    runtimeService.setVariable(pi.getId(), SingletonBeanDelegate.class.getName(), false);
    
    taskService.complete(taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult().getId());
    
    waitForJobExecutorToProcessAllJobs(6000, 300);
    
    Assert.assertEquals(true, runtimeService.getVariable(pi.getId(), SingletonBeanDelegate.class.getName()));
    
    taskService.complete(taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult().getId());
    
  }
  

  @Test
  public void testBeanResolutionfromClient() {
    
    // this testcase invokes a CDI bean that injects the EJB
    
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testBeanResolutionfromClient");
    
    Assert.assertEquals(true, runtimeService.getVariable(pi.getId(), SingletonBeanDelegate.class.getName()));
    
    runtimeService.setVariable(pi.getId(), SingletonBeanDelegate.class.getName(), false);
    
    taskService.complete(taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult().getId());
    
    waitForJobExecutorToProcessAllJobs(6000, 300);
    
    Assert.assertEquals(true, runtimeService.getVariable(pi.getId(), SingletonBeanDelegate.class.getName()));
    
    taskService.complete(taskService.createTaskQuery().processInstanceId(pi.getId()).singleResult().getId());
  }
  
  @Test
  public void testMultipleInvocations() {
    
    // this is greater than any Datasource- / EJB- / Thread-Pool size -> make sure all resources are released properly.
    int instances = 100;    
    String[] ids = new String[instances];
    
    for(int i=0; i<instances; i++) {    
      ids[i] = runtimeService.startProcessInstanceByKey("testBeanResolutionfromClient").getId();    
      Assert.assertEquals("Incovation=" + i, true, runtimeService.getVariable(ids[i], SingletonBeanDelegate.class.getName()));      
      runtimeService.setVariable(ids[i], SingletonBeanDelegate.class.getName(), false);
      taskService.complete(taskService.createTaskQuery().processInstanceId(ids[i]).singleResult().getId());
    }
        
    waitForJobExecutorToProcessAllJobs(60*1000, 300);
    
    for(int i=0; i<instances; i++) {    
      Assert.assertEquals("Incovation=" + i, true, runtimeService.getVariable(ids[i], SingletonBeanDelegate.class.getName()));    
      taskService.complete(taskService.createTaskQuery().processInstanceId(ids[i]).singleResult().getId());
    }
    
  }

}
