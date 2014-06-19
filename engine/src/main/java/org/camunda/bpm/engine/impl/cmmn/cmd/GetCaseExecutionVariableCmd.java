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
package org.camunda.bpm.engine.impl.cmmn.cmd;

import java.io.Serializable;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * @author Roman Smirnov
 *
 */
public class GetCaseExecutionVariableCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String caseExecutionId;
  protected String variableName;
  protected boolean isLocal;

  public GetCaseExecutionVariableCmd(String caseExecutionId, String variableName, boolean isLocal) {
    this.caseExecutionId = caseExecutionId;
    this.variableName = variableName;
    this.isLocal = isLocal;
  }

  public Object execute(CommandContext commandContext) {
    if(caseExecutionId == null) {
      throw new ProcessEngineException("caseExecutionId is null");
    }
    if(variableName == null) {
      throw new ProcessEngineException("variableName is null");
    }

    CaseExecutionEntity caseExecution = commandContext
        .getCaseExecutionManager()
        .findCaseExecutionById(caseExecutionId);

    if (caseExecution==null) {
      throw new ProcessEngineException("case execution "+caseExecutionId+" doesn't exist");
    }

    Object value;

    if (isLocal) {
      value = caseExecution.getVariableLocal(variableName);
    } else {
      value = caseExecution.getVariable(variableName);
    }

    return value;
  }

}
