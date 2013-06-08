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

/**
 * @author Marcel Wieczorek
 * 
 */
public class HistoricScopeInstanceEventEntity extends HistoryEvent {

  private static final long serialVersionUID = 1L;

  /** fired when an activity instance is started. */
  public static final String ACTIVITY_EVENT_TYPE_START = "start";
  /** fired when an activity instance is ended. */
  public static final String ACTIVITY_EVENT_TYPE_END = "end";
  /** fired when an activity instance is updated. */
  public static final String ACTIVITY_EVENT_TYPE_UPDATE = "update";

  /** the id of the activity */
  protected String activityId;

  /** the name of the activity */
  protected String activityName;

  /** the type of the activity (startEvent, serviceTask ...) */
  protected String activityType;

  /** the id of this activity instance */
  protected String activityInstanceId;

  /** the id of the parent activity instance */
  protected String parentActivityInstanceId;

  /**
   * The type of the activity audit event.
   * 
   * @see #ACTIVITY_EVENT_TYPE_START
   * @see #ACTIVITY_EVENT_TYPE_END
   * 
   * */
  protected String eventType;

  // getters / setters ///////////////////////////////

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
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

}
