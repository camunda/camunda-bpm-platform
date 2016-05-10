/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.management.UpdateJobDefinitionSuspensionStateBuilderImpl;
import org.camunda.bpm.engine.impl.persistence.entity.BatchManager;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;

public abstract class AbstractSetBatchStateCmd implements Command<Void> {

  public static final String SUSPENSION_STATE_PROPERTY = "suspensionState";

  protected String batchId;

  public AbstractSetBatchStateCmd(String batchId) {
    this.batchId = batchId;
  }

  public Void execute(CommandContext commandContext) {
    ensureNotNull(BadUserRequestException.class, "Batch id must not be null", "batch id", batchId);

    BatchManager batchManager = commandContext.getBatchManager();

    BatchEntity batch = batchManager.findBatchById(batchId);
    ensureNotNull(BadUserRequestException.class, "Batch for id '" + batchId + "' cannot be found", "batch", batch);

    checkAccess(commandContext, batch);

    setJobDefinitionState(commandContext, batch.getSeedJobDefinitionId());
    setJobDefinitionState(commandContext, batch.getMonitorJobDefinitionId());
    setJobDefinitionState(commandContext, batch.getBatchJobDefinitionId());

    batchManager.updateBatchSuspensionStateById(batchId, getNewSuspensionState());

    logUserOperation(commandContext);

    return null;
  }

  protected abstract SuspensionState getNewSuspensionState();

  protected void checkAccess(CommandContext commandContext, BatchEntity batch) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checkAccess(checker, batch);
    }
  }

  protected abstract void checkAccess(CommandChecker checker, BatchEntity batch);

  protected void setJobDefinitionState(CommandContext commandContext, String jobDefinitionId) {
    createSetJobDefinitionStateCommand(jobDefinitionId).execute(commandContext);
  }

  protected AbstractSetJobDefinitionStateCmd createSetJobDefinitionStateCommand(String jobDefinitionId) {
    AbstractSetJobDefinitionStateCmd suspendJobDefinitionCmd = createSetJobDefinitionStateCommand(new UpdateJobDefinitionSuspensionStateBuilderImpl()
      .byJobDefinitionId(jobDefinitionId)
      .includeJobs(true)
    );
    suspendJobDefinitionCmd.disableLogUserOperation();
    return suspendJobDefinitionCmd;
  }

  protected abstract AbstractSetJobDefinitionStateCmd createSetJobDefinitionStateCommand(UpdateJobDefinitionSuspensionStateBuilderImpl builder);

  protected void logUserOperation(CommandContext commandContext) {
    PropertyChange propertyChange = new PropertyChange(SUSPENSION_STATE_PROPERTY, null, getNewSuspensionState().getName());
    commandContext.getOperationLogManager()
      .logBatchOperation(getUserOperationType(), batchId, propertyChange);
  }

  protected abstract String getUserOperationType();
}
