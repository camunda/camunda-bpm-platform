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
package org.camunda.bpm.engine.impl.batch;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import org.camunda.bpm.engine.impl.batch.BatchSeedJobHandler.BatchSeedJobConfiguration;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;

/**
 * The batch seed job handler is responsible to
 * create all jobs to be executed by the batch.
 *
 * If all jobs are created a seed monitor job is
 * created to oversee the completion of the batch
 * (see {@link BatchMonitorJobHandler}).
 */
public class BatchSeedJobHandler implements JobHandler<BatchSeedJobConfiguration> {

  public static final String TYPE = "batch-seed-job";

  public String getType() {
    return TYPE;
  }

  public void execute(BatchSeedJobConfiguration configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId) {

    String batchId = configuration.getBatchId();
    BatchEntity batch = commandContext.getBatchManager().findBatchById(batchId);
    ensureNotNull("Batch with id '" + batchId + "' cannot be found", "batch", batch);

    BatchJobHandler<?> batchJobHandler = commandContext
        .getProcessEngineConfiguration()
        .getBatchHandlers()
        .get(batch.getType());

    boolean done = batchJobHandler.createJobs(batch);

    if (!done) {
      batch.createSeedJob();
    }
    else {
      // create monitor job initially without due date to
      // enable rapid completion of simple batches
      batch.createMonitorJob(false);
    }
  }

  @Override
  public BatchSeedJobConfiguration newConfiguration(String canonicalString) {
    return new BatchSeedJobConfiguration(canonicalString);
  }

  public static class BatchSeedJobConfiguration implements JobHandlerConfiguration {
    protected String batchId;

    public BatchSeedJobConfiguration(String batchId) {
      this.batchId = batchId;
    }

    public String getBatchId() {
      return batchId;
    }

    @Override
    public String toCanonicalString() {
      return batchId;
    }
  }

  public void onDelete(BatchSeedJobConfiguration configuration, JobEntity jobEntity) {
    // do nothing
  }

}
