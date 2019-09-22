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
package org.camunda.bpm.engine.spring.test.scripttask;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.spring.test.SpringProcessEngineTestCase;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author Laszlo Palossy
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:org/camunda/bpm/engine/spring/test/scripttask/ScriptTaskTest-applicationContext.xml" })
public class ScriptTaskTest extends SpringProcessEngineTestCase {

  @Autowired
  private RuntimeService runtimeService;

  @Autowired
  private RepositoryService repositoryService;

  @Autowired
  Testbean testbean;

  private static final String JAVASCRIPT = "javascript";
  private static final String PYTHON = "python";
  private static final String GROOVY = "groovy";
  private static final String JUEL = "juel";

  private List<String> deploymentIds = new ArrayList<String>();

  @After
  public void after() {
    for (String deploymentId : deploymentIds) {
      repositoryService.deleteDeployment(deploymentId, true);
    }
  }

  @Test
  public void testJavascriptProcessVarVisibility() {

    deployProcess(JAVASCRIPT,

        // GIVEN
        // an execution variable 'foo', value is set to the testbean's name property
        "execution.setVariable('foo', testbean.name);");

    // GIVEN
    // that we start an instance of this process
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    // THEN
    // the script task can be executed without exceptions
    // the execution variable is stored and has the correct value
    Object variableValue = runtimeService.getVariable(pi.getId(), "foo");
    assertEquals("name property of the testbean", variableValue);

  }

  @Test
  public void testPythonProcessVarVisibility() {

    deployProcess(PYTHON,

        // GIVEN
        // an execution variable 'foo', value is set to the testbean's name property
        "execution.setVariable('foo', testbean.name)\n");

    // GIVEN
    // that we start an instance of this process
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    // THEN
    // the script task can be executed without exceptions
    // the execution variable is stored and has the correct value
    Object variableValue = runtimeService.getVariable(pi.getId(), "foo");
    assertEquals("name property of the testbean", variableValue);

  }

  @Test
  public void testGroovyProcessVarVisibility() {

    deployProcess(GROOVY,

        // GIVEN
        // an execution variable 'foo', value is set to the testbean's name property
        "execution.setVariable('foo', testbean.name)\n");

    // GIVEN
    // that we start an instance of this process
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    // THEN
    // the script task can be executed without exceptions
    // the execution variable is stored and has the correct value
    Object variableValue = runtimeService.getVariable(pi.getId(), "foo");
    assertEquals("name property of the testbean", variableValue);

  }

  @Test
  public void testJuelExpression() {
    deployProcess(JUEL, "${execution.setVariable('foo', testbean.name)}");

    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    String variableValue = (String) runtimeService.getVariable(pi.getId(), "foo");
    assertEquals("name property of the testbean", variableValue);
  }

  protected void deployProcess(BpmnModelInstance process) {
    Deployment deployment = repositoryService.createDeployment().addModelInstance("testProcess.bpmn", process).deploy();
    deploymentIds.add(deployment.getId());
  }

  protected void deployProcess(String scriptFormat, String scriptText) {
    BpmnModelInstance process = createProcess(scriptFormat, scriptText);
    deployProcess(process);
  }

  protected BpmnModelInstance createProcess(String scriptFormat, String scriptText) {

    return Bpmn.createExecutableProcess("testProcess").startEvent().scriptTask().scriptFormat(scriptFormat)
        .scriptText(scriptText).userTask().endEvent().done();
  }
}
