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
package org.camunda.bpm.engine.test.examples.variables;

import static org.camunda.bpm.engine.variable.Variables.createVariables;
import static org.camunda.bpm.engine.variable.Variables.objectValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.history.SerializableVariable;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;

/**
 * @author Tom Baeyens
 */
public class VariablesTest extends PluggableProcessEngineTestCase {

  @Deployment
  public void testBasicVariableOperations() {

    Date now = new Date();
    List<String> serializable = new ArrayList<String>();
    serializable.add("one");
    serializable.add("two");
    serializable.add("three");
    byte[] bytes = "somebytes".getBytes();

    // Start process instance with different types of variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("longVar", 928374L);
    variables.put("shortVar", (short) 123);
    variables.put("integerVar", 1234);
    variables.put("stringVar", "coca-cola");
    variables.put("dateVar", now);
    variables.put("nullVar", null);
    variables.put("serializableVar", serializable);
    variables.put("bytesVar", bytes);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskAssigneeProcess", variables);

    variables = runtimeService.getVariables(processInstance.getId());
    assertEquals("coca-cola", variables.get("stringVar"));
    assertEquals(928374L, variables.get("longVar"));
    assertEquals((short) 123, variables.get("shortVar"));
    assertEquals(1234, variables.get("integerVar"));
    assertEquals(now, variables.get("dateVar"));
    assertEquals(null, variables.get("nullVar"));
    assertEquals(serializable, variables.get("serializableVar"));
    assertTrue(Arrays.equals(bytes, (byte[]) variables.get("bytesVar")));
    assertEquals(8, variables.size());

    // Set all existing variables values to null
    runtimeService.setVariable(processInstance.getId(), "longVar", null);
    runtimeService.setVariable(processInstance.getId(), "shortVar", null);
    runtimeService.setVariable(processInstance.getId(), "integerVar", null);
    runtimeService.setVariable(processInstance.getId(), "stringVar", null);
    runtimeService.setVariable(processInstance.getId(), "dateVar", null);
    runtimeService.setVariable(processInstance.getId(), "nullVar", null);
    runtimeService.setVariable(processInstance.getId(), "serializableVar", null);
    runtimeService.setVariable(processInstance.getId(), "bytesVar", null);

    variables = runtimeService.getVariables(processInstance.getId());
    assertEquals(null, variables.get("longVar"));
    assertEquals(null, variables.get("shortVar"));
    assertEquals(null, variables.get("integerVar"));
    assertEquals(null, variables.get("stringVar"));
    assertEquals(null, variables.get("dateVar"));
    assertEquals(null, variables.get("nullVar"));
    assertEquals(null, variables.get("serializableVar"));
    assertEquals(null, variables.get("bytesVar"));
    assertEquals(8, variables.size());

    // Update existing variable values again, and add a new variable
    runtimeService.setVariable(processInstance.getId(), "new var", "hi");
    runtimeService.setVariable(processInstance.getId(), "longVar", 9987L);
    runtimeService.setVariable(processInstance.getId(), "shortVar", (short) 456);
    runtimeService.setVariable(processInstance.getId(), "integerVar", 4567);
    runtimeService.setVariable(processInstance.getId(), "stringVar", "colgate");
    runtimeService.setVariable(processInstance.getId(), "dateVar", now);
    runtimeService.setVariable(processInstance.getId(), "serializableVar", serializable);
    runtimeService.setVariable(processInstance.getId(), "bytesVar", bytes);

    variables = runtimeService.getVariables(processInstance.getId());
    assertEquals("hi", variables.get("new var"));
    assertEquals(9987L, variables.get("longVar"));
    assertEquals((short)456, variables.get("shortVar"));
    assertEquals(4567, variables.get("integerVar"));
    assertEquals("colgate", variables.get("stringVar"));
    assertEquals(now, variables.get("dateVar"));
    assertEquals(null, variables.get("nullVar"));
    assertEquals(serializable, variables.get("serializableVar"));
    assertTrue(Arrays.equals(bytes, (byte[]) variables.get("bytesVar")));
    assertEquals(9, variables.size());

    Collection<String> varFilter = new ArrayList<String>(2);
    varFilter.add("stringVar");
    varFilter.add("integerVar");

    Map<String, Object> filteredVariables = runtimeService.getVariables(processInstance.getId(), varFilter);
    assertEquals(2, filteredVariables.size());
    assertTrue(filteredVariables.containsKey("stringVar"));
    assertTrue(filteredVariables.containsKey("integerVar"));

    // Try setting the value of the variable that was initially created with value 'null'
    runtimeService.setVariable(processInstance.getId(), "nullVar", "a value");
    Object newValue = runtimeService.getVariable(processInstance.getId(), "nullVar");
    assertNotNull(newValue);
    assertEquals("a value", newValue);

    // Try setting the value of the serializableVar to an integer value
    runtimeService.setVariable(processInstance.getId(), "serializableVar", 100);
    variables = runtimeService.getVariables(processInstance.getId());
    assertEquals(100, variables.get("serializableVar"));

    // Try setting the value of the serializableVar back to a serializable value
    runtimeService.setVariable(processInstance.getId(), "serializableVar", serializable);
    variables = runtimeService.getVariables(processInstance.getId());
    assertEquals(serializable, variables.get("serializableVar"));
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/examples/variables/VariablesTest.testBasicVariableOperations.bpmn20.xml"})
  public void testOnlyChangeType() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariable", 1234);
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("taskAssigneeProcess", variables);

    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableName("aVariable");

    VariableInstance variable = query.singleResult();
    assertEquals(ValueType.INTEGER.getName(), variable.getTypeName());

    runtimeService.setVariable(pi.getId(), "aVariable", 1234L);
    variable = query.singleResult();
    assertEquals(ValueType.LONG.getName(), variable.getTypeName());

    runtimeService.setVariable(pi.getId(), "aVariable", (short)1234);
    variable = query.singleResult();
    assertEquals(ValueType.SHORT.getName(), variable.getTypeName());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/examples/variables/VariablesTest.testBasicVariableOperations.bpmn20.xml"})
  public void testChangeTypeFromSerializableUsingApi() {

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariable", new SerializableVariable("foo"));
    ProcessInstance pi = runtimeService.startProcessInstanceByKey("taskAssigneeProcess", variables);

    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableName("aVariable");

    VariableInstance variable = query.singleResult();
    assertEquals(ValueType.OBJECT.getName(), variable.getTypeName());

    runtimeService.setVariable(pi.getId(), "aVariable", null);
    variable = query.singleResult();
    assertEquals(ValueType.NULL.getName(), variable.getTypeName());

  }

  @Deployment
  public void testChangeSerializableInsideEngine() {

    runtimeService.startProcessInstanceByKey("testProcess");

    Task task = taskService.createTaskQuery().singleResult();

    SerializableVariable var = (SerializableVariable) taskService.getVariable(task.getId(), "variableName");
    assertNotNull(var);

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/examples/variables/VariablesTest.testBasicVariableOperations.bpmn20.xml"})
  public void testChangeToSerializableUsingApi() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariable", "test");
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("taskAssigneeProcess", variables);

    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery().variableName("aVariable");

    VariableInstance variable = query.singleResult();
    assertEquals(ValueType.STRING.getName(), variable.getTypeName());

    runtimeService.setVariable(processInstance.getId(), "aVariable", new SerializableVariable("foo"));
    variable = query.singleResult();
    assertEquals(ValueType.OBJECT.getName(), variable.getTypeName());

  }

  @Deployment
  public void testGetVariableInstancesFromVariableScope() {

    VariableMap variables = createVariables()
      .putValue("anIntegerVariable", 1234)
      .putValue("anObjectValue", objectValue(new SimpleSerializableBean(10)).serializationDataFormat(Variables.SerializationDataFormats.JAVA))
      .putValue("anUntypedObjectValue", new SimpleSerializableBean(30));

    runtimeService.startProcessInstanceByKey("testProcess", variables);

    // assertions are part of the java delegate AssertVariableInstancesDelegate
    // only there we can access the VariableScope methods
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/examples/variables/VariablesTest.testSetVariableInScope.bpmn20.xml")
  public void testSetVariableInScopeExplicitUpdate() {
    // when a process instance is started and the task after the subprocess reached
    runtimeService.startProcessInstanceByKey("testProcess",
        Collections.<String, Object>singletonMap("shouldExplicitlyUpdateVariable", true));

    // then there should be only the "shouldExplicitlyUpdateVariable" variable
    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery().singleResult();
    assertNotNull(variableInstance);
    assertEquals("shouldExplicitlyUpdateVariable", variableInstance.getName());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/examples/variables/VariablesTest.testSetVariableInScope.bpmn20.xml")
  public void testSetVariableInScopeImplicitUpdate() {
    // when a process instance is started and the task after the subprocess reached
    runtimeService.startProcessInstanceByKey("testProcess",
        Collections.<String, Object>singletonMap("shouldExplicitlyUpdateVariable", true));

    // then there should be only the "shouldExplicitlyUpdateVariable" variable
    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery().singleResult();
    assertNotNull(variableInstance);
    assertEquals("shouldExplicitlyUpdateVariable", variableInstance.getName());
  }

  @Deployment
  public void testUpdateVariableInProcessWithoutWaitstate() {
    // when a process instance is started
    runtimeService.startProcessInstanceByKey("oneScriptTaskProcess",
        Collections.<String, Object>singletonMap("var", new SimpleSerializableBean(10)));

    // then it should succeeds successfully
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNull(processInstance);
  }

  @Deployment
  public void testSetUpdateAndDeleteComplexVariable() {
    // when a process instance is started
    runtimeService.startProcessInstanceByKey("oneUserTaskProcess",
        Collections.<String, Object>singletonMap("var", new SimpleSerializableBean(10)));

    // then it should wait at the user task
    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertNotNull(processInstance);
  }

}
