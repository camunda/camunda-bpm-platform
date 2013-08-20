package org.camunda.bpm.engine.test.bpmn.async;

import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
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
  
  @Deployment
  public void testAsyncStartEventListeners() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("asyncStartEvent");
    
    Assert.assertNull(runtimeService.getVariable(instance.getId(), "listener"));
    
    waitForJobExecutorToProcessAllJobs(6000L);
    
    Assert.assertNotNull(runtimeService.getVariable(instance.getId(), "listener"));
  }
  
  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/async/AsyncStartEventTest.testAsyncStartEvent.bpmn20.xml")
  public void testAsyncStartEventHistory() {
    runtimeService.startProcessInstanceByKey("asyncStartEvent");
    
    HistoricProcessInstance historicInstance = historyService.createHistoricProcessInstanceQuery().singleResult();
    Assert.assertNotNull(historicInstance);
    Assert.assertNotNull(historicInstance.getStartTime());
    
    HistoricActivityInstance historicStartEvent = historyService.createHistoricActivityInstanceQuery().singleResult();
    Assert.assertNull(historicStartEvent);
  }
}
