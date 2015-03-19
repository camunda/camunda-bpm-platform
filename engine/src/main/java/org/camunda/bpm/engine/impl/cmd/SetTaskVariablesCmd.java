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
import java.util.Map;

import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.*;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class SetTaskVariablesCmd extends AbstractVariableCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  protected Map<String, ? extends Object> variables;
  protected boolean isLocal;
  protected String taskId;

  public SetTaskVariablesCmd(String taskId, Map<String, ? extends Object> variables, boolean isLocal) {
    this.taskId = taskId;
    this.variables = variables;
    this.isLocal = isLocal;
  }

  public Void execute(CommandContext commandContext) {
    ensureNotNull("taskId", taskId);

    TaskEntity task = commandContext
      .getTaskManager()
      .findTaskById(taskId);

    ensureNotNull("Cannot find task with id " + taskId, "task", task);

    if (isLocal) {
      task.setVariablesLocal(variables);
    } else {
      task.setVariables(variables);
    }

    if(!preventLogUserOperation) {
      String processDefinitionKey = null;
      if(task.getExecution() != null) {
        processDefinitionKey = ((ProcessDefinitionEntity) task.getExecution().getProcessDefinition()).getKey();
      } else if(task.getProcessInstance() != null) {
        processDefinitionKey = ((ProcessDefinitionEntity) task.getProcessInstance().getProcessDefinition()).getKey();
      }

      commandContext.getOperationLogManager().logVariableOperation(getLogEntryOperation(), task.getExecutionId(),
        task.getProcessInstanceId(), task.getProcessDefinitionId(), processDefinitionKey, PropertyChange.EMPTY_CHANGE);
    }

    return null;
  }

  public String getLogEntryOperation() {
    return UserOperationLogEntry.OPERATION_TYPE_SET_TASK_VARIABLE;
  }
}
