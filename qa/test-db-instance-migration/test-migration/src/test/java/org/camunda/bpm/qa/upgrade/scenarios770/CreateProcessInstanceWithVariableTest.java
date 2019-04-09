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
package org.camunda.bpm.qa.upgrade.scenarios770;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.qa.upgrade.Origin;
import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.junit.Rule;
import org.junit.Test;

@ScenarioUnderTest("CreateProcessInstanceWithVariableScenario")
@Origin("7.7.0")
public class CreateProcessInstanceWithVariableTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule("camunda.cfg.xml");

  @ScenarioUnderTest("initProcessInstance.1")
  @Test
  public void testCreateProcessInstanceWithVariable() {
    // then
    ProcessInstance processInstance = engineRule.getRuntimeService().createProcessInstanceQuery().processInstanceBusinessKey("process").singleResult();
    List<HistoricVariableInstance> variables = engineRule.getHistoryService().createHistoricVariableInstanceQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(1, variables.size());
    assertEquals("foo", variables.get(0).getName());
    assertEquals("bar", variables.get(0).getValue());
  }
}
