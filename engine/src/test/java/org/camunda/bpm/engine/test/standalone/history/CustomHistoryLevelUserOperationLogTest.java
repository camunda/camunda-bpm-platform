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
package org.camunda.bpm.engine.test.standalone.history;

import static org.camunda.bpm.engine.EntityTypes.JOB;
import static org.camunda.bpm.engine.EntityTypes.JOB_DEFINITION;
import static org.camunda.bpm.engine.EntityTypes.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.EntityTypes.PROCESS_INSTANCE;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ACTIVATE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ACTIVATE_JOB;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ACTIVATE_JOB_DEFINITION;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_DELETE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SET_JOB_RETRIES;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SUSPEND;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SUSPEND_JOB;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SUSPEND_JOB_DEFINITION;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION;
import static org.camunda.bpm.engine.impl.cmd.AbstractSetBatchStateCmd.SUSPENSION_STATE_PROPERTY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.impl.ManagementServiceImpl;
import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.impl.TaskServiceImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestBaseRule;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class CustomHistoryLevelUserOperationLogTest {

  public static final String USER_ID = "demo";
  protected static final String ONE_TASK_PROCESS = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";
  protected static final String ONE_TASK_CASE = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";

  static HistoryLevel customHistoryLevelUOL = new CustomHistoryLevelUserOperationLog();

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration -> {
      configuration.setJdbcUrl("jdbc:h2:mem:CustomHistoryLevelUserOperationLogTest");
      configuration.setCustomHistoryLevels(Arrays.asList(customHistoryLevelUOL));
      configuration.setHistory("aCustomHistoryLevelUOL");
      configuration.setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_CREATE_DROP);
  });

  public ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public AuthorizationTestBaseRule authRule = new AuthorizationTestBaseRule(engineRule);
  public ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(authRule).around(testRule);

  protected HistoryService historyService;
  protected RuntimeService runtimeService;
  protected ManagementServiceImpl managementService;
  protected IdentityService identityService;
  protected RepositoryService repositoryService;
  protected TaskService taskService;
  protected CaseService caseService;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  protected ProcessInstance process;
  protected Task userTask;
  protected String processTaskId;

  @Before
  public void setUp() throws Exception {
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    managementService = (ManagementServiceImpl) engineRule.getManagementService();
    identityService = engineRule.getIdentityService();
    repositoryService = engineRule.getRepositoryService();
    taskService = engineRule.getTaskService();
    caseService = engineRule.getCaseService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    identityService.setAuthenticatedUserId(USER_ID);
  }

  @After
  public void tearDown() throws Exception {
    identityService.clearAuthentication();
    List<UserOperationLogEntry> logs = query().list();
    for (UserOperationLogEntry log : logs) {
      historyService.deleteUserOperationLogEntry(log.getId());
    }
  }

  @Test
  @Deployment(resources = ONE_TASK_PROCESS)
  public void testQueryProcessInstanceOperationsById() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.suspendProcessInstanceById(process.getId());
    runtimeService.activateProcessInstanceById(process.getId());

    runtimeService.deleteProcessInstance(process.getId(), "a delete reason");

    // then
    assertEquals(4, query().entityType(PROCESS_INSTANCE).count());

    UserOperationLogEntry deleteEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processInstanceId(process.getId())
        .operationType(OPERATION_TYPE_DELETE)
        .singleResult();

    assertNotNull(deleteEntry);
    assertEquals(process.getId(), deleteEntry.getProcessInstanceId());
    assertNotNull(deleteEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", deleteEntry.getProcessDefinitionKey());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, deleteEntry.getCategory());

    UserOperationLogEntry suspendEntry = query()
      .entityType(PROCESS_INSTANCE)
      .processInstanceId(process.getId())
      .operationType(OPERATION_TYPE_SUSPEND)
      .singleResult();

    assertNotNull(suspendEntry);
    assertEquals(process.getId(), suspendEntry.getProcessInstanceId());
    assertNotNull(suspendEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", suspendEntry.getProcessDefinitionKey());

    assertEquals("suspensionState", suspendEntry.getProperty());
    assertEquals("suspended", suspendEntry.getNewValue());
    assertNull(suspendEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, suspendEntry.getCategory());

    UserOperationLogEntry activateEntry = query()
        .entityType(PROCESS_INSTANCE)
        .processInstanceId(process.getId())
        .operationType(OPERATION_TYPE_ACTIVATE)
        .singleResult();

    assertNotNull(activateEntry);
    assertEquals(process.getId(), activateEntry.getProcessInstanceId());
    assertNotNull(activateEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", activateEntry.getProcessDefinitionKey());

    assertEquals("suspensionState", activateEntry.getProperty());
    assertEquals("active", activateEntry.getNewValue());
    assertNull(activateEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, activateEntry.getCategory());
  }

  @Test
  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryProcessDefinitionOperationsByKey() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    repositoryService.suspendProcessDefinitionByKey("oneTaskProcess", true, null);
    repositoryService.activateProcessDefinitionByKey("oneTaskProcess", true, null);
    repositoryService.deleteProcessDefinitions().byKey("oneTaskProcess").cascade().delete();

    // then
    assertEquals(5, query().entityType(PROCESS_DEFINITION).count());

    UserOperationLogEntry suspendDefinitionEntry = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionKey("oneTaskProcess")
      .operationType(OPERATION_TYPE_SUSPEND_PROCESS_DEFINITION)
      .property(SUSPENSION_STATE_PROPERTY)
      .singleResult();

    assertNotNull(suspendDefinitionEntry);
    assertNull(suspendDefinitionEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", suspendDefinitionEntry.getProcessDefinitionKey());
    assertNull(suspendDefinitionEntry.getDeploymentId());

    assertEquals("suspensionState", suspendDefinitionEntry.getProperty());
    assertEquals("suspended", suspendDefinitionEntry.getNewValue());
    assertNull(suspendDefinitionEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, suspendDefinitionEntry.getCategory());

    UserOperationLogEntry activateDefinitionEntry = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionKey("oneTaskProcess")
      .operationType(OPERATION_TYPE_ACTIVATE_PROCESS_DEFINITION)
      .property(SUSPENSION_STATE_PROPERTY)
      .singleResult();

    assertNotNull(activateDefinitionEntry);
    assertNull(activateDefinitionEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", activateDefinitionEntry.getProcessDefinitionKey());
    assertNull(activateDefinitionEntry.getDeploymentId());

    assertEquals("suspensionState", activateDefinitionEntry.getProperty());
    assertEquals("active", activateDefinitionEntry.getNewValue());
    assertNull(activateDefinitionEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, activateDefinitionEntry.getCategory());

    UserOperationLogEntry deleteDefinitionEntry = query()
      .entityType(PROCESS_DEFINITION)
      .processDefinitionKey("oneTaskProcess")
      .operationType(OPERATION_TYPE_DELETE)
      .singleResult();

    assertNotNull(deleteDefinitionEntry);
    assertNotNull(deleteDefinitionEntry.getProcessDefinitionId());
    assertEquals("oneTaskProcess", deleteDefinitionEntry.getProcessDefinitionKey());
    assertNotNull(deleteDefinitionEntry.getDeploymentId());

    assertEquals("cascade", deleteDefinitionEntry.getProperty());
    assertEquals("true", deleteDefinitionEntry.getNewValue());
    assertNotNull(deleteDefinitionEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, deleteDefinitionEntry.getCategory());
  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/engine/test/history/HistoricJobLogTest.testAsyncContinuation.bpmn20.xml"})
  public void testQueryJobOperations() {
    // given
    process = runtimeService.startProcessInstanceByKey("process");

    // when
    managementService.suspendJobDefinitionByProcessDefinitionId(process.getProcessDefinitionId());
    managementService.activateJobDefinitionByProcessDefinitionId(process.getProcessDefinitionId());
    managementService.suspendJobByProcessInstanceId(process.getId());
    managementService.activateJobByProcessInstanceId(process.getId());

    // then
    assertEquals(2, query().entityType(JOB_DEFINITION).count());
    assertEquals(2, query().entityType(JOB).count());

    // active job definition
    UserOperationLogEntry activeJobDefinitionEntry = query()
      .entityType(JOB_DEFINITION)
      .processDefinitionId(process.getProcessDefinitionId())
      .operationType(OPERATION_TYPE_ACTIVATE_JOB_DEFINITION)
      .singleResult();

    assertNotNull(activeJobDefinitionEntry);
    assertEquals(process.getProcessDefinitionId(), activeJobDefinitionEntry.getProcessDefinitionId());

    assertEquals("suspensionState", activeJobDefinitionEntry.getProperty());
    assertEquals("active", activeJobDefinitionEntry.getNewValue());
    assertNull(activeJobDefinitionEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, activeJobDefinitionEntry.getCategory());

    // active job
    UserOperationLogEntry activateJobIdEntry = query()
      .entityType(JOB)
      .processInstanceId(process.getProcessInstanceId())
      .operationType(OPERATION_TYPE_ACTIVATE_JOB)
      .singleResult();

    assertNotNull(activateJobIdEntry);
    assertEquals(process.getProcessInstanceId(), activateJobIdEntry.getProcessInstanceId());

    assertEquals("suspensionState", activateJobIdEntry.getProperty());
    assertEquals("active", activateJobIdEntry.getNewValue());
    assertNull(activateJobIdEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, activateJobIdEntry.getCategory());

    // suspended job definition
    UserOperationLogEntry suspendJobDefinitionEntry = query()
      .entityType(JOB_DEFINITION)
      .processDefinitionId(process.getProcessDefinitionId())
      .operationType(OPERATION_TYPE_SUSPEND_JOB_DEFINITION)
      .singleResult();

    assertNotNull(suspendJobDefinitionEntry);
    assertEquals(process.getProcessDefinitionId(), suspendJobDefinitionEntry.getProcessDefinitionId());

    assertEquals("suspensionState", suspendJobDefinitionEntry.getProperty());
    assertEquals("suspended", suspendJobDefinitionEntry.getNewValue());
    assertNull(suspendJobDefinitionEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, suspendJobDefinitionEntry.getCategory());

    // suspended job
    UserOperationLogEntry suspendedJobEntry = query()
      .entityType(JOB)
      .processInstanceId(process.getProcessInstanceId())
      .operationType(OPERATION_TYPE_SUSPEND_JOB)
      .singleResult();

    assertNotNull(suspendedJobEntry);
    assertEquals(process.getProcessInstanceId(), suspendedJobEntry.getProcessInstanceId());

    assertEquals("suspensionState", suspendedJobEntry.getProperty());
    assertEquals("suspended", suspendedJobEntry.getNewValue());
    assertNull(suspendedJobEntry.getOrgValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, suspendedJobEntry.getCategory());
  }

  @Test
  @Deployment(resources = { "org/camunda/bpm/engine/test/bpmn/async/FoxJobRetryCmdTest.testFailedServiceTask.bpmn20.xml" })
  public void testQueryJobRetryOperationsById() {
    // given
    process = runtimeService.startProcessInstanceByKey("failedServiceTask");
    Job job = managementService.createJobQuery().processInstanceId(process.getProcessInstanceId()).singleResult();

    managementService.setJobRetries(job.getId(), 10);

    // then
    assertEquals(1, query().entityType(JOB).operationType(OPERATION_TYPE_SET_JOB_RETRIES).count());

    UserOperationLogEntry jobRetryEntry = query()
      .entityType(JOB)
      .jobId(job.getId())
      .operationType(OPERATION_TYPE_SET_JOB_RETRIES)
      .singleResult();

    assertNotNull(jobRetryEntry);
    assertEquals(job.getId(), jobRetryEntry.getJobId());

    assertEquals("3", jobRetryEntry.getOrgValue());
    assertEquals("10", jobRetryEntry.getNewValue());
    assertEquals("retries", jobRetryEntry.getProperty());
    assertEquals(job.getJobDefinitionId(), jobRetryEntry.getJobDefinitionId());
    assertEquals(job.getProcessInstanceId(), jobRetryEntry.getProcessInstanceId());
    assertEquals(job.getProcessDefinitionKey(), jobRetryEntry.getProcessDefinitionKey());
    assertEquals(job.getProcessDefinitionId(), jobRetryEntry.getProcessDefinitionId());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, jobRetryEntry.getCategory());
  }

  // ----- PROCESS INSTANCE MODIFICATION -----

  @Test
  @Deployment(resources = { ONE_TASK_PROCESS })
  public void testQueryProcessInstanceModificationOperation() {
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    String processInstanceId = processInstance.getId();

    ProcessDefinition definition = repositoryService.createProcessDefinitionQuery().singleResult();

    runtimeService
      .createProcessInstanceModification(processInstance.getId())
      .startBeforeActivity("theTask")
      .execute();

    UserOperationLogQuery logQuery = query()
      .entityType(EntityTypes.PROCESS_INSTANCE)
      .operationType(UserOperationLogEntry.OPERATION_TYPE_MODIFY_PROCESS_INSTANCE);

    assertEquals(1, logQuery.count());
    UserOperationLogEntry logEntry = logQuery.singleResult();

    assertEquals(processInstanceId, logEntry.getProcessInstanceId());
    assertEquals(processInstance.getProcessDefinitionId(), logEntry.getProcessDefinitionId());
    assertEquals(definition.getKey(), logEntry.getProcessDefinitionKey());
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_MODIFY_PROCESS_INSTANCE, logEntry.getOperationType());
    assertEquals(EntityTypes.PROCESS_INSTANCE, logEntry.getEntityType());
    assertNull(logEntry.getProperty());
    assertNull(logEntry.getOrgValue());
    assertNull(logEntry.getNewValue());
    assertEquals(UserOperationLogEntry.CATEGORY_OPERATOR, logEntry.getCategory());
  }

  // ----- ADD VARIABLES -----

  @Test
  @Deployment(resources = { ONE_TASK_PROCESS })
  public void testQueryAddExecutionVariableOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.setVariable(process.getId(), "testVariable1", "THIS IS TESTVARIABLE!!!");

    // then
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE, UserOperationLogEntry.CATEGORY_OPERATOR);
  }

  @Test
  @Deployment(resources = { ONE_TASK_PROCESS })
  public void testQueryAddTaskVariablesSingleAndMapOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.setVariable(processTaskId, "testVariable3", "foo");
    taskService.setVariables(processTaskId, createMapForVariableAddition());
    taskService.setVariable(processTaskId, "testVariable4", "bar");

    // then
    verifyVariableOperationAsserts(3, UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE, UserOperationLogEntry.CATEGORY_TASK_WORKER);
  }

  // ----- PATCH VARIABLES -----

  @Test
  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryPatchExecutionVariablesOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    ((RuntimeServiceImpl) runtimeService)
      .updateVariables(process.getId(), createMapForVariableAddition(), createCollectionForVariableDeletion());

    // then
   verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_MODIFY_VARIABLE, UserOperationLogEntry.CATEGORY_OPERATOR);
  }

  @Test
  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryPatchTaskVariablesOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // when
    ((TaskServiceImpl) taskService)
      .updateVariablesLocal(processTaskId, createMapForVariableAddition(), createCollectionForVariableDeletion());

    // then
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_MODIFY_VARIABLE, UserOperationLogEntry.CATEGORY_TASK_WORKER);
  }

  // ----- REMOVE VARIABLES -----


  @Test
  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryRemoveExecutionVariablesMapOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");

    // when
    runtimeService.removeVariables(process.getId(), createCollectionForVariableDeletion());

    // then
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_REMOVE_VARIABLE, UserOperationLogEntry.CATEGORY_OPERATOR);
  }

  @Test
  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryRemoveTaskVariableOperation() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.removeVariable(processTaskId, "testVariable1");

    // then
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_REMOVE_VARIABLE, UserOperationLogEntry.CATEGORY_TASK_WORKER);
  }

  @Test
  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryByEntityTypes() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.setAssignee(processTaskId, "foo");
    taskService.setVariable(processTaskId, "foo", "bar");

    // then
    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .entityTypeIn(EntityTypes.TASK, EntityTypes.VARIABLE);

    verifyQueryResults(query, 2);
  }

  @Test
  @Deployment(resources = {ONE_TASK_PROCESS})
  public void testQueryByCategories() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    processTaskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.setAssignee(processTaskId, "foo");
    taskService.setVariable(processTaskId, "foo", "bar");

    // then
    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .categoryIn(UserOperationLogEntry.CATEGORY_TASK_WORKER);

    verifyQueryResults(query, 2);
  }
  // --------------- CMMN --------------------

  @Test
  @Deployment(resources={ONE_TASK_CASE})
  public void testQueryByCaseExecutionId() {
    // given:
    // a deployed case definition
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // an active case instance
    caseService
       .withCaseDefinition(caseDefinitionId)
       .create();

    String caseExecutionId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    Task task = taskService.createTaskQuery().singleResult();

    assertNotNull(task);

    // when
    taskService.setAssignee(task.getId(), "demo");

    // then

    UserOperationLogQuery query = historyService
      .createUserOperationLogQuery()
      .caseExecutionId(caseExecutionId);

    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryByDeploymentId() {
    // given
    String deploymentId = repositoryService
        .createDeployment()
        .addClasspathResource(ONE_TASK_PROCESS)
        .deploy()
        .getId();

    // when
    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .deploymentId(deploymentId);

    // then
    verifyQueryResults(query, 1);

    repositoryService.deleteDeployment(deploymentId, true);
  }

  @Test
  @Deployment(resources = { ONE_TASK_PROCESS })
  public void testUserOperationLogDeletion() {
    // given
    process = runtimeService.startProcessInstanceByKey("oneTaskProcess");
    runtimeService.setVariable(process.getId(), "testVariable1", "THIS IS TESTVARIABLE!!!");

    // assume
    verifyVariableOperationAsserts(1, UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE, UserOperationLogEntry.CATEGORY_OPERATOR);
    UserOperationLogQuery query = query().entityType(EntityTypes.VARIABLE).operationType(UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE);
    assertEquals(1, query.count());

    // when
    historyService.deleteUserOperationLogEntry(query.singleResult().getId());

    // then
    assertEquals(0, query.count());
  }

  protected void verifyQueryResults(UserOperationLogQuery query, int countExpected) {
    assertEquals(countExpected, query.list().size());
    assertEquals(countExpected, query.count());

    if (countExpected == 1) {
      assertNotNull(query.singleResult());
    } else if (countExpected > 1){
      verifySingleResultFails(query);
    } else if (countExpected == 0) {
      assertNull(query.singleResult());
    }
  }

  protected void verifySingleResultFails(UserOperationLogQuery query) {
    try {
      query.singleResult();
      fail();
    } catch (ProcessEngineException e) {}
  }

  protected Map<String, Object> createMapForVariableAddition() {
    Map<String, Object> variables =  new HashMap<>();
    variables.put("testVariable1", "THIS IS TESTVARIABLE!!!");
    variables.put("testVariable2", "OVER 9000!");

    return variables;
  }

  protected Collection<String> createCollectionForVariableDeletion() {
    Collection<String> variables = new ArrayList<>();
    variables.add("testVariable3");
    variables.add("testVariable4");

    return variables;
  }

  protected void verifyVariableOperationAsserts(int countAssertValue, String operationType, String category) {
    UserOperationLogQuery logQuery = query().entityType(EntityTypes.VARIABLE).operationType(operationType);
    assertEquals(countAssertValue, logQuery.count());

    if(countAssertValue > 1) {
      List<UserOperationLogEntry> logEntryList = logQuery.list();

      for (UserOperationLogEntry logEntry : logEntryList) {
        assertEquals(process.getProcessDefinitionId(), logEntry.getProcessDefinitionId());
        assertEquals(process.getProcessInstanceId(), logEntry.getProcessInstanceId());
        assertEquals(category, logEntry.getCategory());
      }
    } else {
      UserOperationLogEntry logEntry = logQuery.singleResult();
      assertEquals(process.getProcessDefinitionId(), logEntry.getProcessDefinitionId());
      assertEquals(process.getProcessInstanceId(), logEntry.getProcessInstanceId());
      assertEquals(category, logEntry.getCategory());
    }
  }

  protected UserOperationLogQuery query() {
    return historyService.createUserOperationLogQuery();
  }

}
