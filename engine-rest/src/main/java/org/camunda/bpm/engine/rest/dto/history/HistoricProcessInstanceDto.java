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

import org.camunda.bpm.engine.history.HistoricProcessInstance;

public class HistoricProcessInstanceDto {

  private String id;
  private String businessKey;
  private String processDefinitionId;
  private Date startTime;
  private Date endTime;
  private Long durationInMillis;
  private String startUserId;
  private String startActivityId;
  private String deleteReason;
  private String superProcessInstanceId;

  public String getId() {
    return id;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public Date getStartTime() {
    return startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public Long getDurationInMillis() {
    return durationInMillis;
  }

  public String getStartUserId() {
    return startUserId;
  }

  public String getStartActivityId() {
    return startActivityId;
  }

  public String getDeleteReason() {
    return deleteReason;
  }

  public String getSuperProcessInstanceId() {
    return superProcessInstanceId;
  }

  public static HistoricProcessInstanceDto fromHistoricProcessInstance(HistoricProcessInstance historicProcessInstance) {

    HistoricProcessInstanceDto dto = new HistoricProcessInstanceDto();

    dto.id = historicProcessInstance.getId();
    dto.businessKey = historicProcessInstance.getBusinessKey();
    dto.processDefinitionId = historicProcessInstance.getProcessDefinitionId();
    dto.startTime = historicProcessInstance.getStartTime();
    dto.endTime = historicProcessInstance.getEndTime();
    dto.durationInMillis = historicProcessInstance.getDurationInMillis();
    dto.startUserId = historicProcessInstance.getStartUserId();
    dto.startActivityId = historicProcessInstance.getStartActivityId();
    dto.deleteReason = historicProcessInstance.getDeleteReason();
    dto.superProcessInstanceId = historicProcessInstance.getSuperProcessInstanceId();

    return dto;
  }

}
