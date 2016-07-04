package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

public class MultiTenancyFormServiceCmdsTenantCheckTest {
 protected static final String TENANT_ONE = "tenant1";
  
  protected static final String PROCESS_DEFINITION_KEY = "formKeyProcess";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected TaskService taskService;

  protected FormService formService;

  protected RuntimeService runtimeService;

  protected IdentityService identityService;

  protected RepositoryService repositoryService;

  protected ProcessEngineConfiguration processEngineConfiguration;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ExpectedException thrown= ExpectedException.none();

  @Before
  public void init() {

    taskService = engineRule.getTaskService();
    
    formService = engineRule.getFormService();

    identityService = engineRule.getIdentityService();

    runtimeService = engineRule.getRuntimeService();

    repositoryService = engineRule.getRepositoryService();

    processEngineConfiguration = engineRule.getProcessEngineConfiguration();

  }

  // GetStartForm test
  @Test
  public void testGetStartFormWithAuthenticatedTenant() {

    testRule.deployForTenant(TENANT_ONE,
      "org/camunda/bpm/engine/test/api/authorization/formKeyProcess.bpmn20.xml");
    
    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    StartFormData startFormData = formService.getStartFormData(instance.getProcessDefinitionId()); 

    // then
    assertNotNull(startFormData);
    assertEquals("aStartFormKey",startFormData.getFormKey());
  }

  @Test
  public void testGetStartFormWithNoAuthenticatedTenant() {

    testRule.deployForTenant(TENANT_ONE,
    "org/camunda/bpm/engine/test/api/authorization/formKeyProcess.bpmn20.xml");

    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    identityService.setAuthentication("aUserId", null);
 
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the process definition '" + instance.getProcessDefinitionId() 
      +"' because it belongs to no authenticated tenant.");
    
    // when
    formService.getStartFormData(instance.getProcessDefinitionId());
    
  }

  @Test
  public void testGetStartFormWithDisabledTenantCheck() {

    testRule.deployForTenant(TENANT_ONE,
    "org/camunda/bpm/engine/test/api/authorization/formKeyProcess.bpmn20.xml");

    ProcessInstance instance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);

    identityService.setAuthentication("aUserId", null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    StartFormData startFormData = formService.getStartFormData(instance.getProcessDefinitionId()); 

    // then
    assertNotNull(startFormData);
    assertEquals("aStartFormKey",startFormData.getFormKey());

  }

  // GetRenderedStartForm
  @Test
  public void testGetRenderedStartFormWithAuthenticatedTenant() {

    testRule.deployForTenant(TENANT_ONE,
      "org/camunda/bpm/engine/test/api/form/util/VacationRequest_deprecated_forms.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/form/util/request.form");
    
    String processDefinitionId = repositoryService.createProcessDefinitionQuery()
      .singleResult().getId();
 
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    assertNotNull(formService.getRenderedStartForm(processDefinitionId, "juel"));
  }

  @Test
  public void testGetRenderedStartFormWithNoAuthenticatedTenant() {

    testRule.deployForTenant(TENANT_ONE,
      "org/camunda/bpm/engine/test/api/form/util/VacationRequest_deprecated_forms.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/form/util/request.form");

    String processDefinitionId = repositoryService.createProcessDefinitionQuery()
      .singleResult().getId();

    identityService.setAuthentication("aUserId", null);
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the process definition '" + processDefinitionId
    +"' because it belongs to no authenticated tenant.");   

    // when
    formService.getRenderedStartForm(processDefinitionId, "juel");
  }

  @Test
  public void testGetRenderedStartFormWithDisabledTenantCheck() {

    testRule.deployForTenant(TENANT_ONE,
      "org/camunda/bpm/engine/test/api/form/util/VacationRequest_deprecated_forms.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/form/util/request.form");

    String processDefinitionId = repositoryService.createProcessDefinitionQuery()
      .singleResult().getId();

    identityService.setAuthentication("aUserId", null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    assertNotNull(formService.getRenderedStartForm(processDefinitionId, "juel"));
  }

  // submitStartForm
  @Test
  public void testSubmitStartFormWithAuthenticatedTenant() {

    testRule.deployForTenant(TENANT_ONE,
      "org/camunda/bpm/engine/test/api/form/util/VacationRequest_deprecated_forms.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/form/util/request.form");

    String processDefinitionId = repositoryService.createProcessDefinitionQuery()
      .singleResult().getId();

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("employeeName", "demo");

    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    assertNotNull(formService.submitStartForm(processDefinitionId, properties));
  }

  @Test
  public void testSubmitStartFormWithNoAuthenticatedTenant() {

    testRule.deployForTenant(TENANT_ONE,
      "org/camunda/bpm/engine/test/api/form/util/VacationRequest_deprecated_forms.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/form/util/request.form");

    String processDefinitionId = repositoryService.createProcessDefinitionQuery()
      .singleResult().getId();

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("employeeName", "demo");

    identityService.setAuthentication("aUserId", null);

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot create an instance of the process definition '" + processDefinitionId
      +"' because it belongs to no authenticated tenant.");  

    // when
    formService.submitStartForm(processDefinitionId, properties);
    
  }

  @Test
  public void testSubmitStartFormWithDisabledTenantcheck() {

    testRule.deployForTenant(TENANT_ONE,
      "org/camunda/bpm/engine/test/api/form/util/VacationRequest_deprecated_forms.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/form/util/request.form");

    String processDefinitionId = repositoryService.createProcessDefinitionQuery()
      .singleResult().getId();

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("employeeName", "demo");

    identityService.setAuthentication("aUserId", null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    // when
    assertNotNull(formService.submitStartForm(processDefinitionId, properties));
    
  }

  // getStartFormKey
  @Test
  public void testGetStartFormKeyWithAuthenticatedTenant() {
    
    testRule.deployForTenant(TENANT_ONE, "org/camunda/bpm/engine/test/api/authorization/formKeyProcess.bpmn20.xml");
    
    String processDefinitionId = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getProcessDefinitionId();
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    assertEquals("aStartFormKey", formService.getStartFormKey(processDefinitionId));
    
  }
  
  @Test
  public void testGetStartFormKeyWithNoAuthenticatedTenant() {
    
    testRule.deployForTenant(TENANT_ONE, "org/camunda/bpm/engine/test/api/authorization/formKeyProcess.bpmn20.xml");
    
    String processDefinitionId = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getProcessDefinitionId();
    
    identityService.setAuthentication("aUserId", null);
    
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the process definition '" + processDefinitionId + "' because it belongs to no authenticated tenant.");
    formService.getStartFormKey(processDefinitionId);
    
  }
  
  @Test
  public void testGetStartFormKeyWithDisabledTenantCheck() {
    
    testRule.deployForTenant(TENANT_ONE, "org/camunda/bpm/engine/test/api/authorization/formKeyProcess.bpmn20.xml");
    
    String processDefinitionId = runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY).getProcessDefinitionId();
    
    identityService.setAuthentication("aUserId", null);
    processEngineConfiguration.setTenantCheckEnabled(false);
    
    // then
    assertEquals("aStartFormKey", formService.getStartFormKey(processDefinitionId));
    
  }
  
  // GetTaskForm test
  @Test
  public void testGetTaskFormWithAuthenticatedTenant() {
    
    testRule.deployForTenant(TENANT_ONE, "org/camunda/bpm/engine/test/api/authorization/formKeyProcess.bpmn20.xml");
    
    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    String taskId = taskService.createTaskQuery().singleResult().getId();

    TaskFormData taskFormData = formService.getTaskFormData(taskId);

    // then
    assertNotNull(taskFormData);
    assertEquals("aTaskFormKey", taskFormData.getFormKey());    
  }
  
  @Test
  public void testGetTaskFormWithNoAuthenticatedTenant() {
    
    testRule.deployForTenant(TENANT_ONE, "org/camunda/bpm/engine/test/api/authorization/formKeyProcess.bpmn20.xml");
    
    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
    
    String taskId = taskService.createTaskQuery().singleResult().getId();
    
    identityService.setAuthentication("aUserId", null);
    
    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the task '" + taskId + "' because it belongs to no authenticated tenant.");
    
    // when
    formService.getTaskFormData(taskId);
    
  }
  
  @Test
  public void testGetTaskFormWithDisabledTenantCheck() {
    
    testRule.deployForTenant(TENANT_ONE, "org/camunda/bpm/engine/test/api/authorization/formKeyProcess.bpmn20.xml");
    
    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
    
    String taskId = taskService.createTaskQuery().singleResult().getId();
    
    identityService.setAuthentication("aUserId", null);
    processEngineConfiguration.setTenantCheckEnabled(false);
    
    TaskFormData taskFormData = formService.getTaskFormData(taskId);

    // then
    assertNotNull(taskFormData);
    assertEquals("aTaskFormKey", taskFormData.getFormKey());
    
  }
  
  // submitTaskForm
  @Test
  public void testSubmitTaskFormWithAuthenticatedTenant() {

    testRule.deployForTenant(TENANT_ONE,
    "org/camunda/bpm/engine/test/api/authorization/formKeyProcess.bpmn20.xml");
    
    String processDefinitionId = repositoryService.createProcessDefinitionQuery()
      .singleResult().getId();

    runtimeService.startProcessInstanceById(processDefinitionId);

    assertEquals(taskService.createTaskQuery().processDefinitionId(processDefinitionId).count(), 1);

    String taskId = taskService.createTaskQuery().processDefinitionId(processDefinitionId).singleResult().getId();
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    
    formService.submitTaskForm(taskId, null);

    // task gets completed on execution of submitTaskForm
    assertEquals(taskService.createTaskQuery().processDefinitionId(processDefinitionId).count(), 0);
  }

  @Test
  public void testSubmitTaskFormWithNoAuthenticatedTenant() {

    testRule.deployForTenant(TENANT_ONE,
      "org/camunda/bpm/engine/test/api/authorization/formKeyProcess.bpmn20.xml");

    String processDefinitionId = repositoryService.createProcessDefinitionQuery()
      .singleResult().getId();

    runtimeService.startProcessInstanceById(processDefinitionId);

    String taskId = taskService.createTaskQuery().processDefinitionId(processDefinitionId).singleResult().getId();
    
    identityService.setAuthentication("aUserId", null);

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot work on task '" + taskId
      +"' because it belongs to no authenticated tenant.");

    // when
    formService.submitTaskForm(taskId, null);
  }

  @Test
  public void testSubmitTaskFormWithDisabledTenantCheck() {

    testRule.deployForTenant(TENANT_ONE,
      "org/camunda/bpm/engine/test/api/authorization/formKeyProcess.bpmn20.xml");

    String processDefinitionId = repositoryService.createProcessDefinitionQuery()
      .singleResult().getId();

    runtimeService.startProcessInstanceById(processDefinitionId);

    String taskId = taskService.createTaskQuery().processDefinitionId(processDefinitionId).singleResult().getId();
    
    identityService.setAuthentication("aUserId", null);
    processEngineConfiguration.setTenantCheckEnabled(false);
    
    formService.submitTaskForm(taskId, null);

    // task gets completed on execution of submitTaskForm
    assertEquals(taskService.createTaskQuery().processDefinitionId(processDefinitionId).count(), 0);
  }

  // getRenderedTaskForm
  @Test
  public void testGetRenderedTaskFormWithAuthenticatedTenant() {

    // deploy tenants
    testRule.deployForTenant(TENANT_ONE,
      "org/camunda/bpm/engine/test/api/form/FormsProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/form/task.form").getId();
    
    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("room", "5b");
    properties.put("speaker", "Mike");
    formService.submitStartForm(procDefId, properties).getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));

    // then
    assertEquals("Mike is speaking in room 5b", formService.getRenderedTaskForm(taskId, "juel"));
  }

  @Test
  public void testGetRenderedTaskFormWithNoAuthenticatedTenant() {

    // deploy tenants
    testRule.deployForTenant(TENANT_ONE,
      "org/camunda/bpm/engine/test/api/form/FormsProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/form/task.form").getId();
    
    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("room", "5b");
    properties.put("speaker", "Mike");
    formService.submitStartForm(procDefId, properties);

    String taskId = taskService.createTaskQuery().singleResult().getId();
    identityService.setAuthentication("aUserId", null);

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot read the task '" + taskId
      +"' because it belongs to no authenticated tenant.");

    // when
    formService.getRenderedTaskForm(taskId, "juel");
  }

  @Test
  public void testGetRenderedTaskFormWithDisabledTenantCheck() {

    // deploy tenants
    testRule.deployForTenant(TENANT_ONE,
      "org/camunda/bpm/engine/test/api/form/FormsProcess.bpmn20.xml",
      "org/camunda/bpm/engine/test/api/form/task.form").getId();
    
    String procDefId = repositoryService.createProcessDefinitionQuery().singleResult().getId();

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("room", "5b");
    properties.put("speaker", "Mike");
    formService.submitStartForm(procDefId, properties);

    String taskId = taskService.createTaskQuery().singleResult().getId();
    identityService.setAuthentication("aUserId", null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    // then
    assertEquals("Mike is speaking in room 5b", formService.getRenderedTaskForm(taskId, "juel"));
  }

  // getTaskFormKey
  @Test
  public void testGetTaskFormKeyWithAuthenticatedTenant() {

    testRule.deployForTenant(TENANT_ONE,
      "org/camunda/bpm/engine/test/api/authorization/formKeyProcess.bpmn20.xml");
    
    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
    
    Task task = taskService.createTaskQuery().singleResult();
    
    identityService.setAuthentication("aUserId", null, Arrays.asList(TENANT_ONE));
    assertEquals("aTaskFormKey", formService.getTaskFormKey(task.getProcessDefinitionId(), task.getTaskDefinitionKey()));
    
  }

  @Test
  public void testGetTaskFormKeyWithNoAuthenticatedTenant() {

    testRule.deployForTenant(TENANT_ONE,
      "org/camunda/bpm/engine/test/api/authorization/formKeyProcess.bpmn20.xml");
    
    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
    
    Task task = taskService.createTaskQuery().singleResult();
    
    identityService.setAuthentication("aUserId", null);

    // then
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the process definition '" + task.getProcessDefinitionId()
      +"' because it belongs to no authenticated tenant.");

    // when
    formService.getTaskFormKey(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
    
  }

  @Test
  public void testGetTaskFormKeyWithDisabledTenantCheck() {

    testRule.deployForTenant(TENANT_ONE,
      "org/camunda/bpm/engine/test/api/authorization/formKeyProcess.bpmn20.xml");
    
    runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
    
    Task task = taskService.createTaskQuery().singleResult();
    
    identityService.setAuthentication("aUserId", null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    formService.getTaskFormKey(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
    // then
    assertEquals("aTaskFormKey", formService.getTaskFormKey(task.getProcessDefinitionId(), task.getTaskDefinitionKey()));
  }
}
