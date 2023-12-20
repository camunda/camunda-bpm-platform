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
package org.camunda.bpm.engine.test.api.runtime.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import java.util.Map;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class MessageCorrelationUserOperationLogTest {

  private static final String USER_ID = "userId";
  private static final String SINGLE_INTERMEDIATE_MESSAGE_PROCESS = "intermediateMessage";
  private static final String INTERMEDIATE_MESSAGE_NAME = "intermediate";
  private static final String START_MESSAGE_NAME = "start";
  private static final String NUMBER_OF_INSTANCES = "nrOfInstances";
  private static final String NUMBER_OF_VARIABLES = "nrOfVariables";
  private static final String PROCESS_INSTANCE_ID = "processInstanceId";
  private static final String PROCESS_DEFINITION_ID = "processDefinitionId";
  private static final String MESSAGE_NAME = "messageName";
  private static final String PROPERTY = "property";
  private static final String NEW_VALUE = "newValue";

  private static final Long LIMIT_1 = 1L;
  private static final Long LIMIT_3 = 3L;
  private static final Long UNLIMITED = -1L;

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RuntimeService runtimeService;
  protected HistoryService historyService;
  protected IdentityService identityService;

  protected Long defaultLogEntriesPerSyncOperationLimit;

  @Before
  public void setup() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    identityService = engineRule.getIdentityService();
    defaultLogEntriesPerSyncOperationLimit = processEngineConfiguration.getLogEntriesPerSyncOperationLimit();
  }

  @After
  public void tearDown() {
    processEngineConfiguration.setLogEntriesPerSyncOperationLimit(defaultLogEntriesPerSyncOperationLimit);
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/message/MessageCorrelationUserOperationLogTest.intermediateMessageEvent.bpmn" })
  public void shouldCreateSingleUserOperationLogForMessageCorrelationWithReturnVariables() {
    // given
    // limit for message correlation operation log to 1
    // one process is waiting at one intermediate message catch event
    // authenticated user to enable user operation log
    processEngineConfiguration.setLogEntriesPerSyncOperationLimit(LIMIT_1);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(SINGLE_INTERMEDIATE_MESSAGE_PROCESS, Map.of("foo","bar"));
    identityService.setAuthenticatedUserId(USER_ID);

    // when
    runtimeService.createMessageCorrelation(INTERMEDIATE_MESSAGE_NAME).correlateAllWithResultAndVariables(false);

    // then
    String processInstanceId = processInstance.getId();
    String processDefinitionId = processInstance.getProcessDefinitionId();
    List<UserOperationLogEntry> logs = historyService.createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_CORRELATE_MESSAGE).list();
    assertThat(logs).hasSize(2); // only the two properties for this message correlation are found, no variables set
    assertThat(logs)
      .extracting(PROPERTY, NEW_VALUE, PROCESS_INSTANCE_ID, PROCESS_DEFINITION_ID)
      .containsExactlyInAnyOrder(
           tuple(MESSAGE_NAME, INTERMEDIATE_MESSAGE_NAME, processInstanceId, processDefinitionId),
           tuple(PROCESS_INSTANCE_ID, processInstanceId, processInstanceId, processDefinitionId));
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/message/MessageCorrelationUserOperationLogTest.intermediateMessageEvent.bpmn" })
  public void shouldCreateSingleUserOperationLogForMessageCorrelationWithVariables() {
    // given
    // limit for message correlation operation log to 1
    // one process is waiting at one intermediate message catch event
    // authenticated user to enable user operation log
    processEngineConfiguration.setLogEntriesPerSyncOperationLimit(LIMIT_1);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(SINGLE_INTERMEDIATE_MESSAGE_PROCESS);
    identityService.setAuthenticatedUserId(USER_ID);

    // when
    runtimeService.createMessageCorrelation(INTERMEDIATE_MESSAGE_NAME).setVariable("foo", "bar").correlateAll();

    // then
    String processInstanceId = processInstance.getId();
    String processDefinitionId = processInstance.getProcessDefinitionId();
    List<UserOperationLogEntry> logs = historyService.createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_CORRELATE_MESSAGE).list();
    assertThat(logs).hasSize(3); // only the three properties for this message correlation are found
    assertThat(logs)
      .extracting(PROPERTY, NEW_VALUE, PROCESS_INSTANCE_ID, PROCESS_DEFINITION_ID)
      .containsExactlyInAnyOrder(
          tuple(MESSAGE_NAME, INTERMEDIATE_MESSAGE_NAME, processInstanceId, processDefinitionId),
          tuple(PROCESS_INSTANCE_ID, processInstanceId, processInstanceId, processDefinitionId),
          tuple(NUMBER_OF_VARIABLES, "1", processInstanceId, processDefinitionId));
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/message/MessageCorrelationUserOperationLogTest.intermediateMessageEvent.bpmn" })
  public void shouldCreateSummarizingUserOperationLogForMessageCorrelations() {
    // given
    // limit for message correlation operation log to 1 -> only one summary op log
    // three processes are waiting at one intermediate message catch event
    // authenticated user to enable user operation log
    processEngineConfiguration.setLogEntriesPerSyncOperationLimit(LIMIT_1);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(SINGLE_INTERMEDIATE_MESSAGE_PROCESS);
    runtimeService.startProcessInstanceByKey(SINGLE_INTERMEDIATE_MESSAGE_PROCESS);
    runtimeService.startProcessInstanceByKey(SINGLE_INTERMEDIATE_MESSAGE_PROCESS);
    identityService.setAuthenticatedUserId(USER_ID);

    // when
    runtimeService.createMessageCorrelation(INTERMEDIATE_MESSAGE_NAME).correlateAll();

    // then
    String processDefinitionId = processInstance.getProcessDefinitionId();
    List<UserOperationLogEntry> logs = historyService.createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_CORRELATE_MESSAGE).list();
    assertThat(logs).hasSize(2); // only two properties found summarizing the operation
    assertThat(logs)
      .extracting(PROPERTY, NEW_VALUE, PROCESS_INSTANCE_ID, PROCESS_DEFINITION_ID)
      .containsExactlyInAnyOrder(
          tuple(MESSAGE_NAME, INTERMEDIATE_MESSAGE_NAME, null, processDefinitionId),
          tuple(NUMBER_OF_INSTANCES, "3", null, processDefinitionId));
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/message/MessageCorrelationUserOperationLogTest.intermediateMessageEvent.bpmn" })
  public void shouldCreateDetailedUserOperationLogForMessageCorrelationsWhenOnlyOneCorrelation() {
    // given
    // limit for message correlation operation log to 1
    // one process is waiting at one intermediate message catch event
    // authenticated user to enable user operation log
    processEngineConfiguration.setLogEntriesPerSyncOperationLimit(LIMIT_1);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(SINGLE_INTERMEDIATE_MESSAGE_PROCESS);
    identityService.setAuthenticatedUserId(USER_ID);

    // when
    List<MessageCorrelationResult> correlationResult = runtimeService.createMessageCorrelation(INTERMEDIATE_MESSAGE_NAME).correlateAllWithResult();

    // then
    String processInstanceId = processInstance.getId();
    String processDefinitionId = processInstance.getProcessDefinitionId();
    List<UserOperationLogEntry> logs = historyService.createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_CORRELATE_MESSAGE).list();
    assertThat(logs).hasSize(2); // only the two properties for this message correlation are found, no variables set
    assertThat(logs)
      .extracting(PROPERTY, NEW_VALUE, PROCESS_INSTANCE_ID, PROCESS_DEFINITION_ID)
      .containsExactlyInAnyOrder(
           tuple(MESSAGE_NAME, INTERMEDIATE_MESSAGE_NAME, processInstanceId, processDefinitionId),
           tuple(PROCESS_INSTANCE_ID, processInstanceId, processInstanceId, processDefinitionId));
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/message/MessageCorrelationUserOperationLogTest.intermediateMessageEvent.bpmn" })
  public void shouldCreateUserOperationLogForMessageCorrelations() {
    // given
    // limit for message correlation operation log to 3
    // three processes are waiting at one intermediate message catch event
    // authenticated user to enable user operation log
    processEngineConfiguration.setLogEntriesPerSyncOperationLimit(LIMIT_3);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey(SINGLE_INTERMEDIATE_MESSAGE_PROCESS);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey(SINGLE_INTERMEDIATE_MESSAGE_PROCESS);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey(SINGLE_INTERMEDIATE_MESSAGE_PROCESS);
    identityService.setAuthenticatedUserId(USER_ID);

    // when
    runtimeService.createMessageCorrelation(INTERMEDIATE_MESSAGE_NAME).correlateAll();

    // then
    List<UserOperationLogEntry> logs = historyService.createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_CORRELATE_MESSAGE).list();
    assertThat(logs).hasSize(6); // two properties per process instance found
    String processDefinitionId = processInstance1.getProcessDefinitionId();
    String processInstanceId1 = processInstance1.getId();
    List<UserOperationLogEntry> p1Logs = historyService.createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_CORRELATE_MESSAGE).processInstanceId(processInstanceId1).list();
    assertThat(p1Logs)
      .extracting(PROPERTY, NEW_VALUE, PROCESS_INSTANCE_ID, PROCESS_DEFINITION_ID)
      .containsExactlyInAnyOrder(
          tuple(MESSAGE_NAME, INTERMEDIATE_MESSAGE_NAME, processInstanceId1, processDefinitionId),
          tuple(PROCESS_INSTANCE_ID, processInstanceId1, processInstanceId1, processDefinitionId));
    String processInstanceId2 = processInstance2.getId();
    List<UserOperationLogEntry> p2Logs = historyService.createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_CORRELATE_MESSAGE).processInstanceId(processInstanceId2).list();
    assertThat(p2Logs)
      .extracting(PROPERTY, NEW_VALUE, PROCESS_INSTANCE_ID, PROCESS_DEFINITION_ID)
      .containsExactlyInAnyOrder(
          tuple(MESSAGE_NAME, INTERMEDIATE_MESSAGE_NAME, processInstanceId2, processDefinitionId),
          tuple(PROCESS_INSTANCE_ID, processInstanceId2, processInstanceId2, processDefinitionId));
    String processInstanceId3 = processInstance3.getId();
    List<UserOperationLogEntry> p3Logs = historyService.createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_CORRELATE_MESSAGE).processInstanceId(processInstanceId3).list();
    assertThat(p3Logs)
      .extracting(PROPERTY, NEW_VALUE, PROCESS_INSTANCE_ID, PROCESS_DEFINITION_ID)
      .containsExactlyInAnyOrder(
          tuple(MESSAGE_NAME, INTERMEDIATE_MESSAGE_NAME, processInstanceId3, processDefinitionId),
          tuple(PROCESS_INSTANCE_ID, processInstanceId3, processInstanceId3, processDefinitionId));
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/message/MessageCorrelationUserOperationLogTest.intermediateMessageEvent.bpmn" })
  public void shouldThrowExceptionForMessageCorrelationsExceedingLimit() {
    // given
    // limit for message correlation operation log to 3
    // four  processes are waiting at one intermediate message catch event
    // authenticated user to enable user operation log
    processEngineConfiguration.setLogEntriesPerSyncOperationLimit(LIMIT_3);
    runtimeService.startProcessInstanceByKey(SINGLE_INTERMEDIATE_MESSAGE_PROCESS).getId();
    runtimeService.startProcessInstanceByKey(SINGLE_INTERMEDIATE_MESSAGE_PROCESS).getId();
    runtimeService.startProcessInstanceByKey(SINGLE_INTERMEDIATE_MESSAGE_PROCESS).getId();
    runtimeService.startProcessInstanceByKey(SINGLE_INTERMEDIATE_MESSAGE_PROCESS).getId();
    identityService.setAuthenticatedUserId(USER_ID);

    // when
    assertThatThrownBy(() -> runtimeService.createMessageCorrelation(INTERMEDIATE_MESSAGE_NAME).correlateAll())

    // then
    .isInstanceOf(ProcessEngineException.class)
    .hasMessage("Maximum number of operation log entries for operation type synchronous APIs reached. Configured limit is 3 but 4 entities were affected by API call.");

    List<UserOperationLogEntry> logs = historyService.createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_CORRELATE_MESSAGE).list();
    assertThat(logs).isEmpty();
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/message/MessageCorrelationUserOperationLogTest.intermediateMessageEvent.bpmn" })
  public void shouldCreateUserOperationLogForMessageCorrelationsWhenUnlimited() {
    // given
    // operation log for message correlation is not limited (-1)
    // four processes are waiting at one intermediate message catch event
    // authenticated user to enable user operation log
    processEngineConfiguration.setLogEntriesPerSyncOperationLimit(UNLIMITED);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey(SINGLE_INTERMEDIATE_MESSAGE_PROCESS);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey(SINGLE_INTERMEDIATE_MESSAGE_PROCESS);
    ProcessInstance processInstance3 = runtimeService.startProcessInstanceByKey(SINGLE_INTERMEDIATE_MESSAGE_PROCESS);
    ProcessInstance processInstance4 = runtimeService.startProcessInstanceByKey(SINGLE_INTERMEDIATE_MESSAGE_PROCESS);
    identityService.setAuthenticatedUserId(USER_ID);

    // when
    runtimeService.createMessageCorrelation(INTERMEDIATE_MESSAGE_NAME).correlateAll();

    // then
    List<UserOperationLogEntry> logs = historyService.createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_CORRELATE_MESSAGE).list();
    assertThat(logs).hasSize(8); // two properties per process instance found
    String processDefinitionId = processInstance1.getProcessDefinitionId();
    String processInstanceId1 = processInstance1.getId();
    List<UserOperationLogEntry> p1Logs = historyService.createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_CORRELATE_MESSAGE).processInstanceId(processInstanceId1).list();
    assertThat(p1Logs)
      .extracting(PROPERTY, NEW_VALUE, PROCESS_INSTANCE_ID, PROCESS_DEFINITION_ID)
      .containsExactlyInAnyOrder(
          tuple(MESSAGE_NAME, INTERMEDIATE_MESSAGE_NAME, processInstanceId1, processDefinitionId),
          tuple(PROCESS_INSTANCE_ID, processInstanceId1, processInstanceId1, processDefinitionId));
    String processInstanceId2 = processInstance2.getId();
    List<UserOperationLogEntry> p2Logs = historyService.createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_CORRELATE_MESSAGE).processInstanceId(processInstanceId2).list();
    assertThat(p2Logs)
      .extracting(PROPERTY, NEW_VALUE, PROCESS_INSTANCE_ID, PROCESS_DEFINITION_ID)
      .containsExactlyInAnyOrder(
          tuple(MESSAGE_NAME, INTERMEDIATE_MESSAGE_NAME, processInstanceId2, processDefinitionId),
          tuple(PROCESS_INSTANCE_ID, processInstanceId2, processInstanceId2, processDefinitionId));
    String processInstanceId3 = processInstance3.getId();
    List<UserOperationLogEntry> p3Logs = historyService.createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_CORRELATE_MESSAGE).processInstanceId(processInstanceId3).list();
    assertThat(p3Logs)
      .extracting(PROPERTY, NEW_VALUE, PROCESS_INSTANCE_ID, PROCESS_DEFINITION_ID)
      .containsExactlyInAnyOrder(
          tuple(MESSAGE_NAME, INTERMEDIATE_MESSAGE_NAME, processInstanceId3, processDefinitionId),
          tuple(PROCESS_INSTANCE_ID, processInstanceId3, processInstanceId3, processDefinitionId));
    String processInstanceId4 = processInstance4.getId();
    List<UserOperationLogEntry> p4Logs = historyService.createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_CORRELATE_MESSAGE).processInstanceId(processInstanceId4).list();
    assertThat(p4Logs)
    .extracting(PROPERTY, NEW_VALUE, PROCESS_INSTANCE_ID, PROCESS_DEFINITION_ID)
    .containsExactlyInAnyOrder(
        tuple(MESSAGE_NAME, INTERMEDIATE_MESSAGE_NAME, processInstanceId4, processDefinitionId),
        tuple(PROCESS_INSTANCE_ID, processInstanceId4, processInstanceId4, processDefinitionId));
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/message/MessageCorrelationUserOperationLogTest.intermediateMessageEvent.bpmn" })
  public void shouldNotCreateUserOperationLogIfNoCorrelationResult() {
    // given
    // limit for message correlation operation log to 1
    // no processes are waiting at an intermediate message catch event
    // authenticated user to enable user operation log
    processEngineConfiguration.setLogEntriesPerSyncOperationLimit(LIMIT_1);
    identityService.setAuthenticatedUserId(USER_ID);

    // when
    runtimeService.createMessageCorrelation(INTERMEDIATE_MESSAGE_NAME).correlateAll();

    // then
    List<UserOperationLogEntry> logs = historyService.createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_CORRELATE_MESSAGE).list();
    assertThat(logs).isEmpty();
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/message/MessageCorrelationUserOperationLogTest.messageStartEvent.bpmn" })
  public void shouldCreateSingleUserOperationLogForMessageCorrelationStart() {
    // given
    // limit for message correlation operation log to 1
    // no processes are waiting at an intermediate message catch event
    // authenticated user to enable user operation log
    processEngineConfiguration.setLogEntriesPerSyncOperationLimit(LIMIT_1);
    identityService.setAuthenticatedUserId(USER_ID);

    // when
    List<MessageCorrelationResult> correlationResult = runtimeService.createMessageCorrelation(START_MESSAGE_NAME).correlateAllWithResult();

    // then correlation should always start only one instance
    assertThat(correlationResult).hasSize(1); // only one instance is started even with correlateAll
    List<UserOperationLogEntry> logs = historyService.createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_CORRELATE_MESSAGE).list();
    assertThat(logs).hasSize(2); // only the two properties for a single message correlation are found

    ProcessInstance processInstance = correlationResult.get(0).getProcessInstance();
    String processInstanceId = processInstance.getId();
    String processDefinitionId = processInstance.getProcessDefinitionId();
    assertThat(logs)
      .extracting(PROPERTY, NEW_VALUE, PROCESS_INSTANCE_ID, PROCESS_DEFINITION_ID)
      .containsExactlyInAnyOrder(
          tuple(MESSAGE_NAME, START_MESSAGE_NAME, processInstanceId, processDefinitionId),
          tuple(PROCESS_INSTANCE_ID, processInstanceId, processInstanceId, processDefinitionId));
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/message/MessageCorrelationUserOperationLogTest.messageStartEvent.bpmn" })
  public void shouldCreateSingleUserOperationLogForMessageCorrelationStartWithinLimit() {
    // given
    // limit for message correlation operation log to 3
    // no processes are waiting at an intermediate message catch event
    // authenticated user to enable user operation log
    processEngineConfiguration.setLogEntriesPerSyncOperationLimit(LIMIT_3);
    identityService.setAuthenticatedUserId(USER_ID);

    // when
    List<MessageCorrelationResult> correlationResult = runtimeService.createMessageCorrelation(START_MESSAGE_NAME).correlateAllWithResult();

    // then correlation should always start only one instance
    assertThat(correlationResult).hasSize(1); // only one instance is started even with correlateAll
    List<UserOperationLogEntry> logs = historyService.createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_CORRELATE_MESSAGE).list();
    assertThat(logs).hasSize(2); // only the two properties for a single message correlation are found

    ProcessInstance processInstance = correlationResult.get(0).getProcessInstance();
    String processInstanceId = processInstance.getId();
    String processDefinitionId = processInstance.getProcessDefinitionId();
    assertThat(logs)
      .extracting(PROPERTY, NEW_VALUE, PROCESS_INSTANCE_ID, PROCESS_DEFINITION_ID)
      .containsExactlyInAnyOrder(
          tuple(MESSAGE_NAME, START_MESSAGE_NAME, processInstanceId, processDefinitionId),
          tuple(PROCESS_INSTANCE_ID, processInstanceId, processInstanceId, processDefinitionId));
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/message/MessageCorrelationUserOperationLogTest.messageStartEvent.bpmn" })
  public void shouldCreateSingleUserOperationLogForMessageCorrelationStartUnlimited() {
    // given
    // limit for message correlation operation log to -1
    // no processes are waiting at an intermediate message catch event
    // authenticated user to enable user operation log
    processEngineConfiguration.setLogEntriesPerSyncOperationLimit(UNLIMITED);
    identityService.setAuthenticatedUserId(USER_ID);

    // when
    List<MessageCorrelationResult> correlationResult = runtimeService.createMessageCorrelation(START_MESSAGE_NAME).correlateAllWithResult();

    // then correlation should always start only one instance
    assertThat(correlationResult).hasSize(1); // only one instance is started even with correlateAll
    List<UserOperationLogEntry> logs = historyService.createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_CORRELATE_MESSAGE).list();
    assertThat(logs).hasSize(2); // only the two properties for a single message correlation are found

    ProcessInstance processInstance = correlationResult.get(0).getProcessInstance();
    String processInstanceId = processInstance.getId();
    String processDefinitionId = processInstance.getProcessDefinitionId();
    assertThat(logs)
      .extracting(PROPERTY, NEW_VALUE, PROCESS_INSTANCE_ID, PROCESS_DEFINITION_ID)
      .containsExactlyInAnyOrder(
          tuple(MESSAGE_NAME, START_MESSAGE_NAME, processInstanceId, processDefinitionId),
          tuple(PROCESS_INSTANCE_ID, processInstanceId, processInstanceId, processDefinitionId));
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/api/runtime/message/MessageCorrelationUserOperationLogTest.messageStartEvent.bpmn" })
  public void shouldCreateSingleUserOperationLogForMessageCorrelationStartWithVariables() {
    // given
    // limit for message correlation operation log to 1
    // no processes are waiting at an intermediate message catch event
    // authenticated user to enable user operation log
    processEngineConfiguration.setLogEntriesPerSyncOperationLimit(LIMIT_1);
    identityService.setAuthenticatedUserId(USER_ID);

    // when
    List<MessageCorrelationResult> correlationResult = runtimeService.createMessageCorrelation(START_MESSAGE_NAME)
        .setVariable("foo","bar")
        .setVariableLocal("foobar","baz")
        .setVariablesToTriggeredScope(Map.of("qux", "bar", "thud", "foo"))
        .correlateAllWithResult();

    // then correlation should always start only one instance
    assertThat(correlationResult).hasSize(1); // only one instance is started even with correlateAll
    List<UserOperationLogEntry> logs = historyService.createUserOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_CORRELATE_MESSAGE).list();
    assertThat(logs).hasSize(3); // only the three properties for a single message correlation are found

    ProcessInstance processInstance = correlationResult.get(0).getProcessInstance();
    String processInstanceId = processInstance.getId();
    String processDefinitionId = processInstance.getProcessDefinitionId();
    assertThat(logs)
      .extracting(PROPERTY, NEW_VALUE, PROCESS_INSTANCE_ID, PROCESS_DEFINITION_ID)
      .containsExactlyInAnyOrder(
          tuple(MESSAGE_NAME, START_MESSAGE_NAME, processInstanceId, processDefinitionId),
          tuple(PROCESS_INSTANCE_ID, processInstanceId, processInstanceId, processDefinitionId),
          tuple(NUMBER_OF_VARIABLES, "4", processInstanceId, processDefinitionId)); // all variables are counted
  }
}
