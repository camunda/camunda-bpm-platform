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
import org.camunda.bpm.engine.variable.value.ShortValue;

/**
 * @author Daniel Meyer
 * @author Joram Barrez
 */
public class ShortValueSerializer extends PrimitiveValueSerializer<ShortValue> {

  public ShortValueSerializer() {
    super(ValueType.SHORT);
  }

  public ShortValue convertToTypedValue(UntypedValueImpl untypedValue) {
    return Variables.shortValue((Short) untypedValue.getValue(), untypedValue.isTransient());
  }

  public ShortValue readValue(ValueFields valueFields, boolean asTransientValue) {
    Long longValue = valueFields.getLongValue();
    Short shortValue = null;

    if(longValue != null) {
      shortValue = Short.valueOf(longValue.shortValue());
    }

    return Variables.shortValue(shortValue, asTransientValue);
  }

  public void writeValue(ShortValue value, ValueFields valueFields) {

    Short shortValue = value.getValue();

    if (shortValue != null) {
      valueFields.setLongValue(shortValue.longValue());
      valueFields.setTextValue(value.toString());
    } else {
      valueFields.setLongValue(null);
      valueFields.setTextValue(null);
    }
  }

}
