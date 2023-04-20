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
package org.camunda.bpm.engine.test.api.variables;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.history.RemoveAndUpdateValueDelegate;
import org.camunda.bpm.engine.test.history.ReplaceAndUpdateValueDelegate;
import org.camunda.bpm.engine.test.history.UpdateValueDelegate;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
public class ImplicitVariableUpdateTest extends PluggableProcessEngineTest {

  @Deployment(resources = "org/camunda/bpm/engine/test/api/variables/ImplicitVariableUpdateTest.sequence.bpmn20.xml")
  @Test
  public void testUpdate() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("serviceTaskProcess",
        Variables.createVariables()
          .putValue("listVar", new ArrayList<String>())
          .putValue("delegate", new UpdateValueDelegate()));

    List<String> list = (List<String>) runtimeService.getVariable(instance.getId(), "listVar");
    assertNotNull(list);
    assertEquals(1, list.size());
    assertEquals(UpdateValueDelegate.NEW_ELEMENT, list.get(0));
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/variables/ImplicitVariableUpdateTest.parallel.bpmn20.xml")
  @Test
  public void testUpdateParallelFlow() {
    // should also work when execution tree is expanded between the implicit update
    // and when the engine notices it

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("serviceTaskProcess",
        Variables.createVariables()
          .putValue("listVar", new ArrayList<String>())
          .putValue("delegate", new UpdateValueDelegate()));

    List<String> list = (List<String>) runtimeService.getVariable(instance.getId(), "listVar");
    assertNotNull(list);
    assertEquals(1, list.size());
    assertEquals(UpdateValueDelegate.NEW_ELEMENT, list.get(0));
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/variables/ImplicitVariableUpdateTest.sequence.bpmn20.xml")
  @Test
  public void testUpdatePreviousValue() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("serviceTaskProcess",
        Variables.createVariables()
          .putValue("listVar", new ArrayList<String>())
          .putValue("delegate", new ReplaceAndUpdateValueDelegate()));

    List<String> list = (List<String>) runtimeService.getVariable(instance.getId(), "listVar");
    assertNotNull(list);
    assertTrue(list.isEmpty());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/variables/ImplicitVariableUpdateTest.sequence.bpmn20.xml")
  @Test
  public void testRemoveAndUpdateValue() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("serviceTaskProcess",
        Variables.createVariables()
          .putValue("listVar", new ArrayList<String>())
          .putValue("delegate", new RemoveAndUpdateValueDelegate()));

    Object variableValue = runtimeService.getVariable(instance.getId(), "listVar");
    assertNull(variableValue);
  }

  /**
   * In addition to the previous test cases, this method ensures that
   * the variable is also implicitly updated when only the serialized
   * value changes (without explicit instructions to change the object)
   */
  @Deployment(resources = "org/camunda/bpm/engine/test/api/variables/ImplicitVariableUpdateTest.sequence.bpmn20.xml")
  @Test
  public void testSerialization() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("serviceTaskProcess",
        Variables.createVariables()
            .putValue("pojo", new Pojo(1))
            .putValue("delegate", new NoopDelegate()));

    // at this point, the value of Pojo.foo = 1 in database.

    Pojo.shouldUpdateFoo = true; // implicitly update the value of 'foo' during deserialization.

    // will read foo = 1 from database but increment it to 2 during deserialization.
    Pojo pojo1 = (Pojo) runtimeService.getVariable(instance.getId(), "pojo");

    // foo = 2
    assertThat(pojo1.getFoo()).isEqualTo(2);

    // at this point, the database has value of foo = 2 since the implicit update was detected and flushed.

    Pojo.shouldUpdateFoo = false; // turn off implicit update of 'foo'

    // read foo again from database
    Pojo pojo2 = (Pojo) runtimeService.getVariable(instance.getId(), "pojo");

    // foo = 2 was fetched from database
    assertThat(pojo2.getFoo()).isEqualTo(2);
  }
}
