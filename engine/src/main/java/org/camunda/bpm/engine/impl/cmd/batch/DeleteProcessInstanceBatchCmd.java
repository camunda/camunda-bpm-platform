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

package org.camunda.bpm.engine.impl.cmd.batch;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.ProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobHandler;
import org.camunda.bpm.engine.impl.batch.deletion.DeleteProcessInstanceBatchConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;

/**
 * @author Askar Akhmerov
 */
public class DeleteProcessInstanceBatchCmd extends AbstractIDBasedBatchCmd<Batch> {
  protected final String deleteReason;
  protected List<String> processInstanceIds;
  protected ProcessInstanceQuery processInstanceQuery;
  protected boolean skipCustomListeners;
  protected boolean skipSubprocesses;

  public DeleteProcessInstanceBatchCmd(List<String> processInstances, ProcessInstanceQuery processInstanceQuery, String deleteReason, boolean skipCustomListeners, boolean skipSubprocesses) {
    super();
    this.processInstanceIds = processInstances;
    this.processInstanceQuery = processInstanceQuery;
    this.deleteReason = deleteReason;
    this.skipCustomListeners = skipCustomListeners;
    this.skipSubprocesses = skipSubprocesses;
  }

  protected List<String> collectProcessInstanceIds() {

    Set<String> collectedProcessInstanceIds = new HashSet<String>();

    List<String> processInstanceIds = this.getProcessInstanceIds();
    if (processInstanceIds != null) {
      collectedProcessInstanceIds.addAll(processInstanceIds);
    }

    final ProcessInstanceQueryImpl processInstanceQuery = (ProcessInstanceQueryImpl) this.processInstanceQuery;
    if (processInstanceQuery != null) {
      collectedProcessInstanceIds.addAll(processInstanceQuery.listIds());
    }

    return new ArrayList<String>(collectedProcessInstanceIds);
  }

  public List<String> getProcessInstanceIds() {
    return processInstanceIds;
  }

  @Override
  public Batch execute(CommandContext commandContext) {
    List<String> processInstanceIds = collectProcessInstanceIds();

    ensureNotEmpty(BadUserRequestException.class, "processInstanceIds", processInstanceIds);
    checkAuthorizations(commandContext);
    writeUserOperationLog(commandContext,
        deleteReason,
        processInstanceIds.size(),
        true);

    BatchEntity batch = createBatch(commandContext, processInstanceIds);

    batch.createSeedJobDefinition();
    batch.createMonitorJobDefinition();
    batch.createBatchJobDefinition();

    batch.fireHistoricStartEvent();

    batch.createSeedJob();

    return batch;
  }

  protected BatchConfiguration getAbstractIdsBatchConfiguration(List<String> processInstanceIds) {
    return new DeleteProcessInstanceBatchConfiguration(processInstanceIds, deleteReason, skipCustomListeners, skipSubprocesses);
  }

  protected BatchJobHandler<DeleteProcessInstanceBatchConfiguration> getBatchJobHandler(ProcessEngineConfigurationImpl processEngineConfiguration) {
    return (BatchJobHandler<DeleteProcessInstanceBatchConfiguration>) processEngineConfiguration.getBatchHandlers().get(Batch.TYPE_PROCESS_INSTANCE_DELETION);
  }

  protected void writeUserOperationLog(CommandContext commandContext,
                                       String deleteReason,
                                       int numInstances,
                                       boolean async) {

    List<PropertyChange> propertyChanges = new ArrayList<PropertyChange>();
    propertyChanges.add(new PropertyChange("nrOfInstances",
        null,
        numInstances));
    propertyChanges.add(new PropertyChange("async", null, async));
    propertyChanges.add(new PropertyChange("deleteReason", null, deleteReason));

    commandContext.getOperationLogManager()
        .logProcessInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_DELETE,
            null,
            null,
            null,
            propertyChanges);
  }

}
