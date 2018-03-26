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
package org.camunda.bpm.client.impl.variable;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.impl.EngineClientException;
import org.camunda.bpm.client.impl.EngineClientLogger;
import org.camunda.bpm.client.impl.ExternalTaskClientLogger;
import org.camunda.bpm.client.impl.variable.mapper.ValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.primitive.BooleanValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.primitive.BytesValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.primitive.DateValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.primitive.DoubleValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.primitive.IntegerValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.primitive.LongValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.primitive.NullValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.primitive.PrimitiveValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.primitive.ShortValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.primitive.StringValueMapper;
import org.camunda.bpm.client.impl.variable.mapper.serializable.ObjectValueMapper;
import org.camunda.bpm.client.task.impl.dto.TypedValueDto;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.VariableMapImpl;
import org.camunda.bpm.engine.variable.impl.type.ObjectTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tassilo Weidner
 */
public class VariableMappers {

  protected static final EngineClientLogger INTERNAL_LOG = ExternalTaskClientLogger.ENGINE_CLIENT_LOGGER;
  protected static final ExternalTaskClientLogger USER_LOG = ExternalTaskClientLogger.CLIENT_LOGGER;

  protected Map<String, ValueMapper<?>> mappers = new HashMap<>();

  public VariableMappers(ObjectMapper objectMapper) {
    registerMapper(new NullValueMapper());
    registerMapper(new BooleanValueMapper());
    registerMapper(new StringValueMapper());
    registerMapper(new DateValueMapper());
    registerMapper(new BytesValueMapper());

    // number mappers
    registerMapper(new ShortValueMapper());
    registerMapper(new IntegerValueMapper());
    registerMapper(new LongValueMapper());
    registerMapper(new DoubleValueMapper());

    // object mappers
    registerMapper(new ObjectValueMapper(objectMapper));
  }

  public VariableMap deserializeVariables(Map<String, TypedValueDto> typedValueDtoMap) throws EngineClientException {
    VariableMap variableMap = new VariableMapImpl();

    for (Map.Entry<String, TypedValueDto> entry : typedValueDtoMap.entrySet()) {
      String variableName = entry.getKey();
      TypedValueDto typedValueDto = entry.getValue();

      String variableType = decapitalize(typedValueDto.getType()); // decapitalize first letter

      ValueMapper<?> mapper = mappers.get(variableType);
      if (mapper == null) { // external task client does not support the variable type; don't invoke handler
        throw INTERNAL_LOG.exceptionWhileDeserializingVariables(variableName, variableType);
      }
      else {
        TypedValue typedValue = mapper.deserializeTypedValue(typedValueDto);
        if (typedValue == null) { // type of value and type of variable response dto not the same; don't invoke handler
          Object variableValue = typedValueDto.getValue();
          throw INTERNAL_LOG.exceptionWhileDeserializingVariablesWrongType(variableName, variableType, variableValue);
        }

        variableMap.put(variableName, typedValue);
      }
    }

    return variableMap;
  }

  @SuppressWarnings("unchecked")
  public  <T extends TypedValue> T convertToTypedValue(TypedValue typedValue) {
    Object value = null;
    if (typedValue != null) {
      value = typedValue.getValue();
    }

    if (typedValue == null || typedValue instanceof UntypedValueImpl) {
      UntypedValueImpl untypedValue = new UntypedValueImpl(value);

      PrimitiveValueMapper mapper = getMapperByValue(untypedValue);
      if (mapper == null) { // throw user exception due to unsupported type
        throw USER_LOG.unsupportedTypeException(value);
      }

      return (T) mapper.convertToTypedValue(untypedValue);
    }
    else if (typedValue instanceof ObjectValue) {
      checkSerializationDataFormat(typedValue);

      ObjectValueMapper mapper = (ObjectValueMapper) mappers.get(ObjectTypeImpl.TYPE_NAME);

      return (T) mapper.convertToObjectValue((ObjectValue) typedValue);
    }
    else {
      return (T) typedValue;
    }
  }

  protected void checkSerializationDataFormat(TypedValue variableTypedValue) {
    String serializationDataFormat = ((ObjectValue) variableTypedValue).getSerializationDataFormat();

    if (serializationDataFormat != null) {
      String serializationDataFormatJson = Variables.SerializationDataFormats.JSON.getName();
      boolean isDataFormatJson = serializationDataFormat.equals(serializationDataFormatJson);

      if (!isDataFormatJson) {
        throw USER_LOG.unsupportedSerializationDataFormat(variableTypedValue);
      }
    } // else: default json
  }

  public Map<String, TypedValueDto> serializeVariables(VariableMap variableMap) {
    Map<String, TypedValueDto> typedValueDtoMap = new HashMap<>();

    variableMap.keySet().forEach(variableName -> {
      TypedValue typedValue = variableMap.getValueTyped(variableName);

      ValueMapper mapper = mappers.get(typedValue.getType().getName()); // mapper cannot be null as variable has been checked for unsupported type
      TypedValueDto typedValueDto = mapper.serializeTypedValue(typedValue);

      typedValueDtoMap.put(variableName, typedValueDto);
    });

    return typedValueDtoMap;
  }

  protected PrimitiveValueMapper<?> getMapperByValue(TypedValue value) {
    return (PrimitiveValueMapper) mappers.values().stream()
      .filter(mapper -> mapper instanceof PrimitiveValueMapper)
      .filter(mapper -> ((PrimitiveValueMapper) mapper).isAssignable(value))
      .findFirst()
      .orElse(null);
  }

  protected void registerMapper(ValueMapper<?> valueMapper) {
    mappers.put(valueMapper.getTypeName(), valueMapper);
  }

  protected String decapitalize(String name) {
    return Character.toLowerCase(name.charAt(0)) + name.substring(1);
  }

}
