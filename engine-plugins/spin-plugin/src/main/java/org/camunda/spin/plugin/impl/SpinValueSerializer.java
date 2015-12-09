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
package org.camunda.spin.plugin.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.impl.variable.serializer.AbstractSerializableValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.type.SerializableValueType;
import org.camunda.spin.Spin;
import org.camunda.spin.plugin.variable.value.SpinValue;
import org.camunda.spin.plugin.variable.value.impl.SpinValueImpl;
import org.camunda.spin.spi.DataFormat;

/**
 * @author Roman Smirnov
 *
 */
public abstract class SpinValueSerializer extends AbstractSerializableValueSerializer<SpinValue> {

  protected DataFormat<?> dataFormat;
  protected String name;

  public SpinValueSerializer(SerializableValueType type, DataFormat<?> dataFormat, String name) {
    super(type, dataFormat.getName());
    this.dataFormat = dataFormat;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  protected void writeToValueFields(SpinValue value, ValueFields valueFields, byte[] serializedValue) {
    valueFields.setByteArrayValue(serializedValue);
  }

  protected void updateTypedValue(SpinValue value, String serializedStringValue) {
    SpinValueImpl spinValue = (SpinValueImpl) value;
    spinValue.setValueSerialized(serializedStringValue);
    spinValue.setSerializationDataFormat(serializationDataFormat);
  }

  protected boolean canSerializeValue(Object value) {
    if (value instanceof Spin<?>) {
      Spin<?> wrapper = (Spin<?>) value;
      return wrapper.getDataFormatName().equals(serializationDataFormat);
    }

    return false;
  }

  protected byte[] serializeToByteArray(Object deserializedObject) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    OutputStreamWriter outWriter = new OutputStreamWriter(out, Context.getProcessEngineConfiguration().getDefaultCharset());
    BufferedWriter bufferedWriter = new BufferedWriter(outWriter);

    try {
      Spin<?> wrapper = (Spin<?>) deserializedObject;
      wrapper.writeToWriter(bufferedWriter);
      return out.toByteArray();
    }
    finally {
      IoUtil.closeSilently(out);
      IoUtil.closeSilently(outWriter);
      IoUtil.closeSilently(bufferedWriter);
    }
  }

  protected Object deserializeFromByteArray(byte[] object, ValueFields valueFields) throws Exception {
    ByteArrayInputStream bais = new ByteArrayInputStream(object);
    InputStreamReader inReader = new InputStreamReader(bais, Context.getProcessEngineConfiguration().getDefaultCharset());
    BufferedReader bufferedReader = new BufferedReader(inReader);

    try {
      Object wrapper = dataFormat.getReader().readInput(bufferedReader);
      return dataFormat.createWrapperInstance(wrapper);
    }
    finally{
      IoUtil.closeSilently(bais);
      IoUtil.closeSilently(inReader);
      IoUtil.closeSilently(bufferedReader);
    }

  }

  protected boolean isSerializationTextBased() {
    return true;
  }

}
