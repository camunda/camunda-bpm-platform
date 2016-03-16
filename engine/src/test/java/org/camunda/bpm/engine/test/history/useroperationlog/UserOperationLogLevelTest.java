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

import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.impl.history.useroperation.UserOperationLogLevel;
import org.camunda.bpm.engine.test.Deployment;

public class UserOperationLogLevelTest extends AbstractUserOperationLogTest {

  protected static final String PROCESS_PATH = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";
  protected static final String PROCESS_KEY = "oneTaskProcess";

  @Deployment(resources = PROCESS_PATH)
  public void testCompleteTask() {
    // given
    processEngineConfiguration.setUserOperationLogLevel(UserOperationLogLevel.USER_OPERATION_LOG_LEVEL_NONE);
    
    runtimeService.startProcessInstanceByKey(PROCESS_KEY);
    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.complete(taskId);

    // then
    verifyNoUserOperationLogged();
  }

  protected void verifyNoUserOperationLogged() {
    UserOperationLogQuery query = historyService.createUserOperationLogQuery();
    assertEquals(0, query.count());
  }

  @Override
  protected void tearDown() throws Exception {
    processEngineConfiguration.setUserOperationLogLevel(UserOperationLogLevel.USER_OPERATION_LOG_LEVEL_FULL);
    
    super.tearDown();
  }
}
