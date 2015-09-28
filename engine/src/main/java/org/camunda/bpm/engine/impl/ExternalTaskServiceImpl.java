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
import org.camunda.bpm.engine.impl.cmd.UnlockExternalTaskCmd;
import org.camunda.bpm.engine.impl.externaltask.ExternalTaskQueryBuilderImpl;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExternalTaskServiceImpl extends ServiceImpl implements ExternalTaskService {

  public ExternalTaskQueryBuilder fetchAndLock(int maxTasks, String workerId) {
    return new ExternalTaskQueryBuilderImpl(commandExecutor, workerId, maxTasks);
  }

  public void complete(String externalTaskId, String workerId) {
    commandExecutor.execute(new CompleteExternalTaskCmd(externalTaskId, workerId, null));

  }

  public void complete(String externalTaskId, String workerId, Map<String, Object> variables) {
    commandExecutor.execute(new CompleteExternalTaskCmd(externalTaskId, workerId, variables));
  }

  public void unlock(String externalTaskId) {
    commandExecutor.execute(new UnlockExternalTaskCmd(externalTaskId));

  }

  public ExternalTaskQuery createExternalTaskQuery() {
    return new ExternalTaskQueryImpl(commandExecutor);
  }

}
