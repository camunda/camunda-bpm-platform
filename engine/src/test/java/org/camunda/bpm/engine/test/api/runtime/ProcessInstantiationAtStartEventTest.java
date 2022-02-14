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
package org.camunda.bpm.engine.test.api.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.Map;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.junit.Before;
import org.junit.Test;

public class ProcessInstantiationAtStartEventTest extends PluggableProcessEngineTest {

  protected static final String PROCESS_DEFINITION_KEY = "testProcess";

  @Before
  public void setUp() throws Exception {
   testRule.deploy(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .userTask()
        .endEvent()
        .done());
  }

  @Test
  public void testStartProcessInstanceById() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    runtimeService.createProcessInstanceById(processDefinition.getId()).execute();

    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1L);
  }

  @Test
  public void testStartProcessInstanceByKey() {

    runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).execute();

    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1L);
  }

  @Test
  public void testStartProcessInstanceAndSetBusinessKey() {

    runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).businessKey("businessKey").execute();

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertThat(processInstance).isNotNull();
    assertThat(processInstance.getBusinessKey()).isEqualTo("businessKey");
  }

  @Test
  public void testStartProcessInstanceAndSetCaseInstanceId() {

    runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).caseInstanceId("caseInstanceId").execute();

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertThat(processInstance).isNotNull();
    assertThat(processInstance.getCaseInstanceId()).isEqualTo("caseInstanceId");
  }

  @Test
  public void testStartProcessInstanceAndSetVariable() {

    ProcessInstance processInstance = runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).setVariable("var", "value").execute();

    Object variable = runtimeService.getVariable(processInstance.getId(), "var");
    assertThat(variable).isNotNull();
    assertThat(variable).isEqualTo("value");
  }

  @Test
  public void testStartProcessInstanceAndSetVariables() {
    Map<String, Object> variables = Variables.createVariables().putValue("var1", "v1").putValue("var2", "v2");

    ProcessInstance processInstance = runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).setVariables(variables).execute();

    assertThat(runtimeService.getVariables(processInstance.getId())).isEqualTo(variables);
  }

  @Test
  public void testStartProcessInstanceNoSkipping() {

    runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).execute(false, false);

    assertThat(runtimeService.createProcessInstanceQuery().count()).isEqualTo(1L);
  }

  @Test
  public void testFailToStartProcessInstanceSkipListeners() {
    try {
      runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).execute(true, false);

      fail("expected exception");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage()).contains("Cannot skip");
    }
  }

  @Test
  public void testFailToStartProcessInstanceSkipInputOutputMapping() {
    try {
      runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).execute(false, true);

      fail("expected exception");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage()).contains("Cannot skip");
    }
  }

}
