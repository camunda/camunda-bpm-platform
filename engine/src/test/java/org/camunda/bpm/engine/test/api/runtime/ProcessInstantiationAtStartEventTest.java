/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.test.api.runtime;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;

public class ProcessInstantiationAtStartEventTest extends PluggableProcessEngineTestCase {

  protected static final String PROCESS_DEFINITION_KEY = "testProcess";

  @Override
  protected void setUp() throws Exception {
    deployment(Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .userTask()
        .endEvent()
        .done());
  }

  public void testStartProcessInstanceById() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    runtimeService.createProcessInstanceById(processDefinition.getId()).execute();

    assertThat(runtimeService.createProcessInstanceQuery().count(), is(1L));
  }

  public void testStartProcessInstanceByKey() {

    runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).execute();

    assertThat(runtimeService.createProcessInstanceQuery().count(), is(1L));
  }

  public void testStartProcessInstanceAndSetBusinessKey() {

    runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).businessKey("businessKey").execute();

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertThat(processInstance, is(notNullValue()));
    assertThat(processInstance.getBusinessKey(), is("businessKey"));
  }

  public void testStartProcessInstanceAndSetCaseInstanceId() {

    runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).caseInstanceId("caseInstanceId").execute();

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertThat(processInstance, is(notNullValue()));
    assertThat(processInstance.getCaseInstanceId(), is("caseInstanceId"));
  }

  public void testStartProcessInstanceAndSetVariable() {

    ProcessInstance processInstance = runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).setVariable("var", "value").execute();

    Object variable = runtimeService.getVariable(processInstance.getId(), "var");
    assertThat(variable, is(notNullValue()));
    assertThat(variable, is((Object) "value"));
  }

  public void testStartProcessInstanceAndSetVariables() {
    Map<String, Object> variables = Variables.createVariables().putValue("var1", "v1").putValue("var2", "v2");

    ProcessInstance processInstance = runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).setVariables(variables).execute();

    assertThat(runtimeService.getVariables(processInstance.getId()), is(variables));
  }

  public void testStartProcessInstanceNoSkipping() {

    runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).execute(false, false);

    assertThat(runtimeService.createProcessInstanceQuery().count(), is(1L));
  }

  public void testFailToStartProcessInstanceSkipListeners() {
    try {
      runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).execute(true, false);

      fail("expected exception");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage(), containsString("Cannot skip"));
    }
  }

  public void testFailToStartProcessInstanceSkipInputOutputMapping() {
    try {
      runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).execute(false, true);

      fail("expected exception");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage(), containsString("Cannot skip"));
    }
  }

}
