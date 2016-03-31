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

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Map;

import org.camunda.bpm.engine.exception.NotAllowedException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.exception.cmmn.CaseDefinitionNotFoundException;
import org.camunda.bpm.engine.exception.cmmn.CaseIllegalStateTransitionException;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmmn.cmd.CreateCaseInstanceCmd;
import org.camunda.bpm.engine.impl.cmmn.operation.CmmnOperationLogger;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.CaseInstanceBuilder;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

/**
 * @author Roman Smirnov
 *
 */
public class CaseInstanceBuilderImpl implements CaseInstanceBuilder {

  private final static CmmnOperationLogger LOG = ProcessEngineLogger.CMMN_OPERATION_LOGGER;

  protected CommandExecutor commandExecutor;
  protected CommandContext commandContext;

  protected String caseDefinitionKey;
  protected String caseDefinitionId;
  protected String businessKey;
  protected VariableMap variables;

  protected String caseDefinitionTenantId;
  protected boolean isTenantIdSet = false;

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
    this.caseDefinitionId = caseDefinitionId;
  }

  public CaseInstanceBuilder businessKey(String businessKey) {
    this.businessKey = businessKey;
    return this;
  }

  public CaseInstanceBuilder caseDefinitionTenantId(String tenantId) {
    this.caseDefinitionTenantId = tenantId;
    isTenantIdSet = true;
    return this;
  }

  public CaseInstanceBuilder caseDefinitionWithoutTenantId() {
    this.caseDefinitionTenantId = null;
    isTenantIdSet = true;
    return this;
  }

  public CaseInstanceBuilder setVariable(String variableName, Object variableValue) {
    ensureNotNull(NotValidException.class, "variableName", variableName);
    if (variables == null) {
      variables = Variables.createVariables();
    }
    variables.putValue(variableName, variableValue);
    return this;
  }

  public CaseInstanceBuilder setVariables(Map<String, Object> variables) {
    if (variables != null) {
      if (this.variables == null) {
        this.variables = Variables.fromMap(variables);
      }
      else {
        this.variables.putAll(variables);
      }
    }
    return this;
  }

  public CaseInstance create() {
    if (isTenantIdSet && caseDefinitionId != null) {
      throw LOG.exceptionCreateCaseInstanceByIdAndTenantId();
    }

    try {
      CreateCaseInstanceCmd command = new CreateCaseInstanceCmd(this);
      if(commandExecutor != null) {
        return commandExecutor.execute(command);
      } else {
        return command.execute(commandContext);
      }

    } catch (CaseDefinitionNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);

    } catch (NullValueException e) {
      throw new NotValidException(e.getMessage(), e);

    } catch (CaseIllegalStateTransitionException e) {
      throw new NotAllowedException(e.getMessage(), e);

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

  public VariableMap getVariables() {
    return variables;
  }

  public String getCaseDefinitionTenantId() {
    return caseDefinitionTenantId;
  }

  public boolean isTenantIdSet() {
    return isTenantIdSet;
  }

}
