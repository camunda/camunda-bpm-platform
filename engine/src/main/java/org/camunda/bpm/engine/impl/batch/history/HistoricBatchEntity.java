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
package org.camunda.bpm.engine.impl.batch.history;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricIncidentManager;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricJobLogManager;

public class HistoricBatchEntity extends HistoryEvent implements HistoricBatch, DbEntity {

  private static final long serialVersionUID = 1L;

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
  protected Date endTime;
  protected Date executionStartTime;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getTotalJobs() {
    return totalJobs;
  }

  public void setTotalJobs(int totalJobs) {
    this.totalJobs = totalJobs;
  }

  public int getBatchJobsPerSeed() {
    return batchJobsPerSeed;
  }

  public void setBatchJobsPerSeed(int batchJobsPerSeed) {
    this.batchJobsPerSeed = batchJobsPerSeed;
  }

  public int getInvocationsPerBatchJob() {
    return invocationsPerBatchJob;
  }

  public void setInvocationsPerBatchJob(int invocationsPerBatchJob) {
    this.invocationsPerBatchJob = invocationsPerBatchJob;
  }

  public String getSeedJobDefinitionId() {
    return seedJobDefinitionId;
  }

  public void setSeedJobDefinitionId(String seedJobDefinitionId) {
    this.seedJobDefinitionId = seedJobDefinitionId;
  }

  public String getMonitorJobDefinitionId() {
    return monitorJobDefinitionId;
  }

  public void setMonitorJobDefinitionId(String monitorJobDefinitionId) {
    this.monitorJobDefinitionId = monitorJobDefinitionId;
  }

  public String getBatchJobDefinitionId() {
    return batchJobDefinitionId;
  }

  public void setBatchJobDefinitionId(String batchJobDefinitionId) {
    this.batchJobDefinitionId = batchJobDefinitionId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getCreateUserId() {
    return createUserId;
  }

  public void setCreateUserId(String createUserId) {
    this.createUserId = createUserId;
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  @Override
  public Date getExecutionStartTime() {
    return executionStartTime;
  }

  public void setExecutionStartTime(final Date executionStartTime) {
    this.executionStartTime = executionStartTime;
  }

  @Override
  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    persistentState.put("endTime", endTime);
    persistentState.put("executionStartTime", executionStartTime);
    return persistentState;
  }

  public void delete() {
    HistoricIncidentManager historicIncidentManager = Context.getCommandContext().getHistoricIncidentManager();
    historicIncidentManager.deleteHistoricIncidentsByJobDefinitionId(seedJobDefinitionId);
    historicIncidentManager.deleteHistoricIncidentsByJobDefinitionId(monitorJobDefinitionId);
    historicIncidentManager.deleteHistoricIncidentsByJobDefinitionId(batchJobDefinitionId);

    HistoricJobLogManager historicJobLogManager = Context.getCommandContext().getHistoricJobLogManager();
    historicJobLogManager.deleteHistoricJobLogsByJobDefinitionId(seedJobDefinitionId);
    historicJobLogManager.deleteHistoricJobLogsByJobDefinitionId(monitorJobDefinitionId);
    historicJobLogManager.deleteHistoricJobLogsByJobDefinitionId(batchJobDefinitionId);

    Context.getCommandContext().getHistoricBatchManager().delete(this);
  }

}
