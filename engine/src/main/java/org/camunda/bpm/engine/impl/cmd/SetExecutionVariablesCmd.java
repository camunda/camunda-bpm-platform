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

import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

import java.io.Serializable;
import java.util.Map;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class SetExecutionVariablesCmd extends AbstractVariableCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;
  
  private String executionId;
  protected Map<String, ? extends Object> variables;
  protected boolean isLocal;

  
  public SetExecutionVariablesCmd(String executionId, Map<String, ? extends Object> variables, boolean isLocal) {
    this.executionId = executionId;
    this.variables = variables;
    this.isLocal = isLocal;

  }
  
  @Override
  public Void execute(CommandContext commandContext) {
    ensureNotNull("executionId", executionId);

    ExecutionEntity execution = commandContext
      .getExecutionManager()
      .findExecutionById(executionId);

    ensureNotNull("execution " + executionId + " doesn't exist", "execution", execution);

    if (isLocal) {
      execution.setVariablesLocal(variables);
    } else {
      execution.setVariables(variables);
    }

    if(!preventLogUserOperation) {
      String processDefinitionKey = ((ProcessDefinitionEntity) execution.getProcessDefinition()).getKey();
      commandContext.getOperationLogManager().logVariableOperation(getLogEntryOperation(), execution.getId(),
        execution.getProcessInstanceId(), execution.getProcessDefinitionId(), processDefinitionKey, PropertyChange.EMPTY_CHANGE);
    }

    return null;
  }

  public String getLogEntryOperation() {
    return UserOperationLogEntry.OPERATION_TYPE_SET_EXECUTION_VARIABLE;
  }
}

