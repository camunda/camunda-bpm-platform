/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.ScriptCompilationException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Sebastian Menski
 */
public class ExternalScriptTaskTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testDefaultExternalScript() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    String greeting = (String) runtimeService.getVariable(processInstance.getId(), "greeting");
    assertNotNull(greeting);
    assertEquals("Greetings camunda BPM speaking", greeting);
  }

  @Deployment
  public void testDefaultExternalScriptAsVariable() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptPath", "org/camunda/bpm/engine/test/bpmn/scripttask/greeting.py");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variables);

    String greeting = (String) runtimeService.getVariable(processInstance.getId(), "greeting");
    assertNotNull(greeting);
    assertEquals("Greetings camunda BPM speaking", greeting);
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/bpmn/scripttask/ExternalScriptTaskTest.testDefaultExternalScriptAsVariable.bpmn20.xml"})
  public void testDefaultExternalScriptAsNonExistingVariable() {
    try {
      runtimeService.startProcessInstanceByKey("process");
      fail("Process variable 'scriptPath' not defined");
    }
    catch(ProcessEngineException e) {
      assertTextPresentIgnoreCase("Cannot resolve identifier 'scriptPath'", e.getMessage());
    }
  }

  @Deployment
  public void testDefaultExternalScriptAsBean() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptResourceBean", new ScriptResourceBean());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variables);

    String greeting = (String) runtimeService.getVariable(processInstance.getId(), "greeting");
    assertNotNull(greeting);
    assertEquals("Greetings camunda BPM speaking", greeting);
  }

  @Deployment
  public void testScriptInClasspath() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    String greeting = (String) runtimeService.getVariable(processInstance.getId(), "greeting");
    assertNotNull(greeting);
    assertEquals("Greetings camunda BPM speaking", greeting);
  }

  @Deployment
  public void testScriptInClasspathAsVariable() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptPath", "classpath://org/camunda/bpm/engine/test/bpmn/scripttask/greeting.py");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variables);

    String greeting = (String) runtimeService.getVariable(processInstance.getId(), "greeting");
    assertNotNull(greeting);
    assertEquals("Greetings camunda BPM speaking", greeting);
  }

  @Deployment
  public void testScriptInClasspathAsBean() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptResourceBean", new ScriptResourceBean());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variables);

    String greeting = (String) runtimeService.getVariable(processInstance.getId(), "greeting");
    assertNotNull(greeting);
    assertEquals("Greetings camunda BPM speaking", greeting);
  }

  @Deployment
  public void testScriptNotFoundInClasspath() {
    try {
      runtimeService.startProcessInstanceByKey("process");
      fail("Resource does not exist in classpath");
    }
    catch (NotFoundException e) {
      assertTextPresentIgnoreCase("unable to find resource at path classpath://org/camunda/bpm/engine/test/bpmn/scripttask/notexisting.py", e.getMessage());
    }
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/scripttask/ExternalScriptTaskTest.testScriptInDeployment.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/scripttask/greeting.py"
  })
  public void testScriptInDeployment() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    String greeting = (String) runtimeService.getVariable(processInstance.getId(), "greeting");
    assertNotNull(greeting);
    assertEquals("Greetings camunda BPM speaking", greeting);
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/scripttask/ExternalScriptTaskTest.testScriptInDeployment.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/scripttask/greeting.py"
  })
  public void testScriptInDeploymentAfterCacheWasCleaned() {
    processEngineConfiguration.getDeploymentCache().discardProcessDefinitionCache();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process");

    String greeting = (String) runtimeService.getVariable(processInstance.getId(), "greeting");
    assertNotNull(greeting);
    assertEquals("Greetings camunda BPM speaking", greeting);
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/scripttask/ExternalScriptTaskTest.testScriptInDeploymentAsVariable.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/scripttask/greeting.py"
  })
  public void testScriptInDeploymentAsVariable() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptPath", "deployment://org/camunda/bpm/engine/test/bpmn/scripttask/greeting.py");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variables);

    String greeting = (String) runtimeService.getVariable(processInstance.getId(), "greeting");
    assertNotNull(greeting);
    assertEquals("Greetings camunda BPM speaking", greeting);
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/scripttask/ExternalScriptTaskTest.testScriptInDeploymentAsBean.bpmn20.xml",
    "org/camunda/bpm/engine/test/bpmn/scripttask/greeting.py"
  })
  public void testScriptInDeploymentAsBean() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptResourceBean", new ScriptResourceBean());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("process", variables);

    String greeting = (String) runtimeService.getVariable(processInstance.getId(), "greeting");
    assertNotNull(greeting);
    assertEquals("Greetings camunda BPM speaking", greeting);
  }

  @Deployment
  public void testScriptNotFoundInDeployment() {
    try {
      runtimeService.startProcessInstanceByKey("process");
      fail("Resource does not exist in classpath");
    }
    catch (NotFoundException e) {
      assertTextPresentIgnoreCase("unable to find resource at path deployment://org/camunda/bpm/engine/test/bpmn/scripttask/notexisting.py", e.getMessage());
    }
  }

  @Deployment
  public void testNotExistingImport() {
    try {
      runtimeService.startProcessInstanceByKey("process");
      fail("Should fail during script compilation");
    }
    catch (ScriptCompilationException e) {
      assertTextPresentIgnoreCase("import unknown", e.getMessage());
    }
  }

}
