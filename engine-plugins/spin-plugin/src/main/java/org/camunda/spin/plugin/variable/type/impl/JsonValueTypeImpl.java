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

import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.type.JsonValueType;
import org.camunda.spin.plugin.variable.value.SpinValue;
import org.camunda.spin.plugin.variable.value.builder.JsonValueBuilder;

/**
 * @author Roman Smirnov
 *
 */
public class JsonValueTypeImpl extends SpinValueTypeImpl implements JsonValueType {

  private static final long serialVersionUID = 1L;

  public JsonValueTypeImpl() {
    super(TYPE_NAME);
  }

  protected JsonValueBuilder createValue(SpinValue value) {
    return SpinValues.jsonValue((SpinJsonNode) value);
  }

  protected JsonValueBuilder createValueFromSerialized(String value) {
    return SpinValues.jsonValue(value);
  }

}
