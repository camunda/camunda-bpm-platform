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
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.variable.ValueFields;
import org.camunda.bpm.engine.impl.variable.VariableType;

/**
 * @author Christian Lipphardt (camunda)
 */
public class HistoricVariableInstanceEntity implements ValueFields, HistoricVariableInstance, DbEntity, HasDbRevision, Serializable {

  private static final long serialVersionUID = 1L;

  protected String id;
  protected String processInstanceId;

  protected String taskId;
  protected String executionId;
  protected String activityInstanceId;

  protected String name;
  protected int revision;
  protected String variableTypeName;
  protected VariableType variableType;

  protected Long longValue;
  protected Double doubleValue;
  protected String textValue;
  protected String textValue2;

  protected ByteArrayEntity byteArrayValue;
  protected String byteArrayId;

  protected Object cachedValue;

  protected String errorMessage;

  protected String dataFormatId;


  public HistoricVariableInstanceEntity() {
  }

  public HistoricVariableInstanceEntity(HistoricVariableUpdateEventEntity historyEvent) {
    updateFromEvent(historyEvent);
  }

  public void updateFromEvent(HistoricVariableUpdateEventEntity historyEvent) {
    this.id = historyEvent.getVariableInstanceId();
    this.processInstanceId = historyEvent.getProcessInstanceId();
    this.taskId = historyEvent.getTaskId();
    this.executionId = historyEvent.getExecutionId();
    this.activityInstanceId = historyEvent.getScopeActivityInstanceId();
    this.name = historyEvent.getVariableName();
    this.variableTypeName = historyEvent.getVariableTypeName();
    this.longValue = historyEvent.getLongValue();
    this.doubleValue = historyEvent.getDoubleValue();
    this.textValue = historyEvent.getTextValue();
    this.textValue2 = historyEvent.getTextValue2();
    this.dataFormatId = historyEvent.getDataFormatId();

    deleteByteArrayValue();
    if(historyEvent.getByteValue() != null) {
      setByteArrayValue(historyEvent.getByteValue());
    }

  }

  public void delete() {
    deleteByteArrayValue();
    Context
      .getCommandContext()
      .getDbSqlSession()
      .delete(this);
  }

  public Object getPersistentState() {
    List<Object> state = new ArrayList<Object>(5);
    state.add(variableTypeName);
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
    if (errorMessage == null && (!variableType.isCachable() || cachedValue==null)) {
      try {
        cachedValue = variableType.getValue(this);

      } catch(RuntimeException e) {
        // catch error message
        errorMessage = e.getMessage();

        //re-throw the exception
        throw e;
      }
    }
    return cachedValue;
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
          .getDbSqlSession()
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
        .getDbSqlSession()
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

  // getters and setters //////////////////////////////////////////////////////

  public String getVariableTypeName() {
    return variableTypeName;
  }

  public String getVariableName() {
    return name;
  }

  public VariableType getVariableType() {
    return variableType;
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

  public Object getCachedValue() {
    return cachedValue;
  }

  public void setCachedValue(Object cachedValue) {
    this.cachedValue = cachedValue;
  }

  public void setVariableType(VariableType variableType) {
    this.variableType = variableType;
  }

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
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

  public String getActivtyInstanceId() {
    return activityInstanceId;
  }

  public void setActivtyInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[id=" + id
           + ", processInstanceId=" + processInstanceId
           + ", taskId=" + taskId
           + ", executionId=" + executionId
           + ", activityInstanceId=" + activityInstanceId
           + ", name=" + name
           + ", revision=" + revision
           + ", variableTypeName=" + variableTypeName
           + ", longValue=" + longValue
           + ", doubleValue=" + doubleValue
           + ", textValue=" + textValue
           + ", textValue2=" + textValue2
           + ", byteArrayId=" + byteArrayId
           + "]";
  }

  public String getDataFormatId() {
    return dataFormatId;
  }

  public void setDataFormatId(String dataFormatId) {
    this.dataFormatId = dataFormatId;
  }

  public Object getRawValue() {
    return variableType.getRawValue(this);
  }

}
