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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.externaltask.ExternalTaskQueryTopicBuilder;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.impl.cmd.FetchExternalTasksCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

/**
 * @author Thorben Lindhauer
 * @author Christopher Zell
 *
 */
public class ExternalTaskQueryTopicBuilderImpl implements ExternalTaskQueryTopicBuilder {

  protected CommandExecutor commandExecutor;

  protected String workerId;
  protected int maxTasks;
  /**
   * Indicates that priority is enabled.
   */
  protected boolean usePriority;

  protected Map<String, TopicFetchInstruction> instructions;

  protected boolean filterByBusinessKey;

  protected TopicFetchInstruction currentInstruction;

  public ExternalTaskQueryTopicBuilderImpl(CommandExecutor commandExecutor, String workerId, int maxTasks, boolean usePriority) {
    this.commandExecutor = commandExecutor;
    this.workerId = workerId;
    this.maxTasks = maxTasks;
    this.usePriority = usePriority;
    this.filterByBusinessKey = false;
    this.instructions = new HashMap<String, TopicFetchInstruction>();
  }

  public List<LockedExternalTask> execute() {
    submitCurrentInstruction();
    return commandExecutor.execute(new FetchExternalTasksCmd(workerId, maxTasks, instructions, filterByBusinessKey, usePriority));
  }

  public ExternalTaskQueryTopicBuilder topic(String topicName, long lockDuration) {
    submitCurrentInstruction();
    currentInstruction = new TopicFetchInstruction(topicName, lockDuration);
    return this;
  }

  public ExternalTaskQueryTopicBuilder variables(String... variables) {
    // don't use plain Arrays.asList since this returns an instance of a different list class
    // that is private and may mess mybatis queries up
    if (variables != null) {
      currentInstruction.setVariablesToFetch(new ArrayList<String>(Arrays.asList(variables)));
    }
    return this;
  }

  public ExternalTaskQueryTopicBuilder variables(List<String> variables) {
    currentInstruction.setVariablesToFetch(variables);
    return this;
  }

  public ExternalTaskQueryTopicBuilder processInstanceVariableEquals(Map<String, Object> variables) {
    currentInstruction.setFilterVariables(variables);
    return this;
  }

  public ExternalTaskQueryTopicBuilder processInstanceVariableEquals(String name, Object value) {
    currentInstruction.addFilterVariable(name, value);
    return this;
  }

  public ExternalTaskQueryTopicBuilder businessKey(String businessKey) {
    this.filterByBusinessKey = true;
    currentInstruction.setBusinessKey(businessKey);
    return this;
  }

  protected void submitCurrentInstruction() {
    if (currentInstruction != null) {
      this.instructions.put(currentInstruction.getTopicName(), currentInstruction);
    }
  }

  public ExternalTaskQueryTopicBuilder enableCustomObjectDeserialization() {
    currentInstruction.setDeserializeVariables(true);
    return this;
  }

  public ExternalTaskQueryTopicBuilder localVariables() {
    currentInstruction.setLocalVariables(true);
    return this;
  }


}
