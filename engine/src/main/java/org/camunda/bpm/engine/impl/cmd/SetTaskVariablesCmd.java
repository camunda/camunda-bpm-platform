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

import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.persistence.entity.*;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class SetTaskVariablesCmd extends AbstractSetVariableCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  public SetTaskVariablesCmd(String taskId, Map<String, ? extends Object> variables, boolean isLocal) {
    super(taskId, variables, isLocal);
  }

  @Override
  public void checkParameters() {
    ensureNotNull("taskId", entityId);
  }

  @Override
  public AbstractVariableScope getEntity() {
    TaskEntity task =  commandContext
      .getTaskManager()
      .findTaskById(entityId);
    ensureNotNull("task " + entityId + " doesn't exist", "task", task);

    return task;
  }

  @Override
  public void logVariableOperation(AbstractVariableScope scope) {
    TaskEntity task = (TaskEntity) scope;
    commandContext.getOperationLogManager().logVariableOperation(getLogEntryOperation(), task,
      PropertyChange.EMPTY_CHANGE);
  }
}
