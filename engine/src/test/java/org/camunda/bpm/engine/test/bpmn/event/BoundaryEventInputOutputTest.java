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
package org.camunda.bpm.engine.test.bpmn.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.bpmn.iomapping.VariableLogDelegate;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class BoundaryEventInputOutputTest extends PluggableProcessEngineTest {

  protected static final BpmnModelInstance EVENT_GATEWAY_PROCESS =
    Bpmn.createExecutableProcess("process")
      .startEvent()
      .manualTask("manualTask")
      .boundaryEvent()
        .conditionalEventDefinition()
        .condition("${moveOn}")
        .conditionalEventDefinitionDone()
      .serviceTask("inputParameterTask")
        .camundaInputParameter("variable1", "testValue")
        .camundaClass(VariableLogDelegate.class)
      .endEvent()
      .moveToActivity("manualTask")
      .endEvent()
      .done();

  protected boolean skipOutputMappingVal;

  @Before
  public void setUp() {
    skipOutputMappingVal = processEngineConfiguration.isSkipOutputMappingOnCanceledActivities();
    processEngineConfiguration.setSkipOutputMappingOnCanceledActivities(true);
    VariableLogDelegate.reset();
  }

  @After
  public void tearDown() {
    processEngineConfiguration.setSkipOutputMappingOnCanceledActivities(skipOutputMappingVal);
    VariableLogDelegate.reset();
  }

  @Test
  public void shouldProcessInputOutputParametersAfterEventGateway() {
    // given
    testRule.deploy(EVENT_GATEWAY_PROCESS);

    // when
    runtimeService.startProcessInstanceByKey("process", Variables.putValue("moveOn", true));

    // then
    List<HistoricVariableInstance> vars = historyService.createHistoricVariableInstanceQuery()
        .variableName("variable1")
        .list();
    assertThat(vars).hasSize(1);
    assertThat(vars.get(0).getValue()).isEqualTo("testValue");
  }
}