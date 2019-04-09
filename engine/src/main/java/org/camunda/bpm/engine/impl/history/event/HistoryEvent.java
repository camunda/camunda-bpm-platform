/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.impl.history.event;

import java.io.Serializable;
import java.util.Date;

import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.HistoricEntity;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
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
 * {@link DbEntityManager}. This may not be used by all {@link HistoryEventHandler}
 * implementations but it does also not cause harm.</p>
 *
 * @author Daniel Meyer
 *
 */
public class HistoryEvent implements Serializable, DbEntity, HistoricEntity {

  private static final long serialVersionUID = 1L;

  // constants deprecated since 7.2

  @Deprecated
  public static final String ACTIVITY_EVENT_TYPE_START = HistoryEventTypes.ACTIVITY_INSTANCE_START.getEventName();
  @Deprecated
  public static final String ACTIVITY_EVENT_TYPE_UPDATE = HistoryEventTypes.ACTIVITY_INSTANCE_END.getEventName();
  @Deprecated
  public static final String ACTIVITY_EVENT_TYPE_END = HistoryEventTypes.ACTIVITY_INSTANCE_END.getEventName();

  @Deprecated
  public static final String TASK_EVENT_TYPE_CREATE = HistoryEventTypes.TASK_INSTANCE_CREATE.getEventName();
  @Deprecated
  public static final String TASK_EVENT_TYPE_UPDATE = HistoryEventTypes.TASK_INSTANCE_UPDATE.getEventName();
  @Deprecated
  public static final String TASK_EVENT_TYPE_COMPLETE = HistoryEventTypes.TASK_INSTANCE_COMPLETE.getEventName();
  @Deprecated
  public static final String TASK_EVENT_TYPE_DELETE = HistoryEventTypes.TASK_INSTANCE_DELETE.getEventName();

  @Deprecated
  public static final String VARIABLE_EVENT_TYPE_CREATE = HistoryEventTypes.VARIABLE_INSTANCE_CREATE.getEventName();
  @Deprecated
  public static final String VARIABLE_EVENT_TYPE_UPDATE = HistoryEventTypes.VARIABLE_INSTANCE_UPDATE.getEventName();
  @Deprecated
  public static final String VARIABLE_EVENT_TYPE_DELETE = HistoryEventTypes.VARIABLE_INSTANCE_DELETE.getEventName();

  @Deprecated
  public static final String FORM_PROPERTY_UPDATE = HistoryEventTypes.FORM_PROPERTY_UPDATE.getEventName();

  @Deprecated
  public static final String INCIDENT_CREATE = HistoryEventTypes.INCIDENT_CREATE.getEventName();
  @Deprecated
  public static final String INCIDENT_DELETE = HistoryEventTypes.INCIDENT_DELETE.getEventName();
  @Deprecated
  public static final String INCIDENT_RESOLVE = HistoryEventTypes.INCIDENT_RESOLVE.getEventName();

  public static final String IDENTITY_LINK_ADD = HistoryEventTypes.IDENTITY_LINK_ADD.getEventName();

  public static final String IDENTITY_LINK_DELETE = HistoryEventTypes.IDENTITY_LINK_DELETE.getEventName();

  /** each {@link HistoryEvent} has a unique id */
  protected String id;

  /** the root process instance in which the event has happened */
  protected String rootProcessInstanceId;

  /** the process instance in which the event has happened */
  protected String processInstanceId;

  /** the id of the execution in which the event has happened */
  protected String executionId;

  /** the id of the process definition */
  protected String processDefinitionId;

  /** the key of the process definition */
  protected String processDefinitionKey;

  /** the name of the process definition */
  protected String processDefinitionName;

  /** the version of the process definition */
  protected Integer processDefinitionVersion;

  /** the case instance in which the event has happened */
  protected String caseInstanceId;

  /** the id of the case execution in which the event has happened */
  protected String caseExecutionId;

  /** the id of the case definition */
  protected String caseDefinitionId;

  /** the key of the case definition */
  protected String caseDefinitionKey;

  /** the name of the case definition */
  protected String caseDefinitionName;

  /**
   * The type of the activity audit event.
   * @see HistoryEventType#getEventName()
   * */
  protected String eventType;

  protected long sequenceCounter;

  /* the time when the history event will be deleted */
  protected Date removalTime;

  // getters / setters ///////////////////////////////////

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getRootProcessInstanceId() {
    return rootProcessInstanceId;
  }

  public void setRootProcessInstanceId(String rootProcessInstanceId) {
    this.rootProcessInstanceId = rootProcessInstanceId;
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

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public String getProcessDefinitionName() {
    return processDefinitionName;
  }

  public void setProcessDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
  }

  public Integer getProcessDefinitionVersion() {
    return processDefinitionVersion;
  }

  public void setProcessDefinitionVersion(Integer processDefinitionVersion) {
    this.processDefinitionVersion = processDefinitionVersion;
  }

  public String getCaseDefinitionName() {
    return caseDefinitionName;
  }

  public void setCaseDefinitionName(String caseDefinitionName) {
    this.caseDefinitionName = caseDefinitionName;
  }

  public String getCaseDefinitionKey() {
    return caseDefinitionKey;
  }

  public void setCaseDefinitionKey(String caseDefinitionKey) {
    this.caseDefinitionKey = caseDefinitionKey;
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

  public long getSequenceCounter() {
    return sequenceCounter;
  }

  public void setSequenceCounter(long sequenceCounter) {
    this.sequenceCounter = sequenceCounter;
  }

  public Date getRemovalTime() {
    return removalTime;
  }

  public void setRemovalTime(Date removalTime) {
    this.removalTime = removalTime;
  }

  // persistent object implementation ///////////////

  public Object getPersistentState() {
    // events are immutable
    return HistoryEvent.class;
  }

  // state inspection

  public boolean isEventOfType(HistoryEventType type) {
    return type.getEventName().equals(eventType);
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[id=" + id
           + ", eventType=" + eventType
           + ", executionId=" + executionId
           + ", processDefinitionId=" + processDefinitionId
           + ", processInstanceId=" + processInstanceId
           + ", rootProcessInstanceId=" + rootProcessInstanceId
           + ", removalTime=" + removalTime
           + "]";
  }

}
