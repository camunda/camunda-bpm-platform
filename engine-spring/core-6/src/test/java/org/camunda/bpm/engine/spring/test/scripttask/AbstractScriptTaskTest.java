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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.ScriptEvaluationException;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractScriptTaskTest {

  @Autowired
  private RuntimeService runtimeService;

  @Autowired
  private RepositoryService repositoryService;

  private static final String TEST_BEAN_NAME = "name property of testbean";

  private List<String> deploymentIds = new ArrayList<>();

  @After
  public void after() {
    for (String deploymentId : deploymentIds) {
      repositoryService.deleteDeployment(deploymentId, true);
    }
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
    return Bpmn.createExecutableProcess("testProcess")
        .startEvent()
        .scriptTask()
        .scriptFormat(scriptFormat)
        .scriptText(scriptText).userTask().endEvent().done();
  }

  /**
   * Test if a Spring bean is visible for scripting for the scripftormat
   *
   * @param scriptFormat
   *          the scriptformat like 'javascript', 'groovy', etc.
   * @param scriptText
   *          sets execution variable 'foo' to the testbean's name property
   */
  protected void testSpringBeanVisibility(String scriptFormat, String scriptText) {
    testSpringBeanVisibility(scriptFormat, scriptText, true);
  }

  protected void testSpringBeanVisibility(String scriptFormat, String scriptText, boolean visible) {

    // GIVEN
    // execution variable 'foo' is set the testbean's name property (by the
    // scriptText code)
    deployProcess(scriptFormat, scriptText);

    // WHEN
    // we start an instance of this process
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    // THEN
    // the script task can be executed without exceptions
    // the execution variable is stored and has the correct value
    Object variableValue = runtimeService.getVariable(pi.getId(), "foo");
    if (visible) {
      assertEquals(TEST_BEAN_NAME, variableValue);
    } else {
      assertNull(variableValue);
    }
  }

  protected void testNoSpringBean(String scriptFormat, String scriptText) {
    // GIVEN
    // accessing a bean that cannot be resolved in a script
    deployProcess(scriptFormat, scriptText);

    // THEN
    // an exception is thrown
    assertThatThrownBy(() -> {
        // WHEN
        // we start an instance of this process
        runtimeService.startProcessInstanceByKey("testProcess");})
      .isInstanceOf(ScriptEvaluationException.class)
      .hasMessageContaining("testbean");
  }

}
