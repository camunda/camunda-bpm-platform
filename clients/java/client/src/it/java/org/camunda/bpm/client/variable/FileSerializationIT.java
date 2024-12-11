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
package org.camunda.bpm.client.variable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.client.util.ProcessModels.EXTERNAL_TASK_TOPIC_BAR;
import static org.camunda.bpm.client.util.ProcessModels.EXTERNAL_TASK_TOPIC_FOO;
import static org.camunda.bpm.client.util.ProcessModels.PROCESS_KEY_2;
import static org.camunda.bpm.client.util.ProcessModels.TWO_EXTERNAL_TASK_PROCESS;
import static org.camunda.bpm.client.util.ProcessModels.USER_TASK_ID;
import static org.camunda.bpm.client.util.ProcessModels.createProcessWithExclusiveGateway;
import static org.camunda.bpm.engine.variable.type.ValueType.FILE;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.dto.ProcessDefinitionDto;
import org.camunda.bpm.client.dto.ProcessInstanceDto;
import org.camunda.bpm.client.dto.TaskDto;
import org.camunda.bpm.client.dto.VariableInstanceDto;
import org.camunda.bpm.client.exception.ValueMapperException;
import org.camunda.bpm.client.rule.ClientRule;
import org.camunda.bpm.client.rule.EngineRule;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.client.util.RecordingExternalTaskHandler;
import org.camunda.bpm.client.util.RecordingInvocationHandler;
import org.camunda.bpm.client.util.RecordingInvocationHandler.RecordedInvocation;
import org.camunda.bpm.client.variable.impl.value.DeferredFileValueImpl;
import org.camunda.bpm.client.variable.value.DeferredFileValue;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.commons.utils.IoUtil;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

public class FileSerializationIT {

  protected static final String VARIABLE_NAME_FILE = "fileVariable";
  protected static final String VARIABLE_VALUE_FILE_NAME = "aFileName.txt";
  protected static final byte[] VARIABLE_VALUE_FILE_VALUE = "ABC".getBytes();
  protected static final String LOCAL_VARIABLE_NAME_FILE = "localFileName.txt";

  protected static final String VARIABLE_VALUE_FILE_ENCODING = "UTF-8";
  protected static final String VARIABLE_VALUE_FILE_MIME_TYPE = "text/plain";

  protected static final String ANOTHER_VARIABLE_NAME_FILE = "anotherFileVariable";
  protected static final byte[] ANOTHER_VARIABLE_VALUE_FILE = "DEF".getBytes();

  protected static final FileValue VARIABLE_VALUE_FILE = Variables
    .fileValue(VARIABLE_VALUE_FILE_NAME)
    .file(VARIABLE_VALUE_FILE_VALUE)
    .create();

  protected ClientRule clientRule = new ClientRule();
  protected EngineRule engineRule = new EngineRule();
  protected ExpectedException thrown = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(clientRule).around(thrown);

  protected ExternalTaskClient client;

  protected ProcessDefinitionDto processDefinition;

  protected RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler();
  protected RecordingInvocationHandler invocationHandler = new RecordingInvocationHandler();

  @Before
  public void setup() throws Exception {
    client = clientRule.client();
    processDefinition = engineRule.deploy(TWO_EXTERNAL_TASK_PROCESS).get(0);

    handler.clear();
    invocationHandler.clear();
  }

  @Test
  public void shouldGet() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_FILE, VARIABLE_VALUE_FILE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    // then
    assertThat(task.getAllVariables().size()).isEqualTo(1);
    assertThat(IoUtil.inputStreamAsString(task.getVariable(VARIABLE_NAME_FILE)))
      .isEqualTo(new String(VARIABLE_VALUE_FILE_VALUE));
  }

  @Test
  public void shouldGetLocalAndGlobalVariables() {
    // given
    ProcessDefinitionDto processDefinitionDto = engineRule.deploy(
        Bpmn.createExecutableProcess("process")
        .startEvent("startEvent")
        .serviceTask("serviceTaskFoo")
          .camundaExternalTask(EXTERNAL_TASK_TOPIC_FOO)
            // create the local file variable with the same content but different name
          .camundaInputParameter(LOCAL_VARIABLE_NAME_FILE, "${execution.getVariableTyped('fileVariable')}")
        .serviceTask("serviceTaskBar")
          .camundaExternalTask(EXTERNAL_TASK_TOPIC_BAR)
        .endEvent("endEvent")
        .done()
    ).get(0);

    engineRule.startProcessInstance(processDefinitionDto.getId(), VARIABLE_NAME_FILE, VARIABLE_VALUE_FILE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
        .handler(handler)
        .open();

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    // then
    assertThat(task.getAllVariables().size()).isEqualTo(2);
    assertThat(IoUtil.inputStreamAsString(task.getVariable(VARIABLE_NAME_FILE)))
        .isEqualTo(new String(VARIABLE_VALUE_FILE_VALUE));
    assertThat(IoUtil.inputStreamAsString(task.getVariable(LOCAL_VARIABLE_NAME_FILE)))
        .isEqualTo(new String(VARIABLE_VALUE_FILE_VALUE));
  }

  @Test
  public void shouldGetAll() {
    // given
    Map<String, TypedValue> variables = new HashMap<>();
    variables.put(VARIABLE_NAME_FILE, VARIABLE_VALUE_FILE);
    variables.put(ANOTHER_VARIABLE_NAME_FILE, Variables.fileValue(VARIABLE_VALUE_FILE_NAME).file(ANOTHER_VARIABLE_VALUE_FILE).create());
    engineRule.startProcessInstance(processDefinition.getId(), variables);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    // then
    assertThat(task.getAllVariables().size()).isEqualTo(2);
    assertThat(IoUtil.inputStreamAsString((InputStream) task.getAllVariables().get(VARIABLE_NAME_FILE)))
      .isEqualTo(new String(VARIABLE_VALUE_FILE_VALUE));
    assertThat(IoUtil.inputStreamAsString((InputStream) task.getAllVariables().get(ANOTHER_VARIABLE_NAME_FILE)))
      .isEqualTo(new String(ANOTHER_VARIABLE_VALUE_FILE));
  }

  @Test
  public void shouldGetTyped_Deferred() {
    // given
    ProcessInstanceDto processInstanceDto = engineRule.startProcessInstance(processDefinition.getId(),
        VARIABLE_NAME_FILE,
        VARIABLE_VALUE_FILE);

    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // when
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    // then
    DeferredFileValue typedValue = task.getVariableTyped(VARIABLE_NAME_FILE);
    assertThat(typedValue.getFilename()).isEqualTo(VARIABLE_VALUE_FILE_NAME);
    assertThat(typedValue.getType()).isEqualTo(FILE);
    assertThat(typedValue.isLoaded()).isFalse();
    assertThat(typedValue.getEncoding()).isNull();
    assertThat(typedValue.getMimeType()).isNull();

    DeferredFileValueImpl typedValueImpl = (DeferredFileValueImpl) typedValue;
    assertThat(typedValueImpl.getExecutionId()).isEqualTo(task.getExecutionId());
  }

  @Test
  public void shouldGetVariableTypedForLocalVariable() {
    // given
    ProcessDefinitionDto processDefinitionDto = engineRule.deploy(
        Bpmn.createExecutableProcess("process")
        .startEvent("startEvent")
        .serviceTask("serviceTaskFoo")
          .camundaExternalTask(EXTERNAL_TASK_TOPIC_FOO)
            // create the local file variable with the same content but different name
          .camundaInputParameter(LOCAL_VARIABLE_NAME_FILE, "${execution.getVariableTyped('fileVariable')}")
        .serviceTask("serviceTaskBar")
          .camundaExternalTask(EXTERNAL_TASK_TOPIC_BAR)
        .endEvent("endEvent")
        .done()
    ).get(0);

    engineRule.startProcessInstance(processDefinitionDto.getId(), VARIABLE_NAME_FILE, VARIABLE_VALUE_FILE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
        .handler(handler)
        .open();

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    // then
    DeferredFileValue typedValue = task.getVariableTyped(LOCAL_VARIABLE_NAME_FILE);
    assertThat(typedValue.getFilename()).isEqualTo(VARIABLE_VALUE_FILE_NAME);
    assertThat(typedValue.getType()).isEqualTo(FILE);
    assertThat(typedValue.isLoaded()).isFalse();
    assertThat(typedValue.getEncoding()).isNull();
    assertThat(typedValue.getMimeType()).isNull();

    InputStream value = typedValue.getValue();
    assertThat(IoUtil.inputStreamAsString(value)).isEqualTo(new String(VARIABLE_VALUE_FILE_VALUE));
  }

  @Test
  public void shouldGetTyped_Loaded() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_FILE, VARIABLE_VALUE_FILE);

    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    DeferredFileValue deferredFileValue = task.getVariableTyped(VARIABLE_NAME_FILE);

    // assume
    assertThat(deferredFileValue.isLoaded()).isFalse();

    // when
    deferredFileValue.getValue();

    // then
    DeferredFileValue typedValue = task.getVariableTyped(VARIABLE_NAME_FILE);

    assertThat(typedValue.isLoaded()).isTrue();
    assertThat(IoUtil.inputStreamAsString(typedValue.getValue()))
      .isEqualTo(new String(VARIABLE_VALUE_FILE_VALUE));
    assertThat(typedValue.getFilename()).isEqualTo(VARIABLE_VALUE_FILE_NAME);
    assertThat(typedValue.getType()).isEqualTo(FILE);
    assertThat(typedValue.getEncoding()).isNull();
    assertThat(typedValue.getMimeType()).isNull();
  }

  @Test
  public void shouldGetTyped_Encoding() {
    // given
    FileValue fileValue = Variables
      .fileValue(VARIABLE_VALUE_FILE_NAME)
      .file(VARIABLE_VALUE_FILE_VALUE)
      .encoding(VARIABLE_VALUE_FILE_ENCODING)
      .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_FILE, fileValue);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    DeferredFileValue deferredFileValue = task.getVariableTyped(VARIABLE_NAME_FILE);

    // assume
    assertThat(deferredFileValue.isLoaded()).isFalse();

    // then
    DeferredFileValue typedValue = task.getVariableTyped(VARIABLE_NAME_FILE);
    assertThat(typedValue.getEncoding()).isEqualTo(VARIABLE_VALUE_FILE_ENCODING);
  }

  @Test
  public void shouldGetTyped_MimeType() {
    // given
    FileValue fileValue = Variables
      .fileValue(VARIABLE_VALUE_FILE_NAME)
      .file(VARIABLE_VALUE_FILE_VALUE)
      .mimeType(VARIABLE_VALUE_FILE_MIME_TYPE)
      .create();

    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_FILE, fileValue);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    DeferredFileValue deferredFileValue = task.getVariableTyped(VARIABLE_NAME_FILE);

    // assume
    assertThat(deferredFileValue.isLoaded()).isFalse();

    // then
    DeferredFileValue typedValue = task.getVariableTyped(VARIABLE_NAME_FILE);
    assertThat(typedValue.getMimeType()).isEqualTo(VARIABLE_VALUE_FILE_MIME_TYPE);
  }

  @Test
  public void shouldSetTyped_Encoding() {
    // given
    engineRule.startProcessInstance(processDefinition.getId());

    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(invocationHandler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !invocationHandler.getInvocations().isEmpty());

    RecordedInvocation invocation = invocationHandler.getInvocations().get(0);
    ExternalTask fooTask = invocation.getExternalTask();
    ExternalTaskService fooService = invocation.getExternalTaskService();

    client.subscribe(EXTERNAL_TASK_TOPIC_BAR)
      .handler(handler)
      .open();

    // when
    FileValue fileValue = Variables
      .fileValue(VARIABLE_VALUE_FILE_NAME)
      .file(VARIABLE_VALUE_FILE_VALUE)
      .encoding(VARIABLE_VALUE_FILE_ENCODING)
      .create();

    fooService.complete(fooTask, Variables.createVariables().putValueTyped(VARIABLE_NAME_FILE, fileValue));

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    // then
    DeferredFileValue typedValue = task.getVariableTyped(VARIABLE_NAME_FILE);

    assertThat(typedValue.isLoaded()).isFalse();
    assertThat(typedValue.getFilename()).isEqualTo(VARIABLE_VALUE_FILE_NAME);
    assertThat(typedValue.getType()).isEqualTo(FILE);
    assertThat(typedValue.getEncoding()).isEqualTo(VARIABLE_VALUE_FILE_ENCODING);
    assertThat(typedValue.getMimeType()).isNull();
  }

  @Test
  public void shouldSetTyped_MimeType() {
    // given
    engineRule.startProcessInstance(processDefinition.getId());

    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(invocationHandler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !invocationHandler.getInvocations().isEmpty());

    RecordedInvocation invocation = invocationHandler.getInvocations().get(0);
    ExternalTask fooTask = invocation.getExternalTask();
    ExternalTaskService fooService = invocation.getExternalTaskService();

    client.subscribe(EXTERNAL_TASK_TOPIC_BAR)
      .handler(handler)
      .open();

    // when
    FileValue fileValue = Variables
      .fileValue(VARIABLE_VALUE_FILE_NAME)
      .file(VARIABLE_VALUE_FILE_VALUE)
      .mimeType(VARIABLE_VALUE_FILE_MIME_TYPE)
      .create();
    fooService.complete(fooTask, Variables.createVariables().putValueTyped(VARIABLE_NAME_FILE, fileValue));

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    // then
    DeferredFileValue typedValue = task.getVariableTyped(VARIABLE_NAME_FILE);

    assertThat(typedValue.isLoaded()).isFalse();
    assertThat(typedValue.getFilename()).isEqualTo(VARIABLE_VALUE_FILE_NAME);
    assertThat(typedValue.getType()).isEqualTo(FILE);
    assertThat(typedValue.getEncoding()).isNull();
    assertThat(typedValue.getMimeType()).isEqualTo(VARIABLE_VALUE_FILE_MIME_TYPE);
  }

  @Test
  public void shouldSet_Bytes() {
    // given
    engineRule.startProcessInstance(processDefinition.getId());

    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(invocationHandler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !invocationHandler.getInvocations().isEmpty());

    RecordedInvocation invocation = invocationHandler.getInvocations().get(0);
    ExternalTask fooTask = invocation.getExternalTask();
    ExternalTaskService fooService = invocation.getExternalTaskService();

    client.subscribe(EXTERNAL_TASK_TOPIC_BAR)
      .handler(handler)
      .open();

    // when
    fooService.complete(fooTask, Variables.createVariables().putValueTyped(VARIABLE_NAME_FILE, VARIABLE_VALUE_FILE));

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    DeferredFileValue deferredFileValue = task.getVariableTyped(VARIABLE_NAME_FILE);

    // assume
    assertThat(deferredFileValue.isLoaded()).isFalse();

    deferredFileValue.getValue();

    // then
    DeferredFileValue typedValue = task.getVariableTyped(VARIABLE_NAME_FILE);

    assertThat(typedValue.isLoaded()).isTrue();
    assertThat(IoUtil.inputStreamAsString(typedValue.getValue()))
      .isEqualTo(new String(VARIABLE_VALUE_FILE_VALUE));
    assertThat(typedValue.getFilename()).isEqualTo(VARIABLE_VALUE_FILE_NAME);
    assertThat(typedValue.getType()).isEqualTo(FILE);
    assertThat(typedValue.getEncoding()).isNull();
    assertThat(typedValue.getMimeType()).isNull();
  }

  @Test
  public void shouldSet_InputStream() {
    // given
    engineRule.startProcessInstance(processDefinition.getId());

    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(invocationHandler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !invocationHandler.getInvocations().isEmpty());

    RecordedInvocation invocation = invocationHandler.getInvocations().get(0);
    ExternalTask fooTask = invocation.getExternalTask();
    ExternalTaskService fooService = invocation.getExternalTaskService();

    client.subscribe(EXTERNAL_TASK_TOPIC_BAR)
      .handler(handler)
      .open();

    // when
    FileValue fileValue = Variables.fileValue(VARIABLE_VALUE_FILE_NAME).file(new ByteArrayInputStream(VARIABLE_VALUE_FILE_VALUE)).create();
    fooService.complete(fooTask, Variables.createVariables().putValueTyped(VARIABLE_NAME_FILE, fileValue));

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    DeferredFileValue deferredFileValue = task.getVariableTyped(VARIABLE_NAME_FILE);

    // assume
    assertThat(deferredFileValue.isLoaded()).isFalse();

    deferredFileValue.getValue();

    // then
    DeferredFileValue typedValue = task.getVariableTyped(VARIABLE_NAME_FILE);

    assertThat(typedValue.isLoaded()).isTrue();
    assertThat(IoUtil.inputStreamAsString(typedValue.getValue()))
      .isEqualTo(new String(VARIABLE_VALUE_FILE_VALUE));
    assertThat(typedValue.getFilename()).isEqualTo(VARIABLE_VALUE_FILE_NAME);
    assertThat(typedValue.getType()).isEqualTo(FILE);
    assertThat(typedValue.getEncoding()).isNull();
    assertThat(typedValue.getMimeType()).isNull();
  }

  @Test
  public void shouldSet_File() {
    // given
    engineRule.startProcessInstance(processDefinition.getId());

    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(invocationHandler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !invocationHandler.getInvocations().isEmpty());

    RecordedInvocation invocation = invocationHandler.getInvocations().get(0);
    ExternalTask fooTask = invocation.getExternalTask();
    ExternalTaskService fooService = invocation.getExternalTaskService();

    client.subscribe(EXTERNAL_TASK_TOPIC_BAR)
      .handler(handler)
      .open();

    // when
    FileValue fileValue = Variables.fileValue(VARIABLE_VALUE_FILE_NAME).file(new File("src/it/resources/aFileName.txt")).create();
    fooService.complete(fooTask, Variables.createVariables().putValueTyped(VARIABLE_NAME_FILE, fileValue));

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    DeferredFileValue deferredFileValue = task.getVariableTyped(VARIABLE_NAME_FILE);

    // assume
    assertThat(deferredFileValue.isLoaded()).isFalse();

    deferredFileValue.getValue();

    // then
    DeferredFileValue typedValue = task.getVariableTyped(VARIABLE_NAME_FILE);

    assertThat(typedValue.isLoaded()).isTrue();
    assertThat(IoUtil.inputStreamAsString(typedValue.getValue()))
      .isEqualTo(new String(VARIABLE_VALUE_FILE_VALUE));
    assertThat(typedValue.getFilename()).isEqualTo(VARIABLE_VALUE_FILE_NAME);
    assertThat(typedValue.getType()).isEqualTo(FILE);
    assertThat(typedValue.getEncoding()).isNull();
    assertThat(typedValue.getMimeType()).isNull();
  }

  @Test
  public void shouldFailWhenCompletingWihtDeferredFileValue() {
    // given
    ProcessInstanceDto processInstance = engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_FILE, VARIABLE_VALUE_FILE);

    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(invocationHandler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !invocationHandler.getInvocations().isEmpty());

    RecordedInvocation invocation = invocationHandler.getInvocations().get(0);
    ExternalTask fooTask = invocation.getExternalTask();
    ExternalTaskService fooService = invocation.getExternalTaskService();

    client.subscribe(EXTERNAL_TASK_TOPIC_BAR)
      .handler(handler)
      .open();

    // then
    thrown.expect(ValueMapperException.class);

    // when
    Map<String, Object> variables = new HashMap<>();
    DeferredFileValue deferredFileValue = fooTask.getVariableTyped(VARIABLE_NAME_FILE);
    variables.put("deferredFile", deferredFileValue);
    variables.put(ANOTHER_VARIABLE_NAME_FILE, ANOTHER_VARIABLE_VALUE_FILE);
    fooService.complete(fooTask, variables);

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    // then
    List<VariableInstanceDto> variableInstances = engineRule.getVariablesByProcessInstanceIdAndVariableName(processInstance.getId(), null);
    assertThat(variableInstances.size()).isEqualTo(2);

    List<String> variableNames = new ArrayList<>();
    for (VariableInstanceDto variableInstance : variableInstances) {
      variableNames.add(variableInstance.getName());
    }

    assertThat(variableNames).containsExactlyInAnyOrder(VARIABLE_NAME_FILE, ANOTHER_VARIABLE_NAME_FILE); // contains not "deferredFile"
  }

  @Test
  public void shouldSet_LoadedFileValue() {
    // given
    ProcessInstanceDto processInstance = engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_FILE, VARIABLE_VALUE_FILE);

    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(invocationHandler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !invocationHandler.getInvocations().isEmpty());

    RecordedInvocation invocation = invocationHandler.getInvocations().get(0);
    ExternalTask fooTask = invocation.getExternalTask();
    ExternalTaskService fooService = invocation.getExternalTaskService();

    client.subscribe(EXTERNAL_TASK_TOPIC_BAR)
      .handler(handler)
      .open();

    // when
    Map<String, Object> variables = new HashMap<>();
    DeferredFileValue deferredFileValue = fooTask.getVariableTyped(VARIABLE_NAME_FILE);
    deferredFileValue.getValue();
    variables.put("deferredFile", deferredFileValue);
    variables.put(ANOTHER_VARIABLE_NAME_FILE, ANOTHER_VARIABLE_VALUE_FILE);
    fooService.complete(fooTask, variables);

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    // then
    List<VariableInstanceDto> variableInstances = engineRule.getVariablesByProcessInstanceIdAndVariableName(processInstance.getId(), null);
    assertThat(variableInstances.size()).isEqualTo(3);

    List<String> variableNames = new ArrayList<>();
    for (VariableInstanceDto variableInstance : variableInstances) {
      variableNames.add(variableInstance.getName());
    }

    assertThat(variableNames).containsExactlyInAnyOrder(VARIABLE_NAME_FILE, ANOTHER_VARIABLE_NAME_FILE, "deferredFile");
  }

  @Test
  public void shouldSet_Transient() {
    // given
    BpmnModelInstance process = createProcessWithExclusiveGateway(PROCESS_KEY_2,
      "${" + VARIABLE_NAME_FILE + " != null}");
    ProcessDefinitionDto definition = engineRule.deploy(process).get(0);
    ProcessInstanceDto processInstance = engineRule.startProcessInstance(definition.getId());

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(invocationHandler)
      .open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !invocationHandler.getInvocations().isEmpty());

    RecordedInvocation invocation = invocationHandler.getInvocations().get(0);
    ExternalTask fooTask = invocation.getExternalTask();
    ExternalTaskService fooService = invocation.getExternalTaskService();

    FileValue transientFileValue = Variables.fileValue(VARIABLE_VALUE_FILE_NAME).file(new File("src/it/resources/aFileName.txt")).setTransient(true).create();
    fooService.complete(fooTask, Variables.createVariables().putValueTyped(VARIABLE_NAME_FILE, transientFileValue));

    TaskDto nextTask = engineRule.getTaskByProcessInstanceId(processInstance.getId());
    assertThat(nextTask).isNotNull();
    assertThat(nextTask.getProcessInstanceId()).isEqualTo(processInstance.getId());
    assertThat(nextTask.getTaskDefinitionKey()).isEqualTo(USER_TASK_ID);

    List<VariableInstanceDto> variables = engineRule.getVariablesByProcessInstanceIdAndVariableName(processInstance.getId(), VARIABLE_NAME_FILE);
    assertThat(variables).isEmpty();
  }

}
