package org.camunda.bpm.engine.test.bpmn.async;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Assert;

public class AsyncStartEventTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testAsyncStartEvent() {
    runtimeService.startProcessInstanceByKey("asyncStartEvent");
    
    Task task = taskService.createTaskQuery().singleResult();
    Assert.assertNull("The user task should not have been reached yet", task);
    
    Assert.assertEquals(1, runtimeService.createExecutionQuery().activityId("startEvent").count());
    
    waitForJobExecutorToProcessAllJobs(6000L);
    task = taskService.createTaskQuery().singleResult();
    
    Assert.assertEquals(0, runtimeService.createExecutionQuery().activityId("startEvent").count());
    
    Assert.assertNotNull("The user task should have been reached", task);
  }
}
