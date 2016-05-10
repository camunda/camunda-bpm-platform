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

package org.camunda.bpm.engine.impl.management;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureOnlyOneNotNull;

import java.util.Date;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmd.ActivateJobDefinitionCmd;
import org.camunda.bpm.engine.impl.cmd.CommandLogger;
import org.camunda.bpm.engine.impl.cmd.SuspendJobDefinitionCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.management.UpdateJobDefinitionSuspensionStateBuilder;
import org.camunda.bpm.engine.management.UpdateJobDefinitionSuspensionStateSelectBuilder;
import org.camunda.bpm.engine.management.UpdateJobDefinitionSuspensionStateTenantBuilder;

public class UpdateJobDefinitionSuspensionStateBuilderImpl
    implements UpdateJobDefinitionSuspensionStateBuilder, UpdateJobDefinitionSuspensionStateSelectBuilder, UpdateJobDefinitionSuspensionStateTenantBuilder {

  private final static CommandLogger LOG = ProcessEngineLogger.CMD_LOGGER;

  protected final CommandExecutor commandExecutor;

  protected String jobDefinitionId;

  protected String processDefinitionKey;
  protected String processDefinitionId;

  protected String processDefinitionTenantId;
  protected boolean isProcessDefinitionTenantIdSet = false;

  protected boolean includeJobs = false;
  protected Date executionDate;

  public UpdateJobDefinitionSuspensionStateBuilderImpl(CommandExecutor commandExecutor) {
    this.commandExecutor = commandExecutor;
  }

  /**
   * Creates a builder without CommandExecutor which can not be used to update
   * the suspension state via {@link #activate()} or {@link #suspend()}. Can only be
   * used in combination with your own command.
   */
  public UpdateJobDefinitionSuspensionStateBuilderImpl() {
    this(null);
  }

  @Override
  public UpdateJobDefinitionSuspensionStateBuilderImpl byJobDefinitionId(String jobDefinitionId) {
    ensureNotNull("jobDefinitionId", jobDefinitionId);
    this.jobDefinitionId = jobDefinitionId;
    return this;
  }

  @Override
  public UpdateJobDefinitionSuspensionStateBuilderImpl byProcessDefinitionId(String processDefinitionId) {
    ensureNotNull("processDefinitionId", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    return this;
  }

  @Override
  public UpdateJobDefinitionSuspensionStateBuilderImpl byProcessDefinitionKey(String processDefinitionKey) {
    ensureNotNull("processDefinitionKey", processDefinitionKey);
    this.processDefinitionKey = processDefinitionKey;
    return this;
  }

  @Override
  public UpdateJobDefinitionSuspensionStateBuilderImpl processDefinitionWithoutTenantId() {
    this.processDefinitionTenantId = null;
    this.isProcessDefinitionTenantIdSet = true;
    return this;
  }

  @Override
  public UpdateJobDefinitionSuspensionStateBuilderImpl processDefinitionTenantId(String tenantId) {
    ensureNotNull("tenantId", tenantId);

    this.processDefinitionTenantId = tenantId;
    this.isProcessDefinitionTenantIdSet = true;
    return this;
  }

  @Override
  public UpdateJobDefinitionSuspensionStateBuilderImpl includeJobs(boolean includeJobs) {
    this.includeJobs = includeJobs;
    return this;
  }

  @Override
  public UpdateJobDefinitionSuspensionStateBuilderImpl executionDate(Date executionDate) {
    this.executionDate = executionDate;
    return this;
  }


  @Override
  public void activate() {
    validateParameters();

    ActivateJobDefinitionCmd command = new ActivateJobDefinitionCmd(this);
    commandExecutor.execute(command);
  }

  @Override
  public void suspend() {
    validateParameters();

    SuspendJobDefinitionCmd command = new SuspendJobDefinitionCmd(this);
    commandExecutor.execute(command);
  }

  protected void validateParameters() {
    ensureOnlyOneNotNull("Need to specify either a job definition id, a process definition id or a process definition key.",
        jobDefinitionId, processDefinitionId, processDefinitionKey);

    if (isProcessDefinitionTenantIdSet & (jobDefinitionId != null || processDefinitionId != null)) {
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

  public String getJobDefinitionId() {
    return jobDefinitionId;
  }

  public boolean isIncludeJobs() {
    return includeJobs;
  }

  public Date getExecutionDate() {
    return executionDate;
  }

}
