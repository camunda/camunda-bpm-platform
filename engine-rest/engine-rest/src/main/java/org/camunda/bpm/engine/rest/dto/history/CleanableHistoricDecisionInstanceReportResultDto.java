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

package org.camunda.bpm.engine.rest.dto.history;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.history.CleanableHistoricDecisionInstanceReportResult;

public class CleanableHistoricDecisionInstanceReportResultDto implements Serializable {

  private static final long serialVersionUID = 1L;

  protected String decisionDefinitionId;
  protected String decisionDefinitionKey;
  protected String decisionDefinitionName;
  protected int decisionDefinitionVersion;
  protected Integer historyTimeToLive;
  protected long finishedDecisionInstanceCount;
  protected long cleanableDecisionInstanceCount;

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

  public void setFinishedDecisionInstanceCount(Long finishedDecisionInstanceCount) {
    this.finishedDecisionInstanceCount = finishedDecisionInstanceCount;
  }

  public long getCleanableDecisionInstanceCount() {
    return cleanableDecisionInstanceCount;
  }

  public void setCleanableDecisionInstanceCount(Long cleanableDecisionInstanceCount) {
    this.cleanableDecisionInstanceCount = cleanableDecisionInstanceCount;
  }

  public static List<CleanableHistoricDecisionInstanceReportResultDto> convert(List<CleanableHistoricDecisionInstanceReportResult> reportResult) {
    List<CleanableHistoricDecisionInstanceReportResultDto> dtos = new ArrayList<CleanableHistoricDecisionInstanceReportResultDto>();
    for (CleanableHistoricDecisionInstanceReportResult current : reportResult) {
      CleanableHistoricDecisionInstanceReportResultDto dto = new CleanableHistoricDecisionInstanceReportResultDto();
      dto.setDecisionDefinitionId(current.getDecisionDefinitionId());
      dto.setDecisionDefinitionKey(current.getDecisionDefinitionKey());
      dto.setDecisionDefinitionName(current.getDecisionDefinitionName());
      dto.setDecisionDefinitionVersion(current.getDecisionDefinitionVersion());
      dto.setHistoryTimeToLive(current.getHistoryTimeToLive());
      dto.setFinishedDecisionInstanceCount(current.getFinishedDecisionInstanceCount());
      dto.setCleanableDecisionInstanceCount(current.getCleanableDecisionInstanceCount());
      dtos.add(dto);
    }
    return dtos;
  }
}
