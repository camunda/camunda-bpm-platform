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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;

import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.variable.value.TypedValue;


/**
 * @author Daniel Meyer
 */
public class GetTaskVariableCmdTyped implements Command<TypedValue>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String taskId;
  protected String variableName;
  protected boolean isLocal;
  protected boolean deserializeValue;

  public GetTaskVariableCmdTyped(String taskId, String variableName, boolean isLocal, boolean deserializeValue) {
    this.taskId = taskId;
    this.variableName = variableName;
    this.isLocal = isLocal;
    this.deserializeValue = deserializeValue;
  }

  public TypedValue execute(CommandContext commandContext) {
    ensureNotNull("taskId", taskId);
    ensureNotNull("variableName", variableName);

    TaskEntity task = Context
      .getCommandContext()
      .getTaskManager()
      .findTaskById(taskId);

    ensureNotNull("task " + taskId + " doesn't exist", "task", task);

    checkGetTaskVariableTyped(task, commandContext);

    TypedValue value;

    if (isLocal) {
      value = task.getVariableLocalTyped(variableName, deserializeValue);
    } else {
      value = task.getVariableTyped(variableName, deserializeValue);
    }

    return value;
  }

  protected void checkGetTaskVariableTyped(TaskEntity task, CommandContext commandContext) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkReadTask(task);
    }
  }
}
