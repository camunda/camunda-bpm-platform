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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * Patches task variables: First, applies modifications to existing variables and then deletes
 * specified variables.
 *
 * @author kristin.polenz@camunda.com
 *
 */
public class PatchTaskVariablesCmd extends AbstractVariableCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String taskId;
  protected Map<String, ? extends Object> modifications;
  protected Collection<String> deletions;
  protected boolean isLocal;

  public PatchTaskVariablesCmd(String taskId, Map<String, ? extends Object> modifications, Collection<String> deletions, boolean isLocal) {
    this.modifications = modifications;
    this.deletions = deletions;
    this.taskId = taskId;
    this.isLocal = isLocal;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    ensureNotNull("taskId", taskId);

    TaskEntity task = commandContext
      .getTaskManager()
      .findTaskById(taskId);

    ensureNotNull("Cannot find task with id " + taskId, "task", task);

    new SetTaskVariablesCmd(taskId, modifications, isLocal).disableLogUserOperation().execute(commandContext);
    new RemoveTaskVariablesCmd(taskId, deletions, isLocal).disableLogUserOperation().execute(commandContext);

    if(!preventLogUserOperation) {
      String processDefinitionKey = null;
      if(task.getExecution() != null) {
        processDefinitionKey = ((ProcessDefinitionEntity) task.getExecution().getProcessDefinition()).getKey();
      } else if(task.getProcessInstance() != null) {
        processDefinitionKey = ((ProcessDefinitionEntity) task.getProcessInstance().getProcessDefinition()).getKey();
      }

      commandContext.getOperationLogManager().logVariableOperation(getLogEntryOperation(), task.getExecutionId(),
        task.getProcessInstanceId(), task.getProcessDefinitionId(), processDefinitionKey,
        PropertyChange.EMPTY_CHANGE);
    }
    return null;
  }

  public String getLogEntryOperation() {
    return UserOperationLogEntry.OPERATION_TYPE_PATCH_TASK_VARIABLE;
  }

}
