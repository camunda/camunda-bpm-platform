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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.client.impl.EngineClientException;
import org.camunda.bpm.client.impl.EngineClientLogger;
import org.camunda.bpm.client.impl.ExternalTaskClientLogger;
import org.camunda.bpm.client.impl.variable.mapper.ValueMapper;
import org.camunda.bpm.client.task.impl.dto.TypedValueDto;
import org.camunda.bpm.engine.variable.impl.value.ObjectValueImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;
import java.util.Map;

/**
 * @author Tassilo Weidner
 */
public class ObjectValueMapper implements ValueMapper<ObjectValue> {

  protected static final EngineClientLogger INTERNAL_LOG = ExternalTaskClientLogger.ENGINE_CLIENT_LOGGER;
  protected static final ExternalTaskClientLogger USER_LOG = ExternalTaskClientLogger.CLIENT_LOGGER;

  protected static final String OBJECT_TYPE_NAME = "objectTypeName";
  protected static final String SERIALIZATION_DATA_FORMAT = "serializationDataFormat";
  protected static final String TRANSIENT = "transient";

  protected static final String SERIALIZATION_DATA_FORMAT_JSON = "application/json";
  protected static final String SERIALIZATION_DATA_FORMAT_XML = "application/xml";
  protected static final String SERIALIZATION_DATA_FORMAT_JAVA = "application/x-java-serialized-object";

  protected ObjectMapper objectMapper;
  protected Class<?> spinDataFormatsClass;

  public ObjectValueMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.spinDataFormatsClass = reflectSpinDataFormatsClazz();
  }

  @Override
  public String getTypeName() {
    return ValueType.OBJECT.getName();
  }

  @Override
  public ObjectValue deserializeTypedValue(TypedValueDto typedValueDto) throws EngineClientException {
    Map<String, Object> valueInfo = typedValueDto.getValueInfo();

    String serializedValue = (String) typedValueDto.getValue();
    String objectTypeName = (String) valueInfo.get(OBJECT_TYPE_NAME);
    Class<?> type = null;
    try {
      type = Class.forName(objectTypeName);
    }
    catch (ClassNotFoundException e) {
      throw INTERNAL_LOG.objectTypeNameUnknownException(objectTypeName, serializedValue);
    }

    String serializationDataFormat = (String) valueInfo.get(SERIALIZATION_DATA_FORMAT);
    Object deserializedValue = null;

    switch (serializationDataFormat) {
      case SERIALIZATION_DATA_FORMAT_JSON:
        deserializedValue = fromJson(serializedValue, type);
        break;

      case SERIALIZATION_DATA_FORMAT_XML:
        deserializedValue = fromXml(serializedValue, type);
        break;

      case SERIALIZATION_DATA_FORMAT_JAVA:
        deserializedValue = fromJavaBase64(serializedValue, type);
        break;

      default:
        throw INTERNAL_LOG.unsupportedSerializationDataFormatException(serializationDataFormat, typedValueDto);
    }

    ObjectValueImpl objectValue = new ObjectValueImpl(deserializedValue);

    objectValue.setSerializedValue(serializedValue);
    objectValue.setSerializationDataFormat(serializationDataFormat);

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
    catch (JsonParseException | JsonMappingException e) {
      throw INTERNAL_LOG.invalidSerializedValueException(serializedValue, e.getMessage()); // avoid exceptions of third party components
    }
    catch (IOException e) {
      throw INTERNAL_LOG.invalidSerializedValueException(serializedValue, e.toString());
    }
  }

  protected Object fromXml(String serializedValue, Class<?> type) throws EngineClientException {
    if (spinDataFormatsClass == null) {
      throw INTERNAL_LOG.missingSpinXmlDependencyExceptionInternal();
    }

    try {
      Object xmlDataFormat = reflectXmlDataFormat();
      Object reader = xmlDataFormat.getClass()
        .getMethod("getReader")
        .invoke(xmlDataFormat);

      Object mappedObject = null;
      try (StringReader stringReader = new StringReader(serializedValue)) {
        try (BufferedReader bufferedReader = new BufferedReader(stringReader)) {
          mappedObject = reader.getClass()
            .getMethod("readInput", Reader.class)
            .invoke(reader, bufferedReader);
        }
        catch (IOException e) {
          throw new EngineClientException(e); // unable to close resource
        }
      }

      Object mapper = reflectXmlMapper(xmlDataFormat);
      return mapper.getClass()
        .getMethod("mapInternalToJava", Object.class, String.class)
        .invoke(mapper, mappedObject, type.getTypeName());

    }
    catch (InvocationTargetException e) {
      throw INTERNAL_LOG.invalidSerializedValueException(serializedValue, e.getTargetException().toString());
    }
    catch (IllegalAccessException | NoSuchMethodException e) {
      throw INTERNAL_LOG.invalidSerializedValueException(serializedValue, e.toString()); // reflection problem
    }
  }

  protected Object fromJavaBase64(String serializedValue, Class<?> type) throws EngineClientException {
    byte[] base64DecodedSerializedValue = Base64.getDecoder().decode(serializedValue);
    try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(base64DecodedSerializedValue))) {
      return objectInputStream.readObject();
    }
    catch (IOException e) {
      throw INTERNAL_LOG.invalidSerializedValueException(serializedValue, e.toString());
    }
    catch (ClassNotFoundException e) {
      throw INTERNAL_LOG.objectTypeNameUnknownException(type.getTypeName(), serializedValue);
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

      case SERIALIZATION_DATA_FORMAT_XML:
        serializedObject = toXml(objectValue);
        break;

      case SERIALIZATION_DATA_FORMAT_JAVA:
        serializedObject = toJavaBase64(objectValue);
      break;

      default:
        throw USER_LOG.unsupportedSerializationDataFormat(objectValue);
    }

    serializedObjectValue.setSerializedValue(serializedObject);

    String className = objectValue.getValue().getClass().getName();
    serializedObjectValue.setObjectTypeName(className);

    serializedObjectValue.setTransient(objectValue.isTransient());

    return serializedObjectValue;
  }

  protected String toJson(ObjectValue objectValue) {
    try {
      return objectMapper.writeValueAsString(objectValue.getValue());
    }
    catch (JsonProcessingException e) {
      throw USER_LOG.unknownTypeException(objectValue);
    }
  }

  protected String toXml(ObjectValue objectValue) {
    if (spinDataFormatsClass == null) {
      throw USER_LOG.missingSpinXmlDependencyException();
    }

    try {

      Object xmlDataFormat = reflectXmlDataFormat();
      Object mapper = reflectXmlMapper(xmlDataFormat);

      Object object = mapper.getClass()
        .getMethod("mapJavaToInternal", Object.class)
        .invoke(mapper, objectValue.getValue());

      Object writer = xmlDataFormat.getClass()
        .getMethod("getWriter")
        .invoke(xmlDataFormat);

      StringWriter valueWriter = new StringWriter();

      writer.getClass()
        .getMethod("writeToWriter", Writer.class, Object.class)
        .invoke(writer, valueWriter, object);

      return valueWriter.toString();

    }
    catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
      throw USER_LOG.unknownTypeException(objectValue);
    }
  }

  protected String toJavaBase64(ObjectValue objectValue) {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
      objectOutputStream.writeObject(objectValue.getValue());
    }
    catch (IOException e) {
      throw USER_LOG.unknownTypeException(objectValue);
    }

    return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
  }

  protected Class<?> reflectSpinDataFormatsClazz() {
    try {
      return Class.forName("org.camunda.spin.DataFormats");
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  protected Object reflectXmlDataFormat() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    return spinDataFormatsClass
      .getMethod("getDataFormat", String.class)
      .invoke(null, SERIALIZATION_DATA_FORMAT_XML);
  }

  protected Object reflectXmlMapper(Object xmlDataFormat) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    return xmlDataFormat.getClass()
      .getMethod("getMapper")
      .invoke(xmlDataFormat);
  }

}
