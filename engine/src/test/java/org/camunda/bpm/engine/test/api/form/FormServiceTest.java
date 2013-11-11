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

package org.camunda.bpm.engine.test.api.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.form.FormProperty;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Joram Barrez
 * @author Frederik Heremans
 * @author Tom Baeyens
 * @author Falko Menge (camunda)
 */
public class FormServiceTest extends PluggableProcessEngineTestCase {

  @Deployment(resources = { "org/camunda/bpm/engine/test/examples/taskforms/VacationRequest_deprecated_forms.bpmn20.xml",
      "org/camunda/bpm/engine/test/examples/taskforms/approve.form",
      "org/camunda/bpm/engine/test/examples/taskforms/request.form",
      "org/camunda/bpm/engine/test/examples/taskforms/adjustRequest.form" })
  public void testGetStartFormByProcessDefinitionId() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertEquals(1, processDefinitions.size());
    ProcessDefinition processDefinition = processDefinitions.get(0);

    Object startForm = formService.getRenderedStartForm(processDefinition.getId(), "juel");
    assertNotNull(startForm);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  public void testGetStartFormByProcessDefinitionIdWithoutStartform() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertEquals(1, processDefinitions.size());
    ProcessDefinition processDefinition = processDefinitions.get(0);

    Object startForm = formService.getRenderedStartForm(processDefinition.getId());
    assertNull(startForm);
  }

  public void testGetStartFormByKeyNullKey() {
    try {
      formService.getRenderedStartForm(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      // Exception expected
    }
  }

  public void testGetStartFormByIdNullId() {
    try {
      formService.getRenderedStartForm(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      // Exception expected
    }
  }

  public void testGetStartFormByIdUnexistingProcessDefinitionId() {
    try {
      formService.getRenderedStartForm("unexistingId");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("no deployed process definition found with id", ae.getMessage());
    }
  }

  public void testGetTaskFormNullTaskId() {
    try {
      formService.getRenderedTaskForm(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      // Expected Exception
    }
  }

  public void testGetTaskFormUnexistingTaskId() {
    try {
      formService.getRenderedTaskForm("unexistingtask");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("Task 'unexistingtask' not found", ae.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/form/FormsProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/form/start.form",
      "org/camunda/bpm/engine/test/api/form/task.form" })
  public void testTaskFormPropertyDefaultsAndFormRendering() {
    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    StartFormData startForm = formService.getStartFormData(procDefId);
    assertNotNull(startForm);
    assertEquals(deploymentId, startForm.getDeploymentId());
    assertEquals("org/camunda/bpm/engine/test/api/form/start.form", startForm.getFormKey());
    assertEquals(new ArrayList<FormProperty>(), startForm.getFormProperties());
    assertEquals(procDefId, startForm.getProcessDefinition().getId());

    Object renderedStartForm = formService.getRenderedStartForm(procDefId, "juel");
    assertEquals("start form content", renderedStartForm);

    Map<String, String> properties = new HashMap<String, String>();
    properties.put("room", "5b");
    properties.put("speaker", "Mike");
    String processInstanceId = formService.submitStartFormData(procDefId, properties).getId();

    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("room", "5b");
    expectedVariables.put("speaker", "Mike");

    Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
    assertEquals(expectedVariables, variables);

    Task task = taskService.createTaskQuery().singleResult();
    String taskId = task.getId();
    TaskFormData taskForm = formService.getTaskFormData(taskId);
    assertEquals(deploymentId, taskForm.getDeploymentId());
    assertEquals("org/camunda/bpm/engine/test/api/form/task.form", taskForm.getFormKey());
    assertEquals(new ArrayList<FormProperty>(), taskForm.getFormProperties());
    assertEquals(taskId, taskForm.getTask().getId());

    assertEquals("Mike is speaking in room 5b", formService.getRenderedTaskForm(taskId, "juel"));

    properties = new HashMap<String, String>();
    properties.put("room", "3f");
    formService.submitTaskFormData(taskId, properties);

    expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("room", "3f");
    expectedVariables.put("speaker", "Mike");

    variables = runtimeService.getVariables(processInstanceId);
    assertEquals(expectedVariables, variables);
  }

  @Deployment
  public void testFormPropertyHandlingDeprecated() {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("room", "5b"); // default
    properties.put("speaker", "Mike"); // variable name mapping
    properties.put("duration", "45"); // type conversion
    properties.put("free", "true"); // type conversion

    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    String processInstanceId = formService.submitStartFormData(procDefId, properties).getId();

    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("room", "5b");
    expectedVariables.put("SpeakerName", "Mike");
    expectedVariables.put("duration", new Long(45));
    expectedVariables.put("free", Boolean.TRUE);

    Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
    assertEquals(expectedVariables, variables);

    Address address = new Address();
    address.setStreet("broadway");
    runtimeService.setVariable(processInstanceId, "address", address);

    String taskId = taskService.createTaskQuery().singleResult().getId();
    TaskFormData taskFormData = formService.getTaskFormData(taskId);

    List<FormProperty> formProperties = taskFormData.getFormProperties();
    FormProperty propertyRoom = formProperties.get(0);
    assertEquals("room", propertyRoom.getId());
    assertEquals("5b", propertyRoom.getValue());

    FormProperty propertyDuration = formProperties.get(1);
    assertEquals("duration", propertyDuration.getId());
    assertEquals("45", propertyDuration.getValue());

    FormProperty propertySpeaker = formProperties.get(2);
    assertEquals("speaker", propertySpeaker.getId());
    assertEquals("Mike", propertySpeaker.getValue());

    FormProperty propertyStreet = formProperties.get(3);
    assertEquals("street", propertyStreet.getId());
    assertEquals("broadway", propertyStreet.getValue());

    FormProperty propertyFree = formProperties.get(4);
    assertEquals("free", propertyFree.getId());
    assertEquals("true", propertyFree.getValue());

    assertEquals(5, formProperties.size());

    try {
      formService.submitTaskFormData(taskId, new HashMap<String, String>());
      fail("expected exception about required form property 'street'");
    } catch (ProcessEngineException e) {
      // OK
    }

    try {
      properties = new HashMap<String, String>();
      properties.put("speaker", "its not allowed to update speaker!");
      formService.submitTaskFormData(taskId, properties);
      fail("expected exception about a non writable form property 'speaker'");
    } catch (ProcessEngineException e) {
      // OK
    }

    properties = new HashMap<String, String>();
    properties.put("street", "rubensstraat");
    formService.submitTaskFormData(taskId, properties);

    expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("room", "5b");
    expectedVariables.put("SpeakerName", "Mike");
    expectedVariables.put("duration", new Long(45));
    expectedVariables.put("free", Boolean.TRUE);

    variables = runtimeService.getVariables(processInstanceId);
    address = (Address) variables.remove("address");
    assertEquals("rubensstraat", address.getStreet());
    assertEquals(expectedVariables, variables);
  }

  @Deployment
  public void testFormPropertyHandling() {
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("room", "5b"); // default
    properties.put("speaker", "Mike"); // variable name mapping
    properties.put("duration", 45L); // type conversion
    properties.put("free", "true"); // type conversion

    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    String processInstanceId = formService.submitStartForm(procDefId, properties).getId();

    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("room", "5b");
    expectedVariables.put("SpeakerName", "Mike");
    expectedVariables.put("duration", new Long(45));
    expectedVariables.put("free", Boolean.TRUE);

    Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
    assertEquals(expectedVariables, variables);

    Address address = new Address();
    address.setStreet("broadway");
    runtimeService.setVariable(processInstanceId, "address", address);

    String taskId = taskService.createTaskQuery().singleResult().getId();
    TaskFormData taskFormData = formService.getTaskFormData(taskId);

    List<FormProperty> formProperties = taskFormData.getFormProperties();
    FormProperty propertyRoom = formProperties.get(0);
    assertEquals("room", propertyRoom.getId());
    assertEquals("5b", propertyRoom.getValue());

    FormProperty propertyDuration = formProperties.get(1);
    assertEquals("duration", propertyDuration.getId());
    assertEquals("45", propertyDuration.getValue());

    FormProperty propertySpeaker = formProperties.get(2);
    assertEquals("speaker", propertySpeaker.getId());
    assertEquals("Mike", propertySpeaker.getValue());

    FormProperty propertyStreet = formProperties.get(3);
    assertEquals("street", propertyStreet.getId());
    assertEquals("broadway", propertyStreet.getValue());

    FormProperty propertyFree = formProperties.get(4);
    assertEquals("free", propertyFree.getId());
    assertEquals("true", propertyFree.getValue());

    assertEquals(5, formProperties.size());

    try {
      formService.submitTaskForm(taskId, new HashMap<String, Object>());
      fail("expected exception about required form property 'street'");
    } catch (ProcessEngineException e) {
      // OK
    }

    try {
      properties = new HashMap<String, Object>();
      properties.put("speaker", "its not allowed to update speaker!");
      formService.submitTaskForm(taskId, properties);
      fail("expected exception about a non writable form property 'speaker'");
    } catch (ProcessEngineException e) {
      // OK
    }

    properties = new HashMap<String, Object>();
    properties.put("street", "rubensstraat");
    formService.submitTaskForm(taskId, properties);

    expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("room", "5b");
    expectedVariables.put("SpeakerName", "Mike");
    expectedVariables.put("duration", new Long(45));
    expectedVariables.put("free", Boolean.TRUE);

    variables = runtimeService.getVariables(processInstanceId);
    address = (Address) variables.remove("address");
    assertEquals("rubensstraat", address.getStreet());
    assertEquals(expectedVariables, variables);
  }

  @SuppressWarnings("unchecked")
  @Deployment
  public void testFormPropertyDetails() {
    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    StartFormData startFormData = formService.getStartFormData(procDefId);
    FormProperty property = startFormData.getFormProperties().get(0);
    assertEquals("speaker", property.getId());
    assertNull(property.getValue());
    assertTrue(property.isReadable());
    assertTrue(property.isWritable());
    assertFalse(property.isRequired());
    assertEquals("string", property.getType().getName());

    property = startFormData.getFormProperties().get(1);
    assertEquals("start", property.getId());
    assertNull(property.getValue());
    assertTrue(property.isReadable());
    assertTrue(property.isWritable());
    assertFalse(property.isRequired());
    assertEquals("date", property.getType().getName());
    assertEquals("dd-MMM-yyyy", property.getType().getInformation("datePattern"));

    property = startFormData.getFormProperties().get(2);
    assertEquals("direction", property.getId());
    assertNull(property.getValue());
    assertTrue(property.isReadable());
    assertTrue(property.isWritable());
    assertFalse(property.isRequired());
    assertEquals("enum", property.getType().getName());
    Map<String, String> values = (Map<String, String>) property.getType().getInformation("values");

    Map<String, String> expectedValues = new LinkedHashMap<String, String>();
    expectedValues.put("left", "Go Left");
    expectedValues.put("right", "Go Right");
    expectedValues.put("up", "Go Up");
    expectedValues.put("down", "Go Down");

    // ACT-1023: check if ordering is retained
    Iterator<Entry<String, String>> expectedValuesIterator = expectedValues.entrySet().iterator();
    for(Entry<String, String> entry : values.entrySet()) {
      Entry<String, String> expectedEntryAtLocation = expectedValuesIterator.next();
      assertEquals(expectedEntryAtLocation.getKey(), entry.getKey());
      assertEquals(expectedEntryAtLocation.getValue(), entry.getValue());
    }
    assertEquals(expectedValues, values);
  }

  @Deployment
  public void testInvalidFormKeyReference() {
    try {
      formService.getRenderedStartForm(repositoryService.createProcessDefinitionQuery().singleResult().getId(), "juel");
      fail();
    } catch (ProcessEngineException e) {
      assertTextPresent("Form with formKey 'IDoNotExist' does not exist", e.getMessage());
    }
  }

  @Deployment
  public void testSubmitStartFormDataWithBusinessKey() {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put("duration", "45");
    properties.put("speaker", "Mike");
    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    ProcessInstance processInstance = formService.submitStartFormData(procDefId, "123", properties);
    assertEquals("123", processInstance.getBusinessKey());

    assertEquals(processInstance.getId(), runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("123").singleResult().getId());
  }

  @Deployment
  public void testSubmitStartFormWithBusinessKey() {
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("duration", 45L);
    properties.put("speaker", "Mike");
    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    ProcessInstance processInstance = formService.submitStartForm(procDefId, "123", properties);
    assertEquals("123", processInstance.getBusinessKey());

    assertEquals(processInstance.getId(), runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("123").singleResult().getId());
    Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
    assertEquals("Mike", variables.get("SpeakerName"));
    assertEquals(45L, variables.get("duration"));
  }

  @Deployment
  public void testSubmitStartFormWithoutProperties() {
    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("duration", 45L);
    properties.put("speaker", "Mike");
    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    ProcessInstance processInstance = formService.submitStartForm(procDefId, "123", properties);
    assertEquals("123", processInstance.getBusinessKey());

    assertEquals(processInstance.getId(), runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("123").singleResult().getId());
    Map<String, Object> variables = runtimeService.getVariables(processInstance.getId());
    assertEquals("Mike", variables.get("speaker"));
    assertEquals(45L, variables.get("duration"));
  }

  public void testGetStartFormKeyEmptyArgument() {
    try {
      formService.getStartFormKey(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("The process definition id is mandatory, but 'null' has been provided.", ae.getMessage());
    }

    try {
      formService.getStartFormKey("");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("The process definition id is mandatory, but '' has been provided.", ae.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/form/FormsProcess.bpmn20.xml")
  public void testGetStartFormKey() {
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    String expectedFormKey = formService.getStartFormData(processDefinitionId).getFormKey();
    String actualFormKey = formService.getStartFormKey(processDefinitionId);
    assertEquals(expectedFormKey, actualFormKey);
  }

  public void testGetTaskFormKeyEmptyArguments() {
    try {
      formService.getTaskFormKey(null, "23");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("The process definition id is mandatory, but 'null' has been provided.", ae.getMessage());
    }

    try {
      formService.getTaskFormKey("", "23");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("The process definition id is mandatory, but '' has been provided.", ae.getMessage());
    }

    try {
      formService.getTaskFormKey("42", null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("The task definition key is mandatory, but 'null' has been provided.", ae.getMessage());
    }

    try {
      formService.getTaskFormKey("42", "");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      assertTextPresent("The task definition key is mandatory, but '' has been provided.", ae.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/form/FormsProcess.bpmn20.xml")
  public void testGetTaskFormKey() {
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    runtimeService.startProcessInstanceById(processDefinitionId);
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    String expectedFormKey = formService.getTaskFormData(task.getId()).getFormKey();
    String actualFormKey = formService.getTaskFormKey(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
    assertEquals(expectedFormKey, actualFormKey);
  }

  @Deployment
  public void testGetTaskFormKeyWithExpression() {
    runtimeService.startProcessInstanceByKey("FormsProcess", CollectionUtil.singletonMap("dynamicKey", "test"));
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("test", formService.getTaskFormData(task.getId()).getFormKey());
  }

}
