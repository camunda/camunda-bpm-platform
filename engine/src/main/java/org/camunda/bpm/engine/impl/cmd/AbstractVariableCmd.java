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

import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Stefan Hentschel.
 */
public abstract class AbstractVariableCmd implements Command<Void>, Serializable {

  protected boolean preventLogUserOperation = false;
  protected boolean isLocal;
  protected Map<String, ?> variables;
  protected String entityId;
  protected CommandContext commandContext;

  public AbstractVariableCmd(String entityId, Map<String, ?> variables, boolean isLocal) {
    this.entityId = entityId;
    this.variables = variables;
    this.isLocal = isLocal;
  }

  public AbstractVariableCmd disableLogUserOperation() {
    this.preventLogUserOperation = true;
    return this;
  }

  public void setVariables(AbstractVariableScope scope, Map<String, ?> variables, boolean isLocal) {
    if (isLocal) {
      scope.setVariablesLocal(variables);
    } else {
      scope.setVariables(variables);
    }
  }


  public Void execute(CommandContext commandContext) {
    this.commandContext = commandContext;
    checkParameters();

    AbstractVariableScope scope = getEntity();

    executeOperation(scope);

    if(!preventLogUserOperation) {
      logVariableOperation(scope);
    }

    return null;
  };

  public abstract void checkParameters();

  public abstract AbstractVariableScope getEntity();

  public abstract void logVariableOperation(AbstractVariableScope scope);

  public abstract void executeOperation(AbstractVariableScope scope);

  public abstract String getLogEntryOperation();
}
