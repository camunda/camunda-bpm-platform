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
package org.camunda.bpm.engine.impl.management;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureOnlyOneNotNull;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.ActivateJobCmd;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.cmd.SuspendJobCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.management.UpdateJobSuspensionStateBuilder;
import org.camunda.bpm.engine.management.UpdateJobSuspensionStateSelectBuilder;
import org.camunda.bpm.engine.management.UpdateJobSuspensionStateTenantBuilder;

public class UpdateJobSuspensionStateBuilderImpl
    implements UpdateJobSuspensionStateBuilder, UpdateJobSuspensionStateSelectBuilder, UpdateJobSuspensionStateTenantBuilder {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected final CommandExecutor commandExecutor;

  protected String jobId;
  protected String jobDefinitionId;

  protected String processInstanceId;

  protected String processDefinitionKey;
  protected String processDefinitionId;

  protected String processDefinitionTenantId;
  protected boolean isProcessDefinitionTenantIdSet = false;

  public UpdateJobSuspensionStateBuilderImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  /**
   * Creates a builder without CommandExecutor which can not be used to update
   * the suspension state via {@link #activate()} or {@link #suspend()}. Can be
   * used in combination with your own command.
   */
  public UpdateJobSuspensionStateBuilderImpl() {
    this(null);
  }

  @Override
  public UpdateJobSuspensionStateBuilderImpl byJobId(String jobId) {
    ensureNotNull("jobId", jobId);
    this.jobId = jobId;
    return this;
  }

  @Override
  public UpdateJobSuspensionStateBuilderImpl byJobDefinitionId(String jobDefinitionId) {
    ensureNotNull("jobDefinitionId", jobDefinitionId);
    this.jobDefinitionId = jobDefinitionId;
    return this;
  }

  @Override
  public UpdateJobSuspensionStateBuilderImpl byProcessInstanceId(String processInstanceId) {
    ensureNotNull("processInstanceId", processInstanceId);
    this.processInstanceId = processInstanceId;
    return this;
  }

  @Override
  public UpdateJobSuspensionStateBuilderImpl byProcessDefinitionId(String processDefinitionId) {
    ensureNotNull("processDefinitionId", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  @Override
  public UpdateJobSuspensionStateBuilderImpl byProcessDefinitionKey(String processDefinitionKey) {
    ensureNotNull("processDefinitionKey", processDefinitionKey);
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  @Override
  public UpdateJobSuspensionStateBuilderImpl processDefinitionWithoutTenantId() {
    this.processDefinitionTenantId = null;
    this.isProcessDefinitionTenantIdSet = true;
    return this;
  }

  @Override
  public UpdateJobSuspensionStateBuilderImpl processDefinitionTenantId(String tenantId) {
    ensureNotNull("tenantId", tenantId);

    this.processDefinitionTenantId = tenantId;
    this.isProcessDefinitionTenantIdSet = true;
    return this;
  }

  @Override
  public void activate() {
    validateParameters();

    ActivateJobCmd command = new ActivateJobCmd(this);
    commandExecutor.execute(command);
  }

  @Override
  public void suspend() {
    validateParameters();

    SuspendJobCmd command = new SuspendJobCmd(this);
    commandExecutor.execute(command);
  }

  protected void validateParameters() {
    ensureOnlyOneNotNull("Need to specify either a job id, a job definition id, a process instance id, a process definition id or a process definition key.", jobId,
        jobDefinitionId, processInstanceId, processDefinitionId, processDefinitionKey);

    if (isProcessDefinitionTenantIdSet && (jobId != null || jobDefinitionId != null || processInstanceId != null || processDefinitionId != null)) {
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

  public String getJobId() {
    return jobId;
  }

  public String getJobDefinitionId() {
    return jobDefinitionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

}
