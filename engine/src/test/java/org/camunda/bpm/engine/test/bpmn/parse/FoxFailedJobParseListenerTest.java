package org.camunda.bpm.engine.test.bpmn.parse;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessInstanceWithVariablesImpl;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;

import static org.camunda.bpm.engine.impl.bpmn.parser.DefaultFailedJobParseListener.FAILED_JOB_CONFIGURATION;

public class FoxFailedJobParseListenerTest extends PluggableProcessEngineTestCase {

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/parse/FoxFailedJobParseListenerTest.testUserTask.bpmn20.xml" })
  public void testUserTaskParseFailedJobRetryTimeCycle() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("asyncUserTaskFailedJobRetryTimeCycle");

    ActivityImpl userTask = findActivity(pi, "task");
    checkFoxFailedJobConfig(userTask);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/parse/CamundaFailedJobParseListenerTest.testUserTask.bpmn20.xml" })
  public void testUserTaskParseFailedJobRetryTimeCycleInActivitiNamespace() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("asyncUserTaskFailedJobRetryTimeCycle");

    ActivityImpl userTask = findActivity(pi, "task");
    checkFoxFailedJobConfig(userTask);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/parse/FoxFailedJobParseListenerTest.testUserTask.bpmn20.xml" })
  public void testNotAsyncUserTaskParseFailedJobRetryTimeCycle() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("notAsyncUserTaskFailedJobRetryTimeCycle");

    ActivityImpl userTask = findActivity(pi, "notAsyncTask");
    checkNotContainingFoxFailedJobConfig(userTask);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/parse/FoxFailedJobParseListenerTest.testUserTask.bpmn20.xml" })
  public void testAsyncUserTaskButWithoutParseFailedJobRetryTimeCycle() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("asyncUserTaskButWithoutFailedJobRetryTimeCycle");

    ActivityImpl userTask = findActivity(pi, "asyncTaskWithoutFailedJobRetryTimeCycle");
    checkNotContainingFoxFailedJobConfig(userTask);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/parse/FoxFailedJobParseListenerTest.testTimer.bpmn20.xml" })
  public void testTimerBoundaryEventWithFailedJobRetryTimeCycle() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("boundaryEventWithFailedJobRetryTimeCycle");

    ActivityImpl boundaryActivity = findActivity(pi, "boundaryTimerWithFailedJobRetryTimeCycle");
    checkFoxFailedJobConfig(boundaryActivity);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/parse/FoxFailedJobParseListenerTest.testTimer.bpmn20.xml" })
  public void testTimerBoundaryEventWithoutFailedJobRetryTimeCycle() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("boundaryEventWithoutFailedJobRetryTimeCycle");

    ActivityImpl boundaryActivity = findActivity(pi, "boundaryTimerWithoutFailedJobRetryTimeCycle");
    checkNotContainingFoxFailedJobConfig(boundaryActivity);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/parse/FoxFailedJobParseListenerTest.testTimer.bpmn20.xml" })
  public void testTimerStartEventWithFailedJobRetryTimeCycle() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("startEventWithFailedJobRetryTimeCycle");

    ActivityImpl startEvent = findActivity(pi, "startEventFailedJobRetryTimeCycle");
    checkFoxFailedJobConfig(startEvent);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/parse/FoxFailedJobParseListenerTest.testTimer.bpmn20.xml" })
  public void testIntermediateCatchTimerEventWithFailedJobRetryTimeCycle() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("intermediateTimerEventWithFailedJobRetryTimeCycle");

    ActivityImpl timer = findActivity(pi, "timerEventWithFailedJobRetryTimeCycle");
    checkFoxFailedJobConfig(timer);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/parse/FoxFailedJobParseListenerTest.testSignal.bpmn20.xml" })
  public void testSignalEventWithFailedJobRetryTimeCycle() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("signalEventWithFailedJobRetryTimeCycle");

    ActivityImpl signal = findActivity(pi, "signalWithFailedJobRetryTimeCycle");
    checkFoxFailedJobConfig(signal);
  }

  @Deployment
  public void testMultiInstanceBodyWithFailedJobRetryTimeCycle() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    ActivityImpl miBody = findMultiInstanceBody(pi, "task");
    checkFoxFailedJobConfig(miBody);

    ActivityImpl innerActivity = findActivity(pi, "task");
    checkNotContainingFoxFailedJobConfig(innerActivity);
  }

  @Deployment
  public void testInnerMultiInstanceActivityWithFailedJobRetryTimeCycle() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    ActivityImpl miBody = findMultiInstanceBody(pi, "task");
    checkNotContainingFoxFailedJobConfig(miBody);

    ActivityImpl innerActivity = findActivity(pi, "task");
    checkFoxFailedJobConfig(innerActivity);
  }

  @Deployment
  public void testMultiInstanceBodyAndInnerActivityWithFailedJobRetryTimeCycle() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("process");

    ActivityImpl miBody = findMultiInstanceBody(pi, "task");
    checkFoxFailedJobConfig(miBody);

    ActivityImpl innerActivity = findActivity(pi, "task");
    checkFoxFailedJobConfig(innerActivity);
  }

  protected ActivityImpl findActivity(ProcessInstance pi, String activityId) {

    ProcessInstanceWithVariablesImpl entity = (ProcessInstanceWithVariablesImpl) pi;
    ProcessDefinitionEntity processDefEntity = entity.getExecutionEntity().getProcessDefinition();

    assertNotNull(processDefEntity);
    ActivityImpl activity = processDefEntity.findActivity(activityId);
    assertNotNull(activity);
    return activity;
  }

  protected ActivityImpl findMultiInstanceBody(ProcessInstance pi, String activityId) {
    return findActivity(pi, activityId + BpmnParse.MULTI_INSTANCE_BODY_ID_SUFFIX);
  }

  protected void checkFoxFailedJobConfig(ActivityImpl activity) {
    assertNotNull(activity);

    assertTrue(activity.getProperties().contains(FAILED_JOB_CONFIGURATION));

    Object value = activity.getProperties().get(FAILED_JOB_CONFIGURATION).getRetryIntervals().get(0);
    assertEquals("R5/PT5M", value);
  }

  protected void checkNotContainingFoxFailedJobConfig(ActivityImpl activity) {
    assertFalse(activity.getProperties().contains(FAILED_JOB_CONFIGURATION));
  }

}
