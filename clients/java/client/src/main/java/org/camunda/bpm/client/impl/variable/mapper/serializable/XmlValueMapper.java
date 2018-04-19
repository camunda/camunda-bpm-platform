/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.client.impl.variable.mapper.serializable;

import org.camunda.bpm.client.impl.variable.TypedValueField;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.camunda.spin.DataFormats;
import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.type.SpinValueType;
import org.camunda.spin.plugin.variable.type.XmlValueType;
import org.camunda.spin.plugin.variable.value.XmlValue;
import org.camunda.spin.plugin.variable.value.impl.XmlValueImpl;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.xml.SpinXmlElement;

public class XmlValueMapper extends SpinValueMapper<XmlValue> {

  public XmlValueMapper(DataFormat<SpinXmlElement> dataFormat) {
    super(SpinValueType.XML, dataFormat, XmlValueType.TYPE_NAME);
  }

  public XmlValueMapper() {
    this(DataFormats.xml());
  }

  public XmlValue convertToTypedValue(UntypedValueImpl untypedValue) {
    return SpinValues.xmlValue((SpinXmlElement) untypedValue.getValue()).create();
  }

  protected XmlValue createDeserializedValue(Object deserializedObject, String serializedValue, TypedValueField typedValueField) {
    SpinXmlElement value = (SpinXmlElement) deserializedObject;
    return new XmlValueImpl(value, serializedValue, serializationDataFormat, true);
  }

  protected XmlValue createSerializedValue(String serializedValue, TypedValueField typedValueField) {
    return new XmlValueImpl(serializedValue, serializationDataFormat);
  }

}
