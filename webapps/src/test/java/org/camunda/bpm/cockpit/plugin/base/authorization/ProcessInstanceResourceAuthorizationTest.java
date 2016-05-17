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
package org.camunda.bpm.cockpit.plugin.base.authorization;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import static org.fest.assertions.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.cockpit.impl.plugin.base.dto.CalledProcessInstanceDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.query.CalledProcessInstanceQueryDto;
import org.camunda.bpm.cockpit.impl.plugin.base.sub.resources.ProcessInstanceResource;
import org.camunda.bpm.engine.impl.db.AuthorizationCheck;
import org.camunda.bpm.engine.impl.db.PermissionCheck;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class ProcessInstanceResourceAuthorizationTest extends AuthorizationTest {

  protected static final String USER_TASK_PROCESS_KEY = "userTaskProcess";
  protected static final String CALLING_USER_TASK_PROCESS_KEY = "CallingUserTaskProcess";

  protected String deploymentId;

  protected ProcessInstanceResource resource;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();

    runtimeService = processEngine.getRuntimeService();

    deploymentId = createDeployment(null, "processes/user-task-process.bpmn", "processes/calling-user-task-process.bpmn").getId();

    startProcessInstances(CALLING_USER_TASK_PROCESS_KEY, 3);
  }

  @Override
  @After
  public void tearDown() {
    deleteDeployment(deploymentId);
    super.tearDown();
  }

  // ProcessInstanceResource#queryCalledProcessInstances() /////////////////////////////

  @Test
  public void testCalledInstancesWithoutAuthorization() {
    // given
    String processInstanceId = selectAnyProcessInstanceByKey(CALLING_USER_TASK_PROCESS_KEY).getId();

    resource = new ProcessInstanceResource(engineName, processInstanceId);

    CalledProcessInstanceQueryDto queryParameter = new CalledProcessInstanceQueryDto();

    // when
    List<CalledProcessInstanceDto> calledInstances = resource.queryCalledProcessInstances(queryParameter);

    // then
    assertThat(calledInstances).isEmpty();
  }

  @Test
  public void testCalledInstancesWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = selectAnyProcessInstanceByKey(CALLING_USER_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    resource = new ProcessInstanceResource(engineName, processInstanceId);

    CalledProcessInstanceQueryDto queryParameter = new CalledProcessInstanceQueryDto();

    // when
    List<CalledProcessInstanceDto> calledInstances = resource.queryCalledProcessInstances(queryParameter);

    // then
    assertThat(calledInstances).isNotEmpty();
    assertThat(calledInstances).hasSize(1);
    assertThat(calledInstances.get(0).getId()).isNotEqualTo(processInstanceId);
  }

  @Test
  public void testCalledInstancesWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = selectAnyProcessInstanceByKey(CALLING_USER_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    resource = new ProcessInstanceResource(engineName, processInstanceId);

    CalledProcessInstanceQueryDto queryParameter = new CalledProcessInstanceQueryDto();

    // when
    List<CalledProcessInstanceDto> calledInstances = resource.queryCalledProcessInstances(queryParameter);

    // then
    assertThat(calledInstances).isNotEmpty();
    assertThat(calledInstances).hasSize(1);
    assertThat(calledInstances.get(0).getId()).isNotEqualTo(processInstanceId);
  }

  @Test
  public void testCalledInstancesWithReadPermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, CALLING_USER_TASK_PROCESS_KEY, userId, READ_INSTANCE);

    String processInstanceId = selectAnyProcessInstanceByKey(CALLING_USER_TASK_PROCESS_KEY).getId();
    resource = new ProcessInstanceResource(engineName, processInstanceId);

    CalledProcessInstanceQueryDto queryParameter = new CalledProcessInstanceQueryDto();

    // when
    List<CalledProcessInstanceDto> calledInstances = resource.queryCalledProcessInstances(queryParameter);

    // then
    assertThat(calledInstances).isNotEmpty();
    assertThat(calledInstances).hasSize(1);
    assertThat(calledInstances.get(0).getId()).isNotEqualTo(processInstanceId);
  }

  @Test
  public void testCalledInstancesWithMultipleReadPermissions() {
    // given
    String processInstanceId = selectAnyProcessInstanceByKey(CALLING_USER_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, CALLING_USER_TASK_PROCESS_KEY, userId, READ_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    resource = new ProcessInstanceResource(engineName, processInstanceId);

    CalledProcessInstanceQueryDto queryParameter = new CalledProcessInstanceQueryDto();

    // when
    List<CalledProcessInstanceDto> calledInstances = resource.queryCalledProcessInstances(queryParameter);

    // then
    assertThat(calledInstances).isNotEmpty();
    assertThat(calledInstances).hasSize(1);
    assertThat(calledInstances.get(0).getId()).isNotEqualTo(processInstanceId);
  }


  // query "selectCalledProcessInstances" //////////////////////////////////////////////

  @Test
  public void testCalledInstancesQueryWithoutAuthorization() {
    // given
    CalledProcessInstanceQueryDto queryParameter = new CalledProcessInstanceQueryDto();

    // when
    List<CalledProcessInstanceDto> calledInstances = executeCalledInstancesQueryWithAuthorization(queryParameter);

    // then
    assertThat(calledInstances).isEmpty();
  }

  @Test
  public void testCalledInstancesQueryWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = selectAnyProcessInstanceByKey(CALLING_USER_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    CalledProcessInstanceQueryDto queryParameter = new CalledProcessInstanceQueryDto();

    // when
    List<CalledProcessInstanceDto> calledInstances = executeCalledInstancesQueryWithAuthorization(queryParameter);

    // then
    assertThat(calledInstances).isNotEmpty();
    assertThat(calledInstances).hasSize(1);
    assertThat(calledInstances.get(0).getId()).isNotEqualTo(processInstanceId);
  }

  @Test
  public void testCalledInstancesQueryWithReadPermissionOnAnyProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    CalledProcessInstanceQueryDto queryParameter = new CalledProcessInstanceQueryDto();

    // when
    List<CalledProcessInstanceDto> calledInstances = executeCalledInstancesQueryWithAuthorization(queryParameter);

    // then
    assertThat(calledInstances).isNotEmpty();
    assertThat(calledInstances).hasSize(3);
  }

  @Test
  public void testCalledInstancesQueryWithReadInstancePermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, CALLING_USER_TASK_PROCESS_KEY, userId, READ_INSTANCE);

    CalledProcessInstanceQueryDto queryParameter = new CalledProcessInstanceQueryDto();

    // when
    List<CalledProcessInstanceDto> calledInstances = executeCalledInstancesQueryWithAuthorization(queryParameter);

    // then
    assertThat(calledInstances).isNotEmpty();
    assertThat(calledInstances).hasSize(3);
  }

  @Test
  public void testCalledInstancesQueryWithMultipleReadPermissins() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, CALLING_USER_TASK_PROCESS_KEY, userId, READ_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    CalledProcessInstanceQueryDto queryParameter = new CalledProcessInstanceQueryDto();

    // when
    List<CalledProcessInstanceDto> calledInstances = executeCalledInstancesQueryWithAuthorization(queryParameter);

    // then
    assertThat(calledInstances).isNotEmpty();
    assertThat(calledInstances).hasSize(3);
  }

  // helper ////////////////////////////////////////////////////////////////////////////

  protected Authentication getCurrentAuthentication() {
    return identityService.getCurrentAuthentication();
  }

  protected List<CalledProcessInstanceDto> executeCalledInstancesQueryWithAuthorization(CalledProcessInstanceQueryDto query) {
    Authentication currentAuthentication = getCurrentAuthentication();

    AuthorizationCheck authCheck = query.getAuthCheck();

    authCheck.getPermissionChecks().clear();
    authCheck.setAuthorizationCheckEnabled(true);
    String currentUserId = currentAuthentication.getUserId();
    List<String> currentGroupIds = currentAuthentication.getGroupIds();
    authCheck.setAuthUserId(currentUserId);
    authCheck.setAuthGroupIds(currentGroupIds);

    PermissionCheck firstPermCheck = new PermissionCheck();
    firstPermCheck.setResource(PROCESS_INSTANCE);
    firstPermCheck.setResourceIdQueryParam("EXEC1.PROC_INST_ID_");
    firstPermCheck.setPermission(READ);
    authCheck.addAtomicPermissionCheck(firstPermCheck);

    PermissionCheck secondPermCheck = new PermissionCheck();
    secondPermCheck.setResource(PROCESS_DEFINITION);
    secondPermCheck.setResourceIdQueryParam("PROCDEF.KEY_");
    secondPermCheck.setPermission(READ_INSTANCE);
    authCheck.addAtomicPermissionCheck(secondPermCheck);

    return getQueryService().executeQuery("selectCalledProcessInstances", query);
  }

}
