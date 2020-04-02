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
package org.camunda.spin.plugin.script;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Daniel Meyer
 *
 */
@RunWith(Parameterized.class)
public class SpinScriptTaskSupportTest {

  @Rule
  public ProcessEngineRule engineRule = new ProcessEngineRule();

  @Parameters(name = "{index}: {0}")
  public static Object[] data() {
      return new Object[][] {
               { "groovy", "" },
               { "javascript", "" },
               { "python", "" },
               { "ruby", "$" }
         };
  }

  @Parameter(0)
  public String language;

  @Parameter(1)
  public String variablePrefix;

  private RuntimeService runtimeService;
  private RepositoryService repositoryService;


  @Before
  public void setUp() {
    this.runtimeService = engineRule.getRuntimeService();
    this.repositoryService = engineRule.getRepositoryService();
  }

  @Test
  public void testSpinAvailable() {
    deployProcess(language, setVariableScript("name", "S('<test />').name()"));
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    String var = (String) runtimeService.getVariable(pi.getId(), "name");
    assertThat(var).isEqualTo("test");
  }

  @Test
  public void testTwoScriptTasks() {
    // given
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .scriptTask()
        .scriptFormat(language)
        .scriptText(setVariableScript("task1Name", "S('<task1 />').name()"))
      .scriptTask()
        .scriptFormat(language)
        .scriptText(setVariableScript("task2Name", "S('<task2 />').name()"))
      .userTask()
      .endEvent()
    .done();

    Deployment deployment = repositoryService.createDeployment().addModelInstance("process.bpmn", modelInstance).deploy();
    engineRule.manageDeployment(deployment);

    // when
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    // then
    Object task1Name = runtimeService.getVariable(pi.getId(), "task1Name");
    assertThat(task1Name).isEqualTo("task1");

    Object task2Name = runtimeService.getVariable(pi.getId(), "task2Name");
    assertThat(task2Name).isEqualTo("task2");
  }

  protected String setVariableScript(String name, String valueExpression) {
    return scriptVariableName("execution") + ".setVariable('" + name + "',  " + valueExpression + ")";
  }

  protected String scriptVariableName(String name) {
    return variablePrefix + name;
  }

  protected void deployProcess(String scriptFormat, String scriptText) {
    BpmnModelInstance process = createProcess(scriptFormat, scriptText);
    Deployment deployment = repositoryService.createDeployment()
      .addModelInstance("testProcess.bpmn", process)
      .deploy();

    engineRule.manageDeployment(deployment);
  }

  protected BpmnModelInstance createProcess(String scriptFormat, String scriptText) {

    return Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .scriptTask()
        .scriptFormat(scriptFormat)
        .scriptText(scriptText)
      .userTask()
      .endEvent()
    .done();

  }
}
