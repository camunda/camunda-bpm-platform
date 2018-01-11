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
package org.camunda.bpm.engine.variable.impl.type;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.type.SerializableValueType;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.engine.variable.value.SerializableValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.engine.variable.value.builder.ObjectValueBuilder;
import org.camunda.bpm.engine.variable.value.builder.SerializedObjectValueBuilder;

/**
 * @author Daniel Meyer
 *
 */
public class ObjectTypeImpl extends AbstractValueTypeImpl implements SerializableValueType {

  private static final long serialVersionUID = 1L;

  public static final String TYPE_NAME = "object";

  public ObjectTypeImpl() {
    super(TYPE_NAME);
  }

  public boolean isPrimitiveValueType() {
    return false;
  }

  public TypedValue createValue(Object value, Map<String, Object> valueInfo) {
    ObjectValueBuilder builder = Variables.objectValue(value);

    if(valueInfo != null) {
      applyValueInfo(builder, valueInfo);
    }

    return builder.create();
  }

  public Map<String, Object> getValueInfo(TypedValue typedValue) {
    if(!(typedValue instanceof ObjectValue)) {
      throw new IllegalArgumentException("Value not of type Object.");
    }
    ObjectValue objectValue = (ObjectValue) typedValue;

    Map<String, Object> valueInfo = new HashMap<String, Object>();

    String serializationDataFormat = objectValue.getSerializationDataFormat();
    if(serializationDataFormat != null) {
      valueInfo.put(VALUE_INFO_SERIALIZATION_DATA_FORMAT, serializationDataFormat);
    }

    String objectTypeName = objectValue.getObjectTypeName();
    if(objectTypeName != null) {
      valueInfo.put(VALUE_INFO_OBJECT_TYPE_NAME, objectTypeName);
    }

    if (objectValue.isTransient()) {
      valueInfo.put(VALUE_INFO_TRANSIENT, objectValue.isTransient());
    }

    return valueInfo;
  }

  public SerializableValue createValueFromSerialized(String serializedValue, Map<String, Object> valueInfo) {
    SerializedObjectValueBuilder builder = Variables.serializedObjectValue(serializedValue);

    if(valueInfo != null) {
      applyValueInfo(builder, valueInfo);
    }

    return builder.create();
  }

  protected void applyValueInfo(ObjectValueBuilder builder, Map<String, Object> valueInfo) {

    String objectValueTypeName = (String) valueInfo.get(VALUE_INFO_OBJECT_TYPE_NAME);
    if (builder instanceof SerializedObjectValueBuilder) {
      ((SerializedObjectValueBuilder) builder).objectTypeName(objectValueTypeName);
    }

    String serializationDataFormat = (String) valueInfo.get(VALUE_INFO_SERIALIZATION_DATA_FORMAT);
    if(serializationDataFormat != null) {
      builder.serializationDataFormat(serializationDataFormat);
    }

    builder.setTransient(isTransient(valueInfo));
  }

}
