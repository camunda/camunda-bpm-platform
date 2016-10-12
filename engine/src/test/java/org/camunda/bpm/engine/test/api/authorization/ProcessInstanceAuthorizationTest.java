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
import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.TASK;

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

  protected String deploymentId;

  @Override
  public void setUp() throws Exception {
    deploymentId = createDeployment(null,
        "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/messageStartEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/messageBoundaryEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/signalBoundaryEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/signalStartEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/throwWarningSignalEventProcess.bpmn20.xml",
        "org/camunda/bpm/engine/test/api/authorization/throwAlertSignalEventProcess.bpmn20.xml"
        ).getId();
    super.setUp();
  }

  @Override
  public void tearDown() {
    super.tearDown();
    deleteDeployment(deploymentId);
  }

  // process instance query //////////////////////////////////////////////////////////

  public void testSimpleQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(PROCESS_KEY);

    // when
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    // then
    verifyQueryResults(query, 0);
  }

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

  public void testStartProcessInstanceByKeyWithoutAuthorization() {
    // given
    // no authorization to start a process instance

    try {
      // when
      runtimeService.startProcessInstanceByKey(PROCESS_KEY);
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  public void testStartProcessInstanceByKeyWithCreatePermissionOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    try {
      // when
      runtimeService.startProcessInstanceByKey(PROCESS_KEY);
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      assertTextPresent("The user with id 'test' does not have 'CREATE_INSTANCE' permission on resource 'oneTaskProcess' of type 'ProcessDefinition'", e.getMessage());
    }
  }

  public void testStartProcessInstanceByKeyWithCreateInstancesPermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, CREATE_INSTANCE);

    try {
      // when
      runtimeService.startProcessInstanceByKey(PROCESS_KEY);
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

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
      assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

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
      assertTextPresent("The user with id 'test' does not have 'CREATE_INSTANCE' permission on resource 'oneTaskProcess' of type 'ProcessDefinition'", e.getMessage());
    }
  }

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
      assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

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

  public void testStartProcessInstanceAtActivitiesByKeyWithoutAuthorization() {
    // given
    // no authorization to start a process instance

    try {
      // when
      runtimeService.createProcessInstanceByKey(PROCESS_KEY).startBeforeActivity("theTask").execute();
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  public void testStartProcessInstanceAtActivitiesByKeyWithCreatePermissionOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    try {
      // when
      runtimeService.createProcessInstanceByKey(PROCESS_KEY).startBeforeActivity("theTask").execute();
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      assertTextPresent("The user with id 'test' does not have 'CREATE_INSTANCE' permission on resource 'oneTaskProcess' of type 'ProcessDefinition'", e.getMessage());
    }
  }

  public void testStartProcessInstanceAtActivitiesByKeyWithCreateInstancesPermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, CREATE_INSTANCE);

    try {
      // when
      runtimeService.createProcessInstanceByKey(PROCESS_KEY).startBeforeActivity("theTask").execute();
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

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
      assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

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
      assertTextPresent("The user with id 'test' does not have 'CREATE_INSTANCE' permission on resource 'oneTaskProcess' of type 'ProcessDefinition'", e.getMessage());
    }
  }

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
      assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  // start process instance by message //////////////////////////////////////////////

  public void testStartProcessInstanceByMessageWithoutAuthorization() {
    // given
    // no authorization to start a process instance

    try {
      // when
      runtimeService.startProcessInstanceByMessage("startInvoiceMessage");
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  public void testStartProcessInstanceByMessageWithCreatePermissionOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    try {
      // when
      runtimeService.startProcessInstanceByMessage("startInvoiceMessage");
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      assertTextPresent("The user with id 'test' does not have 'CREATE_INSTANCE' permission on resource 'messageStartProcess' of type 'ProcessDefinition'", e.getMessage());
    }
  }

  public void testStartProcessInstanceByMessageWithCreateInstancesPermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_START_PROCESS_KEY, userId, CREATE_INSTANCE);

    try {
      // when
      runtimeService.startProcessInstanceByMessage("startInvoiceMessage");
      fail("Exception expected: It should not be possible to start a process instance");
    } catch (AuthorizationException e) {

      // then
      assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

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
      assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

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
      assertTextPresent("The user with id 'test' does not have 'CREATE_INSTANCE' permission on resource 'messageStartProcess' of type 'ProcessDefinition'", e.getMessage());
    }
  }

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
      assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

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
      assertTextPresent(userId, message);
      assertTextPresent(DELETE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(DELETE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testDeleteProcessInstanceWithDeletePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, DELETE);

    // when
    runtimeService.deleteProcessInstance(processInstanceId, null);

    // then
    disableAuthorization();
    assertProcessEnded(processInstanceId);
    enableAuthorization();
  }

  public void testDeleteProcessInstanceWithDeletePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, DELETE);

    // when
    runtimeService.deleteProcessInstance(processInstanceId, null);

    // then
    disableAuthorization();
    assertProcessEnded(processInstanceId);
    enableAuthorization();
  }

  public void testDeleteProcessInstanceWithDeleteInstancesPermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, DELETE_INSTANCE);

    // when
    runtimeService.deleteProcessInstance(processInstanceId, null);

    // then
    disableAuthorization();
    assertProcessEnded(processInstanceId);
    enableAuthorization();
  }

  public void testDeleteProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, DELETE);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, DELETE_INSTANCE);

    // when
    runtimeService.deleteProcessInstance(processInstanceId, null);

    // then
    disableAuthorization();
    assertProcessEnded(processInstanceId);
    enableAuthorization();
  }

  // get active activity ids ///////////////////////////////////

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
//      assertTextPresent(userId, message);
//      assertTextPresent(READ.getName(), message);
//      assertTextPresent(processInstanceId, message);
//      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
    }
  }

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
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(READ_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testGetActivityInstanceWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstanceId);

    // then
    assertNotNull(activityInstance);
  }

  public void testGetActivityInstanceWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstanceId);

    // then
    assertNotNull(activityInstance);
  }

  public void testGetActivityInstanceWithReadInstancesPermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstanceId);

    // then
    assertNotNull(activityInstance);
  }

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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSignalWithUpdatePermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    // when
    runtimeService.signal(processInstanceId);

    // then
    assertProcessEnded(processInstanceId);
  }

  public void testSignalWithUpdatePermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    runtimeService.signal(processInstanceId);

    // then
    assertProcessEnded(processInstanceId);
  }

  public void testSignalWithUpdateInstancesPermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.signal(processInstanceId);

    // then
    assertProcessEnded(processInstanceId);
  }

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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(SIGNAL_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(secondProcessInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(SIGNAL_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(SIGNAL_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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

  public void testStartProcessInstanceBySignalEventReceivedWithoutAuthorization() {
    // given
    // no authorization to start a process instance

    try {
      // when
      runtimeService.signalEventReceived("warning");
      fail("Exception expected");
    } catch (AuthorizationException e) {
      // then
      assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  public void testStartProcessInstanceBySignalEventReceivedWithCreatePermissionOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    try {
      // when
      runtimeService.signalEventReceived("warning");
      fail("Exception expected");
    } catch (AuthorizationException e) {
      // then
      assertTextPresent("The user with id 'test' does not have 'CREATE_INSTANCE' permission on resource 'signalStartProcess' of type 'ProcessDefinition'", e.getMessage());
    }
  }

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
      assertTextPresent("The user with id 'test' does not have 'CREATE_INSTANCE' permission on resource 'signalStartProcess' of type 'ProcessDefinition'", e.getMessage());
    }
  }

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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(SIGNAL_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(MESSAGE_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(MESSAGE_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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

  public void testCorrelateMessageProcessDefinitionWithoutAuthorization() {
    // given

    try {
      // when
      runtimeService.correlateMessage("startInvoiceMessage");
      fail("Exception expected: It should not be possible to correlate a message.");
    } catch (AuthorizationException e) {
      // then
      assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

  public void testCorrelateMessageProcessDefinitionWithCreatePermissionOnProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, CREATE);

    try {
      // when
      runtimeService.correlateMessage("startInvoiceMessage");
      fail("Exception expected: It should not be possible to correlate a message.");
    } catch (AuthorizationException e) {

      // then
      assertTextPresent("The user with id 'test' does not have 'CREATE_INSTANCE' permission on resource 'messageStartProcess' of type 'ProcessDefinition'", e.getMessage());
    }
  }

  public void testCorrelateMessageProcessDefinitionWithCreateInstancesPermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, MESSAGE_START_PROCESS_KEY, userId, CREATE_INSTANCE);

    try {
      // when
      runtimeService.correlateMessage("startInvoiceMessage");
      fail("Exception expected: It should not be possible to correlate a message.");
    } catch (AuthorizationException e) {

      // then
      assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(MESSAGE_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(secondProcessInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(MESSAGE_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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
      assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

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
      assertTextPresent("The user with id 'test' does not have 'CREATE_INSTANCE' permission on resource 'messageStartProcess' of type 'ProcessDefinition'", e.getMessage());
    }
  }

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
      assertTextPresent("The user with id 'test' does not have 'CREATE' permission on resource 'ProcessInstance'", e.getMessage());
    }
  }

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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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

  public void testSuspendProcessInstanceById() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.suspendProcessInstanceById(processInstanceId);

    // then
    ProcessInstance instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  // activate process instance by id /////////////////////////////

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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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

  public void testActivateProcessInstanceById() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    suspendProcessInstanceById(processInstanceId);
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.activateProcessInstanceById(processInstanceId);

    // then
    ProcessInstance instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  // suspend process instance by process definition id /////////////////////////////

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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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

  public void testSuspendProcessInstanceByProcessDefinitionId() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    String processDefinitionId = instance.getProcessDefinitionId();

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.suspendProcessInstanceByProcessDefinitionId(processDefinitionId);

    // then
    instance = selectSingleProcessInstance();
    assertTrue(instance.isSuspended());
  }

  // activate process instance by process definition id /////////////////////////////

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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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

  public void testActivateProcessInstanceByProcessDefinitionId() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    String processDefinitionId = instance.getProcessDefinitionId();
    suspendProcessInstanceById(processInstanceId);

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.activateProcessInstanceByProcessDefinitionId(processDefinitionId);

    // then
    instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  // suspend process instance by process definition key /////////////////////////////

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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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

  // activate process instance by process definition key /////////////////////////////

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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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

  public void testActivateProcessInstanceByProcessDefinitionKey() {
    // given
    ProcessInstance instance = startProcessInstanceByKey(PROCESS_KEY);
    String processInstanceId = instance.getId();
    suspendProcessInstanceById(processInstanceId);

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.activateProcessInstanceByProcessDefinitionKey(PROCESS_KEY);

    // then
    instance = selectSingleProcessInstance();
    assertFalse(instance.isSuspended());
  }

  // modify process instance /////////////////////////////////////

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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(MESSAGE_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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
      assertTextPresent(userId, message);
      assertTextPresent(DELETE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(DELETE_INSTANCE.getName(), message);
      assertTextPresent(MESSAGE_BOUNDARY_PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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
    assertProcessEnded(processInstanceId);
  }

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
    assertProcessEnded(processInstanceId);
  }

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
    assertProcessEnded(processInstanceId);
  }

  // clear process instance authorization ////////////////////////

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
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(READ_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testGetVariableWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    Object variable = runtimeService.getVariable(processInstanceId, VARIABLE_NAME);

    // then
    assertEquals(VARIABLE_VALUE, variable);
  }

  public void testGetVariableWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    Object variable = runtimeService.getVariable(processInstanceId, VARIABLE_NAME);

    // then
    assertEquals(VARIABLE_VALUE, variable);
  }

  public void testGetVariableWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    Object variable = runtimeService.getVariable(processInstanceId, VARIABLE_NAME);

    // then
    assertEquals(VARIABLE_VALUE, variable);
  }

  public void testGetVariableWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    Object variable = runtimeService.getVariable(processInstanceId, VARIABLE_NAME);

    // then
    assertEquals(VARIABLE_VALUE, variable);
  }

  // RuntimeService#getVariableLocal() ////////////////////////////////////////////

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
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(READ_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testGetVariableLocalWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    Object variable = runtimeService.getVariableLocal(processInstanceId, VARIABLE_NAME);

    // then
    assertEquals(VARIABLE_VALUE, variable);
  }

  public void testGetVariableLocalWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    Object variable = runtimeService.getVariableLocal(processInstanceId, VARIABLE_NAME);

    // then
    assertEquals(VARIABLE_VALUE, variable);
  }

  public void testGetVariableLocalWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    Object variable = runtimeService.getVariableLocal(processInstanceId, VARIABLE_NAME);

    // then
    assertEquals(VARIABLE_VALUE, variable);
  }

  public void testGetVariableLocalWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    Object variable = runtimeService.getVariableLocal(processInstanceId, VARIABLE_NAME);

    // then
    assertEquals(VARIABLE_VALUE, variable);
  }

  // RuntimeService#getVariableTyped() ////////////////////////////////////////////

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
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(READ_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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

  // RuntimeService#getVariableLocalTyped() ////////////////////////////////////////////

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
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(READ_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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

  // RuntimeService#getVariables() ////////////////////////////////////////////

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
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(READ_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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

  // RuntimeService#getVariablesLocal() ////////////////////////////////////////////

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
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(READ_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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

  // RuntimeService#getVariablesTyped() ////////////////////////////////////////////

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
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(READ_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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

  // RuntimeService#getVariablesLocalTyped() ////////////////////////////////////////////

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
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(READ_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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

  // RuntimeService#getVariables() ////////////////////////////////////////////

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
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(READ_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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

  // RuntimeService#getVariablesLocal() ////////////////////////////////////////////

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
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(READ_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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

  // RuntimeService#getVariablesTyped() ////////////////////////////////////////////

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
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(READ_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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

  // RuntimeService#getVariablesLocalTyped() ////////////////////////////////////////////

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
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(READ_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

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

  // RuntimeService#setVariable() ////////////////////////////////////////////

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
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSetVariableWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    // when
    runtimeService.setVariable(processInstanceId, VARIABLE_NAME, VARIABLE_VALUE);

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();
  }

  public void testSetVariableWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    runtimeService.setVariable(processInstanceId, VARIABLE_NAME, VARIABLE_VALUE);

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();
  }

  public void testSetVariableWithReadInstancePermissionOnProcessDefinition() {
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

  public void testSetVariableWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.setVariable(processInstanceId, VARIABLE_NAME, VARIABLE_VALUE);

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();
  }

  // RuntimeService#setVariableLocal() ////////////////////////////////////////////

  public void testSetVariableLocalWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    try {
      // when
      runtimeService.setVariableLocal(processInstanceId, VARIABLE_NAME, VARIABLE_VALUE);
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSetVariableLocalWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    // when
    runtimeService.setVariableLocal(processInstanceId, VARIABLE_NAME, VARIABLE_VALUE);

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();
  }

  public void testSetVariableLocalWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    runtimeService.setVariableLocal(processInstanceId, VARIABLE_NAME, VARIABLE_VALUE);

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();
  }

  public void testSetVariableLocalWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.setVariableLocal(processInstanceId, VARIABLE_NAME, VARIABLE_VALUE);

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();
  }

  public void testSetVariableLocalWithReadInstancePermissionOnAnyProcessDefinition() {
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

  // RuntimeService#setVariables() ////////////////////////////////////////////

  public void testSetVariablesWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    try {
      // when
      runtimeService.setVariables(processInstanceId, getVariables());
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSetVariablesWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    // when
    runtimeService.setVariables(processInstanceId, getVariables());

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();
  }

  public void testSetVariablesWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    runtimeService.setVariables(processInstanceId, getVariables());

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();
  }

  public void testSetVariablesWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.setVariables(processInstanceId, getVariables());

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();
  }

  public void testSetVariablesWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.setVariables(processInstanceId, getVariables());

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();
  }

  // RuntimeService#setVariablesLocal() ////////////////////////////////////////////

  public void testSetVariablesLocalWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    try {
      // when
      runtimeService.setVariablesLocal(processInstanceId, getVariables());
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testSetVariablesLocalWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    // when
    runtimeService.setVariablesLocal(processInstanceId, getVariables());

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();
  }

  public void testSetVariablesLocalWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    runtimeService.setVariablesLocal(processInstanceId, getVariables());

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();
  }

  public void testSetVariablesLocalWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.setVariablesLocal(processInstanceId, getVariables());

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();
  }

  public void testSetVariablesLocalWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.setVariablesLocal(processInstanceId, getVariables());

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();
  }

  // RuntimeService#removeVariable() ////////////////////////////////////////////

  public void testRemoveVariableWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();

    try {
      // when
      runtimeService.removeVariable(processInstanceId, VARIABLE_NAME);
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testRemoveVariableWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    // when
    runtimeService.removeVariable(processInstanceId, VARIABLE_NAME);

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  public void testRemoveVariableWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    runtimeService.removeVariable(processInstanceId, VARIABLE_NAME);

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  public void testRemoveVariableWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.removeVariable(processInstanceId, VARIABLE_NAME);

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  public void testRemoveVariableWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.removeVariable(processInstanceId, VARIABLE_NAME);

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  // RuntimeService#removeVariableLocal() ////////////////////////////////////////////

  public void testRemoveVariableLocalWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();

    try {
      // when
      runtimeService.removeVariableLocal(processInstanceId, VARIABLE_NAME);
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testRemoveVariableLocalWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    // when
    runtimeService.removeVariableLocal(processInstanceId, VARIABLE_NAME);

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  public void testRemoveVariableLocalWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    runtimeService.removeVariableLocal(processInstanceId, VARIABLE_NAME);

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  public void testRemoveVariableLocalWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.removeVariableLocal(processInstanceId, VARIABLE_NAME);

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  public void testRemoveVariableLocalWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.removeVariableLocal(processInstanceId, VARIABLE_NAME);

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  // RuntimeService#removeVariables() ////////////////////////////////////////////

  public void testRemoveVariablesWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();

    try {
      // when
      runtimeService.removeVariables(processInstanceId, Arrays.asList(VARIABLE_NAME));
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testRemoveVariablesWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    // when
    runtimeService.removeVariables(processInstanceId, Arrays.asList(VARIABLE_NAME));

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  public void testRemoveVariablesWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    runtimeService.removeVariables(processInstanceId, Arrays.asList(VARIABLE_NAME));

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  public void testRemoveVariablesWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.removeVariables(processInstanceId, Arrays.asList(VARIABLE_NAME));

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  public void testRemoveVariablesWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.removeVariables(processInstanceId, Arrays.asList(VARIABLE_NAME));

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  // RuntimeService#removeVariablesLocal() ////////////////////////////////////////////

  public void testRemoveVariablesLocalWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();

    try {
      // when
      runtimeService.removeVariablesLocal(processInstanceId, Arrays.asList(VARIABLE_NAME));
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testRemoveVariablesLocalWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);

    // when
    runtimeService.removeVariablesLocal(processInstanceId, Arrays.asList(VARIABLE_NAME));

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  public void testRemoveVariablesLocalWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);

    // when
    runtimeService.removeVariablesLocal(processInstanceId, Arrays.asList(VARIABLE_NAME));

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  public void testRemoveVariablesLocalWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.removeVariablesLocal(processInstanceId, Arrays.asList(VARIABLE_NAME));

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  public void testRemoveVariablesLocalWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY, getVariables()).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);

    // when
    runtimeService.removeVariablesLocal(processInstanceId, Arrays.asList(VARIABLE_NAME));

    // then
    disableAuthorization();
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  // RuntimeServiceImpl#updateVariables() ////////////////////////////////////////////

  public void testUpdateVariablesWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    try {
      // when (1)
      ((RuntimeServiceImpl)runtimeService).updateVariables(processInstanceId, getVariables(), null);
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then (1)
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }

    try {
      // when (2)
      ((RuntimeServiceImpl)runtimeService).updateVariables(processInstanceId, null, Arrays.asList(VARIABLE_NAME));
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then (2)
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }

    try {
      // when (3)
      ((RuntimeServiceImpl)runtimeService).updateVariables(processInstanceId, getVariables(), Arrays.asList(VARIABLE_NAME));
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then (3)
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testUpdateVariablesWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // when (1)
    ((RuntimeServiceImpl)runtimeService).updateVariables(processInstanceId, getVariables(), null);

    // then (1)
    disableAuthorization();
    verifyQueryResults(query, 1);
    enableAuthorization();

    // when (2)
    ((RuntimeServiceImpl)runtimeService).updateVariables(processInstanceId, null, Arrays.asList(VARIABLE_NAME));

    // then (2)
    disableAuthorization();
    verifyQueryResults(query, 0);
    enableAuthorization();

    // when (3)
    ((RuntimeServiceImpl)runtimeService).updateVariables(processInstanceId, getVariables(), Arrays.asList(VARIABLE_NAME));

    // then (3)
    disableAuthorization();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  public void testUpdateVariablesWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // when (1)
    ((RuntimeServiceImpl)runtimeService).updateVariables(processInstanceId, getVariables(), null);

    // then (1)
    disableAuthorization();
    verifyQueryResults(query, 1);
    enableAuthorization();

    // when (2)
    ((RuntimeServiceImpl)runtimeService).updateVariables(processInstanceId, null, Arrays.asList(VARIABLE_NAME));

    // then (2)
    disableAuthorization();
    verifyQueryResults(query, 0);
    enableAuthorization();

    // when (3)
    ((RuntimeServiceImpl)runtimeService).updateVariables(processInstanceId, getVariables(), Arrays.asList(VARIABLE_NAME));

    // then (3)
    disableAuthorization();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  public void testUpdateVariablesWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // when (1)
    ((RuntimeServiceImpl)runtimeService).updateVariables(processInstanceId, getVariables(), null);

    // then (1)
    disableAuthorization();
    verifyQueryResults(query, 1);
    enableAuthorization();

    // when (2)
    ((RuntimeServiceImpl)runtimeService).updateVariables(processInstanceId, null, Arrays.asList(VARIABLE_NAME));

    // then (2)
    disableAuthorization();
    verifyQueryResults(query, 0);
    enableAuthorization();

    // when (3)
    ((RuntimeServiceImpl)runtimeService).updateVariables(processInstanceId, getVariables(), Arrays.asList(VARIABLE_NAME));

    // then (3)
    disableAuthorization();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  public void testUpdateVariablesWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // when (1)
    ((RuntimeServiceImpl)runtimeService).updateVariables(processInstanceId, getVariables(), null);

    // then (1)
    disableAuthorization();
    verifyQueryResults(query, 1);
    enableAuthorization();

    // when (2)
    ((RuntimeServiceImpl)runtimeService).updateVariables(processInstanceId, null, Arrays.asList(VARIABLE_NAME));

    // then (2)
    disableAuthorization();
    verifyQueryResults(query, 0);
    enableAuthorization();

    // when (3)
    ((RuntimeServiceImpl)runtimeService).updateVariables(processInstanceId, getVariables(), Arrays.asList(VARIABLE_NAME));

    // then (3)
    disableAuthorization();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  // RuntimeServiceImpl#updateVariablesLocal() ////////////////////////////////////////////

  public void testUpdateVariablesLocalWithoutAuthorization() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();

    try {
      // when (1)
      ((RuntimeServiceImpl)runtimeService).updateVariablesLocal(processInstanceId, getVariables(), null);
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then (1)
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }

    try {
      // when (2)
      ((RuntimeServiceImpl)runtimeService).updateVariablesLocal(processInstanceId, null, Arrays.asList(VARIABLE_NAME));
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then (2)
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }

    try {
      // when (3)
      ((RuntimeServiceImpl)runtimeService).updateVariablesLocal(processInstanceId, getVariables(), Arrays.asList(VARIABLE_NAME));
      fail("Exception expected: It should not be to set a variable");
    } catch (AuthorizationException e) {
      // then (3)
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(processInstanceId, message);
      assertTextPresent(PROCESS_INSTANCE.resourceName(), message);
      assertTextPresent(UPDATE_INSTANCE.getName(), message);
      assertTextPresent(PROCESS_KEY, message);
      assertTextPresent(PROCESS_DEFINITION.resourceName(), message);
    }
  }

  public void testUpdateVariablesLocalWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, UPDATE);
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // when (1)
    ((RuntimeServiceImpl)runtimeService).updateVariablesLocal(processInstanceId, getVariables(), null);

    // then (1)
    disableAuthorization();
    verifyQueryResults(query, 1);
    enableAuthorization();

    // when (2)
    ((RuntimeServiceImpl)runtimeService).updateVariablesLocal(processInstanceId, null, Arrays.asList(VARIABLE_NAME));

    // then (2)
    disableAuthorization();
    verifyQueryResults(query, 0);
    enableAuthorization();

    // when (3)
    ((RuntimeServiceImpl)runtimeService).updateVariablesLocal(processInstanceId, getVariables(), Arrays.asList(VARIABLE_NAME));

    // then (3)
    disableAuthorization();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  public void testUpdateVariablesLocalWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, UPDATE);
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // when (1)
    ((RuntimeServiceImpl)runtimeService).updateVariablesLocal(processInstanceId, getVariables(), null);

    // then (1)
    disableAuthorization();
    verifyQueryResults(query, 1);
    enableAuthorization();

    // when (2)
    ((RuntimeServiceImpl)runtimeService).updateVariablesLocal(processInstanceId, null, Arrays.asList(VARIABLE_NAME));

    // then (2)
    disableAuthorization();
    verifyQueryResults(query, 0);
    enableAuthorization();

    // when (3)
    ((RuntimeServiceImpl)runtimeService).updateVariablesLocal(processInstanceId, getVariables(), Arrays.asList(VARIABLE_NAME));

    // then (3)
    disableAuthorization();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  public void testUpdateVariablesLocalWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, UPDATE_INSTANCE);
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // when (1)
    ((RuntimeServiceImpl)runtimeService).updateVariablesLocal(processInstanceId, getVariables(), null);

    // then (1)
    disableAuthorization();
    verifyQueryResults(query, 1);
    enableAuthorization();

    // when (2)
    ((RuntimeServiceImpl)runtimeService).updateVariablesLocal(processInstanceId, null, Arrays.asList(VARIABLE_NAME));

    // then (2)
    disableAuthorization();
    verifyQueryResults(query, 0);
    enableAuthorization();

    // when (3)
    ((RuntimeServiceImpl)runtimeService).updateVariablesLocal(processInstanceId, getVariables(), Arrays.asList(VARIABLE_NAME));

    // then (3)
    disableAuthorization();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  public void testUpdateVariablesLocalWithReadInstancePermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessInstanceByKey(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, UPDATE_INSTANCE);
    VariableInstanceQuery query = runtimeService.createVariableInstanceQuery();

    // when (1)
    ((RuntimeServiceImpl)runtimeService).updateVariablesLocal(processInstanceId, getVariables(), null);

    // then (1)
    disableAuthorization();
    verifyQueryResults(query, 1);
    enableAuthorization();

    // when (2)
    ((RuntimeServiceImpl)runtimeService).updateVariablesLocal(processInstanceId, null, Arrays.asList(VARIABLE_NAME));

    // then (2)
    disableAuthorization();
    verifyQueryResults(query, 0);
    enableAuthorization();

    // when (3)
    ((RuntimeServiceImpl)runtimeService).updateVariablesLocal(processInstanceId, getVariables(), Arrays.asList(VARIABLE_NAME));

    // then (3)
    disableAuthorization();
    verifyQueryResults(query, 0);
    enableAuthorization();
  }

  // helper /////////////////////////////////////////////////////

  protected void verifyQueryResults(ProcessInstanceQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

  protected void verifyQueryResults(VariableInstanceQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

}
