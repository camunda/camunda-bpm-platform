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
package org.camunda.bpm.engine.rest.dto.batch;

import org.camunda.bpm.engine.batch.BatchStatistics;

public class BatchStatisticsDto extends BatchDto {

  protected int remainingJobs;
  protected int completedJobs;
  protected int failedJobs;

  public int getRemainingJobs() {
    return remainingJobs;
  }

  public int getCompletedJobs() {
    return completedJobs;
  }

  public int getFailedJobs() {
    return failedJobs;
  }

  public static BatchStatisticsDto fromBatchStatistics(BatchStatistics batchStatistics) {
    BatchStatisticsDto dto = new BatchStatisticsDto();
    dto.id = batchStatistics.getId();
    dto.type = batchStatistics.getType();
    dto.totalJobs = batchStatistics.getTotalJobs();
    dto.jobsCreated = batchStatistics.getJobsCreated();
    dto.batchJobsPerSeed = batchStatistics.getBatchJobsPerSeed();
    dto.invocationsPerBatchJob = batchStatistics.getInvocationsPerBatchJob();
    dto.seedJobDefinitionId = batchStatistics.getSeedJobDefinitionId();
    dto.monitorJobDefinitionId = batchStatistics.getMonitorJobDefinitionId();
    dto.batchJobDefinitionId = batchStatistics.getBatchJobDefinitionId();
    dto.tenantId = batchStatistics.getTenantId();
    dto.createUserId = batchStatistics.getCreateUserId();
    dto.suspended = batchStatistics.isSuspended();
    dto.startTime = batchStatistics.getStartTime();
    dto.executionStartTime = batchStatistics.getExecutionStartTime();

    dto.remainingJobs = batchStatistics.getRemainingJobs();
    dto.completedJobs = batchStatistics.getCompletedJobs();
    dto.failedJobs = batchStatistics.getFailedJobs();
    return dto;
  }

}
