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

import java.util.HashMap;
import java.util.Map;
import org.camunda.bpm.engine.impl.cmmn.cmd.CreateCaseInstanceCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.CaseInstanceBuilder;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author Roman Smirnov
 *
 */
public class CaseInstanceBuilderImpl implements CaseInstanceBuilder {

  protected CommandExecutor commandExecutor;
  protected CommandContext commandContext;

  protected String caseDefinitionKey;
  protected String caseDefinitionId;
  protected String businessKey;
  protected Map<String, Object> variables;

  public CaseInstanceBuilderImpl(CommandExecutor commandExecutor, String caseDefinitionKey, String caseDefinitionId) {
    this(caseDefinitionKey, caseDefinitionId);
    ensureNotNull("commandExecutor", commandExecutor);
    this.commandExecutor = commandExecutor;
  }

  public CaseInstanceBuilderImpl(CommandContext commandContext, String caseDefinitionKey, String caseDefinitionId) {
    this(caseDefinitionKey, caseDefinitionId);
    ensureNotNull("commandContext", commandContext);
    this.commandContext = commandContext;
  }

  private CaseInstanceBuilderImpl(String caseDefinitionKey, String caseDefinitionId) {
    this.caseDefinitionKey = caseDefinitionKey;
    this.caseDefinitionId= caseDefinitionId;
  }

  public CaseInstanceBuilder businessKey(String businessKey) {
    this.businessKey = businessKey;
    return this;
  }

  public CaseInstanceBuilder setVariable(String variableName, Object variableValue) {
    ensureNotNull("variableName", variableName);
    if (variables == null) {
      variables = new HashMap<String, Object>();
    }
    variables.put(variableName, variableValue);
    return this;
  }

  public CaseInstanceBuilder setVariables(Map<String, Object> variables) {
    if (variables != null) {
      if (this.variables == null) {
        this.variables = new HashMap<String, Object>();
      }
      this.variables.putAll(variables);
    }
    return this;
  }

  public CaseInstance create() {
    CreateCaseInstanceCmd command = new CreateCaseInstanceCmd(this);
    if(commandExecutor != null) {
      return commandExecutor.execute(command);
    } else {
      return command.execute(commandContext);
    }
  }

  // getters ////////////////////////////////////

  public String getCaseDefinitionKey() {
    return caseDefinitionKey;
  }

  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }

}
