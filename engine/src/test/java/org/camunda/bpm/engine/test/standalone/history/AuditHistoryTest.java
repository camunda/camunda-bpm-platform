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
package org.camunda.bpm.engine.test.standalone.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class AuditHistoryTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(
      "org/camunda/bpm/engine/test/standalone/history/audithistory.camunda.cfg.xml");

  @Rule
  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);

  protected RuntimeService runtimeService;
  protected HistoryService historyService;

  @Before
  public void setUp() {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
  @Test
  public void testReceivesNoHistoricVariableUpdatesAsDetails() {
    // given
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    String value = "a Variable Value";
    runtimeService.setVariable(instance.getId(), "aStringVariable", value);
    runtimeService.setVariable(instance.getId(), "aBytesVariable", value.getBytes());

    String newValue = "a new Variable Value";
    runtimeService.setVariable(instance.getId(), "aStringVariable", newValue);
    runtimeService.setVariable(instance.getId(), "aBytesVariable", newValue.getBytes());

    // then the historic variable instances exist and they have the latest values
    assertEquals(2, historyService.createHistoricVariableInstanceQuery().count());

    HistoricVariableInstance historicStringVariable =
        historyService.createHistoricVariableInstanceQuery().variableName("aStringVariable").singleResult();
    assertNotNull(historicStringVariable);
    assertEquals(newValue, historicStringVariable.getValue());

    HistoricVariableInstance historicBytesVariable =
        historyService.createHistoricVariableInstanceQuery().variableName("aBytesVariable").singleResult();
    assertNotNull(historicBytesVariable);
    assertTrue(Arrays.equals(newValue.getBytes(), (byte[]) historicBytesVariable.getValue()));

    // and no historic details exist
    assertEquals(0, historyService.createHistoricDetailQuery().variableUpdates().count());

  }
}
