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
package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionManager;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.persistence.entity.TaskManager;
import org.camunda.bpm.engine.runtime.Job;

/**
 * @author Daniel Meyer
 * @author Joram Barrez
 * @author roman.smirnov
 */
public abstract class AbstractSetProcessInstanceStateCmd implements Command<Void> {

  protected final String processInstanceId;
  protected String processDefinitionId;
  protected String processDefinitionKey;


  public AbstractSetProcessInstanceStateCmd(String processInstanceId, String processDefinitionId, String processDefinitionKey) {
    this.processInstanceId = processInstanceId;
    this.processDefinitionId = processDefinitionId;
    this.processDefinitionKey = processDefinitionKey;
  }

  public Void execute(CommandContext commandContext) {

    if(processInstanceId == null && processDefinitionId == null && processDefinitionKey == null) {
      throw new ProcessEngineException("ProcessInstanceId, ProcessDefinitionId nor ProcessDefinitionKey cannot be null.");
    }

    ExecutionManager executionManager = commandContext.getExecutionManager();
    TaskManager taskManager = commandContext.getTaskManager();

    SuspensionState suspensionState = getNewSuspensionState();

    if (processInstanceId != null) {
      executionManager.updateExecutionSuspensionStateByProcessInstanceId(processInstanceId, suspensionState);
      taskManager.updateTaskSuspensionStateByProcessInstanceId(processInstanceId, suspensionState);
    } else

    if (processDefinitionId != null) {
      executionManager.updateExecutionSuspensionStateByProcessDefinitionId(processDefinitionId, suspensionState);
      taskManager.updateTaskSuspensionStateByProcessDefinitionId(processDefinitionId, suspensionState);
    } else

    if (processDefinitionKey != null) {
      executionManager.updateExecutionSuspensionStateByProcessDefinitionKey(processDefinitionKey, suspensionState);
      taskManager.updateTaskSuspensionStateByProcessDefinitionKey(processDefinitionKey, suspensionState);
    }

    getSetJobStateCmd().execute(commandContext);

    return null;
  }

  /**
   * Subclasses should return the wanted {@link SuspensionState} here.
   */
  protected abstract SuspensionState getNewSuspensionState();

  /**
   * Subclasses should return the type of the {@link AbstractSetJobStateCmd} here.
   * It will be used to suspend or activate the {@link Job}s.
   */
  protected abstract AbstractSetJobStateCmd getSetJobStateCmd();

}
