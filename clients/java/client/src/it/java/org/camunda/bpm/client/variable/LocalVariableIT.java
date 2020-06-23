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
import static org.camunda.bpm.client.util.ProcessModels.EXTERNAL_TASK_TOPIC_FOO;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.dto.ProcessDefinitionDto;
import org.camunda.bpm.client.dto.ProcessInstanceDto;
import org.camunda.bpm.client.rule.ClientRule;
import org.camunda.bpm.client.rule.EngineRule;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.util.RecordingExternalTaskHandler;
import org.camunda.bpm.engine.variable.value.StringValue;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class LocalVariableIT {

  private static final String GLOBAL_VARIABLE_NAME = "globalVariable";
  private static final StringValue GLOBAL_VARIABLE_VALUE = ClientValues.stringValue("globalVariableValue");
  private static final String LOCAL_VARIABLE_NAME = "localVariable";
  private static final StringValue LOCAL_VARIABLE_VALUE = ClientValues.stringValue("localVariableValue");

  private static final BpmnModelInstance EXTERNAL_TASK_PROCESS = Bpmn.createExecutableProcess()
      .startEvent("startEvent")
        .serviceTask()
          .camundaExternalTask(EXTERNAL_TASK_TOPIC_FOO)
          .camundaInputParameter(LOCAL_VARIABLE_NAME, LOCAL_VARIABLE_VALUE.getValue())
      .endEvent("endEvent")
      .done();

  protected ClientRule clientRule = new ClientRule();
  protected EngineRule engineRule = new EngineRule();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(clientRule);

  protected ExternalTaskClient client;

  protected ProcessDefinitionDto processDefinition;
  protected ProcessInstanceDto processInstance;

  protected RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler();

  @Before
  public void setup() throws Exception {
    client = clientRule.client();
    processDefinition = engineRule.deploy(EXTERNAL_TASK_PROCESS).get(0);
    handler.clear();
  }

  @Test
  public void shouldFetchAllVariables() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), GLOBAL_VARIABLE_NAME, GLOBAL_VARIABLE_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO).handler(handler).open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    String variableValue = task.getVariable(GLOBAL_VARIABLE_NAME);
    assertThat(variableValue).isEqualTo(GLOBAL_VARIABLE_VALUE.getValue());
    variableValue = task.getVariable(LOCAL_VARIABLE_NAME);
    assertThat(variableValue).isEqualTo(LOCAL_VARIABLE_VALUE.getValue());
  }

  @Test
  public void shouldOnlyFetchLocalVariable() {
    // given
    engineRule.startProcessInstance(processDefinition.getId(), GLOBAL_VARIABLE_NAME, GLOBAL_VARIABLE_VALUE);

    // when
    client.subscribe(EXTERNAL_TASK_TOPIC_FOO).localVariables(true).handler(handler).open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());

    ExternalTask task = handler.getHandledTasks().get(0);

    String variableValue = task.getVariable(GLOBAL_VARIABLE_NAME);
    assertThat(variableValue).isNull();
    variableValue = task.getVariable(LOCAL_VARIABLE_NAME);
    assertThat(variableValue).isEqualTo(LOCAL_VARIABLE_VALUE.getValue());
  }
}
