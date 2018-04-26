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
package org.camunda.spin.plugin.variable.value.impl;

import org.camunda.spin.DataFormats;
import org.camunda.spin.plugin.variable.type.SpinValueType;
import org.camunda.spin.plugin.variable.type.XmlValueType;
import org.camunda.spin.plugin.variable.value.XmlValue;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.xml.SpinXmlElement;

/**
 * @author Roman Smirnov
 *
 */
public class XmlValueImpl extends SpinValueImpl implements XmlValue {

  private static final long serialVersionUID = 1L;

  public XmlValueImpl(SpinXmlElement value) {
    this(value, null, value.getDataFormatName(), true);
  }

  public XmlValueImpl(String value, String dataFormatName) {
    this(null, value, dataFormatName, false);
  }

  public XmlValueImpl(String value) {
    this(null, value, DataFormats.XML_DATAFORMAT_NAME, false);
  }

  public XmlValueImpl(
      SpinXmlElement value,
      String serializedValue,
      String dataFormatName,
      boolean isDeserialized) {
    this(value, serializedValue, dataFormatName, isDeserialized, false);
  }

  public XmlValueImpl(
      SpinXmlElement value,
      String serializedValue,
      String dataFormatName,
      boolean isDeserialized,
      boolean isTransient) {
    super(value, serializedValue, dataFormatName, isDeserialized, SpinValueType.XML, isTransient);
  }

  @SuppressWarnings("unchecked")
  public DataFormat<SpinXmlElement> getDataFormat() {
    return (DataFormat<SpinXmlElement>) super.getDataFormat();
  }

  public XmlValueType getType() {
    return (XmlValueType) super.getType();
  }

  public SpinXmlElement getValue() {
    return (SpinXmlElement) super.getValue();
  }

}
