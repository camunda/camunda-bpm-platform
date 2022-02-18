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
package org.camunda.bpm.qa.upgrade.scenarios780;

import java.util.HashMap;
import java.util.List;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.qa.upgrade.Origin;

import org.camunda.bpm.qa.upgrade.ScenarioUnderTest;
import org.camunda.bpm.qa.upgrade.json.beans.ObjectList;
import org.camunda.bpm.qa.upgrade.json.beans.Order;

import org.camunda.bpm.qa.upgrade.json.beans.RegularCustomer;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ScenarioUnderTest("CreateProcessInstanceWithJsonVariablesScenario")
@Origin("7.8.0")
public class CreateProcessInstanceWithJsonVariablesTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule("camunda.json.cfg.xml");

  @ScenarioUnderTest("initProcessInstance.1")
  @Test
  public void testCreateProcessInstanceWithVariable() {
    // then
    ProcessInstance processInstance = engineRule.getRuntimeService().createProcessInstanceQuery().processInstanceBusinessKey("processWithJsonVariables").singleResult();
    List<VariableInstance> variables = engineRule.getRuntimeService().createVariableInstanceQuery().processInstanceIdIn(processInstance.getId()).list();
    assertEquals(4, variables.size());

    final Object objectVariable = engineRule.getRuntimeService().getVariable(processInstance.getId(), "objectVariable");
    assertObjectVariable(objectVariable);

    final Object plainTypeArrayVariable = engineRule.getRuntimeService().getVariable(processInstance.getId(), "plainTypeArrayVariable");
    assertPlainTypeArrayVariable(plainTypeArrayVariable);

    final Object notGenericObjectListVariable = engineRule.getRuntimeService().getVariable(processInstance.getId(), "notGenericObjectListVariable");
    assertNotGenericObjectListVariable(notGenericObjectListVariable);

    final TypedValue serializedObject = engineRule.getRuntimeService().getVariableTyped(processInstance.getId(), "serializedMapVariable", true);
    assertSerializedMap(serializedObject);
  }

  private void assertNotGenericObjectListVariable(Object notGenericObjectListVariable) {
    assertTrue(notGenericObjectListVariable instanceof ObjectList);
    final ObjectList list = (ObjectList) notGenericObjectListVariable;
    assertEquals(2, list.size());
    assertTrue(list.get(0) instanceof RegularCustomer);
    assertEquals("someCustomer", ((RegularCustomer) list.get(0)).getName());
    assertEquals(5, ((RegularCustomer) list.get(0)).getContractStartDate());
    assertTrue(list.get(1) instanceof RegularCustomer);
    assertEquals("secondCustomer", ((RegularCustomer) list.get(1)).getName());
    assertEquals(666, ((RegularCustomer) list.get(1)).getContractStartDate());
  }

  public void assertObjectVariable(Object objectVariable) {
    assertTrue(objectVariable instanceof Order);
    Order order = (Order)objectVariable;
    //check couple of fields
    assertEquals(1234567890987654321L, order.getId());
    assertEquals("order1", order.getOrder());
    assertTrue(order.isActive());
  }

  public void assertPlainTypeArrayVariable(Object plainTypeArrayVariable) {
    assertTrue(plainTypeArrayVariable instanceof int[]);
    int[] array = (int[])plainTypeArrayVariable;
    assertEquals(2, array.length);
    assertEquals(5, array[0]);
    assertEquals(10, array[1]);
  }

  public void assertSerializedMap(TypedValue typedValue) {
    HashMap<String, String> expected = new HashMap<>();
    expected.put("foo", "bar");
    Assert.assertEquals(expected, typedValue.getValue());
  }

}