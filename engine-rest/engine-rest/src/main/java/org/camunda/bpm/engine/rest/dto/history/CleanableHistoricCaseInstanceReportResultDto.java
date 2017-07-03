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

import org.camunda.bpm.engine.history.CleanableHistoricCaseInstanceReportResult;

public class CleanableHistoricCaseInstanceReportResultDto implements Serializable {

  private static final long serialVersionUID = 1L;

  protected String caseDefinitionId;
  protected String caseDefinitionKey;
  protected String caseDefinitionName;
  protected int caseDefinitionVersion;
  protected Integer historyTimeToLive;
  protected long finishedCaseInstanceCount;
  protected long cleanableCaseInstanceCount;

  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  public void setCaseDefinitionId(String caseDefinitionId) {
    this.caseDefinitionId = caseDefinitionId;
  }

  public String getCaseDefinitionKey() {
    return caseDefinitionKey;
  }

  public void setCaseDefinitionKey(String caseDefinitionKey) {
    this.caseDefinitionKey = caseDefinitionKey;
  }

  public String getCaseDefinitionName() {
    return caseDefinitionName;
  }

  public void setCaseDefinitionName(String caseDefinitionName) {
    this.caseDefinitionName = caseDefinitionName;
  }

  public int getCaseDefinitionVersion() {
    return caseDefinitionVersion;
  }

  public void setCaseDefinitionVersion(int caseDefinitionVersion) {
    this.caseDefinitionVersion = caseDefinitionVersion;
  }

  public Integer getHistoryTimeToLive() {
    return historyTimeToLive;
  }

  public void setHistoryTimeToLive(Integer historyTimeToLive) {
    this.historyTimeToLive = historyTimeToLive;
  }

  public long getFinishedCaseInstanceCount() {
    return finishedCaseInstanceCount;
  }

  public void setFinishedCaseInstanceCount(Long finishedCaseInstanceCount) {
    this.finishedCaseInstanceCount = finishedCaseInstanceCount;
  }

  public long getCleanableCaseInstanceCount() {
    return cleanableCaseInstanceCount;
  }

  public void setCleanableCaseInstanceCount(Long cleanableCaseInstanceCount) {
    this.cleanableCaseInstanceCount = cleanableCaseInstanceCount;
  }

  public static List<CleanableHistoricCaseInstanceReportResultDto> convert(List<CleanableHistoricCaseInstanceReportResult> reportResult) {
    List<CleanableHistoricCaseInstanceReportResultDto> dtos = new ArrayList<CleanableHistoricCaseInstanceReportResultDto>();
    for (CleanableHistoricCaseInstanceReportResult current : reportResult) {
      CleanableHistoricCaseInstanceReportResultDto dto = new CleanableHistoricCaseInstanceReportResultDto();
      dto.setCaseDefinitionId(current.getCaseDefinitionId());
      dto.setCaseDefinitionKey(current.getCaseDefinitionKey());
      dto.setCaseDefinitionName(current.getCaseDefinitionName());
      dto.setCaseDefinitionVersion(current.getCaseDefinitionVersion());
      dto.setHistoryTimeToLive(current.getHistoryTimeToLive());
      dto.setFinishedCaseInstanceCount(current.getFinishedCaseInstanceCount());
      dto.setCleanableCaseInstanceCount(current.getCleanableCaseInstanceCount());
      dtos.add(dto);
    }
    return dtos;
  }
}
