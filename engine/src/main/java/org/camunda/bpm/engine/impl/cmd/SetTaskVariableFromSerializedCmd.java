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

import java.util.Map;

import org.camunda.bpm.engine.delegate.PersistentVariableScope;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;

/**
 * @author Thorben Lindhauer
 */
public class SetTaskVariableFromSerializedCmd extends SetScopeVariableFromSerializedCmd {

  public SetTaskVariableFromSerializedCmd(String taskId, String variableName, Object serializedVariableValue, String variableType,
      Map<String, Object> configuration, boolean isLocal) {
    super(taskId, variableName, serializedVariableValue, variableType, configuration, isLocal);
  }

  protected PersistentVariableScope getPersistentVariableScope(CommandContext commandContext) {
    ensureNotNull("taskId", scopeId);

    TaskEntity task = commandContext
      .getTaskManager()
      .findTaskById(scopeId);

    ensureNotNull("task " + scopeId + " doesn't exist", "task", task);

    return task;
  }

}
