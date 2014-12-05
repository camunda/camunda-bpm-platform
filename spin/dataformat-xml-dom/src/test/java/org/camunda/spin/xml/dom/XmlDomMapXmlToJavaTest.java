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

import static org.assertj.core.api.Assertions.fail;
import static org.camunda.spin.Spin.XML;
import static org.camunda.spin.xml.XmlTestConstants.EXAMPLE_VALIDATION_XML;
import static org.camunda.spin.xml.XmlTestConstants.assertIsExampleOrder;

import org.camunda.spin.xml.SpinXmlDataFormatException;
import org.camunda.spin.xml.mapping.Order;
import org.junit.Test;

public class XmlDomMapXmlToJavaTest {

  @Test
  public void shouldMapXmlObjectToJavaObject() {
    Order order = XML(EXAMPLE_VALIDATION_XML).mapTo(Order.class);
    assertIsExampleOrder(order);
  }

  @Test
  public void shouldMapByCanonicalString() {
    Order order = XML(EXAMPLE_VALIDATION_XML).mapTo(Order.class.getCanonicalName());
    assertIsExampleOrder(order);
  }

  @Test
  public void shouldFailForMalformedTypeString() {
    try {
      XML(EXAMPLE_VALIDATION_XML).mapTo("rubbish");
      fail("Expected SpinXmlDataFormatException");
    } catch (SpinXmlDataFormatException e) {
      // happy path
    }
  }
}
