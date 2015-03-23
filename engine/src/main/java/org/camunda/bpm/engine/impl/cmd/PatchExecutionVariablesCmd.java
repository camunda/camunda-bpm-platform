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

import org.camunda.bpm.engine.impl.core.variable.scope.AbstractVariableScope;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * Patches execution variables: First, applies modifications to existing variables and then deletes
 * specified variables. 
 * 
 * @author Thorben Lindhauer
 *
 */
public class PatchExecutionVariablesCmd extends AbstractPatchVariablesCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;

  
  public PatchExecutionVariablesCmd(String executionId, Map<String, ? extends Object> modifications, Collection<String> deletions, boolean isLocal) {
    super(executionId, modifications, deletions, isLocal);
  }

  @Override
  public void checkParameters() {
    ensureNotNull("executionId", entityId);
  }

  @Override
  public AbstractVariableScope getEntity() {
    ExecutionEntity execution = commandContext
      .getExecutionManager()
      .findExecutionById(entityId);

    ensureNotNull("execution " + entityId + " doesn't exist", "execution", execution);

    return execution;
  }

  @Override
  public void logVariableOperation(AbstractVariableScope scope) {
    ExecutionEntity execution = (ExecutionEntity) scope;
    commandContext.getOperationLogManager().logVariableOperation(getLogEntryOperation(), execution,
      PropertyChange.EMPTY_CHANGE);
  }

  @Override
  public void executeOperation(AbstractVariableScope scope) {
    new SetExecutionVariablesCmd(entityId, variables, isLocal).disableLogUserOperation().execute(commandContext);
    new RemoveExecutionVariablesCmd(entityId, deletions, isLocal).disableLogUserOperation().execute(commandContext);
  }
}
