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

package org.camunda.bpm.engine.impl;

import java.io.Serializable;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializers;
import org.camunda.bpm.engine.impl.variable.serializer.jpa.JPAVariableSerializer;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.TypedValue;


/**
 * Represents a variable value used in queries.
 *
 * @author Frederik Heremans
 */
public class QueryVariableValue implements Serializable {
  private static final long serialVersionUID = 1L;
  private String name;
  private TypedValue value;
  private QueryOperator operator;

  private VariableInstanceEntity variableInstanceEntity;
  private boolean local;

  public QueryVariableValue(String name, Object value, QueryOperator operator, boolean local) {
    this.name = name;
    this.value = Variables.untypedValue(value);
    this.operator = operator;
    this.local = local;
  }

  public void initialize(VariableSerializers types) {

    if(variableInstanceEntity == null) {

      // serializer implementation determines which fields are set on the entity
      variableInstanceEntity = VariableInstanceEntity.create(name, value);

      TypedValueSerializer<?> serializer = variableInstanceEntity.getSerializer();
      if(serializer.getType() == ValueType.BYTES){
        throw new ProcessEngineException("Variables of type ByteArray cannot be used to query");

      }
      else if(serializer instanceof JPAVariableSerializer) {
        if(operator != QueryOperator.EQUALS) {
          throw new ProcessEngineException("JPA entity variables can only be used in 'variableValueEquals'");
        }

      }
      else {
        if(!serializer.getType().isPrimitiveValueType()) {
          throw new ProcessEngineException("Object values cannot be used to query");
        }

      }


    }
  }

  public String getName() {
    return name;
  }

  public String getOperator() {
    if(operator != null) {
      return operator.toString();
    }
    return QueryOperator.EQUALS.toString();
  }

  public Object getValue() {
    return value.getValue();
  }

  public String getTextValue() {
    if(variableInstanceEntity != null) {
      return variableInstanceEntity.getTextValue();
    }
    return null;
  }

  public Long getLongValue() {
    if(variableInstanceEntity != null) {
      return variableInstanceEntity.getLongValue();
    }
    return null;
  }

  public Double getDoubleValue() {
    if(variableInstanceEntity != null) {
      return variableInstanceEntity.getDoubleValue();
    }
    return null;
  }

  public String getTextValue2() {
    if(variableInstanceEntity != null) {
      return variableInstanceEntity.getTextValue2();
    }
    return null;
  }

  public String getType() {
    if(variableInstanceEntity != null) {
      return variableInstanceEntity.getSerializer().getName();
    }
    return null;
  }

  public boolean isLocal() {
    return local;
  }
}
