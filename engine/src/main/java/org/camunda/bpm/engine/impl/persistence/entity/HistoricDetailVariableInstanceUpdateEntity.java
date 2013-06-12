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
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.DbSqlSession;
import org.camunda.bpm.engine.impl.db.HasRevision;
import org.camunda.bpm.engine.impl.db.PersistentObject;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.impl.variable.ValueFields;
import org.camunda.bpm.engine.impl.variable.VariableType;


/**
 * @author Tom Baeyens
 */
public class HistoricDetailVariableInstanceUpdateEntity extends HistoricVariableUpdateEventEntity implements ValueFields, HistoricVariableUpdate {
  
  private static final long serialVersionUID = 1L;
  
  protected VariableType variableType;
  protected ByteArrayEntity byteArrayValue;
  protected String byteArrayValueId;

  protected Object cachedValue;

  public HistoricDetailVariableInstanceUpdateEntity(VariableInstanceEntity variableInstance) {
    this.processInstanceId = variableInstance.getProcessInstanceId();
    this.executionId = variableInstance.getExecutionId();
    this.taskId = variableInstance.getTaskId();
    this.revision = variableInstance.getRevision();
    this.name = variableInstance.getName();
    this.variableType = variableInstance.getType();
    this.time = ClockUtil.getCurrentTime();
    if (variableInstance.getByteArrayValueId()!=null) {
      // TODO test and review.  name ok here?
      this.byteArrayValue = new ByteArrayEntity(name, variableInstance.getByteArrayValue().getBytes());
      Context
        .getCommandContext()
        .getDbSqlSession()
        .insert(byteArrayValue);
      this.byteArrayValueId = byteArrayValue.getId();
    }
    this.textValue = variableInstance.getTextValue();
    this.textValue2 = variableInstance.getTextValue2();
    this.doubleValue = variableInstance.getDoubleValue();
    this.longValue = variableInstance.getLongValue();
  }
  
  public Object getValue() {
    if (!variableType.isCachable() || cachedValue==null) {
      cachedValue = variableType.getValue(this);
    }
    return cachedValue;
  }

  public void delete() {
    
    DbSqlSession dbSqlSession = Context
        .getCommandContext()
        .getDbSqlSession();

    dbSqlSession.delete(this);

    if (byteArrayValueId != null) {
      // the next apparently useless line is probably to ensure consistency in the DbSqlSession 
      // cache, but should be checked and docced here (or removed if it turns out to be unnecessary)
      // @see also HistoricVariableInstanceEntity
      getByteArrayValue();
      Context
        .getCommandContext()
        .getByteArrayManager()
        .deleteByteArrayById(byteArrayValueId);
    }
  }
  
  public String getVariableTypeName() {
    return (variableType!=null ? variableType.getTypeName() : null);
  }

  // byte array value /////////////////////////////////////////////////////////
  
  // i couldn't find a easy readable way to extract the common byte array value logic
  // into a common class.  therefor it's duplicated in VariableInstanceEntity, 
  // HistoricVariableInstance and HistoricDetailVariableInstanceUpdateEntity 
  
  public String getByteArrayValueId() {
    return byteArrayValueId;
  }

  public ByteArrayEntity getByteArrayValue() {
    if ((byteArrayValue == null) && (byteArrayValueId != null)) {
      byteArrayValue = Context
        .getCommandContext()
        .getDbSqlSession()
        .selectById(ByteArrayEntity.class, byteArrayValueId);
    }
    return byteArrayValue;
  }
  
  public void setByteArrayValue(byte[] bytes) {
    ByteArrayEntity byteArrayValue = null;
    if (this.byteArrayValueId!=null) {
      getByteArrayValue();
      Context
        .getCommandContext()
        .getByteArrayManager()
       .deleteByteArrayById(this.byteArrayValueId);
    }
    if (bytes!=null) {
      byteArrayValue = new ByteArrayEntity(bytes);
      Context
        .getCommandContext()
        .getDbSqlSession()
        .insert(byteArrayValue);
    }
    this.byteArrayValue = byteArrayValue;
    if (byteArrayValue != null) {
      this.byteArrayValueId = byteArrayValue.getId();
    } else {
      this.byteArrayValueId = null;
    }
  }

  protected void deleteByteArrayValue() {
    if (byteArrayValueId != null) {
      // the next apparently useless line is probably to ensure consistency in the DbSqlSession 
      // cache, but should be checked and docced here (or removed if it turns out to be unnecessary)
      getByteArrayValue();
      Context
        .getCommandContext()
        .getByteArrayManager()
        .deleteByteArrayById(this.byteArrayValueId);
    }
  }
  
  public String getName() {
    return getVariableName();
  }
  
  // getters and setters //////////////////////////////////////////////////////
  
  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public VariableType getVariableType() {
    return variableType;
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
}
