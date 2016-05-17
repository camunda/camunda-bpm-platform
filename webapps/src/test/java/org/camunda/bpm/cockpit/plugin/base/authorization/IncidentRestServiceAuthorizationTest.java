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

import org.camunda.bpm.cockpit.impl.plugin.base.dto.IncidentDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.query.IncidentQueryDto;
import org.camunda.bpm.cockpit.impl.plugin.resources.IncidentRestService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class IncidentRestServiceAuthorizationTest extends AuthorizationTest {

  protected static final String FAILING_PROCESS_KEY = "FailingProcess";

  protected String deploymentId;

  protected IncidentRestService resource;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();

    runtimeService = processEngine.getRuntimeService();

    deploymentId = createDeployment(null, "processes/failing-process.bpmn").getId();

    resource = new IncidentRestService(engineName);

    startProcessInstances(FAILING_PROCESS_KEY, 3);
    disableAuthorization();
    executeAvailableJobs();
    enableAuthorization();
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
    IncidentQueryDto queryParameter = new IncidentQueryDto();

    // when
    List<IncidentDto> incidents = resource.queryIncidents(queryParameter, null, null);

    // then
    assertThat(incidents).isEmpty();
  }

  @Test
  public void testQueryWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = selectAnyProcessInstanceByKey(FAILING_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    IncidentQueryDto queryParameter = new IncidentQueryDto();

    // when
    List<IncidentDto> incidents = resource.queryIncidents(queryParameter, null, null);

    // then
    assertThat(incidents).isNotEmpty();
    assertThat(incidents).hasSize(1);
    assertThat(incidents.get(0).getProcessInstanceId()).isEqualTo(processInstanceId);
  }

  @Test
  public void testQueryWithReadPermissionOnAnyProcessInstance() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    IncidentQueryDto queryParameter = new IncidentQueryDto();

    // when
    List<IncidentDto> incidents = resource.queryIncidents(queryParameter, null, null);

    // then
    assertThat(incidents).isNotEmpty();
    assertThat(incidents).hasSize(3);
  }

  @Test
  public void testQueryWithMultipleReadPermissions() {
    // given
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);
    String processInstanceId = selectAnyProcessInstanceByKey(FAILING_PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    IncidentQueryDto queryParameter = new IncidentQueryDto();

    // when
    List<IncidentDto> incidents = resource.queryIncidents(queryParameter, null, null);

    // then
    assertThat(incidents).isNotEmpty();
    assertThat(incidents).hasSize(3);
  }

  @Test
  public void testQueryWithReadPermissionOnProcessDefinition() {
    // given
    createGrantAuthorization(PROCESS_DEFINITION, FAILING_PROCESS_KEY, userId, READ_INSTANCE);

    IncidentQueryDto queryParameter = new IncidentQueryDto();

    // when
    List<IncidentDto> incidents = resource.queryIncidents(queryParameter, null, null);

    // then
    assertThat(incidents).isNotEmpty();
    assertThat(incidents).hasSize(3);
  }

}
