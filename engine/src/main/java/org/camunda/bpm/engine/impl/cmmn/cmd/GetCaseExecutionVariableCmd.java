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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;

import org.camunda.bpm.engine.exception.cmmn.CaseExecutionNotFoundException;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
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
    ensureNotNull("caseExecutionId", caseExecutionId);
    ensureNotNull("variableName", variableName);

    CaseExecutionEntity caseExecution = commandContext
      .getCaseExecutionManager()
      .findCaseExecutionById(caseExecutionId);

    ensureNotNull(CaseExecutionNotFoundException.class, "case execution " + caseExecutionId + " doesn't exist", "caseExecution", caseExecution);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkReadCaseInstance(caseExecution);
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
