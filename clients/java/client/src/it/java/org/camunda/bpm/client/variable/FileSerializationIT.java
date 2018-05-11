/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.dto.ProcessDefinitionDto;
import org.camunda.bpm.client.dto.ProcessInstanceDto;
import org.camunda.bpm.client.dto.TaskDto;
import org.camunda.bpm.client.dto.VariableInstanceDto;
import org.camunda.bpm.client.rule.ClientRule;
import org.camunda.bpm.client.rule.EngineRule;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.client.util.RecordingExternalTaskHandler;
import org.camunda.bpm.client.util.RecordingInvocationHandler;
import org.camunda.bpm.client.util.RecordingInvocationHandler.RecordedInvocation;
import org.camunda.bpm.client.variable.impl.value.DeferredFileValue;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.engine.variable.value.builder.FileValueBuilder;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.commons.utils.IoUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.client.util.ProcessModels.EXTERNAL_TASK_TOPIC_BAR;
import static org.camunda.bpm.client.util.ProcessModels.EXTERNAL_TASK_TOPIC_FOO;
import static org.camunda.bpm.client.util.ProcessModels.PROCESS_KEY_2;
import static org.camunda.bpm.client.util.ProcessModels.TWO_EXTERNAL_TASK_PROCESS;
import static org.camunda.bpm.client.util.ProcessModels.USER_TASK_ID;
import static org.camunda.bpm.client.util.ProcessModels.createProcessWithExclusiveGateway;
import static org.camunda.bpm.engine.variable.type.ValueType.FILE;

public class FileSerializationIT {

  protected static final String VARIABLE_NAME_FILE = "fileVariable";
  protected static final String VARIABLE_VALUE_FILE_NAME = "aFileName.txt";
  protected static final byte[] VARIABLE_VALUE_FILE_VALUE = "ABC".getBytes();
  protected static final String VARIABLE_VALUE_FILE_ENCODING = "UTF-8";
  protected static final String VARIABLE_VALUE_FILE_MIME_TYPE = "text/plain";

  protected static final FileValueBuilder VARIABLE_BUILDER =
    Variables.fileValue(VARIABLE_VALUE_FILE_NAME);

  protected static final FileValue VARIABLE_VALUE_FILE = Variables
    .fileValue(VARIABLE_VALUE_FILE_NAME)
    .file(VARIABLE_VALUE_FILE_VALUE)
    .create();

  protected ClientRule clientRule = new ClientRule();
  protected EngineRule engineRule = new EngineRule();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(clientRule);

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
    assertThat((Object) task.getVariable(VARIABLE_NAME_FILE)).isNull();
    assertThat(task.getAllVariables().size()).isEqualTo(1);
  }

  @Test
  public void shouldGet_Loaded() {
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
    deferredFileValue.load();

    // then
    assertThat(deferredFileValue.isLoaded()).isTrue();
    assertThat(IoUtil.inputStreamAsString(task.getVariable(VARIABLE_NAME_FILE)))
      .isEqualTo(new String(VARIABLE_VALUE_FILE_VALUE));
  }

  @Test
  @Ignore("Unhandled Exception")
  public void shouldGet_Null() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_FILE, VARIABLE_BUILDER.create());

    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    DeferredFileValue deferredFileValue = task.getVariableTyped(VARIABLE_NAME_FILE);

    // assume
    assertThat(deferredFileValue.isLoaded()).isFalse();

    // when
    deferredFileValue.load();

    // then
    assertThat(deferredFileValue.isLoaded()).isTrue();
    assertThat((Object) task.getVariable(VARIABLE_NAME_FILE)).isNull();
  }

  @Test
  public void shouldGetTyped() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), VARIABLE_NAME_FILE, VARIABLE_VALUE_FILE);

    client.subscribe(EXTERNAL_TASK_TOPIC_FOO)
      .handler(handler)
      .open();

    // when
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    // then
    DeferredFileValue typedValue = task.getVariableTyped(VARIABLE_NAME_FILE);
    assertThat(typedValue.getValue()).isEqualTo(null);
    assertThat(typedValue.getFilename()).isEqualTo(VARIABLE_VALUE_FILE_NAME);
    assertThat(typedValue.getType()).isEqualTo(FILE);
    assertThat(typedValue.isLoaded()).isFalse();
    assertThat(typedValue.getEncoding()).isNull();
    assertThat(typedValue.getMimeType()).isNull();
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
    deferredFileValue.load();

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
    Map<String, Object> variables = Variables.createVariables();
    variables.put(VARIABLE_NAME_FILE, VARIABLE_VALUE_FILE);
    fooService.complete(fooTask, variables);

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    DeferredFileValue deferredFileValue = task.getVariableTyped(VARIABLE_NAME_FILE);

    // assume
    assertThat(deferredFileValue.isLoaded()).isFalse();

    deferredFileValue.load();

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
    Map<String, Object> variables = Variables.createVariables();
    variables.put(VARIABLE_NAME_FILE, VARIABLE_BUILDER
      .file(new ByteArrayInputStream(VARIABLE_VALUE_FILE_VALUE))
      .create());
    fooService.complete(fooTask, variables);

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    DeferredFileValue deferredFileValue = task.getVariableTyped(VARIABLE_NAME_FILE);

    // assume
    assertThat(deferredFileValue.isLoaded()).isFalse();

    deferredFileValue.load();

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
    Map<String, Object> variables = Variables.createVariables();
    variables.put(VARIABLE_NAME_FILE, VARIABLE_BUILDER
      .file(new File("src/it/resources/aFileName.txt")).create());
    fooService.complete(fooTask, variables);

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    DeferredFileValue deferredFileValue = task.getVariableTyped(VARIABLE_NAME_FILE);

    // assume
    assertThat(deferredFileValue.isLoaded()).isFalse();

    deferredFileValue.load();

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
  public void shouldSet_Transient() {
    // given
    BpmnModelInstance process = createProcessWithExclusiveGateway(PROCESS_KEY_2,
      "${" + VARIABLE_NAME_FILE + " != null}");
    ProcessDefinitionDto definition = engineRule.deploy(process).get(0);
    ProcessInstanceDto processInstance = engineRule.startProcessInstance(definition.getId());

    RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler((task, client) -> {
      Map<String, Object> variables = new HashMap<>();
      variables.put(VARIABLE_NAME_FILE, VARIABLE_BUILDER.setTransient(true).create());
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

    List<VariableInstanceDto> variables = engineRule
      .getVariablesByProcessInstanceIdAndVariableName(processInstance.getId(), "fileVariable");
    assertThat(variables).isEmpty();
  }

  @Test
  @Ignore("Unhandled Exception")
  public void shouldSet_Null() {
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
    Map<String, Object> variables = Variables.createVariables();
    variables.put(VARIABLE_NAME_FILE, VARIABLE_BUILDER.create());
    fooService.complete(fooTask, variables);

    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);
    DeferredFileValue deferredFileValue = task.getVariableTyped(VARIABLE_NAME_FILE);

    // assume
    assertThat(deferredFileValue.isLoaded()).isFalse();

    deferredFileValue.load();

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

}
