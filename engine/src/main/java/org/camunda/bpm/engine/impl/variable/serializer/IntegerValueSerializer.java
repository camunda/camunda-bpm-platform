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
package org.camunda.bpm.engine.impl.variable.serializer;

import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.IntegerValue;

/**
 * @author Joram Barrez
 * @author Daniel Meyer
 */
public class IntegerValueSerializer extends PrimitiveValueSerializer<IntegerValue> {

  public IntegerValueSerializer() {
    super(ValueType.INTEGER);
  }

  public IntegerValue convertToTypedValue(UntypedValueImpl untypedValue) {
    return Variables.integerValue((Integer) untypedValue.getValue(), untypedValue.isTransient());
  }

  public void writeValue(IntegerValue variableValue, ValueFields valueFields) {
    Integer value = variableValue.getValue();

    if (value!=null) {
      valueFields.setLongValue(((Integer) value).longValue());
      valueFields.setTextValue(value.toString());
    } else {
      valueFields.setLongValue(null);
      valueFields.setTextValue(null);
    }

  }

  public IntegerValue readValue(ValueFields valueFields, boolean asTransientValue) {
    Integer intValue = null;

    if(valueFields.getLongValue() != null) {
      intValue = Integer.valueOf(valueFields.getLongValue().intValue());
    }

    return Variables.integerValue(intValue, asTransientValue);
  }

}
