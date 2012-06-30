package com.camunda.fox.platform.test.functional.cdi;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.test.functional.cdi.beans.InvocationCounter;
import com.camunda.fox.platform.test.functional.cdi.beans.InvocationCounterDelegateBean;
import com.camunda.fox.platform.test.functional.cdi.beans.InvocationCounterDelegateBeanLocal;
import com.camunda.fox.platform.test.functional.cdi.beans.InvocationCounterService;
import com.camunda.fox.platform.test.functional.cdi.beans.InvocationCounterServiceBean;
import com.camunda.fox.platform.test.functional.cdi.beans.InvocationCounterServiceLocal;
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
 
  @Deployment(name="pa", order=2)
  public static WebArchive processArchive() {    
    return initWebArchiveDeployment()
      .addClass(RequestScopedDelegateBean.class)            
      .addClass(InvocationCounterDelegateBean.class)
      .addClass(InvocationCounterDelegateBeanLocal.class)
      .addAsResource("com/camunda/fox/platform/test/functional/cdi/JobExecutorRequestContextTest.testResolveBean.bpmn20.xml")
      .addAsResource("com/camunda/fox/platform/test/functional/cdi/JobExecutorRequestContextTest.testScoping.bpmn20.xml")
      .addAsResource("com/camunda/fox/platform/test/functional/cdi/JobExecutorRequestContextTest.testScopingExclusiveJobs.bpmn20.xml")
      .addAsResource("com/camunda/fox/platform/test/functional/cdi/JobExecutorRequestContextTest.testContextPropagationEjbRemote.bpmn20.xml")
      .addAsResource("com/camunda/fox/platform/test/functional/cdi/JobExecutorRequestContextTest.testContextPropagationEjbLocal.bpmn20.xml")      
      .addAsWebInfResource("com/camunda/fox/platform/test/functional/cdi/jboss-deployment-structure.xml","jboss-deployment-structure.xml");
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
  public void testResolveBean() {
    
    // verifies that @RequestScoped Beans can be resolved
    
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testResolveBean");
    
    waitForJobExecutorToProcessAllJobs(6000, 100);
    
    Object variable = runtimeService.getVariable(pi.getId(), "invocationCounter");
    Assert.assertEquals(1, variable);    
  }
  
  @Test
  @OperateOnDeployment("pa")
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
  
  @Test
  @OperateOnDeployment("pa")
  public void testScopingExclusiveJobs() {
    
    // verifies that if the same @RequestScoped Bean is invoked 
    // in the context of two subsequent exclusive jobs, we have 
    // seperate requests for each job, eben if the jobs are executed 
    // subsequently by the same thread. 
    
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testScopingExclusiveJobs");    
    
    waitForJobExecutorToProcessAllJobs(6000, 500);
    
    Object variable = runtimeService.getVariable(pi.getId(), "invocationCounter");
    // -> seperate requests
    Assert.assertEquals(1, variable);
    
    Task task = taskService.createTaskQuery()
      .processInstanceId(pi.getProcessInstanceId())
      .singleResult();
    taskService.complete(task.getId());
    
    waitForJobExecutorToProcessAllJobs(6000, 100);
    
    variable = runtimeService.getVariable(pi.getId(), "invocationCounter");
    Assert.assertEquals(1, variable);
    
  }
  
  @Ignore // does not work with a local invocation (Bug in Jboss AS?)
  @Test
  @OperateOnDeployment("pa")
  public void testRequestContextPropagationEjbLocal() throws Exception{
    
    // This test verifies that if a delegate bean invoked from the Job Executor 
    // calls an EJB from a different deployment, the RequestContest is active there as well.
    
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
  
  @Ignore // does not work with a remote invocation (Bug in Jboss AS?)
  @Test
  @OperateOnDeployment("pa")
  public void testRequestContextPropagationEjbRemote() {
    
    // This test verifies that if a delegate bean invoked from the Job Executor 
    // calls an EJB from a different deployment, the RequestContest is active there as well.
      
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testContextPropagationEjbRemote");    
    
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
