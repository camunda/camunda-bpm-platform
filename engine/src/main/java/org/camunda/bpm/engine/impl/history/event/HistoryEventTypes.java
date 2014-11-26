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
 * The set of built-in history event types.
 *
 * @author Daniel Meyer
 * @since 7.2
 */
public enum HistoryEventTypes implements HistoryEventType {

  /** fired when a process instance is started. */
  PROCESS_INSTANCE_START("process-instance", "start"),
  /** fired when a process instance is ended. */
  PROCESS_INSTANCE_END("process-instance", "end"),

  /** fired when an activity instance is started. */
  ACTIVITY_INSTANCE_START("activity-instance", "start"),
  /** fired when an activity instance is updated. */
  ACTIVITY_INSTANCE_UPDATE("activity-instance", "update"),
  /** fired when an activity instance is ended. */
  ACTIVITY_INSTANCE_END("activity-instance", "end"),

  /** fired when a task instance is created. */
  TASK_INSTANCE_CREATE("task-instance", "create"),
  /** fired when a task instance is updated. */
  TASK_INSTANCE_UPDATE("task-instance", "update"),
  /** fired when a task instance is completed. */
  TASK_INSTANCE_COMPLETE("task-instance", "complete"),
  /** fired when a task instance is deleted. */
  TASK_INSTANCE_DELETE("task-instance", "delete"),

  /** fired when a variable instance is created. */
  VARIABLE_INSTANCE_CREATE("variable-instance", "create"),
  /** fired when a variable instance is updated. */
  VARIABLE_INSTANCE_UPDATE("variable-instance", "update"),
  /** fired when a variable instance is deleted. */
  VARIABLE_INSTANCE_DELETE("variable-instance", "delete"),

  /** fired when a form property is updated. */
  FORM_PROPERTY_UPDATE("form-property", "form-property-update"),

  /** fired when an incident is created. */
  INCIDENT_CREATE("incident", "create"),
  /** fired when an incident is deleted. */
  INCIDENT_DELETE("incident", "delete"),
  /** fired when an incident is resolved. */
  INCIDENT_RESOLVE("incident", "resolve"),

  /** fired when a case instance is created. */
  CASE_INSTANCE_CREATE("case-instance", "create"),
  /** fired when a case instance is updated. */
  CASE_INSTANCE_UPDATE("case-instance", "update"),
  /** fired when a case instance is closed. */
  CASE_INSTANCE_CLOSE("case-instance", "close"),

  /** fired when a case activity instance is created. */
  CASE_ACTIVITY_INSTANCE_CREATE("case-activity-instance", "create"),
  /** fired when a case activity instance is updated. */
  CASE_ACTIVITY_INSTANCE_UPDATE("case-activity-instance", "update"),
  /** fired when a case instance is ended. */
  CASE_ACTIVITY_INSTANCE_END("case-activity_instance", "end");

  private HistoryEventTypes(String entityType, String eventName) {
    this.entityType = eventName;
    this.eventName = eventName;
  }

  protected String entityType;
  protected String eventName;

  public String getEntityType() {
    return entityType;
  }

  public String getEventName() {
    return eventName;
  }

}
