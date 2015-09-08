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
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.variable.CoreVariableInstance;
import org.camunda.bpm.engine.impl.db.DbEntity;
import org.camunda.bpm.engine.impl.db.DbEntityLifecycleAware;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.HasDbRevision;
import org.camunda.bpm.engine.impl.persistence.entity.util.ByteArrayField;
import org.camunda.bpm.engine.impl.persistence.entity.util.TypedValueField;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Tom Baeyens
 */
public class VariableInstanceEntity implements VariableInstance, CoreVariableInstance, ValueFields, DbEntity, DbEntityLifecycleAware, HasDbRevision, Serializable {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  private static final long serialVersionUID = 1L;

  protected String id;
  protected int revision;

  protected String name;

  protected String processInstanceId;
  protected String executionId;
  protected String taskId;
  protected String caseInstanceId;
  protected String caseExecutionId;
  protected String activityInstanceId;

  protected Long longValue;
  protected Double doubleValue;
  protected String textValue;
  protected String textValue2;

  protected ByteArrayField byteArrayField = new ByteArrayField(this);

  protected TypedValueField typedValueField = new TypedValueField(this);

  boolean forcedUpdate;

  protected String configuration;

  protected long sequenceCounter = 1;

  /**
   * <p>Determines whether this variable is supposed to be a local variable
   * in case of concurrency in its scope. This affects
   * </p>
   *
   * <ul>
   * <li>tree expansion (not evaluated yet by the engine)
   * <li>activity instance IDs of variable instances: concurrentLocal
   *   variables always receive the activity instance id of their execution
   *   (which may not be the scope execution), while non-concurrentLocal variables
   *   always receive the activity instance id of their scope (which is set in the
   *   parent execution)
   * </ul>
   *
   * <p>
   *   In the future, this field could be used for restoring the variable distribution
   *   when the tree is expanded/compacted multiple times.
   *   On expansion, the goal would be to keep concurrentLocal variables always with
   *   their concurrent replacing executions while non-concurrentLocal variables
   *   stay in the scope execution
   * </p>
   */
  protected boolean isConcurrentLocal = false;

  // Default constructor for SQL mapping
  public VariableInstanceEntity() {
  }

  public static VariableInstanceEntity createAndInsert(String name, TypedValue value) {
    VariableInstanceEntity variableInstance = create(name, value);
    insert(variableInstance);

    return variableInstance;
  }

  public static void insert(VariableInstanceEntity variableInstance) {
    Context
    .getCommandContext()
    .getDbEntityManager()
    .insert(variableInstance);
  }

  public static VariableInstanceEntity create(String name, TypedValue value) {
    VariableInstanceEntity variableInstance = new VariableInstanceEntity();
    variableInstance.name = name;
    variableInstance.setValue(value);

    return variableInstance;
  }

  public void delete() {

    // clear value
    clearValueFields();

    // delete variable
    Context
      .getCommandContext()
      .getDbEntityManager()
      .delete(this);
  }

  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<String, Object>();
    if (typedValueField.getSerializerName() != null) {
      persistentState.put("serializerName", typedValueField.getSerializerName());
    }
    if (longValue != null) {
      persistentState.put("longValue", longValue);
    }
    if (doubleValue != null) {
      persistentState.put("doubleValue", doubleValue);
    }
    if (textValue != null) {
      persistentState.put("textValue", textValue);
    }
    if (textValue2 != null) {
      persistentState.put("textValue2", textValue2);
    }
    if (byteArrayField.getByteArrayId() != null) {
      persistentState.put("byteArrayValueId", byteArrayField.getByteArrayId());
    }
    if (forcedUpdate) {
      persistentState.put("forcedUpdate", Boolean.TRUE);
    }

    persistentState.put("sequenceCounter", getSequenceCounter());
    persistentState.put("concurrentLocal", isConcurrentLocal);

    return persistentState;
  }

  public int getRevisionNext() {
    return revision+1;
  }

  // lazy initialized relations ///////////////////////////////////////////////

  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  public void setCaseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
  }

  public void setCaseExecutionId(String caseExecutionId) {
    this.caseExecutionId = caseExecutionId;
  }

  // byte array value /////////////////////////////////////////////////////////

  // i couldn't find a easy readable way to extract the common byte array value logic
  // into a common class.  therefor it's duplicated in VariableInstanceEntity,
  // HistoricVariableInstance and HistoricDetailVariableInstanceUpdateEntity

  public String getByteArrayValueId() {
    return byteArrayField.getByteArrayId();
  }

  public void setByteArrayValueId(String byteArrayValueId) {
    this.byteArrayField.setByteArrayId(byteArrayValueId);
  }

  public ByteArrayEntity getByteArrayValue() {
    return byteArrayField.getByteArrayValue();
  }

  public void setByteArrayValue(byte[] bytes) {
    byteArrayField.setByteArrayValue(bytes);
  }

  protected void deleteByteArrayValue() {
    byteArrayField.deleteByteArrayValue();
  }

  // type /////////////////////////////////////////////////////////////////////

  public Object getValue() {
    return typedValueField.getValue();
  }

  public TypedValue getTypedValue() {
    return typedValueField.getTypedValue();
  }

  public TypedValue getTypedValue(boolean deserializeValue) {
    return typedValueField.getTypedValue(deserializeValue);
  }

  public TypedValue setValue(TypedValue value) {
    // clear value fields
    clearValueFields();

    return typedValueField.setValue(value);
  }

  public void clearValueFields() {
    this.longValue = null;
    this.doubleValue = null;
    this.textValue = null;
    this.textValue2 = null;
    typedValueField.clear();

    if(byteArrayField.getByteArrayId() != null) {
      deleteByteArrayValue();
      setByteArrayValueId(null);
    }
  }

  public String getTypeName() {
    return typedValueField.getTypeName();
  }

  // entity lifecycle /////////////////////////////////////////////////////////

  public void postLoad() {
    // make sure the serializer is initialized
    typedValueField.postLoad();
  }

  // execution ////////////////////////////////////////////////////////////////

  public ExecutionEntity getExecution() {
    if (executionId != null) {
      return Context
        .getCommandContext()
        .getExecutionManager()
        .findExecutionById(executionId);
    }
    return null;
  }

  public void setExecution(ExecutionEntity execution) {
    this.executionId = execution.getId();
    this.processInstanceId = execution.getProcessInstanceId();
    forcedUpdate = true;
  }

  // case execution ///////////////////////////////////////////////////////////

  public CaseExecutionEntity getCaseExecution() {
    if (caseExecutionId != null) {
      return Context
          .getCommandContext()
          .getCaseExecutionManager()
          .findCaseExecutionById(caseExecutionId);
    }
    return null;
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTextValue() {
    return textValue;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public String getCaseExecutionId() {
    return caseExecutionId;
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

  public void setName(String name) {
    this.name = name;
  }

  public void setTextValue(String textValue) {
    this.textValue = textValue;
  }

  public String getName() {
    return name;
  }

  public int getRevision() {
    return revision;
  }

  public void setRevision(int revision) {
    this.revision = revision;
  }

  public void setSerializer(TypedValueSerializer<?> serializer) {
    typedValueField.setSerializerName(serializer.getName());
  }

  public void setSerializerName(String type) {
    typedValueField.setSerializerName(type);
  }

  public TypedValueSerializer<?> getSerializer() {
    return typedValueField.getSerializer();
  }

  public String getTextValue2() {
    return textValue2;
  }

  public void setTextValue2(String textValue2) {
    this.textValue2 = textValue2;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  public void setActivityInstanceId(String activityInstanceId) {
    this.activityInstanceId = activityInstanceId;
  }

  public String getSerializerName() {
    return typedValueField.getSerializerName();
  }

  public String getErrorMessage() {
    return typedValueField.getErrorMessage();
  }

  public String getVariableScope() {
    if (taskId != null) {
      return taskId;
    }

    if (executionId != null) {
      return executionId;
    }

    return caseExecutionId;
  }

  //sequence counter ///////////////////////////////////////////////////////////

  public long getSequenceCounter() {
    return sequenceCounter;
  }

  public void setSequenceCounter(long sequenceCounter) {
    this.sequenceCounter = sequenceCounter;
  }

   public void incrementSequenceCounter() {
    sequenceCounter++;
  }


  public boolean isConcurrentLocal() {
    return isConcurrentLocal;
  }

  public void setConcurrentLocal(boolean isConcurrentLocal) {
    this.isConcurrentLocal = isConcurrentLocal;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
      + "[id=" + id
      + ", revision=" + revision
      + ", name=" + name
      + ", processInstanceId=" + processInstanceId
      + ", executionId=" + executionId
      + ", caseInstanceId=" + caseInstanceId
      + ", caseExecutionId=" + caseExecutionId
      + ", taskId=" + taskId
      + ", activityInstanceId=" + activityInstanceId
      + ", longValue=" + longValue
      + ", doubleValue=" + doubleValue
      + ", textValue=" + textValue
      + ", textValue2=" + textValue2
      + ", byteArrayValueId=" + getByteArrayValueId()
      + ", forcedUpdate=" + forcedUpdate
      + ", configuration=" + configuration
      + ", isConcurrentLocal=" + isConcurrentLocal
      + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    VariableInstanceEntity other = (VariableInstanceEntity) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }

}
