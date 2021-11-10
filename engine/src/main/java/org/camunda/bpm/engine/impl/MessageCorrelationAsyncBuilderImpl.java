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

import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.cmd.batch.CorrelateAllMessageBatchCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.MessageCorrelationAsyncBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;

public class MessageCorrelationAsyncBuilderImpl implements MessageCorrelationAsyncBuilder {

  protected CommandExecutor commandExecutor;

  protected String messageName;
  protected Map<String, Object> payloadProcessInstanceVariables;

  protected List<String> processInstanceIds;
  protected ProcessInstanceQuery processInstanceQuery;
  protected HistoricProcessInstanceQuery historicProcessInstanceQuery;

  public MessageCorrelationAsyncBuilderImpl(CommandExecutor commandExecutor, String messageName) {
    this(messageName);
    ensureNotNull("commandExecutor", commandExecutor);
    this.commandExecutor = commandExecutor;
  }

  private MessageCorrelationAsyncBuilderImpl(String messageName) {
    this.messageName = messageName;
  }

  public MessageCorrelationAsyncBuilder processInstanceIds(List<String> ids) {
    ensureNotNull("processInstanceIds", ids);
    this.processInstanceIds = ids;
    return this;
  }

  @Override
  public MessageCorrelationAsyncBuilder processInstanceQuery(ProcessInstanceQuery processInstanceQuery) {
    ensureNotNull("processInstanceQuery", processInstanceQuery);
    this.processInstanceQuery = processInstanceQuery;
    return this;
  }

  @Override
  public MessageCorrelationAsyncBuilder historicProcessInstanceQuery(HistoricProcessInstanceQuery historicProcessInstanceQuery) {
    ensureNotNull("historicProcessInstanceQuery", historicProcessInstanceQuery);
    this.historicProcessInstanceQuery = historicProcessInstanceQuery;
    return this;
  }

  public MessageCorrelationAsyncBuilder setVariable(String variableName, Object variableValue) {
    ensureNotNull("variableName", variableName);
    ensurePayloadProcessInstanceVariablesInitialized();
    payloadProcessInstanceVariables.put(variableName, variableValue);
    return this;
  }

  public MessageCorrelationAsyncBuilder setVariables(Map<String, Object> variables) {
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

  @Override
  public Batch correlateAllAsync() {
    return commandExecutor.execute(new CorrelateAllMessageBatchCmd(this));
  }

  // getters //////////////////////////////////

  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  public String getMessageName() {
    return messageName;
  }

  public List<String> getProcessInstanceIds() {
    return processInstanceIds;
  }

  public ProcessInstanceQuery getProcessInstanceQuery() {
    return processInstanceQuery;
  }

  public HistoricProcessInstanceQuery getHistoricProcessInstanceQuery() {
    return historicProcessInstanceQuery;
  }

  public Map<String, Object> getPayloadProcessInstanceVariables() {
    return payloadProcessInstanceVariables;
  }

}
