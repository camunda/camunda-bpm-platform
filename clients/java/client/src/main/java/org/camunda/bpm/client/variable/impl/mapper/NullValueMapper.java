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

import org.camunda.bpm.client.variable.impl.AbstractTypedValueMapper;
import org.camunda.bpm.client.variable.impl.TypedValueField;
import org.camunda.bpm.engine.variable.impl.value.NullValueImpl;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class NullValueMapper extends AbstractTypedValueMapper<NullValueImpl> {

  public NullValueMapper() {
    super(ValueType.NULL);
  }

  public String getName() {
    return ValueType.NULL.getName().toLowerCase();
  }

  public NullValueImpl convertToTypedValue(UntypedValueImpl untypedValue) {
    return NullValueImpl.INSTANCE;
  }

  public void writeValue(NullValueImpl typedValue, TypedValueField typedValueField) {
    typedValueField.setValue(null);
  }

  public NullValueImpl readValue(TypedValueField typedValueField, boolean deserialize) {
    return NullValueImpl.INSTANCE;
  }

  protected boolean isNull(Object value) {
    return value == null;
  }

  protected boolean canWriteValue(TypedValue value) {
    return isNull(value.getValue());
  }

  protected boolean canReadValue(TypedValueField value) {
    return isNull(value.getValue());
  }
}
