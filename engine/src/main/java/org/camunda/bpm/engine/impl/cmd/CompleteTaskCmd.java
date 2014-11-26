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
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


/**
 * @author Joram Barrez
 */
public class CompleteTaskCmd implements Command<Void>, Serializable {
      
  private static final long serialVersionUID = 1L;
  
  protected String taskId;
  protected Map<String, Object> variables;
  
  public CompleteTaskCmd(String taskId, Map<String, Object> variables) {
    this.taskId = taskId;
    this.variables = variables;
  }
  
  public Void execute(CommandContext commandContext) {
    ensureNotNull("taskId", taskId);

    TaskEntity task = commandContext
      .getTaskManager()
      .findTaskById(taskId);

    ensureNotNull("Cannot find task with id " + taskId, "task", task);

    if (variables != null) {
      task.setExecutionVariables(variables);
    }

    completeTask(task);

    return null;
  }
  
  protected void completeTask(TaskEntity task) {
    task.complete();
  }
}
