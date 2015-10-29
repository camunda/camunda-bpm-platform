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

import java.util.Collections;
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
    return builder.create();
  }

  public SerializableValue createValueFromSerialized(String serializedValue, Map<String, Object> valueInfo) {
    SpinValueBuilder<?> builder = createValueFromSerialized(serializedValue);
    return builder.create();
  }

  public boolean isPrimitiveValueType() {
    return false;
  }

  public Map<String, Object> getValueInfo(TypedValue typedValue) {
    return Collections.emptyMap();
  }

  protected abstract SpinValueBuilder<?> createValue(SpinValue value);

  protected abstract SpinValueBuilder<?> createValueFromSerialized(String value);

}
