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

package org.camunda.bpm.engine.rest.dto.history.batch;

import java.util.Date;

import org.camunda.bpm.engine.batch.history.HistoricBatch;

public class HistoricBatchDto {

  protected String id;
  protected String type;
  protected int totalJobs;
  protected int batchJobsPerSeed;
  protected int invocationsPerBatchJob;
  protected String seedJobDefinitionId;
  protected String monitorJobDefinitionId;
  protected String batchJobDefinitionId;
  protected String tenantId;
  protected Date startTime;
  protected Date endTime;

  public String getId() {
    return id;
  }

  public String getType() {
    return type;
  }

  public int getTotalJobs() {
    return totalJobs;
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

  public String getTenantId() {
    return tenantId;
  }

  public Date getStartTime() {
    return startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public static HistoricBatchDto fromBatch(HistoricBatch historicBatch) {
    HistoricBatchDto dto = new HistoricBatchDto();
    dto.id = historicBatch.getId();
    dto.type = historicBatch.getType();
    dto.totalJobs = historicBatch.getTotalJobs();
    dto.batchJobsPerSeed = historicBatch.getBatchJobsPerSeed();
    dto.invocationsPerBatchJob = historicBatch.getInvocationsPerBatchJob();
    dto.seedJobDefinitionId = historicBatch.getSeedJobDefinitionId();
    dto.monitorJobDefinitionId = historicBatch.getMonitorJobDefinitionId();
    dto.batchJobDefinitionId = historicBatch.getBatchJobDefinitionId();
    dto.tenantId = historicBatch.getTenantId();
    dto.startTime = historicBatch.getStartTime();
    dto.endTime = historicBatch.getEndTime();
    return dto;
  }

}
