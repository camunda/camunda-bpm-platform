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

import java.io.Serializable;

import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;


/**
 * @author Tom Baeyens
 */
public class GetExecutionVariableCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String executionId;
  protected String variableName;
  protected boolean isLocal;

  public GetExecutionVariableCmd(String executionId, String variableName, boolean isLocal) {
    this.executionId = executionId;
    this.variableName = variableName;
    this.isLocal = isLocal;
  }

  public Object execute(CommandContext commandContext) {
    ensureNotNull("executionId", executionId);
    ensureNotNull("variableName", variableName);

    ExecutionEntity execution = commandContext
      .getExecutionManager()
      .findExecutionById(executionId);

    ensureNotNull("execution " + executionId + " doesn't exist", "execution", execution);

    checkGetExecutionVariable(execution, commandContext);

    Object value;

    if (isLocal) {
      value = execution.getVariableLocal(variableName, true);
    } else {
      value = execution.getVariable(variableName, true);
    }

    return value;
  }

  protected void checkGetExecutionVariable(ExecutionEntity execution, CommandContext commandContext) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkReadProcessInstance(execution);
    }
  }
}
