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
import org.camunda.bpm.engine.impl.batch.BatchConfiguration;
import org.camunda.bpm.engine.impl.batch.BatchEntity;
import org.camunda.bpm.engine.impl.batch.BatchJobHandler;
import org.camunda.bpm.engine.impl.batch.job.SetJobRetriesBatchConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.batch.AbstractIDBasedBatchCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;

/**
 * @author Askar Akhmerov
 */
public class SetJobsRetriesBatchCmd extends AbstractIDBasedBatchCmd<Batch> {
  protected final List<String> jobIds;
  protected final int retries;
  protected final JobQuery jobQuery;

  public SetJobsRetriesBatchCmd(List<String> jobIds, JobQuery jobQuery, int retries) {
    this.jobQuery = jobQuery;
    this.jobIds = jobIds;
    this.retries = retries;
  }

  @Override
  public Batch execute(CommandContext commandContext) {
    List<String> jobIds = collectJobIds();

    ensureNotEmpty(BadUserRequestException.class, "jobIds", jobIds);
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

  protected List<String> collectJobIds() {
    Set<String> collectedJobIds = new HashSet<String>();

    List<String> processInstanceIds = this.getJobIds();
    if (processInstanceIds != null) {
      collectedJobIds.addAll(processInstanceIds);
    }

    final JobQuery jobQuery = this.jobQuery;
    if (jobQuery != null) {
      for (Job job : jobQuery.list()) {
        collectedJobIds.add(job.getId());
      }
    }

    return new ArrayList<String>(collectedJobIds);
  }

  @Override
  protected SetJobRetriesBatchConfiguration getAbstractIdsBatchConfiguration(List<String> processInstanceIds) {
    return new SetJobRetriesBatchConfiguration(processInstanceIds, retries);
  }

  @Override
  protected BatchJobHandler<SetJobRetriesBatchConfiguration> getBatchJobHandler(ProcessEngineConfigurationImpl processEngineConfiguration) {
    return (BatchJobHandler<SetJobRetriesBatchConfiguration>) processEngineConfiguration.getBatchHandlers().get(Batch.TYPE_SET_JOB_RETRIES);
  }

  public List<String> getJobIds() {
    return jobIds;
  }

  public int getRetries() {
    return retries;
  }

  public JobQuery getJobQuery() {
    return jobQuery;
  }
}
