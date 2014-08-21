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
package org.camunda.spin.xml.dom;

import org.camunda.spin.impl.xml.dom.XmlDomDataFormat;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlDomConfigureMapperTest {

  @Test
  @SuppressWarnings("unchecked")
  public void shouldPassConfigurationToNewInstance() {

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("bKey", "bValue");

    XmlDomDataFormat xmlDomDataFormat = new XmlDomDataFormat();
    xmlDomDataFormat.mapper()
      .config("aKey", "aValue")
      .config("properties", properties);

    XmlDomDataFormat xmlDomDataFormatInstance =
        xmlDomDataFormat.newInstance().mapper().config("anotherKey", "anotherValue").done();

    // old instance
    assertThat(xmlDomDataFormat.mapper().getValue("aKey")).isEqualTo("aValue");
    assertThat(xmlDomDataFormat.mapper().getValue("anotherKey")).isNull();

    // new instance
    assertThat(xmlDomDataFormatInstance.mapper().getValue("aKey")).isEqualTo("aValue");
    assertThat(xmlDomDataFormatInstance.mapper().getValue("anotherKey")).isEqualTo("anotherValue");
    Map<String, Object> checkProperties = (Map<String, Object>) xmlDomDataFormatInstance.mapper().getValue("properties");
    assertThat(checkProperties.get("bKey")).isEqualTo("bValue");
  }

}
