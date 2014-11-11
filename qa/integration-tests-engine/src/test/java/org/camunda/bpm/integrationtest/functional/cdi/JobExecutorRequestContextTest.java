package org.camunda.bpm.integrationtest.functional.cdi;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.integrationtest.functional.cdi.beans.RequestScopedDelegateBean;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * These test cases verify that the CDI RequestContext is active,
 * when the job executor executes a job and is scoped as expected
 *
 * @author Daniel Meyer
 *
 */
@RunWith(Arquillian.class)
public class JobExecutorRequestContextTest extends AbstractFoxPlatformIntegrationTest {

  /**
   *
   */
  private static final int _6000 = 6000;

  @Deployment
  public static WebArchive processArchive() {
    return initWebArchiveDeployment()
      .addClass(RequestScopedDelegateBean.class)
      .addAsResource("org/camunda/bpm/integrationtest/functional/cdi/JobExecutorRequestContextTest.testResolveBean.bpmn20.xml")
      .addAsResource("org/camunda/bpm/integrationtest/functional/cdi/JobExecutorRequestContextTest.testScoping.bpmn20.xml")
      .addAsResource("org/camunda/bpm/integrationtest/functional/cdi/JobExecutorRequestContextTest.testScopingExclusiveJobs.bpmn20.xml");
  }


  @Test
  public void testResolveBean() {

    // verifies that @RequestScoped Beans can be resolved

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testResolveBean");

    waitForJobExecutorToProcessAllJobs();

    Object variable = runtimeService.getVariable(pi.getId(), "invocationCounter");
    Assert.assertEquals(1, variable);
  }

  @Test
  public void testScoping() {

    // verifies that if the same @RequestScoped Bean is invoked multiple times
    // in the context of the same job, we get the same instance

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testScoping");

    waitForJobExecutorToProcessAllJobs();

    Object variable = runtimeService.getVariable(pi.getId(), "invocationCounter");
    // -> the same bean instance was invoked 2 times!
    Assert.assertEquals(2, variable);

    Task task = taskService.createTaskQuery()
      .processInstanceId(pi.getProcessInstanceId())
      .singleResult();
    taskService.complete(task.getId());

    waitForJobExecutorToProcessAllJobs();

    variable = runtimeService.getVariable(pi.getId(), "invocationCounter");
    // now it's '1' again! -> new instance of the bean
    Assert.assertEquals(1, variable);

  }

  @Test
  public void testScopingExclusiveJobs() {

    // verifies that if the same @RequestScoped Bean is invoked
    // in the context of two subsequent exclusive jobs, we have
    // seperate requests for each job, eben if the jobs are executed
    // subsequently by the same thread.

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testScopingExclusiveJobs");

    waitForJobExecutorToProcessAllJobs();

    Object variable = runtimeService.getVariable(pi.getId(), "invocationCounter");
    // -> seperate requests
    Assert.assertEquals(1, variable);

    Task task = taskService.createTaskQuery()
      .processInstanceId(pi.getProcessInstanceId())
      .singleResult();
    taskService.complete(task.getId());

    waitForJobExecutorToProcessAllJobs();

    variable = runtimeService.getVariable(pi.getId(), "invocationCounter");
    Assert.assertEquals(1, variable);

  }

}
