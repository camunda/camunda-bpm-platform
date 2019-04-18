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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class SetExternalTaskRetriesUserOperationLogTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testRule);

  private static String PROCESS_DEFINITION_KEY = "oneExternalTaskProcess";
  private static String PROCESS_DEFINITION_KEY_2 = "twoExternalTaskWithPriorityProcess";

  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected ExternalTaskService externalTaskService;
  protected static final Date START_DATE = new Date(1457326800000L);

  protected List<String> processInstanceIds;

  @Before
  public void initServices() {
    runtimeService = rule.getRuntimeService();
    externalTaskService = rule.getExternalTaskService();
    managementService = rule.getManagementService();
  }

  @Before
  public void deployTestProcesses() throws Exception {
    org.camunda.bpm.engine.repository.Deployment deployment = rule.getRepositoryService().createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/api/externaltask/oneExternalTaskProcess.bpmn20.xml")
      .addClasspathResource("org/camunda/bpm/engine/test/api/externaltask/externalTaskPriorityExpression.bpmn20.xml")
      .deploy();

    rule.manageDeployment(deployment);

    RuntimeService runtimeService = rule.getRuntimeService();
    processInstanceIds = new ArrayList<String>();
    for (int i = 0; i < 4; i++) {
      processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, i + "").getId());
    }
    processInstanceIds.add(runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY_2).getId());
  }

  @Before
  public void setClock() {
    ClockUtil.setCurrentTime(START_DATE);
  }

  @After
  public void resetClock() {
    ClockUtil.reset();
  }

  @After
  public void removeAllRunningAndHistoricBatches() {
    HistoryService historyService = rule.getHistoryService();
    ManagementService managementService = rule.getManagementService();
    for (Batch batch : managementService.createBatchQuery().list()) {
      managementService.deleteBatch(batch.getId(), true);
    }
    // remove history of completed batches
    for (HistoricBatch historicBatch : historyService.createHistoricBatchQuery().list()) {
      historyService.deleteHistoricBatch(historicBatch.getId());
    }
  }

  @Test
  public void testLogCreationForOneExternalTaskId() {
    // given
    rule.getIdentityService().setAuthenticatedUserId("userId");

    // when
    ExternalTask externalTask = externalTaskService.createExternalTaskQuery().processInstanceId(processInstanceIds.get(0)).singleResult();
    externalTaskService.setRetries(externalTask.getId(), 5);
    rule.getIdentityService().clearAuthentication();
    // then
    List<UserOperationLogEntry> opLogEntries = rule.getHistoryService().createUserOperationLogQuery().list();
    Assert.assertEquals(1, opLogEntries.size());

    Map<String, UserOperationLogEntry> entries = asMap(opLogEntries);

    UserOperationLogEntry retriesEntry = entries.get("retries");
    Assert.assertNotNull(retriesEntry);
    Assert.assertEquals(EntityTypes.EXTERNAL_TASK, retriesEntry.getEntityType());
    Assert.assertEquals("SetExternalTaskRetries", retriesEntry.getOperationType());
    Assert.assertEquals(externalTask.getId(), retriesEntry.getExternalTaskId());
    Assert.assertEquals(externalTask.getProcessInstanceId(), retriesEntry.getProcessInstanceId());
    Assert.assertEquals(externalTask.getProcessDefinitionId(), retriesEntry.getProcessDefinitionId());
    Assert.assertEquals(externalTask.getProcessDefinitionKey(), retriesEntry.getProcessDefinitionKey());
    Assert.assertNull(retriesEntry.getOrgValue());
    Assert.assertEquals("5", retriesEntry.getNewValue());
    Assert.assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, retriesEntry.getCategory());
  }

  @Test
  public void testLogCreationSync() {
    // given
    rule.getIdentityService().setAuthenticatedUserId("userId");
    List<ExternalTask> list = externalTaskService.createExternalTaskQuery().list();
    List<String> externalTaskIds = new ArrayList<String>();

    for (ExternalTask task : list) {
      externalTaskIds.add(task.getId());
    }

    // when
    externalTaskService.setRetries(externalTaskIds, 5);
    rule.getIdentityService().clearAuthentication();
    // then
    List<UserOperationLogEntry> opLogEntries = rule.getHistoryService().createUserOperationLogQuery().list();
    Assert.assertEquals(3, opLogEntries.size());

    Map<String, UserOperationLogEntry> entries = asMap(opLogEntries);

    UserOperationLogEntry asyncEntry = entries.get("async");
    Assert.assertNotNull(asyncEntry);
    Assert.assertEquals(EntityTypes.EXTERNAL_TASK, asyncEntry.getEntityType());
    Assert.assertEquals("SetExternalTaskRetries", asyncEntry.getOperationType());
    Assert.assertNull(asyncEntry.getExternalTaskId());
    Assert.assertNull(asyncEntry.getProcessDefinitionId());
    Assert.assertNull(asyncEntry.getProcessDefinitionKey());
    Assert.assertNull(asyncEntry.getProcessInstanceId());
    Assert.assertNull(asyncEntry.getOrgValue());
    Assert.assertEquals("false", asyncEntry.getNewValue());
    Assert.assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, asyncEntry.getCategory());

    UserOperationLogEntry numInstancesEntry = entries.get("nrOfInstances");
    Assert.assertNotNull(numInstancesEntry);
    Assert.assertEquals(EntityTypes.EXTERNAL_TASK, numInstancesEntry.getEntityType());
    Assert.assertEquals("SetExternalTaskRetries", numInstancesEntry.getOperationType());
    Assert.assertNull(numInstancesEntry.getExternalTaskId());
    Assert.assertNull(numInstancesEntry.getProcessDefinitionId());
    Assert.assertNull(numInstancesEntry.getProcessDefinitionKey());
    Assert.assertNull(numInstancesEntry.getProcessInstanceId());
    Assert.assertNull(numInstancesEntry.getOrgValue());
    Assert.assertEquals("6", numInstancesEntry.getNewValue());
    Assert.assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, numInstancesEntry.getCategory());

    UserOperationLogEntry retriesEntry = entries.get("retries");
    Assert.assertNotNull(retriesEntry);
    Assert.assertEquals(EntityTypes.EXTERNAL_TASK, retriesEntry.getEntityType());
    Assert.assertEquals("SetExternalTaskRetries", retriesEntry.getOperationType());
    Assert.assertNull(retriesEntry.getExternalTaskId());
    Assert.assertNull(retriesEntry.getProcessDefinitionId());
    Assert.assertNull(retriesEntry.getProcessDefinitionKey());
    Assert.assertNull(retriesEntry.getProcessInstanceId());
    Assert.assertNull(retriesEntry.getOrgValue());
    Assert.assertEquals("5", retriesEntry.getNewValue());
    Assert.assertEquals(asyncEntry.getOperationId(), retriesEntry.getOperationId());
    Assert.assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, retriesEntry.getCategory());
  }

  @Test
  public void testLogCreationAsync() {
    // given
    rule.getIdentityService().setAuthenticatedUserId("userId");

    // when
    externalTaskService.setRetriesAsync(null, externalTaskService.createExternalTaskQuery(), 5);
    rule.getIdentityService().clearAuthentication();
    // then
    List<UserOperationLogEntry> opLogEntries = rule.getHistoryService().createUserOperationLogQuery().list();
    Assert.assertEquals(3, opLogEntries.size());

    Map<String, UserOperationLogEntry> entries = asMap(opLogEntries);

    UserOperationLogEntry asyncEntry = entries.get("async");
    Assert.assertNotNull(asyncEntry);
    Assert.assertEquals(EntityTypes.EXTERNAL_TASK, asyncEntry.getEntityType());
    Assert.assertEquals("SetExternalTaskRetries", asyncEntry.getOperationType());
    Assert.assertNull(asyncEntry.getExternalTaskId());
    Assert.assertNull(asyncEntry.getProcessDefinitionId());
    Assert.assertNull(asyncEntry.getProcessDefinitionKey());
    Assert.assertNull(asyncEntry.getProcessInstanceId());
    Assert.assertNull(asyncEntry.getOrgValue());
    Assert.assertEquals("true", asyncEntry.getNewValue());
    Assert.assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, asyncEntry.getCategory());

    UserOperationLogEntry numInstancesEntry = entries.get("nrOfInstances");
    Assert.assertNotNull(numInstancesEntry);
    Assert.assertEquals(EntityTypes.EXTERNAL_TASK, numInstancesEntry.getEntityType());
    Assert.assertEquals("SetExternalTaskRetries", numInstancesEntry.getOperationType());
    Assert.assertNull(numInstancesEntry.getExternalTaskId());
    Assert.assertNull(numInstancesEntry.getProcessDefinitionId());
    Assert.assertNull(numInstancesEntry.getProcessDefinitionKey());
    Assert.assertNull(numInstancesEntry.getProcessInstanceId());
    Assert.assertNull(numInstancesEntry.getOrgValue());
    Assert.assertEquals("6", numInstancesEntry.getNewValue());
    Assert.assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, numInstancesEntry.getCategory());

    UserOperationLogEntry retriesEntry = entries.get("retries");
    Assert.assertNotNull(retriesEntry);
    Assert.assertEquals(EntityTypes.EXTERNAL_TASK, retriesEntry.getEntityType());
    Assert.assertEquals("SetExternalTaskRetries", retriesEntry.getOperationType());
    Assert.assertNull(retriesEntry.getExternalTaskId());
    Assert.assertNull(retriesEntry.getProcessDefinitionId());
    Assert.assertNull(retriesEntry.getProcessDefinitionKey());
    Assert.assertNull(retriesEntry.getProcessInstanceId());
    Assert.assertNull(retriesEntry.getOrgValue());
    Assert.assertEquals("5", retriesEntry.getNewValue());
    Assert.assertEquals(asyncEntry.getOperationId(), retriesEntry.getOperationId());
    Assert.assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, retriesEntry.getCategory());
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
