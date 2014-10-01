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
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.exception.cmmn.CaseExecutionNotFoundException;
import org.camunda.bpm.engine.impl.ServiceImpl;
import org.camunda.bpm.engine.impl.cmmn.cmd.GetCaseExecutionVariableCmd;
import org.camunda.bpm.engine.impl.cmmn.cmd.GetCaseExecutionVariablesCmd;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionQueryImpl;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseInstanceQueryImpl;
import org.camunda.bpm.engine.runtime.CaseExecutionCommandBuilder;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.CaseInstanceBuilder;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;

/**
 * @author Roman Smirnov
 *
 */
public class CaseServiceImpl extends ServiceImpl implements CaseService {

  public CaseInstanceBuilder withCaseDefinitionByKey(String caseDefinitionKey) {
    return new CaseInstanceBuilderImpl(commandExecutor, caseDefinitionKey, null);
  }

  public CaseInstanceBuilder withCaseDefinition(String caseDefinitionId) {
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
    try {
      return commandExecutor.execute(new GetCaseExecutionVariablesCmd(caseExecutionId, null, false));

    } catch (NullValueException e) {
      throw new NotValidException(e.getMessage(), e);

    } catch (CaseExecutionNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);

    }
  }

  public Map<String, Object> getVariablesLocal(String caseExecutionId) {
    try {
      return commandExecutor.execute(new GetCaseExecutionVariablesCmd(caseExecutionId, null, true));

    } catch (NullValueException e) {
      throw new NotValidException(e.getMessage(), e);

    } catch (CaseExecutionNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);

    }
  }

  public Map<String, Object> getVariables(String caseExecutionId, Collection<String> variableNames) {
    try {
      return commandExecutor.execute(new GetCaseExecutionVariablesCmd(caseExecutionId, variableNames, false));

    } catch (NullValueException e) {
      throw new NotValidException(e.getMessage(), e);

    } catch (CaseExecutionNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);

    }

  }

  public Map<String, Object> getVariablesLocal(String caseExecutionId, Collection<String> variableNames) {
    try {
      return commandExecutor.execute(new GetCaseExecutionVariablesCmd(caseExecutionId, variableNames, true));

    } catch (NullValueException e) {
      throw new NotValidException(e.getMessage(), e);

    } catch (CaseExecutionNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);

    }
  }

  public Object getVariable(String caseExecutionId, String variableName) {
    try {
      return commandExecutor.execute(new GetCaseExecutionVariableCmd(caseExecutionId, variableName, false));

    } catch (NullValueException e) {
      throw new NotValidException(e.getMessage(), e);

    } catch (CaseExecutionNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);

    }

  }

  public Object getVariableLocal(String caseExecutionId, String variableName) {
    try {
      return commandExecutor.execute(new GetCaseExecutionVariableCmd(caseExecutionId, variableName, true));

    } catch (NullValueException e) {
      throw new NotValidException(e.getMessage(), e);

    } catch (CaseExecutionNotFoundException e) {
      throw new NotFoundException(e.getMessage(), e);

    }
  }

  public void setVariables(String caseExecutionId, Map<String, Object> variables) {
    withCaseExecution(caseExecutionId).setVariables(variables).execute();
  }

  public void setVariablesLocal(String caseExecutionId, Map<String, Object> variables) {
    withCaseExecution(caseExecutionId).setVariablesLocal(variables).execute();
  }

  public void setVariable(String caseExecutionId, String variableName, Object value) {
    withCaseExecution(caseExecutionId).setVariable(variableName, value).execute();
  }

  public void setVariableLocal(String caseExecutionId, String variableName, Object value) {
    withCaseExecution(caseExecutionId).setVariableLocal(variableName, value).execute();
  }

  public void removeVariables(String caseExecutionId, Collection<String> variableNames) {
    withCaseExecution(caseExecutionId).removeVariables(variableNames).execute();
  }

  public void removeVariablesLocal(String caseExecutionId, Collection<String> variableNames) {
    withCaseExecution(caseExecutionId).removeVariablesLocal(variableNames).execute();
  }

  public void removeVariable(String caseExecutionId, String variableName) {
    withCaseExecution(caseExecutionId).removeVariable(variableName).execute();
  }

  public void removeVariableLocal(String caseExecutionId, String variableName) {
    withCaseExecution(caseExecutionId).removeVariableLocal(variableName).execute();
  }

  public CaseInstance createCaseInstanceByKey(String caseDefinitionKey) {
    return withCaseDefinitionByKey(caseDefinitionKey).create();
  }

  public CaseInstance createCaseInstanceByKey(String caseDefinitionKey, String businessKey) {
    return withCaseDefinitionByKey(caseDefinitionKey).businessKey(businessKey).create();
  }

  public CaseInstance createCaseInstanceByKey(String caseDefinitionKey, Map<String, Object> variables) {
    return withCaseDefinitionByKey(caseDefinitionKey).setVariables(variables).create();
  }

  public CaseInstance createCaseInstanceByKey(String caseDefinitionKey, String businessKey, Map<String, Object> variables) {
    return withCaseDefinitionByKey(caseDefinitionKey).businessKey(businessKey)
        .setVariables(variables).create();
  }

  public CaseInstance createCaseInstanceById(String caseDefinitionId) {
    return withCaseDefinition(caseDefinitionId).create();
  }

  public CaseInstance createCaseInstanceById(String caseDefinitionId, String businessKey) {
    return withCaseDefinition(caseDefinitionId).businessKey(businessKey).create();
  }

  public CaseInstance createCaseInstanceById(String caseDefinitionId, Map<String, Object> variables) {
    return withCaseDefinition(caseDefinitionId).setVariables(variables).create();
  }

  public CaseInstance createCaseInstanceById(String caseDefinitionId, String businessKey, Map<String, Object> variables) {
    return withCaseDefinition(caseDefinitionId).businessKey(businessKey)
        .setVariables(variables).create();
  }

  public void manuallyStartCaseExecution(String caseExecutionId) {
    withCaseExecution(caseExecutionId).manualStart();
  }

  public void manuallyStartCaseExecution(String caseExecutionId, Map<String, Object> variables) {
    withCaseExecution(caseExecutionId).setVariables(variables).manualStart();
  }

  public void disableCaseExecution(String caseExecutionId) {
    withCaseExecution(caseExecutionId).disable();
  }

  public void disableCaseExecution(String caseExecutionId, Map<String, Object> variables) {
    withCaseExecution(caseExecutionId).setVariables(variables).disable();
  }

  public void reenableCaseExecution(String caseExecutionId) {
    withCaseExecution(caseExecutionId).reenable();
  }

  public void reenableCaseExecution(String caseExecutionId, Map<String, Object> variables) {
    withCaseExecution(caseExecutionId).setVariables(variables).reenable();
  }

  public void completeCaseExecution(String caseExecutionId) {
    withCaseExecution(caseExecutionId).complete();
  }

  public void completeCaseExecution(String caseExecutionId, Map<String, Object> variables) {
    withCaseExecution(caseExecutionId).setVariables(variables).complete();
  }

  public void closeCaseInstance(String caseInstanceId) {
    withCaseExecution(caseInstanceId).close();
  }

}
