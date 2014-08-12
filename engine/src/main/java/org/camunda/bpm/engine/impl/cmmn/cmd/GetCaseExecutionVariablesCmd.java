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
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.exception.cmmn.CaseExecutionNotFoundException;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * @author Roman Smirnov
 *
 */
public class GetCaseExecutionVariablesCmd implements Command<Map<String, Object>>, Serializable {

  private static final long serialVersionUID = 1L;

  protected String caseExecutionId;
  protected Collection<String> variableNames;
  protected boolean isLocal;

  public GetCaseExecutionVariablesCmd(String caseExecutionId, Collection<String> variableNames, boolean isLocal) {
    this.caseExecutionId = caseExecutionId;
    this.variableNames = variableNames;
    this.isLocal = isLocal;
  }

  public Map<String, Object> execute(CommandContext commandContext) {
    ensureNotNull("caseExecutionId", caseExecutionId);

    CaseExecutionEntity caseExecution = commandContext
      .getCaseExecutionManager()
      .findCaseExecutionById(caseExecutionId);

    ensureNotNull(CaseExecutionNotFoundException.class, "case execution " + caseExecutionId + " doesn't exist", "caseExecution", caseExecution);

    Map<String, Object> caseExecutionVariables;

    if (isLocal) {
      caseExecutionVariables = caseExecution.getVariablesLocal();
    } else {
      caseExecutionVariables = caseExecution.getVariables();
    }

    if (variableNames != null && variableNames.size() > 0) {
      // if variableNames is not empty, return only variable names mentioned in it
      Map<String, Object> tempVariables = new HashMap<String, Object>();

      for (String variableName : variableNames) {
        if (caseExecutionVariables.containsKey(variableName)) {
          tempVariables.put(variableName, caseExecutionVariables.get(variableName));
        }
      }

      caseExecutionVariables = tempVariables;
    }

    return caseExecutionVariables;
  }

}
