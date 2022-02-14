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
import org.camunda.bpm.engine.variable.value.IntegerValue;

public class IntegerValueMapper extends NumberValueMapper<IntegerValue> {

  public IntegerValueMapper() {
    super(ValueType.INTEGER);
  }

  public IntegerValue convertToTypedValue(UntypedValueImpl untypedValue) {
    return Variables.integerValue((Integer) untypedValue.getValue());
  }

  public void writeValue(IntegerValue intValue, TypedValueField typedValueField) {
    typedValueField.setValue(intValue.getValue());
  }

  public IntegerValue readValue(TypedValueField typedValueField) {
    Integer intValue = null;

    Object value = typedValueField.getValue();
    if (value != null) {
      Number numValue = (Number) value;
      intValue = numValue.intValue();
    }

    return Variables.integerValue(intValue);
  }

}
