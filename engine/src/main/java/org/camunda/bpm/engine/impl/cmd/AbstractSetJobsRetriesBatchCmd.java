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

package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobHandler;
import org.camunda.bpm.engine.impl.batch.SetRetriesBatchConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.batch.AbstractIDBasedBatchCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

import java.util.ArrayList;
import java.util.List;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;

/**
 * @author Askar Akhmerov
 */
public abstract class AbstractSetJobsRetriesBatchCmd extends AbstractIDBasedBatchCmd<Batch> {
  protected int retries;

  @Override
  public Batch execute(CommandContext commandContext) {
    List<String> jobIds = collectJobIds(commandContext);

    ensureNotEmpty(BadUserRequestException.class, "jobIds", jobIds);
    EnsureUtil.ensureGreaterThanOrEqual("Retries count", retries, 0);
    checkAuthorizations(commandContext);
    writeUserOperationLog(commandContext,
        retries,
        jobIds.size(),
        true);

    BatchEntity batch = createBatch(commandContext, jobIds);

    batch.createSeedJobDefinition();
    batch.createMonitorJobDefinition();
    batch.createBatchJobDefinition();

    batch.fireHistoricStartEvent();

    batch.createSeedJob();

    return batch;
  }


  protected void writeUserOperationLog(CommandContext commandContext,
                                       int retries,
                                       int numInstances,
                                       boolean async) {

    List<PropertyChange> propertyChanges = new ArrayList<PropertyChange>();
    propertyChanges.add(new PropertyChange("nrOfInstances",
        null,
        numInstances));
    propertyChanges.add(new PropertyChange("async", null, async));
    propertyChanges.add(new PropertyChange("retries", null, retries));

    commandContext.getOperationLogManager()
        .logProcessInstanceOperation(UserOperationLogEntry.OPERATION_TYPE_SET_JOB_RETRIES,
            null,
            null,
            null,
            propertyChanges);
  }

  protected abstract List<String> collectJobIds(CommandContext commandContext);

  @Override
  protected SetRetriesBatchConfiguration getAbstractIdsBatchConfiguration(List<String> ids) {
    return new SetRetriesBatchConfiguration(ids, retries);
  }

  @Override
  protected BatchJobHandler<SetRetriesBatchConfiguration> getBatchJobHandler(ProcessEngineConfigurationImpl processEngineConfiguration) {
    return (BatchJobHandler<SetRetriesBatchConfiguration>) processEngineConfiguration.getBatchHandlers().get(Batch.TYPE_SET_JOB_RETRIES);
  }
}
