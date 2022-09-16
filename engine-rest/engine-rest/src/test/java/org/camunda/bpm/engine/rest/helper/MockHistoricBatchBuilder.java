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

import java.util.Date;

import org.camunda.bpm.engine.batch.history.HistoricBatch;

public class MockHistoricBatchBuilder {

  protected String id;
  protected String type;
  protected int totalJobs;
  protected int batchJobsPerSeed;
  protected int invocationsPerBatchJob;
  protected String seedJobDefinitionId;
  protected String monitorJobDefinitionId;
  protected String batchJobDefinitionId;
  protected String tenantId;
  protected String createUserId;
  protected Date startTime;
  protected Date executionStartTime;
  protected Date endTime;
  protected Date removalTime;

  public MockHistoricBatchBuilder id(String id) {
    this.id = id;
    return this;
  }

  public MockHistoricBatchBuilder type(String type) {
    this.type = type;
    return this;
  }

  public MockHistoricBatchBuilder totalJobs(int totalJobs) {
    this.totalJobs = totalJobs;
    return this;
  }

  public MockHistoricBatchBuilder batchJobsPerSeed(int batchJobsPerSeed) {
    this.batchJobsPerSeed = batchJobsPerSeed;
    return this;
  }

  public MockHistoricBatchBuilder invocationsPerBatchJob(int invocationsPerBatchJob) {
    this.invocationsPerBatchJob = invocationsPerBatchJob;
    return this;
  }

  public MockHistoricBatchBuilder seedJobDefinitionId(String seedJobDefinitionId) {
    this.seedJobDefinitionId = seedJobDefinitionId;
    return this;
  }

  public MockHistoricBatchBuilder monitorJobDefinitionId(String monitorJobDefinitionId) {
    this.monitorJobDefinitionId = monitorJobDefinitionId;
    return this;
  }

  public MockHistoricBatchBuilder batchJobDefinitionId(String batchJobDefinitionId) {
    this.batchJobDefinitionId = batchJobDefinitionId;
    return this;
  }

  public MockHistoricBatchBuilder tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  public MockHistoricBatchBuilder createUserId(String createUserId) {
    this.createUserId = createUserId;
    return this;
  }

  public MockHistoricBatchBuilder startTime(Date startTime) {
    this.startTime = startTime;
    return this;
  }

  public MockHistoricBatchBuilder executionStartTime(Date executionStartTime) {
    this.executionStartTime = executionStartTime;
    return this;
  }

  public MockHistoricBatchBuilder endTime(Date endTime) {
    this.endTime = endTime;
    return this;
  }

  public MockHistoricBatchBuilder removalTime(Date removalTime) {
    this.removalTime = removalTime;
    return this;
  }

  public HistoricBatch build() {
    HistoricBatch historicBatch = mock(HistoricBatch.class);
    when(historicBatch.getId()).thenReturn(id);
    when(historicBatch.getType()).thenReturn(type);
    when(historicBatch.getTotalJobs()).thenReturn(totalJobs);
    when(historicBatch.getBatchJobsPerSeed()).thenReturn(batchJobsPerSeed);
    when(historicBatch.getInvocationsPerBatchJob()).thenReturn(invocationsPerBatchJob);
    when(historicBatch.getSeedJobDefinitionId()).thenReturn(seedJobDefinitionId);
    when(historicBatch.getMonitorJobDefinitionId()).thenReturn(monitorJobDefinitionId);
    when(historicBatch.getBatchJobDefinitionId()).thenReturn(batchJobDefinitionId);
    when(historicBatch.getTenantId()).thenReturn(tenantId);
    when(historicBatch.getCreateUserId()).thenReturn(createUserId);
    when(historicBatch.getStartTime()).thenReturn(startTime);
    when(historicBatch.getExecutionStartTime()).thenReturn(executionStartTime);
    when(historicBatch.getEndTime()).thenReturn(endTime);
    when(historicBatch.getRemovalTime()).thenReturn(removalTime);
    return historicBatch;
  }

}
