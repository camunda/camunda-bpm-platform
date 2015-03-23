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
import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.interceptor.Command;
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
public class PatchTaskVariablesCmd extends AbstractPatchVariablesCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  public PatchTaskVariablesCmd(String taskId, Map<String, ? extends Object> modifications, Collection<String> deletions, boolean isLocal) {
    super(taskId, modifications, deletions, isLocal);
  }

  @Override
  public void checkParameters() {
    ensureNotNull("taskId", entityId);
  }

  @Override
  public AbstractVariableScope getEntity() {
    TaskEntity task = commandContext
      .getTaskManager()
      .findTaskById(entityId);

    ensureNotNull("Cannot find task with id " + entityId, "task", task);

    return task;
  }

  @Override
  public void logVariableOperation(AbstractVariableScope scope) {
    TaskEntity task = (TaskEntity) scope;
    commandContext.getOperationLogManager().logVariableOperation(getLogEntryOperation(), task,
      PropertyChange.EMPTY_CHANGE);

  }

  @Override
  public void executeOperation(AbstractVariableScope scope) {
    new SetTaskVariablesCmd(entityId, variables, isLocal).disableLogUserOperation().execute(commandContext);
    new RemoveTaskVariablesCmd(entityId, deletions, isLocal).disableLogUserOperation().execute(commandContext);
  }

  public String getLogEntryOperation() {
    return UserOperationLogEntry.OPERATION_TYPE_MODIFY_VARIABLE;
  }

}
