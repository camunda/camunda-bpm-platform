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

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;

import org.camunda.bpm.client.impl.variable.TypedValueField;
import org.camunda.bpm.engine.variable.type.SerializableValueType;
import org.camunda.commons.utils.IoUtil;
import org.camunda.spin.Spin;
import org.camunda.spin.plugin.variable.value.SpinValue;
import org.camunda.spin.plugin.variable.value.impl.SpinValueImpl;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.spi.DataFormatReader;

public abstract class SpinValueMapper<T extends SpinValue> extends AbstractSerializableValueMapper<T> {

  protected DataFormat<?> dataFormat;
  protected String name;

  public SpinValueMapper(SerializableValueType type, DataFormat<?> dataFormat, String name) {
    super(type, dataFormat.getName());
    this.dataFormat = dataFormat;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  protected void updateTypedValue(SpinValue value, String serializedValue) {
    SpinValueImpl spinValue = (SpinValueImpl) value;
    spinValue.setValueSerialized(serializedValue);
    spinValue.setSerializationDataFormat(serializationDataFormat);
  }

  @Override
  protected boolean canReadValue(TypedValueField typedValueField) {
    Object value = typedValueField.getValue();
    return value == null || value instanceof String;
  }

  protected boolean canSerializeValue(Object value) {
    if (value instanceof Spin<?>) {
      Spin<?> wrapper = (Spin<?>) value;
      String dataFormat = wrapper.getDataFormatName();
      return dataFormat.equals(serializationDataFormat);
    }

    return false;
  }

  protected String serializeToString(Object deserializedObject) throws Exception {
    StringWriter writer = new StringWriter();

    try {
      Spin<?> wrapper = (Spin<?>) deserializedObject;
      wrapper.writeToWriter(writer);
      return writer.toString();
    }
    finally {
      IoUtil.closeSilently(writer);
    }
  }

  protected Object deserializeFromString(String serializedValue, TypedValueField typedValueField) throws Exception {
    DataFormatReader reader = dataFormat.getReader();

    StringReader stringReader = new StringReader(serializedValue);
    BufferedReader bufferedReader = new BufferedReader(stringReader);

    try {
      Object mappedObject = reader.readInput(bufferedReader);
      return dataFormat.createWrapperInstance(mappedObject);
    }
    finally {
      IoUtil.closeSilently(bufferedReader);
      IoUtil.closeSilently(stringReader);
    }
  }

}
