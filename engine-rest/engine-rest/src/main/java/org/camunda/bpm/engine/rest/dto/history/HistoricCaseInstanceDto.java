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

  protected String id;
  protected String businessKey;
  protected String caseDefinitionId;
  protected String caseDefinitionKey;
  protected String caseDefinitionName;
  protected Date createTime;
  protected Date closeTime;
  protected Long durationInMillis;
  protected String createUserId;
  protected String superCaseInstanceId;
  protected String superProcessInstanceId;
  protected String tenantId;
  protected Boolean active;
  protected Boolean completed;
  protected Boolean terminated;
  protected Boolean closed;

  public String getId() {
    return id;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  public String getCaseDefinitionKey() {
    return caseDefinitionKey;
  }

  public String getCaseDefinitionName() {
    return caseDefinitionName;
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

  public String getSuperProcessInstanceId() {
    return superProcessInstanceId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public Boolean getActive() {
    return active;
  }

  public Boolean getCompleted() {
    return completed;
  }

  public Boolean getTerminated() {
    return terminated;
  }

  public Boolean getClosed() {
    return closed;
  }

  public static HistoricCaseInstanceDto fromHistoricCaseInstance(HistoricCaseInstance historicCaseInstance) {

    HistoricCaseInstanceDto dto = new HistoricCaseInstanceDto();

    dto.id = historicCaseInstance.getId();
    dto.businessKey = historicCaseInstance.getBusinessKey();
    dto.caseDefinitionId = historicCaseInstance.getCaseDefinitionId();
    dto.caseDefinitionKey = historicCaseInstance.getCaseDefinitionKey();
    dto.caseDefinitionName = historicCaseInstance.getCaseDefinitionName();
    dto.createTime = historicCaseInstance.getCreateTime();
    dto.closeTime = historicCaseInstance.getCloseTime();
    dto.durationInMillis = historicCaseInstance.getDurationInMillis();
    dto.createUserId = historicCaseInstance.getCreateUserId();
    dto.superCaseInstanceId = historicCaseInstance.getSuperCaseInstanceId();
    dto.superProcessInstanceId = historicCaseInstance.getSuperProcessInstanceId();
    dto.tenantId = historicCaseInstance.getTenantId();
    dto.active = historicCaseInstance.isActive();
    dto.completed = historicCaseInstance.isCompleted();
    dto.terminated = historicCaseInstance.isTerminated();
    dto.closed = historicCaseInstance.isClosed();

    return dto;
  }

}
