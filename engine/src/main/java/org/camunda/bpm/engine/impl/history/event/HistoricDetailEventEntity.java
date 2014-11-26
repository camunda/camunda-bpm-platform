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

import org.camunda.bpm.engine.impl.context.Context;


/**
 * @author Daniel Meyer
 *
 */
public class HistoricDetailEventEntity extends HistoryEvent  {

  private static final long serialVersionUID = 1L;

  protected String activityInstanceId;
  protected String taskId;
  protected Date timestamp;

  // getters and setters //////////////////////////////////////////////////////

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public void delete() {
    Context
      .getCommandContext()
      .getDbEntityManager()
      .delete(this);
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[activityInstanceId=" + activityInstanceId
           + ", taskId=" + taskId
           + ", timestamp=" + timestamp
           + ", eventType=" + eventType
           + ", executionId=" + executionId
           + ", processDefinitionId=" + processDefinitionId
           + ", processInstanceId=" + processInstanceId
           + ", id=" + id
           + "]";
  }

}
