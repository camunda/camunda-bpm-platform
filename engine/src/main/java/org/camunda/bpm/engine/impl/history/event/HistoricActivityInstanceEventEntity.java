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

import org.camunda.bpm.engine.impl.persistence.entity.ActivityInstanceState;


/**
 * <p>{@link HistoryEvent} implementation for events that happen in an activity.</p>
 *
 * @author Daniel Meyer
 * @author Marcel Wieczorek
 * @author roman.smirnov
 *
 */
public class HistoricActivityInstanceEventEntity extends HistoricScopeInstanceEvent {

  private static final long serialVersionUID = 1L;

  /** the id of the activity */
  protected String activityId;

  /** the name of the activity */
  protected String activityName;

  /** the type of the activity (startEvent, serviceTask ...) */
  protected String activityType;

  /** the id of this activity instance */
  protected String activityInstanceId;

  /** the state of this activity instance */
  protected int activityInstanceState;

  /** the id of the parent activity instance */
  protected String parentActivityInstanceId;

  /** the id of the child activity instance */
  protected String calledProcessInstanceId;

  protected String taskId;
  protected String taskAssignee;


  // getters and setters //////////////////////////////////////////////////////

  public String getAssignee() {
    return taskAssignee;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public String getActivityType() {
    return activityType;
  }

  public void setActivityType(String activityType) {
    this.activityType = activityType;
  }

  public String getActivityName() {
    return activityName;
  }

  public void setActivityName(String activityName) {
    this.activityName = activityName;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  public String getParentActivityInstanceId() {
    return parentActivityInstanceId;
  }

  public void setParentActivityInstanceId(String parentActivityInstanceId) {
    this.parentActivityInstanceId = parentActivityInstanceId;
  }

  public String getCalledProcessInstanceId() {
    return calledProcessInstanceId;
  }

  public void setCalledProcessInstanceId(String calledProcessInstanceId) {
    this.calledProcessInstanceId = calledProcessInstanceId;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getTaskAssignee() {
    return taskAssignee;
  }

  public void setTaskAssignee(String taskAssignee) {
    this.taskAssignee = taskAssignee;
  }

  public void setActivityInstanceState(int activityInstanceState) {
    this.activityInstanceState = activityInstanceState;
  }

  public int getActivityInstanceState() {
    return activityInstanceState;
  }

  public boolean isCompleteScope() {
    return ActivityInstanceState.SCOPE_COMPLETE.getStateCode() == activityInstanceState;
  }

  public boolean isCanceled() {
    return ActivityInstanceState.CANCELED.getStateCode() == activityInstanceState;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[activityId=" + activityId
           + ", activityName=" + activityName
           + ", activityType=" + activityType
           + ", activityInstanceId=" + activityInstanceId
           + ", activityInstanceState=" + activityInstanceState
           + ", parentActivityInstanceId=" + parentActivityInstanceId
           + ", calledProcessInstanceId=" + calledProcessInstanceId
           + ", taskId=" + taskId
           + ", taskAssignee=" + taskAssignee
           + ", durationInMillis=" + durationInMillis
           + ", startTime=" + startTime
           + ", endTime=" + endTime
           + ", eventType=" + eventType
           + ", executionId=" + executionId
           + ", processDefinitionId=" + processDefinitionId
           + ", processInstanceId=" + processInstanceId
           + "]";
  }
}
