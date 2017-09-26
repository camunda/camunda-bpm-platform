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
package org.camunda.bpm.engine.test.history.useroperationlog;

import java.util.List;

import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 * @author Roman Smirnov
 *
 */
public class UserOperationLogDeploymentTest extends AbstractUserOperationLogTest {

  protected static final String DEPLOYMENT_NAME = "my-deployment";
  protected static final String RESOURCE_NAME = "path/to/my/process.bpmn";
  protected static final String PROCESS_KEY = "process";


  protected void tearDown() throws Exception {
    super.tearDown();

    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    for (Deployment deployment : deployments) {
      repositoryService.deleteDeployment(deployment.getId(), true, true);
    }
  }

  public void testCreateDeployment() {
    // when
    Deployment deployment = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, createProcessWithServiceTask(PROCESS_KEY))
        .deploy();

    // then
    UserOperationLogEntry userOperationLogEntry = historyService.createUserOperationLogQuery().singleResult();
    assertNotNull(userOperationLogEntry);

    assertEquals(EntityTypes.DEPLOYMENT, userOperationLogEntry.getEntityType());
    assertEquals(deployment.getId(), userOperationLogEntry.getDeploymentId());

    assertEquals(UserOperationLogEntry.OPERATION_TYPE_CREATE, userOperationLogEntry.getOperationType());

    assertEquals("duplicateFilterEnabled", userOperationLogEntry.getProperty());
    assertNull(userOperationLogEntry.getOrgValue());
    assertFalse(Boolean.valueOf(userOperationLogEntry.getNewValue()));

    assertEquals(USER_ID, userOperationLogEntry.getUserId());

    assertNull(userOperationLogEntry.getJobDefinitionId());
    assertNull(userOperationLogEntry.getProcessInstanceId());
    assertNull(userOperationLogEntry.getProcessDefinitionId());
    assertNull(userOperationLogEntry.getProcessDefinitionKey());
    assertNull(userOperationLogEntry.getCaseInstanceId());
    assertNull(userOperationLogEntry.getCaseDefinitionId());
  }

  public void testCreateDeploymentPa() {
    // given
    EmbeddedProcessApplication application = new EmbeddedProcessApplication();

    // when
    Deployment deployment = repositoryService
        .createDeployment(application.getReference())
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, createProcessWithServiceTask(PROCESS_KEY))
        .deploy();

    // then
    UserOperationLogEntry userOperationLogEntry = historyService.createUserOperationLogQuery().singleResult();
    assertNotNull(userOperationLogEntry);

    assertEquals(EntityTypes.DEPLOYMENT, userOperationLogEntry.getEntityType());
    assertEquals(deployment.getId(), userOperationLogEntry.getDeploymentId());

    assertEquals(UserOperationLogEntry.OPERATION_TYPE_CREATE, userOperationLogEntry.getOperationType());

    assertEquals("duplicateFilterEnabled", userOperationLogEntry.getProperty());
    assertNull(userOperationLogEntry.getOrgValue());
    assertFalse(Boolean.valueOf(userOperationLogEntry.getNewValue()));

    assertEquals(USER_ID, userOperationLogEntry.getUserId());

    assertNull(userOperationLogEntry.getJobDefinitionId());
    assertNull(userOperationLogEntry.getProcessInstanceId());
    assertNull(userOperationLogEntry.getProcessDefinitionId());
    assertNull(userOperationLogEntry.getProcessDefinitionKey());
    assertNull(userOperationLogEntry.getCaseInstanceId());
    assertNull(userOperationLogEntry.getCaseDefinitionId());
  }

  public void testPropertyDuplicateFiltering() {
    // given
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);

    // when
    Deployment deployment = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model)
        .enableDuplicateFiltering(false)
        .deploy();

    // then
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    assertEquals(2, query.count());

    // (1): duplicate filter enabled property
    UserOperationLogEntry logDuplicateFilterEnabledProperty = query.property("duplicateFilterEnabled").singleResult();
    assertNotNull(logDuplicateFilterEnabledProperty);

    assertEquals(EntityTypes.DEPLOYMENT, logDuplicateFilterEnabledProperty.getEntityType());
    assertEquals(deployment.getId(), logDuplicateFilterEnabledProperty.getDeploymentId());
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_CREATE, logDuplicateFilterEnabledProperty.getOperationType());

    assertEquals(USER_ID, logDuplicateFilterEnabledProperty.getUserId());

    assertEquals("duplicateFilterEnabled", logDuplicateFilterEnabledProperty.getProperty());
    assertNull(logDuplicateFilterEnabledProperty.getOrgValue());
    assertTrue(Boolean.valueOf(logDuplicateFilterEnabledProperty.getNewValue()));

    // (2): deploy changed only
    UserOperationLogEntry logDeployChangedOnlyProperty = query.property("deployChangedOnly").singleResult();
    assertNotNull(logDeployChangedOnlyProperty);

    assertEquals(EntityTypes.DEPLOYMENT, logDeployChangedOnlyProperty.getEntityType());
    assertEquals(deployment.getId(), logDeployChangedOnlyProperty.getDeploymentId());
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_CREATE, logDeployChangedOnlyProperty.getOperationType());
    assertEquals(USER_ID, logDeployChangedOnlyProperty.getUserId());

    assertEquals("deployChangedOnly", logDeployChangedOnlyProperty.getProperty());
    assertNull(logDeployChangedOnlyProperty.getOrgValue());
    assertFalse(Boolean.valueOf(logDeployChangedOnlyProperty.getNewValue()));

    // (3): operation id
    assertEquals(logDuplicateFilterEnabledProperty.getOperationId(), logDeployChangedOnlyProperty.getOperationId());
  }

  public void testPropertiesDuplicateFilteringAndDeployChangedOnly() {
    // given
    BpmnModelInstance model = createProcessWithServiceTask(PROCESS_KEY);

    // when
    Deployment deployment = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, model)
        .enableDuplicateFiltering(true)
        .deploy();

    // then
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    assertEquals(2, query.count());

    // (1): duplicate filter enabled property
    UserOperationLogEntry logDuplicateFilterEnabledProperty = query.property("duplicateFilterEnabled").singleResult();
    assertNotNull(logDuplicateFilterEnabledProperty);
    assertEquals(EntityTypes.DEPLOYMENT, logDuplicateFilterEnabledProperty.getEntityType());
    assertEquals(deployment.getId(), logDuplicateFilterEnabledProperty.getDeploymentId());
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_CREATE, logDuplicateFilterEnabledProperty.getOperationType());
    assertEquals(USER_ID, logDuplicateFilterEnabledProperty.getUserId());

    assertEquals("duplicateFilterEnabled", logDuplicateFilterEnabledProperty.getProperty());
    assertNull(logDuplicateFilterEnabledProperty.getOrgValue());
    assertTrue(Boolean.valueOf(logDuplicateFilterEnabledProperty.getNewValue()));

    // (2): deploy changed only
    UserOperationLogEntry logDeployChangedOnlyProperty = query.property("deployChangedOnly").singleResult();
    assertNotNull(logDeployChangedOnlyProperty);

    assertEquals(EntityTypes.DEPLOYMENT, logDeployChangedOnlyProperty.getEntityType());
    assertEquals(deployment.getId(), logDeployChangedOnlyProperty.getDeploymentId());
    assertEquals(UserOperationLogEntry.OPERATION_TYPE_CREATE, logDeployChangedOnlyProperty.getOperationType());
    assertEquals(USER_ID, logDeployChangedOnlyProperty.getUserId());

    assertEquals("deployChangedOnly", logDeployChangedOnlyProperty.getProperty());
    assertNull(logDeployChangedOnlyProperty.getOrgValue());
    assertTrue(Boolean.valueOf(logDeployChangedOnlyProperty.getNewValue()));

    // (3): operation id
    assertEquals(logDuplicateFilterEnabledProperty.getOperationId(), logDeployChangedOnlyProperty.getOperationId());
  }

  public void testDeleteDeploymentCascadingShouldKeepCreateUserOperationLog() {
    // given
    Deployment deployment = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, createProcessWithServiceTask(PROCESS_KEY))
        .deploy();

    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_CREATE);

    assertEquals(1, query.count());

    // when
    repositoryService.deleteDeployment(deployment.getId(), true);

    // then
    assertEquals(1, query.count());
  }

  public void testDeleteDeploymentWithoutCascadingShouldKeepCreateUserOperationLog() {
    // given
    Deployment deployment = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, createProcessWithServiceTask(PROCESS_KEY))
        .deploy();

    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_CREATE);

    assertEquals(1, query.count());

    // when
    repositoryService.deleteDeployment(deployment.getId(), false);

    // then
    assertEquals(1, query.count());
  }

  public void testDeleteDeployment() {
    // given
    Deployment deployment = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, createProcessWithServiceTask(PROCESS_KEY))
        .deploy();

    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_DELETE);

    // when
    repositoryService.deleteDeployment(deployment.getId(), false);

    // then
    assertEquals(1, query.count());

    UserOperationLogEntry log = query.singleResult();
    assertNotNull(log);

    assertEquals(EntityTypes.DEPLOYMENT, log.getEntityType());
    assertEquals(deployment.getId(), log.getDeploymentId());

    assertEquals(UserOperationLogEntry.OPERATION_TYPE_DELETE, log.getOperationType());

    assertEquals("cascade", log.getProperty());
    assertNull(log.getOrgValue());
    assertFalse(Boolean.valueOf(log.getNewValue()));

    assertEquals(USER_ID, log.getUserId());

    assertNull(log.getJobDefinitionId());
    assertNull(log.getProcessInstanceId());
    assertNull(log.getProcessDefinitionId());
    assertNull(log.getProcessDefinitionKey());
    assertNull(log.getCaseInstanceId());
    assertNull(log.getCaseDefinitionId());
  }

  public void testDeleteDeploymentCascading() {
    // given
    Deployment deployment = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, createProcessWithServiceTask(PROCESS_KEY))
        .deploy();

    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_DELETE);

    // when
    repositoryService.deleteDeployment(deployment.getId(), true);

    // then
    assertEquals(1, query.count());

    UserOperationLogEntry log = query.singleResult();
    assertNotNull(log);

    assertEquals(EntityTypes.DEPLOYMENT, log.getEntityType());
    assertEquals(deployment.getId(), log.getDeploymentId());

    assertEquals(UserOperationLogEntry.OPERATION_TYPE_DELETE, log.getOperationType());

    assertEquals("cascade", log.getProperty());
    assertNull(log.getOrgValue());
    assertTrue(Boolean.valueOf(log.getNewValue()));

    assertEquals(USER_ID, log.getUserId());

    assertNull(log.getJobDefinitionId());
    assertNull(log.getProcessInstanceId());
    assertNull(log.getProcessDefinitionId());
    assertNull(log.getProcessDefinitionKey());
    assertNull(log.getCaseInstanceId());
    assertNull(log.getCaseDefinitionId());
  }


  public void testDeleteProcessDefinitionCascadingShouldKeepCreateUserOperationLog() {
    // given
    Deployment deployment = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, createProcessWithServiceTask(PROCESS_KEY))
        .deploy();

    ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery()
                                                 .deploymentId(deployment.getId())
                                                 .singleResult();

    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_CREATE);

    assertEquals(1, query.count());

    // when
    repositoryService.deleteProcessDefinition(procDef.getId(), true);

    // then
    assertEquals(1, query.count());
  }

  public void testDeleteProcessDefinitiontWithoutCascadingShouldKeepCreateUserOperationLog() {
    // given
    Deployment deployment = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, createProcessWithServiceTask(PROCESS_KEY))
        .deploy();

    ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery()
                                                 .deploymentId(deployment.getId())
                                                 .singleResult();

    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_CREATE);

    assertEquals(1, query.count());

    // when
    repositoryService.deleteProcessDefinition(procDef.getId());

    // then
    assertEquals(1, query.count());
  }

  public void testDeleteProcessDefinition() {
    // given
    Deployment deployment = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, createProcessWithServiceTask(PROCESS_KEY))
        .deploy();

    ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery()
                                                 .deploymentId(deployment.getId())
                                                 .singleResult();

    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_DELETE);

    // when
    repositoryService.deleteProcessDefinition(procDef.getId(), false);

    // then
    assertEquals(1, query.count());

    UserOperationLogEntry log = query.singleResult();
    assertNotNull(log);

    assertEquals(EntityTypes.PROCESS_DEFINITION, log.getEntityType());
    assertEquals(procDef.getId(), log.getProcessDefinitionId());
    assertEquals(procDef.getKey(), log.getProcessDefinitionKey());
    assertEquals(deployment.getId(), log.getDeploymentId());

    assertEquals(UserOperationLogEntry.OPERATION_TYPE_DELETE, log.getOperationType());

    assertEquals("cascade", log.getProperty());
    assertFalse(Boolean.valueOf(log.getOrgValue()));
    assertFalse(Boolean.valueOf(log.getNewValue()));

    assertEquals(USER_ID, log.getUserId());

    assertNull(log.getJobDefinitionId());
    assertNull(log.getProcessInstanceId());
    assertNull(log.getCaseInstanceId());
    assertNull(log.getCaseDefinitionId());
  }

  public void testDeleteProcessDefinitionCascading() {
    // given
    Deployment deployment = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addModelInstance(RESOURCE_NAME, createProcessWithServiceTask(PROCESS_KEY))
        .deploy();

    ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery()
                                                 .deploymentId(deployment.getId())
                                                 .singleResult();

    UserOperationLogQuery query = historyService
        .createUserOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_DELETE);

    // when
    repositoryService.deleteProcessDefinition(procDef.getId(), true);

    // then
    assertEquals(1, query.count());

    UserOperationLogEntry log = query.singleResult();
    assertNotNull(log);

    assertEquals(EntityTypes.PROCESS_DEFINITION, log.getEntityType());
    assertEquals(procDef.getId(), log.getProcessDefinitionId());
    assertEquals(procDef.getKey(), log.getProcessDefinitionKey());
    assertEquals(deployment.getId(), log.getDeploymentId());

    assertEquals(UserOperationLogEntry.OPERATION_TYPE_DELETE, log.getOperationType());

    assertEquals("cascade", log.getProperty());
    assertFalse(Boolean.valueOf(log.getOrgValue()));
    assertTrue(Boolean.valueOf(log.getNewValue()));

    assertEquals(USER_ID, log.getUserId());

    assertNull(log.getJobDefinitionId());
    assertNull(log.getProcessInstanceId());
    assertNull(log.getCaseInstanceId());
    assertNull(log.getCaseDefinitionId());
  }

  protected BpmnModelInstance createProcessWithServiceTask(String key) {
    return Bpmn.createExecutableProcess(key)
      .startEvent()
      .serviceTask()
        .camundaExpression("${true}")
      .endEvent()
    .done();
  }

}
