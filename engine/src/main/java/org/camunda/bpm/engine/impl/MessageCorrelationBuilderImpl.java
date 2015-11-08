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

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.cmd.CorrelateAllMessageCmd;
import org.camunda.bpm.engine.impl.cmd.CorrelateMessageCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

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

  public MessageCorrelationBuilder processInstanceId(String id) {
    this.processInstanceId = id;
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
    CorrelateMessageCmd command = new CorrelateMessageCmd(this);
    if(commandExecutor != null) {
      commandExecutor.execute(command);
    } else {
      command.execute(commandContext);
    }
  }


  public void correlateExclusively() {
    isExclusiveCorrelation = true;
    correlate();
  }

  public void correlateAll() {
    CorrelateAllMessageCmd command = new CorrelateAllMessageCmd(this);
    if(commandExecutor != null) {
      commandExecutor.execute(command);
    } else {
      command.execute(commandContext);
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
