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
import org.camunda.bpm.engine.variable.impl.value.ObjectValueImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.Map;

/**
 * @author Tassilo Weidner
 */
public class ObjectValueMapper implements ValueMapper<ObjectValue> {

  protected static final ExternalTaskClientLogger LOG = ExternalTaskClientLogger.CLIENT_LOGGER;

  protected static final String OBJECT_TYPE_NAME = "objectTypeName";
  protected static final String SERIALIZATION_DATA_FORMAT = "serializationDataFormat";
  protected static final String TRANSIENT = "transient";

  public static final String SERIALIZATION_DATA_FORMAT_JSON = "application/json";
  public static final String SERIALIZATION_DATA_FORMAT_JAVA = "application/x-java-serialized-object";

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
    }
    catch (ClassNotFoundException e) {
      throw new EngineClientException(e);
    }

    String serializationDataFormat = (String) valueInfo.get(SERIALIZATION_DATA_FORMAT);
    Object deserializedValue = null;

    switch (serializationDataFormat) {
      case SERIALIZATION_DATA_FORMAT_JSON:
        deserializedValue = fromJson(serializedValue, type);
        break;

      case SERIALIZATION_DATA_FORMAT_JAVA:
        deserializedValue = fromJavaBase64(serializedValue);
        break;

      default:
        throw LOG.unsupportedSerializationDataFormat(typedValueDto);
    }

    ObjectValueImpl objectValue = new ObjectValueImpl(deserializedValue);

    objectValue.setSerializedValue(serializedValue);
    objectValue.setSerializationDataFormat(serializationDataFormat);

    String objectTypeName = (String) valueInfo.get(OBJECT_TYPE_NAME);
    objectValue.setObjectTypeName(objectTypeName);

    Boolean isTransient = (Boolean) valueInfo.get(TRANSIENT);
    if (isTransient != null) {
      objectValue.setTransient(isTransient);
    }

    return objectValue;
  }

  protected Object fromJson(String serializedValue, Class<?> type) throws EngineClientException {
    try {
      return objectMapper.readValue(serializedValue, type);
    }
    catch (IOException e) {
      throw new EngineClientException(e.getMessage()); // avoid exceptions of third party components
    }
  }

  protected Object fromJavaBase64(String serializedValue) throws EngineClientException {
    byte[] base64DecodedSerializedValue = Base64.getDecoder().decode(serializedValue);
    try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(base64DecodedSerializedValue))) {
      return objectInputStream.readObject();
    }
    catch (IOException | ClassNotFoundException e) {
      throw new EngineClientException(e);
    }
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

    String serializationDataFormat = serializedObjectValue.getSerializationDataFormat();
    if (serializationDataFormat == null) {
      serializationDataFormat = SERIALIZATION_DATA_FORMAT_JSON; // default serialization format
      serializedObjectValue.setSerializationDataFormat(serializationDataFormat);
    }

    String serializedObject = null;
    switch (serializationDataFormat) {
      case SERIALIZATION_DATA_FORMAT_JSON:
        serializedObject = toJson(objectValue);
        break;

      case SERIALIZATION_DATA_FORMAT_JAVA:
        serializedObject = toJavaBase64(objectValue);
      break;

      default:
        throw LOG.unsupportedSerializationDataFormat(objectValue);
    }

    serializedObjectValue.setSerializedValue(serializedObject);

    String className = objectValue.getValue().getClass().getName();
    serializedObjectValue.setObjectTypeName(className);

    serializedObjectValue.setTransient(objectValue.isTransient());

    return serializedObjectValue;
  }

  protected String toJavaBase64(ObjectValue objectValue) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
      objectOutputStream.writeObject(objectValue.getValue());
    }
    catch (IOException e) {
      throw LOG.unsupportedTypeException(objectValue);
    }

    return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
  }

  protected String toJson(ObjectValue objectValue) {
    try {
      return objectMapper.writeValueAsString(objectValue.getValue());
    }
    catch (JsonProcessingException e) {
      throw LOG.unsupportedTypeException(objectValue);
    }
  }

}
