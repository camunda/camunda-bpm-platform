package com.camunda.fox.platform.test.jobexecutor;

import static org.junit.Assert.assertEquals;

import org.activiti.engine.runtime.ProcessInstance;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.camunda.fox.platform.test.util.AbstractFoxPlatformIntegrationTest;

@RunWith(Arquillian.class)
public class AsyncIntermediateThrowSignalEventTest extends AbstractFoxPlatformIntegrationTest {
  
  @Deployment
  public static WebArchive processArchive() {
    return initWebArchiveDeployment()
            .addAsResource("com/camunda/fox/platform/test/jobexecutor/AsyncIntermediateThrowSignalEventTest.catchAlertSignalBoundaryWithBoundarySignalEvent.bpmn20.xml")
            .addAsResource("com/camunda/fox/platform/test/jobexecutor/AsyncIntermediateThrowSignalEventTest.throwAlertSignalWithIntermediateCatchSignalEvent.bpmn20.xml")
            .addAsWebInfResource("persistence.xml", "classes/META-INF/persistence.xml");
  }
  
  @Test
  public void testAsyncSignalEvent() throws InterruptedException {
    ProcessInstance piCatchSignal = runtimeService.startProcessInstanceByKey("catchSignal");

    ProcessInstance piThrowSignal = runtimeService.startProcessInstanceByKey("throwSignal");

    waitForJobExecutorToProcessAllJobs(2000, 200);

    assertEquals(1, runtimeService.createExecutionQuery().processInstanceId(piCatchSignal.getProcessInstanceId()).activityId("receiveTask").count());
    assertEquals(1, runtimeService.createExecutionQuery().processInstanceId(piThrowSignal.getProcessInstanceId()).activityId("receiveTask").count());

    // clean up
    runtimeService.signal(piCatchSignal.getId());
    runtimeService.signal(piThrowSignal.getId());

    assertEquals(0, runtimeService.createExecutionQuery().processInstanceId(piCatchSignal.getProcessInstanceId()).count());
    assertEquals(0, runtimeService.createExecutionQuery().processInstanceId(piThrowSignal.getProcessInstanceId()).count());
  }


}
