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

import org.camunda.bpm.client.impl.variable.TypedValueField;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.spin.DataFormats;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.type.JsonValueType;
import org.camunda.spin.plugin.variable.type.SpinValueType;
import org.camunda.spin.plugin.variable.value.JsonValue;
import org.camunda.spin.plugin.variable.value.impl.JsonValueImpl;
import org.camunda.spin.spi.DataFormat;

public class JsonValueMapper extends SpinValueMapper<JsonValue> {

  public JsonValueMapper(DataFormat<SpinJsonNode> dataFormat) {
    super(SpinValueType.JSON, dataFormat, JsonValueType.TYPE_NAME);
  }

  public JsonValueMapper() {
    this(DataFormats.json());
  }

  public JsonValue convertToTypedValue(UntypedValueImpl untypedValue) {
    return SpinValues.jsonValue((SpinJsonNode) untypedValue.getValue()).create();
  }

  protected JsonValue createDeserializedValue(Object deserializedObject, String serializedValue, TypedValueField typedValueField) {
    SpinJsonNode value = (SpinJsonNode) deserializedObject;
    return new JsonValueImpl(value, serializedValue, value.getDataFormatName(), true);
  }

  protected JsonValue createSerializedValue(String serializedValue, TypedValueField typedValueField) {
    return new JsonValueImpl(serializedValue, serializationDataFormat);
  }

}
