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
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializers;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.TypedValue;


/**
 * @author Tom Baeyens
 */
public class HistoricDetailVariableInstanceUpdateEntity extends HistoricVariableUpdateEventEntity implements ValueFields, HistoricVariableUpdate, DbEntityLifecycleAware {

  private static final long serialVersionUID = 1L;
  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  protected TypedValueSerializer<?> serializer;
  protected ByteArrayEntity byteArrayValue;

  protected TypedValue cachedValue;

  protected String errorMessage;

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

  public void delete() {

    DbEntityManager dbEntityManger = Context
        .getCommandContext()
        .getDbEntityManager();

    dbEntityManger.delete(this);

    if (byteArrayId != null) {
      // the next apparently useless line is probably to ensure consistency in the DbSqlSession
      // cache, but should be checked and docced here (or removed if it turns out to be unnecessary)
      // @see also HistoricVariableInstanceEntity
      getByteArrayValue();
      Context
        .getCommandContext()
        .getByteArrayManager()
        .deleteByteArrayById(byteArrayId);
    }
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

  public String getErrorMessage() {
    return errorMessage;
  }

  // byte array value /////////////////////////////////////////////////////////

  // i couldn't find a easy readable way to extract the common byte array value logic
  // into a common class.  therefor it's duplicated in VariableInstanceEntity,
  // HistoricVariableInstance and HistoricDetailVariableInstanceUpdateEntity

  public String getByteArrayValueId() {
    return byteArrayId;
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
    if (this.byteArrayId!=null) {
      getByteArrayValue();
      Context
        .getCommandContext()
        .getByteArrayManager()
       .deleteByteArrayById(this.byteArrayId);
    }
    if (bytes!=null) {
      byteArrayValue = new ByteArrayEntity(bytes);
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
    }
  }

  public String getName() {
    return getVariableName();
  }

  // entity lifecycle /////////////////////////////////////////////////////////

  public void postLoad() {
    // make sure the serializer is initialized
    ensureSerializerInitialized();
  }

  // getters and setters //////////////////////////////////////////////////////

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

  public Date getTime() {
    return timestamp;
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName()
           + "[byteArrayValue=" + byteArrayValue
           + ", variableName=" + variableName
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
