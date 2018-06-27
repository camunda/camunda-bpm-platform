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
package org.camunda.bpm.engine.test.api.authorization;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.ALL;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.DECISION_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;

import java.io.InputStream;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.camunda.bpm.engine.repository.DiagramLayout;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 * @author Roman Smirnov
 *
 */
public class ProcessDefinitionAuthorizationTest extends AuthorizationTest {

  protected static final String ONE_TASK_PROCESS_KEY = "oneTaskProcess";
  protected static final String TWO_TASKS_PROCESS_KEY = "twoTasksProcess";

  protected String deploymentId;

  @Override
  public void setUp() throws Exception {
    deploymentId = createDeployment(null,
        "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml").getId();
    super.setUp();
  }

  @Override
  public void tearDown() {
    super.tearDown();
    deleteDeployment(deploymentId);
  }

  public void testQueryWithoutAuthorization() {
    // given
    // given user is not authorized to read any process definition

    // when
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testQueryWithReadPermissionOnAnyProcessDefinition() {
    // given
    // given user gets read permission on any process definition
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ);

    // when
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    // then
    verifyQueryResults(query, 2);
  }

  public void testQueryWithMultiple() {
    // given
    // given user gets read permission on any process definition
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ);

    // when
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    // then
    verifyQueryResults(query, 2);
  }

  public void testQueryWithReadPermissionOnOneTaskProcess() {
    // given
    // given user gets read permission on "oneTaskProcess" process definition
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ);

    // when
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    // then
    verifyQueryResults(query, 1);

    ProcessDefinition definition = query.singleResult();
    assertNotNull(definition);
    assertEquals(ONE_TASK_PROCESS_KEY, definition.getKey());
  }

  public void testQueryWithRevokedReadPermission() {
    // given
    // given user gets all permissions on any process definition
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, ALL);

    Authorization authorization = createRevokeAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY);
    authorization.setUserId(userId);
    authorization.removePermission(READ);
    saveAuthorization(authorization);

    // when
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    // then
    verifyQueryResults(query, 1);

    ProcessDefinition definition = query.singleResult();
    assertNotNull(definition);
    assertEquals(TWO_TASKS_PROCESS_KEY, definition.getKey());
  }

  public void testQueryWithGroupAuthorizationRevokedReadPermission() {
    // given
    // given user gets all permissions on any process definition
    Authorization authorization = createGrantAuthorization(PROCESS_DEFINITION, ANY);
    authorization.setGroupId(groupId);
    authorization.addPermission(ALL);
    saveAuthorization(authorization);

    authorization = createRevokeAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY);
    authorization.setGroupId(groupId);
    authorization.removePermission(READ);
    saveAuthorization(authorization);

    // when
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    // then
    verifyQueryResults(query, 1);

    ProcessDefinition definition = query.singleResult();
    assertNotNull(definition);
    assertEquals(TWO_TASKS_PROCESS_KEY, definition.getKey());
  }

  // get process definition /////////////////////////////////////////////////////

  public void testGetProcessDefinitionWithoutAuthorizations() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();

    try {
      // when
      repositoryService.getProcessDefinition(processDefinitionId);
      fail("Exception expected: It should not be possible to get the process definition");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testGetProcessDefinition() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ);

    // when
    ProcessDefinition definition = repositoryService.getProcessDefinition(processDefinitionId);

    // then
    assertNotNull(definition);
  }

  // get deployed process definition /////////////////////////////////////////////////////

  public void testGetDeployedProcessDefinitionWithoutAuthorizations() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();

    try {
      // when
      ((RepositoryServiceImpl)repositoryService).getDeployedProcessDefinition(processDefinitionId);
      fail("Exception expected: It should not be possible to get the process definition");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testGetDeployedProcessDefinition() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ);

    // when
    ReadOnlyProcessDefinition definition = ((RepositoryServiceImpl)repositoryService).getDeployedProcessDefinition(processDefinitionId);

    // then
    assertNotNull(definition);
  }

  // get process diagram /////////////////////////////////////////////////////

  public void testGetProcessDiagramWithoutAuthorizations() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();

    try {
      // when
      repositoryService.getProcessDiagram(processDefinitionId);
      fail("Exception expected: It should not be possible to get the process diagram");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testGetProcessDiagram() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ);

    // when
    InputStream stream = repositoryService.getProcessDiagram(processDefinitionId);

    // then
    // no process diagram deployed
    assertNull(stream);
  }

  // get process model /////////////////////////////////////////////////////

  public void testGetProcessModelWithoutAuthorizations() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();

    try {
      // when
      repositoryService.getProcessModel(processDefinitionId);
      fail("Exception expected: It should not be possible to get the process model");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testGetProcessModel() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ);

    // when
    InputStream stream = repositoryService.getProcessModel(processDefinitionId);

    // then
    assertNotNull(stream);
  }

  // get bpmn model instance /////////////////////////////////////////////////////

  public void testGetBpmnModelInstanceWithoutAuthorizations() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();

    try {
      // when
      repositoryService.getBpmnModelInstance(processDefinitionId);
      fail("Exception expected: It should not be possible to get the bpmn model instance");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testGetBpmnModelInstance() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ);

    // when
    BpmnModelInstance modelInstance = repositoryService.getBpmnModelInstance(processDefinitionId);

    // then
    assertNotNull(modelInstance);
  }

  // get process diagram layout /////////////////////////////////////////////////

  public void testGetProcessDiagramLayoutWithoutAuthorization() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();

    try {
      // when
      repositoryService.getProcessDiagramLayout(processDefinitionId);
      fail("Exception expected: It should not be possible to get the process diagram layout");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testGetProcessDiagramLayout() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ);

    // when
    DiagramLayout diagramLayout = repositoryService.getProcessDiagramLayout(processDefinitionId);

    // then
    // no process diagram deployed
    assertNull(diagramLayout);
  }

  // suspend process definition by id ///////////////////////////////////////////

  public void testSuspendProcessDefinitionByIdWithoutAuthorization() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();

    try {
      // when
      repositoryService.suspendProcessDefinitionById(processDefinitionId);
      fail("Exception expected: It should not be possible to suspend the process definition");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSuspendProcessDefinitionById() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE);

    // when
    repositoryService.suspendProcessDefinitionById(processDefinitionId);

    // then
    ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    assertTrue(definition.isSuspended());
  }

  // activate process definition by id ///////////////////////////////////////////

  public void testActivateProcessDefinitionByIdWithoutAuthorization() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    suspendProcessDefinitionById(processDefinitionId);

    try {
      // when
      repositoryService.activateProcessDefinitionById(processDefinitionId);
      fail("Exception expected: It should not be possible to activate the process definition");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testActivateProcessDefinitionById() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    suspendProcessDefinitionById(processDefinitionId);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE);

    // when
    repositoryService.activateProcessDefinitionById(processDefinitionId);

    // then
    ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    assertFalse(definition.isSuspended());
  }

  // suspend process definition by id including instances ///////////////////////////////////////////

  public void testSuspendProcessDefinitionByIdIncludingInstancesWithoutAuthorization() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE);

    try {
      // when
      repositoryService.suspendProcessDefinitionById(processDefinitionId, true, null);
      fail("Exception expected: It should not be possible to suspend the process definition");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSuspendProcessDefinitionByIdIncludingInstancesWithUpdatePermissionOnProcessInstance() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE);

    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    try {
      // when
      repositoryService.suspendProcessDefinitionById(processDefinitionId, true, null);
      fail("Exception expected: It should not be possible to suspend the process definition");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSuspendProcessDefinitionByIdIncludingInstancesWithUpdatePermissionOnAnyProcessInstance() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE);

    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    repositoryService.suspendProcessDefinitionById(processDefinitionId, true, null);

    // then
    ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    assertTrue(definition.isSuspended());

    ProcessInstance instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  public void testSuspendProcessDefinitionByIdIncludingInstancesWithUpdateInstancePermissionOnProcessDefinition() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE, UPDATE_INSTANCE);

    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    // when
    repositoryService.suspendProcessDefinitionById(processDefinitionId, true, null);

    // then
    ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    assertTrue(definition.isSuspended());

    ProcessInstance instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  // activate process definition by id including instances ///////////////////////////////////////////

  public void testActivateProcessDefinitionByIdIncludingInstancesWithoutAuthorization() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    suspendProcessDefinitionById(processDefinitionId);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE);

    try {
      // when
      repositoryService.activateProcessDefinitionById(processDefinitionId, true, null);
      fail("Exception expected: It should not be possible to activate the process definition");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testActivateProcessDefinitionByIdIncludingInstancesWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    suspendProcessDefinitionById(processDefinitionId);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE);

    try {
      // when
      repositoryService.activateProcessDefinitionById(processDefinitionId, true, null);
      fail("Exception expected: It should not be possible to activate the process definition");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testActivateProcessDefinitionByIdIncludingInstancesWithUpdatePermissionOnAnyProcessInstance() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    suspendProcessDefinitionById(processDefinitionId);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE);

    // when
    repositoryService.activateProcessDefinitionById(processDefinitionId, true, null);

    // then
    ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    assertFalse(definition.isSuspended());

    ProcessInstance instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  public void testActivateProcessDefinitionByIdIncludingInstancesWithUpdateInstancePermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    suspendProcessDefinitionById(processDefinitionId);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE, UPDATE_INSTANCE);

    // when
    repositoryService.activateProcessDefinitionById(processDefinitionId, true, null);

    // then
    ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    assertFalse(definition.isSuspended());

    ProcessInstance instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  // suspend process definition by key ///////////////////////////////////////////

  public void testSuspendProcessDefinitionByKeyWithoutAuthorization() {
    // given

    try {
      // when
      repositoryService.suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
      fail("Exception expected: It should not be possible to suspend the process definition");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSuspendProcessDefinitionByKey() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE);

    // when
    repositoryService.suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);

    // then
    ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    assertTrue(definition.isSuspended());
  }

  // activate process definition by id ///////////////////////////////////////////

  public void testActivateProcessDefinitionByKeyWithoutAuthorization() {
    // given
    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);

    try {
      // when
      repositoryService.activateProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
      fail("Exception expected: It should not be possible to activate the process definition");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testActivateProcessDefinitionByKey() {
    // given
    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE);

    // when
    repositoryService.activateProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);

    // then
    ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    assertFalse(definition.isSuspended());
  }

  // suspend process definition by key including instances ///////////////////////////////////////////

  public void testSuspendProcessDefinitionByKeyIncludingInstancesWithoutAuthorization() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE);

    try {
      // when
      repositoryService.suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, null);
      fail("Exception expected: It should not be possible to suspend the process definition");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSuspendProcessDefinitionByKeyIncludingInstancesWithUpdatePermissionOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE);

    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    try {
      // when
      repositoryService.suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, null);
      fail("Exception expected: It should not be possible to suspend the process definition");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSuspendProcessDefinitionByKeyIncludingInstancesWithUpdatePermissionOnAnyProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE);

    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    repositoryService.suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, null);

    // then
    ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    assertTrue(definition.isSuspended());

    ProcessInstance instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  public void testSuspendProcessDefinitionByKeyIncludingInstancesWithUpdateInstancePermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE, UPDATE_INSTANCE);

    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    // when
    repositoryService.suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, null);

    // then
    ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    assertTrue(definition.isSuspended());

    ProcessInstance instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  // activate process definition by key including instances ///////////////////////////////////////////

  public void testActivateProcessDefinitionByKeyIncludingInstancesWithoutAuthorization() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE);

    try {
      // when
      repositoryService.activateProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, null);
      fail("Exception expected: It should not be possible to activate the process definition");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testActivateProcessDefinitionByKeyIncludingInstancesWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE);

    try {
      // when
      repositoryService.activateProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, null);
      fail("Exception expected: It should not be possible to activate the process definition");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testActivateProcessDefinitionByKeyIncludingInstancesWithUpdatePermissionOnAnyProcessInstance() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE);

    // when
    repositoryService.activateProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, null);

    // then
    ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    assertFalse(definition.isSuspended());

    ProcessInstance instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  public void testActivateProcessDefinitionByKeyIncludingInstancesWithUpdateInstancePermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE, UPDATE_INSTANCE);

    // when
    repositoryService.activateProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, null);

    // then
    ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    assertFalse(definition.isSuspended());

    ProcessInstance instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }


  public void testProcessDefinitionUpdateTimeToLive() {

    // given
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE);
    ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);

    // when
    repositoryService.updateProcessDefinitionHistoryTimeToLive(definition.getId(), 6);

    // then
    definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    assertEquals(6, definition.getHistoryTimeToLive().intValue());

  }

  public void testDecisionDefinitionUpdateTimeToLiveWithoutAuthorizations() {
    //given
    ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    try {
      //when
      repositoryService.updateProcessDefinitionHistoryTimeToLive(definition.getId(), 6);
      fail("Exception expected");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }

  }

  // helper /////////////////////////////////////////////////////////////////////

  protected void verifyQueryResults(ProcessDefinitionQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

}
