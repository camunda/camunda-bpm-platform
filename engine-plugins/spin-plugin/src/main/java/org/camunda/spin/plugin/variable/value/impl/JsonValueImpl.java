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

import org.camunda.spin.DataFormats;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.type.JsonValueType;
import org.camunda.spin.plugin.variable.type.SpinValueType;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.camunda.spin.spi.DataFormat;

/**
 * @author Roman Smirnov
 *
 */
public class JsonValueImpl extends SpinValueImpl implements JsonValue {

  private static final long serialVersionUID = 1L;

  public JsonValueImpl(String value) {
    this(null, value, DataFormats.JSON_DATAFORMAT_NAME, false);
  }

  public JsonValueImpl(SpinJsonNode value) {
    this(value, null, value.getDataFormatName(), true);
  }

  public JsonValueImpl(String value, String dataFormatName) {
    this(null, value, dataFormatName, false);
  }

  public JsonValueImpl(
      SpinJsonNode value,
      String serializedValue,
      String dataFormatName,
      boolean isDeserialized) {
    this(value, serializedValue, dataFormatName, isDeserialized, false);
  }

  public JsonValueImpl(
      SpinJsonNode value,
      String serializedValue,
      String dataFormatName,
      boolean isDeserialized,
      boolean isTransient) {
    super(value, serializedValue, dataFormatName, isDeserialized, SpinValueType.JSON, isTransient);
  }

  @SuppressWarnings("unchecked")
  public DataFormat<SpinJsonNode> getDataFormat() {
    return (DataFormat<SpinJsonNode>) super.getDataFormat();
  }

  public JsonValueType getType() {
    return (JsonValueType) super.getType();
  }

  public SpinJsonNode getValue() {
    return (SpinJsonNode) super.getValue();
  }

}
