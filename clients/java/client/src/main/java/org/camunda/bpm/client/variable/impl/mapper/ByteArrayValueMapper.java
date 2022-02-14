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

import java.io.InputStream;

import org.apache.commons.codec.binary.Base64;
import org.camunda.bpm.client.variable.impl.TypedValueField;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.value.BytesValue;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.commons.utils.IoUtil;

/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class ByteArrayValueMapper extends PrimitiveValueMapper<BytesValue> {

  public ByteArrayValueMapper() {
    super(ValueType.BYTES);
  }

  public BytesValue convertToTypedValue(UntypedValueImpl untypedValue) {
    byte[] byteArr = null;

    Object value = untypedValue.getValue();
    if (value instanceof byte[]) {
      byteArr = (byte[]) value;
    }
    else {
      byteArr = IoUtil.inputStreamAsByteArray((InputStream) value);
    }

    return Variables.byteArrayValue(byteArr);
  }

  public BytesValue readValue(TypedValueField typedValueField) {
    byte[] byteArr = null;

    String value = (String) typedValueField.getValue();
    if (value != null) {
      byteArr = Base64.decodeBase64(value);
    }

    return Variables.byteArrayValue(byteArr);
  }

  public void writeValue(BytesValue byteValue, TypedValueField typedValueField) {
    byte[] bytes = (byte[]) byteValue.getValue();

    if (bytes != null) {
      typedValueField.setValue(Base64.encodeBase64String(bytes));
    }
  }

  protected boolean canWriteValue(TypedValue typedValue) {
    Object value = typedValue.getValue();
    return super.canWriteValue(typedValue) || value instanceof InputStream;
  }

  protected boolean canReadValue(TypedValueField typedValueField) {
    Object value = typedValueField.getValue();
    return value == null || value instanceof String;
  }
}
