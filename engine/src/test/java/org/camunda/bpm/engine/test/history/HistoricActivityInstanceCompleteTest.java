package org.camunda.bpm.engine.test.history;

import java.util.List;

import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;

public class HistoricActivityInstanceCompleteTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testSingleEndEvent() {
    startProcess();

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "start", 1);

    assertIsCompletingActivityInstances(allInstances, "end", 1);
  }

  @Deployment
  public void testSingleEndActivity() {
    startProcess();

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "start", 1);

    assertIsCompletingActivityInstances(allInstances, "end", 1);
  }

  @Deployment
  public void testSingleEndEventAfterParallelJoin() {
    startProcess();

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "parallelJoin", 2);

    assertIsCompletingActivityInstances(allInstances, "end", 1);
  }

  @Deployment
  public void testEndParallelJoin() {
    startProcess();

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "task1");
    assertNonCompletingActivityInstance(allInstances, "task2");

    assertIsCompletingActivityInstances(allInstances, "parallelJoinEnd");
  }

  @Deployment
  public void testTwoEndEvents() {
    startProcess();

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "parallelSplit");

    assertIsCompletingActivityInstances(allInstances, "end1");
    assertIsCompletingActivityInstances(allInstances, "end2");
  }

  @Deployment
  public void testTwoEndActivities() {
    startProcess();

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "parallelSplit");

    assertIsCompletingActivityInstances(allInstances, "end1");
    assertIsCompletingActivityInstances(allInstances, "end2");
  }

  @Deployment
  public void testSingleEndEventAndSingleEndActivity() {
    startProcess();

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "parallelSplit");

    assertIsCompletingActivityInstances(allInstances, "end1");
    assertIsCompletingActivityInstances(allInstances, "end2");
  }

  @Deployment
  public void testSimpleSubProcess() {
    startProcess();

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "intermediateSubprocess");

    assertIsCompletingActivityInstances(allInstances, "subprocessEnd");
    assertIsCompletingActivityInstances(allInstances, "end");
  }

  @Deployment
  public void testParallelMultiInstanceSubProcess() {
    startProcess();

    List<HistoricActivityInstance> activityInstances = getEndActivityInstances();

    assertEquals(4, activityInstances.size());

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "intermediateSubprocess");

    assertIsCompletingActivityInstances(allInstances, "subprocessEnd", 3);
    assertIsCompletingActivityInstances(allInstances, "end", 1);
  }

  @Deployment
  public void testSequentialMultiInstanceSubProcess() {
    startProcess();

    List<HistoricActivityInstance> activityInstances = getEndActivityInstances();

    assertEquals(4, activityInstances.size());

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "intermediateSubprocess");

    assertIsCompletingActivityInstances(allInstances, "subprocessEnd", 3);
    assertIsCompletingActivityInstances(allInstances, "end", 1);
  }

  @Deployment
  public void testIntermediateTask() {
    startProcess();

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "intermediateTask");

    assertIsCompletingActivityInstances(allInstances, "end", 1);
  }

  @Deployment
  public void testBoundarySignalCancel() {
    ProcessInstance processInstance = startProcess();

    // should wait in user task
    assertFalse(processInstance.isEnded());

    // signal sub process
    runtimeService.signalEventReceived("interrupt");

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "subprocess");

    assertIsCompletingActivityInstances(allInstances, "subprocessBoundary", 1);
    assertIsCompletingActivityInstances(allInstances, "endAfterBoundary", 1);
  }

  @Deployment
  public void testBoundaryErrorCancel() {
    ProcessInstance processInstance = startProcess();

    assertTrue(processInstance.isEnded());

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "subprocess");

    assertIsCompletingActivityInstances(allInstances, "errorSubprocessEnd", 1);

    assertIsCompletingActivityInstances(allInstances, "subprocessBoundary", 1);
    assertIsCompletingActivityInstances(allInstances, "endAfterBoundary", 1);
  }

  @Deployment
  public void testEventSubprocessErrorCancel() {
    ProcessInstance processInstance = startProcess();

    assertTrue(processInstance.isEnded());

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "userTask");

    assertIsCompletingActivityInstances(allInstances, "errorEnd", 1);

    assertIsCompletingActivityInstances(allInstances, "eventSubprocess", 1);
    assertIsCompletingActivityInstances(allInstances, "eventSubprocessEnd", 1);
  }

  @Deployment
  public void testEventSubprocessSignalCancel() {
    ProcessInstance processInstance = startProcess();

    assertTrue(processInstance.isEnded());

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "userTask");

    assertIsCompletingActivityInstances(allInstances, "errorEnd", 1);

    assertIsCompletingActivityInstances(allInstances, "eventSubprocess", 1);
    assertIsCompletingActivityInstances(allInstances, "eventSubprocessEnd", 1);
  }

  private void assertIsCompletingActivityInstances(List<HistoricActivityInstance> allInstances, String activityId) {
    assertIsCompletingActivityInstances(allInstances, activityId, -1);
  }

  private void assertIsCompletingActivityInstances(List<HistoricActivityInstance> allInstances, String activityId, int count) {
    assertCorrectCompletingState(allInstances, activityId, count, true);
  }

  private void assertNonCompletingActivityInstance(List<HistoricActivityInstance> instances, String activityId) {
    assertNonCompletingActivityInstance(instances, activityId, -1);
  }

  private void assertNonCompletingActivityInstance(List<HistoricActivityInstance> instances, String activityId, int count) {
    assertCorrectCompletingState(instances, activityId, count, false);
  }

  private void assertCorrectCompletingState(List<HistoricActivityInstance> allInstances, String activityId, int expectedCount, boolean completing) {
    int found = 0;

    for (HistoricActivityInstance instance : allInstances) {
      if (instance.getActivityId().equals(activityId)) {
        found++;
        assertEquals(String.format("expect <%s> to be %scompleting", activityId, (completing ? "" : "non-")), completing, instance.isCompleteScope());
      }
    }

    assertTrue("contains entry for activity <" + activityId + ">", found > 0);

    if (expectedCount != -1) {
      assertTrue("contains <" + expectedCount + "> entries for activity <" + activityId + ">", found == expectedCount);
    }
  }

  private List<HistoricActivityInstance> getEndActivityInstances() {
    return historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceEndTime().asc().completeScope().list();
  }

  private List<HistoricActivityInstance> getAllActivityInstances() {
    return historyService.createHistoricActivityInstanceQuery().orderByHistoricActivityInstanceStartTime().asc().list();
  }

  private ProcessInstance startProcess() {
    return runtimeService.startProcessInstanceByKey("process");
  }
}
