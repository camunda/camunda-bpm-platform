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
package org.camunda.bpm.engine.test.api.form;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.camunda.bpm.engine.test.util.CamundaFormUtils.findAllCamundaFormDefinitionEntities;
import static org.camunda.bpm.engine.variable.Variables.booleanValue;
import static org.camunda.bpm.engine.variable.Variables.createVariables;
import static org.camunda.bpm.engine.variable.Variables.objectValue;
import static org.camunda.bpm.engine.variable.Variables.serializedObjectValue;
import static org.camunda.bpm.engine.variable.Variables.stringValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.form.FormProperty;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.form.type.AbstractFormFieldType;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.util.CollectionUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.form.deployment.FindCamundaFormDefinitionsCmd;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.commons.utils.IoUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Joram Barrez
 * @author Frederik Heremans
 * @author Tom Baeyens
 * @author Falko Menge (camunda)
 */
public class FormServiceTest {

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration ->
      configuration.setJavaSerializationFormatEnabled(true));
  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  private RuntimeService runtimeService;
  private TaskService taskService;
  private RepositoryService repositoryService;
  private HistoryService historyService;
  private IdentityService identityService;
  private FormService formService;
  private CaseService caseService;
  private ProcessEngineConfigurationImpl processEngineConfiguration;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
    repositoryService = engineRule.getRepositoryService();
    historyService = engineRule.getHistoryService();
    formService = engineRule.getFormService();
    caseService = engineRule.getCaseService();
    identityService = engineRule.getIdentityService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    identityService.saveUser(identityService.newUser("fozzie"));
    identityService.saveGroup(identityService.newGroup("management"));
    identityService.createMembership("fozzie", "management");
  }

  @After
  public void tearDown() throws Exception {
    identityService.deleteGroup("management");
    identityService.deleteUser("fozzie");

    VariablesRecordingListener.reset();
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/form/util/VacationRequest_deprecated_forms.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/form/util/approve.html",
      "org/camunda/bpm/engine/test/api/form/util/request.html",
      "org/camunda/bpm/engine/test/api/form/util/adjustRequest.html" })
  @Test
  public void testGetStartFormByProcessDefinitionId() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertEquals(1, processDefinitions.size());
    ProcessDefinition processDefinition = processDefinitions.get(0);

    Object startForm = formService.getRenderedStartForm(processDefinition.getId(), "juel");
    assertNotNull(startForm);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  @Test
  public void testGetStartFormByProcessDefinitionIdWithoutStartform() {
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().list();
    assertEquals(1, processDefinitions.size());
    ProcessDefinition processDefinition = processDefinitions.get(0);

    Object startForm = formService.getRenderedStartForm(processDefinition.getId());
    assertNull(startForm);
  }

  @Test
  public void testGetStartFormByKeyNullKey() {
    try {
      formService.getRenderedStartForm(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      // Exception expected
    }
  }

  @Test
  public void testGetStartFormByIdNullId() {
    try {
      formService.getRenderedStartForm(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      // Exception expected
    }
  }

  @Test
  public void testGetStartFormByIdUnexistingProcessDefinitionId() {
    try {
      formService.getRenderedStartForm("unexistingId");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("no deployed process definition found with id", ae.getMessage());
    }
  }

  @Test
  public void testGetTaskFormNullTaskId() {
    try {
      formService.getRenderedTaskForm(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      // Expected Exception
    }
  }

  @Test
  public void testGetTaskFormUnexistingTaskId() {
    try {
      formService.getRenderedTaskForm("unexistingtask");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("Task 'unexistingtask' not found", ae.getMessage());
    }
  }

  @Test
  public void testTaskFormPropertyDefaultsAndFormRendering() {

    final String deploymentId = testRule.deploy("org/camunda/bpm/engine/test/api/form/FormsProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/form/start.html",
      "org/camunda/bpm/engine/test/api/form/task.html")
      .getId();

    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    StartFormData startForm = formService.getStartFormData(procDefId);
    assertNotNull(startForm);
    assertEquals(deploymentId, startForm.getDeploymentId());
    assertEquals("org/camunda/bpm/engine/test/api/form/start.html", startForm.getFormKey());
    assertEquals(new ArrayList<FormProperty>(), startForm.getFormProperties());
    assertEquals(procDefId, startForm.getProcessDefinition().getId());

    Object renderedStartForm = formService.getRenderedStartForm(procDefId, "juel");
    assertEquals("start form content", renderedStartForm);

    Map<String, String> properties = new HashMap<>();
    properties.put("room", "5b");
    properties.put("speaker", "Mike");
    String processInstanceId = formService.submitStartFormData(procDefId, properties).getId();

    Map<String, Object> expectedVariables = new HashMap<>();
    expectedVariables.put("room", "5b");
    expectedVariables.put("speaker", "Mike");

    Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
    assertEquals(expectedVariables, variables);

    Task task = taskService.createTaskQuery().singleResult();
    String taskId = task.getId();
    TaskFormData taskForm = formService.getTaskFormData(taskId);
    assertEquals(deploymentId, taskForm.getDeploymentId());
    assertEquals("org/camunda/bpm/engine/test/api/form/task.html", taskForm.getFormKey());
    assertEquals(new ArrayList<FormProperty>(), taskForm.getFormProperties());
    assertEquals(taskId, taskForm.getTask().getId());

    assertEquals("Mike is speaking in room 5b", formService.getRenderedTaskForm(taskId, "juel"));

    properties = new HashMap<>();
    properties.put("room", "3f");
    formService.submitTaskFormData(taskId, properties);

    expectedVariables = new HashMap<>();
    expectedVariables.put("room", "3f");
    expectedVariables.put("speaker", "Mike");

    variables = runtimeService.getVariables(processInstanceId);
    assertEquals(expectedVariables, variables);
  }

  @Deployment
  @Test
  public void testFormPropertyHandlingDeprecated() {
    Map<String, String> properties = new HashMap<>();
    properties.put("room", "5b"); // default
    properties.put("speaker", "Mike"); // variable name mapping
    properties.put("duration", "45"); // type conversion
    properties.put("free", "true"); // type conversion

    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    String processInstanceId = formService.submitStartFormData(procDefId, properties).getId();

    Map<String, Object> expectedVariables = new HashMap<>();
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
      properties = new HashMap<>();
      properties.put("speaker", "its not allowed to update speaker!");
      formService.submitTaskFormData(taskId, properties);
      fail("expected exception about a non writable form property 'speaker'");
    } catch (ProcessEngineException e) {
      // OK
    }

    properties = new HashMap<>();
    properties.put("street", "rubensstraat");
    formService.submitTaskFormData(taskId, properties);

    expectedVariables = new HashMap<>();
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
  @Test
  public void testFormPropertyHandling() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("room", "5b"); // default
    properties.put("speaker", "Mike"); // variable name mapping
    properties.put("duration", 45L); // type conversion
    properties.put("free", "true"); // type conversion

    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    String processInstanceId = formService.submitStartForm(procDefId, properties).getId();

    Map<String, Object> expectedVariables = new HashMap<>();
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
      properties = new HashMap<>();
      properties.put("speaker", "its not allowed to update speaker!");
      formService.submitTaskForm(taskId, properties);
      fail("expected exception about a non writable form property 'speaker'");
    } catch (ProcessEngineException e) {
      // OK
    }

    properties = new HashMap<>();
    properties.put("street", "rubensstraat");
    formService.submitTaskForm(taskId, properties);

    expectedVariables = new HashMap<>();
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
  @Test
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

    Map<String, String> expectedValues = new LinkedHashMap<>();
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
  @Test
  public void testInvalidFormKeyReference() {
    try {
      formService.getRenderedStartForm(repositoryService.createProcessDefinitionQuery().singleResult().getId(), "juel");
      fail();
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("Form with formKey 'IDoNotExist' does not exist", e.getMessage());
    }
  }

  @Deployment
  @Test
  public void testSubmitStartFormDataWithBusinessKey() {
    Map<String, String> properties = new HashMap<>();
    properties.put("duration", "45");
    properties.put("speaker", "Mike");
    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    ProcessInstance processInstance = formService.submitStartFormData(procDefId, "123", properties);
    assertEquals("123", processInstance.getBusinessKey());

    assertEquals(processInstance.getId(), runtimeService.createProcessInstanceQuery().processInstanceBusinessKey("123").singleResult().getId());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/form/FormsProcess.bpmn20.xml"})
  @Test
  public void testSubmitStartFormDataTypedVariables() {
    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    String stringValue = "some string";
    String serializedValue = "some value";

    ProcessInstance processInstance = formService.submitStartForm(procDefId,
        createVariables()
          .putValueTyped("boolean", booleanValue(null))
          .putValueTyped("string", stringValue(stringValue))
          .putValueTyped("serializedObject", serializedObjectValue(serializedValue)
              .objectTypeName(String.class.getName())
              .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
              .create())
          .putValueTyped("object", objectValue(serializedValue).create()));

    VariableMap variables = runtimeService.getVariablesTyped(processInstance.getId(), false);
    assertEquals(booleanValue(null), variables.getValueTyped("boolean"));
    assertEquals(stringValue(stringValue), variables.getValueTyped("string"));
    assertNotNull(variables.<ObjectValue>getValueTyped("serializedObject").getValueSerialized());
    assertNotNull(variables.<ObjectValue>getValueTyped("object").getValueSerialized());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/form/FormsProcess.bpmn20.xml"})
  @Test
  public void testSubmitTaskFormDataTypedVariables() {
    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    ProcessInstance processInstance = formService.submitStartForm(procDefId, createVariables());

    Task task = taskService.createTaskQuery().singleResult();

    String stringValue = "some string";
    String serializedValue = "some value";

    formService.submitTaskForm(task.getId(), createVariables()
        .putValueTyped("boolean", booleanValue(null))
        .putValueTyped("string", stringValue(stringValue))
        .putValueTyped("serializedObject", serializedObjectValue(serializedValue)
            .objectTypeName(String.class.getName())
            .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
            .create())
        .putValueTyped("object", objectValue(serializedValue).create()));

    VariableMap variables = runtimeService.getVariablesTyped(processInstance.getId(), false);
    assertEquals(booleanValue(null), variables.getValueTyped("boolean"));
    assertEquals(stringValue(stringValue), variables.getValueTyped("string"));
    assertNotNull(variables.<ObjectValue>getValueTyped("serializedObject").getValueSerialized());
    assertNotNull(variables.<ObjectValue>getValueTyped("object").getValueSerialized());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/api/form/FormsProcess.bpmn20.xml"})
  @Test
  public void testSubmitFormVariablesNull() {
    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    // assert that I can submit the start form with variables null
    formService.submitStartForm(procDefId, null);

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    // assert that I can submit the task form with variables null
    formService.submitTaskForm(task.getId(), null);
  }

  @Test
  public void testSubmitTaskFormForStandaloneTask() {
    // given
    String id = "standaloneTask";
    Task task = taskService.newTask(id);
    taskService.saveTask(task);

    // when
    formService.submitTaskForm(task.getId(), Variables.createVariables().putValue("foo", "bar"));


    if (processEngineConfiguration.getHistoryLevel().getId() >= HistoryLevel.HISTORY_LEVEL_AUDIT.getId()) {
      HistoricVariableInstance variableInstance = historyService
        .createHistoricVariableInstanceQuery()
        .taskIdIn(id)
        .singleResult();

      assertNotNull(variableInstance);
      assertEquals("foo", variableInstance.getName());
      assertEquals("bar", variableInstance.getValue());
    }

    taskService.deleteTask(id, true);
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn"})
  @Test
  public void testSubmitTaskFormForCmmnHumanTask() {
    caseService.createCaseInstanceByKey("oneTaskCase");

    Task task = taskService.createTaskQuery().singleResult();

    String stringValue = "some string";
    String serializedValue = "some value";

    formService.submitTaskForm(task.getId(), createVariables()
        .putValueTyped("boolean", booleanValue(null))
        .putValueTyped("string", stringValue(stringValue))
        .putValueTyped("serializedObject", serializedObjectValue(serializedValue)
            .objectTypeName(String.class.getName())
            .serializationDataFormat(Variables.SerializationDataFormats.JAVA)
            .create())
        .putValueTyped("object", objectValue(serializedValue).create()));
  }


  @Deployment
  @Test
  public void testSubmitStartFormWithBusinessKey() {
    Map<String, Object> properties = new HashMap<>();
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
  @Test
  public void testSubmitStartFormWithoutProperties() {
    Map<String, Object> properties = new HashMap<>();
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

  @Test
  public void testSubmitStartFormWithExecutionListenerOnStartEvent() {
    // given
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess()
      .startEvent()
        .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, VariablesRecordingListener.class)
        .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, VariablesRecordingListener.class)
      .endEvent()
      .done();

    testRule.deploy(modelInstance);
    ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().singleResult();

    VariableMap formData = Variables.createVariables().putValue("foo", "bar");

    // when
    formService.submitStartForm(procDef.getId(), formData);

    // then
    List<VariableMap> variableEvents = VariablesRecordingListener.getVariableEvents();
    assertThat(variableEvents).hasSize(2);
    assertThat(variableEvents.get(0)).containsExactly(entry("foo", "bar"));
    assertThat(variableEvents.get(1)).containsExactly(entry("foo", "bar"));
  }


  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testSubmitStartFormWithAsyncStartEvent() {
    // given
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess()
      .startEvent().camundaAsyncBefore()
      .endEvent()
      .done();

    testRule.deploy(modelInstance);
    ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().singleResult();

    VariableMap formData = Variables.createVariables().putValue("foo", "bar");

    // when
    ProcessInstance processInstance = formService.submitStartForm(procDef.getId(), formData);

    // then
    VariableMap runtimeVariables = runtimeService.getVariablesTyped(processInstance.getId());
    assertThat(runtimeVariables).containsExactly(entry("foo", "bar"));

    HistoricVariableInstance historicVariable = historyService.createHistoricVariableInstanceQuery().singleResult();
    assertThat(historicVariable).isNotNull();
    assertThat(historicVariable.getName()).isEqualTo("foo");
    assertThat(historicVariable.getValue()).isEqualTo("bar");
  }


  @Test
  public void testSubmitStartFormWithAsyncStartEventExecuteJob() {
    // given
    BpmnModelInstance modelInstance = Bpmn.createExecutableProcess()
      .startEvent().camundaAsyncBefore()
      .userTask()
      .endEvent()
      .done();

    testRule.deploy(modelInstance);
    ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().singleResult();

    VariableMap formData = Variables.createVariables().putValue("foo", "bar");
    ProcessInstance processInstance = formService.submitStartForm(procDef.getId(), formData);

    ManagementService managementService = engineRule.getManagementService();
    Job job = managementService.createJobQuery().singleResult();

    // when
    managementService.executeJob(job.getId());

    // then the job can be executed successfully (e.g. we don't try to insert the variables a second time)
    // and
    VariableMap runtimeVariables = runtimeService.getVariablesTyped(processInstance.getId());
    assertThat(runtimeVariables).containsExactly(entry("foo", "bar"));
  }

  public static class VariablesRecordingListener implements ExecutionListener {

    private static List<VariableMap> variableEvents = new ArrayList<>();

    public static void reset() {
      variableEvents.clear();
    }

    public static List<VariableMap> getVariableEvents() {
      return variableEvents;
    }

    @Override
    public void notify(DelegateExecution execution) throws Exception {
      variableEvents.add(execution.getVariablesTyped());
    }
  }

  @Test
  public void testGetStartFormKeyEmptyArgument() {
    try {
      formService.getStartFormKey(null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("The process definition id is mandatory, but 'null' has been provided.", ae.getMessage());
    }

    try {
      formService.getStartFormKey("");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("The process definition id is mandatory, but '' has been provided.", ae.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/form/FormsProcess.bpmn20.xml")
  @Test
  public void testGetStartFormKey() {
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    String expectedFormKey = formService.getStartFormData(processDefinitionId).getFormKey();
    String actualFormKey = formService.getStartFormKey(processDefinitionId);
    assertEquals(expectedFormKey, actualFormKey);
  }

  @Test
  public void testGetTaskFormKeyEmptyArguments() {
    try {
      formService.getTaskFormKey(null, "23");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("The process definition id is mandatory, but 'null' has been provided.", ae.getMessage());
    }

    try {
      formService.getTaskFormKey("", "23");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("The process definition id is mandatory, but '' has been provided.", ae.getMessage());
    }

    try {
      formService.getTaskFormKey("42", null);
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("The task definition key is mandatory, but 'null' has been provided.", ae.getMessage());
    }

    try {
      formService.getTaskFormKey("42", "");
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException ae) {
      testRule.assertTextPresent("The task definition key is mandatory, but '' has been provided.", ae.getMessage());
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/api/form/FormsProcess.bpmn20.xml")
  @Test
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
  @Test
  public void testGetTaskFormKeyWithExpression() {
    runtimeService.startProcessInstanceByKey("FormsProcess", CollectionUtil.singletonMap("dynamicKey", "test"));
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);
    assertEquals("test", formService.getTaskFormData(task.getId()).getFormKey());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/form/FormServiceTest.startFormFields.bpmn20.xml"})
  @Test
  public void testGetStartFormVariables() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    VariableMap variables = formService.getStartFormVariables(processDefinition.getId());
    assertEquals(4, variables.size());

    assertEquals("someString", variables.get("stringField"));
    assertEquals("someString", variables.getValueTyped("stringField").getValue());
    assertEquals(ValueType.STRING, variables.getValueTyped("stringField").getType());

    assertEquals(5l, variables.get("longField"));
    assertEquals(5l, variables.getValueTyped("longField").getValue());
    assertEquals(ValueType.LONG, variables.getValueTyped("longField").getType());

    assertNull(variables.get("customField"));
    assertNull(variables.getValueTyped("customField").getValue());
    assertEquals(ValueType.STRING, variables.getValueTyped("customField").getType());

    assertNotNull(variables.get("dateField"));
    assertEquals(variables.get("dateField"), variables.getValueTyped("dateField").getValue());
    assertEquals(ValueType.STRING, variables.getValueTyped("dateField").getType());

    AbstractFormFieldType dateFormType = processEngineConfiguration.getFormTypes().getFormType("date");
    Date dateValue = (Date) dateFormType.convertToModelValue(variables.getValueTyped("dateField")).getValue();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(dateValue);
    assertEquals(10, calendar.get(Calendar.DAY_OF_MONTH));
    assertEquals(Calendar.JANUARY, calendar.get(Calendar.MONTH));
    assertEquals(2013, calendar.get(Calendar.YEAR));

    // get restricted set of variables:
    variables = formService.getStartFormVariables(processDefinition.getId(), Arrays.asList("stringField"), true);
    assertEquals(1, variables.size());
    assertEquals("someString", variables.get("stringField"));
    assertEquals("someString", variables.getValueTyped("stringField").getValue());
    assertEquals(ValueType.STRING, variables.getValueTyped("stringField").getType());

    // request non-existing variable
    variables = formService.getStartFormVariables(processDefinition.getId(), Arrays.asList("non-existing!"), true);
    assertEquals(0, variables.size());

    // null => all
    variables = formService.getStartFormVariables(processDefinition.getId(), null, true);
    assertEquals(4, variables.size());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/form/FormServiceTest.startFormFieldsUnknownType.bpmn20.xml"})
  @Test
  public void testGetStartFormVariablesEnumType() {

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    VariableMap startFormVariables = formService.getStartFormVariables(processDefinition.getId());
    assertEquals("a", startFormVariables.get("enumField"));
    assertEquals(ValueType.STRING, startFormVariables.getValueTyped("enumField").getType());
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/api/form/FormServiceTest.taskFormFields.bpmn20.xml"})
  @Test
  public void testGetTaskFormVariables() {

    Map<String, Object> processVars = new HashMap<>();
    processVars.put("someString", "initialValue");
    processVars.put("initialBooleanVariable", true);
    processVars.put("initialLongVariable", 1l);
    processVars.put("serializable", Arrays.asList("a", "b", "c"));

    runtimeService.startProcessInstanceByKey("testProcess", processVars);

    Task task = taskService.createTaskQuery().singleResult();
    VariableMap variables = formService.getTaskFormVariables(task.getId());
    assertEquals(7, variables.size());

    assertEquals("someString", variables.get("stringField"));
    assertEquals("someString", variables.getValueTyped("stringField").getValue());
    assertEquals(ValueType.STRING, variables.getValueTyped("stringField").getType());

    assertEquals(5l, variables.get("longField"));
    assertEquals(5l, variables.getValueTyped("longField").getValue());
    assertEquals(ValueType.LONG, variables.getValueTyped("longField").getType());

    assertNull(variables.get("customField"));
    assertNull(variables.getValueTyped("customField").getValue());
    assertEquals(ValueType.STRING, variables.getValueTyped("customField").getType());

    assertEquals("initialValue", variables.get("someString"));
    assertEquals("initialValue", variables.getValueTyped("someString").getValue());
    assertEquals(ValueType.STRING, variables.getValueTyped("someString").getType());

    assertEquals(true, variables.get("initialBooleanVariable"));
    assertEquals(true, variables.getValueTyped("initialBooleanVariable").getValue());
    assertEquals(ValueType.BOOLEAN, variables.getValueTyped("initialBooleanVariable").getType());

    assertEquals(1l, variables.get("initialLongVariable"));
    assertEquals(1l, variables.getValueTyped("initialLongVariable").getValue());
    assertEquals(ValueType.LONG, variables.getValueTyped("initialLongVariable").getType());

    assertNotNull(variables.get("serializable"));

    // override the long variable
    taskService.setVariableLocal(task.getId(), "initialLongVariable", 2l);

    variables = formService.getTaskFormVariables(task.getId());
    assertEquals(7, variables.size());

    assertEquals(2l, variables.get("initialLongVariable"));
    assertEquals(2l, variables.getValueTyped("initialLongVariable").getValue());
    assertEquals(ValueType.LONG, variables.getValueTyped("initialLongVariable").getType());

    // get restricted set of variables (form field):
    variables = formService.getTaskFormVariables(task.getId(), Arrays.asList("someString"), true);
    assertEquals(1, variables.size());
    assertEquals("initialValue", variables.get("someString"));
    assertEquals("initialValue", variables.getValueTyped("someString").getValue());
    assertEquals(ValueType.STRING, variables.getValueTyped("someString").getType());

    // get restricted set of variables (process variable):
    variables = formService.getTaskFormVariables(task.getId(), Arrays.asList("initialBooleanVariable"), true);
    assertEquals(1, variables.size());
    assertEquals(true, variables.get("initialBooleanVariable"));
    assertEquals(true, variables.getValueTyped("initialBooleanVariable").getValue());
    assertEquals(ValueType.BOOLEAN, variables.getValueTyped("initialBooleanVariable").getType());

    // request non-existing variable
    variables = formService.getTaskFormVariables(task.getId(), Arrays.asList("non-existing!"), true);
    assertEquals(0, variables.size());

    // null => all
    variables = formService.getTaskFormVariables(task.getId(), null, true);
    assertEquals(7, variables.size());

  }

  @Test
  public void testGetTaskFormVariables_StandaloneTask() {

    Map<String, Object> processVars = new HashMap<>();
    processVars.put("someString", "initialValue");
    processVars.put("initialBooleanVariable", true);
    processVars.put("initialLongVariable", 1l);
    processVars.put("serializable", Arrays.asList("a", "b", "c"));

    // create new standalone task
    Task standaloneTask = taskService.newTask();
    standaloneTask.setName("A Standalone Task");
    taskService.saveTask(standaloneTask);

    Task task = taskService.createTaskQuery().singleResult();

    // set variables
    taskService.setVariables(task.getId(), processVars);

    VariableMap variables = formService.getTaskFormVariables(task.getId());
    assertEquals(4, variables.size());

    assertEquals("initialValue", variables.get("someString"));
    assertEquals("initialValue", variables.getValueTyped("someString").getValue());
    assertEquals(ValueType.STRING, variables.getValueTyped("someString").getType());

    assertEquals(true, variables.get("initialBooleanVariable"));
    assertEquals(true, variables.getValueTyped("initialBooleanVariable").getValue());
    assertEquals(ValueType.BOOLEAN, variables.getValueTyped("initialBooleanVariable").getType());

    assertEquals(1l, variables.get("initialLongVariable"));
    assertEquals(1l, variables.getValueTyped("initialLongVariable").getValue());
    assertEquals(ValueType.LONG, variables.getValueTyped("initialLongVariable").getType());

    assertNotNull(variables.get("serializable"));

    // override the long variable
    taskService.setVariable(task.getId(), "initialLongVariable", 2l);

    variables = formService.getTaskFormVariables(task.getId());
    assertEquals(4, variables.size());

    assertEquals(2l, variables.get("initialLongVariable"));
    assertEquals(2l, variables.getValueTyped("initialLongVariable").getValue());
    assertEquals(ValueType.LONG, variables.getValueTyped("initialLongVariable").getType());

    // get restricted set of variables
    variables = formService.getTaskFormVariables(task.getId(), Arrays.asList("someString"), true);
    assertEquals(1, variables.size());
    assertEquals("initialValue", variables.get("someString"));
    assertEquals("initialValue", variables.getValueTyped("someString").getValue());
    assertEquals(ValueType.STRING, variables.getValueTyped("someString").getType());

    // request non-existing variable
    variables = formService.getTaskFormVariables(task.getId(), Arrays.asList("non-existing!"), true);
    assertEquals(0, variables.size());

    // null => all
    variables = formService.getTaskFormVariables(task.getId(), null, true);
    assertEquals(4, variables.size());

    // Finally, delete task
    taskService.deleteTask(task.getId(), true);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml" })
  @Test
  public void testSubmitStartFormWithObjectVariables() {
    // given
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    // when a start form is submitted with an object variable
    Map<String, Object> variables = new HashMap<>();
    variables.put("var", new ArrayList<String>());
    ProcessInstance processInstance = formService.submitStartForm(processDefinition.getId(), variables);

    // then the variable is available as a process variable
    ArrayList<String> var = (ArrayList<String>) runtimeService.getVariable(processInstance.getId(), "var");
    assertNotNull(var);
    assertTrue(var.isEmpty());

    // then no historic form property event has been written since this is not supported for custom objects
    if(processEngineConfiguration.getHistoryLevel().getId() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_FULL) {
      assertEquals(0, historyService.createHistoricDetailQuery().formFields().count());
    }

  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml" })
  @Test
  public void testSubmitTaskFormWithObjectVariables() {
    // given
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("twoTasksProcess");

    // when a task form is submitted with an object variable
    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    Map<String, Object> variables = new HashMap<>();
    variables.put("var", new ArrayList<String>());
    formService.submitTaskForm(task.getId(), variables);

    // then the variable is available as a process variable
    ArrayList<String> var = (ArrayList<String>) runtimeService.getVariable(processInstance.getId(), "var");
    assertNotNull(var);
    assertTrue(var.isEmpty());

    // then no historic form property event has been written since this is not supported for custom objects
    if(processEngineConfiguration.getHistoryLevel().getId() >= ProcessEngineConfigurationImpl.HISTORYLEVEL_FULL) {
      assertEquals(0, historyService.createHistoricDetailQuery().formFields().count());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/task/TaskServiceTest.testCompleteTaskWithVariablesInReturn.bpmn20.xml" })
  @Test
  public void testSubmitTaskFormWithVariablesInReturn() {
    String processVarName = "processVar";
    String processVarValue = "processVarValue";

    String taskVarName = "taskVar";
    String taskVarValue = "taskVarValue";

    Map<String, Object> variables = new HashMap<>();
    variables.put(processVarName, processVarValue);

    runtimeService.startProcessInstanceByKey("TaskServiceTest.testCompleteTaskWithVariablesInReturn", variables);

    Task firstUserTask = taskService.createTaskQuery().taskName("First User Task").singleResult();
    taskService.setVariable(firstUserTask.getId(), "x", 1);

    Map<String, Object> additionalVariables = new HashMap<>();
    additionalVariables.put(taskVarName, taskVarValue);

    // After completion of firstUserTask a script Task sets 'x' = 5
    VariableMap vars = formService.submitTaskFormWithVariablesInReturn(firstUserTask.getId(), additionalVariables, true);
    assertEquals(3, vars.size());
    assertEquals(5, vars.get("x"));
    assertEquals(ValueType.INTEGER, vars.getValueTyped("x").getType());
    assertEquals(processVarValue, vars.get(processVarName));
    assertEquals(ValueType.STRING, vars.getValueTyped(processVarName).getType());
    assertEquals(taskVarValue, vars.get(taskVarName));

    additionalVariables = new HashMap<>();
    additionalVariables.put("x", 7);
    Task secondUserTask = taskService.createTaskQuery().taskName("Second User Task").singleResult();
    vars = formService.submitTaskFormWithVariablesInReturn(secondUserTask.getId(), additionalVariables, true);
    assertEquals(3, vars.size());
    assertEquals(7, vars.get("x"));
    assertEquals(processVarValue, vars.get(processVarName));
    assertEquals(taskVarValue, vars.get(taskVarName));
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/twoParallelTasksProcess.bpmn20.xml" })
  @Test
  public void testSubmitTaskFormWithVariablesInReturnParallel() {
    String processVarName = "processVar";
    String processVarValue = "processVarValue";

    String task1VarName = "taskVar1";
    String task2VarName = "taskVar2";
    String task1VarValue = "taskVarValue1";
    String task2VarValue = "taskVarValue2";

    String additionalVar = "additionalVar";
    String additionalVarValue = "additionalVarValue";

    Map<String, Object> variables = new HashMap<>();
    variables.put(processVarName, processVarValue);
    runtimeService.startProcessInstanceByKey("twoParallelTasksProcess", variables);

    Task firstTask = taskService.createTaskQuery().taskName("First Task").singleResult();
    taskService.setVariable(firstTask.getId(), task1VarName, task1VarValue);
    Task secondTask = taskService.createTaskQuery().taskName("Second Task").singleResult();
    taskService.setVariable(secondTask.getId(), task2VarName, task2VarValue);

    Map<String, Object> vars = formService.submitTaskFormWithVariablesInReturn(firstTask.getId(), null, true);

    assertEquals(3, vars.size());
    assertEquals(processVarValue, vars.get(processVarName));
    assertEquals(task1VarValue, vars.get(task1VarName));
    assertEquals(task2VarValue, vars.get(task2VarName));

    Map<String, Object> additionalVariables = new HashMap<>();
    additionalVariables.put(additionalVar, additionalVarValue);

    vars = formService.submitTaskFormWithVariablesInReturn(secondTask.getId(), additionalVariables, true);
    assertEquals(4, vars.size());
    assertEquals(processVarValue, vars.get(processVarName));
    assertEquals(task1VarValue, vars.get(task1VarName));
    assertEquals(task2VarValue, vars.get(task2VarName));
    assertEquals(additionalVarValue, vars.get(additionalVar));
  }

  /**
   * Tests that the variablesInReturn logic is not applied
   * when we call the regular complete API. This is a performance optimization.
   * Loading all variables may be expensive.
   */
  @Test
  public void testSubmitTaskFormAndDoNotDeserializeVariables()
  {
    // given
    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
      .startEvent()
      .subProcess()
      .embeddedSubProcess()
      .startEvent()
      .userTask("task1")
      .userTask("task2")
      .endEvent()
      .subProcessDone()
      .endEvent()
      .done();

    testRule.deploy(process);

    runtimeService.startProcessInstanceByKey("process", Variables.putValue("var", "val"));

    final Task task = taskService.createTaskQuery().singleResult();

    // when
    final boolean hasLoadedAnyVariables =
      processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<Boolean>() {

        @Override
        public Boolean execute(CommandContext commandContext) {
          formService.submitTaskForm(task.getId(), null);
          return !commandContext.getDbEntityManager().getCachedEntitiesByType(VariableInstanceEntity.class).isEmpty();
        }
      });

    // then
    assertThat(hasLoadedAnyVariables).isFalse();
  }



  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml")
  public void testSubmitTaskFormWithVarialbesInReturnShouldDeserializeObjectValue()
  {
    // given
    ObjectValue value = Variables.objectValue("value").create();
    VariableMap variables = Variables.createVariables().putValue("var", value);

    runtimeService.startProcessInstanceByKey("twoTasksProcess", variables);

    Task task = taskService.createTaskQuery().singleResult();

    // when
    VariableMap result = formService.submitTaskFormWithVariablesInReturn(task.getId(), null, true);

    // then
    ObjectValue returnedValue = result.getValueTyped("var");
    assertThat(returnedValue.isDeserialized()).isTrue();
    assertThat(returnedValue.getValue()).isEqualTo("value");
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml")
  public void testSubmitTaskFormWithVarialbesInReturnShouldNotDeserializeObjectValue()
  {
    // given
    ObjectValue value = Variables.objectValue("value").create();
    VariableMap variables = Variables.createVariables().putValue("var", value);

    ProcessInstance instance = runtimeService.startProcessInstanceByKey("twoTasksProcess", variables);
    String serializedValue = ((ObjectValue) runtimeService.getVariableTyped(instance.getId(), "var")).getValueSerialized();

    Task task = taskService.createTaskQuery().singleResult();

    // when
    VariableMap result = formService.submitTaskFormWithVariablesInReturn(task.getId(), null, false);

    // then
    ObjectValue returnedValue = result.getValueTyped("var");
    assertThat(returnedValue.isDeserialized()).isFalse();
    assertThat(returnedValue.getValueSerialized()).isEqualTo(serializedValue);
  }

  @Deployment
  @Test
  public void testSubmitTaskFormContainingReadonlyVariable() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());

    Task task = taskService.createTaskQuery().singleResult();
    assertNotNull(task);

    formService.submitTaskForm(task.getId(), new HashMap<String, Object>());

    testRule.assertProcessEnded(processInstance.getId());

  }

  @Deployment
  @Test
  public void testGetTaskFormWithoutLabels() {
    runtimeService.startProcessInstanceByKey("testProcess");

    Task task = taskService.createTaskQuery().singleResult();

    // form data can be retrieved
    TaskFormData formData = formService.getTaskFormData(task.getId());

    List<FormField> formFields = formData.getFormFields();
    assertEquals(3, formFields.size());

    List<String> formFieldIds = new ArrayList<>();
    for (FormField field : formFields) {
      assertNull(field.getLabel());
      formFieldIds.add(field.getId());
    }

    assertTrue(formFieldIds.containsAll(Arrays.asList("stringField", "customField", "longField")));

    // the form can be rendered
    Object startForm = formService.getRenderedTaskForm(task.getId());
    assertNotNull(startForm);
  }

  @Test
  public void testDeployTaskFormWithoutFieldTypes() {
    try {
      repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/form/FormServiceTest.testDeployTaskFormWithoutFieldTypes.bpmn20.xml")
        .deploy();
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("form field must have a 'type' attribute", e.getMessage());
    }
  }

  @Deployment
  @Test
  public void testGetStartFormWithoutLabels() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceById(processDefinition.getId());

    // form data can be retrieved
    StartFormData formData = formService.getStartFormData(processDefinition.getId());

    List<FormField> formFields = formData.getFormFields();
    assertEquals(3, formFields.size());

    List<String> formFieldIds = new ArrayList<>();
    for (FormField field : formFields) {
      assertNull(field.getLabel());
      formFieldIds.add(field.getId());
    }

    assertTrue(formFieldIds.containsAll(Arrays.asList("stringField", "customField", "longField")));

    // the form can be rendered
    Object startForm = formService.getRenderedStartForm(processDefinition.getId());
    assertNotNull(startForm);
  }

  @Test
  public void testDeployStartFormWithoutFieldTypes() {
    try {
      repositoryService
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/api/form/FormServiceTest.testDeployStartFormWithoutFieldTypes.bpmn20.xml")
        .deploy();
    } catch (ProcessEngineException e) {
      testRule.assertTextPresent("form field must have a 'type' attribute", e.getMessage());
    }
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/api/form/util/VacationRequest_deprecated_forms.bpmn20.xml",
    "org/camunda/bpm/engine/test/api/form/util/approve.html",
    "org/camunda/bpm/engine/test/api/form/util/request.html",
    "org/camunda/bpm/engine/test/api/form/util/adjustRequest.html" })
  @Test
  public void testTaskFormsWithVacationRequestProcess() {

    // Get start form
    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    Object startForm = formService.getRenderedStartForm(procDefId, "juel");
    assertNotNull(startForm);

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    String processDefinitionId = processDefinition.getId();
    assertEquals("org/camunda/bpm/engine/test/api/form/util/request.html", formService.getStartFormData(processDefinitionId).getFormKey());

    // Define variables that would be filled in through the form
    Map<String, String> formProperties = new HashMap<>();
    formProperties.put("employeeName", "kermit");
    formProperties.put("numberOfDays", "4");
    formProperties.put("vacationMotivation", "I'm tired");
    formService.submitStartFormData(procDefId, formProperties);

    // Management should now have a task assigned to them
    Task task = taskService.createTaskQuery().taskCandidateGroup("management").singleResult();
    assertEquals("Vacation request by kermit", task.getDescription());
    Object taskForm = formService.getRenderedTaskForm(task.getId(), "juel");
    assertNotNull(taskForm);

    // Rejecting the task should put the process back to first task
    taskService.complete(task.getId(), CollectionUtil.singletonMap("vacationApproved", "false"));
    task = taskService.createTaskQuery().singleResult();
    assertEquals("Adjust vacation request", task.getName());
  }

  @Deployment
  @Test
  public void testTaskFormUnavailable() {
    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    assertNull(formService.getRenderedStartForm(procDefId));

    runtimeService.startProcessInstanceByKey("noStartOrTaskForm");
    Task task = taskService.createTaskQuery().singleResult();
    assertNull(formService.getRenderedTaskForm(task.getId()));
  }

  @Deployment
  @Test
  public void testBusinessKey() {
    // given
    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    // when
    StartFormData startFormData = formService.getStartFormData(procDefId);

    // then
    FormField formField = startFormData.getFormFields().get(0);
    assertTrue(formField.isBusinessKey());
  }

  @Deployment
  @Test
  public void testSubmitStartFormWithFormFieldMarkedAsBusinessKey() {
    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();
    ProcessInstance pi = formService.submitStartForm(procDefId, "foo", Variables.createVariables().putValue("secondParam", "bar"));

    assertEquals("foo", pi.getBusinessKey());

    List<VariableInstance> result = runtimeService.createVariableInstanceQuery().list();
    assertEquals(1, result.size());
    assertTrue(result.get(0).getName().equals("secondParam"));
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/form/DeployedFormsProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/form/start.html",
      "org/camunda/bpm/engine/test/api/form/task.html" })
  @Test
  public void testGetDeployedStartForm() {
    // given
    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    // when
    InputStream deployedStartForm = formService.getDeployedStartForm(procDefId);

    // then
    assertNotNull(deployedStartForm);
    String fileAsString = IoUtil.fileAsString("org/camunda/bpm/engine/test/api/form/start.html");
    String deployedStartFormAsString = IoUtil.inputStreamAsString(deployedStartForm);
    assertEquals(deployedStartFormAsString, fileAsString);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/form/EmbeddedDeployedFormsProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/form/start.html",
      "org/camunda/bpm/engine/test/api/form/task.html" })
  @Test
  public void testGetEmbeddedDeployedStartForm() {
    // given
    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    // when
    InputStream deployedStartForm = formService.getDeployedStartForm(procDefId);

    // then
    assertNotNull(deployedStartForm);
    String fileAsString = IoUtil.fileAsString("org/camunda/bpm/engine/test/api/form/start.html");
    String deployedStartFormAsString = IoUtil.inputStreamAsString(deployedStartForm);
    assertEquals(deployedStartFormAsString, fileAsString);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/form/DeployedCamundaFormsProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/form/start.html",
      "org/camunda/bpm/engine/test/api/form/task.html" })
  @Test
  public void testGetDeployedCamundaStartForm() {
    // given
    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    // when
    InputStream deployedStartForm = formService.getDeployedStartForm(procDefId);

    // then
    assertNotNull(deployedStartForm);
    String fileAsString = IoUtil.fileAsString("org/camunda/bpm/engine/test/api/form/start.html");
    String deployedStartFormAsString = IoUtil.inputStreamAsString(deployedStartForm);
    assertEquals(deployedStartFormAsString, fileAsString);
  }

  @Test
  public void testGetDeployedStartFormWithNullProcDefId() {
    try {
      formService.getDeployedStartForm(null);
      fail("Exception expected");
    } catch (BadUserRequestException e) {
      assertEquals("Process definition id cannot be null: processDefinitionId is null", e.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/form/DeployedFormsProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/form/start.html",
      "org/camunda/bpm/engine/test/api/form/task.html" })
  @Test
  public void testGetDeployedTaskForm() {
    // given
    runtimeService.startProcessInstanceByKey("FormsProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    InputStream deployedTaskForm = formService.getDeployedTaskForm(taskId);

    // then
    assertNotNull(deployedTaskForm);
    String fileAsString = IoUtil.fileAsString("org/camunda/bpm/engine/test/api/form/task.html");
    String deployedStartFormAsString = IoUtil.inputStreamAsString(deployedTaskForm);
    assertEquals(deployedStartFormAsString, fileAsString);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/form/DeployedFormsCase.cmmn11.xml",
    "org/camunda/bpm/engine/test/api/form/task.html" })
  @Test
  public void testGetDeployedTaskForm_Case() {
    // given
    caseService.createCaseInstanceByKey("Case_1");
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    InputStream deployedTaskForm = formService.getDeployedTaskForm(taskId);

    // then
    assertNotNull(deployedTaskForm);
    String fileAsString = IoUtil.fileAsString("org/camunda/bpm/engine/test/api/form/task.html");
    String deployedStartFormAsString = IoUtil.inputStreamAsString(deployedTaskForm);
    assertEquals(deployedStartFormAsString, fileAsString);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/form/EmbeddedDeployedFormsProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/form/start.html",
      "org/camunda/bpm/engine/test/api/form/task.html" })
  @Test
  public void testGetEmbeddedDeployedTaskForm() {
    // given
    runtimeService.startProcessInstanceByKey("FormsProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    InputStream deployedTaskForm = formService.getDeployedTaskForm(taskId);

    // then
    assertNotNull(deployedTaskForm);
    String fileAsString = IoUtil.fileAsString("org/camunda/bpm/engine/test/api/form/task.html");
    String deployedStartFormAsString = IoUtil.inputStreamAsString(deployedTaskForm);
    assertEquals(deployedStartFormAsString, fileAsString);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/form/DeployedCamundaFormsProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/form/start.html",
      "org/camunda/bpm/engine/test/api/form/task.html" })
  @Test
  public void testGetDeployedCamundaTaskForm() {
    // given
    runtimeService.startProcessInstanceByKey("FormsProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    InputStream deployedTaskForm = formService.getDeployedTaskForm(taskId);

    // then
    assertNotNull(deployedTaskForm);
    String fileAsString = IoUtil.fileAsString("org/camunda/bpm/engine/test/api/form/task.html");
    String deployedStartFormAsString = IoUtil.inputStreamAsString(deployedTaskForm);
    assertEquals(deployedStartFormAsString, fileAsString);
  }

  @Test
  public void testGetDeployedTaskFormWithNullTaskId() {
    try {
      formService.getDeployedTaskForm(null);
      fail("Exception expected");
    } catch (BadUserRequestException e) {
      assertEquals("Task id cannot be null: taskId is null", e.getMessage());
    }
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/form/DeployedFormsProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/form/task.html" })
  @Test
  public void testGetDeployedStartForm_DeploymentNotFound() {
    // given
    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    // when
    assertThatThrownBy(() -> {
      formService.getDeployedStartForm(procDefId);
    }).isInstanceOf(NotFoundException.class)
    .hasMessageContaining("The form with the resource name 'org/camunda/bpm/engine/test/api/form/start.html' cannot be found in deployment");
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/api/form/DeployedFormsProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/form/start.html" })
  @Test
  public void testGetDeployedTaskForm_DeploymentNotFound() {
    // given
    runtimeService.startProcessInstanceByKey("FormsProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    assertThatThrownBy(() -> {
      formService.getDeployedTaskForm(taskId);
    }).isInstanceOf(NotFoundException.class)
    .hasMessageContaining("The form with the resource name 'org/camunda/bpm/engine/test/api/form/task.html' cannot be found in deployment");
  }

  @Test
  public void testGetDeployedStartForm_FormKeyNotSet() {
    // given
    testRule.deploy(ProcessModels.ONE_TASK_PROCESS);
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    // when
    assertThatThrownBy(() -> {
      formService.getDeployedStartForm(processDefinitionId);
    }).isInstanceOf(BadUserRequestException.class)
    .hasMessage("One of the attributes 'formKey' and 'camunda:formRef' must be supplied but none were set.");
  }

  @Test
  public void testGetDeployedTaskForm_FormKeyNotSet() {
    // given
    testRule.deploy(ProcessModels.ONE_TASK_PROCESS);
    runtimeService.startProcessInstanceByKey("Process");
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    assertThatThrownBy(() -> {
      formService.getDeployedTaskForm(taskId);
    }).isInstanceOf(BadUserRequestException.class)
    .hasMessage("One of the attributes 'formKey' and 'camunda:formRef' must be supplied but none were set.");
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/form/FormServiceTest.testGetDeployedStartFormWithWrongKeyFormat.bpmn20.xml" })
  @Test
  public void testGetDeployedStartFormWithWrongKeyFormat() {
    String processDefinitionId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    try {
      formService.getDeployedStartForm(processDefinitionId);
      fail("Exception expected");
    } catch (BadUserRequestException e) {
      testRule.assertTextPresent("The form key 'formKey' does not reference a deployed form.", e.getMessage());
    }
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/form/FormServiceTest.testGetDeployedTaskFormWithWrongKeyFormat.bpmn20.xml" })
  @Test
  public void testGetDeployedTaskFormWithWrongKeyFormat() {
    runtimeService.startProcessInstanceByKey("FormsProcess");
    String taskId = taskService.createTaskQuery().singleResult().getId();

    try {
      formService.getDeployedTaskForm(taskId);
      fail("Exception expected");
    } catch (BadUserRequestException e) {
      testRule.assertTextPresent("The form key 'formKey' does not reference a deployed form.", e.getMessage());
    }
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/form/FormServiceTest.shouldSubmitStartFormUsingFormKeyAndCamundaFormDefinition.bpmn",
      "org/camunda/bpm/engine/test/api/form/start.form" })
  @Test
  public void shouldSubmitStartFormUsingFormKeyAndCamundaFormDefinition() {
    // given
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("CamundaStartFormProcess").singleResult();

    // when
    ProcessInstance processInstance = formService.submitStartForm(processDefinition.getId(),
        Variables.createVariables());

    // then
    assertThat(repositoryService.createDeploymentQuery().list()).hasSize(1);
    assertThat(findAllCamundaFormDefinitionEntities(processEngineConfiguration)).hasSize(1);
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).list()).hasSize(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).list()).hasSize(1);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/form/FormServiceTest.shouldSubmitTaskFormUsingFormKeyAndCamundaFormDefinition.bpmn",
  "org/camunda/bpm/engine/test/api/form/task.form" })
  @Test
  public void shouldSubmitTaskFormUsingFormKeyAndCamundaFormDefinition() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("CamundaTaskFormProcess");

    // when
    Task task = taskService.createTaskQuery().singleResult();
    formService.submitTaskForm(task.getId(), Variables.createVariables().putValue("variable", "my variable"));

    // then
    assertThat(repositoryService.createDeploymentQuery().list()).hasSize(1);
    assertThat(findAllCamundaFormDefinitionEntities(processEngineConfiguration)).hasSize(1);
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).list()).hasSize(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).list()).hasSize(1);
    assertThat(taskService.createTaskQuery().list()).hasSize(0);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/form/FormServiceTest.shouldSubmitStartFormUsingFormRefAndCamundaFormDefinition.bpmn",
  "org/camunda/bpm/engine/test/api/form/start.form" })
  @Test
  public void shouldSubmitStartFormUsingFormRefAndCamundaFormDefinition() {
    // given
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("CamundaStartFormProcess").singleResult();

    // when
    ProcessInstance processInstance = formService.submitStartForm(processDefinition.getId(),
        Variables.createVariables());

    // then
    assertThat(repositoryService.createDeploymentQuery().list()).hasSize(1);
    assertThat(engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired()
        .execute(new FindCamundaFormDefinitionsCmd())).hasSize(1);
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).list()).hasSize(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).list()).hasSize(1);
  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/form/FormServiceTest.shouldSubmitTaskFormUsingFormRefAndCamundaFormDefinition.bpmn",
  "org/camunda/bpm/engine/test/api/form/task.form" })
  @Test
  public void shouldSubmitTaskFormUsingFormRefAndCamundaFormDefinition() {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("CamundaTaskFormProcess");

    // when
    Task task = taskService.createTaskQuery().singleResult();
    formService.submitTaskForm(task.getId(), Variables.createVariables().putValue("variable", "my variable"));

    // then
    assertThat(repositoryService.createDeploymentQuery().list()).hasSize(1);
    assertThat(engineRule.getProcessEngineConfiguration().getCommandExecutorTxRequired()
        .execute(new FindCamundaFormDefinitionsCmd())).hasSize(1);
    assertThat(runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).list()).hasSize(0);
    assertThat(historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstance.getId()).list()).hasSize(1);
    assertThat(taskService.createTaskQuery().list()).hasSize(0);
  }
}
