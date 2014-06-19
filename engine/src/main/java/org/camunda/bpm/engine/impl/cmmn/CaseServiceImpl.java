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
package org.camunda.bpm.engine.impl.cmmn;

import java.util.Collection;
import java.util.Map;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.impl.ServiceImpl;
import org.camunda.bpm.engine.impl.cmmn.cmd.GetCaseExecutionVariableCmd;
import org.camunda.bpm.engine.impl.cmmn.cmd.GetCaseExecutionVariablesCmd;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionQueryImpl;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseInstanceQueryImpl;
import org.camunda.bpm.engine.runtime.CaseExecutionCommandBuilder;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;
import org.camunda.bpm.engine.runtime.CaseInstanceBuilder;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;

/**
 * @author Roman Smirnov
 *
 */
public class CaseServiceImpl extends ServiceImpl implements CaseService {

  public CaseInstanceBuilder createCaseInstanceByKey(String caseDefinitionKey) {
    return new CaseInstanceBuilderImpl(commandExecutor, caseDefinitionKey, null);
  }

  public CaseInstanceBuilder createCaseInstanceById(String caseDefinitionId) {
    return new CaseInstanceBuilderImpl(commandExecutor, null, caseDefinitionId);
  }

  public CaseInstanceQuery createCaseInstanceQuery() {
    return new CaseInstanceQueryImpl(commandExecutor);
  }

  public CaseExecutionQuery createCaseExecutionQuery() {
    return new CaseExecutionQueryImpl(commandExecutor);
  }

  public CaseExecutionCommandBuilder withCaseExecution(String caseExecutionId) {
    return new CaseExecutionCommandBuilderImpl(commandExecutor, caseExecutionId);
  }

  public Map<String, Object> getVariables(String caseExecutionId) {
    return commandExecutor.execute(new GetCaseExecutionVariablesCmd(caseExecutionId, null, false));
  }

  public Map<String, Object> getVariablesLocal(String caseExecutionId) {
    return commandExecutor.execute(new GetCaseExecutionVariablesCmd(caseExecutionId, null, true));
  }

  public Map<String, Object> getVariables(String caseExecutionId, Collection<String> variableNames) {
    return commandExecutor.execute(new GetCaseExecutionVariablesCmd(caseExecutionId, variableNames, false));
  }

  public Map<String, Object> getVariablesLocal(String caseExecutionId, Collection<String> variableNames) {
    return commandExecutor.execute(new GetCaseExecutionVariablesCmd(caseExecutionId, variableNames, true));
  }

  public Object getVariable(String caseExecutionId, String variableName) {
    return commandExecutor.execute(new GetCaseExecutionVariableCmd(caseExecutionId, variableName, false));
  }

  public Object getVariableLocal(String caseExecutionId, String variableName) {
    return commandExecutor.execute(new GetCaseExecutionVariableCmd(caseExecutionId, variableName, true));
  }

}
