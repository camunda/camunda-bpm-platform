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

import java.util.Map;

import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.cmd.SignalEventReceivedCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.SignalEventReceivedBuilder;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;

public class SignalEventReceivedBuilderImpl implements SignalEventReceivedBuilder {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected final CommandExecutor commandExecutor;
  protected final String signalName;

  protected String executionId = null;

  protected String tenantId = null;
  protected boolean isTenantIdSet = false;

  protected VariableMap variables = null;

  public SignalEventReceivedBuilderImpl(CommandExecutor commandExecutor, String signalName) {
    this.commandExecutor = commandExecutor;
    this.signalName = signalName;
  }

  @Override
  public SignalEventReceivedBuilder setVariables(Map<String, Object> variables) {
    if (variables != null) {

      if (this.variables == null) {
        this.variables = new VariableMapImpl();
      }
      this.variables.putAll(variables);
    }
    return this;
  }

  @Override
  public SignalEventReceivedBuilder executionId(String executionId) {
    ensureNotNull("executionId", executionId);
    this.executionId = executionId;
    return this;
  }

  @Override
  public SignalEventReceivedBuilder tenantId(String tenantId) {
    ensureNotNull(
        "The tenant-id cannot be null. Use 'withoutTenantId()' if you want to send the signal to a process definition or an execution which has no tenant-id.",
        "tenantId", tenantId);

    this.tenantId = tenantId;
    isTenantIdSet = true;

    return this;
  }

  @Override
  public SignalEventReceivedBuilder withoutTenantId() {
    // tenant-id is null
    isTenantIdSet = true;
    return this;
  }

  @Override
  public void send() {
    if (executionId != null && isTenantIdSet) {
      throw LOG.exceptionDeliverSignalToSingleExecutionWithTenantId();
    }

    SignalEventReceivedCmd command = new SignalEventReceivedCmd(this);
    commandExecutor.execute(command);
  }

  public String getSignalName() {
    return signalName;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public boolean isTenantIdSet() {
    return isTenantIdSet;
  }

  public VariableMap getVariables() {
    return variables;
  }

}
