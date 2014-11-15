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

import java.util.Collection;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.impl.core.variable.VariableMapImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;
import org.camunda.bpm.engine.variable.VariableMap;

/**
 * @author Daniel Meyer
 *
 */
public class GetTaskFormVariablesCmd extends AbstractGetFormVariablesCmd {

  private static final long serialVersionUID = 1L;

  public GetTaskFormVariablesCmd(String taskId, Collection<String> variableNames, boolean deserializeObjectValues) {
    super(taskId, variableNames, deserializeObjectValues);
  }

  public VariableMap execute(CommandContext commandContext) {

    TaskEntity task = commandContext.getTaskManager()
      .findTaskById(resourceId);

    if(task == null) {
      throw new BadUserRequestException("Cannot find task with id '"+resourceId+"'.");
    }

    VariableMapImpl result = new VariableMapImpl();

    // first, evaluate form fields
    TaskFormData taskFormData = task.getTaskDefinition().getTaskFormHandler().createTaskForm(task);
    for (FormField formField : taskFormData.getFormFields()) {
      if(formVariableNames == null || formVariableNames.contains(formField.getId())) {
        result.put(formField.getId(), createVariable(formField, task));
      }
    }

    // collect remaining variables from task scope and parent scopes
    task.collectVariables(result, formVariableNames, false, deserializeObjectValues);

    return result;
  }

}
