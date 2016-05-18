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
import java.util.Collection;

import org.camunda.bpm.engine.exception.cmmn.CaseExecutionNotFoundException;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;

/**
 * @author Roman Smirnov
 * @author Daniel Meyer
 */
public class GetCaseExecutionVariablesCmd implements Command<VariableMap>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String caseExecutionId;
  protected Collection<String> variableNames;
  protected boolean isLocal;
  protected boolean deserializeValues;

  public GetCaseExecutionVariablesCmd(String caseExecutionId, Collection<String> variableNames, boolean isLocal, boolean deserializeValues) {
    this.caseExecutionId = caseExecutionId;
    this.variableNames = variableNames;
    this.isLocal = isLocal;
    this.deserializeValues = deserializeValues;
  }

  public VariableMap execute(CommandContext commandContext) {
    ensureNotNull("caseExecutionId", caseExecutionId);

    CaseExecutionEntity caseExecution = commandContext
      .getCaseExecutionManager()
      .findCaseExecutionById(caseExecutionId);

    ensureNotNull(CaseExecutionNotFoundException.class, "case execution " + caseExecutionId + " doesn't exist", "caseExecution", caseExecution);

    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkReadCaseInstance(caseExecution);
    }

    VariableMapImpl result = new VariableMapImpl();
    // collect variables
    caseExecution.collectVariables(result, variableNames, isLocal, deserializeValues);

    return result;
  }

}
