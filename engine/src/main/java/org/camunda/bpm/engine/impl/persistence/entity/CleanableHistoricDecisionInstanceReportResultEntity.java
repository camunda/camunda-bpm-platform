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

import org.camunda.bpm.engine.history.CleanableHistoricDecisionInstanceReportResult;

public class CleanableHistoricDecisionInstanceReportResultEntity implements CleanableHistoricDecisionInstanceReportResult {

  protected String decisionDefinitionId;
  protected String decisionDefinitionKey;
  protected String decisionDefinitionName;
  protected int decisionDefinitionVersion;
  protected Integer historyTimeToLive;
  protected long finishedDecisionInstanceCount;
  protected long cleanableDecisionInstanceCount;
  protected String tenantId;

  public String getDecisionDefinitionId() {
    return decisionDefinitionId;
  }

  public void setDecisionDefinitionId(String decisionDefinitionId) {
    this.decisionDefinitionId = decisionDefinitionId;
  }

  public String getDecisionDefinitionKey() {
    return decisionDefinitionKey;
  }

  public void setDecisionDefinitionKey(String decisionDefinitionKey) {
    this.decisionDefinitionKey = decisionDefinitionKey;
  }

  public String getDecisionDefinitionName() {
    return decisionDefinitionName;
  }

  public void setDecisionDefinitionName(String decisionDefinitionName) {
    this.decisionDefinitionName = decisionDefinitionName;
  }

  public int getDecisionDefinitionVersion() {
    return decisionDefinitionVersion;
  }

  public void setDecisionDefinitionVersion(int decisionDefinitionVersion) {
    this.decisionDefinitionVersion = decisionDefinitionVersion;
  }

  public Integer getHistoryTimeToLive() {
    return historyTimeToLive;
  }

  public void setHistoryTimeToLive(Integer historyTimeToLive) {
    this.historyTimeToLive = historyTimeToLive;
  }

  public long getFinishedDecisionInstanceCount() {
    return finishedDecisionInstanceCount;
  }

  public void setFinishedDecisionInstanceCount(long finishedDecisionInstanceCount) {
    this.finishedDecisionInstanceCount = finishedDecisionInstanceCount;
  }

  public long getCleanableDecisionInstanceCount() {
    return cleanableDecisionInstanceCount;
  }

  public void setCleanableDecisionInstanceCount(long cleanableDecisionInstanceCount) {
    this.cleanableDecisionInstanceCount = cleanableDecisionInstanceCount;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String toString() {
    return this.getClass().getSimpleName()
        + "[decisionDefinitionId = " + decisionDefinitionId
        + ", decisionDefinitionKey = " + decisionDefinitionKey
        + ", decisionDefinitionName = " + decisionDefinitionName
        + ", decisionDefinitionVersion = " + decisionDefinitionVersion
        + ", historyTimeToLive = " + historyTimeToLive
        + ", finishedDecisionInstanceCount = " + finishedDecisionInstanceCount
        + ", cleanableDecisionInstanceCount = " + cleanableDecisionInstanceCount
        + ", tenantId = " + tenantId
        + "]";
  }

}
