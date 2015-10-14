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
package org.camunda.bpm.engine.impl.externaltask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryBuilder;
import org.camunda.bpm.engine.externaltask.ExternalTaskQueryTopicBuilder;
import org.camunda.bpm.engine.impl.cmd.FetchExternalTasksCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

/**
 * @author Thorben Lindhauer
 *
 */
public class ExternalTaskQueryBuilderImpl implements ExternalTaskQueryBuilder {

  protected CommandExecutor commandExecutor;

  protected String workerId;
  protected int maxTasks;

  protected Map<String, TopicFetchInstruction> instructions;

  public ExternalTaskQueryBuilderImpl(CommandExecutor commandExecutor, String workerId, int maxTasks) {
    this.commandExecutor = commandExecutor;
    this.workerId = workerId;
    this.maxTasks = maxTasks;
    this.instructions = new HashMap<String, TopicFetchInstruction>();
  }

  public ExternalTaskQueryTopicBuilder topic(String topicName, long lockDuration) {
    return new ExternalTaskQueryTopicBuilderImpl(this, topicName, lockDuration);
  }

  public List<LockedExternalTask> execute() {
    return commandExecutor.execute(new FetchExternalTasksCmd(workerId, maxTasks, instructions));
  }

  public void addInstruction(TopicFetchInstruction instruction) {
    this.instructions.put(instruction.getTopicName(), instruction);
  }

}
