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

import org.camunda.spin.xml.mapping.Order;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.camunda.spin.Spin.XML;
import static org.camunda.spin.xml.XmlTestConstants.*;

public class XmlDomMapJavaToXmlTest {

  @Test
  public void shouldMapJavaToXml() {
    Order order = createExampleOrder();
    String orderAsString = XML(order).toString();
    assertThat(replaceLineBreaks(orderAsString)).isEqualTo(replaceLineBreaks(EXAMPLE_VALIDATION_XML));
  }

  @Test
  public void shouldFailWithNull() {
    try {
      String s = XML(null).toString();
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected!
    }
  }
}
