/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.engine.impl.persistence.entity;

import org.camunda.bpm.engine.history.CleanableHistoricProcessInstanceReportResult;


public class CleanableHistoricProcessInstanceReportResultEntity implements CleanableHistoricProcessInstanceReportResult {

  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processDefinitionName;
  protected int processDefinitionVersion;
  protected Integer historyTimeToLive;
  protected long finishedProcessInstanceCount;
  protected long cleanableProcessInstanceCount;
  protected String tenantId;

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public String getProcessDefinitionName() {
    return processDefinitionName;
  }

  public void setProcessDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
  }

  public int getProcessDefinitionVersion() {
    return processDefinitionVersion;
  }

  public void setProcessDefinitionVersion(int processDefinitionVersion) {
    this.processDefinitionVersion = processDefinitionVersion;
  }

  public Integer getHistoryTimeToLive() {
    return historyTimeToLive;
  }

  public void setHistoryTimeToLive(Integer historyTimeToLive) {
    this.historyTimeToLive = historyTimeToLive;
  }

  public long getFinishedProcessInstanceCount() {
    return finishedProcessInstanceCount;
  }

  public void setFinishedProcessInstanceCount(Long finishedProcessInstanceCount) {
    this.finishedProcessInstanceCount = finishedProcessInstanceCount;
  }

  public long getCleanableProcessInstanceCount() {
    return cleanableProcessInstanceCount;
  }

  public void setCleanableProcessInstanceCount(Long cleanableProcessInstanceCount) {
    this.cleanableProcessInstanceCount = cleanableProcessInstanceCount;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String toString() {
    return this.getClass().getSimpleName()
        + "[processDefinitionId = " + processDefinitionId
        + ", processDefinitionKey = " + processDefinitionKey
        + ", processDefinitionName = " + processDefinitionName
        + ", processDefinitionVersion = " + processDefinitionVersion
        + ", historyTimeToLive = " + historyTimeToLive
        + ", finishedProcessInstanceCount = " + finishedProcessInstanceCount
        + ", cleanableProcessInstanceCount = " + cleanableProcessInstanceCount
        + ", tenantId = " + tenantId
        + "]";
  }
}
