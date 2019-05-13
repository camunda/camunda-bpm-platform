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
import org.camunda.bpm.engine.variable.value.BooleanValue;

/**
 * Serializes booleans as long values.
 *
 * @author Daniel Meyer
 */
public class BooleanValueSerializer extends PrimitiveValueSerializer<BooleanValue> {

  // boolean is modeled as long values
  private static final Long TRUE = 1L;
  private static final Long FALSE = 0L;

  public BooleanValueSerializer() {
    super(ValueType.BOOLEAN);
  }

  public BooleanValue convertToTypedValue(UntypedValueImpl untypedValue) {
    return Variables.booleanValue((Boolean) untypedValue.getValue(), untypedValue.isTransient());
  }

  public BooleanValue readValue(ValueFields valueFields, boolean asTransientValue) {
    Boolean boolValue = null;
    Long longValue = valueFields.getLongValue();

    if(longValue != null) {
      boolValue = longValue.equals(TRUE);
    }

    return Variables.booleanValue(boolValue, asTransientValue);
  }

  public void writeValue(BooleanValue variableValue, ValueFields valueFields) {
    Long longValue = null;
    Boolean boolValue = variableValue.getValue();

    if(boolValue != null) {
      longValue = boolValue ? TRUE : FALSE;
    }

    valueFields.setLongValue(longValue);
  }

}
