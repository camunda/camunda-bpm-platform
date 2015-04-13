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
import static org.camunda.bpm.engine.authorization.Permissions.ALL;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;

import java.io.InputStream;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;

/**
 * @author Roman Smirnov
 *
 */
public class ProcessDefinitionAuthorizationTest extends AuthorizationTest {

  protected static final String ONE_TASK_PROCESS_KEY = "oneTaskProcess";
  protected static final String TWO_TASKS_PROCESS_KEY = "twoTasksProcess";

  protected String deploymentId;

  public void setUp() {
    deploymentId = repositoryService
      .createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
      .addClasspathResource("org/camunda/bpm/engine/test/api/twoTasksProcess.bpmn20.xml")
      .deploy()
      .getId();
  }

  public void tearDown() {
    super.tearDown();
    repositoryService.deleteDeployment(deploymentId, true);
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

  // helper /////////////////////////////////////////////////////////////////////

  protected void verifyQueryResults(ProcessDefinitionQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }


}
