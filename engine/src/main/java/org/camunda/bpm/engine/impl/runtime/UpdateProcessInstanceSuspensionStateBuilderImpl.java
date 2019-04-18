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
package org.camunda.bpm.engine.impl.runtime;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureOnlyOneNotNull;


import java.util.List;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.UpdateProcessInstancesSuspensionStateBuilderImpl;
import org.camunda.bpm.engine.impl.cmd.ActivateProcessInstanceCmd;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.cmd.SuspendProcessInstanceCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.UpdateProcessInstanceSuspensionStateBuilder;
import org.camunda.bpm.engine.runtime.UpdateProcessInstanceSuspensionStateSelectBuilder;
import org.camunda.bpm.engine.runtime.UpdateProcessInstanceSuspensionStateTenantBuilder;
import org.camunda.bpm.engine.runtime.UpdateProcessInstancesSuspensionStateBuilder;

public class UpdateProcessInstanceSuspensionStateBuilderImpl implements UpdateProcessInstanceSuspensionStateBuilder,
    UpdateProcessInstanceSuspensionStateSelectBuilder, UpdateProcessInstanceSuspensionStateTenantBuilder {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected final CommandExecutor commandExecutor;

  protected String processInstanceId;

  protected String processDefinitionKey;
  protected String processDefinitionId;

  protected String processDefinitionTenantId;
  protected boolean isProcessDefinitionTenantIdSet = false;

  public UpdateProcessInstanceSuspensionStateBuilderImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  /**
   * Creates a builder without CommandExecutor which can not be used to update
   * the suspension state via {@link #activate()} or {@link #suspend()}. Can be
   * used in combination with your own command.
   */
  public UpdateProcessInstanceSuspensionStateBuilderImpl() {
    this(null);
  }

  @Override
  public UpdateProcessInstancesSuspensionStateBuilder byProcessInstanceIds(List<String> processInstanceIds){
    return new UpdateProcessInstancesSuspensionStateBuilderImpl(commandExecutor).byProcessInstanceIds(processInstanceIds);
  }

  @Override
  public UpdateProcessInstancesSuspensionStateBuilder byProcessInstanceIds(String... processInstanceIds) {
    return new UpdateProcessInstancesSuspensionStateBuilderImpl(commandExecutor).byProcessInstanceIds(processInstanceIds);
  }

  @Override
  public UpdateProcessInstancesSuspensionStateBuilder byProcessInstanceQuery(ProcessInstanceQuery processInstanceQuery) {
    return new UpdateProcessInstancesSuspensionStateBuilderImpl(commandExecutor).byProcessInstanceQuery(processInstanceQuery);
  }

  @Override
  public UpdateProcessInstancesSuspensionStateBuilder byHistoricProcessInstanceQuery(HistoricProcessInstanceQuery historicProcessInstanceQuery) {
    return new UpdateProcessInstancesSuspensionStateBuilderImpl(commandExecutor).byHistoricProcessInstanceQuery(historicProcessInstanceQuery);
  }


  @Override
  public UpdateProcessInstanceSuspensionStateBuilderImpl byProcessInstanceId(String processInstanceId) {
    ensureNotNull("processInstanceId", processInstanceId);
    this.processInstanceId = processInstanceId;
    return this;
  }

  @Override
  public UpdateProcessInstanceSuspensionStateBuilderImpl byProcessDefinitionId(String processDefinitionId) {
    ensureNotNull("processDefinitionId", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  @Override
  public UpdateProcessInstanceSuspensionStateBuilderImpl byProcessDefinitionKey(String processDefinitionKey) {
    ensureNotNull("processDefinitionKey", processDefinitionKey);
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  @Override
  public UpdateProcessInstanceSuspensionStateBuilderImpl processDefinitionWithoutTenantId() {
    this.processDefinitionTenantId = null;
    this.isProcessDefinitionTenantIdSet = true;
    return this;
  }

  @Override
  public UpdateProcessInstanceSuspensionStateBuilderImpl processDefinitionTenantId(String tenantId) {
    ensureNotNull("tenantId", tenantId);

    this.processDefinitionTenantId = tenantId;
    this.isProcessDefinitionTenantIdSet = true;
    return this;
  }

  @Override
  public void activate() {
    validateParameters();

    ActivateProcessInstanceCmd command = new ActivateProcessInstanceCmd(this);
    commandExecutor.execute(command);
  }

  @Override
  public void suspend() {
    validateParameters();

    SuspendProcessInstanceCmd command = new SuspendProcessInstanceCmd(this);
    commandExecutor.execute(command);
  }

  protected void validateParameters() {
    ensureOnlyOneNotNull("Need to specify either a process instance id, a process definition id or a process definition key.", processInstanceId, processDefinitionId, processDefinitionKey);

    if (isProcessDefinitionTenantIdSet && (processInstanceId != null || processDefinitionId != null)) {
      throw LOG.exceptionUpdateSuspensionStateForTenantOnlyByProcessDefinitionKey();
    }

    ensureNotNull("commandExecutor", commandExecutor);
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessDefinitionTenantId() {
    return processDefinitionTenantId;
  }

  public boolean isProcessDefinitionTenantIdSet() {
    return isProcessDefinitionTenantIdSet;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

}
