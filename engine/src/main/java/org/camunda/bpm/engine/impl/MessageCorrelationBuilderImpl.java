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

import java.util.List;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Map;

import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.cmd.CorrelateAllMessageCmd;
import org.camunda.bpm.engine.impl.cmd.CorrelateMessageCmd;
import org.camunda.bpm.engine.impl.cmd.CorrelateStartMessageCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;

/**
 * @author Daniel Meyer
 * @author Christopher Zell
 *
 */
public class MessageCorrelationBuilderImpl implements MessageCorrelationBuilder {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected CommandExecutor commandExecutor;
  protected CommandContext commandContext;

  protected boolean isExclusiveCorrelation = false;

  protected String messageName;
  protected String businessKey;
  protected String processInstanceId;
  protected String processDefinitionId;

  protected VariableMap correlationProcessInstanceVariables;
  protected VariableMap correlationLocalVariables;
  protected VariableMap payloadProcessInstanceVariables;

  protected String tenantId = null;
  protected boolean isTenantIdSet = false;

  public MessageCorrelationBuilderImpl(CommandExecutor commandExecutor, String messageName) {
    this(messageName);
    ensureNotNull("commandExecutor", commandExecutor);
    this.commandExecutor = commandExecutor;
  }

  public MessageCorrelationBuilderImpl(CommandContext commandContext, String messageName) {
    this(messageName);
    ensureNotNull("commandContext", commandContext);
    this.commandContext = commandContext;
  }

  private MessageCorrelationBuilderImpl(String messageName) {
    this.messageName = messageName;
  }

  public MessageCorrelationBuilder processInstanceBusinessKey(String businessKey) {
    ensureNotNull("businessKey", businessKey);
    this.businessKey = businessKey;
    return this;
  }

  public MessageCorrelationBuilder processInstanceVariableEquals(String variableName, Object variableValue) {
    ensureNotNull("variableName", variableName);
    ensureCorrelationProcessInstanceVariablesInitialized();

    correlationProcessInstanceVariables.put(variableName, variableValue);
    return this;
  }

  public MessageCorrelationBuilder processInstanceVariablesEqual(Map<String, Object> variables) {
    ensureNotNull("variables", variables);
    ensureCorrelationProcessInstanceVariablesInitialized();

    correlationProcessInstanceVariables.putAll(variables);
    return this;
  }

  public MessageCorrelationBuilder localVariableEquals(String variableName, Object variableValue) {
    ensureNotNull("variableName", variableName);
    ensureCorrelationLocalVariablesInitialized();

    correlationLocalVariables.put(variableName, variableValue);
    return this;
  }

  public MessageCorrelationBuilder localVariablesEqual(Map<String, Object> variables) {
    ensureNotNull("variables", variables);
    ensureCorrelationLocalVariablesInitialized();

    correlationLocalVariables.putAll(variables);
    return this;
  }

  protected void ensureCorrelationProcessInstanceVariablesInitialized() {
    if(correlationProcessInstanceVariables == null) {
      correlationProcessInstanceVariables = new VariableMapImpl();
    }
  }

  protected void ensureCorrelationLocalVariablesInitialized() {
    if(correlationLocalVariables == null) {
      correlationLocalVariables = new VariableMapImpl();
    }
  }

  public MessageCorrelationBuilder processInstanceId(String id) {
    ensureNotNull("processInstanceId", id);
    this.processInstanceId = id;
    return this;
  }

  public MessageCorrelationBuilder processDefinitionId(String processDefinitionId) {
    ensureNotNull("processDefinitionId", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  public MessageCorrelationBuilder setVariable(String variableName, Object variableValue) {
    ensureNotNull("variableName", variableName);
    ensurePayloadProcessInstanceVariablesInitialized();
    payloadProcessInstanceVariables.put(variableName, variableValue);
    return this;
  }

  public MessageCorrelationBuilder setVariables(Map<String, Object> variables) {
    if (variables != null) {
      ensurePayloadProcessInstanceVariablesInitialized();
      payloadProcessInstanceVariables.putAll(variables);
    }
    return this;
  }

  protected void ensurePayloadProcessInstanceVariablesInitialized() {
    if (payloadProcessInstanceVariables == null) {
      payloadProcessInstanceVariables = new VariableMapImpl();
    }
  }

  public MessageCorrelationBuilder tenantId(String tenantId) {
    ensureNotNull(
        "The tenant-id cannot be null. Use 'withoutTenantId()' if you want to correlate the message to a process definition or an execution which has no tenant-id.",
        "tenantId", tenantId);

    isTenantIdSet = true;
    this.tenantId = tenantId;
    return this;
  }

  public MessageCorrelationBuilder withoutTenantId() {
    isTenantIdSet = true;
    tenantId = null;
    return this;
  }

  @Override
  public void correlate() {
    correlateWithResult();
  }

  @Override
  public MessageCorrelationResult correlateWithResult() {
    ensureProcessDefinitionIdNotSet();
    ensureProcessInstanceAndTenantIdNotSet();

    return execute(new CorrelateMessageCmd(this));
  }

  @Override
  public void correlateExclusively() {
    isExclusiveCorrelation = true;

    correlate();
  }

  @Override
  public void correlateAll() {
    correlateAllWithResult();
  }

  @Override
  public List<MessageCorrelationResult> correlateAllWithResult() {
    ensureProcessDefinitionIdNotSet();
    ensureProcessInstanceAndTenantIdNotSet();

    return execute(new CorrelateAllMessageCmd(this));
  }

  public ProcessInstance correlateStartMessage() {
    ensureCorrelationVariablesNotSet();
    ensureProcessDefinitionAndTenantIdNotSet();

    return execute(new CorrelateStartMessageCmd(this));
  }

  protected void ensureProcessDefinitionIdNotSet() {
    if(processDefinitionId != null) {
      throw LOG.exceptionCorrelateMessageWithProcessDefinitionId();
    }
  }

  protected void ensureProcessInstanceAndTenantIdNotSet() {
    if (processInstanceId != null && isTenantIdSet) {
      throw LOG.exceptionCorrelateMessageWithProcessInstanceAndTenantId();
    }
  }

  protected void ensureCorrelationVariablesNotSet() {
    if (correlationProcessInstanceVariables != null || correlationLocalVariables != null) {
      throw LOG.exceptionCorrelateStartMessageWithCorrelationVariables();
    }
  }

  protected void ensureProcessDefinitionAndTenantIdNotSet() {
    if (processDefinitionId != null && isTenantIdSet) {
      throw LOG.exceptionCorrelateMessageWithProcessDefinitionAndTenantId();
    }
  }

  protected <T> T execute(Command<T> command) {
    if(commandExecutor != null) {
      return commandExecutor.execute(command);
    } else {
      return command.execute(commandContext);
    }
  }

  // getters //////////////////////////////////

  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  public CommandContext getCommandContext() {
    return commandContext;
  }

  public String getMessageName() {
    return messageName;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public Map<String, Object> getCorrelationProcessInstanceVariables() {
    return correlationProcessInstanceVariables;
  }

  public Map<String, Object> getCorrelationLocalVariables() {
    return correlationLocalVariables;
  }

  public Map<String, Object> getPayloadProcessInstanceVariables() {
    return payloadProcessInstanceVariables;
  }

  public boolean isExclusiveCorrelation() {
    return isExclusiveCorrelation;
  }

  public String getTenantId() {
    return tenantId;
  }

  public boolean isTenantIdSet() {
    return isTenantIdSet;
  }

}
