package org.camunda.bpm.engine.test.bpmn.parse;

import org.camunda.bpm.engine.impl.bpmn.parser.FoxFailedJobParseListener;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;


public class FoxFailedJobParseListenerTest extends PluggableProcessEngineTestCase {

  @Deployment
          (resources = 
                { "org/camunda/bpm/engine/test/bpmn/parse/FoxFailedJobParseListenerTest.testUserTask.bpmn20.xml" })
  public void testUserTaskParseFailedJobRetryTimeCycle() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("asyncUserTaskFailedJobRetryTimeCycle");

    assertTrue(pi instanceof ExecutionEntity);
    ExecutionEntity execution = (ExecutionEntity) pi;

    ProcessDefinitionImpl processDefinition = execution.getProcessDefinition();
    assertNotNull(processDefinition);

    ActivityImpl userTask = processDefinition.findActivity("task");
    assertNotNull(userTask);

    this.checkFoxFailedJobConfig(userTask);
  }

  @Deployment
      (resources =
          { "org/camunda/bpm/engine/test/bpmn/parse/CamundaFailedJobParseListenerTest.testUserTask.bpmn20.xml" })
  public void testUserTaskParseFailedJobRetryTimeCycleInActivitiNamespace() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("asyncUserTaskFailedJobRetryTimeCycle");

    assertTrue(pi instanceof ExecutionEntity);
    ExecutionEntity execution = (ExecutionEntity) pi;

    ProcessDefinitionImpl processDefinition = execution.getProcessDefinition();
    assertNotNull(processDefinition);

    ActivityImpl userTask = processDefinition.findActivity("task");
    assertNotNull(userTask);

    this.checkFoxFailedJobConfig(userTask);
  }

  @Deployment
          (resources = 
              { "org/camunda/bpm/engine/test/bpmn/parse/FoxFailedJobParseListenerTest.testUserTask.bpmn20.xml" })
  public void testNotAsyncUserTaskParseFailedJobRetryTimeCycle() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("notAsyncUserTaskFailedJobRetryTimeCycle");

    assertTrue(pi instanceof ExecutionEntity);
    ExecutionEntity execution = (ExecutionEntity) pi;

    ProcessDefinitionImpl processDefinition = execution.getProcessDefinition();
    assertNotNull(processDefinition);

    ActivityImpl userTask = processDefinition.findActivity("notAsyncTask");
    assertNotNull(userTask);

    this.checkNotContainingFoxFailedJobConfig(userTask);
  }
  
  @Deployment
          (resources = 
                { "org/camunda/bpm/engine/test/bpmn/parse/FoxFailedJobParseListenerTest.testUserTask.bpmn20.xml" })
  public void testAsyncUserTaskButWithoutParseFailedJobRetryTimeCycle() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("asyncUserTaskButWithoutFailedJobRetryTimeCycle");

    assertTrue(pi instanceof ExecutionEntity);
    ExecutionEntity execution = (ExecutionEntity) pi;

    ProcessDefinitionImpl processDefinition = execution.getProcessDefinition();
    assertNotNull(processDefinition);

    ActivityImpl userTask = processDefinition.findActivity("asyncTaskWithoutFailedJobRetryTimeCycle");
    assertNotNull(userTask);
    assertTrue(userTask.isAsync());

    this.checkNotContainingFoxFailedJobConfig(userTask);
  }
  
  @Deployment
          (resources = 
                { "org/camunda/bpm/engine/test/bpmn/parse/FoxFailedJobParseListenerTest.testTimer.bpmn20.xml" })
  public void testTimerBoundaryEventWithFailedJobRetryTimeCycle() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("boundaryEventWithFailedJobRetryTimeCycle");
    
    assertTrue(pi instanceof ExecutionEntity);
    ExecutionEntity execution = (ExecutionEntity) pi;
    
    ProcessDefinitionImpl processDefinition = execution.getProcessDefinition();
    assertNotNull(processDefinition);

    ActivityImpl boundaryActivity = processDefinition.findActivity("boundaryTimerWithFailedJobRetryTimeCycle");
    assertNotNull(boundaryActivity);
    
    this.checkFoxFailedJobConfig(boundaryActivity);
  }
  
  @Deployment
    (resources = 
          { "org/camunda/bpm/engine/test/bpmn/parse/FoxFailedJobParseListenerTest.testTimer.bpmn20.xml" })
  public void testTimerBoundaryEventWithoutFailedJobRetryTimeCycle() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("boundaryEventWithoutFailedJobRetryTimeCycle");

    assertTrue(pi instanceof ExecutionEntity);
    ExecutionEntity execution = (ExecutionEntity) pi;

    ProcessDefinitionImpl processDefinition = execution.getProcessDefinition();
    assertNotNull(processDefinition);

    ActivityImpl boundaryActivity = processDefinition.findActivity("boundaryTimerWithoutFailedJobRetryTimeCycle");
    assertNotNull(boundaryActivity);

    this.checkNotContainingFoxFailedJobConfig(boundaryActivity);
  }
  
  @Deployment
          (resources = 
                { "org/camunda/bpm/engine/test/bpmn/parse/FoxFailedJobParseListenerTest.testTimer.bpmn20.xml" })
  public void testTimerStartEventWithFailedJobRetryTimeCycle() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("startEventWithFailedJobRetryTimeCycle");
  
    assertTrue(pi instanceof ExecutionEntity);
    ExecutionEntity execution = (ExecutionEntity) pi;
  
    ProcessDefinitionImpl processDefinition = execution.getProcessDefinition();
    assertNotNull(processDefinition);
  
    ActivityImpl startEvent = processDefinition.findActivity("startEventFailedJobRetryTimeCycle");
    assertNotNull(startEvent);
  
    this.checkFoxFailedJobConfig(startEvent);
  }
  
  @Deployment
          (resources = 
                { "org/camunda/bpm/engine/test/bpmn/parse/FoxFailedJobParseListenerTest.testTimer.bpmn20.xml" })
  public void testIntermediateCatchTimerEventWithFailedJobRetryTimeCycle() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("intermediateTimerEventWithFailedJobRetryTimeCycle");
    
    assertTrue(pi instanceof ExecutionEntity);
    ExecutionEntity execution = (ExecutionEntity) pi;
    
    ProcessDefinitionImpl processDefinition = execution.getProcessDefinition();
    assertNotNull(processDefinition);
    
    ActivityImpl timer = processDefinition.findActivity("timerEventWithFailedJobRetryTimeCycle");
    assertNotNull(timer);
    
    this.checkFoxFailedJobConfig(timer);
  }
  
  @Deployment
          (resources = 
                { "org/camunda/bpm/engine/test/bpmn/parse/FoxFailedJobParseListenerTest.testSignal.bpmn20.xml" })
  public void testSignalEventWithFailedJobRetryTimeCycle() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("signalEventWithFailedJobRetryTimeCycle");
    
    assertTrue(pi instanceof ExecutionEntity);
    ExecutionEntity execution = (ExecutionEntity) pi;
    
    ProcessDefinitionImpl processDefinition = execution.getProcessDefinition();
    assertNotNull(processDefinition);
    
    ActivityImpl signal = processDefinition.findActivity("signalWithFailedJobRetryTimeCycle");
    assertNotNull(signal);
    
    this.checkFoxFailedJobConfig(signal);
  }

  private void checkFoxFailedJobConfig(ActivityImpl activity) {
    assertTrue(activity.getProperties().containsKey(FoxFailedJobParseListener.FOX_FAILED_JOB_CONFIGURATION));

    Object value = activity.getProperties().get(FoxFailedJobParseListener.FOX_FAILED_JOB_CONFIGURATION);
    assertNotNull(value);
    assertEquals("R5/PT5M", value);
  }

  private void checkNotContainingFoxFailedJobConfig(ActivityImpl activity) {
    assertFalse(activity.getProperties().containsKey(FoxFailedJobParseListener.FOX_FAILED_JOB_CONFIGURATION));
  }

}
