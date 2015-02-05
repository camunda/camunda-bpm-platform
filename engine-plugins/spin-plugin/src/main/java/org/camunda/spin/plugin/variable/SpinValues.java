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
package org.camunda.spin.plugin.variable;

import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.plugin.variable.value.builder.JsonValueBuilder;
import org.camunda.spin.plugin.variable.value.builder.XmlValueBuilder;
import org.camunda.spin.plugin.variable.value.impl.builder.JsonValueBuilderImpl;
import org.camunda.spin.plugin.variable.value.impl.builder.XmlValueBuilderImpl;
import org.camunda.spin.xml.SpinXmlElement;

/**
 * @author Roman Smirnov
 *
 */
public class SpinValues {

  public static JsonValueBuilder jsonValue(SpinJsonNode value) {
    return new JsonValueBuilderImpl(value);
  }

  public static JsonValueBuilder jsonValue(String value) {
    return new JsonValueBuilderImpl(value);
  }

  public static XmlValueBuilder xmlValue(SpinXmlElement value) {
    return new XmlValueBuilderImpl(value);
  }

  public static XmlValueBuilder xmlValue(String value) {
    return new XmlValueBuilderImpl(value);
  }

}
