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

import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;

import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;

/**
 * @author Roman Smirnov
 *
 */
public class ExecutionAuthorizationTest extends AuthorizationTest {

  protected static final String ONE_TASK_PROCESS_KEY = "oneTaskProcess";

  protected String deploymentId;

  public void setUp() {
    deploymentId = repositoryService
      .createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
      .deploy()
      .getId();
  }

  public void tearDown() {
    super.tearDown();
    repositoryService.deleteDeployment(deploymentId, true);
  }

  public void testQueryWithoutAuthorization() {
    // given
    startProcessInstanceByKey(ONE_TASK_PROCESS_KEY);

    // when
    ExecutionQuery query = runtimeService.createExecutionQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testQueryWithReadPermissionOnProcessInstance() {
    // given
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();

    Authorization authorization = createGrantAuthorization(PROCESS_INSTANCE, processInstanceId);
    authorization.setUserId(userId);
    authorization.addPermission(READ);
    saveAuthorization(authorization);

    // when
    ExecutionQuery query = runtimeService.createExecutionQuery();

    // then
    verifyQueryResults(query, 1);

    Execution execution = query.singleResult();
    assertNotNull(execution);
    assertEquals(processInstanceId, execution.getProcessInstanceId());
  }

  public void testQueryWithReadInstancesPermissionOnOneTaskProcess() {
    // given
    String processInstanceId = startProcessInstanceByKey(ONE_TASK_PROCESS_KEY).getId();

    Authorization authorization = createGrantAuthorization(PROCESS_DEFINITION, ONE_TASK_PROCESS_KEY);
    authorization.setUserId(userId);
    authorization.addPermission(READ_INSTANCE);
    saveAuthorization(authorization);

    // when
    ExecutionQuery query = runtimeService.createExecutionQuery();

    // then
    verifyQueryResults(query, 1);

    Execution execution = query.singleResult();
    assertNotNull(execution);
    assertEquals(processInstanceId, execution.getProcessInstanceId());
  }

  protected void verifyQueryResults(ExecutionQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }
}
