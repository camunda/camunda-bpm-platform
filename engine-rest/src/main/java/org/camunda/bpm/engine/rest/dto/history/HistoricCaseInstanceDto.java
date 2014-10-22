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

import java.util.Date;

import org.camunda.bpm.engine.history.HistoricCaseInstance;

public class HistoricCaseInstanceDto {

  private String id;
  private String businessKey;
  private String caseDefinitionId;
  private Date createTime;
  private Date closeTime;
  private Long durationInMillis;
  private String createUserId;
  private String superCaseInstanceId;

  public String getId() {
    return id;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public Date getCloseTime() {
    return closeTime;
  }

  public Long getDurationInMillis() {
    return durationInMillis;
  }

  public String getCreateUserId() {
    return createUserId;
  }

  public String getSuperCaseInstanceId() {
    return superCaseInstanceId;
  }

  public static HistoricCaseInstanceDto fromHistoricCaseInstance(HistoricCaseInstance historicCaseInstance) {

    HistoricCaseInstanceDto dto = new HistoricCaseInstanceDto();

    dto.id = historicCaseInstance.getId();
    dto.businessKey = historicCaseInstance.getBusinessKey();
    dto.caseDefinitionId = historicCaseInstance.getCaseDefinitionId();
    dto.createTime = historicCaseInstance.getCreateTime();
    dto.closeTime = historicCaseInstance.getCloseTime();
    dto.durationInMillis = historicCaseInstance.getDurationInMillis();
    dto.createUserId = historicCaseInstance.getCreateUserId();
    dto.superCaseInstanceId = historicCaseInstance.getSuperCaseInstanceId();

    return dto;
  }

}
