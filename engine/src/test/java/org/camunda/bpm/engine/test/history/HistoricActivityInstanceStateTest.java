/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.history;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;

/**
 *
 * @author Nico Rehwaldt
 * @author Roman Smirnov
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class HistoricActivityInstanceStateTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testSingleEndEvent() {
    startProcess();

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "start", 1);
    assertNonCanceledActivityInstance(allInstances, "start");

    assertIsCompletingActivityInstances(allInstances, "end", 1);
    assertNonCanceledActivityInstance(allInstances, "end");
  }

  @Deployment
  public void testSingleEndActivity() {
    startProcess();

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "start", 1);
    assertNonCanceledActivityInstance(allInstances, "start");

    assertIsCompletingActivityInstances(allInstances, "end", 1);
    assertNonCanceledActivityInstance(allInstances, "end");
  }

  @Deployment
  public void testSingleEndEventAfterParallelJoin() {
    startProcess();

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "parallelJoin", 2);
    assertNonCanceledActivityInstance(allInstances, "parallelJoin");

    assertIsCompletingActivityInstances(allInstances, "end", 1);
    assertNonCanceledActivityInstance(allInstances, "end");
  }

  @Deployment
  public void testEndParallelJoin() {
    startProcess();

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "task1", 1);
    assertNonCanceledActivityInstance(allInstances, "task1");

    assertNonCompletingActivityInstance(allInstances, "task2", 1);
    assertNonCanceledActivityInstance(allInstances, "task2");

    assertIsCompletingActivityInstances(allInstances, "parallelJoinEnd", 2);
    assertNonCanceledActivityInstance(allInstances, "parallelJoinEnd");
  }

  @Deployment
  public void testTwoEndEvents() {
    startProcess();

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "parallelSplit", 1);
    assertNonCanceledActivityInstance(allInstances, "parallelSplit", 1);

    assertIsCompletingActivityInstances(allInstances, "end1", 1);
    assertNonCanceledActivityInstance(allInstances, "end1");

    assertIsCompletingActivityInstances(allInstances, "end2", 1);
    assertNonCanceledActivityInstance(allInstances, "end2");
  }

  @Deployment
  public void testTwoEndActivities() {
    startProcess();

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "parallelSplit", 1);
    assertNonCanceledActivityInstance(allInstances, "parallelSplit");

    assertIsCompletingActivityInstances(allInstances, "end1", 1);
    assertNonCanceledActivityInstance(allInstances, "end1");

    assertIsCompletingActivityInstances(allInstances, "end2", 1);
    assertNonCanceledActivityInstance(allInstances, "end2");
  }

  @Deployment
  public void testSingleEndEventAndSingleEndActivity() {
    startProcess();

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "parallelSplit", 1);
    assertNonCanceledActivityInstance(allInstances, "parallelSplit");

    assertIsCompletingActivityInstances(allInstances, "end1");
    assertNonCanceledActivityInstance(allInstances, "end1");

    assertIsCompletingActivityInstances(allInstances, "end2");
    assertNonCanceledActivityInstance(allInstances, "end2");
  }

  @Deployment
  public void testSimpleSubProcess() {
    startProcess();

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "intermediateSubprocess", 1);
    assertNonCanceledActivityInstance(allInstances, "intermediateSubprocess");

    assertIsCompletingActivityInstances(allInstances, "subprocessEnd", 1);
    assertNonCanceledActivityInstance(allInstances, "subprocessEnd");

    assertIsCompletingActivityInstances(allInstances, "end", 1);
    assertNonCanceledActivityInstance(allInstances, "end");
  }

  @Deployment
  public void testParallelMultiInstanceSubProcess() {
    startProcess();

    List<HistoricActivityInstance> activityInstances = getEndActivityInstances();

    assertEquals(7, activityInstances.size());

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertIsCompletingActivityInstances(allInstances, "intermediateSubprocess", 3);
    assertNonCanceledActivityInstance(allInstances, "intermediateSubprocess");

    assertIsCompletingActivityInstances(allInstances, "subprocessEnd", 3);
    assertNonCanceledActivityInstance(allInstances, "subprocessEnd");

    assertNonCompletingActivityInstance(allInstances, "intermediateSubprocess#multiInstanceBody", 1);
    assertNonCanceledActivityInstance(allInstances, "intermediateSubprocess#multiInstanceBody");

    assertIsCompletingActivityInstances(allInstances, "end", 1);
    assertNonCanceledActivityInstance(allInstances, "end");
  }

  @Deployment
  public void testSequentialMultiInstanceSubProcess() {
    startProcess();

    List<HistoricActivityInstance> activityInstances = getEndActivityInstances();

    assertEquals(7, activityInstances.size());

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertIsCompletingActivityInstances(allInstances, "intermediateSubprocess", 3);
    assertNonCanceledActivityInstance(allInstances, "intermediateSubprocess");

    assertIsCompletingActivityInstances(allInstances, "subprocessEnd", 3);
    assertNonCanceledActivityInstance(allInstances, "subprocessEnd");

    assertNonCompletingActivityInstance(allInstances, "intermediateSubprocess#multiInstanceBody", 1);
    assertNonCanceledActivityInstance(allInstances, "intermediateSubprocess#multiInstanceBody");

    assertIsCompletingActivityInstances(allInstances, "end", 1);
    assertNonCanceledActivityInstance(allInstances, "end");
  }

  @Deployment
  public void testIntermediateTask() {
    startProcess();

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "intermediateTask", 1);
    assertNonCanceledActivityInstance(allInstances, "intermediateTask");

    assertIsCompletingActivityInstances(allInstances, "end", 1);
    assertNonCanceledActivityInstance(allInstances, "end");
  }

  @Deployment
  public void testBoundaryErrorCancel() {
    ProcessInstance processInstance = startProcess();
    runtimeService.correlateMessage("continue");
    assertProcessEnded(processInstance.getId());


    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCanceledActivityInstance(allInstances, "start");
    assertNonCompletingActivityInstance(allInstances, "start");

    assertNonCanceledActivityInstance(allInstances, "subprocessStart");
    assertNonCompletingActivityInstance(allInstances, "subprocessStart");

    assertNonCanceledActivityInstance(allInstances, "gtw");
    assertNonCompletingActivityInstance(allInstances, "gtw");

    assertIsCanceledActivityInstances(allInstances, "subprocess", 1);
    assertNonCompletingActivityInstance(allInstances, "subprocess");

    assertIsCanceledActivityInstances(allInstances, "errorSubprocessEnd", 1);
    assertNonCompletingActivityInstance(allInstances, "errorSubprocessEnd");

    assertIsCanceledActivityInstances(allInstances, "userTask", 1);
    assertNonCompletingActivityInstance(allInstances, "userTask");

    assertNonCanceledActivityInstance(allInstances, "subprocessBoundary");
    assertNonCompletingActivityInstance(allInstances, "subprocessBoundary");

    assertNonCanceledActivityInstance(allInstances, "endAfterBoundary");
    assertIsCompletingActivityInstances(allInstances, "endAfterBoundary", 1);
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
    assertIsCanceledActivityInstances(allInstances, "subprocess", 1);

    assertIsCanceledActivityInstances(allInstances, "userTask", 1);
    assertNonCompletingActivityInstance(allInstances, "userTask");

    assertNonCanceledActivityInstance(allInstances, "subprocessBoundary");
    assertNonCompletingActivityInstance(allInstances, "subprocessBoundary");

    assertNonCanceledActivityInstance(allInstances, "endAfterBoundary");
    assertIsCompletingActivityInstances(allInstances, "endAfterBoundary", 1);
  }

  @Deployment
  public void testEventSubprocessErrorCancel() {
    ProcessInstance processInstance = startProcess();
    runtimeService.correlateMessage("continue");
    assertProcessEnded(processInstance.getId());

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertIsCanceledActivityInstances(allInstances, "userTask", 1);
    assertNonCompletingActivityInstance(allInstances, "userTask");

    assertIsCanceledActivityInstances(allInstances, "errorEnd", 1);
    assertNonCompletingActivityInstance(allInstances, "errorEnd");

    assertNonCanceledActivityInstance(allInstances, "eventSubprocessStart");
    assertNonCompletingActivityInstance(allInstances, "eventSubprocessStart");

    assertNonCanceledActivityInstance(allInstances, "eventSubprocessEnd");
    assertIsCompletingActivityInstances(allInstances, "eventSubprocessEnd", 1);
  }

  @Deployment
  public void testEventSubprocessMessageCancel() {
    startProcess();

    runtimeService.correlateMessage("message");

    assertNull(runtimeService.createProcessInstanceQuery().singleResult());

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertIsCanceledActivityInstances(allInstances, "userTask", 1);
    assertNonCompletingActivityInstance(allInstances, "userTask");

    assertNonCanceledActivityInstance(allInstances, "eventSubprocessStart");
    assertNonCompletingActivityInstance(allInstances, "eventSubprocessStart");

    assertNonCanceledActivityInstance(allInstances, "eventSubprocessEnd");
    assertIsCompletingActivityInstances(allInstances, "eventSubprocessEnd", 1);
  }

  @Deployment
  public void testEventSubprocessSignalCancel() {
    ProcessInstance processInstance = startProcess();
    runtimeService.correlateMessage("continue");
    assertProcessEnded(processInstance.getId());

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertIsCanceledActivityInstances(allInstances, "userTask", 1);
    assertNonCompletingActivityInstance(allInstances, "userTask");

    // fails due to CAM-4527: end execution listeners are executed twice for the signal end event
//    assertIsCanceledActivityInstances(allInstances, "signalEnd", 1);
//    assertNonCompletingActivityInstance(allInstances, "signalEnd");

    assertNonCanceledActivityInstance(allInstances, "eventSubprocessStart");
    assertNonCompletingActivityInstance(allInstances, "eventSubprocessStart");

    assertNonCanceledActivityInstance(allInstances, "eventSubprocessEnd");
    assertIsCompletingActivityInstances(allInstances, "eventSubprocessEnd", 1);
  }

  @Deployment
  public void testEndTerminateEventCancel() {
    ProcessInstance processInstance = startProcess();
    runtimeService.correlateMessage("continue");
    assertProcessEnded(processInstance.getId());

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertIsCanceledActivityInstances(allInstances, "userTask", 1);
    assertNonCompletingActivityInstance(allInstances, "userTask");

    assertNonCanceledActivityInstance(allInstances, "terminateEnd");
    assertIsCompletingActivityInstances(allInstances, "terminateEnd", 1);

  }

  @Deployment
  public void testEndTerminateEventCancelInSubprocess() {
    ProcessInstance processInstance = startProcess();
    runtimeService.correlateMessage("continue");
    assertProcessEnded(processInstance.getId());

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertNonCompletingActivityInstance(allInstances, "subprocess");
    assertNonCanceledActivityInstance(allInstances, "subprocess");

    assertIsCanceledActivityInstances(allInstances, "userTask", 1);
    assertNonCompletingActivityInstance(allInstances, "userTask");

    assertNonCanceledActivityInstance(allInstances, "terminateEnd");
    assertIsCompletingActivityInstances(allInstances, "terminateEnd", 1);

    assertIsCompletingActivityInstances(allInstances, "end", 1);
    assertNonCanceledActivityInstance(allInstances, "end");

  }

  @Deployment
  public void testEndTerminateEventCancelWithSubprocess() {
    ProcessInstance processInstance = startProcess();
    runtimeService.correlateMessage("continue");
    assertProcessEnded(processInstance.getId());

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertIsCanceledActivityInstances(allInstances, "subprocess", 1);
    assertNonCompletingActivityInstance(allInstances, "subprocess");

    assertIsCanceledActivityInstances(allInstances, "userTask", 1);
    assertNonCompletingActivityInstance(allInstances, "userTask");

    assertNonCanceledActivityInstance(allInstances, "terminateEnd");
    assertIsCompletingActivityInstances(allInstances, "terminateEnd", 1);

  }

  @Deployment (resources={ "org/camunda/bpm/engine/test/history/HistoricActivityInstanceStateTest.testCancelProcessInstanceInUserTask.bpmn",
      "org/camunda/bpm/engine/test/history/HistoricActivityInstanceStateTest.testEndTerminateEventWithCallActivity.bpmn" })
  public void testEndTerminateEventCancelWithCallActivity() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process1");
    runtimeService.correlateMessage("continue");
    assertProcessEnded(processInstance.getId());

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertIsCanceledActivityInstances(allInstances, "callActivity", 1);
    assertNonCompletingActivityInstance(allInstances, "callActivity");

    assertIsCanceledActivityInstances(allInstances, "userTask", 1);
    assertNonCompletingActivityInstance(allInstances, "userTask");

    assertNonCanceledActivityInstance(allInstances, "terminateEnd");
    assertIsCompletingActivityInstances(allInstances, "terminateEnd", 1);

  }

  @Deployment
  public void testCancelProcessInstanceInUserTask() {
    ProcessInstance processInstance = startProcess();

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertIsCanceledActivityInstances(allInstances, "userTask", 1);
    assertNonCompletingActivityInstance(allInstances, "userTask");

  }

  @Deployment
  public void testCancelProcessInstanceInSubprocess() {
    ProcessInstance processInstance = startProcess();

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertIsCanceledActivityInstances(allInstances, "userTask", 1);
    assertNonCompletingActivityInstance(allInstances, "userTask");

    assertIsCanceledActivityInstances(allInstances, "subprocess", 1);
    assertNonCompletingActivityInstance(allInstances, "subprocess");

  }

  @Deployment
  public void testCancelProcessWithParallelGateway() {
    ProcessInstance processInstance = startProcess();

    runtimeService.deleteProcessInstance(processInstance.getId(), "test");

    List<HistoricActivityInstance> allInstances = getAllActivityInstances();

    assertIsCanceledActivityInstances(allInstances, "userTask1", 1);
    assertNonCompletingActivityInstance(allInstances, "userTask1");

    assertIsCanceledActivityInstances(allInstances, "userTask2", 1);
    assertNonCompletingActivityInstance(allInstances, "userTask2");

    assertIsCanceledActivityInstances(allInstances, "subprocess", 1);
    assertNonCompletingActivityInstance(allInstances, "subprocess");

  }

  private void assertIsCanceledActivityInstances(List<HistoricActivityInstance> allInstances, String activityId) {
    assertIsCanceledActivityInstances(allInstances, activityId, -1);
  }

  private void assertIsCanceledActivityInstances(List<HistoricActivityInstance> allInstances, String activityId, int count) {
    assertCorrectCanceledState(allInstances, activityId, count, true);
  }

  private void assertNonCanceledActivityInstance(List<HistoricActivityInstance> instances, String activityId) {
    assertNonCanceledActivityInstance(instances, activityId, -1);
  }

  private void assertNonCanceledActivityInstance(List<HistoricActivityInstance> instances, String activityId, int count) {
    assertCorrectCanceledState(instances, activityId, count, false);
  }

  private void assertCorrectCanceledState(List<HistoricActivityInstance> allInstances, String activityId, int expectedCount, boolean canceled) {
    int found = 0;

    for (HistoricActivityInstance instance : allInstances) {
      if (instance.getActivityId().equals(activityId)) {
        found++;
        assertEquals(String.format("expect <%s> to be %scanceled", activityId, (canceled ? "" : "non-")), canceled, instance.isCanceled());
      }
    }

    assertTrue("contains entry for activity <" + activityId + ">", found > 0);

    if (expectedCount != -1) {
      assertTrue("contains <" + expectedCount + "> entries for activity <" + activityId + ">", found == expectedCount);
    }
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
