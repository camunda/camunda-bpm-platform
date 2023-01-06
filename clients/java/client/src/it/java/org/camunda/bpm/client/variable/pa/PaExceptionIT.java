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
package org.camunda.bpm.client.variable.pa;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.dto.HistoricProcessInstanceDto;
import org.camunda.bpm.client.dto.ProcessInstanceDto;
import org.camunda.bpm.client.exception.BadRequestException;
import org.camunda.bpm.client.exception.EngineException;
import org.camunda.bpm.client.exception.NotFoundException;
import org.camunda.bpm.client.exception.UnknownHttpErrorException;
import org.camunda.bpm.client.rule.ClientRule;
import org.camunda.bpm.client.rule.EngineRule;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.camunda.bpm.client.task.impl.ExternalTaskImpl;
import org.camunda.bpm.client.util.RecordingExternalTaskHandler;
import org.camunda.bpm.client.util.RecordingInvocationHandler;
import org.camunda.bpm.client.util.RecordingInvocationHandler.RecordedInvocation;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.camunda.bpm.client.util.PropertyUtil.CAMUNDA_ENGINE_NAME;
import static org.camunda.bpm.client.util.PropertyUtil.CAMUNDA_ENGINE_REST;
import static org.camunda.bpm.client.util.PropertyUtil.DEFAULT_PROPERTIES_PATH;
import static org.camunda.bpm.client.util.PropertyUtil.loadProperties;

public class PaExceptionIT {

  protected static final String ENGINE_NAME = "/engine/another-engine";

  private static final String PROCESS_DEFINITION_KEY = "KYsKNUbyVawGRt6H";

  protected ClientRule clientRule = new ClientRule(() -> {
    Properties properties = loadProperties(DEFAULT_PROPERTIES_PATH);
    String baseUrl = properties.getProperty(CAMUNDA_ENGINE_REST) + ENGINE_NAME;
    return ExternalTaskClient.create()
      .workerId("aWorkerId")
      .baseUrl(baseUrl)
      .disableAutoFetching();
  });

  protected EngineRule engineRule = new EngineRule(() -> {
    Properties properties = loadProperties(DEFAULT_PROPERTIES_PATH);
    properties.put(CAMUNDA_ENGINE_NAME, ENGINE_NAME);
    return properties;
  });

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(clientRule);

  protected ExternalTaskClient client;

  protected List<ProcessInstanceDto> processInstances = new ArrayList<>();

  protected RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler();
  protected RecordingInvocationHandler invocationHandler = new RecordingInvocationHandler();

  @Before
  public void setup() throws Exception {
    client = clientRule.client();

    ProcessInstanceDto processInstanceDto = engineRule.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
    processInstances.add(processInstanceDto);

    handler.clear();
    invocationHandler.clear();
  }

  @After
  public void tearDown() {
    processInstances.forEach(processInstance -> {
      String processInstanceId = processInstance.getId();
      HistoricProcessInstanceDto historicProcessInstance = engineRule.getHistoricProcessInstanceById(processInstanceId);
      if (historicProcessInstance.getEndTime() == null) {
        engineRule.deleteProcessInstance(processInstance.getId());
      }
    });
    processInstances.clear();
  }

  @Test
  public void shouldThrowEngineException() {
    ProcessInstanceDto processInstance = engineRule.startProcessInstanceByKey("Process_14ltot0");
    processInstances.add(processInstance);

    // given
    client.subscribe("my-topic")
        .handler(invocationHandler)
        .open();

    // when
    clientRule.waitForFetchAndLockUntil(() -> !invocationHandler.getInvocations().isEmpty());

    RecordedInvocation invocation = invocationHandler.getInvocations().get(0);
    ExternalTaskService service = invocation.getExternalTaskService();
    ExternalTask task = invocation.getExternalTask();

    // then
    EngineException px = (EngineException) catchThrowable(() -> service.complete(task));
    assertThat(px).isInstanceOf(EngineException.class);
    assertThat(px.getCode()).isEqualTo(22_222);
    assertThat(px.getType()).isEqualTo("ProcessEngineException");
    assertThat(px.getMessage()).isEqualTo("TASK/CLIENT-01009 Exception while completing the external task: my_error_message");
    assertThat(px.getHttpStatusCode()).isEqualTo(500);
    assertThat(px.getCause().getMessage()).isEqualTo("my_error_message");
  }

  @Test
  public void shouldThrowNotFoundException() {
    ProcessInstanceDto processInstance = engineRule.startProcessInstanceByKey("Process_14ltot0");
    processInstances.add(processInstance);

    // given
    client.subscribe("my-topic")
        .handler(invocationHandler)
        .open();

    // when
    clientRule.waitForFetchAndLockUntil(() -> !invocationHandler.getInvocations().isEmpty());

    RecordedInvocation invocation = invocationHandler.getInvocations().get(0);
    ExternalTaskService service = invocation.getExternalTaskService();

    ExternalTaskImpl externalTask = new ExternalTaskImpl();
    externalTask.setId("not-existing-id");

    // then
    NotFoundException px = (NotFoundException) catchThrowable(() -> service.complete(externalTask));
    assertThat(px.getCode()).isEqualTo(0);
    assertThat(px.getType()).isEqualTo("RestException");
    assertThat(px.getMessage()).isEqualTo("TASK/CLIENT-01008 Exception while completing the external task: External task with id not-existing-id does not exist");
    assertThat(px.getHttpStatusCode()).isEqualTo(404);
    assertThat(px.getCause().getMessage()).isEqualTo("External task with id not-existing-id does not exist");
  }

  @Test
  public void shouldThrowUnknownHttpError() {
    ProcessInstanceDto processInstance = engineRule.startProcessInstanceByKey("Process_14ltot0");
    processInstances.add(processInstance);

    // given
    client.subscribe("my-topic")
        .handler(invocationHandler)
        .open();

    // when
    clientRule.waitForFetchAndLockUntil(() -> !invocationHandler.getInvocations().isEmpty());

    RecordedInvocation invocation = invocationHandler.getInvocations().get(0);
    ExternalTaskService service = invocation.getExternalTaskService();

    // then
    ExternalTaskImpl externalTask = new ExternalTaskImpl();
    externalTask.setId("");
    UnknownHttpErrorException px = (UnknownHttpErrorException) catchThrowable(() -> service.complete(externalTask));
    assertThat(px.getCode()).isNull();
    assertThat(px.getType()).isEqualTo("NotAllowedException");
    assertThat(px.getMessage()).isEqualTo("TASK/CLIENT-01031 Exception while completing the external task: "
        + "The request failed with status code 405 and message: "
        + "\"RESTEASY003650: No resource method found for POST, return 405 with Allow header\"");
    assertThat(px.getHttpStatusCode()).isEqualTo(405);
    assertThat(px.getCause().getMessage()).isEqualTo("RESTEASY003650: No resource method found for POST, return 405 with Allow header");
  }

  @Test
  public void shouldThrowBadRequestException() {
    ProcessInstanceDto processInstance1 = engineRule.startProcessInstanceByKey("Process_14ltot0");
    processInstances.add(processInstance1);

    // given
    client.subscribe("my-topic")
        .handler(invocationHandler)
        .open();

    // when
    clientRule.waitForFetchAndLockUntil(() -> !invocationHandler.getInvocations().isEmpty());

    RecordedInvocation invocation = invocationHandler.getInvocations().get(0);
    ExternalTaskService service = invocation.getExternalTaskService();

    ProcessInstanceDto processInstance2 = engineRule.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
    processInstances.add(processInstance2);

    ExternalTask externalTask = engineRule.getExternalTaskByProcessInstanceId(processInstance2.getId());

    // then
    BadRequestException px = (BadRequestException) catchThrowable(() -> service.complete(externalTask));
    assertThat(px).isInstanceOf(BadRequestException.class);
    assertThat(px.getCode()).isEqualTo(0);
    assertThat(px.getType()).isEqualTo("RestException");
    assertThat(px.getMessage()).matches("TASK/CLIENT-01007 Exception while completing the external task: "
        + "External Task ([a-z0-9-]*) cannot be completed by worker 'aWorkerId'. "
        + "It is locked by worker 'null'.");
    assertThat(px.getHttpStatusCode()).isEqualTo(400);
  }

}
