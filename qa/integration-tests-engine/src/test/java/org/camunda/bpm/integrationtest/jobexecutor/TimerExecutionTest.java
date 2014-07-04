package org.camunda.bpm.integrationtest.jobexecutor;

import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import org.junit.Assert;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.integrationtest.jobexecutor.beans.SampleServiceBean;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author nico.rehwaldt
 */
@RunWith(Arquillian.class)
public class TimerExecutionTest extends AbstractFoxPlatformIntegrationTest {

  @Deployment
  public static WebArchive processArchive() {
    WebArchive archive = initWebArchiveDeployment()
            .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/TimerExecution.bpmn20.xml")
            .addClass(SampleServiceBean.class);

    return archive;
  }

  @Inject
  private SampleServiceBean bean;

  @Test
  public void testProcessExecution() throws Exception {

    processEngineConfiguration.getJobExecutor().start();

    long deployedCount = repositoryService
      .createProcessDefinitionQuery()
      .processDefinitionKey("TimerExecutionProcess")
      .count();

    Assert.assertEquals(1, deployedCount);

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("TimerExecutionProcess");

    for (int i = 0; i < 100; i++) {
      Thread.sleep(1000/100);
      runtimeService.createProcessInstanceQuery().processInstanceId(instance.getId()).list();
      managementService.createJobQuery().duedateHigherThen(new Date()).list();
    }

    waitForJobExecutorToProcessAllJobs();

    List<ProcessInstance> finallyRunningInstances = runtimeService.createProcessInstanceQuery().processInstanceId(instance.getId()).list();
    Assert.assertEquals(0, finallyRunningInstances.size());
  }
}
