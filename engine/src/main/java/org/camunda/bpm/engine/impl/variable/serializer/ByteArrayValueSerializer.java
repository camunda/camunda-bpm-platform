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

import java.io.InputStream;

import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.BytesValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class ByteArrayValueSerializer extends PrimitiveValueSerializer<BytesValue> {

  public ByteArrayValueSerializer() {
    super(ValueType.BYTES);
  }

  public BytesValue convertToTypedValue(UntypedValueImpl untypedValue) {
    Object value = untypedValue.getValue();
    if (value instanceof byte[]) {
      return Variables.byteArrayValue((byte[]) value, untypedValue.isTransient());
    } else {
      byte[] data = IoUtil.readInputStream((InputStream) value, null);
      return Variables.byteArrayValue(data, untypedValue.isTransient());
    }
  }

  public BytesValue readValue(ValueFields valueFields, boolean asTransientValue) {
    return Variables.byteArrayValue(valueFields.getByteArrayValue(), asTransientValue);
  }

  public void writeValue(BytesValue variableValue, ValueFields valueFields) {
    valueFields.setByteArrayValue(variableValue.getValue());
  }

  @Override
  protected boolean canWriteValue(TypedValue typedValue) {
    return super.canWriteValue(typedValue) || typedValue.getValue() instanceof InputStream;
  }

}
