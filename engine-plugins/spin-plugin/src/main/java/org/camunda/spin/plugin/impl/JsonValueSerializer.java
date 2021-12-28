/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.spin.plugin.impl;

import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.spin.DataFormats;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.type.JsonValueType;
import org.camunda.spin.plugin.variable.type.SpinValueType;
import org.camunda.spin.plugin.variable.value.SpinValue;
import org.camunda.spin.plugin.variable.value.impl.JsonValueImpl;
import org.camunda.spin.spi.DataFormat;

/**
 * @author Roman Smirnov
 *
 */
public class JsonValueSerializer extends SpinValueSerializer {

  public JsonValueSerializer(DataFormat<SpinJsonNode> dataFormat) {
    super(SpinValueType.JSON, dataFormat, JsonValueType.TYPE_NAME);
  }

  public JsonValueSerializer() {
    this(DataFormats.json());
  }

  public SpinValue convertToTypedValue(UntypedValueImpl untypedValue) {
    return SpinValues.jsonValue((SpinJsonNode) untypedValue.getValue()).create();
  }

  protected SpinValue createDeserializedValue(Object deserializedObject, String serializedStringValue, ValueFields valueFields, boolean asTransientValue) {
    SpinJsonNode value = (SpinJsonNode) deserializedObject;
    JsonValueImpl jsonValue = new JsonValueImpl(value, serializedStringValue, dataFormat.getName(), true);
    jsonValue.setTransient(asTransientValue);
    return jsonValue;
  }

  protected SpinValue createSerializedValue(String serializedStringValue, ValueFields valueFields, boolean asTransientValue) {
    JsonValueImpl jsonValue = new JsonValueImpl(serializedStringValue, serializationDataFormat);
    jsonValue.setTransient(asTransientValue);
    return jsonValue;
  }

}
