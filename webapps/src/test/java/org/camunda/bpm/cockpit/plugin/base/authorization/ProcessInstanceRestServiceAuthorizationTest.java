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

import org.camunda.bpm.cockpit.impl.plugin.base.dto.ProcessInstanceDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.query.ProcessInstanceQueryDto;
import org.camunda.bpm.cockpit.impl.plugin.resources.ProcessInstanceRestService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class ProcessInstanceRestServiceAuthorizationTest extends AuthorizationTest {

  protected static final String USER_TASK_PROCESS_KEY = "userTaskProcess";

  protected String deploymentId;

  protected ProcessInstanceRestService resource;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();

    deploymentId = createDeployment(null, "processes/user-task-process.bpmn").getId();

    startProcessInstances(USER_TASK_PROCESS_KEY, 3);

    resource = new ProcessInstanceRestService(engineName);
  }

  @Override
  @After
  public void tearDown() {
    deleteDeployment(deploymentId);
    super.tearDown();
  }

  @Test
  public void testQueryWithoutAuthorization() {
    // given
    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    // when
    List<ProcessInstanceDto> instances = resource.queryProcessInstances(queryParameter, null, null);

    // then
    assertThat(instances).isEmpty();
  }

  @Test
  public void testQueryWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = selectAnyProcessInstanceByKey(USER_TASK_PROCESS_KEY).getId();

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    // when
    List<ProcessInstanceDto> instances = resource.queryProcessInstances(queryParameter, null, null);

    // then
    assertThat(instances).isNotEmpty();
    assertThat(instances).hasSize(1);
    assertThat(instances.get(0).getId()).isEqualTo(processInstanceId);
  }

  @Test
  public void testQueryWithReadPermissionOnAnyProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    // when
    List<ProcessInstanceDto> instances = resource.queryProcessInstances(queryParameter, null, null);

    // then
    assertThat(instances).isNotEmpty();
    assertThat(instances).hasSize(3);
  }

  @Test
  public void testQueryWithReadInstancePermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, USER_TASK_PROCESS_KEY, userId, READ_INSTANCE);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    // when
    List<ProcessInstanceDto> instances = resource.queryProcessInstances(queryParameter, null, null);

    // then
    assertThat(instances).isNotEmpty();
    assertThat(instances).hasSize(3);
  }

  @Test
  public void testQueryPaginationWithOverlappingPermissions() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, USER_TASK_PROCESS_KEY, userId, READ_INSTANCE);
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    ProcessInstanceQueryDto queryParameter = new ProcessInstanceQueryDto();

    // when
    List<ProcessInstanceDto> result = resource.queryProcessInstances(queryParameter, 0, 3);

    // then
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(3);

    result = resource.queryProcessInstances(queryParameter, 0, 2);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(2);

    result = resource.queryProcessInstances(queryParameter, 2, 2);
    assertThat(result).isNotEmpty();
    assertThat(result).hasSize(1);
  }

}
