/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import org.camunda.bpm.engine.batch.BatchStatistics;

public class MockBatchStatisticsBuilder {

  protected String id;
  protected String type;
  protected int size;
  protected int jobsCreated;
  protected int batchJobsPerSeed;
  protected int invocationsPerBatchJob;
  protected String seedJobDefinitionId;
  protected String monitorJobDefinitionId;
  protected String batchJobDefinitionId;
  protected String tenantId;
  protected int remainingJobs;
  protected int completedJobs;
  protected int failedJobs;
  protected boolean suspended;

  public MockBatchStatisticsBuilder id(String id) {
    this.id = id;
    return this;
  }

  public MockBatchStatisticsBuilder type(String type) {
    this.type = type;
    return this;
  }

  public MockBatchStatisticsBuilder size(int size) {
    this.size = size;
    return this;
  }

  public MockBatchStatisticsBuilder jobsCreated(int jobsCreated) {
    this.jobsCreated = jobsCreated;
    return this;
  }

  public MockBatchStatisticsBuilder batchJobsPerSeed(int batchJobsPerSeed) {
    this.batchJobsPerSeed = batchJobsPerSeed;
    return this;
  }

  public MockBatchStatisticsBuilder invocationsPerBatchJob(int invocationsPerBatchJob) {
    this.invocationsPerBatchJob = invocationsPerBatchJob;
    return this;
  }

  public MockBatchStatisticsBuilder seedJobDefinitionId(String seedJobDefinitionId) {
    this.seedJobDefinitionId = seedJobDefinitionId;
    return this;
  }

  public MockBatchStatisticsBuilder monitorJobDefinitionId(String monitorJobDefinitionId) {
    this.monitorJobDefinitionId = monitorJobDefinitionId;
    return this;
  }

  public MockBatchStatisticsBuilder batchJobDefinitionId(String batchJobDefinitionId) {
    this.batchJobDefinitionId = batchJobDefinitionId;
    return this;
  }

  public MockBatchStatisticsBuilder tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  public MockBatchStatisticsBuilder remainingJobs(int remainingJobs) {
    this.remainingJobs = remainingJobs;
    return this;
  }

  public MockBatchStatisticsBuilder completedJobs(int completedJobs) {
    this.completedJobs = completedJobs;
    return this;
  }

  public MockBatchStatisticsBuilder failedJobs(int failedJobs) {
    this.failedJobs = failedJobs;
    return this;
  }

  public MockBatchStatisticsBuilder suspended() {
    this.suspended = true;
    return this;
  }

  public BatchStatistics build() {
    BatchStatistics batchStatistics = mock(BatchStatistics.class);
    when(batchStatistics.getId()).thenReturn(id);
    when(batchStatistics.getType()).thenReturn(type);
    when(batchStatistics.getTotalJobs()).thenReturn(size);
    when(batchStatistics.getJobsCreated()).thenReturn(jobsCreated);
    when(batchStatistics.getBatchJobsPerSeed()).thenReturn(batchJobsPerSeed);
    when(batchStatistics.getInvocationsPerBatchJob()).thenReturn(invocationsPerBatchJob);
    when(batchStatistics.getSeedJobDefinitionId()).thenReturn(seedJobDefinitionId);
    when(batchStatistics.getMonitorJobDefinitionId()).thenReturn(monitorJobDefinitionId);
    when(batchStatistics.getBatchJobDefinitionId()).thenReturn(batchJobDefinitionId);
    when(batchStatistics.getTenantId()).thenReturn(tenantId);
    when(batchStatistics.getRemainingJobs()).thenReturn(remainingJobs);
    when(batchStatistics.getCompletedJobs()).thenReturn(completedJobs);
    when(batchStatistics.getFailedJobs()).thenReturn(failedJobs);
    when(batchStatistics.isSuspended()).thenReturn(suspended);
    return batchStatistics;
  }
}
