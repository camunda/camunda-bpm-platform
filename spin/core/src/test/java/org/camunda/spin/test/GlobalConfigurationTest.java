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
package org.camunda.spin.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.spin.DataFormats;
import org.camunda.spin.impl.json.tree.JsonJacksonTreeDataFormat;
import org.camunda.spin.impl.xml.dom.XmlDomDataFormat;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 */
public class GlobalConfigurationTest {

  @Test
  public void shouldConfigureJsonTreeGlobally() {

    JsonJacksonTreeDataFormat globalFormat = DataFormats.jsonTreeGlobal();
    globalFormat.mapper().config("aKey", "aValue");

    JsonJacksonTreeDataFormat formatInstance1 = DataFormats.jsonTree();
    assertThat(formatInstance1).isNotSameAs(globalFormat);
    assertThat(formatInstance1.mapper().getValue("aKey")).isEqualTo("aValue");

    JsonJacksonTreeDataFormat formatInstance2 = DataFormats.jsonTree();
    assertThat(formatInstance2).isNotSameAs(globalFormat);
    assertThat(formatInstance2).isNotSameAs(formatInstance1);
    assertThat(formatInstance1.mapper().getValue("aKey")).isEqualTo("aValue");

    // changing the new instance's config should not change the global config
    formatInstance1.mapper().config("aKey", "anotherValue");
    assertThat(globalFormat.mapper().getValue("aKey")).isEqualTo("aValue");

    // changing the global config should not change the instance's config
    globalFormat.mapper().config("aKey", "anotherValue");
    assertThat(formatInstance2.mapper().getValue("aKey")).isEqualTo("aValue");
  }

  @Test
  public void shouldConfigureXmlDomGlobally() {

    XmlDomDataFormat globalFormat = DataFormats.xmlDomGlobal();
    globalFormat.mapper().config("aKey", "aValue");

    XmlDomDataFormat formatInstance1 = DataFormats.xmlDom();
    assertThat(formatInstance1).isNotSameAs(globalFormat);
    assertThat(formatInstance1.mapper().getValue("aKey")).isEqualTo("aValue");

    XmlDomDataFormat formatInstance2 = DataFormats.xmlDom();
    assertThat(formatInstance2).isNotSameAs(globalFormat);
    assertThat(formatInstance2).isNotSameAs(formatInstance1);
    assertThat(formatInstance1.mapper().getValue("aKey")).isEqualTo("aValue");

    // changing the instance's config should not change the global config
    formatInstance1.mapper().config("aKey", "anotherValue");
    assertThat(globalFormat.mapper().getValue("aKey")).isEqualTo("aValue");

    // changing the global config should not change the instance's config
    globalFormat.mapper().config("aKey", "anotherValue");
    assertThat(formatInstance2.mapper().getValue("aKey")).isEqualTo("aValue");
  }
}
