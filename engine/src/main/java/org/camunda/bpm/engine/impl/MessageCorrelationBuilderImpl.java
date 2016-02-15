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

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cmd.CorrelateAllMessageCmd;
import org.camunda.bpm.engine.impl.cmd.CorrelateMessageCmd;
import org.camunda.bpm.engine.impl.cmd.CorrelateStartMessageCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;

/**
 * @author Daniel Meyer
 *
 */
public class MessageCorrelationBuilderImpl implements MessageCorrelationBuilder {

  protected CommandExecutor commandExecutor;
  protected CommandContext commandContext;

  protected boolean isExclusiveCorrelation = false;

  protected String messageName;
  protected String businessKey;
  protected String processInstanceId;
  protected String processDefinitionId;

  protected Map<String, Object> correlationProcessInstanceVariables;
  protected Map<String, Object> payloadProcessInstanceVariables;

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
    if(correlationProcessInstanceVariables == null) {
      correlationProcessInstanceVariables = new HashMap<String, Object>();
    }
    correlationProcessInstanceVariables.put(variableName, variableValue);
    return this;
  }

  public MessageCorrelationBuilder processInstanceVariablesEqual(Map<String, Object> variables) {
    ensureNotNull("variables", variables);
    if(correlationProcessInstanceVariables == null) {
      correlationProcessInstanceVariables = new HashMap<String, Object>();
    }
    correlationProcessInstanceVariables.putAll(variables);
    return this;
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

  public void correlate() {
    ensureProcessDefinitionIdNotSet();

    execute(new CorrelateMessageCmd(this));
  }

  public void correlateExclusively() {
    isExclusiveCorrelation = true;

    correlate();
  }

  public void correlateAll() {
    ensureProcessDefinitionIdNotSet();

    execute(new CorrelateAllMessageCmd(this));
  }

  public ProcessInstance correlateStartMessage() {
    return execute(new CorrelateStartMessageCmd(this));
  }

  protected void ensureProcessDefinitionIdNotSet() {
    if(processDefinitionId != null) {
      throw new ProcessEngineException("It is not supported to specify a process definition id except for correlate a message start event.");
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

  public Map<String, Object> getPayloadProcessInstanceVariables() {
    return payloadProcessInstanceVariables;
  }

  public boolean isExclusiveCorrelation() {
    return isExclusiveCorrelation;
  }

}
