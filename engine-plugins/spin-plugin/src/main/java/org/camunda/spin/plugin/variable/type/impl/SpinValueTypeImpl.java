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
package org.camunda.spin.plugin.variable.type.impl;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.AbstractValueTypeImpl;
import org.camunda.bpm.engine.variable.value.SerializableValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.spin.plugin.variable.type.SpinValueType;
import org.camunda.spin.plugin.variable.value.SpinValue;
import org.camunda.spin.plugin.variable.value.builder.SpinValueBuilder;

/**
 * @author Roman Smirnov
 *
 */
public abstract class SpinValueTypeImpl extends AbstractValueTypeImpl implements SpinValueType {

  private static final long serialVersionUID = 1L;

  public SpinValueTypeImpl(String name) {
    super(name);
  }

  public TypedValue createValue(Object value, Map<String, Object> valueInfo) {
    SpinValueBuilder<?> builder = createValue((SpinValue) value);
    applyValueInfo(builder, valueInfo);
    return builder.create();
  }

  public SerializableValue createValueFromSerialized(String serializedValue, Map<String, Object> valueInfo) {
    SpinValueBuilder<?> builder = createValueFromSerialized(serializedValue);
    applyValueInfo(builder, valueInfo);
    return builder.create();
  }

  public boolean isPrimitiveValueType() {
    return false;
  }

  public Map<String, Object> getValueInfo(TypedValue typedValue) {
    if(!(typedValue instanceof SpinValue)) {
      throw new IllegalArgumentException("Value not of type Spin Value.");
    }
    SpinValue spinValue = (SpinValue) typedValue;

    Map<String, Object> valueInfo = new HashMap<String, Object>();

    if (spinValue.isTransient()) {
      valueInfo.put(VALUE_INFO_TRANSIENT, spinValue.isTransient());
    }

    return valueInfo;
  }

  protected void applyValueInfo(SpinValueBuilder<?> builder, Map<String, Object> valueInfo) {
    if(valueInfo != null) {
      builder.setTransient(isTransient(valueInfo));
    }
  }

  protected abstract SpinValueBuilder<?> createValue(SpinValue value);

  protected abstract SpinValueBuilder<?> createValueFromSerialized(String value);

}
