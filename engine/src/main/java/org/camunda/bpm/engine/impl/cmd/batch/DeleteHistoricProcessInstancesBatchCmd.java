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
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryImpl;
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobHandler;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;

/**
 * @author Askar Akhmerov
 */
public class DeleteHistoricProcessInstancesBatchCmd extends AbstractIDBasedBatchCmd<Batch> {
  protected final String deleteReason;
  protected List<String> historicProcessInstanceIds;
  protected HistoricProcessInstanceQuery historicProcessInstanceQuery;

  public DeleteHistoricProcessInstancesBatchCmd(
      List<String> historicProcessInstanceIds,
      HistoricProcessInstanceQuery historicProcessInstanceQuery,
      String deleteReason) {
    super();
    this.historicProcessInstanceIds = historicProcessInstanceIds;
    this.historicProcessInstanceQuery = historicProcessInstanceQuery;
    this.deleteReason = deleteReason;
  }

  protected List<String> collectHistoricProcessInstanceIds() {

    Set<String> collectedProcessInstanceIds = new HashSet<String>();

    List<String> processInstanceIds = this.getHistoricProcessInstanceIds();
    if (processInstanceIds != null) {
      collectedProcessInstanceIds.addAll(processInstanceIds);
    }

    final HistoricProcessInstanceQueryImpl processInstanceQuery = (HistoricProcessInstanceQueryImpl) this.historicProcessInstanceQuery;
    if (processInstanceQuery != null) {
      for (HistoricProcessInstance hpi : processInstanceQuery.list()) {
        collectedProcessInstanceIds.add(hpi.getId());
      }
    }

    return new ArrayList<String>(collectedProcessInstanceIds);
  }

  public List<String> getHistoricProcessInstanceIds() {
    return historicProcessInstanceIds;
  }

  @Override
  public Batch execute(CommandContext commandContext) {
    List<String> processInstanceIds = collectHistoricProcessInstanceIds();

    ensureNotEmpty(BadUserRequestException.class, "historicProcessInstanceIds", processInstanceIds);
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

  protected void writeUserOperationLog(CommandContext commandContext,
                                       String deleteReason,
                                       int numInstances,
                                       boolean async) {

    List<PropertyChange> propertyChanges = new ArrayList<PropertyChange>();
    propertyChanges.add(new PropertyChange("nrOfInstances",
        null,
        numInstances));
    propertyChanges.add(new PropertyChange("async", null, async));
    propertyChanges.add(new PropertyChange("type", null, "history"));
    propertyChanges.add(new PropertyChange("deleteReason", null, deleteReason));

    commandContext.getOperationLogManager()
        .logProcessInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_DELETE,
            null,
            null,
            null,
            propertyChanges);
  }

  @Override
  protected BatchConfiguration getAbstractIdsBatchConfiguration(List<String> processInstanceIds) {
    return new BatchConfiguration(processInstanceIds);
  }

  @Override
  protected BatchJobHandler<BatchConfiguration> getBatchJobHandler(ProcessEngineConfigurationImpl processEngineConfiguration) {
    return (BatchJobHandler<BatchConfiguration>) processEngineConfiguration.getBatchHandlers().get(Batch.TYPE_HISTORIC_PROCESS_INSTANCE_DELETION);
  }
}
