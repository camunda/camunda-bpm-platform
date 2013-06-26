package org.camunda.bpm.integrationtest.jobexecutor;

import static org.junit.Assert.assertEquals;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.integrationtest.util.AbstractFoxPlatformIntegrationTest;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class AsyncIntermediateThrowSignalEventTest extends AbstractFoxPlatformIntegrationTest {
  
  @Deployment
  public static WebArchive processArchive() {
    return initWebArchiveDeployment()
            .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/AsyncIntermediateThrowSignalEventTest.catchAlertSignalBoundaryWithBoundarySignalEvent.bpmn20.xml")
            .addAsResource("org/camunda/bpm/integrationtest/jobexecutor/AsyncIntermediateThrowSignalEventTest.throwAlertSignalWithIntermediateCatchSignalEvent.bpmn20.xml");
  }
  
  @Test
  public void testAsyncSignalEvent() throws InterruptedException {
    ProcessInstance piCatchSignal = runtimeService.startProcessInstanceByKey("catchSignal");

    ProcessInstance piThrowSignal = runtimeService.startProcessInstanceByKey("throwSignal");

    waitForJobExecutorToProcessAllJobs(2000);

    assertEquals(1, runtimeService.createExecutionQuery().processInstanceId(piCatchSignal.getProcessInstanceId()).activityId("receiveTask").count());
    assertEquals(1, runtimeService.createExecutionQuery().processInstanceId(piThrowSignal.getProcessInstanceId()).activityId("receiveTask").count());

    // clean up
    runtimeService.signal(piCatchSignal.getId());
    runtimeService.signal(piThrowSignal.getId());

    assertEquals(0, runtimeService.createExecutionQuery().processInstanceId(piCatchSignal.getProcessInstanceId()).count());
    assertEquals(0, runtimeService.createExecutionQuery().processInstanceId(piThrowSignal.getProcessInstanceId()).count());
  }


}
