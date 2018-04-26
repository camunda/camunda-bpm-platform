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
package org.camunda.spin.plugin.variable.value.impl;

import static org.camunda.spin.Spin.S;

import org.camunda.bpm.engine.variable.impl.value.AbstractTypedValue;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.spin.DataFormats;
import org.camunda.spin.Spin;
import org.camunda.spin.plugin.variable.type.SpinValueType;
import org.camunda.spin.plugin.variable.value.SpinValue;
import org.camunda.spin.spi.DataFormat;

/**
 * @author Roman Smirnov
 *
 */
public abstract class SpinValueImpl extends AbstractTypedValue<Spin<?>> implements SpinValue {

  private static final long serialVersionUID = 1L;
  protected String serializedValue;
  protected boolean isDeserialized;
  protected String dataFormatName;

  public SpinValueImpl(
      Spin<?> value,
      String serializedValue,
      String dataFormatName,
      boolean isDeserialized,
      ValueType type,
      boolean isTransient) {

    super(value, type);

    this.serializedValue = serializedValue;
    this.dataFormatName = dataFormatName;
    this.isDeserialized = isDeserialized;
    this.isTransient = isTransient;
  }

  public Spin<?> getValue() {
    if(isDeserialized) {
      return super.getValue();
    }
    else {
      // deserialize the serialized value by using
      // the given data format
      value = S(getValueSerialized(), getSerializationDataFormat());
      isDeserialized = true;

      setValueSerialized(null);

      return value;
    }
  }

  public SpinValueType getType() {
    return (SpinValueType) super.getType();
  }

  public boolean isDeserialized() {
    return isDeserialized;
  }

  public String getValueSerialized() {
    return serializedValue;
  }

  public void setValueSerialized(String serializedValue) {
    this.serializedValue = serializedValue;
  }

  public String getSerializationDataFormat() {
    return dataFormatName;
  }

  public void setSerializationDataFormat(String serializationDataFormat) {
    this.dataFormatName = serializationDataFormat;
  }

  public DataFormat<? extends Spin<?>> getDataFormat() {
    if(isDeserialized) {
      return DataFormats.getDataFormat(dataFormatName);
    }
    else {
      throw new IllegalStateException("Spin value is not deserialized.");
    }
  }

}
