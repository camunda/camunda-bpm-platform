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
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * @author Thorben Lindhauer
 */
public abstract class SetScopeVariableFromSerializedCmd implements Command<Void> {

  protected boolean isLocal;
  protected String variableName;
  protected Object serializedVariableValue;
  protected String variableType;
  protected Map<String, Object> configuration;
  protected String scopeId;

  public SetScopeVariableFromSerializedCmd(String scopeId, String variableName, Object serializedVariableValue,
      String variableType, Map<String, Object> configuration, boolean isLocal) {
    this.isLocal = isLocal;
    this.variableName = variableName;
    this.serializedVariableValue = serializedVariableValue;
    this.variableType = variableType;
    this.configuration = configuration;
    this.scopeId = scopeId;
  }

  public Void execute(CommandContext commandContext) {
    ensureNotNull("variableType", variableType);

    PersistentVariableScope scope = getPersistentVariableScope(commandContext);

    if (isLocal) {
      scope.setVariableLocalFromSerialized(variableName, serializedVariableValue, variableType, configuration);
    } else {
      scope.setVariableFromSerialized(variableName, serializedVariableValue, variableType, configuration);
    }

    return null;
  }

  protected abstract PersistentVariableScope getPersistentVariableScope(CommandContext commandContext);
}
