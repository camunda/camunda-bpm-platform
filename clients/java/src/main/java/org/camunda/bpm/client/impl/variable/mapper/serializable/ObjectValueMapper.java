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
package org.camunda.bpm.client.impl.variable.mapper.serializable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.impl.EngineClientException;
import org.camunda.bpm.client.impl.ExternalTaskClientLogger;
import org.camunda.bpm.client.impl.variable.mapper.ValueMapper;
import org.camunda.bpm.client.task.impl.dto.TypedValueDto;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.value.ObjectValueImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

import java.io.IOException;
import java.util.Map;

/**
 * @author Tassilo Weidner
 */
public class ObjectValueMapper implements ValueMapper<ObjectValue> {

  protected static final ExternalTaskClientLogger LOG = ExternalTaskClientLogger.CLIENT_LOGGER;

  protected static final String OBJECT_TYPE_NAME = "objectTypeName";
  protected static final String SERIALIZATION_DATA_FORMAT = "serializationDataFormat";
  protected static final String TRANSIENT = "transient";

  protected ObjectMapper objectMapper;

  public ObjectValueMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public String getTypeName() {
    return ValueType.OBJECT.getName();
  }

  @Override
  public ObjectValue deserializeTypedValue(TypedValueDto typedValueDto) throws EngineClientException {
    String serializedValue = (String) typedValueDto.getValue();

    Map<String, Object> valueInfo = typedValueDto.getValueInfo();

    Class<?> type = null;
    try {
      type = Class.forName((String) valueInfo.get(OBJECT_TYPE_NAME));
    } catch (ClassNotFoundException e) {
      throw new EngineClientException(e);
    }

    Object object = null;
    try {
      object = objectMapper.readValue(serializedValue, type);
    } catch (IOException e) {
      throw new EngineClientException(e.getMessage()); // avoid exceptions of third party components
    }

    ObjectValueImpl objectValue = new ObjectValueImpl(object);

    objectValue.setSerializedValue(serializedValue);

    String objectTypeName = (String) valueInfo.get(OBJECT_TYPE_NAME);
    if (objectTypeName != null) {
      objectValue.setObjectTypeName(objectTypeName);
    }

    String serializationDataFormat = (String) valueInfo.get(SERIALIZATION_DATA_FORMAT);
    if (serializationDataFormat != null) {
      objectValue.setSerializationDataFormat(serializationDataFormat);
    }

    Boolean isTransient = (Boolean) valueInfo.get(TRANSIENT);
    if (isTransient != null) {
      objectValue.setTransient(isTransient);
    }

    return objectValue;
  }

  @Override
  public TypedValueDto serializeTypedValue(TypedValue typedValue) {
    ObjectValue objectValue = (ObjectValue) typedValue;

    TypedValueDto typedValueDto = ValueMapper.super.serializeTypedValue(typedValue);

    Map<String, Object> valueInfo = typedValueDto.getValueInfo();
    valueInfo.put(OBJECT_TYPE_NAME, objectValue.getValue().getClass());
    typedValueDto.setValueInfo(valueInfo);
    typedValueDto.setValue(objectValue.getValueSerialized());

    return typedValueDto;
  }

  public ObjectValue convertToObjectValue(ObjectValue objectValue) {
    ObjectValueImpl serializedObjectValue = (ObjectValueImpl) objectValue;

    Object value = objectValue.getValue();

    String serializedObject = null;
    try {
      serializedObject = objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw LOG.unsupportedTypeException(objectValue);
    }

    serializedObjectValue.setSerializedValue(serializedObject);

    String className = value.getClass().getName();
    serializedObjectValue.setObjectTypeName(className);

    String serializationDataFormat = Variables.SerializationDataFormats.JSON.getName();
    serializedObjectValue.setSerializationDataFormat(serializationDataFormat);

    serializedObjectValue.setTransient(objectValue.isTransient());

    return serializedObjectValue;
  }

}
