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

import java.io.Serializable;

import org.camunda.bpm.engine.impl.db.DbSqlSession;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;

/**
 * <p>The base class for all history events.</p>
 *
 * <p>A history event contains data about an event that has happened
 * in a process instance. Such an event may be the start of an activity,
 * the end of an activity, a task instance that is created or other similar
 * events...</p>
 *
 * <p>History events contain data in a serializable form. Some
 * implementations may persist events directly or may serialize
 * them as an intermediate representation for later processing
 * (ie. in an asynchronous implementation).</p>
 *
 * <p>This class implements {@link DbEntity}. This was chosen so
 * that {@link HistoryEvent}s can be easily persisted using the
 * {@link DbSqlSession}. This may not be used by all {@link HistoryEventHandler}
 * implementations but it does also not cause harm.</p>
 *
 * @author Daniel Meyer
 *
 */
public class HistoryEvent implements Serializable, DbEntity {

  private static final long serialVersionUID = 1L;

  /** fired when an activity instance is started. */
  public static final String ACTIVITY_EVENT_TYPE_START = "start";
  /** fired when an activity instance is updated. */
  public static final String ACTIVITY_EVENT_TYPE_UPDATE = "update";
  /** fired when an activity instance is ended. */
  public static final String ACTIVITY_EVENT_TYPE_END = "end";

  /** fired when a task instance is created */
  public static final String TASK_EVENT_TYPE_CREATE = "create";
  /** fired when a task instance is updated. */
  public static final String TASK_EVENT_TYPE_UPDATE = "update";
  /** fired when a task instance is completed. */
  public static final String TASK_EVENT_TYPE_COMPLETE = "complete";
  /** fired when a task instance is deleted. */
  public static final String TASK_EVENT_TYPE_DELETE = "delete";

  /** fired when a variable instance is created */
  public static final String VARIABLE_EVENT_TYPE_CREATE = "create";
  /** fired when a variable instance is updated */
  public static final String VARIABLE_EVENT_TYPE_UPDATE = "update";
  /** fired when a variable instance is deleted */
  public static final String VARIABLE_EVENT_TYPE_DELETE = "delete";

  /** fired when a form property is updated */
  public static final String FORM_PROPERTY_UPDATE = "form-property-update";

  public static final String INCIDENT_CREATE = "create";
  public static final String INCIDENT_DELETE = "delete";
  public static final String INCIDENT_RESOLVE = "resolve";

  /** each {@link HistoryEvent} has a unique id */
  protected String id;

  /** the process instance in which the event has happened */
  protected String processInstanceId;

  /** the id of the execution in which the event has happened */
  protected String executionId;

  /** the id of the process definition */
  protected String processDefinitionId;

  /** the case instance in which the event has happened */
  protected String caseInstanceId;

  /** the id of the case execution in which the event has happened */
  protected String caseExecutionId;

  /** the id of the case definition */
  protected String caseDefinitionId;

  /**
   * The type of the activity audit event.
   *
   * @see #ACTIVITY_EVENT_TYPE_START
   * @see #ACTIVITY_EVENT_TYPE_END
   * @see #ACTIVITY_EVENT_TYPE_UPDATE
   *
   * @see #TASK_EVENT_TYPE_CREATE
   * @see #TASK_EVENT_TYPE_UPDATE
   * @see #TASK_EVENT_TYPE_COMPLETE
   * @see #TASK_EVENT_TYPE_DELETE
   *
   * @see #VARIABLE_EVENT_TYPE_CREATE
   * @see #VARIABLE_EVENT_TYPE_UPDATE
   * @see #VARIABLE_EVENT_TYPE_DELETE
   * */
  protected String eventType;

  // getters / setters ///////////////////////////////////

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  public void setCaseDefinitionId(String caseDefinitionId) {
    this.caseDefinitionId = caseDefinitionId;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public void setCaseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
  }

  public String getCaseExecutionId() {
    return caseExecutionId;
  }

  public void setCaseExecutionId(String caseExecutionId) {
    this.caseExecutionId = caseExecutionId;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  // persistent object implementation ///////////////

  public Object getPersistentState() {
    // events are immutable
    return HistoryEvent.class;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[id=" + id
           + ", eventType=" + eventType
           + ", executionId=" + executionId
           + ", processDefinitionId=" + processDefinitionId
           + ", processInstanceId=" + processInstanceId
           + "]";
  }

}
