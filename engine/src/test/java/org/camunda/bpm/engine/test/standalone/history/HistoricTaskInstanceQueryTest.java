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
package org.camunda.bpm.engine.test.standalone.history;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Thorben Lindhauer
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class HistoricTaskInstanceQueryTest extends PluggableProcessEngineTestCase {


  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessVariableValueEqualsNumber() throws Exception {
    // long
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123L));

    // non-matching long
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 12345L));

    // short
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", (short) 123));

    // double
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123.0d));

    // integer
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", 123));

    // untyped null (should not match)
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", null));

    // typed null (should not match)
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", Variables.longValue(null)));

    runtimeService.startProcessInstanceByKey("oneTaskProcess",
        Collections.<String, Object>singletonMap("var", "123"));

    assertEquals(4, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("var", Variables.numberValue(123)).count());
    assertEquals(4, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("var", Variables.numberValue(123L)).count());
    assertEquals(4, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("var", Variables.numberValue(123.0d)).count());
    assertEquals(4, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("var", Variables.numberValue((short) 123)).count());

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueEquals("var", Variables.numberValue(null)).count());

    assertEquals(8, historyService.createHistoricTaskInstanceQuery().processVariableValueNotEquals("var", 999L).count());
    assertEquals(8, historyService.createHistoricTaskInstanceQuery().processVariableValueNotEquals("var",  (short) 999).count());
    assertEquals(8, historyService.createHistoricTaskInstanceQuery().processVariableValueNotEquals("var", 999).count());
    assertEquals(8, historyService.createHistoricTaskInstanceQuery().processVariableValueNotEquals("var", "999").count());
    assertEquals(8, historyService.createHistoricTaskInstanceQuery().processVariableValueNotEquals("var", false).count());

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessVariableValueLike() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
            Collections.<String, Object>singletonMap("requester", "vahid alizadeh"));

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueLike("requester", "vahid%").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueLike("requester", "%alizadeh").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueLike("requester", "%ali%").count());

    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueLike("requester", "requester%").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueLike("requester", "%ali").count());

    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueLike("requester", "vahid").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().processVariableValueLike("nonExistingVar", "string%").count());

    // test with null value
    try {
      historyService.createHistoricTaskInstanceQuery().processVariableValueLike("requester", null).count();
      fail("expected exception");
    } catch (final ProcessEngineException e) {/*OK*/}
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessVariableValueGreaterThan() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
            Collections.<String, Object>singletonMap("requestNumber", 123));

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueGreaterThan("requestNumber", 122).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessVariableValueGreaterThanOrEqual() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
            Collections.<String, Object>singletonMap("requestNumber", 123));

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueGreaterThanOrEquals("requestNumber", 122).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueGreaterThanOrEquals("requestNumber", 123).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessVariableValueLessThan() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
            Collections.<String, Object>singletonMap("requestNumber", 123));

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueLessThan("requestNumber", 124).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testProcessVariableValueLessThanOrEqual() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess",
            Collections.<String, Object>singletonMap("requestNumber", 123));

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueLessThanOrEquals("requestNumber", 123).count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().processVariableValueLessThanOrEquals("requestNumber", 124).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  public void testTaskVariableValueEqualsNumber() throws Exception {
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    List<Task> tasks = taskService.createTaskQuery().processDefinitionKey("oneTaskProcess").list();
    assertEquals(8, tasks.size());
    taskService.setVariableLocal(tasks.get(0).getId(), "var", 123L);
    taskService.setVariableLocal(tasks.get(1).getId(), "var", 12345L);
    taskService.setVariableLocal(tasks.get(2).getId(), "var", (short) 123);
    taskService.setVariableLocal(tasks.get(3).getId(), "var", 123.0d);
    taskService.setVariableLocal(tasks.get(4).getId(), "var", 123);
    taskService.setVariableLocal(tasks.get(5).getId(), "var", null);
    taskService.setVariableLocal(tasks.get(6).getId(), "var", Variables.longValue(null));
    taskService.setVariableLocal(tasks.get(7).getId(), "var", "123");

    assertEquals(4, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("var", Variables.numberValue(123)).count());
    assertEquals(4, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("var", Variables.numberValue(123L)).count());
    assertEquals(4, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("var", Variables.numberValue(123.0d)).count());
    assertEquals(4, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("var", Variables.numberValue((short) 123)).count());

    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskVariableValueEquals("var", Variables.numberValue(null)).count());
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testTaskInvolvedUser() {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();
    // if
    identityService.setAuthenticatedUserId("aAssignerId");
    taskService.addCandidateUser(taskId, "aUserId");
    taskService.addCandidateUser(taskId, "bUserId");
    taskService.deleteCandidateUser(taskId, "aUserId");
    taskService.deleteCandidateUser(taskId, "bUserId");
    Task taskAssignee = taskService.newTask("newTask");
    taskAssignee.setAssignee("aUserId");
    taskService.saveTask(taskAssignee);
    // query test
    assertEquals(2, historyService.createHistoricTaskInstanceQuery().taskInvolvedUser("aUserId").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskInvolvedUser("bUserId").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskInvolvedUser("invalidUserId").count());
    taskService.deleteTask("newTask",true);
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testTaskInvolvedGroup() {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();
    // if
    identityService.setAuthenticatedUserId("aAssignerId");
    taskService.addCandidateGroup(taskId, "aGroupId");
    taskService.addCandidateGroup(taskId, "bGroupId");
    taskService.deleteCandidateGroup(taskId, "aGroupId");
    taskService.deleteCandidateGroup(taskId, "bGroupId");
    // query test
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskInvolvedGroup("aGroupId").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskInvolvedGroup("bGroupId").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskInvolvedGroup("invalidGroupId").count());

    taskService.deleteTask("newTask",true);
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testTaskHadCandidateUser() {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();
    // if
    identityService.setAuthenticatedUserId("aAssignerId");
    taskService.addCandidateUser(taskId, "aUserId");
    taskService.addCandidateUser(taskId, "bUserId");
    taskService.deleteCandidateUser(taskId, "bUserId");
    Task taskAssignee = taskService.newTask("newTask");
    taskAssignee.setAssignee("aUserId");
    taskService.saveTask(taskAssignee);
    // query test
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskHadCandidateUser("aUserId").count());
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskHadCandidateUser("bUserId").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskHadCandidateUser("invalidUserId").count());
    // delete test
    taskService.deleteTask("newTask",true);
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testTaskHadCandidateGroup() {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();
    // if
    identityService.setAuthenticatedUserId("aAssignerId");
    taskService.addCandidateGroup(taskId, "bGroupId");
    taskService.deleteCandidateGroup(taskId, "bGroupId");
    // query test
    assertEquals(1, historyService.createHistoricTaskInstanceQuery().taskHadCandidateGroup("bGroupId").count());
    assertEquals(0, historyService.createHistoricTaskInstanceQuery().taskHadCandidateGroup("invalidGroupId").count());
    // delete test
    taskService.deleteTask("newTask",true);
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testWithCandidateGroups() {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    identityService.setAuthenticatedUserId("aAssignerId");
    taskService.addCandidateGroup(taskId, "aGroupId");

    // then
    assertEquals(historyService.createHistoricTaskInstanceQuery().withCandidateGroups().count(), 1);

    // cleanup
    taskService.deleteTask("newTask", true);
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testWithoutCandidateGroups() {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();
    identityService.setAuthenticatedUserId("aAssignerId");
    taskService.addCandidateGroup(taskId, "aGroupId");

    // when
    runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // then
    assertEquals(historyService.createHistoricTaskInstanceQuery().count(), 2);
    assertEquals(historyService.createHistoricTaskInstanceQuery().withoutCandidateGroups().count(), 1);

    // cleanup
    taskService.deleteTask("newTask", true);
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testGroupTaskQuery() {
    // given
    runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();
    // if
    identityService.setAuthenticatedUserId("aAssignerId");
    taskService.addCandidateUser(taskId, "aUserId");
    taskService.addCandidateGroup(taskId, "aGroupId");
    taskService.addCandidateGroup(taskId, "bGroupId");
    Task taskOne = taskService.newTask("taskOne");
    taskOne.setAssignee("aUserId");
    taskService.saveTask(taskOne);
    Task taskTwo = taskService.newTask("taskTwo");
    taskTwo.setAssignee("aUserId");
    taskService.saveTask(taskTwo);
    Task taskThree = taskService.newTask("taskThree");
    taskThree.setOwner("aUserId");
    taskService.saveTask(taskThree);
    taskService.deleteCandidateGroup(taskId, "aGroupId");
    taskService.deleteCandidateGroup(taskId, "bGroupId");
    historyService.createHistoricTaskInstanceQuery();

    // Query test
    HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery();
    assertEquals(4, query.taskInvolvedUser("aUserId").count());
    query = historyService.createHistoricTaskInstanceQuery();
    assertEquals(1, query.taskHadCandidateUser("aUserId").count());
    query = historyService.createHistoricTaskInstanceQuery();
    assertEquals(1, query.taskHadCandidateGroup("aGroupId").count());
    assertEquals(1, query.taskHadCandidateGroup("bGroupId").count());
    assertEquals(0, query.taskInvolvedUser("aUserId").count());
    query = historyService.createHistoricTaskInstanceQuery();
    assertEquals(4, query.taskInvolvedUser("aUserId").count());
    assertEquals(1, query.taskHadCandidateUser("aUserId").count());
    assertEquals(1, query.taskInvolvedUser("aUserId").count());
    // delete task
    taskService.deleteTask("taskOne",true);
    taskService.deleteTask("taskTwo",true);
    taskService.deleteTask("taskThree",true);
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testTaskWasAssigned() {
    // given
    Task taskOne = taskService.newTask("taskOne");
    Task taskTwo = taskService.newTask("taskTwo");
    Task taskThree = taskService.newTask("taskThree");

    // when
    taskOne.setAssignee("aUserId");
    taskService.saveTask(taskOne);

    taskTwo.setAssignee("anotherUserId");
    taskService.saveTask(taskTwo);

    taskService.saveTask(taskThree);

    List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().taskAssigned().list();

    // then
    assertEquals(list.size(), 2);

    // cleanup
    taskService.deleteTask("taskOne",true);
    taskService.deleteTask("taskTwo",true);
    taskService.deleteTask("taskThree",true);
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testTaskWasUnassigned() {
    // given
    Task taskOne = taskService.newTask("taskOne");
    Task taskTwo = taskService.newTask("taskTwo");
    Task taskThree = taskService.newTask("taskThree");

    // when
    taskOne.setAssignee("aUserId");
    taskService.saveTask(taskOne);

    taskTwo.setAssignee("anotherUserId");
    taskService.saveTask(taskTwo);

    taskService.saveTask(taskThree);

    List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().taskUnassigned().list();

    // then
    assertEquals(list.size(), 1);

    // cleanup
    taskService.deleteTask("taskOne",true);
    taskService.deleteTask("taskTwo",true);
    taskService.deleteTask("taskThree",true);
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testTaskReturnedBeforeEndTime() {
    // given
    Task taskOne = taskService.newTask("taskOne");

    // when
    taskOne.setAssignee("aUserId");
    taskService.saveTask(taskOne);

    Calendar hourAgo = Calendar.getInstance();
    hourAgo.add(Calendar.HOUR_OF_DAY, -1);
    ClockUtil.setCurrentTime(hourAgo.getTime());

    taskService.complete(taskOne.getId());

    List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
            .finishedBefore(hourAgo.getTime()).list();

    // then
    assertEquals(1, list.size());

    // cleanup
    taskService.deleteTask("taskOne",true);
    ClockUtil.reset();
  }

  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  @Deployment(resources = { "org/camunda/bpm/engine/test/api/runtime/oneTaskProcess.bpmn20.xml" })
  public void testTaskNotReturnedAfterEndTime() {
    // given
    Task taskOne = taskService.newTask("taskOne");

    // when
    taskOne.setAssignee("aUserId");
    taskService.saveTask(taskOne);

    Calendar hourAgo = Calendar.getInstance();
    hourAgo.add(Calendar.HOUR_OF_DAY, -1);
    ClockUtil.setCurrentTime(hourAgo.getTime());

    taskService.complete(taskOne.getId());

    List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery()
            .finishedAfter(Calendar.getInstance().getTime()).list();

    // then
    assertEquals(0, list.size());

    // cleanup
    taskService.deleteTask("taskOne",true);

    ClockUtil.reset();
  }

}
