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

import com.camunda.fox.platform.test.functional.cdi.beans.RequestScopedDelegateBean;
import com.camunda.fox.platform.test.functional.ejb.request.beans.InvocationCounter;
import com.camunda.fox.platform.test.functional.ejb.request.beans.InvocationCounterDelegateBean;
import com.camunda.fox.platform.test.functional.ejb.request.beans.InvocationCounterDelegateBeanLocal;
import com.camunda.fox.platform.test.functional.ejb.request.beans.InvocationCounterService;
import com.camunda.fox.platform.test.functional.ejb.request.beans.InvocationCounterServiceBean;
import com.camunda.fox.platform.test.functional.ejb.request.beans.InvocationCounterServiceLocal;
import com.camunda.fox.platform.test.functional.ejb.request.beans.RequestScopedSFSBDelegate;
import com.camunda.fox.platform.test.util.AbstractFoxPlatformIntegrationTest;

/**
 * This test verifies that if a delegate bean invoked from the Job Executor 
 * calls a LOCAL SLSB from a different deployment, the RequestContest is active there as well.
 *  
 * NOTE: 
 * - does not work on Jboss (Bug in Jboss AS?) SEE HEMERA-2453
 * - not implemented on Glassfish 
 * 
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class JobExecutorRequestContextLocalInvocationTest extends AbstractFoxPlatformIntegrationTest {
 
  @Deployment(name="pa", order=2)
  public static WebArchive processArchive() {    
    return initWebArchiveDeployment()
      .addClass(RequestScopedDelegateBean.class)            
      .addClass(RequestScopedSFSBDelegate.class)
      .addClass(InvocationCounterDelegateBean.class)
      .addClass(InvocationCounterDelegateBeanLocal.class)
      .addAsResource("com/camunda/fox/platform/test/functional/ejb/request/JobExecutorRequestContextLocalInvocationTest.testContextPropagationEjbLocal.bpmn20.xml")      
      .addAsWebInfResource("com/camunda/fox/platform/test/functional/ejb/request/jboss-deployment-structure.xml","jboss-deployment-structure.xml");
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
  public void testRequestContextPropagationEjbLocal() throws Exception{
    
    // This fails with  WELD-001303 No active contexts for scope type javax.enterprise.context.RequestScoped as well
    
//    InvocationCounterServiceLocal service = InitialContext.doLookup("java:/" +
//    "global/" +
//    "service/" +
//    "InvocationCounterServiceBean!com.camunda.fox.platform.test.functional.cdi.beans.InvocationCounterServiceLocal");
//    
//    service.getNumOfInvocations();
      
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testContextPropagationEjbLocal");    
    
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
