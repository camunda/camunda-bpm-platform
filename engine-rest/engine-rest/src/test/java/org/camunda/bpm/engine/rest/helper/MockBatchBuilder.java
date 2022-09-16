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
package org.camunda.bpm.engine.rest.helper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.batch.Batch;

import java.util.Date;

public class MockBatchBuilder {

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

  public MockBatchBuilder id(String id) {
    this.id = id;
    return this;
  }

  public MockBatchBuilder type(String type) {
    this.type = type;
    return this;
  }

  public MockBatchBuilder totalJobs(int totalJobs) {
    this.totalJobs = totalJobs;
    return this;
  }

  public MockBatchBuilder jobsCreated(int jobsCreated) {
    this.jobsCreated = jobsCreated;
    return this;
  }

  public MockBatchBuilder batchJobsPerSeed(int batchJobsPerSeed) {
    this.batchJobsPerSeed = batchJobsPerSeed;
    return this;
  }

  public MockBatchBuilder invocationsPerBatchJob(int invocationsPerBatchJob) {
    this.invocationsPerBatchJob = invocationsPerBatchJob;
    return this;
  }

  public MockBatchBuilder seedJobDefinitionId(String seedJobDefinitionId) {
    this.seedJobDefinitionId = seedJobDefinitionId;
    return this;
  }

  public MockBatchBuilder monitorJobDefinitionId(String monitorJobDefinitionId) {
    this.monitorJobDefinitionId = monitorJobDefinitionId;
    return this;
  }

  public MockBatchBuilder batchJobDefinitionId(String batchJobDefinitionId) {
    this.batchJobDefinitionId = batchJobDefinitionId;
    return this;
  }

  public MockBatchBuilder suspended() {
    this.suspended = true;
    return this;
  }

  public MockBatchBuilder tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  public MockBatchBuilder createUserId(String createUserId) {
    this.createUserId = createUserId;
    return this;
  }

  public MockBatchBuilder startTime(Date startTime) {
    this.startTime = startTime;
    return this;
  }

  public MockBatchBuilder executionStartTime(Date executionStartTime) {
    this.executionStartTime = executionStartTime;
    return this;
  }

  public Batch build() {
    Batch batch = mock(Batch.class);
    when(batch.getId()).thenReturn(id);
    when(batch.getType()).thenReturn(type);
    when(batch.getTotalJobs()).thenReturn(totalJobs);
    when(batch.getJobsCreated()).thenReturn(jobsCreated);
    when(batch.getBatchJobsPerSeed()).thenReturn(batchJobsPerSeed);
    when(batch.getInvocationsPerBatchJob()).thenReturn(invocationsPerBatchJob);
    when(batch.getSeedJobDefinitionId()).thenReturn(seedJobDefinitionId);
    when(batch.getMonitorJobDefinitionId()).thenReturn(monitorJobDefinitionId);
    when(batch.getBatchJobDefinitionId()).thenReturn(batchJobDefinitionId);
    when(batch.isSuspended()).thenReturn(suspended);
    when(batch.getTenantId()).thenReturn(tenantId);
    when(batch.getCreateUserId()).thenReturn(createUserId);
    when(batch.getStartTime()).thenReturn(startTime);
    when(batch.getExecutionStartTime()).thenReturn(executionStartTime);
    return batch;
  }

}
