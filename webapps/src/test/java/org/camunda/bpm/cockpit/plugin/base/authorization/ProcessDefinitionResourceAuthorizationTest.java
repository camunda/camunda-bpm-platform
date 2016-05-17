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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.cockpit.impl.plugin.base.dto.ProcessDefinitionDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.query.ProcessDefinitionQueryDto;
import org.camunda.bpm.cockpit.impl.plugin.base.sub.resources.ProcessDefinitionResource;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class ProcessDefinitionResourceAuthorizationTest extends AuthorizationTest {

  protected static final String USER_TASK_PROCESS_KEY = "userTaskProcess";
  protected static final String CALLING_USER_TASK_PROCESS_KEY = "CallingUserTaskProcess";

  protected String deploymentId;

  protected ProcessDefinitionResource resource;

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

  @Test
  public void testCalledProcessDefinitionQueryWithoutAuthorization() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(CALLING_USER_TASK_PROCESS_KEY).getId();
    resource = new ProcessDefinitionResource(engineName, processDefinitionId);

    ProcessDefinitionQueryDto queryParameter = new ProcessDefinitionQueryDto();

    // when
    List<ProcessDefinitionDto> calledDefinitions = resource.queryCalledProcessDefinitions(queryParameter);

    // then
    assertThat(calledDefinitions).isEmpty();
  }

  @Test
  public void testCalledProcessDefinitionQueryWithReadPermissionOnProcessInstance() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(CALLING_USER_TASK_PROCESS_KEY).getId();
    resource = new ProcessDefinitionResource(engineName, processDefinitionId);

    String processInstanceId = selectAnyProcessInstanceByKey(CALLING_USER_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    ProcessDefinitionQueryDto queryParameter = new ProcessDefinitionQueryDto();

    // when
    List<ProcessDefinitionDto> calledDefinitions = resource.queryCalledProcessDefinitions(queryParameter);

    // then
    assertThat(calledDefinitions).isNotEmpty();

    ProcessDefinitionDto calledProcessDefinition = calledDefinitions.get(0);
    assertThat(calledProcessDefinition.getKey()).isEqualTo(USER_TASK_PROCESS_KEY);
  }

  @Test
  public void testCalledProcessDefinitionQueryWithReadPermissionOnAnyProcessInstance() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(CALLING_USER_TASK_PROCESS_KEY).getId();
    resource = new ProcessDefinitionResource(engineName, processDefinitionId);

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    ProcessDefinitionQueryDto queryParameter = new ProcessDefinitionQueryDto();

    // when
    List<ProcessDefinitionDto> calledDefinitions = resource.queryCalledProcessDefinitions(queryParameter);

    // then
    assertThat(calledDefinitions).isNotEmpty();

    ProcessDefinitionDto calledProcessDefinition = calledDefinitions.get(0);
    assertThat(calledProcessDefinition.getKey()).isEqualTo(USER_TASK_PROCESS_KEY);
  }

  @Test
  public void testCalledProcessDefinitionQueryWithMultipleReadPermissions() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(CALLING_USER_TASK_PROCESS_KEY).getId();
    resource = new ProcessDefinitionResource(engineName, processDefinitionId);

    String processInstanceId = selectAnyProcessInstanceByKey(CALLING_USER_TASK_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    ProcessDefinitionQueryDto queryParameter = new ProcessDefinitionQueryDto();

    // when
    List<ProcessDefinitionDto> calledDefinitions = resource.queryCalledProcessDefinitions(queryParameter);

    // then
    assertThat(calledDefinitions.size()).isEqualTo(1);

    ProcessDefinitionDto calledProcessDefinition = calledDefinitions.get(0);
    assertThat(calledProcessDefinition.getKey()).isEqualTo(USER_TASK_PROCESS_KEY);
  }

  @Test
  public void testCalledProcessDefinitionQueryWithReadInstancePermissionOnProcessDefinition() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey(CALLING_USER_TASK_PROCESS_KEY).getId();
    resource = new ProcessDefinitionResource(engineName, processDefinitionId);

    createGrantAuthorization(PROCESS_DEFINITION, CALLING_USER_TASK_PROCESS_KEY, userId, READ_INSTANCE);

    ProcessDefinitionQueryDto queryParameter = new ProcessDefinitionQueryDto();

    // when
    List<ProcessDefinitionDto> calledDefinitions = resource.queryCalledProcessDefinitions(queryParameter);

    // then
    assertThat(calledDefinitions).isNotEmpty();

    ProcessDefinitionDto calledProcessDefinition = calledDefinitions.get(0);
    assertThat(calledProcessDefinition.getKey()).isEqualTo(USER_TASK_PROCESS_KEY);
  }

  @Test
  @Deployment(resources = {
    "processes/another-user-task-process.bpmn",
    "processes/dynamic-call-activity.bpmn"
  })
  public void testCalledProcessDefinitionQueryDifferentCalledProcesses() {
    // given
    String processDefinitionId = selectProcessDefinitionByKey("DynamicCallActivity").getId();
    resource = new ProcessDefinitionResource(engineName, processDefinitionId);

    disableAuthorization();
    Map<String, Object> vars1 = new HashMap<String, Object>();
    vars1.put("callProcess", "anotherUserTaskProcess");
    String firstProcessInstanceId = runtimeService.startProcessInstanceByKey("DynamicCallActivity", vars1).getId();

    Map<String, Object> vars2 = new HashMap<String, Object>();
    vars2.put("callProcess", "userTaskProcess");
    runtimeService.startProcessInstanceByKey("DynamicCallActivity", vars2);
    enableAuthorization();

    createGrantAuthorization(PROCESS_INSTANCE, firstProcessInstanceId, userId, READ);

    ProcessDefinitionQueryDto queryParameter = new ProcessDefinitionQueryDto();

    // when
    List<ProcessDefinitionDto> calledDefinitions = resource.queryCalledProcessDefinitions(queryParameter);

    // then
    assertThat(calledDefinitions).isNotEmpty();

    ProcessDefinitionDto calledProcessDefinition = calledDefinitions.get(0);
    assertThat(calledProcessDefinition.getKey()).isEqualTo("anotherUserTaskProcess");
  }

}
