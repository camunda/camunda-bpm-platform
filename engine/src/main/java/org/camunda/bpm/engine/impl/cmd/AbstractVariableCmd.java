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

import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * @author Stefan Hentschel.
 */
public abstract class AbstractVariableCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  protected CommandContext commandContext;
  protected String entityId;
  protected boolean isLocal;
  protected boolean preventLogUserOperation = false;

  public AbstractVariableCmd(String entityId, boolean isLocal) {
    this.entityId = entityId;
    this.isLocal = isLocal;
  }

  public AbstractVariableCmd disableLogUserOperation() {
    this.preventLogUserOperation = true;
    return this;
  }

  public Void execute(CommandContext commandContext) {
    this.commandContext = commandContext;

    AbstractVariableScope scope = getEntity();

    executeOperation(scope);

    if(!preventLogUserOperation) {
      logVariableOperation(scope);
    }

    return null;
  };

  protected abstract AbstractVariableScope getEntity();

  protected abstract void logVariableOperation(AbstractVariableScope scope);

  protected abstract void executeOperation(AbstractVariableScope scope);

  protected abstract String getLogEntryOperation();
}
