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
package org.camunda.bpm.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.DbEntityLifecycleAware;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.db.HistoricEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.persistence.entity.util.ByteArrayField;
import org.camunda.bpm.engine.impl.persistence.entity.util.TypedValueField;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.repository.ResourceTypes;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Christian Lipphardt (camunda)
 */
public class HistoricVariableInstanceEntity implements ValueFields, HistoricVariableInstance, DbEntity, HasDbRevision, HistoricEntity, Serializable, DbEntityLifecycleAware {

  private static final long serialVersionUID = 1L;
  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  protected String id;

  protected String processDefinitionKey;
  protected String processDefinitionId;
  protected String rootProcessInstanceId;
  protected String processInstanceId;

  protected String taskId;
  protected String executionId;
  protected String activityInstanceId;
  protected String tenantId;

  protected String caseDefinitionKey;
  protected String caseDefinitionId;
  protected String caseInstanceId;
  protected String caseExecutionId;

  protected String name;
  protected int revision;
  protected Date createTime;

  protected Long longValue;
  protected Double doubleValue;
  protected String textValue;
  protected String textValue2;

  protected String state = "CREATED";

  protected Date removalTime;

  protected ByteArrayField byteArrayField = new ByteArrayField(this, ResourceTypes.HISTORY);

  protected TypedValueField typedValueField = new TypedValueField(this, false);

  public HistoricVariableInstanceEntity() {
  }

  public HistoricVariableInstanceEntity(HistoricVariableUpdateEventEntity historyEvent) {
    updateFromEvent(historyEvent);
  }

  public void updateFromEvent(HistoricVariableUpdateEventEntity historyEvent) {
    this.id = historyEvent.getVariableInstanceId();
    this.processDefinitionKey = historyEvent.getProcessDefinitionKey();
    this.processDefinitionId = historyEvent.getProcessDefinitionId();
    this.processInstanceId = historyEvent.getProcessInstanceId();
    this.taskId = historyEvent.getTaskId();
    this.executionId = historyEvent.getExecutionId();
    this.activityInstanceId = historyEvent.getScopeActivityInstanceId();
    this.tenantId = historyEvent.getTenantId();
    this.caseDefinitionKey = historyEvent.getCaseDefinitionKey();
    this.caseDefinitionId = historyEvent.getCaseDefinitionId();
    this.caseInstanceId = historyEvent.getCaseInstanceId();
    this.caseExecutionId = historyEvent.getCaseExecutionId();
    this.name = historyEvent.getVariableName();
    this.longValue = historyEvent.getLongValue();
    this.doubleValue = historyEvent.getDoubleValue();
    this.textValue = historyEvent.getTextValue();
    this.textValue2 = historyEvent.getTextValue2();
    this.createTime = historyEvent.getTimestamp();
    this.rootProcessInstanceId = historyEvent.getRootProcessInstanceId();
    this.removalTime = historyEvent.getRemovalTime();

    setSerializerName(historyEvent.getSerializerName());

    byteArrayField.deleteByteArrayValue();

    if(historyEvent.getByteValue() != null) {
      byteArrayField.setRootProcessInstanceId(rootProcessInstanceId);
      byteArrayField.setRemovalTime(removalTime);
      setByteArrayValue(historyEvent.getByteValue());
    }

  }

  public void delete() {
    byteArrayField.deleteByteArrayValue();

    Context
      .getCommandContext()
      .getDbEntityManager()
      .delete(this);
  }

  public Object getPersistentState() {
    List<Object> state = new ArrayList<>(8);
    state.add(getSerializerName());
    state.add(textValue);
    state.add(textValue2);
    state.add(this.state);
    state.add(doubleValue);
    state.add(longValue);
    state.add(processDefinitionId);
    state.add(processDefinitionKey);
    state.add(getByteArrayId());
    return state;
  }

  public int getRevisionNext() {
    return revision+1;
  }

  public Object getValue() {
    return typedValueField.getValue();
  }

  public TypedValue getTypedValue() {
    return typedValueField.getTypedValue(false);
  }

  public TypedValue getTypedValue(boolean deserializeValue) {
    return typedValueField.getTypedValue(deserializeValue, false);
  }

  public TypedValueSerializer<?> getSerializer() {
    return typedValueField.getSerializer();
  }

  public String getByteArrayValueId() {
    return byteArrayField.getByteArrayId();
  }

  public String getByteArrayId() {
    return byteArrayField.getByteArrayId();
  }

  public void setByteArrayId(String byteArrayId) {
    byteArrayField.setByteArrayId(byteArrayId);
  }

  public byte[] getByteArrayValue() {
    return byteArrayField.getByteArrayValue();
  }

  public void setByteArrayValue(byte[] bytes) {
    byteArrayField.setByteArrayValue(bytes);
  }

  // entity lifecycle /////////////////////////////////////////////////////////

  public void postLoad() {
    // make sure the serializer is initialized
    typedValueField.postLoad();
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getSerializerName() {
    return typedValueField.getSerializerName();
  }

  public void setSerializerName(String serializerName) {
    typedValueField.setSerializerName(serializerName);
  }

  public String getTypeName() {
    return typedValueField.getTypeName();
  }

  public String getVariableTypeName() {
    return getTypeName();
  }

  public String getVariableName() {
    return name;
  }

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getLongValue() {
    return longValue;
  }

  public void setLongValue(Long longValue) {
    this.longValue = longValue;
  }

  public Double getDoubleValue() {
    return doubleValue;
  }

  public void setDoubleValue(Double doubleValue) {
    this.doubleValue = doubleValue;
  }

  public String getTextValue() {
    return textValue;
  }

  public void setTextValue(String textValue) {
    this.textValue = textValue;
  }

  public String getTextValue2() {
    return textValue2;
  }

  public void setTextValue2(String textValue2) {
    this.textValue2 = textValue2;
  }

  public void setByteArrayValue(ByteArrayEntity byteArrayValue) {
    byteArrayField.setByteArrayValue(byteArrayValue);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  @Deprecated
  public String getActivtyInstanceId() {
    return activityInstanceId;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
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

  public String getErrorMessage() {
    return typedValueField.getErrorMessage();
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }

  public String getRootProcessInstanceId() {
    return rootProcessInstanceId;
  }

  public void setRootProcessInstanceId(String rootProcessInstanceId) {
    this.rootProcessInstanceId = rootProcessInstanceId;
  }

  public Date getRemovalTime() {
    return removalTime;
  }

  public void setRemovalTime(Date removalTime) {
    this.removalTime = removalTime;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
      + "[id=" + id
      + ", processDefinitionKey=" + processDefinitionKey
      + ", processDefinitionId=" + processDefinitionId
      + ", rootProcessInstanceId=" + rootProcessInstanceId
      + ", removalTime=" + removalTime
      + ", processInstanceId=" + processInstanceId
      + ", taskId=" + taskId
      + ", executionId=" + executionId
      + ", tenantId=" + tenantId
      + ", activityInstanceId=" + activityInstanceId
      + ", caseDefinitionKey=" + caseDefinitionKey
      + ", caseDefinitionId=" + caseDefinitionId
      + ", caseInstanceId=" + caseInstanceId
      + ", caseExecutionId=" + caseExecutionId
      + ", name=" + name
      + ", createTime=" + createTime
      + ", revision=" + revision
      + ", serializerName=" + getSerializerName()
      + ", longValue=" + longValue
      + ", doubleValue=" + doubleValue
      + ", textValue=" + textValue
      + ", textValue2=" + textValue2
      + ", state=" + state
      + ", byteArrayId=" + getByteArrayId()
      + "]";
  }

}
