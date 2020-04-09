/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class ModificationUserOperationLogTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(rule);
  protected BatchModificationHelper helper = new BatchModificationHelper(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testRule);

  protected RuntimeService runtimeService;
  protected HistoryService historyService;
  protected IdentityService identityService;
  protected BpmnModelInstance instance;
  protected static final Date START_DATE = new Date(1457326800000L);

  @Before
  public void initServices() {
    runtimeService = rule.getRuntimeService();
    historyService = rule.getHistoryService();
    identityService = rule.getIdentityService();
  }

  @Before
  public void setClock() {
    ClockUtil.setCurrentTime(START_DATE);
  }

  @Before
  public void createBpmnModelInstance() {
    this.instance = Bpmn.createExecutableProcess("process1")
        .startEvent("start")
        .userTask("user1")
        .sequenceFlowId("seq")
        .userTask("user2")
        .endEvent("end")
        .done();
  }

  @After
  public void resetClock() {
    ClockUtil.reset();
  }

  @After
  public void removeInstanceIds() {
    helper.currentProcessInstances = new ArrayList<String>();
  }

  @After
  public void removeBatches() {
    helper.removeAllRunningAndHistoricBatches();
  }
  @Test
  public void testLogCreation() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    identityService.setAuthenticatedUserId("userId");

    // when
    helper.startBeforeAsync("process1", 10, "user2", processDefinition.getId());
    identityService.clearAuthentication();

    // then
    List<UserOperationLogEntry> opLogEntries = historyService
            .createUserOperationLogQuery()
            .operationType(UserOperationLogEntry.OPERATION_TYPE_MODIFY_PROCESS_INSTANCE)
            .list();
    Assert.assertEquals(2, opLogEntries.size());

    Map<String, UserOperationLogEntry> entries = asMap(opLogEntries);


    UserOperationLogEntry asyncEntry = entries.get("async");
    Assert.assertNotNull(asyncEntry);
    Assert.assertEquals("ProcessInstance", asyncEntry.getEntityType());
    Assert.assertEquals("ModifyProcessInstance", asyncEntry.getOperationType());
    Assert.assertEquals(processDefinition.getId(), asyncEntry.getProcessDefinitionId());
    Assert.assertEquals(processDefinition.getKey(), asyncEntry.getProcessDefinitionKey());
    Assert.assertNull(asyncEntry.getProcessInstanceId());
    Assert.assertNull(asyncEntry.getOrgValue());
    Assert.assertEquals("true", asyncEntry.getNewValue());
    Assert.assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, asyncEntry.getCategory());

    UserOperationLogEntry numInstancesEntry = entries.get("nrOfInstances");
    Assert.assertNotNull(numInstancesEntry);
    Assert.assertEquals("ProcessInstance", numInstancesEntry.getEntityType());
    Assert.assertEquals("ModifyProcessInstance", numInstancesEntry.getOperationType());
    Assert.assertEquals(processDefinition.getId(), numInstancesEntry.getProcessDefinitionId());
    Assert.assertEquals(processDefinition.getKey(), numInstancesEntry.getProcessDefinitionKey());
    Assert.assertNull(numInstancesEntry.getProcessInstanceId());
    Assert.assertNull(numInstancesEntry.getOrgValue());
    Assert.assertEquals("10", numInstancesEntry.getNewValue());
    Assert.assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, numInstancesEntry.getCategory());

    Assert.assertEquals(asyncEntry.getOperationId(), numInstancesEntry.getOperationId());
  }

  @Test
  public void testNoCreationOnSyncBatchJobExecution() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());

    Batch batch = runtimeService.createModification(processDefinition.getId())
      .startAfterActivity("user2")
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .executeAsync();

    helper.completeSeedJobs(batch);

    // when
    identityService.setAuthenticatedUserId("userId");
    helper.executeJobs(batch);
    identityService.clearAuthentication();

    // then
    Assert.assertEquals(0, historyService.createUserOperationLogQuery().entityType(EntityTypes.PROCESS_INSTANCE).count());
  }

  @Test
  public void testNoCreationOnJobExecutorBatchJobExecution() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());

    runtimeService.createModification(processDefinition.getId())
      .cancelAllForActivity("user1")
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .executeAsync();

    // when
    testRule.waitForJobExecutorToProcessAllJobs(5000L);

    // then
    Assert.assertEquals(0, historyService.createUserOperationLogQuery().count());
  }

  @Test
  public void testBatchSyncModificationLogCreationWithAnnotation() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());

    identityService.setAuthenticatedUserId("userId");

    String annotation = "cancelation due to error";

    // when
    runtimeService.createModification(processDefinition.getId())
        .cancelAllForActivity("user1")
        .processInstanceIds(Arrays.asList(processInstance.getId()))
        .setAnnotation(annotation)
        .execute();

    identityService.clearAuthentication();

    // then
    List<UserOperationLogEntry> opLogEntries = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_MODIFY_PROCESS_INSTANCE)
        .list();
    assertEquals(2, opLogEntries.size());

    Map<String, UserOperationLogEntry> entries = asMap(opLogEntries);

    UserOperationLogEntry asyncEntry = entries.get("async");
    Assert.assertNotNull(asyncEntry);
    assertEquals(annotation, asyncEntry.getAnnotation());

    UserOperationLogEntry numInstancesEntry = entries.get("nrOfInstances");
    Assert.assertNotNull(numInstancesEntry);
    assertEquals(annotation, numInstancesEntry.getAnnotation());
  }

  @Test
  public void testBatchAsyncModificationLogCreationWithAnnotation() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());

    identityService.setAuthenticatedUserId("userId");

    String annotation = "cancelation due to error";

    // when
    runtimeService.createModification(processDefinition.getId())
        .startAfterActivity("user1")
        .processInstanceIds(Arrays.asList(processInstance.getId()))
        .setAnnotation(annotation)
        .executeAsync();

    identityService.clearAuthentication();

    // then
    List<UserOperationLogEntry> opLogEntries = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_MODIFY_PROCESS_INSTANCE)
        .list();
    assertEquals(2, opLogEntries.size());
    Map<String, UserOperationLogEntry> entries = asMap(opLogEntries);

    UserOperationLogEntry asyncEntry = entries.get("async");
    Assert.assertNotNull(asyncEntry);
    assertEquals(annotation, asyncEntry.getAnnotation());

    UserOperationLogEntry numInstancesEntry = entries.get("nrOfInstances");
    Assert.assertNotNull(numInstancesEntry);
    assertEquals(annotation, numInstancesEntry.getAnnotation());
  }

  @Test
  public void testSyncModificationLogCreationWithAnnotation() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());

    identityService.setAuthenticatedUserId("userId");

    String annotation = "cancelation due to error";

    // when
    runtimeService.createProcessInstanceModification(processInstance.getId())
        .startBeforeActivity("user1")
        .cancelAllForActivity("user1")
        .setAnnotation(annotation)
        .execute();

    identityService.clearAuthentication();

    // then
    List<UserOperationLogEntry> logs = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_MODIFY_PROCESS_INSTANCE)
        .list();
    assertEquals(1, logs.size());

    assertEquals(annotation, logs.get(0).getAnnotation());
  }

  @Test
  public void testAsyncModificationLogCreationWithAnnotation() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());

    identityService.setAuthenticatedUserId("userId");

    String annotation = "cancelation due to error";

    // when
    runtimeService.createProcessInstanceModification(processInstance.getId())
        .cancelActivityInstance("user1")
        .setAnnotation(annotation)
        .executeAsync();

    identityService.clearAuthentication();

    // then
    List<UserOperationLogEntry> logs = historyService.createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_MODIFY_PROCESS_INSTANCE)
        .list();
    assertEquals(1, logs.size());

    assertEquals(annotation, logs.get(0).getAnnotation());
  }


  @Test
  public void testModificationLogShouldNotIncludeEntryForTaskDeletion() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());

    identityService.setAuthenticatedUserId("userId");

    // when
    runtimeService.createProcessInstanceModification(processInstance.getId())
        .cancelAllForActivity("user1")
        .execute();

    identityService.clearAuthentication();

    // then
    List<UserOperationLogEntry> logs = historyService.createUserOperationLogQuery().list();
    assertEquals(1, logs.size());

    UserOperationLogEntry userOperationLogEntry = logs.get(0);
    assertThat(userOperationLogEntry.getEntityType()).isEqualTo("ProcessInstance");
    assertThat(userOperationLogEntry.getOperationType()).isEqualTo("ModifyProcessInstance");
  }

  protected Map<String, UserOperationLogEntry> asMap(List<UserOperationLogEntry> logEntries) {
    Map<String, UserOperationLogEntry> map = new HashMap<String, UserOperationLogEntry>();

    for (UserOperationLogEntry entry : logEntries) {

      UserOperationLogEntry previousValue = map.put(entry.getProperty(), entry);
      if (previousValue != null) {
        Assert.fail("expected only entry for every property");
      }
    }

    return map;
  }
}
