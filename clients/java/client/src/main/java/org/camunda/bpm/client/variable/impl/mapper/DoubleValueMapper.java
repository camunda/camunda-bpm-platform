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
import org.camunda.bpm.engine.variable.value.DoubleValue;

public class DoubleValueMapper extends NumberValueMapper<DoubleValue> {

  public DoubleValueMapper() {
    super(ValueType.DOUBLE);
  }

  public DoubleValue convertToTypedValue(UntypedValueImpl untypedValue) {
    return Variables.doubleValue((Double) untypedValue.getValue());
  }

  public void writeValue(DoubleValue doubleValue, TypedValueField typedValueField) {
    typedValueField.setValue(doubleValue.getValue());
  }

  public DoubleValue readValue(TypedValueField typedValueField) {
    Double doubleValue = null;

    Object value = typedValueField.getValue();
    if (value != null) {
      Number numValue = (Number) value;
      doubleValue = numValue.doubleValue();
    }

    return Variables.doubleValue(doubleValue);
  }

}
