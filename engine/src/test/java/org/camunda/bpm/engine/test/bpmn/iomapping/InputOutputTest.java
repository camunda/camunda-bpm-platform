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
package org.camunda.bpm.engine.test.bpmn.iomapping;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * Testcase for camunda input / output in BPMN
 *
 * @author Daniel Meyer
 *
 */
public class InputOutputTest extends PluggableProcessEngineTestCase {

  // Input parameters /////////////////////////////////////////

  @Deployment
  public void testInputNullValue() {
    runtimeService.startProcessInstanceByKey("testProcess");
    Execution execution = runtimeService.createExecutionQuery().activityId("wait").singleResult();

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals("null", variable.getTypeName());
    assertEquals(execution.getId(), variable.getExecutionId());
  }

  @Deployment
  public void testInputStringConstantValue() {
    runtimeService.startProcessInstanceByKey("testProcess");
    Execution execution = runtimeService.createExecutionQuery().activityId("wait").singleResult();

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals("stringValue", variable.getValue());
    assertEquals(execution.getId(), variable.getExecutionId());
  }


  @Deployment
  public void testInputElValue() {
    runtimeService.startProcessInstanceByKey("testProcess");
    Execution execution = runtimeService.createExecutionQuery().activityId("wait").singleResult();

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2l, variable.getValue());
    assertEquals(execution.getId(), variable.getExecutionId());
  }

  @Deployment
  public void testInputScriptValue() {
    runtimeService.startProcessInstanceByKey("testProcess");
    Execution execution = runtimeService.createExecutionQuery().activityId("wait").singleResult();

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(execution.getId(), variable.getExecutionId());
  }

  @Deployment
  public void testInputScriptValueAsVariable() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptSource", "return 1 + 1");
    runtimeService.startProcessInstanceByKey("testProcess", variables);
    Execution execution = runtimeService.createExecutionQuery().activityId("wait").singleResult();

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(execution.getId(), variable.getExecutionId());
  }

  @Deployment
  public void testInputScriptValueAsBean() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("onePlusOneBean", new OnePlusOneBean());
    runtimeService.startProcessInstanceByKey("testProcess", variables);
    Execution execution = runtimeService.createExecutionQuery().activityId("wait").singleResult();

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(execution.getId(), variable.getExecutionId());
  }

  @Deployment
  public void testInputExternalScriptValue() {
    runtimeService.startProcessInstanceByKey("testProcess");
    Execution execution = runtimeService.createExecutionQuery().activityId("wait").singleResult();

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(execution.getId(), variable.getExecutionId());
  }

  @Deployment
  public void testInputExternalScriptValueAsVariable() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptPath", "org/camunda/bpm/engine/test/bpmn/iomapping/oneplusone.groovy");
    runtimeService.startProcessInstanceByKey("testProcess", variables);
    Execution execution = runtimeService.createExecutionQuery().activityId("wait").singleResult();

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(execution.getId(), variable.getExecutionId());
  }

  @Deployment
  public void testInputExternalScriptValueAsBean() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("onePlusOneBean", new OnePlusOneBean());
    runtimeService.startProcessInstanceByKey("testProcess", variables);
    Execution execution = runtimeService.createExecutionQuery().activityId("wait").singleResult();

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(execution.getId(), variable.getExecutionId());
  }

  @Deployment
  public void testInputExternalClasspathScriptValue() {
    runtimeService.startProcessInstanceByKey("testProcess");
    Execution execution = runtimeService.createExecutionQuery().activityId("wait").singleResult();

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(execution.getId(), variable.getExecutionId());
  }

  @Deployment
  public void testInputExternalClasspathScriptValueAsVariable() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptPath", "classpath://org/camunda/bpm/engine/test/bpmn/iomapping/oneplusone.groovy");
    runtimeService.startProcessInstanceByKey("testProcess", variables);
    Execution execution = runtimeService.createExecutionQuery().activityId("wait").singleResult();

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(execution.getId(), variable.getExecutionId());
  }

  @Deployment
  public void testInputExternalClasspathScriptValueAsBean() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("onePlusOneBean", new OnePlusOneBean());
    runtimeService.startProcessInstanceByKey("testProcess", variables);
    Execution execution = runtimeService.createExecutionQuery().activityId("wait").singleResult();

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(execution.getId(), variable.getExecutionId());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/iomapping/InputOutputTest.testInputExternalDeploymentScriptValue.bpmn",
    "org/camunda/bpm/engine/test/bpmn/iomapping/oneplusone.groovy"
  })
  public void testInputExternalDeploymentScriptValue() {
    runtimeService.startProcessInstanceByKey("testProcess");
    Execution execution = runtimeService.createExecutionQuery().activityId("wait").singleResult();

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(execution.getId(), variable.getExecutionId());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/iomapping/InputOutputTest.testInputExternalDeploymentScriptValueAsVariable.bpmn",
    "org/camunda/bpm/engine/test/bpmn/iomapping/oneplusone.groovy"
  })
  public void testInputExternalDeploymentScriptValueAsVariable() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptPath", "deployment://org/camunda/bpm/engine/test/bpmn/iomapping/oneplusone.groovy");
    runtimeService.startProcessInstanceByKey("testProcess", variables);
    Execution execution = runtimeService.createExecutionQuery().activityId("wait").singleResult();

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(execution.getId(), variable.getExecutionId());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/iomapping/InputOutputTest.testInputExternalDeploymentScriptValueAsBean.bpmn",
    "org/camunda/bpm/engine/test/bpmn/iomapping/oneplusone.groovy"
  })
  public void testInputExternalDeploymentScriptValueAsBean() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("onePlusOneBean", new OnePlusOneBean());
    runtimeService.startProcessInstanceByKey("testProcess", variables);
    Execution execution = runtimeService.createExecutionQuery().activityId("wait").singleResult();

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(execution.getId(), variable.getExecutionId());
  }

  @Deployment
  @SuppressWarnings("unchecked")
  public void testInputListElValues() {
    runtimeService.startProcessInstanceByKey("testProcess");

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    List<Object> value = (List<Object>) variable.getValue();
    assertEquals(2l, value.get(0));
    assertEquals(3l, value.get(1));
    assertEquals(4l, value.get(2));
  }

  @Deployment
  @SuppressWarnings("unchecked")
  public void testInputListMixedValues() {
    runtimeService.startProcessInstanceByKey("testProcess");

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    List<Object> value = (List<Object>) variable.getValue();
    assertEquals("constantStringValue", value.get(0));
    assertEquals("elValue", value.get(1));
    assertEquals("scriptValue", value.get(2));
  }

  @Deployment
  @SuppressWarnings("unchecked")
  public void testInputMapElValues() {
    runtimeService.startProcessInstanceByKey("testProcess");

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    TreeMap<String, Object> value = (TreeMap) variable.getValue();
    assertEquals(2l, value.get("a"));
    assertEquals(3l, value.get("b"));
    assertEquals(4l, value.get("c"));

  }

  @Deployment
  public void testInputMultipleElValue() {
    runtimeService.startProcessInstanceByKey("testProcess");
    Execution execution = runtimeService.createExecutionQuery().activityId("wait").singleResult();

    VariableInstance var1 = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(var1);
    assertEquals(2l, var1.getValue());
    assertEquals(execution.getId(), var1.getExecutionId());

    VariableInstance var2 = runtimeService.createVariableInstanceQuery().variableName("var2").singleResult();
    assertNotNull(var2);
    assertEquals(3l, var2.getValue());
    assertEquals(execution.getId(), var2.getExecutionId());
  }

  @Deployment
  public void testInputMultipleMixedValue() {
    runtimeService.startProcessInstanceByKey("testProcess");
    Execution execution = runtimeService.createExecutionQuery().activityId("wait").singleResult();

    VariableInstance var1 = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(var1);
    assertEquals(2l, var1.getValue());
    assertEquals(execution.getId(), var1.getExecutionId());

    VariableInstance var2 = runtimeService.createVariableInstanceQuery().variableName("var2").singleResult();
    assertNotNull(var2);
    assertEquals("stringConstantValue", var2.getValue());
    assertEquals(execution.getId(), var2.getExecutionId());
  }

  @Deployment
  @SuppressWarnings("unchecked")
  public void testInputNested() {
    runtimeService.startProcessInstanceByKey("testProcess");
    Execution execution = runtimeService.createExecutionQuery().activityId("wait").singleResult();

    VariableInstance var1 = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    TreeMap<String, Object> value = (TreeMap) var1.getValue();
    List<Object> nestedList = (List<Object>) value.get("a");
    assertEquals("stringInListNestedInMap", nestedList.get(0));
    assertEquals("b", nestedList.get(1));

    VariableInstance var2 = runtimeService.createVariableInstanceQuery().variableName("var2").singleResult();
    assertNotNull(var2);
    assertEquals("stringConstantValue", var2.getValue());
    assertEquals(execution.getId(), var2.getExecutionId());
  }

  @Deployment
  @SuppressWarnings("unchecked")
  public void testInputNestedListValues() {
    runtimeService.startProcessInstanceByKey("testProcess");

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    List<Object> value = (List<Object>) variable.getValue();
    assertEquals("constantStringValue", value.get(0));
    assertEquals("elValue", value.get(1));
    assertEquals("scriptValue", value.get(2));

    List<Object> nestedList = (List<Object>) value.get(3);
    List<Object> nestedNestedList = (List<Object>) nestedList.get(0);
    assertEquals("a", nestedNestedList.get(0));
    assertEquals("b", nestedNestedList.get(1));
    assertEquals("c", nestedNestedList.get(2));
    assertEquals("d", nestedList.get(1));

    TreeMap<String, Object> nestedMap = (TreeMap<String, Object>) value.get(4);
    assertEquals("bar", nestedMap.get("foo"));
    assertEquals("world", nestedMap.get("hello"));
  }

  // output parameter ///////////////////////////////////////////////////////

  @Deployment
  public void testOutputNullValue() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals("null", variable.getTypeName());
    assertEquals(pi.getId(), variable.getExecutionId());
  }

  @Deployment
  public void testOutputStringConstantValue() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals("stringValue", variable.getValue());
    assertEquals(pi.getId(), variable.getExecutionId());
  }


  @Deployment
  public void testOutputElValue() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2l, variable.getValue());
    assertEquals(pi.getId(), variable.getExecutionId());
  }

  @Deployment
  public void testOutputScriptValue() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(pi.getId(), variable.getExecutionId());
  }

  @Deployment
  public void testOutputScriptValueAsVariable() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptSource", "return 1 + 1");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess", variables);

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(pi.getId(), variable.getExecutionId());
  }

  @Deployment
  public void testOutputScriptValueAsBean() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("onePlusOneBean", new OnePlusOneBean());
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess", variables);

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(pi.getId(), variable.getExecutionId());
  }

  @Deployment
  public void testOutputExternalScriptValue() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(pi.getId(), variable.getExecutionId());
  }

  @Deployment
  public void testOutputExternalScriptValueAsVariable() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptPath", "org/camunda/bpm/engine/test/bpmn/iomapping/oneplusone.groovy");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess", variables);

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(pi.getId(), variable.getExecutionId());
  }

  @Deployment
  public void testOutputExternalScriptValueAsBean() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("onePlusOneBean", new OnePlusOneBean());
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess", variables);

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(pi.getId(), variable.getExecutionId());
  }

  @Deployment
  public void testOutputExternalClasspathScriptValue() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(pi.getId(), variable.getExecutionId());
  }

  @Deployment
  public void testOutputExternalClasspathScriptValueAsVariable() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptPath", "classpath://org/camunda/bpm/engine/test/bpmn/iomapping/oneplusone.groovy");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess", variables);

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(pi.getId(), variable.getExecutionId());
  }

  @Deployment
  public void testOutputExternalClasspathScriptValueAsBean() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("onePlusOneBean", new OnePlusOneBean());
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess", variables);

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(pi.getId(), variable.getExecutionId());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/iomapping/InputOutputTest.testOutputExternalDeploymentScriptValue.bpmn",
    "org/camunda/bpm/engine/test/bpmn/iomapping/oneplusone.groovy"
  })
  public void testOutputExternalDeploymentScriptValue() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(pi.getId(), variable.getExecutionId());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/iomapping/InputOutputTest.testOutputExternalDeploymentScriptValueAsVariable.bpmn",
    "org/camunda/bpm/engine/test/bpmn/iomapping/oneplusone.groovy"
  })
  public void testOutputExternalDeploymentScriptValueAsVariable() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("scriptPath", "deployment://org/camunda/bpm/engine/test/bpmn/iomapping/oneplusone.groovy");
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess", variables);

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(pi.getId(), variable.getExecutionId());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/bpmn/iomapping/InputOutputTest.testOutputExternalDeploymentScriptValueAsBean.bpmn",
    "org/camunda/bpm/engine/test/bpmn/iomapping/oneplusone.groovy"
  })
  public void testOutputExternalDeploymentScriptValueAsBean() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("onePlusOneBean", new OnePlusOneBean());
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess", variables);

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    assertEquals(2, variable.getValue());
    assertEquals(pi.getId(), variable.getExecutionId());
  }

  @Deployment
  @SuppressWarnings("unchecked")
  public void testOutputListElValues() {
    runtimeService.startProcessInstanceByKey("testProcess");

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    List<Object> value = (List<Object>) variable.getValue();
    assertEquals(2l, value.get(0));
    assertEquals(3l, value.get(1));
    assertEquals(4l, value.get(2));
  }

  @Deployment
  @SuppressWarnings("unchecked")
  public void testOutputListMixedValues() {
    runtimeService.startProcessInstanceByKey("testProcess");

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    List<Object> value = (List<Object>) variable.getValue();
    assertEquals("constantStringValue", value.get(0));
    assertEquals("elValue", value.get(1));
    assertEquals("scriptValue", value.get(2));
  }

  @Deployment
  @SuppressWarnings("unchecked")
  public void testOutputMapElValues() {
    runtimeService.startProcessInstanceByKey("testProcess");

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    TreeMap<String, Object> value = (TreeMap) variable.getValue();
    assertEquals(2l, value.get("a"));
    assertEquals(3l, value.get("b"));
    assertEquals(4l, value.get("c"));

  }

  @Deployment
  public void testOutputMultipleElValue() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    VariableInstance var1 = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(var1);
    assertEquals(2l, var1.getValue());
    assertEquals(pi.getId(), var1.getExecutionId());

    VariableInstance var2 = runtimeService.createVariableInstanceQuery().variableName("var2").singleResult();
    assertNotNull(var2);
    assertEquals(3l, var2.getValue());
    assertEquals(pi.getId(), var2.getExecutionId());
  }

  @Deployment
  public void testOutputMultipleMixedValue() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    VariableInstance var1 = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(var1);
    assertEquals(2l, var1.getValue());
    assertEquals(pi.getId(), var1.getExecutionId());

    VariableInstance var2 = runtimeService.createVariableInstanceQuery().variableName("var2").singleResult();
    assertNotNull(var2);
    assertEquals("stringConstantValue", var2.getValue());
    assertEquals(pi.getId(), var2.getExecutionId());
  }

  @Deployment
  @SuppressWarnings("unchecked")
  public void testOutputNested() {
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("testProcess");

    VariableInstance var1 = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    TreeMap<String, Object> value = (TreeMap) var1.getValue();
    List<Object> nestedList = (List<Object>) value.get("a");
    assertEquals("stringInListNestedInMap", nestedList.get(0));
    assertEquals("b", nestedList.get(1));
    assertEquals(pi.getId(), var1.getExecutionId());

    VariableInstance var2 = runtimeService.createVariableInstanceQuery().variableName("var2").singleResult();
    assertNotNull(var2);
    assertEquals("stringConstantValue", var2.getValue());
    assertEquals(pi.getId(), var2.getExecutionId());
  }

  @Deployment
  @SuppressWarnings("unchecked")
  public void testOutputListNestedValues() {
    runtimeService.startProcessInstanceByKey("testProcess");

    VariableInstance variable = runtimeService.createVariableInstanceQuery().variableName("var1").singleResult();
    assertNotNull(variable);
    List<Object> value = (List<Object>) variable.getValue();
    assertEquals("constantStringValue", value.get(0));
    assertEquals("elValue", value.get(1));
    assertEquals("scriptValue", value.get(2));

    List<Object> nestedList = (List<Object>) value.get(3);
    List<Object> nestedNestedList = (List<Object>) nestedList.get(0);
    assertEquals("a", nestedNestedList.get(0));
    assertEquals("b", nestedNestedList.get(1));
    assertEquals("c", nestedNestedList.get(2));
    assertEquals("d", nestedList.get(1));

    TreeMap<String, Object> nestedMap = (TreeMap<String, Object>) value.get(4);
    assertEquals("bar", nestedMap.get("foo"));
    assertEquals("world", nestedMap.get("hello"));
  }

  // ensure Io supported on event subprocess /////////////////////////////////

  public void testInterruptingEventSubprocessIoSupport() {
    try {
      repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/bpmn/iomapping/InputOutputTest.testInterruptingEventSubprocessIoSupport.bpmn")
        .deploy();
      fail("exception expected");
    } catch (ProcessEngineException e) {
      // happy path
      assertTextPresent("camunda:inputOutput mapping unsupported for element type 'subProcess' with attribute 'triggeredByEvent = true'", e.getMessage());
    }
  }

  @Deployment
  public void testSubprocessIoSupport() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("processVar", "value");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testProcess", variables);

    Execution subprocessExecution = runtimeService.createExecutionQuery().activityId("subprocessTask").singleResult();
    Map<String, Object> variablesLocal = runtimeService.getVariablesLocal(subprocessExecution.getId());
    assertEquals(1, variablesLocal.size());
    assertEquals("value", variablesLocal.get("innerVar"));

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    String outerVariable = (String) runtimeService.getVariableLocal(processInstance.getId(), "outerVar");
    assertNotNull(outerVariable);
    assertEquals("value", outerVariable);


  }

  @Deployment
  public void testSequentialMIActivityIoSupport() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("counter", new AtomicInteger());
    variables.put("nrOfLoops", 2);
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("miSequentialActivity", variables);

    // first sequential mi execution
    Execution miExecution = runtimeService.createExecutionQuery().activityId("miTask").singleResult();
    assertNotNull(miExecution);
    assertFalse(instance.getId().equals(miExecution.getId()));
    assertEquals(0, runtimeService.getVariable(miExecution.getId(), "loopCounter"));

    // input mapping
    assertEquals(1, runtimeService.createVariableInstanceQuery().variableName("miCounterValue").count());
    assertEquals(1, runtimeService.getVariableLocal(miExecution.getId(), "miCounterValue"));

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // second sequential mi execution
    miExecution = runtimeService.createExecutionQuery().activityId("miTask").singleResult();
    assertNotNull(miExecution);
    assertFalse(instance.getId().equals(miExecution.getId()));
    assertEquals(1, runtimeService.getVariable(miExecution.getId(), "loopCounter"));

    // input mapping
    assertEquals(1, runtimeService.createVariableInstanceQuery().variableName("miCounterValue").count());
    assertEquals(2, runtimeService.getVariableLocal(miExecution.getId(), "miCounterValue"));

    task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // variable does not exist outside of scope
    assertEquals(0, runtimeService.createVariableInstanceQuery().variableName("miCounterValue").count());
  }

  @Deployment
  public void testSequentialMISubprocessIoSupport() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("counter", new AtomicInteger());
    variables.put("nrOfLoops", 2);
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("miSequentialSubprocess", variables);

    // first sequential mi execution
    Execution miScopeExecution = runtimeService.createExecutionQuery().activityId("task").singleResult();
    assertNotNull(miScopeExecution);
    assertEquals(0, runtimeService.getVariable(miScopeExecution.getId(), "loopCounter"));

    // input mapping
    assertEquals(1, runtimeService.createVariableInstanceQuery().variableName("miCounterValue").count());
    assertEquals(1, runtimeService.getVariableLocal(miScopeExecution.getId(), "miCounterValue"));

    Task task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // second sequential mi execution
    miScopeExecution = runtimeService.createExecutionQuery().activityId("task").singleResult();
    assertNotNull(miScopeExecution);
    assertFalse(instance.getId().equals(miScopeExecution.getId()));
    assertEquals(1, runtimeService.getVariable(miScopeExecution.getId(), "loopCounter"));

    // input mapping
    assertEquals(1, runtimeService.createVariableInstanceQuery().variableName("miCounterValue").count());
    assertEquals(2, runtimeService.getVariableLocal(miScopeExecution.getId(), "miCounterValue"));

    task = taskService.createTaskQuery().singleResult();
    taskService.complete(task.getId());

    // variable does not exist outside of scope
    assertEquals(0, runtimeService.createVariableInstanceQuery().variableName("miCounterValue").count());
  }

  @Deployment
  public void testParallelMIActivityIoSupport() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("counter", new AtomicInteger());
    variables.put("nrOfLoops", 2);
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("miParallelActivity", variables);

    Set<Integer> counters = new HashSet<Integer>();

    // first mi execution
    Execution miExecution1 = runtimeService.createExecutionQuery().activityId("miTask")
        .variableValueEquals("loopCounter", 0).singleResult();
    assertNotNull(miExecution1);
    assertFalse(instance.getId().equals(miExecution1.getId()));
    counters.add((Integer) runtimeService.getVariableLocal(miExecution1.getId(), "miCounterValue"));

    // second mi execution
    Execution miExecution2 = runtimeService.createExecutionQuery().activityId("miTask")
        .variableValueEquals("loopCounter", 1).singleResult();
    assertNotNull(miExecution2);
    assertFalse(instance.getId().equals(miExecution2.getId()));
    counters.add((Integer) runtimeService.getVariableLocal(miExecution2.getId(), "miCounterValue"));

    assertTrue(counters.contains(1));
    assertTrue(counters.contains(2));

    assertEquals(2, runtimeService.createVariableInstanceQuery().variableName("miCounterValue").count());

    for (Task task : taskService.createTaskQuery().list()) {
      taskService.complete(task.getId());
    }

    // variable does not exist outside of scope
    assertEquals(0, runtimeService.createVariableInstanceQuery().variableName("miCounterValue").count());
  }

  @Deployment
  public void testParallelMISubprocessIoSupport() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("counter", new AtomicInteger());
    variables.put("nrOfLoops", 2);
    ProcessInstance instance = runtimeService.startProcessInstanceByKey("miParallelSubprocess", variables);

    Set<Integer> counters = new HashSet<Integer>();

    // first parallel mi execution
    Execution miScopeExecution1 = runtimeService.createExecutionQuery().activityId("task")
        .variableValueEquals("loopCounter", 0).singleResult();
    assertNotNull(miScopeExecution1);
    counters.add((Integer) runtimeService.getVariableLocal(miScopeExecution1.getId(), "miCounterValue"));

    // second parallel mi execution
    Execution miScopeExecution2 = runtimeService.createExecutionQuery().activityId("task")
        .variableValueEquals("loopCounter", 1).singleResult();
    assertNotNull(miScopeExecution2);
    assertFalse(instance.getId().equals(miScopeExecution2.getId()));
    counters.add((Integer) runtimeService.getVariableLocal(miScopeExecution2.getId(), "miCounterValue"));

    assertTrue(counters.contains(1));
    assertTrue(counters.contains(2));

    assertEquals(2, runtimeService.createVariableInstanceQuery().variableName("miCounterValue").count());

    for (Task task : taskService.createTaskQuery().list()) {
      taskService.complete(task.getId());
    }

    // variable does not exist outside of scope
    assertEquals(0, runtimeService.createVariableInstanceQuery().variableName("miCounterValue").count());
  }

  public void testMIOutputMappingDisallowed() {
    try {
      repositoryService.createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/bpmn/iomapping/InputOutputTest.testMIOutputMappingDisallowed.bpmn20.xml")
      .deploy();
      fail("Exception expected");
    } catch (ProcessEngineException e) {
      assertTextPresent("camunda:outputParameter not allowed for multi-instance constructs", e.getMessage());
    }

  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/iomapping/InputOutputTest.testThrowErrorInScriptInputOutputMapping.bpmn")
  public void FAILING_testBpmnErrorInScriptInputMapping() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("throwInMapping", "in");
    variables.put("exception", new BpmnError("error"));
    runtimeService.startProcessInstanceByKey("testProcess", variables);
    //we will only reach the user task if the BPMNError from the script was handled by the boundary event
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName(), is("User Task"));
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/iomapping/InputOutputTest.testThrowErrorInScriptInputOutputMapping.bpmn")
  public void testExceptionInScriptInputMapping() {
    String exceptionMessage = "myException";
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("throwInMapping", "in");
    variables.put("exception", new RuntimeException(exceptionMessage));
    try {
      runtimeService.startProcessInstanceByKey("testProcess", variables);
    } catch(RuntimeException re){
      assertThat(re.getMessage(), containsString(exceptionMessage));
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/iomapping/InputOutputTest.testThrowErrorInScriptInputOutputMapping.bpmn")
  public void FAILING_testBpmnErrorInScriptOutputMapping() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("throwInMapping", "out");
    variables.put("exception", new BpmnError("error"));
    runtimeService.startProcessInstanceByKey("testProcess", variables);
    //we will only reach the user task if the BPMNError from the script was handled by the boundary event
    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task.getName(), is("User Task"));
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/bpmn/iomapping/InputOutputTest.testThrowErrorInScriptInputOutputMapping.bpmn")
  public void testExceptionInScriptOutputMapping() {
    String exceptionMessage = "myException";
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("throwInMapping", "out");
    variables.put("exception", new RuntimeException(exceptionMessage));
    try {
      runtimeService.startProcessInstanceByKey("testProcess", variables);
    } catch(RuntimeException re){
      assertThat(re.getMessage(), containsString(exceptionMessage));
    }
  }

  @Deployment
  public void FAILING_testOutputMappingOnErrorBoundaryEvent() {

    // case 1: no error occurs
    runtimeService.startProcessInstanceByKey("testProcess");

    Task task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);
    assertEquals("taskOk", task.getTaskDefinitionKey());

    // then: variable mapped exists
    assertEquals(0, runtimeService.createVariableInstanceQuery().variableName("localNotMapped").count());
    assertEquals(0, runtimeService.createVariableInstanceQuery().variableName("localMapped").count());
    assertEquals(1, runtimeService.createVariableInstanceQuery().variableName("mapped").count());

    taskService.complete(task.getId());

    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    // case 2: error occurs
    runtimeService.startProcessInstanceByKey("testProcess", Collections.<String, Object>singletonMap("throwError", true));

    task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);
    assertEquals("taskError", task.getTaskDefinitionKey());

    assertEquals(0, runtimeService.createVariableInstanceQuery().variableName("localNotMapped").count());
    assertEquals(0, runtimeService.createVariableInstanceQuery().variableName("localMapped").count());
    assertEquals(0, runtimeService.createVariableInstanceQuery().variableName("mapped").count());

    taskService.complete(task.getId());

    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment
  public void FAILING_testOutputMappingOnMessageBoundaryEvent() {

    // case 1: no error occurs
    runtimeService.startProcessInstanceByKey("testProcess");

    Task task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);
    assertEquals("wait", task.getTaskDefinitionKey());

    taskService.complete(task.getId());

    task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);
    assertEquals("taskOk", task.getTaskDefinitionKey());

    // then: variable mapped exists
    assertEquals(1, runtimeService.createVariableInstanceQuery().variableName("mapped").count());

    taskService.complete(task.getId());

    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    // case 2: error occurs
    runtimeService.startProcessInstanceByKey("testProcess", Collections.<String, Object>singletonMap("throwError", true));

    task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);
    assertEquals("wait", task.getTaskDefinitionKey());

    runtimeService.correlateMessage("message");

    task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);
    assertEquals("taskError", task.getTaskDefinitionKey());

    assertEquals(0, runtimeService.createVariableInstanceQuery().variableName("mapped").count());

    taskService.complete(task.getId());

    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment
  public void FAILING_testOutputMappingOnTimerBoundaryEvent() {

    // case 1: no error occurs
    runtimeService.startProcessInstanceByKey("testProcess");

    Task task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);
    assertEquals("wait", task.getTaskDefinitionKey());

    taskService.complete(task.getId());

    task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);
    assertEquals("taskOk", task.getTaskDefinitionKey());

    // then: variable mapped exists
    assertEquals(1, runtimeService.createVariableInstanceQuery().variableName("mapped").count());

    taskService.complete(task.getId());

    assertEquals(0, runtimeService.createProcessInstanceQuery().count());

    // case 2: error occurs
    runtimeService.startProcessInstanceByKey("testProcess", Collections.<String, Object>singletonMap("throwError", true));

    task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);
    assertEquals("wait", task.getTaskDefinitionKey());

    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    managementService.executeJob(job.getId());

    task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);
    assertEquals("taskError", task.getTaskDefinitionKey());

    assertEquals(0, runtimeService.createVariableInstanceQuery().variableName("mapped").count());

    taskService.complete(task.getId());

    assertEquals(0, runtimeService.createProcessInstanceQuery().count());
  }

  @Deployment
  public void testScopeActivityInstanceId() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    ActivityInstance tree = runtimeService.getActivityInstance(processInstanceId);
    ActivityInstance theTaskInstance = tree.getActivityInstances("theTask")[0];

    // when
    VariableInstance variableInstance = runtimeService
      .createVariableInstanceQuery()
      .singleResult();

    // then
    assertEquals(theTaskInstance.getId(), variableInstance.getActivityInstanceId());
  }

}
