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

import java.util.Map;

import org.camunda.bpm.client.impl.ExternalTaskClientLogger;
import org.camunda.bpm.client.impl.variable.TypedValueField;
import org.camunda.bpm.client.impl.variable.mapper.AbstractTypedValueMapper;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.SerializableValueType;
import org.camunda.bpm.engine.variable.value.SerializableValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public abstract class AbstractSerializableValueMapper<T extends SerializableValue> extends AbstractTypedValueMapper<T> {

  protected static final ExternalTaskClientLogger LOG = ExternalTaskClientLogger.CLIENT_LOGGER;

  protected String serializationDataFormat;

  public AbstractSerializableValueMapper(SerializableValueType type, String serializationDataFormat) {
    super(type);
    this.serializationDataFormat = serializationDataFormat;
  }

  public String getSerializationDataformat() {
    return serializationDataFormat;
  }

  public void writeValue(T typedValue, TypedValueField typedValueField) {
    String serializedStringValue = typedValue.getValueSerialized();

    if(typedValue.isDeserialized()) {
      Object objectToSerialize = typedValue.getValue();
      if(objectToSerialize != null) {
        try {
          serializedStringValue = serializeToString(objectToSerialize);
        } catch (Exception e) {
          throw LOG.valueMapperExceptionWhileSerializingObject(e);
        }
      }
    }

    writeSerializedValue(typedValueField, serializedStringValue);
    updateTypedValue(typedValue, serializedStringValue);
  }

  public T readValue(TypedValueField typedValueField, boolean deserializeObjectValue) {
    String serializedStringValue = (String) typedValueField.getValue();

    if(deserializeObjectValue) {

      Object deserializedObject = null;
      if(serializedStringValue != null) {
        try {
          deserializedObject = deserializeFromString(serializedStringValue, typedValueField);
        } catch (Exception e) {
          throw LOG.valueMapperExceptionWhileDeserializingObject(e);
        }
      }

      T value = createDeserializedValue(deserializedObject, serializedStringValue, typedValueField);
      return value;

    }
    else {
      return createSerializedValue(serializedStringValue, typedValueField);
    }
  }

  protected void writeSerializedValue(TypedValueField typedValueField, String serializedValue) {
    typedValueField.setValue(serializedValue);
  }

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
        final boolean canSerialize = typedValue.getValue() == null || canSerializeValue(typedValue.getValue());
        return canSerialize && (requestedDataFormat == null || serializationDataFormat.equals(requestedDataFormat));
      }
    } else {
      return typedValue.getValue() == null || canSerializeValue(typedValue.getValue());
    }
  }

  protected boolean canReadValue(TypedValueField typedValueField) {
    Map<String, Object> valueInfo = typedValueField.getValueInfo();
    String serializationDataformat = (String) valueInfo.get(SerializableValueType.VALUE_INFO_SERIALIZATION_DATA_FORMAT);
    Object value = typedValueField.getValue();
    return (value == null || value instanceof String) && getSerializationDataformat().equals(serializationDataformat);
  }

  // methods to be implemented by subclasses ////////////

  protected abstract boolean canSerializeValue(Object value);

  protected abstract String serializeToString(Object deserializedObject) throws Exception;

  protected abstract void updateTypedValue(T value, String serializedValue);

  protected abstract T createDeserializedValue(Object deserializedObject, String serializedValue, TypedValueField typedValueField);

  protected abstract T createSerializedValue(String serializedValue, TypedValueField typedValueField);

  protected abstract Object deserializeFromString(String serializedValue, TypedValueField typedValueField) throws Exception;

}
