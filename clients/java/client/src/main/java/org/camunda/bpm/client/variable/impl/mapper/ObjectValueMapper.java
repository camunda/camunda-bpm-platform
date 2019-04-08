/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.client.variable.impl.mapper;

import static org.camunda.bpm.engine.variable.type.SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME;
import static org.camunda.bpm.engine.variable.type.SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT;
import static org.camunda.bpm.engine.variable.type.ValueType.OBJECT;

import java.util.Map;

import org.camunda.bpm.client.impl.ExternalTaskClientLogger;
import org.camunda.bpm.client.spi.DataFormat;
import org.camunda.bpm.client.variable.impl.AbstractTypedValueMapper;
import org.camunda.bpm.client.variable.impl.TypedValueField;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.value.ObjectValueImpl;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.SerializableValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class ObjectValueMapper extends AbstractTypedValueMapper<ObjectValue> {

  protected static final ExternalTaskClientLogger LOG = ExternalTaskClientLogger.CLIENT_LOGGER;

  protected DataFormat dataFormat;
  protected String serializationDataFormat;

  public ObjectValueMapper(String serializationDataFormat, DataFormat dataFormat) {
    super(OBJECT);
    this.serializationDataFormat = serializationDataFormat;
    this.dataFormat = dataFormat;
  }

  public String getSerializationDataformat() {
    return serializationDataFormat;
  }

  public void writeValue(ObjectValue typedValue, TypedValueField typedValueField) {
    String serializedStringValue = typedValue.getValueSerialized();

    if(typedValue.isDeserialized()) {
      Object objectToSerialize = typedValue.getValue();
      if(objectToSerialize != null) {
        try {
          serializedStringValue = dataFormat.writeValue(objectToSerialize);
        }
        catch (Exception e) {
          throw LOG.valueMapperExceptionWhileSerializingObject(e);
        }
      }
    }

    writeSerializedValue(typedValueField, serializedStringValue);
    updateTypedValue(typedValue, serializedStringValue);
  }

  public ObjectValue readValue(TypedValueField value, boolean deserializeObjectValue) {
    String serializedStringValue = (String) value.getValue();

    if(deserializeObjectValue) {

      Object deserializedObject = null;
      if(serializedStringValue != null) {
        try {
          String objectTypeName = readObjectNameFromFields(value);
          deserializedObject = dataFormat.readValue(serializedStringValue, objectTypeName);
        }
        catch (Exception e) {
          throw LOG.valueMapperExceptionWhileDeserializingObject(e);
        }
      }

      return createDeserializedValue(deserializedObject, serializedStringValue, value);

    }
    else {
      return createSerializedValue(serializedStringValue, value);
    }
  }

  public ObjectValue convertToTypedValue(UntypedValueImpl untypedValue) {
    return Variables.objectValue(untypedValue.getValue()).create();
  }

  protected void writeSerializedValue(TypedValueField typedValueField, String serializedValue) {
    typedValueField.setValue(serializedValue);
  }

  protected void updateTypedValue(ObjectValue value, String serializedValue) {
    String objectTypeName = getObjectTypeName(value);
    ObjectValueImpl objectValue = (ObjectValueImpl) value;
    objectValue.setObjectTypeName(objectTypeName);
    objectValue.setSerializedValue(serializedValue);
    objectValue.setSerializationDataFormat(serializationDataFormat);
  }

  protected String getObjectTypeName(ObjectValue value) {
    String objectTypeName = value.getObjectTypeName();

    if (objectTypeName == null && !value.isDeserialized() && value.getValueSerialized() != null) {
      throw LOG.valueMapperExceptionDueToNoObjectTypeName();
    }

    // update type name if the object is deserialized
    if (value.isDeserialized() && value.getValue() != null) {
      objectTypeName = dataFormat.getCanonicalTypeName(value.getValue());
    }

    return objectTypeName;
  }

  @Override
  protected boolean canWriteValue(TypedValue typedValue) {
    if (!(typedValue instanceof SerializableValue) && !(typedValue instanceof UntypedValueImpl)) {
      return false;
    }

    if (typedValue instanceof SerializableValue) {
      SerializableValue serializableValue = (SerializableValue) typedValue;
      String requestedDataFormat = serializableValue.getSerializationDataFormat();
      if (!serializableValue.isDeserialized()) {
        // serialized object => dataformat must match
        return serializationDataFormat.equals(requestedDataFormat);
      } else {
        final boolean canSerialize = typedValue.getValue() == null || dataFormat.canMap(typedValue.getValue());
        return canSerialize && (requestedDataFormat == null || serializationDataFormat.equals(requestedDataFormat));
      }
    } else {
      return typedValue.getValue() == null || dataFormat.canMap(typedValue.getValue());
    }
  }

  @Override
  protected boolean canReadValue(TypedValueField typedValueField) {
    Map<String, Object> valueInfo = typedValueField.getValueInfo();
    String serializationDataformat = (String) valueInfo.get(VALUE_INFO_SERIALIZATION_DATA_FORMAT);
    Object value = typedValueField.getValue();
    return (value == null || value instanceof String) && getSerializationDataformat().equals(serializationDataformat);
  }

  protected ObjectValue createDeserializedValue(Object deserializedObject, String serializedValue, TypedValueField typedValueField) {
    String objectTypeName = readObjectNameFromFields(typedValueField);
    return new ObjectValueImpl(deserializedObject, serializedValue, serializationDataFormat, objectTypeName, true);
  }

  protected ObjectValue createSerializedValue(String serializedValue, TypedValueField typedValueField) {
    String objectTypeName = readObjectNameFromFields(typedValueField);
    return new ObjectValueImpl(null, serializedValue, serializationDataFormat, objectTypeName, false);
  }

  protected String readObjectNameFromFields(TypedValueField typedValueField) {
    Map<String, Object> valueInfo = typedValueField.getValueInfo();
    return (String) valueInfo.get(VALUE_INFO_OBJECT_TYPE_NAME);
  }

}
