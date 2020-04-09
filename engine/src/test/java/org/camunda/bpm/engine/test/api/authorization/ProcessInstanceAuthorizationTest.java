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
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions.READ_INSTANCE_VARIABLE;
import static org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions.SUSPEND_INSTANCE;
import static org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions.UPDATE_INSTANCE_VARIABLE;
import static org.camunda.bpm.engine.authorization.ProcessInstancePermissions.SUSPEND;
import static org.camunda.bpm.engine.authorization.ProcessInstancePermissions.UPDATE_VARIABLE;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.TASK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class ProcessInstanceAuthorizationTest extends AuthorizationTest {

  protected static final String PROCESS_KEY = "oneTaskProcess";
  protected static final String MESSAGE_START_PROCESS_KEY = "messageStartProcess";
  protected static final String MESSAGE_BOUNDARY_PROCESS_KEY = "messageBoundaryProcess";
  protected static final String SIGNAL_BOUNDARY_PROCESS_KEY = "signalBoundaryProcess";
  protected static final String SIGNAL_START_PROCESS_KEY = "signalStartProcess";
  protected static final String THROW_WARNING_SIGNAL_PROCESS_KEY = "throwWarningSignalProcess";
  protected static final String THROW_ALERT_SIGNAL_PROCESS_KEY = "throwAlertSignalProcess";

  protected boolean ensureSpecificVariablePermission;

  @Before
  public void setUp() throws Exception {
    testRule.deploy(
        "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/messageStartEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/messageBoundaryEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/signalBoundaryEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/signalStartEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/throwWarningSignalEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/throwAlertSignalEventProcess.bpmn20.xml"
        );
    ensureSpecificVariablePermission = processEngineConfiguration.isEnforceSpecificVariablePermission();
    super.setUp();
  }

  @After
  public void tearDown() {
    super.tearDown();
    processEngineConfiguration.setEnforceSpecificVariablePermission(ensureSpecificVariablePermission);
  }

  // process instance query //////////////////////////////////////////////////////////

  @Test
  public void testSimpleQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);

    // when
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testSimpleQueryWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    // then
    verifyQueryResults(query, 1);

    ProcessInstance instance = query.singleResult();
    assertNotNull(instance);
    assertEquals(processInstanceId, instance.getId());
  }

  @Test
  public void testSimpleQueryWithMultiple() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  @Test
  public void testSimpleQueryWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    // then
    verifyQueryResults(query, 1);

    ProcessInstance instance = query.singleResult();
    assertNotNull(instance);
    assertEquals(processInstanceId, instance.getId());
  }

  @Test
  public void testSimpleQueryWithReadInstancesPermissionOnOneTaskProcess() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    // then
    verifyQueryResults(query, 1);

    ProcessInstance instance = query.singleResult();
    assertNotNull(instance);
    assertEquals(processInstanceId, instance.getId());
  }

  @Test
  public void testSimpleQueryWithReadInstancesPermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    // then
    verifyQueryResults(query, 1);

    ProcessInstance instance = query.singleResult();
    assertNotNull(instance);
    assertEquals(processInstanceId, instance.getId());
  }

  // process instance query (multiple process instances) ////////////////////////

  @Test
  public void testQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);

    // when
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testQueryWithReadPermissionOnProcessInstance() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    // then
    verifyQueryResults(query, 1);

    ProcessInstance instance = query.singleResult();
    assertNotNull(instance);
    assertEquals(processInstanceId, instance.getId());
  }

  @Test
  public void testQueryWithReadPermissionOnAnyProcessInstance() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    // then
    verifyQueryResults(query, 7);
  }

  @Test
  public void testQueryWithReadInstancesPermissionOnOneTaskProcess() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    // then
    verifyQueryResults(query, 3);
  }

  @Test
  public void testQueryWithReadInstancesPermissionOnAnyProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);
    startProcessInstanceByKey(PROCESS_KEY);

    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);
    startProcessInstanceByKey(MESSAGE_START_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    // then
    verifyQueryResults(query, 7);
  }

  // start process instance by key //////////////////////////////////////////////

  @Test
  public void testStartProcessInstanceByKeyWithoutAuthorization() {
    // given
    // no authorization to start a process instance

    try {
      // when
      runtimeService.startProcessInstanceByKey(PROCESS_KEY);
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  @Test
  public void testStartProcessInstanceByKeyWithCreatePermissionOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    try {
      // when
      runtimeService.startProcessInstanceByKey(PROCESS_KEY);
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE_INSTANCE' permission on resource 'oneTaskProcess' of type 'ProcessDefinition'", e.getMessage());
    }
  }

  @Test
  public void testStartProcessInstanceByKeyWithCreateInstancesPermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, CREATE_INSTANCE);

    try {
      // when
      runtimeService.startProcessInstanceByKey(PROCESS_KEY);
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  @Test
  public void testStartProcessInstanceByKey() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, CREATE_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    // when
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);

    // then
    disableAuthorization();
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();
  }

  // start process instance by id //////////////////////////////////////////////

  @Test
  public void testStartProcessInstanceByIdWithoutAuthorization() {
    // given
    // no authorization to start a process instance

    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    try {
      // when
      runtimeService.startProcessInstanceById(processDefinitionId);
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  @Test
  public void testStartProcessInstanceByIdWithCreatePermissionOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    try {
      // when
      runtimeService.startProcessInstanceById(processDefinitionId);
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE_INSTANCE' permission on resource 'oneTaskProcess' of type 'ProcessDefinition'", e.getMessage());
    }
  }

  @Test
  public void testStartProcessInstanceByIdWithCreateInstancesPermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, CREATE_INSTANCE);

    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    try {
      // when
      runtimeService.startProcessInstanceById(processDefinitionId);
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  @Test
  public void testStartProcessInstanceById() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, CREATE_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    // when
    runtimeService.startProcessInstanceById(processDefinitionId);

    // then
    disableAuthorization();
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();
  }

  @Test
  public void testStartProcessInstanceAtActivitiesByKey() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, CREATE_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    // when
    runtimeService.createProcessInstanceByKey(PROCESS_KEY).startBeforeActivity("theTask").execute();

    // then
    disableAuthorization();
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();
  }

  @Test
  public void testStartProcessInstanceAtActivitiesByKeyWithoutAuthorization() {
    // given
    // no authorization to start a process instance

    try {
      // when
      runtimeService.createProcessInstanceByKey(PROCESS_KEY).startBeforeActivity("theTask").execute();
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  @Test
  public void testStartProcessInstanceAtActivitiesByKeyWithCreatePermissionOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    try {
      // when
      runtimeService.createProcessInstanceByKey(PROCESS_KEY).startBeforeActivity("theTask").execute();
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE_INSTANCE' permission on resource 'oneTaskProcess' of type 'ProcessDefinition'", e.getMessage());
    }
  }

  @Test
  public void testStartProcessInstanceAtActivitiesByKeyWithCreateInstancesPermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, CREATE_INSTANCE);

    try {
      // when
      runtimeService.createProcessInstanceByKey(PROCESS_KEY).startBeforeActivity("theTask").execute();
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  @Test
  public void testStartProcessInstanceAtActivitiesById() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, CREATE_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    // when
    runtimeService.createProcessInstanceById(processDefinitionId).startBeforeActivity("theTask").execute();

    // then
    disableAuthorization();
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();
  }

  @Test
  public void testStartProcessInstanceAtActivitiesByIdWithoutAuthorization() {
    // given
    // no authorization to start a process instance

    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    try {
      // when
      runtimeService.createProcessInstanceById(processDefinitionId).startBeforeActivity("theTask").execute();
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  @Test
  public void testStartProcessInstanceAtActivitiesByIdWithCreatePermissionOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    try {
      // when
      runtimeService.createProcessInstanceById(processDefinitionId).startBeforeActivity("theTask").execute();
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE_INSTANCE' permission on resource 'oneTaskProcess' of type 'ProcessDefinition'", e.getMessage());
    }
  }

  @Test
  public void testStartProcessInstanceAtActivitiesByIdWithCreateInstancesPermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, CREATE_INSTANCE);

    String processDefinitionId = selectProcessDefinitionByKey(PROCESS_KEY).getId();

    try {
      // when
      runtimeService.createProcessInstanceById(processDefinitionId).startBeforeActivity("theTask").execute();
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  // start process instance by message //////////////////////////////////////////////

  @Test
  public void testStartProcessInstanceByMessageWithoutAuthorization() {
    // given
    // no authorization to start a process instance

    try {
      // when
      runtimeService.startProcessInstanceByMessage("startInvoiceMessage");
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  @Test
  public void testStartProcessInstanceByMessageWithCreatePermissionOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    try {
      // when
      runtimeService.startProcessInstanceByMessage("startInvoiceMessage");
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE_INSTANCE' permission on resource 'messageStartProcess' of type 'ProcessDefinition'", e.getMessage());
    }
  }

  @Test
  public void testStartProcessInstanceByMessageWithCreateInstancesPermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_START_PROCESS_KEY, userId, CREATE_INSTANCE);

    try {
      // when
      runtimeService.startProcessInstanceByMessage("startInvoiceMessage");
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  @Test
  public void testStartProcessInstanceByMessage() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_START_PROCESS_KEY, userId, CREATE_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    // when
    runtimeService.startProcessInstanceByMessage("startInvoiceMessage");

    // then
    disableAuthorization();
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();
  }

  // start process instance by message and process definition id /////////////////////////////

  @Test
  public void testStartProcessInstanceByMessageAndProcDefIdWithoutAuthorization() {
    // given
    // no authorization to start a process instance

    String processDefinitionId = selectProcessDefinitionByKey(MESSAGE_START_PROCESS_KEY).getId();

    try {
      // when
      runtimeService.startProcessInstanceByMessageAndProcessDefinitionId("startInvoiceMessage", processDefinitionId);
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  @Test
  public void testStartProcessInstanceByMessageAndProcDefIdWithCreatePermissionOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    String processDefinitionId = selectProcessDefinitionByKey(MESSAGE_START_PROCESS_KEY).getId();

    try {
      // when
      runtimeService.startProcessInstanceByMessageAndProcessDefinitionId("startInvoiceMessage", processDefinitionId);
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE_INSTANCE' permission on resource 'messageStartProcess' of type 'ProcessDefinition'", e.getMessage());
    }
  }

  @Test
  public void testStartProcessInstanceByMessageAndProcDefIdWithCreateInstancesPermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_START_PROCESS_KEY, userId, CREATE_INSTANCE);

    String processDefinitionId = selectProcessDefinitionByKey(MESSAGE_START_PROCESS_KEY).getId();

    try {
      // when
      runtimeService.startProcessInstanceByMessageAndProcessDefinitionId("startInvoiceMessage", processDefinitionId);
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  @Test
  public void testStartProcessInstanceByMessageAndProcDefId() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_START_PROCESS_KEY, userId, CREATE_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    String processDefinitionId = selectProcessDefinitionByKey(MESSAGE_START_PROCESS_KEY).getId();

    // when
    runtimeService.startProcessInstanceByMessageAndProcessDefinitionId("startInvoiceMessage", processDefinitionId);

    // then
    disableAuthorization();
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();
  }

  // delete process instance /////////////////////////////

  @Test
  public void testDeleteProcessInstanceWithoutAuthorization() {
    // given
    // no authorization to delete a process instance

    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    try {
      // when
      runtimeService.deleteProcessInstance(processInstanceId, null);
      fail("Exception expected: It should not be possible to delete a process instance");
    } catch (AuthorizationException e) {

      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(DELETE.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(DELETE_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testDeleteProcessInstanceWithDeletePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, DELETE);

    // when
    runtimeService.deleteProcessInstance(processInstanceId, null);

    // then
    disableAuthorization();
    testRule.assertProcessEnded(processInstanceId);
    enableAuthorization();
  }

  @Test
  public void testDeleteProcessInstanceWithDeletePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, DELETE);

    // when
    runtimeService.deleteProcessInstance(processInstanceId, null);

    // then
    disableAuthorization();
    testRule.assertProcessEnded(processInstanceId);
    enableAuthorization();
  }

  @Test
  public void testDeleteProcessInstanceWithDeleteInstancesPermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, DELETE_INSTANCE);

    // when
    runtimeService.deleteProcessInstance(processInstanceId, null);

    // then
    disableAuthorization();
    testRule.assertProcessEnded(processInstanceId);
    enableAuthorization();
  }

  @Test
  public void testDeleteProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, DELETE);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, DELETE_INSTANCE);

    // when
    runtimeService.deleteProcessInstance(processInstanceId, null);

    // then
    disableAuthorization();
    testRule.assertProcessEnded(processInstanceId);
    enableAuthorization();
  }

  // get active activity ids ///////////////////////////////////

  @Test
  public void testGetActiveActivityIdsWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    try {
      // when
      runtimeService.getActiveActivityIds(processInstanceId);
      fail("Exception expected: It should not be possible to retrieve active ativity ids");
    } catch (AuthorizationException e) {

      // then
//      String message = e.getMessage();
//      testRule.assertTextPresent(userId, message);
//      testRule.assertTextPresent(READ.getName(), message);
//      testRule.assertTextPresent(processInstanceId, message);
//      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
    }
  }

  @Test
  public void testGetActiveActivityIdsWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    List<String> activityIds = runtimeService.getActiveActivityIds(processInstanceId);

    // then
    assertNotNull(activityIds);
    assertFalse(activityIds.isEmpty());
  }

  @Test
  public void testGetActiveActivityIdsWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    List<String> activityIds = runtimeService.getActiveActivityIds(processInstanceId);

    // then
    assertNotNull(activityIds);
    assertFalse(activityIds.isEmpty());
  }

  @Test
  public void testGetActiveActivityIdsWithReadInstancesPermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    List<String> activityIds = runtimeService.getActiveActivityIds(processInstanceId);

    // then
    assertNotNull(activityIds);
    assertFalse(activityIds.isEmpty());
  }

  @Test
  public void testGetActiveActivityIds() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    List<String> activityIds = runtimeService.getActiveActivityIds(processInstanceId);

    // then
    assertNotNull(activityIds);
    assertFalse(activityIds.isEmpty());
  }

  // get activity instance ///////////////////////////////////////////

  @Test
  public void testGetActivityInstanceWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    try {
      // when
      runtimeService.getActivityInstance(processInstanceId);
      fail("Exception expected: It should not be possible to retrieve ativity instances");
    } catch (AuthorizationException e) {

      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(READ_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testGetActivityInstanceWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstanceId);

    // then
    assertNotNull(activityInstance);
  }

  @Test
  public void testGetActivityInstanceWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstanceId);

    // then
    assertNotNull(activityInstance);
  }

  @Test
  public void testGetActivityInstanceWithReadInstancesPermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstanceId);

    // then
    assertNotNull(activityInstance);
  }

  @Test
  public void testGetActivityInstanceIds() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstanceId);

    // then
    assertNotNull(activityInstance);
  }

  // signal execution ///////////////////////////////////////////

  @Test
  public void testSignalWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    try {
      // when
      runtimeService.signal(processInstanceId);
      fail("Exception expected: It should not be possible to signal an execution");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testSignalWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    // when
    runtimeService.signal(processInstanceId);

    // then
    testRule.assertProcessEnded(processInstanceId);
  }

  @Test
  public void testSignalWithUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    runtimeService.signal(processInstanceId);

    // then
    testRule.assertProcessEnded(processInstanceId);
  }

  @Test
  public void testSignalWithUpdateInstancesPermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.signal(processInstanceId);

    // then
    testRule.assertProcessEnded(processInstanceId);
  }

  @Test
  public void testSignal() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstanceId);

    // then
    assertNotNull(activityInstance);
  }

  // signal event received //////////////////////////////////////

  @Test
  public void testSignalEventReceivedWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY).getId();

    try {
      // when
      runtimeService.signalEventReceived("alert");
      fail("Exception expected: It should not be possible to trigger a signal event");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(SIGNAL_BOUNDARY_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testSignalEventReceivedWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    // when
    runtimeService.signalEventReceived("alert");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  @Test
  public void testSignalEventReceivedWithUpdatePermissionOnAnyProcessInstance() {
    // given
    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    runtimeService.signalEventReceived("alert");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  @Test
  public void testSignalEventReceivedWithUpdateInstancesPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, SIGNAL_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.signalEventReceived("alert");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  @Test
  public void testSignalEventReceived() {
    // given
    String processInstanceId = startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);
    createGrantAuthorization(PROCESS_DEFINITION, SIGNAL_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.signalEventReceived("alert");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  @Test
  public void testSignalEventReceivedTwoExecutionsShouldFail() {
    // given
    String firstProcessInstanceId = startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY).getId();
    String secondProcessInstanceId = startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_INSTANCE, firstProcessInstanceId, userId, UPDATE);

    try {
      // when
      runtimeService.signalEventReceived("alert");
      fail("Exception expected: It should not be possible to trigger a signal event");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(secondProcessInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(SIGNAL_BOUNDARY_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testSignalEventReceivedTwoExecutionsShouldSuccess() {
    // given
    String firstProcessInstanceId = startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY).getId();
    String secondProcessInstanceId = startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_INSTANCE, firstProcessInstanceId, userId, UPDATE);
    createGrantAuthorization(PROCESS_INSTANCE, secondProcessInstanceId, userId, UPDATE);

    // when
    runtimeService.signalEventReceived("alert");

    // then
    disableAuthorization();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertFalse(tasks.isEmpty());
    for (Task task : tasks) {
      assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
    }
    enableAuthorization();
  }

  // signal event received by execution id //////////////////////////////////////

  @Test
  public void testSignalEventReceivedByExecutionIdWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY).getId();
    String executionId = selectSingleTask().getExecutionId();

    try {
      // when
      runtimeService.signalEventReceived("alert", executionId);
      fail("Exception expected: It should not be possible to trigger a signal event");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(SIGNAL_BOUNDARY_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testSignalEventReceivedByExecutionIdWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    String executionId = selectSingleTask().getExecutionId();

    // when
    runtimeService.signalEventReceived("alert", executionId);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  @Test
  public void testSignalEventReceivedByExecutionIdWithUpdatePermissionOnAnyProcessInstance() {
    // given
    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    String executionId = selectSingleTask().getExecutionId();

    // when
    runtimeService.signalEventReceived("alert", executionId);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  @Test
  public void testSignalEventReceivedByExecutionIdWithUpdateInstancesPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, SIGNAL_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    String executionId = selectSingleTask().getExecutionId();

    // when
    runtimeService.signalEventReceived("alert", executionId);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  @Test
  public void testSignalEventReceivedByExecutionId() {
    // given
    String processInstanceId = startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);
    createGrantAuthorization(PROCESS_DEFINITION, SIGNAL_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    String executionId = selectSingleTask().getExecutionId();

    // when
    runtimeService.signalEventReceived("alert", executionId);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  @Test
  public void testStartProcessInstanceBySignalEventReceivedWithoutAuthorization() {
    // given
    // no authorization to start a process instance

    try {
      // when
      runtimeService.signalEventReceived("warning");
      fail("Exception expected");
    } catch (AuthorizationException e) {
      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  @Test
  public void testStartProcessInstanceBySignalEventReceivedWithCreatePermissionOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    try {
      // when
      runtimeService.signalEventReceived("warning");
      fail("Exception expected");
    } catch (AuthorizationException e) {
      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE_INSTANCE' permission on resource 'signalStartProcess' of type 'ProcessDefinition'", e.getMessage());
    }
  }

  @Test
  public void testStartProcessInstanceBySignalEventReceived() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);
    createGrantAuthorization(PROCESS_DEFINITION, SIGNAL_START_PROCESS_KEY, userId, CREATE_INSTANCE);

    // when
    runtimeService.signalEventReceived("warning");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("task", task.getTaskDefinitionKey());
  }

  /**
   * currently the ThrowSignalEventActivityBehavior does not check authorization
   */
  public void FAILING_testStartProcessInstanceByThrowSignalEventWithCreatePermissionOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);
    createGrantAuthorization(PROCESS_DEFINITION, THROW_WARNING_SIGNAL_PROCESS_KEY, userId, CREATE_INSTANCE);

    try {
      // when
      runtimeService.startProcessInstanceByKey(THROW_WARNING_SIGNAL_PROCESS_KEY);
      fail("Exception expected");
    } catch (AuthorizationException e) {
      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE_INSTANCE' permission on resource 'signalStartProcess' of type 'ProcessDefinition'", e.getMessage());
    }
  }

  @Test
  public void testStartProcessInstanceByThrowSignalEvent() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);
    createGrantAuthorization(PROCESS_DEFINITION, SIGNAL_START_PROCESS_KEY, userId, CREATE_INSTANCE);
    createGrantAuthorization(PROCESS_DEFINITION, THROW_WARNING_SIGNAL_PROCESS_KEY, userId, CREATE_INSTANCE);

    // when
    runtimeService.startProcessInstanceByKey(THROW_WARNING_SIGNAL_PROCESS_KEY);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("task", task.getTaskDefinitionKey());
  }

  /**
   * currently the ThrowSignalEventActivityBehavior does not check authorization
   */
  public void FAILING_testThrowSignalEventWithoutAuthorization() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);
    createGrantAuthorization(PROCESS_DEFINITION, THROW_ALERT_SIGNAL_PROCESS_KEY, userId, CREATE_INSTANCE);

    String processInstanceId = startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY).getId();

    try {
      // when
      runtimeService.startProcessInstanceByKey(THROW_ALERT_SIGNAL_PROCESS_KEY);

      fail("Exception expected");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(SIGNAL_BOUNDARY_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testThrowSignalEvent() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);
    createGrantAuthorization(PROCESS_DEFINITION, THROW_ALERT_SIGNAL_PROCESS_KEY, userId, CREATE_INSTANCE);

    String processInstanceId = startProcessInstanceByKey(SIGNAL_BOUNDARY_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);
    createGrantAuthorization(PROCESS_DEFINITION, SIGNAL_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.startProcessInstanceByKey(THROW_ALERT_SIGNAL_PROCESS_KEY);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  // message event received /////////////////////////////////////

  @Test
  public void testMessageEventReceivedWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY).getId();
    String executionId = selectSingleTask().getExecutionId();

    try {
      // when
      runtimeService.messageEventReceived("boundaryInvoiceMessage", executionId);
      fail("Exception expected: It should not be possible to trigger a message event");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(MESSAGE_BOUNDARY_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testMessageEventReceivedByExecutionIdWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    String executionId = selectSingleTask().getExecutionId();

    // when
    runtimeService.messageEventReceived("boundaryInvoiceMessage", executionId);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  @Test
  public void testMessageEventReceivedByExecutionIdWithUpdatePermissionOnAnyProcessInstance() {
    // given
    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    String executionId = selectSingleTask().getExecutionId();

    // when
    runtimeService.messageEventReceived("boundaryInvoiceMessage", executionId);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  @Test
  public void testMessageEventReceivedByExecutionIdWithUpdateInstancesPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    String executionId = selectSingleTask().getExecutionId();

    // when
    runtimeService.messageEventReceived("boundaryInvoiceMessage", executionId);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  @Test
  public void testMessageEventReceivedByExecutionId() {
    // given
    String processInstanceId = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    String executionId = selectSingleTask().getExecutionId();

    // when
    runtimeService.messageEventReceived("boundaryInvoiceMessage", executionId);

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  // correlate message (correlates to an execution) /////////////

  @Test
  public void testCorrelateMessageExecutionWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY).getId();

    try {
      // when
      runtimeService.correlateMessage("boundaryInvoiceMessage");
      fail("Exception expected: It should not be possible to correlate a message.");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(MESSAGE_BOUNDARY_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testCorrelateMessageExecutionWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    // when
    runtimeService.correlateMessage("boundaryInvoiceMessage");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  @Test
  public void testCorrelateMessageExecutionWithUpdatePermissionOnAnyProcessInstance() {
    // given
    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    runtimeService.correlateMessage("boundaryInvoiceMessage");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  @Test
  public void testCorrelateMessageExecutionWithUpdateInstancesPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.correlateMessage("boundaryInvoiceMessage");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  @Test
  public void testCorrelateMessageExecution() {
    // given
    String processInstanceId = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.correlateMessage("boundaryInvoiceMessage");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  // correlate message (correlates to a process definition) /////////////

  @Test
  public void testCorrelateMessageProcessDefinitionWithoutAuthorization() {
    // given

    try {
      // when
      runtimeService.correlateMessage("startInvoiceMessage");
      fail("Exception expected: It should not be possible to correlate a message.");
    } catch (AuthorizationException e) {
      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  @Test
  public void testCorrelateMessageProcessDefinitionWithCreatePermissionOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    try {
      // when
      runtimeService.correlateMessage("startInvoiceMessage");
      fail("Exception expected: It should not be possible to correlate a message.");
    } catch (AuthorizationException e) {

      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE_INSTANCE' permission on resource 'messageStartProcess' of type 'ProcessDefinition'", e.getMessage());
    }
  }

  @Test
  public void testCorrelateMessageProcessDefinitionWithCreateInstancesPermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_START_PROCESS_KEY, userId, CREATE_INSTANCE);

    try {
      // when
      runtimeService.correlateMessage("startInvoiceMessage");
      fail("Exception expected: It should not be possible to correlate a message.");
    } catch (AuthorizationException e) {

      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  @Test
  public void testCorrelateMessageProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_START_PROCESS_KEY, userId, CREATE_INSTANCE);

    // when
    runtimeService.correlateMessage("startInvoiceMessage");

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("task", task.getTaskDefinitionKey());
  }

  // correlate all (correlates to executions) ///////////////////

  @Test
  public void testCorrelateAllExecutionWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY).getId();

    try {
      // when
      runtimeService
        .createMessageCorrelation("boundaryInvoiceMessage")
        .correlateAll();
      fail("Exception expected: It should not be possible to correlate a message.");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(MESSAGE_BOUNDARY_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testCorrelateAllExecutionWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    // when
    runtimeService
      .createMessageCorrelation("boundaryInvoiceMessage")
      .correlateAll();

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  @Test
  public void testCorrelateAllExecutionWithUpdatePermissionOnAnyProcessInstance() {
    // given
    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    runtimeService
      .createMessageCorrelation("boundaryInvoiceMessage")
      .correlateAll();

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  @Test
  public void testCorrelateAllExecutionWithUpdateInstancesPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService
      .createMessageCorrelation("boundaryInvoiceMessage")
      .correlateAll();

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  @Test
  public void testCorrelateAllExecution() {
    // given
    String processInstanceId = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService
      .createMessageCorrelation("boundaryInvoiceMessage")
      .correlateAll();

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
  }

  @Test
  public void testCorrelateAllTwoExecutionsShouldFail() {
    // given
    String firstProcessInstanceId = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY).getId();
    String secondProcessInstanceId = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_INSTANCE, firstProcessInstanceId, userId, UPDATE);

    try {
      // when
      runtimeService
        .createMessageCorrelation("boundaryInvoiceMessage")
        .correlateAll();
      fail("Exception expected: It should not be possible to trigger a signal event");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(secondProcessInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(MESSAGE_BOUNDARY_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testCorrelateAllTwoExecutionsShouldSuccess() {
    // given
    String firstProcessInstanceId = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY).getId();
    String secondProcessInstanceId = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_INSTANCE, firstProcessInstanceId, userId, UPDATE);
    createGrantAuthorization(PROCESS_INSTANCE, secondProcessInstanceId, userId, UPDATE);

    // when
    runtimeService
      .createMessageCorrelation("boundaryInvoiceMessage")
      .correlateAll();

    // then
    disableAuthorization();
    List<Task> tasks = taskService.createTaskQuery().list();
    assertFalse(tasks.isEmpty());
    for (Task task : tasks) {
      assertEquals("taskAfterBoundaryEvent", task.getTaskDefinitionKey());
    }
    enableAuthorization();
  }

  // correlate all (correlates to a process definition) /////////////

  @Test
  public void testCorrelateAllProcessDefinitionWithoutAuthorization() {
    // given

    try {
      // when
      runtimeService
        .createMessageCorrelation("startInvoiceMessage")
        .correlateAll();
      fail("Exception expected: It should not be possible to correlate a message.");
    } catch (AuthorizationException e) {
      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  @Test
  public void testCorrelateAllProcessDefinitionWithCreatePermissionOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    try {
      // when
      runtimeService
        .createMessageCorrelation("startInvoiceMessage")
        .correlateAll();
      fail("Exception expected: It should not be possible to correlate a message.");
    } catch (AuthorizationException e) {

      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE_INSTANCE' permission on resource 'messageStartProcess' of type 'ProcessDefinition'", e.getMessage());
    }
  }

  @Test
  public void testCorrelateAllProcessDefinitionWithCreateInstancesPermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_START_PROCESS_KEY, userId, CREATE_INSTANCE);

    try {
      // when
      runtimeService
        .createMessageCorrelation("startInvoiceMessage")
        .correlateAll();
      fail("Exception expected: It should not be possible to correlate a message.");
    } catch (AuthorizationException e) {

      // then
      testRule.assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  @Test
  public void testCorrelateAllProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_START_PROCESS_KEY, userId, CREATE_INSTANCE);

    // when
    runtimeService
      .createMessageCorrelation("startInvoiceMessage")
      .correlateAll();

    // then
    Task task = selectSingleTask();
    assertNotNull(task);
    assertEquals("task", task.getTaskDefinitionKey());
  }

  // suspend process instance by id /////////////////////////////

  @Test
  public void testSuspendProcessInstanceByIdWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    try {
      // when
      runtimeService.suspendProcessInstanceById(processInstanceId);
      fail("Exception expected: It should not be posssible to suspend a process instance.");
    } catch (AuthorizationException e) {

      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
      testRule.assertTextPresent(SUSPEND.getName(), message);
      testRule.assertTextPresent(SUSPEND_INSTANCE.getName(), message);
    }
  }

  @Test
  public void testSuspendProcessInstanceByIdWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    // when
    runtimeService.suspendProcessInstanceById(processInstanceId);

    // then
    ProcessInstance instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  @Test
  public void testSuspendProcessInstanceByIdWithUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    runtimeService.suspendProcessInstanceById(processInstanceId);

    // then
    ProcessInstance instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  @Test
  public void testSuspendProcessInstanceByIdWithUpdateInstancesPermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.suspendProcessInstanceById(processInstanceId);

    // then
    ProcessInstance instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  @Test
  public void testSuspendProcessInstanceById() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE, SUSPEND);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE, SUSPEND_INSTANCE);

    // when
    runtimeService.suspendProcessInstanceById(processInstanceId);

    // then
    ProcessInstance instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  @Test
  public void testSuspendProcessInstanceByIdWithSuspendPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, SUSPEND);

    // when
    runtimeService.suspendProcessInstanceById(processInstanceId);

    // then
    ProcessInstance instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  @Test
  public void testSuspendProcessInstanceByIdWithSuspendPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, SUSPEND);

    // when
    runtimeService.suspendProcessInstanceById(processInstanceId);

    // then
    ProcessInstance instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  @Test
  public void testSuspendProcessInstanceByIdWithSuspendInstancesPermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, SUSPEND_INSTANCE);

    // when
    runtimeService.suspendProcessInstanceById(processInstanceId);

    // then
    ProcessInstance instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  // activate process instance by id /////////////////////////////

  @Test
  public void testActivateProcessInstanceByIdWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    suspendProcessInstanceById(processInstanceId);

    try {
      // when
      runtimeService.activateProcessInstanceById(processInstanceId);
      fail("Exception expected: It should not be posssible to activate a process instance.");
    } catch (AuthorizationException e) {

      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
      testRule.assertTextPresent(SUSPEND.getName(), message);
      testRule.assertTextPresent(SUSPEND_INSTANCE.getName(), message);
    }
  }

  @Test
  public void testActivateProcessInstanceByIdWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    suspendProcessInstanceById(processInstanceId);
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    // when
    runtimeService.activateProcessInstanceById(processInstanceId);

    // then
    ProcessInstance instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  @Test
  public void testActivateProcessInstanceByIdWithUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    suspendProcessInstanceById(processInstanceId);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    runtimeService.activateProcessInstanceById(processInstanceId);

    // then
    ProcessInstance instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  @Test
  public void testActivateProcessInstanceByIdWithUpdateInstancesPermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    suspendProcessInstanceById(processInstanceId);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.activateProcessInstanceById(processInstanceId);

    // then
    ProcessInstance instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  @Test
  public void testActivateProcessInstanceById() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    suspendProcessInstanceById(processInstanceId);
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE, SUSPEND);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE, SUSPEND_INSTANCE);

    // when
    runtimeService.activateProcessInstanceById(processInstanceId);

    // then
    ProcessInstance instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  @Test
  public void testActivateProcessInstanceByIdWithSuspendPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    suspendProcessInstanceById(processInstanceId);
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, SUSPEND);

    // when
    runtimeService.activateProcessInstanceById(processInstanceId);

    // then
    ProcessInstance instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  @Test
  public void testActivateProcessInstanceByIdWithSuspendPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    suspendProcessInstanceById(processInstanceId);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, SUSPEND);

    // when
    runtimeService.activateProcessInstanceById(processInstanceId);

    // then
    ProcessInstance instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  @Test
  public void testActivateProcessInstanceByIdWithSuspendInstancesPermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    suspendProcessInstanceById(processInstanceId);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, SUSPEND_INSTANCE);

    // when
    runtimeService.activateProcessInstanceById(processInstanceId);

    // then
    ProcessInstance instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  // suspend process instance by process definition id /////////////////////////////

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionIdWithoutAuthorization() {
    // given
    String processDefinitionId = startProcessInstanceByKey(PROCESS_KEY).getProcessDefinitionId();

    try {
      // when
      runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinitionId);
      fail("Exception expected: It should not be posssible to suspend a process instance.");
    } catch (AuthorizationException e) {

      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
      testRule.assertTextPresent(SUSPEND.getName(), message);
      testRule.assertTextPresent(SUSPEND_INSTANCE.getName(), message);
    }
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionIdWithUpdatePermissionOnProcessInstance() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    String processDefinitionId = instance.getProcessDefinitionId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    try {
      // when
      runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinitionId);
      fail("Exception expected: It should not be posssible to suspend a process instance.");
    } catch (AuthorizationException e) {

      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(SUSPEND_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
      testRule.assertTextPresent(SUSPEND.getName(), message);
      testRule.assertTextPresent(UPDATE.getName(), message);
    }
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionIdWithUpdatePermissionOnAnyProcessInstance() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processDefinitionId = instance.getProcessDefinitionId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinitionId);

    // then
    instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionIdWithUpdateInstancesPermissionOnProcessDefinition() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processDefinitionId = instance.getProcessDefinitionId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinitionId);

    // then
    instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionId() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    String processDefinitionId = instance.getProcessDefinitionId();

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE, SUSPEND);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE, SUSPEND_INSTANCE);

    // when
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinitionId);

    // then
    instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionIdWithSuspendPermissionOnProcessInstance() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    String processDefinitionId = instance.getProcessDefinitionId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, SUSPEND);

    try {
      // when
      runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinitionId);
      fail("Exception expected: It should not be posssible to suspend a process instance.");
    } catch (AuthorizationException e) {

      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(SUSPEND_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
      testRule.assertTextPresent(SUSPEND.getName(), message);
      testRule.assertTextPresent(UPDATE.getName(), message);
    }
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionIdWithSuspendPermissionOnAnyProcessInstance() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processDefinitionId = instance.getProcessDefinitionId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, SUSPEND);

    // when
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinitionId);

    // then
    instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionIdWithSuspendInstancesPermissionOnProcessDefinition() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processDefinitionId = instance.getProcessDefinitionId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, SUSPEND_INSTANCE);

    // when
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinitionId);

    // then
    instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }


  // activate process instance by process definition id /////////////////////////////

  @Test
  public void testActivateProcessInstanceByProcessDefinitionIdWithoutAuthorization() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    String processDefinitionId = instance.getProcessDefinitionId();
    suspendProcessInstanceById(processInstanceId);

    try {
      // when
      runtimeService.activateProcessInstanceByProcessDefinitionId(processDefinitionId);
      fail("Exception expected: It should not be posssible to suspend a process instance.");
    } catch (AuthorizationException e) {

      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
      testRule.assertTextPresent(SUSPEND.getName(), message);
      testRule.assertTextPresent(SUSPEND_INSTANCE.getName(), message);
    }
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionIdWithUpdatePermissionOnProcessInstance() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    String processDefinitionId = instance.getProcessDefinitionId();
    suspendProcessInstanceById(processInstanceId);

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    try {
      // when
      runtimeService.activateProcessInstanceByProcessDefinitionId(processDefinitionId);
      fail("Exception expected: It should not be posssible to suspend a process instance.");
    } catch (AuthorizationException e) {

      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(SUSPEND_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
      testRule.assertTextPresent(SUSPEND.getName(), message);
      testRule.assertTextPresent(UPDATE.getName(), message);
    }
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionIdWithUpdatePermissionOnAnyProcessInstance() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    String processDefinitionId = instance.getProcessDefinitionId();
    suspendProcessInstanceById(processInstanceId);

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    runtimeService.activateProcessInstanceByProcessDefinitionId(processDefinitionId);

    // then
    instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionIdWithUpdateInstancesPermissionOnProcessDefinition() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    String processDefinitionId = instance.getProcessDefinitionId();
    suspendProcessInstanceById(processInstanceId);

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.activateProcessInstanceByProcessDefinitionId(processDefinitionId);

    // then
    instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionId() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    String processDefinitionId = instance.getProcessDefinitionId();
    suspendProcessInstanceById(processInstanceId);

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE, SUSPEND);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE, SUSPEND_INSTANCE);

    // when
    runtimeService.activateProcessInstanceByProcessDefinitionId(processDefinitionId);

    // then
    instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionIdWithSuspendPermissionOnProcessInstance() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    String processDefinitionId = instance.getProcessDefinitionId();
    suspendProcessInstanceById(processInstanceId);

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, SUSPEND);

    try {
      // when
      runtimeService.activateProcessInstanceByProcessDefinitionId(processDefinitionId);
      fail("Exception expected: It should not be posssible to suspend a process instance.");
    } catch (AuthorizationException e) {

      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(SUSPEND_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
      testRule.assertTextPresent(SUSPEND.getName(), message);
      testRule.assertTextPresent(UPDATE.getName(), message);
    }
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionIdWithSuspendPermissionOnAnyProcessInstance() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    String processDefinitionId = instance.getProcessDefinitionId();
    suspendProcessInstanceById(processInstanceId);

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, SUSPEND);

    // when
    runtimeService.activateProcessInstanceByProcessDefinitionId(processDefinitionId);

    // then
    instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionIdWithSuspendInstancesPermissionOnProcessDefinition() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    String processDefinitionId = instance.getProcessDefinitionId();
    suspendProcessInstanceById(processInstanceId);

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, SUSPEND_INSTANCE);

    // when
    runtimeService.activateProcessInstanceByProcessDefinitionId(processDefinitionId);

    // then
    instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  // suspend process instance by process definition key /////////////////////////////

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionKeyWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);

    try {
      // when
      runtimeService.suspendProcessInstanceByProcessDefinitionKey(PROCESS_KEY);
      fail("Exception expected: It should not be posssible to suspend a process instance.");
    } catch (AuthorizationException e) {

      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
      testRule.assertTextPresent(SUSPEND.getName(), message);
      testRule.assertTextPresent(SUSPEND_INSTANCE.getName(), message);
   }
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionKeyWithUpdatePermissionOnProcessInstance() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    try {
      // when
      runtimeService.suspendProcessInstanceByProcessDefinitionKey(PROCESS_KEY);
      fail("Exception expected: It should not be posssible to suspend a process instance.");
    } catch (AuthorizationException e) {

      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(SUSPEND_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
      testRule.assertTextPresent(SUSPEND.getName(), message);
      testRule.assertTextPresent(UPDATE.getName(), message);
    }
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionKeyWithUpdatePermissionOnAnyProcessInstance() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    runtimeService.suspendProcessInstanceByProcessDefinitionKey(PROCESS_KEY);

    // then
    ProcessInstance instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionKeyWithUpdateInstancesPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.suspendProcessInstanceByProcessDefinitionKey(PROCESS_KEY);

    // then
    ProcessInstance instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionKey() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.suspendProcessInstanceByProcessDefinitionKey(PROCESS_KEY);

    // then
    instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionKeyWithSuspendPermissionOnProcessInstance() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, SUSPEND);

    try {
      // when
      runtimeService.suspendProcessInstanceByProcessDefinitionKey(PROCESS_KEY);
      fail("Exception expected: It should not be posssible to suspend a process instance.");
    } catch (AuthorizationException e) {

      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(SUSPEND_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
      testRule.assertTextPresent(SUSPEND.getName(), message);
      testRule.assertTextPresent(UPDATE.getName(), message);
    }
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionKeyWithSuspendPermissionOnAnyProcessInstance() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, SUSPEND);

    // when
    runtimeService.suspendProcessInstanceByProcessDefinitionKey(PROCESS_KEY);

    // then
    ProcessInstance instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  @Test
  public void testSuspendProcessInstanceByProcessDefinitionKeyWithSuspendInstancesPermissionOnProcessDefinition() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, SUSPEND_INSTANCE);

    // when
    runtimeService.suspendProcessInstanceByProcessDefinitionKey(PROCESS_KEY);

    // then
    ProcessInstance instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  // activate process instance by process definition key /////////////////////////////

  @Test
  public void testActivateProcessInstanceByProcessDefinitionKeyWithoutAuthorization() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    suspendProcessInstanceById(processInstanceId);

    try {
      // when
      runtimeService.activateProcessInstanceByProcessDefinitionKey(PROCESS_KEY);
      fail("Exception expected: It should not be posssible to suspend a process instance.");
    } catch (AuthorizationException e) {

      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
      testRule.assertTextPresent(SUSPEND.getName(), message);
      testRule.assertTextPresent(SUSPEND_INSTANCE.getName(), message);
    }
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionKeyWithUpdatePermissionOnProcessInstance() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    suspendProcessInstanceById(processInstanceId);

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    try {
      // when
      runtimeService.activateProcessInstanceByProcessDefinitionKey(PROCESS_KEY);
      fail("Exception expected: It should not be posssible to suspend a process instance.");
    } catch (AuthorizationException e) {

      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(SUSPEND_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
      testRule.assertTextPresent(SUSPEND.getName(), message);
      testRule.assertTextPresent(UPDATE.getName(), message);
    }
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionKeyWithUpdatePermissionOnAnyProcessInstance() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    suspendProcessInstanceById(processInstanceId);

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    runtimeService.activateProcessInstanceByProcessDefinitionKey(PROCESS_KEY);

    // then
    instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionKeyWithUpdateInstancesPermissionOnProcessDefinition() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    suspendProcessInstanceById(processInstanceId);

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.activateProcessInstanceByProcessDefinitionKey(PROCESS_KEY);

    // then
    instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionKey() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    suspendProcessInstanceById(processInstanceId);

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE, SUSPEND);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE, SUSPEND_INSTANCE);

    // when
    runtimeService.activateProcessInstanceByProcessDefinitionKey(PROCESS_KEY);

    // then
    instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionKeyWithSuspendPermissionOnProcessInstance() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    suspendProcessInstanceById(processInstanceId);

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, SUSPEND);

    try {
      // when
      runtimeService.activateProcessInstanceByProcessDefinitionKey(PROCESS_KEY);
      fail("Exception expected: It should not be posssible to suspend a process instance.");
    } catch (AuthorizationException e) {

      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(SUSPEND_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
      testRule.assertTextPresent(SUSPEND.getName(), message);
      testRule.assertTextPresent(UPDATE.getName(), message);
    }
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionKeyWithSuspendPermissionOnAnyProcessInstance() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    suspendProcessInstanceById(processInstanceId);

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, SUSPEND);

    // when
    runtimeService.activateProcessInstanceByProcessDefinitionKey(PROCESS_KEY);

    // then
    instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  @Test
  public void testActivateProcessInstanceByProcessDefinitionKeyWithSuspendInstancesPermissionOnProcessDefinition() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    suspendProcessInstanceById(processInstanceId);

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, SUSPEND_INSTANCE);

    // when
    runtimeService.activateProcessInstanceByProcessDefinitionKey(PROCESS_KEY);

    // then
    instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  // modify process instance /////////////////////////////////////

  @Test
  public void testModifyProcessInstanceWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY).getId();

    try {
      // when
      runtimeService.createProcessInstanceModification(processInstanceId)
        .startBeforeActivity("taskAfterBoundaryEvent")
        .execute();
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(UPDATE.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
      testRule.assertTextPresent(MESSAGE_BOUNDARY_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testModifyProcessInstanceWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    // when
    runtimeService.createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("taskAfterBoundaryEvent")
      .execute();

    // then
    disableAuthorization();
    List<Task> tasks = taskService.createTaskQuery().list();
    enableAuthorization();

    assertFalse(tasks.isEmpty());
    assertEquals(2, tasks.size());
  }

  @Test
  public void testModifyProcessInstanceWithUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    runtimeService.createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("taskAfterBoundaryEvent")
      .execute();

    // then
    disableAuthorization();
    List<Task> tasks = taskService.createTaskQuery().list();
    enableAuthorization();

    assertFalse(tasks.isEmpty());
    assertEquals(2, tasks.size());
  }

  @Test
  public void testModifyProcessInstanceWithUpdateInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("taskAfterBoundaryEvent")
      .execute();

    // then
    disableAuthorization();
    List<Task> tasks = taskService.createTaskQuery().list();
    enableAuthorization();

    assertFalse(tasks.isEmpty());
    assertEquals(2, tasks.size());
  }

  @Test
  public void testModifyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.createProcessInstanceModification(processInstanceId)
      .startBeforeActivity("taskAfterBoundaryEvent")
      .execute();

    // then
    disableAuthorization();
    List<Task> tasks = taskService.createTaskQuery().list();
    enableAuthorization();

    assertFalse(tasks.isEmpty());
    assertEquals(2, tasks.size());
  }

  @Test
  public void testDeleteProcessInstanceByModifyingWithoutDeleteAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);

    try {
      // when
      runtimeService.createProcessInstanceModification(processInstanceId)
        .cancelAllForActivity("task")
        .execute();
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(DELETE.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(DELETE_INSTANCE.getName(), message);
      testRule.assertTextPresent(MESSAGE_BOUNDARY_PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testDeleteProcessInstanceByModifyingWithoutDeletePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, DELETE);

    // when
    runtimeService.createProcessInstanceModification(processInstanceId)
      .cancelAllForActivity("task")
      .execute();

    // then
    testRule.assertProcessEnded(processInstanceId);
  }

  @Test
  public void testDeleteProcessInstanceByModifyingWithoutDeletePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_BOUNDARY_PROCESS_KEY, userId, UPDATE_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, DELETE);

    // when
    runtimeService.createProcessInstanceModification(processInstanceId)
      .cancelAllForActivity("task")
      .execute();

    // then
    testRule.assertProcessEnded(processInstanceId);
  }

  @Test
  public void testDeleteProcessInstanceByModifyingWithoutDeleteInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(MESSAGE_BOUNDARY_PROCESS_KEY).getId();
    Authorization authorization = createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_BOUNDARY_PROCESS_KEY);
    authorization.setUserId(userId);
    authorization.addPermission(UPDATE_INSTANCE);
    authorization.addPermission(DELETE_INSTANCE);
    saveAuthorization(authorization);

    // when
    runtimeService.createProcessInstanceModification(processInstanceId)
      .cancelAllForActivity("task")
      .execute();

    // then
    testRule.assertProcessEnded(processInstanceId);
  }

  // clear process instance authorization ////////////////////////

  @Test
  public void testClearProcessInstanceAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, ALL);
    createGrantAuthorization(TASK, ANY, userId, ALL);

    disableAuthorization();
    Authorization authorization = authorizationService
        .createAuthorizationQuery()
        .resourceId(processInstanceId)
        .singleResult();
    enableAuthorization();
    assertNotNull(authorization);

    String taskId = selectSingleTask().getId();

    // when
    taskService.complete(taskId);

    // then
    disableAuthorization();
    authorization = authorizationService
        .createAuthorizationQuery()
        .resourceId(processInstanceId)
        .singleResult();
    enableAuthorization();

    assertNull(authorization);
  }

  @Test
  public void testDeleteProcessInstanceClearAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, ALL);

    disableAuthorization();
    Authorization authorization = authorizationService
        .createAuthorizationQuery()
        .resourceId(processInstanceId)
        .singleResult();
    enableAuthorization();
    assertNotNull(authorization);

    // when
    runtimeService.deleteProcessInstance(processInstanceId, null);

    // then
    disableAuthorization();
    authorization = authorizationService
        .createAuthorizationQuery()
        .resourceId(processInstanceId)
        .singleResult();
    enableAuthorization();

    assertNull(authorization);
  }

  // RuntimeService#getVariable() ////////////////////////////////////////////

  @Test
  public void testGetVariableWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();

    try {
      // when
      runtimeService.getVariable(processInstanceId, VARIABLE_NAME);
      fail("Exception expected: It should not be to retrieve the variable instance");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(READ_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }

    // given (2)
    setReadVariableAsDefaultReadVariablePermission();

    try {
      // when (2)
      runtimeService.getVariable(processInstanceId, VARIABLE_NAME);
      fail("Exception expected: It should not be to retrieve the variable instance");
    } catch (AuthorizationException e) {
      // then (2)
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ_INSTANCE_VARIABLE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testGetVariableWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    Object variable = runtimeService.getVariable(processInstanceId, VARIABLE_NAME);

    // then
    assertEquals(VARIABLE_VALUE, variable);
  }

  @Test
  public void testGetVariableWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    Object variable = runtimeService.getVariable(processInstanceId, VARIABLE_NAME);

    // then
    assertEquals(VARIABLE_VALUE, variable);
  }

  @Test
  public void testGetVariableWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    Object variable = runtimeService.getVariable(processInstanceId, VARIABLE_NAME);

    // then
    assertEquals(VARIABLE_VALUE, variable);
  }

  @Test
  public void testGetVariableWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    Object variable = runtimeService.getVariable(processInstanceId, VARIABLE_NAME);

    // then
    assertEquals(VARIABLE_VALUE, variable);
  }

  @Test
  public void testGetVariableWithReadInstanceVariablePermissionOnProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE_VARIABLE);

    // when
    Object variable = runtimeService.getVariable(processInstanceId, VARIABLE_NAME);

    // then
    assertEquals(VARIABLE_VALUE, variable);
  }

  @Test
  public void testGetVariableWithReadInstanceVariablePermissionOnAnyProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE_VARIABLE);

    // when
    Object variable = runtimeService.getVariable(processInstanceId, VARIABLE_NAME);

    // then
    assertEquals(VARIABLE_VALUE, variable);
  }

  // RuntimeService#getVariableLocal() ////////////////////////////////////////////

  @Test
  public void testGetVariableLocalWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();

    try {
      // when
      runtimeService.getVariableLocal(processInstanceId, VARIABLE_NAME);
      fail("Exception expected: It should not be to retrieve the variable instance");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(READ_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }

    // given (2)
    setReadVariableAsDefaultReadVariablePermission();

    try {
      // when (2)
      runtimeService.getVariableLocal(processInstanceId, VARIABLE_NAME);
      fail("Exception expected: It should not be to retrieve the variable instance");
    } catch (AuthorizationException e) {
      // then (2)
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ_INSTANCE_VARIABLE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testGetVariableLocalWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    Object variable = runtimeService.getVariableLocal(processInstanceId, VARIABLE_NAME);

    // then
    assertEquals(VARIABLE_VALUE, variable);
  }

  @Test
  public void testGetVariableLocalWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    Object variable = runtimeService.getVariableLocal(processInstanceId, VARIABLE_NAME);

    // then
    assertEquals(VARIABLE_VALUE, variable);
  }

  @Test
  public void testGetVariableLocalWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    Object variable = runtimeService.getVariableLocal(processInstanceId, VARIABLE_NAME);

    // then
    assertEquals(VARIABLE_VALUE, variable);
  }

  @Test
  public void testGetVariableLocalWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    Object variable = runtimeService.getVariableLocal(processInstanceId, VARIABLE_NAME);

    // then
    assertEquals(VARIABLE_VALUE, variable);
  }

  @Test
  public void testGetVariableLocalWithReadInstanceVariablePermissionOnProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE_VARIABLE);

    // when
    Object variable = runtimeService.getVariableLocal(processInstanceId, VARIABLE_NAME);

    // then
    assertEquals(VARIABLE_VALUE, variable);
  }

  @Test
  public void testGetVariableLocalWithReadInstanceVariablePermissionOnAnyProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE_VARIABLE);

    // when
    Object variable = runtimeService.getVariableLocal(processInstanceId, VARIABLE_NAME);

    // then
    assertEquals(VARIABLE_VALUE, variable);
  }

  // RuntimeService#getVariableTyped() ////////////////////////////////////////////

  @Test
  public void testGetVariableTypedWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();

    try {
      // when
      runtimeService.getVariableTyped(processInstanceId, VARIABLE_NAME);
      fail("Exception expected: It should not be to retrieve the variable instance");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(READ_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }

    // given (2)
    setReadVariableAsDefaultReadVariablePermission();

    try {
      // when (2)
      runtimeService.getVariableTyped(processInstanceId, VARIABLE_NAME);
      fail("Exception expected: It should not be to retrieve the variable instance");
    } catch (AuthorizationException e) {
      // then (2)
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ_INSTANCE_VARIABLE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testGetVariableTypedWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    TypedValue typedValue = runtimeService.getVariableTyped(processInstanceId, VARIABLE_NAME);

    // then
    assertNotNull(typedValue);
    assertEquals(VARIABLE_VALUE, typedValue.getValue());
  }

  @Test
  public void testGetVariableTypedWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    TypedValue typedValue = runtimeService.getVariableTyped(processInstanceId, VARIABLE_NAME);

    // then
    assertNotNull(typedValue);
    assertEquals(VARIABLE_VALUE, typedValue.getValue());
  }

  @Test
  public void testGetVariableTypedWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    TypedValue typedValue = runtimeService.getVariableTyped(processInstanceId, VARIABLE_NAME);

    // then
    assertNotNull(typedValue);
    assertEquals(VARIABLE_VALUE, typedValue.getValue());
  }

  @Test
  public void testGetVariableTypedWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    TypedValue typedValue = runtimeService.getVariableTyped(processInstanceId, VARIABLE_NAME);

    // then
    assertNotNull(typedValue);
    assertEquals(VARIABLE_VALUE, typedValue.getValue());
  }

  @Test
  public void testGetVariableTypedWithReadInstanceVariablePermissionOnProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE_VARIABLE);

    // when
    TypedValue typedValue = runtimeService.getVariableTyped(processInstanceId, VARIABLE_NAME);

    // then
    assertNotNull(typedValue);
    assertEquals(VARIABLE_VALUE, typedValue.getValue());
  }

  @Test
  public void testGetVariableTypedWithReadInstanceVariablePermissionOnAnyProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE_VARIABLE);

    // when
    TypedValue typedValue = runtimeService.getVariableTyped(processInstanceId, VARIABLE_NAME);

    // then
    assertNotNull(typedValue);
    assertEquals(VARIABLE_VALUE, typedValue.getValue());
  }

  // RuntimeService#getVariableLocalTyped() ////////////////////////////////////////////

  @Test
  public void testGetVariableLocalTypedWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();

    try {
      // when
      runtimeService.getVariableLocalTyped(processInstanceId, VARIABLE_NAME);
      fail("Exception expected: It should not be to retrieve the variable instance");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(READ_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }

    // given (2)
    setReadVariableAsDefaultReadVariablePermission();

    try {
      // when (2)
      runtimeService.getVariableLocalTyped(processInstanceId, VARIABLE_NAME);
      fail("Exception expected: It should not be to retrieve the variable instance");
    } catch (AuthorizationException e) {
      // then (2)
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ_INSTANCE_VARIABLE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testGetVariableLocalTypedWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    TypedValue typedValue = runtimeService.getVariableLocalTyped(processInstanceId, VARIABLE_NAME);

    // then
    assertNotNull(typedValue);
    assertEquals(VARIABLE_VALUE, typedValue.getValue());
  }

  @Test
  public void testGetVariableLocalTypedWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    TypedValue typedValue = runtimeService.getVariableLocalTyped(processInstanceId, VARIABLE_NAME);

    // then
    assertNotNull(typedValue);
    assertEquals(VARIABLE_VALUE, typedValue.getValue());
  }

  @Test
  public void testGetVariableLocalTypedWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    TypedValue typedValue = runtimeService.getVariableLocalTyped(processInstanceId, VARIABLE_NAME);

    // then
    assertNotNull(typedValue);
    assertEquals(VARIABLE_VALUE, typedValue.getValue());
  }

  @Test
  public void testGetVariableLocalTypedWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    TypedValue typedValue = runtimeService.getVariableLocalTyped(processInstanceId, VARIABLE_NAME);

    // then
    assertNotNull(typedValue);
    assertEquals(VARIABLE_VALUE, typedValue.getValue());
  }

  @Test
  public void testGetVariableLocalTypedWithReadInstanceVariablePermissionOnProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE_VARIABLE);

    // when
    TypedValue typedValue = runtimeService.getVariableLocalTyped(processInstanceId, VARIABLE_NAME);

    // then
    assertNotNull(typedValue);
    assertEquals(VARIABLE_VALUE, typedValue.getValue());
  }

  @Test
  public void testGetVariableLocalTypedWithReadInstanceVariablePermissionOnAnyProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE_VARIABLE);

    // when
    TypedValue typedValue = runtimeService.getVariableLocalTyped(processInstanceId, VARIABLE_NAME);

    // then
    assertNotNull(typedValue);
    assertEquals(VARIABLE_VALUE, typedValue.getValue());
  }

  // RuntimeService#getVariables() ////////////////////////////////////////////

  @Test
  public void testGetVariablesWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();

    try {
      // when
      runtimeService.getVariables(processInstanceId);
      fail("Exception expected: It should not be to retrieve the variable instances");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(READ_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }

    // given (2)
    setReadVariableAsDefaultReadVariablePermission();

    try {
      // when (2)
      runtimeService.getVariables(processInstanceId);
      fail("Exception expected: It should not be to retrieve the variable instances");
    } catch (AuthorizationException e) {
      // then (2)
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ_INSTANCE_VARIABLE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testGetVariablesWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    Map<String, Object> variables = runtimeService.getVariables(processInstanceId);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    Map<String, Object> variables = runtimeService.getVariables(processInstanceId);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    Map<String, Object> variables = runtimeService.getVariables(processInstanceId);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    Map<String, Object> variables = runtimeService.getVariables(processInstanceId);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesWithReadInstanceVariablePermissionOnProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE_VARIABLE);

    // when
    Map<String, Object> variables = runtimeService.getVariables(processInstanceId);

    // then
    verifyGetVariables(variables);
  }

  @Test
  public void testGetVariablesWithReadInstanceVariablePermissionOnAnyProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE_VARIABLE);

    // when
    Map<String, Object> variables = runtimeService.getVariables(processInstanceId);

    // then
    verifyGetVariables(variables);
  }

  // RuntimeService#getVariablesLocal() ////////////////////////////////////////////

  // RuntimeService#getVariablesLocal() ////////////////////////////////////////////

  @Test
  public void testGetVariablesLocalWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();

    try {
      // when
      runtimeService.getVariablesLocal(processInstanceId);
      fail("Exception expected: It should not be to retrieve the variable instances");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(READ_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }

    // given (2)
    setReadVariableAsDefaultReadVariablePermission();

    try {
      // when (2)
      runtimeService.getVariablesLocal(processInstanceId);
      fail("Exception expected: It should not be to retrieve the variable instances");
    } catch (AuthorizationException e) {
      // then (2)
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ_INSTANCE_VARIABLE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testGetVariablesLocalWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    Map<String, Object> variables = runtimeService.getVariablesLocal(processInstanceId);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesLocalWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    Map<String, Object> variables = runtimeService.getVariablesLocal(processInstanceId);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesLocalWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    Map<String, Object> variables = runtimeService.getVariablesLocal(processInstanceId);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesLocalWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    Map<String, Object> variables = runtimeService.getVariablesLocal(processInstanceId);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesLocalWithReadInstanceVariablePermissionOnProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE_VARIABLE);

    // when
    Map<String, Object> variables = runtimeService.getVariablesLocal(processInstanceId);

    // then
    verifyGetVariables(variables);
  }

  @Test
  public void testGetVariablesLocalWithReadInstanceVariablePermissionOnAnyProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE_VARIABLE);

    // when
    Map<String, Object> variables = runtimeService.getVariablesLocal(processInstanceId);

    // then
    verifyGetVariables(variables);
  }

  // RuntimeService#getVariablesTyped() ////////////////////////////////////////////

  // RuntimeService#getVariablesTyped() ////////////////////////////////////////////

  @Test
  public void testGetVariablesTypedWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();

    try {
      // when
      runtimeService.getVariablesTyped(processInstanceId);
      fail("Exception expected: It should not be to retrieve the variable instances");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(READ_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }

    // given (2)
    setReadVariableAsDefaultReadVariablePermission();

    try {
      // when (2)
      runtimeService.getVariablesTyped(processInstanceId);
      fail("Exception expected: It should not be to retrieve the variable instances");
    } catch (AuthorizationException e) {
      // then (2)
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ_INSTANCE_VARIABLE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testGetVariablesTypedWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    VariableMap variables = runtimeService.getVariablesTyped(processInstanceId);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesTypedWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    VariableMap variables = runtimeService.getVariablesTyped(processInstanceId);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesTypedWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    VariableMap variables = runtimeService.getVariablesTyped(processInstanceId);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesTypedWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    VariableMap variables = runtimeService.getVariablesTyped(processInstanceId);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesTypedWithReadInstanceVariablePermissionOnProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE_VARIABLE);

    // when
    VariableMap variables = runtimeService.getVariablesTyped(processInstanceId);

    // then
    verifyGetVariables(variables);
  }

  @Test
  public void testGetVariablesTypedWithReadInstanceVariablePermissionOnAnyProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE_VARIABLE);

    // when
    VariableMap variables = runtimeService.getVariablesTyped(processInstanceId);

    // then
    verifyGetVariables(variables);
  }

  // RuntimeService#getVariablesLocalTyped() ////////////////////////////////////////////

  // RuntimeService#getVariablesLocalTyped() ////////////////////////////////////////////

  @Test
  public void testGetVariablesLocalTypedWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();

    try {
      // when
      runtimeService.getVariablesLocalTyped(processInstanceId);
      fail("Exception expected: It should not be to retrieve the variable instances");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(READ_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }

    // given (2)
    setReadVariableAsDefaultReadVariablePermission();

    try {
      // when (2)
      runtimeService.getVariablesLocalTyped(processInstanceId);
      fail("Exception expected: It should not be to retrieve the variable instances");
    } catch (AuthorizationException e) {
      // then (2)
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ_INSTANCE_VARIABLE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testGetVariablesLocalTypedWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    VariableMap variables = runtimeService.getVariablesLocalTyped(processInstanceId);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesLocalTypedWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    VariableMap variables = runtimeService.getVariablesLocalTyped(processInstanceId);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesLocalTypedWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    VariableMap variables = runtimeService.getVariablesLocalTyped(processInstanceId);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesLocalTypedWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    VariableMap variables = runtimeService.getVariablesLocalTyped(processInstanceId);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesLocalTypedWithReadInstanceVariablePermissionOnProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE_VARIABLE);

    // when
    VariableMap variables = runtimeService.getVariablesLocalTyped(processInstanceId);

    // then
    verifyGetVariables(variables);
  }

  @Test
  public void testGetVariablesLocalTypedWithReadInstanceVariablePermissionOnAnyProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE_VARIABLE);

    // when
    VariableMap variables = runtimeService.getVariablesLocalTyped(processInstanceId);

    // then
    verifyGetVariables(variables);
  }

  // RuntimeService#getVariables() ////////////////////////////////////////////

  @Test
  public void testGetVariablesByNameWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();

    try {
      // when
      runtimeService.getVariables(processInstanceId, Arrays.asList(VARIABLE_NAME));
      fail("Exception expected: It should not be to retrieve the variable instances");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(READ_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }

    // given (2)
    setReadVariableAsDefaultReadVariablePermission();

    try {
      // when (2)
      runtimeService.getVariables(processInstanceId, Arrays.asList(VARIABLE_NAME));
      fail("Exception expected: It should not be to retrieve the variable instances");
    } catch (AuthorizationException e) {
      // then (2)
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ_INSTANCE_VARIABLE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testGetVariablesByNameWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    Map<String, Object> variables = runtimeService.getVariables(processInstanceId, Arrays.asList(VARIABLE_NAME));

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesByNameWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    Map<String, Object> variables = runtimeService.getVariables(processInstanceId, Arrays.asList(VARIABLE_NAME));

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesByNameWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    Map<String, Object> variables = runtimeService.getVariables(processInstanceId, Arrays.asList(VARIABLE_NAME));

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesByNameWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    Map<String, Object> variables = runtimeService.getVariables(processInstanceId, Arrays.asList(VARIABLE_NAME));

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesByNameWithReadInstanceVariablePermissionOnProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE_VARIABLE);

    // when
    Map<String, Object> variables = runtimeService.getVariables(processInstanceId, Arrays.asList(VARIABLE_NAME));

    // then
    verifyGetVariables(variables);
  }

  @Test
  public void testGetVariablesByNameWithReadInstanceVariablePermissionOnAnyProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE_VARIABLE);

    // when
    Map<String, Object> variables = runtimeService.getVariables(processInstanceId, Arrays.asList(VARIABLE_NAME));

    // then
    verifyGetVariables(variables);
  }

  // RuntimeService#getVariablesLocal() ////////////////////////////////////////////

  @Test
  public void testGetVariablesLocalByNameWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();

    try {
      // when
      runtimeService.getVariablesLocal(processInstanceId, Arrays.asList(VARIABLE_NAME));
      fail("Exception expected: It should not be to retrieve the variable instances");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(READ_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }

    // given (2)
    setReadVariableAsDefaultReadVariablePermission();

    try {
      // when (2)
      runtimeService.getVariablesLocal(processInstanceId, Arrays.asList(VARIABLE_NAME));
      fail("Exception expected: It should not be to retrieve the variable instances");
    } catch (AuthorizationException e) {
      // then (2)
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ_INSTANCE_VARIABLE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testGetVariablesLocalByNameWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    Map<String, Object> variables = runtimeService.getVariablesLocal(processInstanceId, Arrays.asList(VARIABLE_NAME));

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesLocalByNameWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    Map<String, Object> variables = runtimeService.getVariablesLocal(processInstanceId, Arrays.asList(VARIABLE_NAME));

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesLocalByNameWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    Map<String, Object> variables = runtimeService.getVariablesLocal(processInstanceId, Arrays.asList(VARIABLE_NAME));

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesLocalByNameWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    Map<String, Object> variables = runtimeService.getVariablesLocal(processInstanceId, Arrays.asList(VARIABLE_NAME));

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesLocalByNameWithReadInstanceVariablePermissionOnProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE_VARIABLE);

    // when
    Map<String, Object> variables = runtimeService.getVariablesLocal(processInstanceId, Arrays.asList(VARIABLE_NAME));

    // then
    verifyGetVariables(variables);
  }

  @Test
  public void testGetVariablesLocalByNameWithReadInstanceVariablePermissionOnAnyProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE_VARIABLE);

    // when
    Map<String, Object> variables = runtimeService.getVariablesLocal(processInstanceId, Arrays.asList(VARIABLE_NAME));

    // then
    verifyGetVariables(variables);
  }

  // RuntimeService#getVariablesTyped() ////////////////////////////////////////////

  @Test
  public void testGetVariablesTypedByNameWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();

    try {
      // when
      runtimeService.getVariablesTyped(processInstanceId, Arrays.asList(VARIABLE_NAME), false);
      fail("Exception expected: It should not be to retrieve the variable instances");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(READ_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }

    // given (2)
    setReadVariableAsDefaultReadVariablePermission();

    try {
      // when (2)
      runtimeService.getVariablesTyped(processInstanceId, Arrays.asList(VARIABLE_NAME), false);
      fail("Exception expected: It should not be to retrieve the variable instances");
    } catch (AuthorizationException e) {
      // then (2)
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ_INSTANCE_VARIABLE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testGetVariablesTypedByNameWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    VariableMap variables = runtimeService.getVariablesTyped(processInstanceId, Arrays.asList(VARIABLE_NAME), false);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesTypedByNameWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    VariableMap variables = runtimeService.getVariablesTyped(processInstanceId, Arrays.asList(VARIABLE_NAME), false);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesTypedByNameWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    VariableMap variables = runtimeService.getVariablesTyped(processInstanceId, Arrays.asList(VARIABLE_NAME), false);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesTypedByNameWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    VariableMap variables = runtimeService.getVariablesTyped(processInstanceId, Arrays.asList(VARIABLE_NAME), false);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesTypedByNameWithReadInstanceVariablePermissionOnProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE_VARIABLE);

    // when
    VariableMap variables = runtimeService.getVariablesTyped(processInstanceId, Arrays.asList(VARIABLE_NAME), false);

    // then
    verifyGetVariables(variables);
  }

  @Test
  public void testGetVariablesTypedByNameWithReadInstanceVariablePermissionOnAnyProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE_VARIABLE);

    // when
    VariableMap variables = runtimeService.getVariablesTyped(processInstanceId, Arrays.asList(VARIABLE_NAME), false);

    // then
    verifyGetVariables(variables);
  }

  // RuntimeService#getVariablesLocalTyped() ////////////////////////////////////////////

  @Test
  public void testGetVariablesLocalTypedByNameWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();

    try {
      // when
      runtimeService.getVariablesLocalTyped(processInstanceId, Arrays.asList(VARIABLE_NAME), false);
      fail("Exception expected: It should not be to retrieve the variable instances");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(processInstanceId, message);
      testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      testRule.assertTextPresent(READ_INSTANCE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }

    // given (2)
    setReadVariableAsDefaultReadVariablePermission();

    try {
      // when (2)
      runtimeService.getVariablesLocalTyped(processInstanceId, Arrays.asList(VARIABLE_NAME), false);
      fail("Exception expected: It should not be to retrieve the variable instances");
    } catch (AuthorizationException e) {
      // then (2)
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ_INSTANCE_VARIABLE.getName(), message);
      testRule.assertTextPresent(PROCESS_KEY, message);
      testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  @Test
  public void testGetVariablesLocalTypedByNameWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    VariableMap variables = runtimeService.getVariablesLocalTyped(processInstanceId, Arrays.asList(VARIABLE_NAME), false);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesLocalTypedByNameWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    VariableMap variables = runtimeService.getVariablesLocalTyped(processInstanceId, Arrays.asList(VARIABLE_NAME), false);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesLocalTypedByNameWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    VariableMap variables = runtimeService.getVariablesLocalTyped(processInstanceId, Arrays.asList(VARIABLE_NAME), false);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesLocalTypedByNameWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    VariableMap variables = runtimeService.getVariablesLocalTyped(processInstanceId, Arrays.asList(VARIABLE_NAME), false);

    // then
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

  @Test
  public void testGetVariablesLocalTypedByNameWithReadInstanceVariablePermissionOnProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE_VARIABLE);

    // when
    VariableMap variables = runtimeService.getVariablesLocalTyped(processInstanceId, Arrays.asList(VARIABLE_NAME), false);

    // then
    verifyGetVariables(variables);
  }

  @Test
  public void testGetVariablesLocalTypedByNameWithReadInstanceVariablePermissionOnAnyProcessDefinition() {
    // given
    setReadVariableAsDefaultReadVariablePermission();
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE_VARIABLE);

    // when
    VariableMap variables = runtimeService.getVariablesLocalTyped(processInstanceId, Arrays.asList(VARIABLE_NAME), false);

    // then
    verifyGetVariables(variables);
  }

  // RuntimeService#setVariable() ////////////////////////////////////////////

  @Test
  public void testSetVariableWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    try {
      // when
      runtimeService.setVariable(processInstanceId, VARIABLE_NAME, VARIABLE_VALUE);
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      verifyMessageIsValid(processInstanceId, message);
    }
  }

  @Test
  public void testSetVariableWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    verifySetVariable(processInstanceId);
  }

  @Test
  public void testSetVariableWithUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    verifySetVariable(processInstanceId);
  }

  @Test
  public void testSetVariableWithUpdateInstanceInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.setVariable(processInstanceId, VARIABLE_NAME, VARIABLE_VALUE);

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();
  }

  @Test
  public void testSetVariableWithUpdateInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    verifySetVariable(processInstanceId);
  }

  @Test
  public void testSetVariableWithUpdateVariablePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE_VARIABLE);

    verifySetVariable(processInstanceId);
  }

  @Test
  public void testSetVariableWithUpdateVariablePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE_VARIABLE);

    verifySetVariable(processInstanceId);
  }

  @Test
  public void testSetVariableWithUpdateInstanceVariablePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE_VARIABLE);

    verifySetVariable(processInstanceId);
  }

  @Test
  public void testSetVariableWithUpdateInstanceVariablePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE_VARIABLE);

    verifySetVariable(processInstanceId);
  }

  // RuntimeService#setVariableLocal() ////////////////////////////////////////////

  @Test
  public void testSetVariableLocalWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    try {
      // when
      runtimeService.setVariableLocal(processInstanceId, VARIABLE_NAME, VARIABLE_VALUE);
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then
      verifyMessageIsValid(processInstanceId, e.getMessage());
    }
  }

  @Test
  public void testSetVariableLocalWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    verifySetVariableLocal(processInstanceId);
  }

  @Test
  public void testSetVariableLocalWithUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    verifySetVariableLocal(processInstanceId);
  }

  @Test
  public void testSetVariableLocalWithUpdateInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    verifySetVariableLocal(processInstanceId);
  }

  @Test
  public void testSetVariableLocalWithUpdateInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.setVariableLocal(processInstanceId, VARIABLE_NAME, VARIABLE_VALUE);

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();
  }

  @Test
  public void testSetVariableLocalWithUpdateVariablePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE_VARIABLE);

    verifySetVariableLocal(processInstanceId);
  }

  @Test
  public void testSetVariableLocalWithUpdateVariablePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE_VARIABLE);

    verifySetVariableLocal(processInstanceId);
  }

  @Test
  public void testSetVariableLocalWithUpdateInstanceVariablePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE_VARIABLE);

    verifySetVariableLocal(processInstanceId);
  }

  @Test
  public void testSetVariableLocalWithUpdateInstanceVariablePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE_VARIABLE);

    verifySetVariableLocal(processInstanceId);
  }

  // RuntimeService#setVariables() ////////////////////////////////////////////

  @Test
  public void testSetVariablesWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    try {
      // when
      runtimeService.setVariables(processInstanceId, getVariables());
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then
      verifyMessageIsValid(processInstanceId, e.getMessage());
    }
  }

  @Test
  public void testSetVariablesWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    verifySetVariables(processInstanceId);
  }

  @Test
  public void testSetVariablesWithUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    verifySetVariables(processInstanceId);
  }

  @Test
  public void testSetVariablesWithUpdateInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    verifySetVariables(processInstanceId);
  }

  @Test
  public void testSetVariablesWithUpdateInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    verifySetVariables(processInstanceId);
  }

  @Test
  public void testSetVariablesWithUpdateVariablePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    verifySetVariables(processInstanceId);
  }

  @Test
  public void testSetVariablesWithUpdateVariablePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE_VARIABLE);

    verifySetVariables(processInstanceId);
  }

  @Test
  public void testSetVariablesWithUpdateInstanceVariablePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE_VARIABLE);

    verifySetVariables(processInstanceId);
  }

  @Test
  public void testSetVariablesWithUpdateInstanceVariablePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE_VARIABLE);

    verifySetVariables(processInstanceId);
  }

  // RuntimeService#setVariablesLocal() ////////////////////////////////////////////

  @Test
  public void testSetVariablesLocalWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    try {
      // when
      runtimeService.setVariablesLocal(processInstanceId, getVariables());
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then
      verifyMessageIsValid(processInstanceId, e.getMessage());
    }
  }

  @Test
  public void testSetVariablesLocalWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    verifySetVariablesLocal(processInstanceId);
  }

  @Test
  public void testSetVariablesLocalWithUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    verifySetVariablesLocal(processInstanceId);
  }

  @Test
  public void testSetVariablesLocalWithUpdateInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    verifySetVariablesLocal(processInstanceId);
  }

  @Test
  public void testSetVariablesLocalWithUpdateInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    verifySetVariablesLocal(processInstanceId);
  }

  @Test
  public void testSetVariablesLocalWithUpdateVariablePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE_VARIABLE);

    verifySetVariablesLocal(processInstanceId);
  }

  @Test
  public void testSetVariablesLocalWithUpdateVariablePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE_VARIABLE);

    verifySetVariablesLocal(processInstanceId);
  }

  @Test
  public void testSetVariablesLocalWithUpdateInstanceVariablePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE_VARIABLE);

    verifySetVariablesLocal(processInstanceId);
  }

  @Test
  public void testSetVariablesLocalWithUpdateInstanceVariablePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE_VARIABLE);

    verifySetVariablesLocal(processInstanceId);
  }

  // RuntimeService#removeVariable() ////////////////////////////////////////////

  @Test
  public void testRemoveVariableWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();

    try {
      // when
      runtimeService.removeVariable(processInstanceId, VARIABLE_NAME);
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then
      verifyMessageIsValid(processInstanceId, e.getMessage());
    }
  }

  @Test
  public void testRemoveVariableWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    verifyRemoveVariable(processInstanceId);
  }

  @Test
  public void testRemoveVariableWithUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    verifyRemoveVariable(processInstanceId);
  }

  @Test
  public void testRemoveVariableWithUpdateInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    verifyRemoveVariable(processInstanceId);
  }

  @Test
  public void testRemoveVariableWithUpdateInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    verifyRemoveVariable(processInstanceId);
  }

  @Test
  public void testRemoveVariableWithUpdateVariablePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE_VARIABLE);

    verifyRemoveVariable(processInstanceId);
  }

  @Test
  public void testRemoveVariableWithUpdateVariablePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE_VARIABLE);

    verifyRemoveVariable(processInstanceId);
  }

  @Test
  public void testRemoveVariableWithUpdateInstanceVariablePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE_VARIABLE);

    verifyRemoveVariable(processInstanceId);
  }

  @Test
  public void testRemoveVariableWithUpdateInstanceVariablePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE_VARIABLE);

    verifyRemoveVariable(processInstanceId);
  }

  // RuntimeService#removeVariableLocal() ////////////////////////////////////////////

  @Test
  public void testRemoveVariableLocalWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();

    try {
      // when
      runtimeService.removeVariableLocal(processInstanceId, VARIABLE_NAME);
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then
      verifyMessageIsValid(processInstanceId, e.getMessage());
    }
  }

  @Test
  public void testRemoveVariableLocalWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    verifyRemoveVariableLocal(processInstanceId);
  }

  @Test
  public void testRemoveVariableLocalWithUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    verifyRemoveVariableLocal(processInstanceId);
  }

  @Test
  public void testRemoveVariableLocalWithUpdateInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    verifyRemoveVariableLocal(processInstanceId);
  }

  @Test
  public void testRemoveVariableLocalWithUpdateInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    verifyRemoveVariableLocal(processInstanceId);
  }

  @Test
  public void testRemoveVariableLocalWithUpdateVariablePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE_VARIABLE);

    verifyRemoveVariableLocal(processInstanceId);
  }

  @Test
  public void testRemoveVariableLocalWithUpdateVariablePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE_VARIABLE);

    verifyRemoveVariableLocal(processInstanceId);
  }

  @Test
  public void testRemoveVariableLocalWithUpdateInstanceVariablePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE_VARIABLE);

    verifyRemoveVariableLocal(processInstanceId);
  }

  @Test
  public void testRemoveVariableLocalWithUpdateInstanceVariablePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE_VARIABLE);

    verifyRemoveVariableLocal(processInstanceId);
  }

  // RuntimeService#removeVariables() ////////////////////////////////////////////

  @Test
  public void testRemoveVariablesWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();

    try {
      // when
      runtimeService.removeVariables(processInstanceId, Arrays.asList(VARIABLE_NAME));
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then
      verifyMessageIsValid(processInstanceId, e.getMessage());
    }
  }

  @Test
  public void testRemoveVariablesWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    verifyRemoveVariables(processInstanceId);
  }

  @Test
  public void testRemoveVariablesWithUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    verifyRemoveVariables(processInstanceId);
  }

  @Test
  public void testRemoveVariablesWithUpdateInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    verifyRemoveVariables(processInstanceId);
  }

  @Test
  public void testRemoveVariablesWithUpdateInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    verifyRemoveVariables(processInstanceId);
  }

  @Test
  public void testRemoveVariablesWithUpdateVariablePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE_VARIABLE);

    verifyRemoveVariables(processInstanceId);
  }

  @Test
  public void testRemoveVariablesWithUpdateVariablePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE_VARIABLE);

    verifyRemoveVariables(processInstanceId);
  }

  @Test
  public void testRemoveVariablesWithUpdateInstanceVariablePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE_VARIABLE);

    verifyRemoveVariables(processInstanceId);
  }

  @Test
  public void testRemoveVariablesWithUpdateInstanceVariablePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE_VARIABLE);

    verifyRemoveVariables(processInstanceId);
  }

  // RuntimeService#removeVariablesLocal() ////////////////////////////////////////////

  @Test
  public void testRemoveVariablesLocalWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();

    try {
      // when
      runtimeService.removeVariablesLocal(processInstanceId, Arrays.asList(VARIABLE_NAME));
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then
      verifyMessageIsValid(processInstanceId, e.getMessage());
    }
  }

  @Test
  public void testRemoveVariablesLocalWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    verifyRemoveVariablesLocal(processInstanceId);
  }

  @Test
  public void testRemoveVariablesLocalWithUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    verifyRemoveVariablesLocal(processInstanceId);
  }

  @Test
  public void testRemoveVariablesLocalWithUpdateInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    verifyRemoveVariablesLocal(processInstanceId);
  }

  @Test
  public void testRemoveVariablesLocalWithUpdateInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    verifyRemoveVariablesLocal(processInstanceId);
  }

  @Test
  public void testRemoveVariablesLocalWithUpdateVariablePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE_VARIABLE);

    verifyRemoveVariablesLocal(processInstanceId);
  }

  @Test
  public void testRemoveVariablesLocalWithUpdateVariablePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE_VARIABLE);

    verifyRemoveVariablesLocal(processInstanceId);
  }

  @Test
  public void testRemoveVariablesLocalWithUpdateInstanceVariablePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE_VARIABLE);

    verifyRemoveVariablesLocal(processInstanceId);
  }

  @Test
  public void testRemoveVariablesLocalWithUpdateInstanceVariablePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE_VARIABLE);

    verifyRemoveVariablesLocal(processInstanceId);
  }

  // RuntimeServiceImpl#updateVariables() ////////////////////////////////////////////

  @Test
  public void testUpdateVariablesWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    try {
      // when (1)
      ((RuntimeServiceImpl)runtimeService).updateVariables(processInstanceId, getVariables(), null);
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then (1)
      verifyMessageIsValid(processInstanceId, e.getMessage());
    }

    try {
      // when (2)
      ((RuntimeServiceImpl)runtimeService).updateVariables(processInstanceId, null, Arrays.asList(VARIABLE_NAME));
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then (2)
      verifyMessageIsValid(processInstanceId, e.getMessage());
    }

    try {
      // when (3)
      ((RuntimeServiceImpl)runtimeService).updateVariables(processInstanceId, getVariables(), Arrays.asList(VARIABLE_NAME));
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then (3)
      verifyMessageIsValid(processInstanceId, e.getMessage());
    }
  }

  @Test
  public void testUpdateVariablesWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    verifyUpdateVariables(processInstanceId);
  }

  @Test
  public void testUpdateVariablesWithUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    verifyUpdateVariables(processInstanceId);
  }

  @Test
  public void testUpdateVariablesWithUpdateInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    verifyUpdateVariables(processInstanceId);
  }

  @Test
  public void testUpdateVariablesWithUpdateInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    verifyUpdateVariables(processInstanceId);
  }

  @Test
  public void testUpdateVariablesWithUpdateVariablePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE_VARIABLE);

    verifyUpdateVariables(processInstanceId);
  }

  @Test
  public void testUpdateVariablesWithUpdateVariablePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE_VARIABLE);

    verifyUpdateVariables(processInstanceId);
  }

  @Test
  public void testUpdateVariablesWithUpdateInstanceVariablePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE_VARIABLE);

    verifyUpdateVariables(processInstanceId);
  }

  @Test
  public void testUpdateVariablesWithUpdateInstanceVariablePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE_VARIABLE);

    verifyUpdateVariables(processInstanceId);
  }

  // RuntimeServiceImpl#updateVariablesLocal() ////////////////////////////////////////////

  @Test
  public void testUpdateVariablesLocalWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    try {
      // when (1)
      ((RuntimeServiceImpl)runtimeService).updateVariablesLocal(processInstanceId, getVariables(), null);
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then (1)
      verifyMessageIsValid(processInstanceId, e.getMessage());
    }

    try {
      // when (2)
      ((RuntimeServiceImpl)runtimeService).updateVariablesLocal(processInstanceId, null, Arrays.asList(VARIABLE_NAME));
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then (2)
      verifyMessageIsValid(processInstanceId, e.getMessage());
    }

    try {
      // when (3)
      ((RuntimeServiceImpl)runtimeService).updateVariablesLocal(processInstanceId, getVariables(), Arrays.asList(VARIABLE_NAME));
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then (3)
      verifyMessageIsValid(processInstanceId, e.getMessage());
    }
  }

  @Test
  public void testUpdateVariablesLocalWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    verifyUpdateVariablesLocal(processInstanceId);
  }

  @Test
  public void testUpdateVariablesLocalWithUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    verifyUpdateVariablesLocal(processInstanceId);
  }

  @Test
  public void testUpdateVariablesLocalWithUpdateInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    verifyUpdateVariablesLocal(processInstanceId);
  }

  @Test
  public void testUpdateVariablesLocalWithUpdateInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    verifyUpdateVariablesLocal(processInstanceId);
  }

  @Test
  public void testUpdateVariablesLocalWithUpdateVariablePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE_VARIABLE);

    verifyUpdateVariablesLocal(processInstanceId);
  }

  @Test
  public void testUpdateVariablesLocalWithUpdateVariablePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE_VARIABLE);

    verifyUpdateVariablesLocal(processInstanceId);
  }

  @Test
  public void testUpdateVariablesLocalWithUpdateInstanceVariablePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE_VARIABLE);

    verifyUpdateVariablesLocal(processInstanceId);
  }

  @Test
  public void testUpdateVariablesLocalWithUpdateInstanceVariablePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE_VARIABLE);

    verifyUpdateVariablesLocal(processInstanceId);
  }

  // helper /////////////////////////////////////////////////////

  protected void verifyQueryResults(ProcessInstanceQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

  protected void verifyQueryResults(VariableInstanceQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

  protected void verifyMessageIsValid(String processInstanceId, String message) {
    testRule.assertTextPresent(userId, message);
    testRule.assertTextPresent(UPDATE.getName(), message);
    testRule.assertTextPresent(UPDATE_VARIABLE.getName(), message);
    testRule.assertTextPresent(processInstanceId, message);
    testRule.assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
    testRule.assertTextPresent(UPDATE_INSTANCE.getName(), message);
    testRule.assertTextPresent(UPDATE_INSTANCE_VARIABLE.getName(), message);
    testRule.assertTextPresent(PROCESS_KEY, message);
    testRule.assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
  }

  protected void verifyVariableInstanceCountDisabledAuthorization(int count) {
    disableAuthorization();
    verifyQueryResults(runtimeService.createVariableInstanceQuery(), count);
    enableAuthorization();
  }

  protected void verifySetVariable(String processInstanceId) {
    // when
    runtimeService.setVariable(processInstanceId, VARIABLE_NAME, VARIABLE_VALUE);

    // then
    verifyVariableInstanceCountDisabledAuthorization(1);
  }

  protected void verifySetVariableLocal(String processInstanceId) {
    // when
    runtimeService.setVariableLocal(processInstanceId, VARIABLE_NAME, VARIABLE_VALUE);

    // then
    verifyVariableInstanceCountDisabledAuthorization(1);
  }

  protected void verifySetVariables(String processInstanceId) {
    // when
    runtimeService.setVariables(processInstanceId, getVariables());

    // then
    verifyVariableInstanceCountDisabledAuthorization(1);
  }

  protected void verifySetVariablesLocal(String processInstanceId) {
    // when
    runtimeService.setVariablesLocal(processInstanceId, getVariables());

    // then
    verifyVariableInstanceCountDisabledAuthorization(1);
  }

  protected void verifyRemoveVariable(String processInstanceId) {
    // when
    runtimeService.removeVariable(processInstanceId, VARIABLE_NAME);

    // then
    verifyVariableInstanceCountDisabledAuthorization(0);
  }

  protected void verifyRemoveVariableLocal(String processInstanceId) {
    // when
    runtimeService.removeVariableLocal(processInstanceId, VARIABLE_NAME);

    // then
    verifyVariableInstanceCountDisabledAuthorization(0);
  }

  protected void verifyRemoveVariables(String processInstanceId) {
    // when
    runtimeService.removeVariables(processInstanceId, Arrays.asList(VARIABLE_NAME));

    // then
    verifyVariableInstanceCountDisabledAuthorization(0);
  }

  protected void verifyRemoveVariablesLocal(String processInstanceId) {
    // when
    runtimeService.removeVariablesLocal(processInstanceId, Arrays.asList(VARIABLE_NAME));

    // then
    verifyVariableInstanceCountDisabledAuthorization(0);
  }

  protected void verifyUpdateVariables(String processInstanceId) {
    // when (1)
    ((RuntimeServiceImpl)runtimeService).updateVariables(processInstanceId, getVariables(), null);

    // then (1)
    verifyVariableInstanceCountDisabledAuthorization(1);

    // when (2)
    ((RuntimeServiceImpl)runtimeService).updateVariables(processInstanceId, null, Arrays.asList(VARIABLE_NAME));

    // then (2)
    verifyVariableInstanceCountDisabledAuthorization(0);

    // when (3)
    ((RuntimeServiceImpl)runtimeService).updateVariables(processInstanceId, getVariables(), Arrays.asList(VARIABLE_NAME));

    // then (3)
    verifyVariableInstanceCountDisabledAuthorization(0);
  }

  protected void verifyUpdateVariablesLocal(String processInstanceId) {
    // when (1)
    ((RuntimeServiceImpl)runtimeService).updateVariablesLocal(processInstanceId, getVariables(), null);

    // then (1)
    verifyVariableInstanceCountDisabledAuthorization(1);

    // when (2)
    ((RuntimeServiceImpl)runtimeService).updateVariablesLocal(processInstanceId, null, Arrays.asList(VARIABLE_NAME));

    // then (2)
    verifyVariableInstanceCountDisabledAuthorization(0);
  
    // when (3)
    ((RuntimeServiceImpl)runtimeService).updateVariablesLocal(processInstanceId, getVariables(), Arrays.asList(VARIABLE_NAME));

    // then (3)
    verifyVariableInstanceCountDisabledAuthorization(0);
  }

  protected void setReadVariableAsDefaultReadVariablePermission() {
    processEngineConfiguration.setEnforceSpecificVariablePermission(true);
  }

  protected void verifyGetVariables(Map<String, Object> variables) {
    assertNotNull(variables);
    assertFalse(variables.isEmpty());
    assertEquals(1, variables.size());

    assertEquals(VARIABLE_VALUE, variables.get(VARIABLE_NAME));
  }

}
