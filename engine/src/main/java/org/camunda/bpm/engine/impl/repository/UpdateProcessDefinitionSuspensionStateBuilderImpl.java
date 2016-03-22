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

package org.camunda.bpm.engine.impl.repository;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Date;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.ActivateProcessDefinitionCmd;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.cmd.SuspendProcessDefinitionCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.repository.UpdateProcessDefinitionSuspensionStateBuilder;

public class UpdateProcessDefinitionSuspensionStateBuilderImpl implements UpdateProcessDefinitionSuspensionStateBuilder {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected final CommandExecutor commandExecutor;

  protected String processDefinitionKey;
  protected String processDefinitionId;

  protected boolean includeProcessInstances = false;
  protected Date executionDate;

  protected String processDefinitionTenantId;
  protected boolean isTenantIdSet = false;

  private UpdateProcessDefinitionSuspensionStateBuilderImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  @Override
  public UpdateProcessDefinitionSuspensionStateBuilderImpl includeProcessInstances(boolean includeProcessInstance) {
    this.includeProcessInstances = includeProcessInstance;
    return this;
  }

  @Override
  public UpdateProcessDefinitionSuspensionStateBuilderImpl executionDate(Date date) {
    this.executionDate = date;
    return this;
  }

  @Override
  public UpdateProcessDefinitionSuspensionStateBuilderImpl processDefinitionWithoutTenantId() {
    this.processDefinitionTenantId = null;
    this.isTenantIdSet = true;
    return this;
  }

  @Override
  public UpdateProcessDefinitionSuspensionStateBuilderImpl processDefinitionTenantId(String tenantId) {
    ensureNotNull("tenantId", tenantId);

    this.processDefinitionTenantId = tenantId;
    this.isTenantIdSet = true;
    return this;
  }

  @Override
  public void activate() {
    ensureEitherProcessDefinitionIdOrTenantId();
    ensureNotNull("commandExecutor", commandExecutor);

    ActivateProcessDefinitionCmd command = new ActivateProcessDefinitionCmd(this);
    commandExecutor.execute(command);
  }

  @Override
  public void suspend() {
    ensureEitherProcessDefinitionIdOrTenantId();
    ensureNotNull("commandExecutor", commandExecutor);

    SuspendProcessDefinitionCmd command = new SuspendProcessDefinitionCmd(this);
    commandExecutor.execute(command);
  }

  protected void ensureEitherProcessDefinitionIdOrTenantId() {
    if(processDefinitionId != null && isTenantIdSet) {
      throw LOG.exceptionUpdateSuspensionStateForTenantOnlyByProcessDefinitionKey();
    }
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public boolean isIncludeProcessInstances() {
    return includeProcessInstances;
  }

  public Date getExecutionDate() {
    return executionDate;
  }

  public String getProcessDefinitionTenantId() {
    return processDefinitionTenantId;
  }

  public boolean isTenantIdSet() {
    return isTenantIdSet;
  }

  public static UpdateProcessDefinitionSuspensionStateBuilderImpl byId(CommandExecutor commandExecutor, String processDefinitionId) {
    UpdateProcessDefinitionSuspensionStateBuilderImpl builder = new UpdateProcessDefinitionSuspensionStateBuilderImpl(commandExecutor);
    builder.processDefinitionId = processDefinitionId;
    return builder;
  }

  public static UpdateProcessDefinitionSuspensionStateBuilderImpl byKey(CommandExecutor commandExecutor, String processDefinitionKey) {
    UpdateProcessDefinitionSuspensionStateBuilderImpl builder = new UpdateProcessDefinitionSuspensionStateBuilderImpl(commandExecutor);
    builder.processDefinitionKey = processDefinitionKey;
    return builder;
  }

  /**
   * Creates a builder without CommandExecutor which can not be used to update
   * the suspension state via {@link #activate()} or {@link #suspend()}. Can be
   * used in combination with your own command.
   */
  public static UpdateProcessDefinitionSuspensionStateBuilderImpl byId(String processDefinitionId) {
    return byId(null, processDefinitionId);
  }

  /**
   * Creates a builder without CommandExecutor which can not be used to update
   * the suspension state via {@link #activate()} or {@link #suspend()}. Can be
   * used in combination with your own command.
   */
  public static UpdateProcessDefinitionSuspensionStateBuilderImpl byKey(String processDefinitionKey) {
    return byKey(null, processDefinitionKey);
  }

}
