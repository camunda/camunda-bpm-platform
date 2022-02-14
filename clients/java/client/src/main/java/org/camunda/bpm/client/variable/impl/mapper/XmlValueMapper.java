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

import org.camunda.bpm.client.variable.ClientValues;
import org.camunda.bpm.client.variable.impl.TypedValueField;
import org.camunda.bpm.client.variable.value.XmlValue;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;

public class XmlValueMapper extends PrimitiveValueMapper<XmlValue> {

  public XmlValueMapper() {
    super(ClientValues.XML);
  }

  public XmlValue convertToTypedValue(UntypedValueImpl untypedValue) {
    return ClientValues.xmlValue((String) untypedValue.getValue());
  }

  public void writeValue(XmlValue xmlValue, TypedValueField typedValueField) {
    typedValueField.setValue(xmlValue.getValue());
  }

  public XmlValue readValue(TypedValueField typedValueField) {
    return ClientValues.xmlValue((String) typedValueField.getValue());
  }

}
