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
package org.camunda.spin.plugin.variable.type.impl;

import org.camunda.spin.plugin.variable.SpinValues;
import org.camunda.spin.plugin.variable.type.XmlValueType;
import org.camunda.spin.plugin.variable.value.SpinValue;
import org.camunda.spin.plugin.variable.value.builder.XmlValueBuilder;
import org.camunda.spin.xml.SpinXmlElement;

/**
 * @author Roman Smirnov
 *
 */
public class XmlValueTypeImpl extends SpinValueTypeImpl implements XmlValueType {

  private static final long serialVersionUID = 1L;

  public XmlValueTypeImpl() {
    super(TYPE_NAME);
  }

  protected XmlValueBuilder createValue(SpinValue value) {
    return SpinValues.xmlValue((SpinXmlElement) value);
  }

  protected XmlValueBuilder createValueFromSerialized(String value) {
    return SpinValues.xmlValue(value);
  }

}
