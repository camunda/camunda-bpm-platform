package org.camunda.bpm.integrationtest.jobexecutor;

import java.util.List;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.integrationtest.jobexecutor.beans.SampleServiceBean;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
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

  @Test
  public void testProcessExecution() {

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("TimerExecutionProcess");

    waitForJobExecutorToProcessAllJobs();

    List<ProcessInstance> finallyRunningInstances = runtimeService.createProcessInstanceQuery().processInstanceId(instance.getId()).list();
    Assert.assertEquals(0, finallyRunningInstances.size());

  }
}
