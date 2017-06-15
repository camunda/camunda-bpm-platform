
/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl.persistence.entity;

import org.camunda.bpm.engine.history.HistoricFinishedProcessInstanceReportResult;

public class HistoricFinishedProcessInstanceReportResultEntity implements HistoricFinishedProcessInstanceReportResult {

  protected String processDefinitionId;
  protected String processDefinitionKey;
  protected String processDefinitionName;
  protected int processDefinitionVersion;
  protected String historyTimeToLive;
  protected Long finishedProcessInstanceCount;
  protected Long cleanableProcessInstanceCount;

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

  public String getHistoryTimeToLive() {
    return historyTimeToLive;
  }

  public void setHistoryTimeToLive(String historyTimeToLive) {
    this.historyTimeToLive = historyTimeToLive;
  }

  public Long getFinishedProcessInstanceCount() {
    return finishedProcessInstanceCount;
  }

  public void setFinishedProcessInstanceCount(Long finishedProcessInstanceCount) {
    this.finishedProcessInstanceCount = finishedProcessInstanceCount;
  }

  public Long getCleanableProcessInstanceCount() {
    return cleanableProcessInstanceCount;
  }

  public void setCleanableProcessInstanceCount(Long cleanableProcessInstanceCount) {
    this.cleanableProcessInstanceCount = cleanableProcessInstanceCount;
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
        + "]";
  }
}
