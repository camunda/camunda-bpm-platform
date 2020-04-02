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
package org.camunda.bpm.engine.impl.variable.serializer;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.SerializableValueType;
import org.camunda.bpm.engine.variable.value.SerializableValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Roman Smirnov
 *
 */
public abstract class AbstractSerializableValueSerializer<T extends SerializableValue> extends AbstractTypedValueSerializer<T> {

  protected String serializationDataFormat;

  public AbstractSerializableValueSerializer(SerializableValueType type, String serializationDataFormat) {
    super(type);
    this.serializationDataFormat = serializationDataFormat;
  }

  public String getSerializationDataformat() {
    return serializationDataFormat;
  }

  public void writeValue(T value, ValueFields valueFields) {

    String serializedStringValue = value.getValueSerialized();
    byte[] serializedByteValue = null;

    if(value.isDeserialized()) {
      Object objectToSerialize = value.getValue();
      if(objectToSerialize != null) {
        // serialize to byte array
        try {
          serializedByteValue = serializeToByteArray(objectToSerialize);
          serializedStringValue = getSerializedStringValue(serializedByteValue);
        } catch(Exception e) {
          throw new ProcessEngineException("Cannot serialize object in variable '"+valueFields.getName()+"': "+e.getMessage(), e);
        }
      }
    }
    else {
      if (serializedStringValue != null) {
        serializedByteValue = getSerializedBytesValue(serializedStringValue);
      }
    }

    // write value and type to fields.
    writeToValueFields(value, valueFields, serializedByteValue);

    // update the ObjectValue to keep it consistent with value fields.
    updateTypedValue(value, serializedStringValue);
  }

  public T readValue(ValueFields valueFields, boolean deserializeObjectValue, boolean asTransientValue) {

    byte[] serializedByteValue = readSerializedValueFromFields(valueFields);
    String serializedStringValue = getSerializedStringValue(serializedByteValue);

    if(deserializeObjectValue) {
      Object deserializedObject = null;
      if(serializedByteValue != null) {
        try {
          deserializedObject = deserializeFromByteArray(serializedByteValue, valueFields);
        } catch (Exception e) {
          throw new ProcessEngineException("Cannot deserialize object in variable '"+valueFields.getName()+"': "+e.getMessage(), e);
        }
      }
      T value = createDeserializedValue(deserializedObject, serializedStringValue, valueFields, asTransientValue);
      return value;
    }
    else {
      return createSerializedValue(serializedStringValue, valueFields, asTransientValue);
    }
  }

  protected abstract T createDeserializedValue(Object deserializedObject, String serializedStringValue, ValueFields valueFields, boolean asTransientValue);

  protected abstract T createSerializedValue(String serializedStringValue, ValueFields valueFields, boolean asTransientValue);

  protected abstract void writeToValueFields(T value, ValueFields valueFields, byte[] serializedValue);

  protected abstract void updateTypedValue(T value, String serializedStringValue);

  protected byte[] readSerializedValueFromFields(ValueFields valueFields) {
    return valueFields.getByteArrayValue();
  }

  protected String getSerializedStringValue(byte[] serializedByteValue) {
    if(serializedByteValue != null) {
      if(!isSerializationTextBased()) {
        serializedByteValue = Base64.encodeBase64(serializedByteValue);
      }
      return StringUtil.fromBytes(serializedByteValue);
    }
    else {
      return null;
    }
  }

  protected byte[] getSerializedBytesValue(String serializedStringValue) {
    if(serializedStringValue != null) {
      byte[] serializedByteValue = StringUtil.toByteArray(serializedStringValue);
      if (!isSerializationTextBased()) {
        serializedByteValue = Base64.decodeBase64(serializedByteValue);
      }
      return serializedByteValue;
    }
    else {
      return null;
    }
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


  /**
   * return true if this serializer is able to serialize the provided object.
   *
   * @param value the object to test (guaranteed to be a non-null value)
   * @return true if the serializer can handle the object.
   */
  protected abstract boolean canSerializeValue(Object value);

  // methods to be implemented by subclasses ////////////

  /**
   * Implementations must return a byte[] representation of the provided object.
   * The object is guaranteed not to be null.
   *
   * @param deserializedObject the object to serialize
   * @return the byte array value of the object
   * @throws exception in case the object cannot be serialized
   */
  protected abstract byte[] serializeToByteArray(Object deserializedObject) throws Exception;

  /**
   * Deserialize the object from a byte array.
   *
   * @param object the object to deserialize
   * @param valueFields the value fields
   * @return the deserialized object
   * @throws exception in case the object cannot be deserialized
   */
  protected abstract Object deserializeFromByteArray(byte[] object, ValueFields valueFields) throws Exception;

  /**
   * Return true if the serialization is text based. Return false otherwise
   *
   */
  protected abstract boolean isSerializationTextBased();

}
