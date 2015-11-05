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
package org.camunda.bpm.engine.test.history.useroperationlog;

import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Roman Smirnov
 *
 */
public class LegacyUserOperationLogTest extends ResourceProcessEngineTestCase {

  public static final String USER_ID = "demo";

  public LegacyUserOperationLogTest() {
    super("org/camunda/bpm/engine/test/history/useroperationlog/enable.legacy.user.operation.log.camunda.cfg.xml");
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/history/useroperationlog/UserOperationLogTaskTest.testOnlyTaskCompletionIsLogged.bpmn20.xml")
  public void testLogAllOperationWithAuthentication() {
    try {
      // given
      identityService.setAuthenticatedUserId(USER_ID);
      String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

      String taskId = taskService.createTaskQuery().singleResult().getId();

      // when
      taskService.complete(taskId);

      // then
      assertTrue((Boolean) runtimeService.getVariable(processInstanceId, "taskListenerCalled"));
      assertTrue((Boolean) runtimeService.getVariable(processInstanceId, "serviceTaskCalled"));

      UserOperationLogQuery query = userOperationLogQuery().userId(USER_ID);
      assertEquals(3, query.count());
      assertEquals(1, query.operationType(UserOperationLogEntry.OPERATION_TYPE_COMPLETE).count());
      assertEquals(2, query.operationType(UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE).count());

    } finally {
      identityService.clearAuthentication();
    }
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/history/useroperationlog/UserOperationLogTaskTest.testOnlyTaskCompletionIsLogged.bpmn20.xml")
  public void testLogOperationWithoutAuthentication() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.complete(taskId);

    // then
    assertTrue((Boolean) runtimeService.getVariable(processInstanceId, "taskListenerCalled"));
    assertTrue((Boolean) runtimeService.getVariable(processInstanceId, "serviceTaskCalled"));

    assertEquals(4, userOperationLogQuery().count());
    assertEquals(1, userOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_COMPLETE).count());
    assertEquals(2, userOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE).count());
    assertEquals(1, userOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_CREATE).count());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/history/useroperationlog/UserOperationLogTaskTest.testOnlyTaskCompletionIsLogged.bpmn20.xml")
  public void testLogSetVariableWithoutAuthentication() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when
    runtimeService.setVariable(processInstanceId, "aVariable", "aValue");

    // then
    assertEquals(2, userOperationLogQuery().count());
    assertEquals(1, userOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE).count());
    assertEquals(1, userOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_CREATE).count());
  }

  protected UserOperationLogQuery userOperationLogQuery() {
    return historyService.createUserOperationLogQuery();
  }

}
