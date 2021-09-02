/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureFalse;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.cmd.CorrelateAllMessageCmd;
import org.camunda.bpm.engine.impl.cmd.CorrelateMessageCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.runtime.MessageCorrelationResultWithVariables;
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
  protected VariableMap payloadProcessInstanceVariablesLocal;

  protected String tenantId = null;
  protected boolean isTenantIdSet = false;

  protected boolean startMessagesOnly = false;
  protected boolean executionsOnly = false;

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

  public MessageCorrelationBuilder setVariableLocal(String variableName, Object variableValue) {
    ensureNotNull("variableName", variableName);
    ensurePayloadProcessInstanceVariablesLocalInitialized();
    payloadProcessInstanceVariablesLocal.put(variableName, variableValue);
    return this;
  }

  public MessageCorrelationBuilder setVariables(Map<String, Object> variables) {
    if (variables != null) {
      ensurePayloadProcessInstanceVariablesInitialized();
      payloadProcessInstanceVariables.putAll(variables);
    }
    return this;
  }

  @Override
  public MessageCorrelationBuilder setVariablesLocal(Map<String, Object> variables) {
    if (variables != null) {
      ensurePayloadProcessInstanceVariablesLocalInitialized();
      payloadProcessInstanceVariablesLocal.putAll(variables);
    }
    return this;
  }

  protected void ensurePayloadProcessInstanceVariablesInitialized() {
    if (payloadProcessInstanceVariables == null) {
      payloadProcessInstanceVariables = new VariableMapImpl();
    }
  }

  protected void ensurePayloadProcessInstanceVariablesLocalInitialized() {
    if (payloadProcessInstanceVariablesLocal == null) {
      payloadProcessInstanceVariablesLocal = new VariableMapImpl();
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
  public MessageCorrelationBuilder startMessageOnly() {
    ensureFalse("Either startMessageOnly or executionsOnly can be set", executionsOnly);
    startMessagesOnly = true;
    return this;
  }

  public MessageCorrelationBuilder executionsOnly() {
    ensureFalse("Either startMessageOnly or executionsOnly can be set", startMessagesOnly);
    executionsOnly = true;
    return this;
  }

  @Override
  public void correlate() {
    correlateWithResult();
  }

  @Override
  public MessageCorrelationResult correlateWithResult() {
    if (startMessagesOnly) {
      ensureCorrelationVariablesNotSet();
      ensureProcessDefinitionAndTenantIdNotSet();
    } else {
      ensureProcessDefinitionIdNotSet();
      ensureProcessInstanceAndTenantIdNotSet();
    }
    return execute(new CorrelateMessageCmd(this, false, false, startMessagesOnly));
  }

  @Override
  public MessageCorrelationResultWithVariables correlateWithResultAndVariables(boolean deserializeValues) {
    if (startMessagesOnly) {
      ensureCorrelationVariablesNotSet();
      ensureProcessDefinitionAndTenantIdNotSet();
    } else {
      ensureProcessDefinitionIdNotSet();
      ensureProcessInstanceAndTenantIdNotSet();
    }
    return execute(new CorrelateMessageCmd(this, true, deserializeValues, startMessagesOnly));
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
    if (startMessagesOnly) {
      ensureCorrelationVariablesNotSet();
      ensureProcessDefinitionAndTenantIdNotSet();
      // only one result can be expected
      MessageCorrelationResult result = execute(new CorrelateMessageCmd(this, false, false, startMessagesOnly));
      return Arrays.asList(result);
    } else {
      ensureProcessDefinitionIdNotSet();
      ensureProcessInstanceAndTenantIdNotSet();
      return (List) execute(new CorrelateAllMessageCmd(this, false, false));
    }
  }

  @Override
  public List<MessageCorrelationResultWithVariables> correlateAllWithResultAndVariables(boolean deserializeValues) {
    if (startMessagesOnly) {
      ensureCorrelationVariablesNotSet();
      ensureProcessDefinitionAndTenantIdNotSet();
      // only one result can be expected
      MessageCorrelationResultWithVariables result = execute(new CorrelateMessageCmd(this, true, deserializeValues, startMessagesOnly));
      return Arrays.asList(result);
    } else {
      ensureProcessDefinitionIdNotSet();
      ensureProcessInstanceAndTenantIdNotSet();
      return (List) execute(new CorrelateAllMessageCmd(this, true, deserializeValues));
    }
  }

  public ProcessInstance correlateStartMessage() {
    startMessageOnly();
    MessageCorrelationResult result = correlateWithResult();
    return result.getProcessInstance();
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

  public VariableMap getPayloadProcessInstanceVariablesLocal() {
    return payloadProcessInstanceVariablesLocal;
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

  public boolean isExecutionsOnly() {
    return executionsOnly;
  }

}
