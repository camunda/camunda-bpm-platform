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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.core.variable.value.ObjectValueImpl;
import org.camunda.bpm.engine.impl.core.variable.value.UntypedValueImpl;
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * Abstract implementation of a {@link TypedValueSerializer} for {@link ObjectValue ObjectValues}.
 *
 * @author Daniel Meyer
 *
 */
public abstract class AbstractObjectValueSerializer extends AbstractTypedValueSerializer<ObjectValue> {

  protected String serializationDataFormat;

  public AbstractObjectValueSerializer(String serializationDataFormat) {
    super(ValueType.OBJECT);
    this.serializationDataFormat = serializationDataFormat;
  }

  public String getSerializationDataformat() {
    return serializationDataFormat;
  }

  public ObjectValue convertToTypedValue(UntypedValueImpl untypedValue) {
    // untyped values are always deserialized
    return Variables.objectValue(untypedValue.getValue()).create();
  }

  public void writeValue(ObjectValue value, ValueFields valueFields) {

    ObjectValueImpl objectValue = (ObjectValueImpl) value;

    String objectTypeName = objectValue.getObjectTypeName();
    String serializedStringValue = objectValue.getValueSerialized();
    byte[] serializedByteValue = null;

    if(objectValue.isDeserialized()) {
      Object objectToSerialize = objectValue.getValue();
      if(objectToSerialize != null) {
        if(objectTypeName == null) {
          // detect a type name
          objectTypeName = getTypeNameForDeserialized(objectToSerialize);
        }
        // serialize to byte array
        try {
          serializedByteValue = serializeToByteArray(objectToSerialize);
          serializedStringValue = getSerializedStringValue(serializedByteValue);
          if(valueFields.getByteArrayValue() == null && objectToSerialize != null) {
            dirtyCheckOnFlush(objectToSerialize, serializedByteValue, valueFields);
          }
        } catch(Exception e) {
          throw new ProcessEngineException("Cannot serialize object in variable '"+valueFields.getName()+"': "+e.getMessage(), e);
        }
      }
    }
    else {
      if (serializedStringValue != null) {
        if (objectTypeName == null) {
          throw new ProcessEngineException("Cannot write serialized value for variable '" + valueFields.getName() + "': no 'objectTypeName' provided for non-null value.");
        }
        serializedByteValue = getSerializedBytesValue(serializedStringValue);
      }
    }

    // write value and type to fields.
    writeToValueFields(valueFields, objectTypeName, serializedByteValue);

    // update the ObjectValue to keep it consistent with value fields.
    updateObjectValue(objectValue, objectTypeName, serializedStringValue);
  }

  public ObjectValue readValue(ValueFields valueFields, boolean deserializeObjectValue) {

    byte[] serializedByteValue = readSerializedValueFromFields(valueFields);
    String serializedStringValue = getSerializedStringValue(serializedByteValue);
    String objectTypeName = readObjectNameFromFields(valueFields);


    if(deserializeObjectValue) {
      Object deserializedObject = null;
      if(serializedByteValue != null) {
        try {
          deserializedObject = deserializeFromByteArray(serializedByteValue, objectTypeName);
        } catch (Exception e) {
          throw new ProcessEngineException("Cannot deserialize object in variable '"+valueFields.getName()+"': "+e.getMessage(), e);
        }
      }
      ObjectValueImpl objectValue = new ObjectValueImpl(deserializedObject, serializedStringValue, serializationDataFormat, objectTypeName, true);
      if(deserializedObject != null) {
        dirtyCheckOnFlush(deserializedObject, serializedByteValue, valueFields);
      }
      return objectValue;
    }
    else {
      return new ObjectValueImpl(null, serializedStringValue, serializationDataFormat, objectTypeName, false);
    }
  }

  protected void writeToValueFields(ValueFields valueFields, String objectTypeName, byte[] serializedValue) {
    ByteArrayValueSerializer.setBytes(valueFields, serializedValue);
    valueFields.setTextValue2(objectTypeName);
  }

  protected void updateObjectValue(ObjectValueImpl objectValue, String objectTypeName, String serializedValue) {
    objectValue.setSerializedValue(serializedValue);
    objectValue.setSerializationDataFormat(serializationDataFormat);
    objectValue.setObjectTypeName(objectTypeName);
  }

  protected String readObjectNameFromFields(ValueFields valueFields) {
    return valueFields.getTextValue2();
  }

  protected byte[] readSerializedValueFromFields(ValueFields valueFields) {
    return ByteArrayValueSerializer.getBytes(valueFields);
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

    Object objectToSerialize = null;
    String requestedDataformat = null;

    if(typedValue instanceof UntypedValueImpl) {
      objectToSerialize = typedValue.getValue();
      requestedDataformat = null;
    }
    else if(typedValue instanceof ObjectValue) {
      ObjectValue objectValue = (ObjectValue) typedValue;
      String requestedDataFormat = objectValue.getSerializationDataFormat();

      if(!objectValue.isDeserialized()) {
        // serialized object => dataformat must match
        return serializationDataFormat.equals(requestedDataFormat);
      }
      else {
        objectToSerialize = typedValue.getValue();
        requestedDataformat = objectValue.getSerializationDataFormat();
      }
    } else {
      // not an object value
      return false;
    }

    boolean canSerialize = objectToSerialize == null || canSerializeObject(objectToSerialize);

    if(requestedDataformat != null) {
      if(requestedDataformat.equals(serializationDataFormat)) {
        return canSerialize;
      }
      else {
        return false;
      }
    }
    else {
      return canSerialize;
    }
  }

  protected void dirtyCheckOnFlush(Object deserializedObject, byte[] serializedValue, ValueFields valueFields) {
    // make sure changes to the object are flushed in case it is
    // further modified in the context of the same command
    if(valueFields instanceof VariableInstanceEntity) {
      Context
        .getCommandContext()
        .getSession(DeserializedObjectsSession.class)
        .addDeserializedObject(this, deserializedObject, serializedValue, (VariableInstanceEntity)valueFields);
    }
  }

  // methods to be implemented by subclasses ////////////

  /**
   * return true if this serializer is able to serialize the provided object.
   *
   * @param value the object to test (guaranteed to be a non-null value)
   * @return true if the serializer can handle the object.
   */
  protected abstract boolean canSerializeObject(Object value);

  /**
   * Returns the type name for the deserialized object.
   *
   * @param deserializedObject. Guaranteed not to be null
   * @return the type name fot the object.
   */
  protected abstract String getTypeNameForDeserialized(Object deserializedObject);

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
   * @param objectTypeName the type name of the object to deserialize
   * @return the deserialized object
   * @throws exception in case the object cannot be deserialized
   */
  protected abstract Object deserializeFromByteArray(byte[] object, String objectTypeName) throws Exception;

  /**
   * Return true if the serialization is text based. Return false otherwise
   *
   */
  protected abstract boolean isSerializationTextBased();

}
