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
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class DeleteProcessInstanceUserOperationLogTest {

  private static final String PROPERTY = "property";
  private static final String NEW_VALUE = "newValue";
  private static final String PROCESS_INSTANCE_ID = "processInstanceId";
  private static final String PROCESS_DEFINITION_ID = "processDefinitionId";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RuntimeService runtimeService;
  protected HistoryService historyService;
  protected IdentityService identityService;
  protected RepositoryService repositoryService;

  protected Long defaultLogEntriesPerSyncOperationLimit;

  protected BpmnModelInstance instance;
  protected Deployment deployment;

  @Before
  public void setup() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    repositoryService = engineRule.getRepositoryService();
    identityService = engineRule.getIdentityService();
    defaultLogEntriesPerSyncOperationLimit = processEngineConfiguration.getLogEntriesPerSyncOperationLimit();

    this.instance = Bpmn.createExecutableProcess("process1")
        .startEvent("start")
        .userTask("user1")
        .sequenceFlowId("seq")
        .userTask("user2")
        .endEvent("end")
        .done();
  }

  @After
  public void tearDown() {
    processEngineConfiguration.setLogEntriesPerSyncOperationLimit(defaultLogEntriesPerSyncOperationLimit);
  }

  @Test
  public void shouldProduceSingleOperationLog() {
    // given
    processEngineConfiguration.setLogEntriesPerSyncOperationLimit(1);
    testRule.deploy(instance);
    engineRule.getIdentityService().setAuthenticatedUserId("userId");
    ProcessInstance instance1 = runtimeService.startProcessInstanceByKey("process1");

    // when
    runtimeService.deleteProcessInstance(instance1.getId(), null);

    // then
    UserOperationLogEntry logEntry = historyService.createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_DELETE).singleResult();
    assertThat(logEntry.getProcessInstanceId()).isEqualTo(instance1.getId());
    assertThat(logEntry.getProcessDefinitionId()).isEqualTo(instance1.getProcessDefinitionId());
    assertThat(logEntry.getProperty()).isNull();
    assertThat(logEntry.getNewValue()).isNull();
  }

  @Test
  public void shouldProduceMultipleOperationLogs() {
    // given
    processEngineConfiguration.setLogEntriesPerSyncOperationLimit(3);
    testRule.deploy(instance);
    engineRule.getIdentityService().setAuthenticatedUserId("userId");
    ProcessInstance instance1 = runtimeService.startProcessInstanceByKey("process1");
    ProcessInstance instance2 = runtimeService.startProcessInstanceByKey("process1");
    ProcessInstance instance3 = runtimeService.startProcessInstanceByKey("process1");

    // when
    runtimeService.deleteProcessInstances(List.of(instance1.getId(), instance2.getId(), instance3.getId()), null, false, true);

    // then
    List<UserOperationLogEntry> logs = historyService.createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_DELETE).list();
    assertThat(logs).hasSize(3);
    assertThat(logs)
    .extracting(PROPERTY, NEW_VALUE, PROCESS_INSTANCE_ID, PROCESS_DEFINITION_ID)
    .containsExactlyInAnyOrder(
         tuple(null, null, instance1.getProcessInstanceId(), instance1.getProcessDefinitionId()),
         tuple(null, null, instance2.getProcessInstanceId(), instance2.getProcessDefinitionId()),
         tuple(null, null, instance3.getProcessInstanceId(), instance3.getProcessDefinitionId()));
  }

  @Test
  public void shouldProduceSummaryOperationLog() {
    // given
    processEngineConfiguration.setLogEntriesPerSyncOperationLimit(1);
    testRule.deploy(instance);
    engineRule.getIdentityService().setAuthenticatedUserId("userId");
    ProcessInstance instance1 = runtimeService.startProcessInstanceByKey("process1");
    ProcessInstance instance2 = runtimeService.startProcessInstanceByKey("process1");
    ProcessInstance instance3 = runtimeService.startProcessInstanceByKey("process1");

    // when
    runtimeService.deleteProcessInstances(List.of(instance1.getId(), instance2.getId(), instance3.getId()), null, false, true);

    // then
    UserOperationLogEntry logEntry = historyService.createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_DELETE).singleResult();
    assertThat(logEntry.getProcessInstanceId()).isNull();
    assertThat(logEntry.getProcessDefinitionId()).isNull();
    assertThat(logEntry.getProperty()).isEqualTo("nrOfInstances");
    assertThat(logEntry.getNewValue()).isEqualTo("3");
  }
}
