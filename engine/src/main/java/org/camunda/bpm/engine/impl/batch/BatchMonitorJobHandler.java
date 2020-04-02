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

import org.camunda.bpm.engine.impl.batch.BatchMonitorJobHandler.BatchMonitorJobConfiguration;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandlerConfiguration;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;

/**
 * Job handler for batch monitor jobs. The batch monitor job
 * polls for the completion of the batch.
 */
public class BatchMonitorJobHandler implements JobHandler<BatchMonitorJobConfiguration> {

  public static final String TYPE = "batch-monitor-job";

  public String getType() {
    return TYPE;
  }

  public void execute(BatchMonitorJobConfiguration configuration, ExecutionEntity execution, CommandContext commandContext, String tenantId) {

    String batchId = configuration.getBatchId();
    BatchEntity batch = commandContext.getBatchManager().findBatchById(configuration.getBatchId());
    ensureNotNull("Batch with id '" + batchId + "' cannot be found", "batch", batch);

    boolean completed = batch.isCompleted();

    if (!completed) {
      batch.createMonitorJob(true);
    }
    else {
      batch.delete(false, false);
    }
  }

  @Override
  public BatchMonitorJobConfiguration newConfiguration(String canonicalString) {
    return new BatchMonitorJobConfiguration(canonicalString);
  }

  public static class BatchMonitorJobConfiguration implements JobHandlerConfiguration {
    protected String batchId;

    public BatchMonitorJobConfiguration(String batchId) {
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

  public void onDelete(BatchMonitorJobConfiguration configuration, JobEntity jobEntity) {
    // do nothing
  }

}
