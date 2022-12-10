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

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.history.UpdateValueDelegate;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class ImplicitVariableUpdateDisabledTest {

  @ClassRule
  public static final ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(
      c -> c.setImplicitVariableUpdateDetectionEnabled(false) // turn off implicit variable update detection.
  );

  private final ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  private final ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public final RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  private RuntimeService runtimeService;

  @Before
  public void createProcessEngine() {
    runtimeService = engineRule.getRuntimeService();
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/variables/ImplicitVariableUpdateTest.sequence.bpmn20.xml")
  @Test
  public void testUpdate() {
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("serviceTaskProcess",
        Variables.createVariables()
            .putValue("listVar", new ArrayList<String>())
            .putValue("delegate", new UpdateValueDelegate()));

    List<String> list = (List<String>) runtimeService.getVariable(instance.getId(), "listVar");

    assertNotNull(list);

    assertThat(list).isEmpty(); // implicit update of 'listVar' in the java delegate was not detected.
  }

  /**
   * In addition to the previous test cases, this method ensures that
   * the variable is also not implicitly updated when only the serialized
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

    // at this point, the database still has value of foo as 1 because implicit update detection is disabled.
    // i.e : ProcessEngineConfiguration.implicitVariableUpdateDetectionEnabled = false

    Pojo.shouldUpdateFoo = false; // turn off the implicit update of 'foo'

    // read foo again from database
    Pojo pojo2 = (Pojo) runtimeService.getVariable(instance.getId(), "pojo");

    // foo = 1 was fetched from database
    assertThat(pojo2.getFoo()).isEqualTo(1);
  }
}
