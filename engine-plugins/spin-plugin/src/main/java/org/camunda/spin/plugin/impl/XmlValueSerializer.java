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
package org.camunda.spin.plugin.impl;

import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.spin.DataFormats;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.type.SpinValueType;
import org.camunda.spin.plugin.variable.type.XmlValueType;
import org.camunda.spin.plugin.variable.value.SpinValue;
import org.camunda.spin.plugin.variable.value.impl.XmlValueImpl;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.xml.SpinXmlElement;

/**
 * @author Roman Smirnov
 *
 */
public class XmlValueSerializer extends SpinValueSerializer {

  public XmlValueSerializer(DataFormat<SpinXmlElement> dataFormat) {
    super(SpinValueType.XML, dataFormat, XmlValueType.TYPE_NAME);
  }

  public XmlValueSerializer() {
    this(DataFormats.xml());
  }

  public SpinValue convertToTypedValue(UntypedValueImpl untypedValue) {
    return SpinValues.xmlValue((SpinXmlElement) untypedValue.getValue()).create();
  }

  protected SpinValue createDeserializedValue(Object deserializedObject, String serializedStringValue, ValueFields valueFields, boolean asTransientValue) {
    SpinXmlElement value = (SpinXmlElement) deserializedObject;
    XmlValueImpl xmlValue = new XmlValueImpl(value, serializedStringValue, value.getDataFormatName(), true);
    xmlValue.setTransient(asTransientValue);
    return xmlValue;
  }

  protected SpinValue createSerializedValue(String serializedStringValue, ValueFields valueFields, boolean asTransientValue) {
    XmlValueImpl xmlValue = new XmlValueImpl(serializedStringValue, serializationDataFormat);
    xmlValue.setTransient(asTransientValue);
    return xmlValue;
  }

}
