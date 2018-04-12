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

import org.camunda.bpm.client.impl.variable.TypedValueField;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.value.ObjectValueImpl;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.SerializableValueType;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.ObjectValue;

public abstract class AbstractObjectMapper extends AbstractSerializableValueMapper<ObjectValue> {

  public AbstractObjectMapper(String serializationDataFormat) {
    super(ValueType.OBJECT, serializationDataFormat);
  }

  public ObjectValue convertToTypedValue(UntypedValueImpl untypedValue) {
    return Variables.objectValue(untypedValue.getValue()).create();
  }

  protected String getObjectTypeName(ObjectValue value) {
    String objectTypeName = value.getObjectTypeName();

    if (objectTypeName == null && !value.isDeserialized() && value.getValueSerialized() != null) {
      throw new RuntimeException("Cannot write serialized value for variable: no 'objectTypeName' provided for non-null value.");
    }

    // update type name if the object is deserialized
    if (value.isDeserialized() && value.getValue() != null) {
      objectTypeName = getTypeNameForDeserialized(value.getValue());
    }

    return objectTypeName;
  }

  protected void updateTypedValue(ObjectValue value, String serializedValue) {
    String objectTypeName = getObjectTypeName(value);
    ObjectValueImpl objectValue = (ObjectValueImpl) value;
    objectValue.setObjectTypeName(objectTypeName);
    objectValue.setSerializedValue(serializedValue);
    objectValue.setSerializationDataFormat(serializationDataFormat);
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
    return (String) valueInfo.get(SerializableValueType.VALUE_INFO_OBJECT_TYPE_NAME);
  }

  // methods to be implemented by subclasses ////////////

  protected abstract String getTypeNameForDeserialized(Object deserializedObject);

  protected Object deserializeFromString(String serializedValue, TypedValueField typedValueField) throws Exception {
    String objectTypeName = readObjectNameFromFields(typedValueField);
    return deserializeFromString(serializedValue, objectTypeName);
  }

  protected abstract Object deserializeFromString(String serializedValue, String objectTypeName) throws Exception;

}
