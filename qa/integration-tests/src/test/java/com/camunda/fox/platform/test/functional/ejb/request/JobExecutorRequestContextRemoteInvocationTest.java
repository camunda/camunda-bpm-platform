package com.camunda.fox.platform.test.functional.ejb.request;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.test.functional.ejb.request.beans.InvocationCounter;
import com.camunda.fox.platform.test.functional.ejb.request.beans.InvocationCounterDelegateBean;
import com.camunda.fox.platform.test.functional.ejb.request.beans.InvocationCounterService;
import com.camunda.fox.platform.test.functional.ejb.request.beans.InvocationCounterServiceBean;
import com.camunda.fox.platform.test.functional.ejb.request.beans.InvocationCounterServiceLocal;
import com.camunda.fox.platform.test.util.AbstractFoxPlatformIntegrationTest;

/**
 * This test verifies that if a delegate bean invoked from the Job Executor 
 * calls a REMOTE SLSB from a different deployment, the RequestContest is active there as well.
 * 
 * NOTE: 
 * - does not work on Jboss AS with a remote invocation (Bug in Jboss AS?) SEE HEMERA-2453
 * - works on Glassfish 
 * 
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class JobExecutorRequestContextRemoteInvocationTest extends AbstractFoxPlatformIntegrationTest {
 
  @Deployment(name="pa", order=2)
  public static WebArchive processArchive() {    
    return initWebArchiveDeployment()
      .addClass(InvocationCounterDelegateBean.class)
      .addClass(InvocationCounterService.class) // interface (remote)
      .addAsResource("com/camunda/fox/platform/test/functional/ejb/request/JobExecutorRequestContextRemoteInvocationTest.testContextPropagationEjbRemote.bpmn20.xml");
  }
  
  @Deployment(order=1)
  public static WebArchive delegateDeployment() {    
    return ShrinkWrap.create(WebArchive.class, "service.war")
      .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
      .addClass(AbstractFoxPlatformIntegrationTest.class)
      .addClass(InvocationCounter.class) // @RequestScoped CDI bean
      .addClass(InvocationCounterService.class) // interface (remote)
      .addClass(InvocationCounterServiceLocal.class) // interface (local)
      .addClass(InvocationCounterServiceBean.class); // @Stateless ejb 
  }
  
  
  @Test
  @OperateOnDeployment("pa")
  public void testRequestContextPropagationEjbRemote() {
    
    // This test verifies that if a delegate bean invoked from the Job Executor 
    // calls an EJB from a different deployment, the RequestContest is active there as well.
      
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testContextPropagationEjbRemote");    
    
    waitForJobExecutorToProcessAllJobs(6000, 100);
    
    Object variable = runtimeService.getVariable(pi.getId(), "invocationCounter");
    // remote invocations of a bean from a seperate deployment constitutes seperate requests
    Assert.assertEquals(1, variable);
    
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
