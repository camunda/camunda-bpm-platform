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

import org.camunda.bpm.engine.batch.BatchStatistics;

public class BatchStatisticsEntity extends BatchEntity implements BatchStatistics {

  protected int remainingJobs;
  protected int failedJobs;

  public int getRemainingJobs() {
    return remainingJobs + getJobsToCreate();
  }

  public void setRemainingJobs(int remainingJobs) {
    this.remainingJobs = remainingJobs;
  }

  public int getCompletedJobs() {
    return totalJobs - getRemainingJobs();
  }

  public int getFailedJobs() {
    return failedJobs;
  }

  public void setFailedJobs(int failedJobs) {
    this.failedJobs = failedJobs;
  }

  public int getJobsToCreate() {
    return totalJobs - jobsCreated;
  }

  public String toString() {
    return "BatchStatisticsEntity{" +
      "batchHandler=" + batchJobHandler +
      ", id='" + id + '\'' +
      ", type='" + type + '\'' +
      ", size=" + totalJobs +
      ", jobCreated=" + jobsCreated +
      ", remainingJobs=" + remainingJobs +
      ", failedJobs=" + failedJobs +
      ", batchJobsPerSeed=" + batchJobsPerSeed +
      ", invocationsPerBatchJob=" + invocationsPerBatchJob +
      ", seedJobDefinitionId='" + seedJobDefinitionId + '\'' +
      ", monitorJobDefinitionId='" + seedJobDefinitionId + '\'' +
      ", batchJobDefinitionId='" + batchJobDefinitionId + '\'' +
      ", configurationId='" + configuration.getByteArrayId() + '\'' +
      '}';
  }


}
