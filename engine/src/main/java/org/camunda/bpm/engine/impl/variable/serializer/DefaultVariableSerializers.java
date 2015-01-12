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
package org.camunda.bpm.engine.impl.variable.serializer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class DefaultVariableSerializers implements Serializable, VariableSerializers {

  private static final long serialVersionUID = 1L;

  protected List<TypedValueSerializer<?>> serializerList = new ArrayList<TypedValueSerializer<?>>();
  protected Map<String, TypedValueSerializer<?>> serializerMap = new HashMap<String, TypedValueSerializer<?>>();

  public TypedValueSerializer<?> getSerializerByName(String serializerName) {
    return serializerMap.get(serializerName);
  }

  public TypedValueSerializer<?> findSerializerForValue(TypedValue value) {

    String defaultSerializationFormat = Context.getProcessEngineConfiguration().getDefaultSerializationFormat();

    List<TypedValueSerializer<?>> matchedSerializers = new ArrayList<TypedValueSerializer<?>>();

    ValueType type = value.getType();
    if (type != null && type.isAbstract()) {
      throw new ProcessEngineException("Cannot serialize value of abstract type " + type.getName());
    }

    for (TypedValueSerializer<?> serializer : serializerList) {
      if(type == null || serializer.getType().equals(type)) {

        // if type is null => ask handler whether it can handle the value
        // OR if types match, this handler can handle values of this type
        //    => BUT we still need to ask as the handler may not be able to handle ALL values of this type.

        if(serializer.canHandle(value)) {
          matchedSerializers.add(serializer);
          if(serializer.getType().isPrimitiveValueType()) {
            break;
          }
        }
      }
    }

    if(matchedSerializers.size() == 0) {
      throw new ProcessEngineException("Cannot find serializer for value '"+value+"'.");
    }
    else if(matchedSerializers.size() == 1) {
      return matchedSerializers.get(0);
    }
    else {
      // ambiguous match, use default serializer
      if(defaultSerializationFormat != null) {
        for (TypedValueSerializer<?> typedValueSerializer : matchedSerializers) {
          if(defaultSerializationFormat.equals(typedValueSerializer.getSerializationDataformat())) {
            return typedValueSerializer;
          }
        }
      }
      // no default serialization dataformat defined or default dataformat cannot serialize this value => use first serializer
      return matchedSerializers.get(0);
    }

  }

  public DefaultVariableSerializers addSerializer(TypedValueSerializer<?> serializer) {
    return addSerializer(serializer, serializerList.size());
  }

  public DefaultVariableSerializers addSerializer(TypedValueSerializer<?> serializer, int index) {
    serializerList.add(index, serializer);
    serializerMap.put(serializer.getName(), serializer);
    return this;
  }

  public void setSerializerList(List<TypedValueSerializer<?>> serializerList) {
    this.serializerList.clear();
    this.serializerList.addAll(serializerList);
    this.serializerMap.clear();
    for (TypedValueSerializer<?> serializer : serializerList) {
      serializerMap.put(serializer.getName(), serializer);
    }
  }

  public int getSerializerIndex(TypedValueSerializer<?> serializer) {
    return serializerList.indexOf(serializer);
  }

  public int getSerializerIndexByName(String serializerName) {
    TypedValueSerializer<?> serializer = serializerMap.get(serializerName);
    if(serializer != null) {
      return getSerializerIndex(serializer);
    } else {
      return -1;
    }
  }

  public VariableSerializers removeSerializer(TypedValueSerializer<?> serializer) {
    serializerList.remove(serializer);
    serializerMap.remove(serializer.getName());
    return this;
  }


}
