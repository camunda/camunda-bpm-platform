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
package org.camunda.bpm.application.impl.deployment;

import java.util.List;

import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Roman Smirnov
 *
 */
public class CmmnDisabledTest extends ResourceProcessEngineTestCase {

  protected EmbeddedProcessApplication processApplication;

  public CmmnDisabledTest() {
    super("org/camunda/bpm/application/impl/deployment/cmmn.disabled.camunda.cfg.xml");
  }

  protected void setUp() throws Exception {
    processApplication = new EmbeddedProcessApplication();
    super.setUp();
  }

  public void testCmmnDisabled() {
    ProcessApplicationDeployment deployment = repositoryService.createDeployment(processApplication.getReference())
        .addClasspathResource("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
        .deploy();

    // process is deployed:
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertNotNull(processDefinition);
    assertEquals(1, processDefinition.getVersion());

    try {
      repositoryService.createCaseDefinitionQuery().singleResult();
      fail("Cmmn Disabled: It should not be possible to query for a case definition.");
    } catch (Exception e) {
      // expected
    }

    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  public void testVariableInstanceQuery() {
    ProcessApplicationDeployment deployment = repositoryService.createDeployment(processApplication.getReference())
        .addClasspathResource("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
        .deploy();

    VariableMap variables = Variables.createVariables().putValue("my-variable", "a-value");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);

    // variable instance query
    List<VariableInstance> result = runtimeService.createVariableInstanceQuery().list();
    assertEquals(1, result.size());

    VariableInstance variableInstance = result.get(0);
    assertEquals("my-variable", variableInstance.getName());

    // get variable
    assertNotNull(runtimeService.getVariable(processInstance.getId(), "my-variable"));

    // get variable local
    assertNotNull(runtimeService.getVariableLocal(processInstance.getId(), "my-variable"));

    repositoryService.deleteDeployment(deployment.getId(), true);
  }

}
