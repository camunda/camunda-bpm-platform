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
package org.camunda.bpm.engine.test.authorization;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;

import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.IncidentQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;

/**
 * @author Roman Smirnov
 *
 */
public class IncidentAuthorizationTest extends AuthorizationTest {

  protected static final String PROCESS_KEY = "process";
  protected static final String ANOTHER_ONE_INCIDENT_PROCESS_KEY = "anotherOneIncidentProcess";

  protected String deploymentId;

  public void setUp() {
    deploymentId = repositoryService
      .createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/authorization/oneIncidentProcess.bpmn20.xml")
      .addClasspathResource("org/camunda/bpm/engine/test/authorization/anotherOneIncidentProcess.bpmn20.xml")
      .deploy()
      .getId();
  }

  public void tearDown() {
    super.tearDown();
    repositoryService.deleteDeployment(deploymentId, true);
  }

  public void testSimpleQueryWithoutAuthorization() {
    // given
    startProcessAndExecuteJob(PROCESS_KEY);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testSimpleQueryWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessAndExecuteJob(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 1);

    Incident incident = query.singleResult();
    assertNotNull(incident);
    assertEquals(processInstanceId, incident.getProcessInstanceId());
  }

  public void testSimpleQueryWithReadPermissionOnAnyProcessInstance() {
    // given
    String processInstanceId = startProcessAndExecuteJob(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 1);

    Incident incident = query.singleResult();
    assertNotNull(incident);
    assertEquals(processInstanceId, incident.getProcessInstanceId());
  }

  public void testSimpleQueryWithReadInstancesPermissionOnOneTaskProcess() {
    // given
    String processInstanceId = startProcessAndExecuteJob(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 1);

    Incident incident = query.singleResult();
    assertNotNull(incident);
    assertEquals(processInstanceId, incident.getProcessInstanceId());
  }

  public void testSimpleQueryWithReadInstancesPermissionOnAnyProcessDefinition() {
    // given
    String processInstanceId = startProcessAndExecuteJob(PROCESS_KEY).getId();
    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 1);

    Incident incident = query.singleResult();
    assertNotNull(incident);
    assertEquals(processInstanceId, incident.getProcessInstanceId());
  }

  public void testQueryWithoutAuthorization() {
    // given
    startProcessAndExecuteJob(PROCESS_KEY);
    startProcessAndExecuteJob(PROCESS_KEY);
    startProcessAndExecuteJob(PROCESS_KEY);

    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testQueryWithReadPermissionOnProcessInstance() {
    // given
    startProcessAndExecuteJob(PROCESS_KEY);
    startProcessAndExecuteJob(PROCESS_KEY);
    String processInstanceId = startProcessAndExecuteJob(PROCESS_KEY).getId();

    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_INSTANCE, processInstanceId, userId, READ);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 1);

    Incident incident = query.singleResult();
    assertNotNull(incident);
    assertEquals(processInstanceId, incident.getProcessInstanceId());
  }

  public void testQueryWithReadPermissionOnAnyProcessInstance() {
    // given
    startProcessAndExecuteJob(PROCESS_KEY);
    startProcessAndExecuteJob(PROCESS_KEY);
    startProcessAndExecuteJob(PROCESS_KEY);

    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_INSTANCE, ANY, userId, READ);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 7);
  }

  public void testQueryWithReadInstancesPermissionOnOneTaskProcess() {
    // given
    startProcessAndExecuteJob(PROCESS_KEY);
    startProcessAndExecuteJob(PROCESS_KEY);
    startProcessAndExecuteJob(PROCESS_KEY);

    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, PROCESS_KEY, userId, READ_INSTANCE);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 3);
  }

  public void testQueryWithReadInstancesPermissionOnAnyProcessDefinition() {
    // given
    startProcessAndExecuteJob(PROCESS_KEY);
    startProcessAndExecuteJob(PROCESS_KEY);
    startProcessAndExecuteJob(PROCESS_KEY);

    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);
    startProcessAndExecuteJob(ANOTHER_ONE_INCIDENT_PROCESS_KEY);

    createGrantAuthorization(PROCESS_DEFINITION, ANY, userId, READ_INSTANCE);

    // when
    IncidentQuery query = runtimeService.createIncidentQuery();

    // then
    verifyQueryResults(query, 7);
  }

  protected ProcessInstance startProcessAndExecuteJob(String key) {
    ProcessInstance processInstance = startProcessInstanceByKey(key);
    executeAvailableJobs();
    return processInstance;
  }

  protected void verifyQueryResults(IncidentQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

}
