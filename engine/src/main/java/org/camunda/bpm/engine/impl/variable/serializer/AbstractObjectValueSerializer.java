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
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.value.ObjectValueImpl;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.ObjectValue;

/**
 * Abstract implementation of a {@link TypedValueSerializer} for {@link ObjectValue ObjectValues}.
 *
 * @author Daniel Meyer
 *
 */
public abstract class AbstractObjectValueSerializer extends AbstractSerializableValueSerializer<ObjectValue> {

  public AbstractObjectValueSerializer(String serializationDataFormat) {
    super(ValueType.OBJECT, serializationDataFormat);
  }

  public ObjectValue convertToTypedValue(UntypedValueImpl untypedValue) {
    // untyped values are always deserialized
    return Variables.objectValue(untypedValue.getValue()).create();
  }

  protected void writeToValueFields(ObjectValue value, ValueFields valueFields, byte[] serializedValue) {
    String objectTypeName = getObjectTypeName(value, valueFields);
    valueFields.setByteArrayValue(serializedValue);
    valueFields.setTextValue2(objectTypeName);
  }

  protected String getObjectTypeName(ObjectValue value, ValueFields valueFields) {
    String objectTypeName = value.getObjectTypeName();

    if (objectTypeName == null && !value.isDeserialized() && value.getValueSerialized() != null) {
      throw new ProcessEngineException("Cannot write serialized value for variable '" + valueFields.getName() + "': no 'objectTypeName' provided for non-null value.");
    }

    // update type name if the object is deserialized
    if (value.isDeserialized() && value.getValue() != null) {
      objectTypeName = getTypeNameForDeserialized(value.getValue());
    }

    return objectTypeName;
  }

  protected void updateTypedValue(ObjectValue value, String serializedStringValue) {
    String objectTypeName = getObjectTypeName(value, null);
    ObjectValueImpl objectValue =(ObjectValueImpl) value;
    objectValue.setObjectTypeName(objectTypeName);
    objectValue.setSerializedValue(serializedStringValue);
    objectValue.setSerializationDataFormat(serializationDataFormat);
  }

  protected ObjectValue createDeserializedValue(Object deserializedObject, String serializedStringValue, ValueFields valueFields) {
    String objectTypeName = readObjectNameFromFields(valueFields);
    return new ObjectValueImpl(deserializedObject, serializedStringValue, serializationDataFormat, objectTypeName, true);
  }


  protected ObjectValue createSerializedValue(String serializedStringValue, ValueFields valueFields) {
    String objectTypeName = readObjectNameFromFields(valueFields);
    return new ObjectValueImpl(null, serializedStringValue, serializationDataFormat, objectTypeName, false);
  }

  protected String readObjectNameFromFields(ValueFields valueFields) {
    return valueFields.getTextValue2();
  }

  public boolean isMutableValue(ObjectValue typedValue) {
    return typedValue.isDeserialized();
  }

  // methods to be implemented by subclasses ////////////

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

  protected Object deserializeFromByteArray(byte[] object, ValueFields valueFields) throws Exception {
    String objectTypeName = readObjectNameFromFields(valueFields);
    return deserializeFromByteArray(object, objectTypeName);
  }

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
