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
package org.camunda.bpm.engine.impl.history.event;

import java.util.Date;

/**
 * <p>{@link HistoryEvent} signifying a top-level event in a process instance.</p>
 * 
 * @author Daniel Meyer
 * @author Marcel Wieczorek
 * 
 */
public class HistoricProcessInstanceEventEntity extends HistoryEvent {

  private static final long serialVersionUID = 1L;

  /** the business key of the process instance */
  protected String businessKey;

  /** the id of the user that started the process instance */
  protected String startUserId;

  /** the id of the super process instance */
  protected String superProcessInstanceId;

  /** the reason why this process instance was cancelled (deleted) */
  protected String deleteReason;

  /** duration in millis */
  protected Long durationInMillis;
  
  protected Date startTime;
  
  protected Date endTime;
  
  /** id of the activity which started the process instance */
  protected String endActivityId;
  
  /** id of the activity which ended the process instance */
  protected String startActivityId;

  // getters / setters ////////////////////////////////////////

  public Long getDurationInMillis() {
    if(endTime != null) {      
      return endTime.getTime() - startTime.getTime();     
    } else {
      return durationInMillis;
    }
  }

  public void setDurationInMillis(Long durationInMillis) {
    this.durationInMillis = durationInMillis;
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

  public String getEndActivityId() {
    return endActivityId;
  }

  public void setEndActivityId(String endActivityId) {
    this.endActivityId = endActivityId;
  }

  public String getStartActivityId() {
    return startActivityId;
  }

  public void setStartActivityId(String startActivityId) {
    this.startActivityId = startActivityId;
  }

  public String getBusinessKey() {
    return businessKey;
  }

  public void setBusinessKey(String businessKey) {
    this.businessKey = businessKey;
  }

  public String getStartUserId() {
    return startUserId;
  }

  public void setStartUserId(String startUserId) {
    this.startUserId = startUserId;
  }

  public String getSuperProcessInstanceId() {
    return superProcessInstanceId;
  }

  public void setSuperProcessInstanceId(String superProcessInstanceId) {
    this.superProcessInstanceId = superProcessInstanceId;
  }

  public String getDeleteReason() {
    return deleteReason;
  }

  public void setDeleteReason(String deleteReason) {
    this.deleteReason = deleteReason;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[businessKey=" + businessKey
           + ", startUserId=" + startUserId
           + ", superProcessInstanceId=" + superProcessInstanceId
           + ", deleteReason=" + deleteReason
           + ", durationInMillis=" + durationInMillis
           + ", startTime=" + startTime
           + ", endTime=" + endTime
           + ", endActivityId=" + endActivityId
           + ", startActivityId=" + startActivityId
           + ", id=" + id
           + ", eventType=" + eventType
           + ", executionId=" + executionId
           + ", processDefinitionId=" + processDefinitionId
           + ", processInstanceId=" + processInstanceId
           + "]";
  }

}
