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

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.EntityTypes;
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

/**
 *
 * @author Anna Pazola
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class RestartProcessInstanceUserOperationLogTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(rule);
  protected BatchRestartHelper helper = new BatchRestartHelper(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testRule);

  protected RuntimeService runtimeService;
  protected BpmnModelInstance instance;
  protected static final Date START_DATE = new Date(1457326800000L);

  @Before
  public void initServices() {
    runtimeService = rule.getRuntimeService();
  }

  @Before
  public void setClock() {
    ClockUtil.setCurrentTime(START_DATE);
  }

  @After
  public void resetEngineConfig() {
    rule.getProcessEngineConfiguration()
        .setRestrictUserOperationLogToAuthenticatedUsers(true);
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
  public void removeBatches() {
    helper.removeAllRunningAndHistoricBatches();
  }
  @Test
  public void testLogCreationAsync() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    rule.getIdentityService().setAuthenticatedUserId("userId");

    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("process1");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("process1");

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    runtimeService.restartProcessInstances(processDefinition.getId()).startAfterActivity("user1").processInstanceIds(processInstance1.getId(), processInstance2.getId()).executeAsync();
    rule.getIdentityService().clearAuthentication();

    // then
    List<UserOperationLogEntry> opLogEntries = rule.getHistoryService().createUserOperationLogQuery().operationType("RestartProcessInstance").list();
    Assert.assertEquals(2, opLogEntries.size());

    Map<String, UserOperationLogEntry> entries = asMap(opLogEntries);


    UserOperationLogEntry asyncEntry = entries.get("async");
    Assert.assertNotNull(asyncEntry);
    Assert.assertEquals("ProcessInstance", asyncEntry.getEntityType());
    Assert.assertEquals("RestartProcessInstance", asyncEntry.getOperationType());
    Assert.assertEquals(processDefinition.getId(), asyncEntry.getProcessDefinitionId());
    Assert.assertEquals(processDefinition.getKey(), asyncEntry.getProcessDefinitionKey());
    Assert.assertNull(asyncEntry.getProcessInstanceId());
    Assert.assertNull(asyncEntry.getOrgValue());
    Assert.assertEquals("true", asyncEntry.getNewValue());
    Assert.assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, asyncEntry.getCategory());

    UserOperationLogEntry numInstancesEntry = entries.get("nrOfInstances");
    Assert.assertNotNull(numInstancesEntry);
    Assert.assertEquals("ProcessInstance", numInstancesEntry.getEntityType());
    Assert.assertEquals("RestartProcessInstance", numInstancesEntry.getOperationType());
    Assert.assertEquals(processDefinition.getId(), numInstancesEntry.getProcessDefinitionId());
    Assert.assertEquals(processDefinition.getKey(), numInstancesEntry.getProcessDefinitionKey());
    Assert.assertNull(numInstancesEntry.getProcessInstanceId());
    Assert.assertNull(numInstancesEntry.getOrgValue());
    Assert.assertEquals("2", numInstancesEntry.getNewValue());
    Assert.assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, numInstancesEntry.getCategory());

    Assert.assertEquals(asyncEntry.getOperationId(), numInstancesEntry.getOperationId());
  }

  @Test
  public void testLogCreationSync() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    rule.getIdentityService().setAuthenticatedUserId("userId");

    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("process1");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("process1");

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    runtimeService.restartProcessInstances(processDefinition.getId()).startAfterActivity("user1").processInstanceIds(processInstance1.getId(), processInstance2.getId()).execute();
    rule.getIdentityService().clearAuthentication();

    // then
    List<UserOperationLogEntry> opLogEntries = rule.getHistoryService().createUserOperationLogQuery().operationType("RestartProcessInstance").list();
    Assert.assertEquals(2, opLogEntries.size());

    Map<String, UserOperationLogEntry> entries = asMap(opLogEntries);


    UserOperationLogEntry asyncEntry = entries.get("async");
    Assert.assertNotNull(asyncEntry);
    Assert.assertEquals("ProcessInstance", asyncEntry.getEntityType());
    Assert.assertEquals("RestartProcessInstance", asyncEntry.getOperationType());
    Assert.assertEquals(processDefinition.getId(), asyncEntry.getProcessDefinitionId());
    Assert.assertEquals(processDefinition.getKey(), asyncEntry.getProcessDefinitionKey());
    Assert.assertNull(asyncEntry.getProcessInstanceId());
    Assert.assertNull(asyncEntry.getOrgValue());
    Assert.assertEquals("false", asyncEntry.getNewValue());
    Assert.assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, asyncEntry.getCategory());

    UserOperationLogEntry numInstancesEntry = entries.get("nrOfInstances");
    Assert.assertNotNull(numInstancesEntry);
    Assert.assertEquals("ProcessInstance", numInstancesEntry.getEntityType());
    Assert.assertEquals("RestartProcessInstance", numInstancesEntry.getOperationType());
    Assert.assertEquals(processDefinition.getId(), numInstancesEntry.getProcessDefinitionId());
    Assert.assertEquals(processDefinition.getKey(), numInstancesEntry.getProcessDefinitionKey());
    Assert.assertNull(numInstancesEntry.getProcessInstanceId());
    Assert.assertNull(numInstancesEntry.getOrgValue());
    Assert.assertEquals("2", numInstancesEntry.getNewValue());
    Assert.assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, numInstancesEntry.getCategory());

    Assert.assertEquals(asyncEntry.getOperationId(), numInstancesEntry.getOperationId());
  }

  @Test
  public void testNoCreationOnSyncBatchJobExecution() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId()).startBeforeActivity("user1").processInstanceIds(processInstance.getId()).executeAsync();

    helper.completeSeedJobs(batch);

    // when
    rule.getIdentityService().setAuthenticatedUserId("userId");
    helper.executeJobs(batch);
    rule.getIdentityService().clearAuthentication();

    // then
    Assert.assertEquals(0, rule.getHistoryService().createUserOperationLogQuery().entityType(EntityTypes.PROCESS_INSTANCE).count());
  }

  @Test
  public void shouldNotLogOnSyncExecutionUnauthenticated() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId()).startBeforeActivity("user1").processInstanceIds(processInstance.getId()).executeAsync();

    helper.completeSeedJobs(batch);

    rule.getProcessEngineConfiguration().setRestrictUserOperationLogToAuthenticatedUsers(false);

    // when
    helper.executeJobs(batch);

    // then
    Assert.assertEquals(0, rule.getHistoryService().createUserOperationLogQuery().entityType(EntityTypes.PROCESS_INSTANCE).count());
  }

  @Test
  public void testNoCreationOnJobExecutorBatchJobExecution() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
    runtimeService.restartProcessInstances(processDefinition.getId())
      .startAfterActivity("user1")
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .executeAsync();

    // when
    testRule.waitForJobExecutorToProcessAllJobs(5000L);

    // then
    Assert.assertEquals(0, rule.getHistoryService().createUserOperationLogQuery().count());
  }

  @Test
  public void shouldNotLogOnExecutionUnauthenticated() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    runtimeService.deleteProcessInstance(processInstance.getId(), "test");
    runtimeService.restartProcessInstances(processDefinition.getId())
      .startAfterActivity("user1")
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .executeAsync();

    rule.getProcessEngineConfiguration().setRestrictUserOperationLogToAuthenticatedUsers(false);

    // when
    testRule.waitForJobExecutorToProcessAllJobs(5000L);

    // then
    Assert.assertEquals(0, rule.getHistoryService().createUserOperationLogQuery().count());
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
