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
package org.camunda.bpm.engine.test.api.authorization;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.ALL;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions.SUSPEND_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.ProcessInstancePermissions;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.RepositoryServiceImpl;
import org.camunda.bpm.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.camunda.bpm.engine.repository.CalledProcessDefinition;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DiagramLayout;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class ProcessDefinitionAuthorizationTest extends AuthorizationTest {

  protected static final String ONE_TASK_PROCESS_KEY = "oneTaskProcess";
  protected static final String TWO_TASKS_PROCESS_KEY = "twoTasksProcess";

  @Before
  public void setUp() throws Exception {
    testRule.deploy(
        "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml");
    super.setUp();
  }

  @Test
  public void testQueryWithoutAuthorization() {
    // given
    // given user is not authorized to read any process definition

    // when
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testQueryWithReadPermissionOnAnyProcessDefinition() {
    // given
    // given user gets read permission on any process definition
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ);

    // when
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    // then
    verifyQueryResults(query, 2);
  }

  @Test
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

  @Test
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

  @Test
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

  @Test
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

  @Test
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
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
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

  @Test
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
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
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

  @Test
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
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
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

  @Test
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
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
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

  @Test
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
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
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

  @Test
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
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
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

  @Test
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
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(ProcessDefinitionPermissions.SUSPEND.getName(), message);
      testRule.assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
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

  @Test
  public void testSuspendProcessDefinitionByIdWithSuspendPermission() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, ProcessDefinitionPermissions.SUSPEND);

    // when
    repositoryService.suspendProcessDefinitionById(processDefinitionId);

    // then
    ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    assertTrue(definition.isSuspended());
  }

  // activate process definition by id ///////////////////////////////////////////

  @Test
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
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(ProcessDefinitionPermissions.SUSPEND.getName(), message);
      testRule.assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
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


  @Test
  public void testActivateProcessDefinitionByIdWithSuspendPermission() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    suspendProcessDefinitionById(processDefinitionId);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, ProcessDefinitionPermissions.SUSPEND);

    // when
    repositoryService.activateProcessDefinitionById(processDefinitionId);

    // then
    ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    assertFalse(definition.isSuspended());
  }

  // suspend process definition by id including instances ///////////////////////////////////////////

  @Test
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
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(ProcessInstancePermissions.SUSPEND.getName(), message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(SUSPEND_INSTANCE.getName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testSuspendProcessDefinitionByIdIncludingInstancesWithUpdatePermissionOnProcessInstance() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE, ProcessDefinitionPermissions.SUSPEND);

    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE, ProcessInstancePermissions.SUSPEND);

    try {
      // when
      repositoryService.suspendProcessDefinitionById(processDefinitionId, true, null);
      fail("Exception expected: It should not be possible to suspend the process definition");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(ProcessInstancePermissions.SUSPEND.getName(), message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(SUSPEND_INSTANCE.getName(), message);
      testRule.assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
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

  @Test
  public void testSuspendProcessDefinitionByIdIncludingInstancesWithSuspendPermissionOnAnyProcessInstance() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, ProcessDefinitionPermissions.SUSPEND);

    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, ProcessInstancePermissions.SUSPEND);

    // when
    repositoryService.suspendProcessDefinitionById(processDefinitionId, true, null);

    // then
    verifyProcessDefinitionSuspendedByKeyIncludingInstances();
  }

  @Test
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

  @Test
  public void testSuspendProcessDefinitionByIdIncludingInstancesWithUpdateAndSuspendInstancePermissionOnProcessDefinition() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE, SUSPEND_INSTANCE);

    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    // when
    repositoryService.suspendProcessDefinitionById(processDefinitionId, true, null);

    // then
    verifyProcessDefinitionSuspendedByKeyIncludingInstances();
  }

  @Test
  public void testSuspendProcessDefinitionByIdIncludingInstancesWithSuspendAndSuspendInstancePermissionOnProcessDefinition() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, ProcessDefinitionPermissions.SUSPEND, SUSPEND_INSTANCE);

    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    // when
    repositoryService.suspendProcessDefinitionById(processDefinitionId, true, null);

    // then
    verifyProcessDefinitionSuspendedByKeyIncludingInstances();
  }

  @Test
  public void testSuspendProcessDefinitionByIdIncludingInstancesWithSuspendAndUpdateInstancePermissionOnProcessDefinition() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, ProcessDefinitionPermissions.SUSPEND, UPDATE_INSTANCE);

    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    // when
    repositoryService.suspendProcessDefinitionById(processDefinitionId, true, null);

    // then
    verifyProcessDefinitionSuspendedByKeyIncludingInstances();
  }

  // activate process definition by id including instances ///////////////////////////////////////////

  @Test
  public void testActivateProcessDefinitionByIdIncludingInstancesWithoutAuthorization() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    suspendProcessDefinitionById(processDefinitionId);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE, ProcessDefinitionPermissions.SUSPEND);

    try {
      // when
      repositoryService.activateProcessDefinitionById(processDefinitionId, true, null);
      fail("Exception expected: It should not be possible to activate the process definition");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(ProcessInstancePermissions.SUSPEND.getName(), message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(SUSPEND_INSTANCE.getName(), message);
      testRule.assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
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
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(ProcessInstancePermissions.SUSPEND.getName(), message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(SUSPEND_INSTANCE.getName(), message);
      testRule.assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
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

  @Test
  public void testActivateProcessDefinitionByIdIncludingInstancesWithSuspendPermissionOnAnyProcessInstance() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, ProcessInstancePermissions.SUSPEND);

    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    suspendProcessDefinitionById(processDefinitionId);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, ProcessDefinitionPermissions.SUSPEND);

    // when
    repositoryService.activateProcessDefinitionById(processDefinitionId, true, null);

    // then
    verifyProcessDefinitionActivatedByKeyIncludingInstances();
  }

  @Test
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

  @Test
  public void testActivateProcessDefinitionByIdIncludingInstancesWithSuspendAndSuspendInstancePermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    suspendProcessDefinitionById(processDefinitionId);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, ProcessDefinitionPermissions.SUSPEND, SUSPEND_INSTANCE);

    // when
    repositoryService.activateProcessDefinitionById(processDefinitionId, true, null);

    // then
    verifyProcessDefinitionActivatedByKeyIncludingInstances();
  }

  @Test
  public void testActivateProcessDefinitionByIdIncludingInstancesWithSuspendAndUpdateInstancePermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    suspendProcessDefinitionById(processDefinitionId);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, ProcessDefinitionPermissions.SUSPEND, UPDATE_INSTANCE);

    // when
    repositoryService.activateProcessDefinitionById(processDefinitionId, true, null);

    // then
    verifyProcessDefinitionActivatedByKeyIncludingInstances();
  }

  @Test
  public void testActivateProcessDefinitionByIdIncludingInstancesWithUpdateAndSuspendInstancePermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    String processDefinitionId = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY).getId();
    suspendProcessDefinitionById(processDefinitionId);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE, SUSPEND_INSTANCE);

    // when
    repositoryService.activateProcessDefinitionById(processDefinitionId, true, null);

    // then
    verifyProcessDefinitionActivatedByKeyIncludingInstances();
  }

  // suspend process definition by key ///////////////////////////////////////////

  @Test
  public void testSuspendProcessDefinitionByKeyWithoutAuthorization() {
    // given

    try {
      // when
      repositoryService.suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
      fail("Exception expected: It should not be possible to suspend the process definition");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(ProcessDefinitionPermissions.SUSPEND.getName(), message);
      testRule.assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testSuspendProcessDefinitionByKey() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE);

    // when
    repositoryService.suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);

    // then
    ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    assertTrue(definition.isSuspended());
  }

  @Test
  public void testSuspendProcessDefinitionByKeyWithSuspendPermission() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, ProcessDefinitionPermissions.SUSPEND);

    // when
    repositoryService.suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);

    // then
    ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    assertTrue(definition.isSuspended());
  }

  // activate process definition by id ///////////////////////////////////////////

  @Test
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
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(ProcessDefinitionPermissions.SUSPEND.getName(), message);
      testRule.assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
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

  @Test
  public void testActivateProcessDefinitionByKeyWithSuspendPermission() {
    // given
    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, ProcessDefinitionPermissions.SUSPEND);

    // when
    repositoryService.activateProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);

    // then
    ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    assertFalse(definition.isSuspended());
  }

  // suspend process definition by key including instances ///////////////////////////////////////////

  @Test
  public void testSuspendProcessDefinitionByKeyIncludingInstancesWithoutAuthorization() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE, ProcessDefinitionPermissions.SUSPEND);

    try {
      // when
      repositoryService.suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, null);
      fail("Exception expected: It should not be possible to suspend the process definition");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(ProcessDefinitionPermissions.SUSPEND.getName(), message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(SUSPEND_INSTANCE.getName(), message);
      testRule.assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testSuspendProcessDefinitionByKeyIncludingInstancesWithUpdatePermissionOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE, ProcessDefinitionPermissions.SUSPEND);

    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE, ProcessInstancePermissions.SUSPEND);

    try {
      // when
      repositoryService.suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, null);
      fail("Exception expected: It should not be possible to suspend the process definition");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(ProcessInstancePermissions.SUSPEND.getName(), message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(SUSPEND_INSTANCE.getName(), message);
      testRule.assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
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

  @Test
  public void testSuspendProcessDefinitionByKeyIncludingInstancesWithSuspendPermissionOnAnyProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, ProcessDefinitionPermissions.SUSPEND);

    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, ProcessInstancePermissions.SUSPEND);

    // when
    repositoryService.suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, null);

    // then
    verifyProcessDefinitionSuspendedByKeyIncludingInstances();
  }

  @Test
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

  @Test
  public void testSuspendProcessDefinitionByKeyIncludingInstancesWithSuspendAndSuspendInstancePermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, ProcessDefinitionPermissions.SUSPEND, SUSPEND_INSTANCE);

    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    // when
    repositoryService.suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, null);

    // then
    verifyProcessDefinitionSuspendedByKeyIncludingInstances();
  }


  @Test
  public void testSuspendProcessDefinitionByKeyIncludingInstancesWithSuspendAndUpdateInstancePermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, ProcessDefinitionPermissions.SUSPEND, UPDATE_INSTANCE);

    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    // when
    repositoryService.suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, null);

    // then
    verifyProcessDefinitionSuspendedByKeyIncludingInstances();
  }


  @Test
  public void testSuspendProcessDefinitionByKeyIncludingInstancesWithUpdateAndSuspendInstancePermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE, SUSPEND_INSTANCE);

    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    // when
    repositoryService.suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, null);

    // then
    verifyProcessDefinitionSuspendedByKeyIncludingInstances();
  }

  // activate process definition by key including instances ///////////////////////////////////////////

  @Test
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
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(ProcessInstancePermissions.SUSPEND.getName(), message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(SUSPEND_INSTANCE.getName(), message);
      testRule.assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testActivateProcessDefinitionByKeyIncludingInstancesWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE, ProcessInstancePermissions.SUSPEND);

    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE, ProcessDefinitionPermissions.SUSPEND);

    try {
      // when
      repositoryService.activateProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, null);
      fail("Exception expected: It should not be possible to activate the process definition");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(ProcessInstancePermissions.SUSPEND.getName(), message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(SUSPEND_INSTANCE.getName(), message);
      testRule.assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
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

  @Test
  public void testActivateProcessDefinitionByKeyIncludingInstancesWithSuspendPermissionOnAnyProcessInstance() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, ProcessInstancePermissions.SUSPEND);

    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, ProcessDefinitionPermissions.SUSPEND);

    // when
    repositoryService.activateProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, null);

    // then
    verifyProcessDefinitionActivatedByKeyIncludingInstances();
  }

  @Test
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

  @Test
  public void testActivateProcessDefinitionByKeyIncludingInstancesWithSuspendAndSuspendInstancePermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, ProcessDefinitionPermissions.SUSPEND, SUSPEND_INSTANCE);

    // when
    repositoryService.activateProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, null);

    // then
    verifyProcessDefinitionActivatedByKeyIncludingInstances();
  }


  @Test
  public void testActivateProcessDefinitionByKeyIncludingInstancesWithSuspendAndUpdateInstancePermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, ProcessDefinitionPermissions.SUSPEND, UPDATE_INSTANCE);

    // when
    repositoryService.activateProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, null);

    // then
    verifyProcessDefinitionActivatedByKeyIncludingInstances();
  }


  @Test
  public void testActivateProcessDefinitionByKeyIncludingInstancesWithUpdateAndSuspendInstancePermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    suspendProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, UPDATE, SUSPEND_INSTANCE);

    // when
    repositoryService.activateProcessDefinitionByKey(ONE_TASK_PROCESS_KEY, true, null);

    // then
    verifyProcessDefinitionActivatedByKeyIncludingInstances();
  }


  // update history time to live ///////////////////////////////////////////

  @Test
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

  @Test
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
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(ONE_TASK_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }

  }

  // startable in tasklist ///////////////////////////////////////////

  @Test
  public void testStartableInTasklist() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ, CREATE_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, "*", userId, CREATE);
    final ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);

    // when
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().startablePermissionCheck().startableInTasklist().list();
    // then
    assertNotNull(processDefinitions);
    assertEquals(1, repositoryService.createProcessDefinitionQuery().startablePermissionCheck().startableInTasklist().count());
    assertEquals(definition.getId(), processDefinitions.get(0).getId());
    assertTrue(processDefinitions.get(0).isStartableInTasklist());
  }

  @Test
  public void testStartableInTasklistReadAllProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, "*", userId, READ);
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, CREATE_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, "*", userId, CREATE);
    final ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);

    // when
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().startablePermissionCheck().startableInTasklist().list();
    // then
    assertNotNull(processDefinitions);
    assertEquals(1, repositoryService.createProcessDefinitionQuery().startablePermissionCheck().startableInTasklist().count());
    assertEquals(definition.getId(), processDefinitions.get(0).getId());
    assertTrue(processDefinitions.get(0).isStartableInTasklist());
  }

  @Test
  public void testStartableInTasklistWithoutCreateInstancePerm() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, READ);
    createGrantAuthorization(PROCESS_INSTANCE, "*", userId, CREATE);
    selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);

    // when
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().startablePermissionCheck().startableInTasklist().list();
    // then
    assertNotNull(processDefinitions);
    assertEquals(0, processDefinitions.size());
  }

  @Test
  public void testStartableInTasklistWithoutReadDefPerm() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY, userId, CREATE_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, "*", userId, CREATE);
    selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);

    // when
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().startablePermissionCheck().startableInTasklist().list();
    // then
    assertNotNull(processDefinitions);
    assertEquals(0, processDefinitions.size());
  }

  @Test
  public void testStartableInTasklistWithoutCreatePerm() {
    // given
    selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);

    // when
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().startablePermissionCheck().startableInTasklist().list();
    // then
    assertNotNull(processDefinitions);
    assertEquals(0, processDefinitions.size());
  }

  @Test
  public void shouldNotResolveUnauthorizedCalledProcessDefinitions() {
    Deployment deployment = createDeployment("test",
      "org/camunda/bpm/engine/test/api/repository/call-activities-with-references.bpmn",
      "org/camunda/bpm/engine/test/api/repository/first-process.bpmn20.xml");
    try {
      //given
      String parentKey = "TestCallActivitiesWithReferences";
      createGrantAuthorization(PROCESS_DEFINITION, parentKey, userId, READ);
      ProcessDefinition parentDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey(parentKey).singleResult();

      //when
      Collection<CalledProcessDefinition> mappings = repositoryService
        .getStaticCalledProcessDefinitions(parentDefinition.getId());

      //then
      assertTrue(mappings.isEmpty());
    } finally {
      deleteDeployment(deployment.getId());
    }

  }

  // helper /////////////////////////////////////////////////////////////////////

  protected void verifyQueryResults(ProcessDefinitionQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

  protected void verifyProcessDefinitionSuspendedByKeyIncludingInstances() {
    ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    assertTrue(definition.isSuspended());

    ProcessInstance instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  protected void verifyProcessDefinitionActivatedByKeyIncludingInstances() {
    ProcessDefinition definition = selectProcessDefinitionByKey(ONE_TASK_PROCESS_KEY);
    assertFalse(definition.isSuspended());

    ProcessInstance instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

}
