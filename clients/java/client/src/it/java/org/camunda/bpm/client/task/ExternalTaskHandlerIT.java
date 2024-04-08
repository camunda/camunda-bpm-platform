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
package org.camunda.bpm.client.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.client.rule.ClientRule.LOCK_DURATION;
import static org.camunda.bpm.client.util.ProcessModels.BPMN_ERROR_EXTERNAL_TASK_PROCESS;
import static org.camunda.bpm.client.util.ProcessModels.EXTERNAL_TASK_ID;
import static org.camunda.bpm.client.util.ProcessModels.EXTERNAL_TASK_PRIORITY;
import static org.camunda.bpm.client.util.ProcessModels.EXTERNAL_TASK_TOPIC_FOO;
import static org.camunda.bpm.client.util.ProcessModels.PROCESS_KEY;
import static org.camunda.bpm.client.util.ProcessModels.PROCESS_KEY_2;
import static org.camunda.bpm.client.util.ProcessModels.USER_TASK_AFTER_BPMN_ERROR;
import static org.camunda.bpm.client.util.ProcessModels.USER_TASK_ID;
import static org.camunda.bpm.client.util.ProcessModels.createProcessWithExclusiveGateway;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.dto.IncidentDto;
import org.camunda.bpm.client.dto.ProcessDefinitionDto;
import org.camunda.bpm.client.dto.ProcessInstanceDto;
import org.camunda.bpm.client.dto.TaskDto;
import org.camunda.bpm.client.dto.VariableInstanceDto;
import org.camunda.bpm.client.rule.ClientRule;
import org.camunda.bpm.client.rule.EngineRule;
import org.camunda.bpm.client.util.ProcessModels;
import org.camunda.bpm.client.util.RecordingExternalTaskHandler;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.BoundaryEvent;
import org.camunda.bpm.model.bpmn.instance.ErrorEventDefinition;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class ExternalTaskHandlerIT {

  private static final String BUSINESS_KEY = "aBusinessKey";
  protected ClientRule clientRule = new ClientRule();
  protected EngineRule engineRule = new EngineRule();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(clientRule);

  protected ExternalTaskClient client;

  protected ProcessDefinitionDto processDefinition;
  protected ProcessInstanceDto processInstance;

  @Before
  public void setup() throws Exception {
    client = clientRule.client();

    adjustProcessToAddErrorMessageVariable();

    processDefinition = engineRule.deploy(BPMN_ERROR_EXTERNAL_TASK_PROCESS).get(0);
    processInstance = engineRule.startProcessInstance(processDefinition.getId(), BUSINESS_KEY);
  }

  private void adjustProcessToAddErrorMessageVariable() {
    BoundaryEvent boundaryError = BPMN_ERROR_EXTERNAL_TASK_PROCESS.getModelElementById("catchBPMNError");

    Collection<ErrorEventDefinition> errorDefinitions = boundaryError.getChildElementsByType(ErrorEventDefinition.class);

    ErrorEventDefinition errorDefinition = errorDefinitions.iterator().next();
    errorDefinition.setCamundaErrorMessageVariable("errorMessage");
  }

  @Test
  public void shouldInvokeHandler() {
    // given
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler();

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    assertThat(handler.getHandledTasks()).hasSize(1);

    ExternalTask task = handler.getHandledTasks().get(0);

    assertThat(task.getProcessDefinitionId()).isEqualTo(processDefinition.getId());
    assertThat(task.getProcessDefinitionKey()).isEqualTo(PROCESS_KEY);

    assertThat(task.getBusinessKey()).isEqualTo(BUSINESS_KEY);
    assertThat(task.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(task.getActivityId()).isEqualTo(EXTERNAL_TASK_ID);
    assertThat(task.getActivityInstanceId()).isNotNull();

    assertThat(task.getTopicName()).isEqualTo(EXTERNAL_TASK_TOPIC_FOO);
    assertThat(task.getLockExpirationTime()).isNotNull();

    assertThat(task.getAllVariables()).isEmpty();
  }

  @Test
  public void shouldCompleteTask() {
    // given
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      client.complete(task);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    TaskDto task = engineRule.getTaskByProcessInstanceId(processInstance.getId());
    assertThat(task).isNotNull();
    assertThat(task.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(task.getTaskDefinitionKey()).isEqualTo(USER_TASK_ID);
  }

  @Test
  public void shouldSetVariablesByProcessInstanceId() {
    // given
    String variableName = "progress";
    Integer variableValue = 10;

    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      Map<String, Object> variables = new HashMap<>();
      variables.put(variableName, variableValue);

      client.setVariables(task.getProcessInstanceId(), variables);
      client.complete(task);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    VariableInstanceDto variable = engineRule.getVariableByProcessInstanceId(processInstance.getId());
    assertThat(variable).isNotNull();
    assertThat(variable.getProcessInstanceId()).isEqualTo(processInstance.getId());

    assertThat(variable.getName()).isEqualTo(variableName);
    assertThat(variable.getValue()).isEqualTo(variableValue);

  }

  @Test
  public void shouldSetVariablesByExternalTask() {
    // given
    String variableName = "progress";
    Integer variableValue = 10;

    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      Map<String, Object> variables = new HashMap<>();
      variables.put(variableName, variableValue);

      client.setVariables(task, variables);
      client.complete(task);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    VariableInstanceDto variable = engineRule.getVariableByProcessInstanceId(processInstance.getId());
    assertThat(variable).isNotNull();
    assertThat(variable.getProcessInstanceId()).isEqualTo(processInstance.getId());

    assertThat(variable.getName()).isEqualTo(variableName);
    assertThat(variable.getValue()).isEqualTo(variableValue);

  }

  @Test
  public void shouldCompleteWithVariables() {
    // given
    String variableName = "foo";
    String variableValue = "bar";

    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      Map<String, Object> variables = new HashMap<>();
      variables.put(variableName, variableValue);
      client.complete(task, variables);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    VariableInstanceDto variable = engineRule.getVariableByProcessInstanceId(processInstance.getId());
    assertThat(variable).isNotNull();
    assertThat(variable.getProcessInstanceId()).isEqualTo(processInstance.getId());

    assertThat(variable.getName()).isEqualTo(variableName);
    assertThat(variable.getValue()).isEqualTo(variableValue);
  }

  @Test
  public void shouldCompleteWithLocalVariables() {
    // given
    ProcessDefinitionDto definition = engineRule.deploy(ProcessModels.ONE_EXTERNAL_TASK_WITH_OUTPUT_PARAM_PROCESS).get(0);
    ProcessInstanceDto processInstance = engineRule.startProcessInstance(definition.getId());

    String variableName = "foo";
    String variableValue = "bar";

    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      Map<String, Object> variables = new HashMap<>();
      variables.put(variableName, variableValue);
      client.complete(task, null, variables);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    VariableInstanceDto variable = engineRule.getVariableByProcessInstanceId(processInstance.getId());
    assertThat(variable).isNotNull();
    assertThat(variable.getProcessInstanceId()).isEqualTo(processInstance.getId());

    assertThat(variable.getName()).isEqualTo("bar");
    assertThat(variable.getValue()).isEqualTo(variableValue);
  }

  @Test
  public void shouldCompleteWithVariablesAndLocalVariables() {
    // given
    ProcessDefinitionDto definition = engineRule.deploy(ProcessModels.ONE_EXTERNAL_TASK_WITH_OUTPUT_PARAM_PROCESS).get(0);
    ProcessInstanceDto processInstance = engineRule.startProcessInstance(definition.getId());

    String variableName = "x";
    String variableValue = "y";
    String localVariableName = "foo";
    String localVariableValue = "bar";

    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      Map<String, Object> variables = new HashMap<>();
      variables.put(variableName, variableValue);

      Map<String, Object> localVariables = new HashMap<>();
      localVariables.put(localVariableName, localVariableValue);

      client.complete(task, variables, localVariables);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    VariableInstanceDto variable = engineRule.getVariableByProcessInstanceId(processInstance.getId(), variableName);
    assertThat(variable).isNotNull();
    assertThat(variable.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(variable.getName()).isEqualTo(variableName);
    assertThat(variable.getValue()).isEqualTo(variableValue);

    VariableInstanceDto localVariable = engineRule.getVariableByProcessInstanceId(processInstance.getId(), "bar");
    assertThat(localVariable).isNotNull();
    assertThat(localVariable.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(localVariable.getName()).isEqualTo("bar");
    assertThat(localVariable.getValue()).isEqualTo(localVariableValue);
  }

  @Test
  public void shouldCompleteWithTransientVariables() {
    // given
    BpmnModelInstance process = createProcessWithExclusiveGateway(PROCESS_KEY_2, "${foo == 'bar'}");
    ProcessDefinitionDto definition = engineRule.deploy(process).get(0);
    ProcessInstanceDto processInstance = engineRule.startProcessInstance(definition.getId());

    String variableName = "foo";
    String variableValue = "bar";

    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      Map<String, Object> variables = new HashMap<>();
      variables.put(variableName, Variables.stringValue(variableValue, true));
      client.complete(task, variables);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    TaskDto task = engineRule.getTaskByProcessInstanceId(processInstance.getId());
    assertThat(task).isNotNull();
    assertThat(task.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(task.getTaskDefinitionKey()).isEqualTo(USER_TASK_ID);

    List<VariableInstanceDto> variables = engineRule.getVariablesByProcessInstanceIdAndVariableName(processInstance.getId(), "foo");
    assertThat(variables).isEmpty();
  }

  @Test
  public void shouldCompleteById() {
    // given
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      client.complete(task.getId(), null, null);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    TaskDto task = engineRule.getTaskByProcessInstanceId(processInstance.getId());
    assertThat(task).isNotNull();
    assertThat(task.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(task.getTaskDefinitionKey()).isEqualTo(USER_TASK_ID);
  }

  @Test
  public void shouldLock() {
    // given
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      // an external task may be locked again by the same worker
      client.lock(task, LOCK_DURATION * 10);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());
    ExternalTask externalTaskBeforeLock = handler.getHandledTasks().get(0);
    ExternalTask externalTaskAfterLock = engineRule.getExternalTaskByProcessInstanceId(processInstance.getId());
    assertThat(externalTaskAfterLock.getLockExpirationTime()).isAfter(externalTaskBeforeLock.getLockExpirationTime());
  }

  @Test
  public void shouldLockById() {
    // given
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      // an external task may be locked again by the same worker
      client.lock(task.getId(), LOCK_DURATION * 10);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());
    ExternalTask externalTaskBeforeLock = handler.getHandledTasks().get(0);
    ExternalTask externalTaskAfterLock = engineRule.getExternalTaskByProcessInstanceId(processInstance.getId());
    assertThat(externalTaskAfterLock.getLockExpirationTime()).isAfter(externalTaskBeforeLock.getLockExpirationTime());
  }

  @Test
  public void shouldExtendLock() {
    // given
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      client.extendLock(task, LOCK_DURATION * 10);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask externalTaskBeforeExtendLock = handler.getHandledTasks().get(0);
    ExternalTask externalTaskAfterExtendLock = engineRule.getExternalTaskByProcessInstanceId(processInstance.getId());

    assertThat(externalTaskAfterExtendLock.getLockExpirationTime()).isAfter(externalTaskBeforeExtendLock.getLockExpirationTime());
  }

  @Test
  public void shouldExtendLockById() {
    // given
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      client.extendLock(task.getId(), LOCK_DURATION * 10);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask externalTaskBeforeExtendLock = handler.getHandledTasks().get(0);
    ExternalTask externalTaskAfterExtendLock = engineRule.getExternalTaskByProcessInstanceId(processInstance.getId());

    assertThat(externalTaskAfterExtendLock.getLockExpirationTime()).isAfter(externalTaskBeforeExtendLock.getLockExpirationTime());
  }

  @Test
  public void shouldUnlockTask() {
    // given
    final AtomicBoolean unlocked = new AtomicBoolean(false);
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      if (!unlocked.get()) {
        client.unlock(task);
      }
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> handler.getHandledTasks().size() >= 2);

    ExternalTask firstInvocation = handler.getHandledTasks().get(0);
    ExternalTask secondInvocation = handler.getHandledTasks().get(1);
    assertThat(firstInvocation.getId()).isEqualTo(secondInvocation.getId());
  }

  @Test
  public void shouldInvokeHandleBpmnError() {
    // given
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      client.handleBpmnError(task, "500");
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    TaskDto task = engineRule.getTaskByProcessInstanceId(processInstance.getId());
    assertThat(task).isNotNull();
    assertThat(task.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(task.getTaskDefinitionKey()).isEqualTo(USER_TASK_AFTER_BPMN_ERROR);
  }

  @Test
  public void shouldInvokeHandleBpmnErrorWithVariables() {
    // given
    String variableName = "foo";
    String variableValue = "bar";
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      Map<String, Object> variables = new HashMap<>();
      variables.put(variableName, variableValue);
      client.handleBpmnError(task, "500", null, variables);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    String processInstanceId = processInstance.getId();
    TaskDto task = engineRule.getTaskByProcessInstanceId(processInstanceId);
    assertThat(task).isNotNull();

    VariableInstanceDto variable = engineRule.getVariableByProcessInstanceId(processInstance.getId(), "foo");
    assertThat(variable).isNotNull();
    assertThat(variable.getProcessInstanceId()).isEqualTo(processInstance.getId());

    assertThat(variable.getName()).isEqualTo(variableName);
    assertThat(variable.getValue()).isEqualTo(variableValue);
  }

  @Test
  public void shouldInvokeHandleBpmnErrorWithErrorMessage() {
    // given
    String anErrorMessage = "meaningful error message";
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      client.handleBpmnError(task, "500", anErrorMessage);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    String processInstanceId = processInstance.getId();
    TaskDto task = engineRule.getTaskByProcessInstanceId(processInstanceId);
    assertThat(task).isNotNull();
    VariableInstanceDto processInstanceVariable = engineRule.getVariableByProcessInstanceId(processInstanceId);
    assertThat(processInstanceVariable).isNotNull();
    assertThat(processInstanceVariable.getName()).isEqualTo("errorMessage");
    assertThat(processInstanceVariable.getValue()).isEqualTo(anErrorMessage);
  }


  @Test
  public void shouldInvokeHandleBpmnErrorById() {
    // given
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      client.handleBpmnError(task.getId(), "500", null, null);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    TaskDto task = engineRule.getTaskByProcessInstanceId(processInstance.getId());
    assertThat(task).isNotNull();
    assertThat(task.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(task.getTaskDefinitionKey()).isEqualTo(USER_TASK_AFTER_BPMN_ERROR);
  }

  @Test
  public void shouldInvokeHandleFailure() {
    // given
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      client.handleFailure(task, "my-message", "my-details", 0, 0);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    IncidentDto incident = engineRule.getIncidentByProcessInstanceId(processInstance.getId());
    assertThat(incident).isNotNull();
    assertThat(incident.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(incident.getActivityId()).isEqualTo(EXTERNAL_TASK_ID);
  }

  @Test
  public void shouldInvokeHandleFailureWithRetries() {
    // given
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      client.handleFailure(task, "my-message", "my-details", 1, 0);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .lockDuration(1000)
            .handler(handler)
            .open();


    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    handler.clear();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    assertThat(task.getErrorMessage()).isEqualTo("my-message");
    assertThat(task.getErrorDetails()).isEqualTo("my-details");
    assertThat(task.getRetries()).isEqualTo(1);
  }

  @Test
  public void shouldInvokeHandleFailureById() {
    // given
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      client.handleFailure(task.getId(), "my-message", "my-details", 0, 0);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    IncidentDto incident = engineRule.getIncidentByProcessInstanceId(processInstance.getId());
    assertThat(incident).isNotNull();
    assertThat(incident.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(incident.getActivityId()).isEqualTo(EXTERNAL_TASK_ID);
  }

  @Test
  public void shouldFailWithVariables() {
    // given
    ProcessDefinitionDto definition = engineRule.deploy(ProcessModels.BPMN_ERROR_EXTERNAL_TASK_WITH_OUTPUT_MAPPING_PROCESS).get(0);
    ProcessInstanceDto processInstance = engineRule.startProcessInstance(definition.getId());

    String variableName = "foo";
    String variableValue = "baz";

    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      Map<String, Object> variables = new HashMap<>();
      variables.put(variableName, variableValue);
      client.handleFailure(task.getId(), "my-message", "my-details", 0, 0, variables, null);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    // variable was mapped
    VariableInstanceDto variable = engineRule.getVariableByProcessInstanceId(processInstance.getId(), "bar");
    assertThat(variable).isNotNull();
    assertThat(variable.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(variable.getName()).isEqualTo("bar");
    assertThat(variable.getValue()).isEqualTo(variableValue);

    // error was caught and flow continued to user task
    TaskDto task = engineRule.getTaskByProcessInstanceId(processInstance.getId());
    assertThat(task.getTaskDefinitionKey()).isEqualTo(ProcessModels.USER_TASK_AFTER_BPMN_ERROR);
  }

  @Test
  public void shouldFailWithLocalVariables() {
    // given
    ProcessDefinitionDto definition = engineRule.deploy(ProcessModels.BPMN_ERROR_EXTERNAL_TASK_WITH_OUTPUT_MAPPING_PROCESS).get(0);
    ProcessInstanceDto processInstance = engineRule.startProcessInstance(definition.getId());

    String variableName = "foo";
    String variableValue = "baz";

    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      Map<String, Object> localVariables = new HashMap<>();
      localVariables.put(variableName, variableValue);
      client.handleFailure(task.getId(), "my-message", "my-details", 0, 0, null, localVariables);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    // variable was mapped
    VariableInstanceDto variable = engineRule.getVariableByProcessInstanceId(processInstance.getId(), "bar");
    assertThat(variable).isNotNull();
    assertThat(variable.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(variable.getName()).isEqualTo("bar");
    assertThat(variable.getValue()).isEqualTo(variableValue);

    // error was caught and flow continued to user task
    TaskDto task = engineRule.getTaskByProcessInstanceId(processInstance.getId());
    assertThat(task.getTaskDefinitionKey()).isEqualTo(ProcessModels.USER_TASK_AFTER_BPMN_ERROR);
  }

  @Test
  public void shouldFailWithVariablesAndLocalVariables() {
    // given
    ProcessDefinitionDto definition = engineRule.deploy(ProcessModels.BPMN_ERROR_EXTERNAL_TASK_WITH_OUTPUT_MAPPING_PROCESS).get(0);
    ProcessInstanceDto processInstance = engineRule.startProcessInstance(definition.getId());

    String variableName = "x";
    String variableValue = "y";
    String localVariableName = "foo";
    String localVariableValue = "bar";

    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      Map<String, Object> variables = new HashMap<>();
      variables.put(variableName, variableValue);

      Map<String, Object> localVariables = new HashMap<>();
      localVariables.put(localVariableName, localVariableValue);

      client.handleFailure(task.getId(), "my-message", "my-details", 0, 0, variables, localVariables);
    });

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    VariableInstanceDto variable = engineRule.getVariableByProcessInstanceId(processInstance.getId(), variableName);
    assertThat(variable).isNotNull();
    assertThat(variable.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(variable.getName()).isEqualTo(variableName);
    assertThat(variable.getValue()).isEqualTo(variableValue);

    // variable was mapped
    VariableInstanceDto localVariable = engineRule.getVariableByProcessInstanceId(processInstance.getId(), "bar");
    assertThat(localVariable).isNotNull();
    assertThat(localVariable.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(localVariable.getName()).isEqualTo("bar");
    assertThat(localVariable.getValue()).isEqualTo(localVariableValue);

    // error was caught and flow continued to user task
    TaskDto task = engineRule.getTaskByProcessInstanceId(processInstance.getId());
    assertThat(task.getTaskDefinitionKey()).isEqualTo(ProcessModels.USER_TASK_AFTER_BPMN_ERROR);
  }

  @Test
  public void shouldCheckExecutionId() {
    // given
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler();

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    List<ExternalTask> handledTasks = handler.getHandledTasks();
    clientRule.waitForFetchAndLockUntil(() -> !handledTasks.isEmpty());

    ExternalTask task = engineRule.getExternalTaskByProcessInstanceId(processInstance.getId());
    assertThat(task).isNotNull();
    assertThat(task.getExecutionId()).isEqualTo(handledTasks.get(0).getExecutionId());
  }

  @Test
  public void shouldCheckTenantId() {
    // given
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler();
    processDefinition = engineRule.deploy("aTenantId", BPMN_ERROR_EXTERNAL_TASK_PROCESS).get(0);
    processInstance = engineRule.startProcessInstanceByKey(processDefinition.getKey(), "aTenantId");

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = engineRule.getExternalTaskByProcessInstanceId(processInstance.getId());
    assertThat(task).isNotNull();
    assertThat(task.getTenantId()).isEqualTo("aTenantId");
  }

  @Test
  public void shouldCheckTaskPriority() {
    // given
    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler();
    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
            .handler(handler)
            .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = engineRule.getExternalTaskByProcessInstanceId(processInstance.getId());
    assertThat(task).isNotNull();
    assertThat(task.getPriority()).isEqualTo(EXTERNAL_TASK_PRIORITY);
  }

}
