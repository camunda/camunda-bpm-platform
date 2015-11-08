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

import java.util.Date;

import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbEntityLifecycleAware;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.persistence.entity.util.ByteArrayField;
import org.camunda.bpm.engine.impl.persistence.entity.util.TypedValueField;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.value.TypedValue;


/**
 * @author Tom Baeyens
 */
public class HistoricDetailVariableInstanceUpdateEntity extends HistoricVariableUpdateEventEntity implements ValueFields, HistoricVariableUpdate, DbEntityLifecycleAware {

  private static final long serialVersionUID = 1L;
  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  protected TypedValueField typedValueField = new TypedValueField(this, false);

  protected ByteArrayField byteArrayField = new ByteArrayField(this);

  public Object getValue() {
    return typedValueField.getValue();
  }

  public TypedValue getTypedValue() {
    return typedValueField.getTypedValue();
  }

  public TypedValue getTypedValue(boolean deserializeValue) {
    return typedValueField.getTypedValue(deserializeValue);
  }

  @Override
  public void delete() {

    DbEntityManager dbEntityManger = Context
        .getCommandContext()
        .getDbEntityManager();

    dbEntityManger.delete(this);

    byteArrayField.deleteByteArrayValue();
  }

  public TypedValueSerializer<?> getSerializer() {
    return typedValueField.getSerializer();
  }

  public String getErrorMessage() {
    return typedValueField.getErrorMessage();
  }

  @Override
  public void setByteArrayId(String id) {
    byteArrayField.setByteArrayId(id);
  }

  @Override
  public String getSerializerName() {
    return typedValueField.getSerializerName();
  }
  @Override
  public void setSerializerName(String serializerName) {
    typedValueField.setSerializerName(serializerName);
  }

  public String getByteArrayValueId() {
    return byteArrayField.getByteArrayId();
  }

  public byte[] getByteArrayValue() {
    return byteArrayField.getByteArrayValue();
  }

  public void setByteArrayValue(byte[] bytes) {
    byteArrayField.setByteArrayValue(bytes);
  }

  public String getName() {
    return getVariableName();
  }

  // entity lifecycle /////////////////////////////////////////////////////////

  public void postLoad() {
    // make sure the serializer is initialized
    typedValueField.postLoad();
  }

  // getters and setters //////////////////////////////////////////////////////

  public String getTypeName() {
    return typedValueField.getTypeName();
  }

  public String getVariableTypeName() {
    return getTypeName();
  }

  public Date getTime() {
    return timestamp;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[variableName=" + variableName
           + ", variableInstanceId=" + variableInstanceId
           + ", revision=" + revision
           + ", serializerName=" + serializerName
           + ", longValue=" + longValue
           + ", doubleValue=" + doubleValue
           + ", textValue=" + textValue
           + ", textValue2=" + textValue2
           + ", byteArrayId=" + byteArrayId
           + ", activityInstanceId=" + activityInstanceId
           + ", eventType=" + eventType
           + ", executionId=" + executionId
           + ", id=" + id
           + ", processDefinitionId=" + processInstanceId
           + ", processInstanceId=" + processInstanceId
           + ", taskId=" + taskId
           + ", timestamp=" + timestamp
           + "]";
  }

}
