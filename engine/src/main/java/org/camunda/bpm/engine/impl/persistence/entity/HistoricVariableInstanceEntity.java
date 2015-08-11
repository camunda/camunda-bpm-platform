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

package org.camunda.bpm.engine.impl.persistence.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.DbEntityLifecycleAware;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializers;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Christian Lipphardt (camunda)
 */
public class HistoricVariableInstanceEntity implements ValueFields, HistoricVariableInstance, DbEntity, HasDbRevision, Serializable, DbEntityLifecycleAware {

  private static final long serialVersionUID = 1L;
  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  protected String id;

  protected String processDefinitionKey;
  protected String processDefinitionId;
  protected String processInstanceId;

  protected String taskId;
  protected String executionId;
  protected String activityInstanceId;

  protected String caseDefinitionKey;
  protected String caseDefinitionId;
  protected String caseInstanceId;
  protected String caseExecutionId;

  protected String name;
  protected int revision;
  protected String serializerName;
  protected TypedValueSerializer<?> serializer;

  protected Long longValue;
  protected Double doubleValue;
  protected String textValue;
  protected String textValue2;

  protected ByteArrayEntity byteArrayValue;
  protected String byteArrayId;

  protected TypedValue cachedValue;

  protected String errorMessage;

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
    this.caseDefinitionKey = historyEvent.getCaseDefinitionKey();
    this.caseDefinitionId = historyEvent.getCaseDefinitionId();
    this.caseInstanceId = historyEvent.getCaseInstanceId();
    this.caseExecutionId = historyEvent.getCaseExecutionId();
    this.name = historyEvent.getVariableName();
    this.serializerName = historyEvent.getSerializerName();
    this.longValue = historyEvent.getLongValue();
    this.doubleValue = historyEvent.getDoubleValue();
    this.textValue = historyEvent.getTextValue();
    this.textValue2 = historyEvent.getTextValue2();

    deleteByteArrayValue();
    if(historyEvent.getByteValue() != null) {
      setByteArrayValue(historyEvent.getByteValue());
    }

  }

  public void delete() {
    deleteByteArrayValue();
    Context
      .getCommandContext()
      .getDbEntityManager()
      .delete(this);
  }

  public Object getPersistentState() {
    List<Object> state = new ArrayList<Object>(5);
    state.add(serializerName);
    state.add(textValue);
    state.add(textValue2);
    state.add(doubleValue);
    state.add(longValue);
    state.add(byteArrayId);
    return state;
  }

  public int getRevisionNext() {
    return revision+1;
  }

  public Object getValue() {
    TypedValue typedValue = getTypedValue();
    if(typedValue != null) {
      return typedValue.getValue();
    } else {
      return null;
    }
  }

  public TypedValue getTypedValue() {
    return getTypedValue(true);
  }

  public TypedValue getTypedValue(boolean deserializeValue) {
    if (cachedValue == null && errorMessage == null) {
      try {
        cachedValue = getSerializer().readValue(this, deserializeValue);
      }
      catch(RuntimeException e) {
        // intercept the error message
        this.errorMessage = e.getMessage();
        throw e;
      }
    }
    return cachedValue;
  }

  public TypedValueSerializer<?> getSerializer() {
    ensureSerializerInitialized();
    return serializer;
  }

  protected void ensureSerializerInitialized() {
    if (serializerName != null && serializer == null) {
      serializer = getSerializers().getSerializerByName(serializerName);
      if (serializer == null) {
        throw LOG.serializerNotDefinedException(this);
      }
    }
  }

  public static VariableSerializers getSerializers() {
    if(Context.getCommandContext() != null) {
      return Context.getProcessEngineConfiguration()
          .getVariableSerializers();
    } else {
      throw LOG.serializerOutOfContextException();
    }
  }

 // byte array value /////////////////////////////////////////////////////////

  // i couldn't find a easy readable way to extract the common byte array value logic
  // into a common class.  therefor it's duplicated in VariableInstanceEntity,
  // HistoricVariableInstance and HistoricDetailVariableInstanceUpdateEntity

  public String getByteArrayValueId() {
    return byteArrayId;
  }

  public String getByteArrayId() {
    return byteArrayId;
  }

  public void setByteArrayId(String byteArrayId) {
    this.byteArrayId = byteArrayId;
    this.byteArrayValue = null;
  }

  public ByteArrayEntity getByteArrayValue() {
    if ((byteArrayValue == null) && (byteArrayId != null)) {
      // no lazy fetching outside of command context
      if(Context.getCommandContext() != null) {
        byteArrayValue = Context
          .getCommandContext()
          .getDbEntityManager()
          .selectById(ByteArrayEntity.class, byteArrayId);
      }
    }
    return byteArrayValue;
  }

  public void setByteArrayValue(byte[] bytes) {
    ByteArrayEntity byteArrayValue = null;
    deleteByteArrayValue();
    if (bytes!=null) {
      byteArrayValue = new ByteArrayEntity(name, bytes);
      Context
        .getCommandContext()
        .getDbEntityManager()
        .insert(byteArrayValue);
    }
    this.byteArrayValue = byteArrayValue;
    if (byteArrayValue != null) {
      this.byteArrayId = byteArrayValue.getId();
    } else {
      this.byteArrayId = null;
    }
  }

  protected void deleteByteArrayValue() {
    if (byteArrayId != null) {
      // the next apparently useless line is probably to ensure consistency in the DbSqlSession
      // cache, but should be checked and docced here (or removed if it turns out to be unnecessary)
      getByteArrayValue();
      Context
        .getCommandContext()
        .getByteArrayManager()
        .deleteByteArrayById(this.byteArrayId);
      byteArrayId = null;
    }
  }

  // entity lifecycle /////////////////////////////////////////////////////////

  public void postLoad() {
    // make sure the serializer is initialized
    ensureSerializerInitialized();
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getSerializerName() {
    return serializerName;
  }

  public void setSerializerName(String serializerName) {
    this.serializerName = serializerName;
  }

  public String getTypeName() {
    if(serializerName == null) {
      return ValueType.NULL.getName();
    } else {
      return getSerializer().getType().getName();
    }
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
    this.byteArrayValue = byteArrayValue;
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
    return errorMessage;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
      + "[id=" + id
      + ", processDefinitionKey=" + processDefinitionKey
      + ", processDefinitionId=" + processDefinitionId
      + ", processInstanceId=" + processInstanceId
      + ", taskId=" + taskId
      + ", executionId=" + executionId
      + ", activityInstanceId=" + activityInstanceId
      + ", caseDefinitionKey=" + caseDefinitionKey
      + ", caseDefinitionId=" + caseDefinitionId
      + ", caseInstanceId=" + caseInstanceId
      + ", caseExecutionId=" + caseExecutionId
      + ", name=" + name
      + ", revision=" + revision
      + ", serializerName=" + serializerName
      + ", longValue=" + longValue
      + ", doubleValue=" + doubleValue
      + ", textValue=" + textValue
      + ", textValue2=" + textValue2
      + ", byteArrayId=" + byteArrayId
      + "]";
  }

}
