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

import org.camunda.bpm.engine.batch.Batch;

import java.util.Date;

public class BatchDto {

  protected String id;
  protected String type;
  protected int totalJobs;
  protected int jobsCreated;
  protected int batchJobsPerSeed;
  protected int invocationsPerBatchJob;
  protected String seedJobDefinitionId;
  protected String monitorJobDefinitionId;
  protected String batchJobDefinitionId;
  protected boolean suspended;
  protected String tenantId;
  protected String createUserId;
  protected Date startTime;
  protected Date executionStartTime;

  public String getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public int getTotalJobs() {
    return totalJobs;
  }

  public int getJobsCreated() {
    return jobsCreated;
  }

  public int getBatchJobsPerSeed() {
    return batchJobsPerSeed;
  }

  public int getInvocationsPerBatchJob() {
    return invocationsPerBatchJob;
  }

  public String getSeedJobDefinitionId() {
    return seedJobDefinitionId;
  }

  public String getMonitorJobDefinitionId() {
    return monitorJobDefinitionId;
  }

  public String getBatchJobDefinitionId() {
    return batchJobDefinitionId;
  }

  public boolean isSuspended() {
    return suspended;
  }

  public String getTenantId() {
    return tenantId;
  }

  public String getCreateUserId() {
    return createUserId;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(final Date startTime) {
    this.startTime = startTime;
  }

  public Date getExecutionStartTime() {
    return executionStartTime;
  }

  public void setExecutionStartTime(final Date executionStartTime) {
    this.executionStartTime = executionStartTime;
  }

  public static BatchDto fromBatch(Batch batch) {
    BatchDto dto = new BatchDto();
    dto.id = batch.getId();
    dto.type = batch.getType();
    dto.totalJobs = batch.getTotalJobs();
    dto.jobsCreated = batch.getJobsCreated();
    dto.batchJobsPerSeed = batch.getBatchJobsPerSeed();
    dto.invocationsPerBatchJob = batch.getInvocationsPerBatchJob();
    dto.seedJobDefinitionId = batch.getSeedJobDefinitionId();
    dto.monitorJobDefinitionId = batch.getMonitorJobDefinitionId();
    dto.batchJobDefinitionId = batch.getBatchJobDefinitionId();
    dto.suspended = batch.isSuspended();
    dto.tenantId = batch.getTenantId();
    dto.createUserId = batch.getCreateUserId();
    dto.startTime = batch.getStartTime();
    dto.executionStartTime = batch.getExecutionStartTime();
    return dto;
  }

}
