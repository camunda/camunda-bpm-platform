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

package org.camunda.bpm.engine.impl;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.cmd.EvaluateStartConditionCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.ConditionEvaluationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;

public class ConditionEvaluationBuilderImpl implements ConditionEvaluationBuilder {
  protected CommandExecutor commandExecutor;
  protected CommandContext commandContext;

  protected String businessKey;
  protected String processInstanceId;
  protected String processDefinitionId;

  protected VariableMap variables;

  protected String tenantId = null;
  protected boolean isTenantIdSet = false;

  public ConditionEvaluationBuilderImpl(CommandExecutor commandExecutor) {
    ensureNotNull("commandExecutor", commandExecutor);
    this.commandExecutor = commandExecutor;
  }

  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  public CommandContext getCommandContext() {
    return commandContext;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public VariableMap getVariables() {
    return variables;
  }

  public void setVariables(VariableMap variables) {
    this.variables = variables;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public boolean isTenantIdSet() {
    return isTenantIdSet;
  }

  public void setTenantIdSet(boolean isTenantIdSet) {
    this.isTenantIdSet = isTenantIdSet;
  }

  protected <T> T execute(Command<T> command) {
    if (commandExecutor != null) {
      return commandExecutor.execute(command);
    } else {
      return command.execute(commandContext);
    }
  }

  @Override
  public ConditionEvaluationBuilder processInstanceBusinessKey(String businessKey) {
    ensureNotNull("businessKey", businessKey);
    this.businessKey = businessKey;
    return this;
  }

  @Override
  public ConditionEvaluationBuilder processDefinitionId(String processDefinitionId) {
    ensureNotNull("processDefinitionId", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  @Override
  public ConditionEvaluationBuilder setVariable(String variableName, Object variableValue) {
    ensureNotNull("variableName", variableName);
    ensureVariablesInitialized();
    this.variables.put(variableName, variableValue);
    return this;
  }

  @Override
  public ConditionEvaluationBuilder setVariables(Map<String, Object> variables) {
    ensureNotNull("variables", variables);
    ensureVariablesInitialized();
    this.variables.putAll(variables);
    return this;
  }

  @Override
  public ConditionEvaluationBuilder tenantId(String tenantId) {
    ensureNotNull(
        "The tenant-id cannot be null. Use 'withoutTenantId()' if you want to evaluate conditional start event with a process definition which has no tenant-id.",
        "tenantId", tenantId);

    isTenantIdSet = true;
    this.tenantId = tenantId;
    return this;
  }

  @Override
  public ConditionEvaluationBuilder withoutTenantId() {
    isTenantIdSet = true;
    tenantId = null;
    return this;
  }

  @Override
  public List<ProcessInstance> evaluateStartConditions() {
    return execute(new EvaluateStartConditionCmd(this));
  }

  protected void ensureVariablesInitialized() {
    if (variables == null) {
      variables = new VariableMapImpl();
    }
  }
}
