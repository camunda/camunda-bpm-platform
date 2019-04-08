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
package org.camunda.bpm.client.variable.impl.mapper;

import org.camunda.bpm.client.variable.impl.TypedValueField;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.ShortValue;

public class ShortValueMapper extends NumberValueMapper<ShortValue> {

  public ShortValueMapper() {
    super(ValueType.SHORT);
  }

  public ShortValue convertToTypedValue(UntypedValueImpl untypedValue) {
    return Variables.shortValue((Short) untypedValue.getValue());
  }

  public void writeValue(ShortValue shortValue, TypedValueField typedValueField) {
    typedValueField.setValue(shortValue.getValue());
  }

  public ShortValue readValue(TypedValueField typedValueField) {
    Short shortValue = null;

    Object value = typedValueField.getValue();
    if (value != null) {
      Number numValue = (Number) value;
      shortValue = numValue.shortValue();
    }

    return Variables.shortValue(shortValue);
  }

}
