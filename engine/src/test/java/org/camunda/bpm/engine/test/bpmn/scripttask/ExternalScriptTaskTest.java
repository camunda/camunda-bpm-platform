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
package org.camunda.bpm.engine.test.bpmn.scripttask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.ScriptCompilationException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

/**
 * @author Sebastian Menski
 */
public class ExternalScriptTaskTest extends PluggableProcessEngineTest {

  @Deployment
  @Test
  public void testDefaultExternalScript() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    String greeting = (String) runtimeService.getVariable(processInstance.getId(), "greeting");
    assertNotNull(greeting);
    assertEquals("Greetings Camunda Platform speaking", greeting);
  }

  @Deployment
  @Test
  public void testDefaultExternalScriptAsVariable() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptPath", "org/camunda/bpm/engine/test/bpmn/scripttask/greeting.py");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variables);

    String greeting = (String) runtimeService.getVariable(processInstance.getId(), "greeting");
    assertNotNull(greeting);
    assertEquals("Greetings Camunda Platform speaking", greeting);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/scripttask/ExternalScriptTaskTest.testDefaultExternalScriptAsVariable.bpmn20.xml"})
  @Test
  public void testDefaultExternalScriptAsNonExistingVariable() {
    try {
      runtimeService.startProcessInstanceByKey("process");
      fail("Process variable 'scriptPath' not defined");
    }
    catch(ProcessEngineException e) {
      testRule.assertTextPresentIgnoreCase("Cannot resolve identifier 'scriptPath'", e.getMessage());
    }
  }

  @Deployment
  @Test
  public void testDefaultExternalScriptAsBean() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptResourceBean", new ScriptResourceBean());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variables);

    String greeting = (String) runtimeService.getVariable(processInstance.getId(), "greeting");
    assertNotNull(greeting);
    assertEquals("Greetings Camunda Platform speaking", greeting);
  }

  @Deployment
  @Test
  public void testScriptInClasspath() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    String greeting = (String) runtimeService.getVariable(processInstance.getId(), "greeting");
    assertNotNull(greeting);
    assertEquals("Greetings Camunda Platform speaking", greeting);
  }

  @Deployment
  @Test
  public void testScriptInClasspathAsVariable() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptPath", "classpath://org/camunda/bpm/engine/test/bpmn/scripttask/greeting.py");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variables);

    String greeting = (String) runtimeService.getVariable(processInstance.getId(), "greeting");
    assertNotNull(greeting);
    assertEquals("Greetings Camunda Platform speaking", greeting);
  }

  @Deployment
  @Test
  public void testScriptInClasspathAsBean() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptResourceBean", new ScriptResourceBean());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variables);

    String greeting = (String) runtimeService.getVariable(processInstance.getId(), "greeting");
    assertNotNull(greeting);
    assertEquals("Greetings Camunda Platform speaking", greeting);
  }

  @Deployment
  @Test
  public void testScriptNotFoundInClasspath() {
    try {
      runtimeService.startProcessInstanceByKey("process");
      fail("Resource does not exist in classpath");
    }
    catch (NotFoundException e) {
      testRule.assertTextPresentIgnoreCase("unable to find resource at path classpath://org/camunda/bpm/engine/test/bpmn/scripttask/notexisting.py", e.getMessage());
    }
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/scripttask/ExternalScriptTaskTest.testScriptInDeployment.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/scripttask/greeting.py"
  })
  @Test
  public void testScriptInDeployment() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    String greeting = (String) runtimeService.getVariable(processInstance.getId(), "greeting");
    assertNotNull(greeting);
    assertEquals("Greetings Camunda Platform speaking", greeting);
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/scripttask/ExternalScriptTaskTest.testScriptInDeployment.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/scripttask/greeting.py"
  })
  @Test
  public void testScriptInDeploymentAfterCacheWasCleaned() {
    processEngineConfiguration.getDeploymentCache().discardProcessDefinitionCache();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    String greeting = (String) runtimeService.getVariable(processInstance.getId(), "greeting");
    assertNotNull(greeting);
    assertEquals("Greetings Camunda Platform speaking", greeting);
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/scripttask/ExternalScriptTaskTest.testScriptInDeploymentAsVariable.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/scripttask/greeting.py"
  })
  @Test
  public void testScriptInDeploymentAsVariable() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptPath", "deployment://org/camunda/bpm/engine/test/bpmn/scripttask/greeting.py");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variables);

    String greeting = (String) runtimeService.getVariable(processInstance.getId(), "greeting");
    assertNotNull(greeting);
    assertEquals("Greetings Camunda Platform speaking", greeting);
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/scripttask/ExternalScriptTaskTest.testScriptInDeploymentAsBean.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/scripttask/greeting.py"
  })
  @Test
  public void testScriptInDeploymentAsBean() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptResourceBean", new ScriptResourceBean());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variables);

    String greeting = (String) runtimeService.getVariable(processInstance.getId(), "greeting");
    assertNotNull(greeting);
    assertEquals("Greetings Camunda Platform speaking", greeting);
  }

  @Deployment
  @Test
  public void testScriptNotFoundInDeployment() {
    try {
      runtimeService.startProcessInstanceByKey("process");
      fail("Resource does not exist in classpath");
    }
    catch (NotFoundException e) {
      testRule.assertTextPresentIgnoreCase("unable to find resource at path deployment://org/camunda/bpm/engine/test/bpmn/scripttask/notexisting.py", e.getMessage());
    }
  }

  @Deployment
  @Test
  public void testNotExistingImport() {
    try {
      runtimeService.startProcessInstanceByKey("process");
      fail("Should fail during script compilation");
    }
    catch (ScriptCompilationException e) {
      testRule.assertTextPresentIgnoreCase("import unknown", e.getMessage());
    }
  }

}
