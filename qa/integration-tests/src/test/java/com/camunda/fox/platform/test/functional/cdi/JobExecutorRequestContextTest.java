package com.camunda.fox.platform.test.functional.cdi;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.test.functional.cdi.beans.RequestScopedDelegateBean;
import com.camunda.fox.platform.test.util.AbstractFoxPlatformIntegrationTest;

/**
 * These test cases verify that the CDI RequestContext is active, 
 * when the job executor executes a job and is scoped as expected
 * 
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class JobExecutorRequestContextTest extends AbstractFoxPlatformIntegrationTest {
 
  @Deployment
  public static WebArchive processArchive() {    
    return initWebArchiveDeployment()
      .addClass(RequestScopedDelegateBean.class)            
      .addAsResource("com/camunda/fox/platform/test/functional/cdi/JobExecutorRequestContextTest.testResolveBean.bpmn20.xml")
      .addAsResource("com/camunda/fox/platform/test/functional/cdi/JobExecutorRequestContextTest.testScoping.bpmn20.xml");
  }
    
  @Test
  public void testResolveBean() {
    
    // verifies that @RequestScoped Beans can be resolved
    
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testResolveBean");
    
    waitForJobExecutorToProcessAllJobs(6000, 100);
    
    Object variable = runtimeService.getVariable(pi.getId(), "invocationCounter");
    Assert.assertEquals(1, variable);    
  }
  
  @Test
  public void testScoping() {
    
    // verifies that if the same @RequestScoped Bean is invoked multiple times 
    // in the context of the same job, we get the same instance
    
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testScoping");    
    
    waitForJobExecutorToProcessAllJobs(6000, 100);
    
    Object variable = runtimeService.getVariable(pi.getId(), "invocationCounter");
    // -> the same bean instance was invoked 2 times!
    Assert.assertEquals(2, variable);
    
    Task task = taskService.createTaskQuery()
      .processInstanceId(pi.getProcessInstanceId())
      .singleResult();
    taskService.complete(task.getId());
    
    waitForJobExecutorToProcessAllJobs(6000, 100);
    
    variable = runtimeService.getVariable(pi.getId(), "invocationCounter");
    // now it's '1' again! -> new instance of the bean
    Assert.assertEquals(1, variable);
    
  }
}
