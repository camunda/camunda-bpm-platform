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
package org.camunda.bpm.engine.impl;

import java.util.Map;

import org.camunda.bpm.engine.ExternalTaskService;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryBuilder;
import org.camunda.bpm.engine.impl.cmd.CompleteExternalTaskCmd;
import org.camunda.bpm.engine.impl.cmd.HandleExternalTaskBpmnErrorCmd;
import org.camunda.bpm.engine.impl.cmd.HandleExternalTaskFailureCmd;
import org.camunda.bpm.engine.impl.cmd.SetExternalTaskPriorityCmd;
import org.camunda.bpm.engine.impl.cmd.SetExternalTaskRetriesCmd;
import org.camunda.bpm.engine.impl.cmd.UnlockExternalTaskCmd;
import org.camunda.bpm.engine.impl.externaltask.ExternalTaskQueryTopicBuilderImpl;

/**
 * @author Thorben Lindhauer
 * @author Christopher Zell
 */
public class ExternalTaskServiceImpl extends ServiceImpl implements ExternalTaskService {

  @Override
  public ExternalTaskQueryBuilder fetchAndLock(int maxTasks, String workerId) {
    return fetchAndLock(maxTasks, workerId, false);
  }
  
  @Override
  public ExternalTaskQueryBuilder fetchAndLock(int maxTasks, String workerId, boolean usePriority) {
    return new ExternalTaskQueryTopicBuilderImpl(commandExecutor, workerId, maxTasks, usePriority);
  }

  public void complete(String externalTaskId, String workerId) {
    commandExecutor.execute(new CompleteExternalTaskCmd(externalTaskId, workerId, null));
  }

  public void complete(String externalTaskId, String workerId, Map<String, Object> variables) {
    commandExecutor.execute(new CompleteExternalTaskCmd(externalTaskId, workerId, variables));
  }

  public void handleFailure(String externalTaskId, String workerId, String errorMessage, int retries, long retryDuration) {
    commandExecutor.execute(new HandleExternalTaskFailureCmd(externalTaskId, workerId, errorMessage, retries, retryDuration));
  }
  
  @Override
  public void handleBpmnError(String externalTaskId, String workerId, String errorCode) {
    commandExecutor.execute(new HandleExternalTaskBpmnErrorCmd(externalTaskId, workerId, errorCode));
  }

  public void unlock(String externalTaskId) {
    commandExecutor.execute(new UnlockExternalTaskCmd(externalTaskId));
  }

  public void setRetries(String externalTaskId, int retries) {
    commandExecutor.execute(new SetExternalTaskRetriesCmd(externalTaskId, retries));
  }  
  
  @Override
  public void setPriority(String externalTaskId, long priority) {
    commandExecutor.execute(new SetExternalTaskPriorityCmd(externalTaskId, priority));
  }
  
  public ExternalTaskQuery createExternalTaskQuery() {
    return new ExternalTaskQueryImpl(commandExecutor);
  }


}
