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
import org.camunda.spin.spi.SpinXmlDataFormatException;
import org.camunda.spin.xml.mapping.Order;
import org.junit.Test;

import javax.xml.bind.helpers.DefaultValidationEventHandler;
import javax.xml.validation.Schema;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.camunda.spin.Spin.XML;
import static org.camunda.spin.xml.XmlTestConstants.*;

public class XmlDomMapJavaToXmlTest {

  @Test
  public void shouldMapJavaToXml() {
    Order order = createExampleOrder();
    assertThat(replaceLineBreaks(XML(order).toString())).isEqualTo(replaceLineBreaks(EXAMPLE_VALIDATION_XML));
  }

  @Test
  public void shouldMapWithValidation() {
    Schema schema = createSchema();

    XmlDomDataFormat dataFormat = new XmlDomDataFormat()
      .mapper()
      .config("schema", schema)
      .config("eventHandler", new DefaultValidationEventHandler())
      .done();

    String xml = XML(createExampleOrder(), dataFormat).toString();
    assertThat(replaceLineBreaks(xml)).isEqualTo(replaceLineBreaks(EXAMPLE_VALIDATION_XML));
  }

  @Test
  public void shouldFailMapWithValidation() {
    Schema schema = createSchema();

    XmlDomDataFormat dataFormat = new XmlDomDataFormat()
      .mapper()
      .config("schema", schema)
      .config("eventHandler", new DefaultValidationEventHandler())
      .done();

    Order order = new Order();
    order.setDueUntil(new Date());

    try {
      XML(order, dataFormat);
      fail("Expected SpinXmlDataFormatException");
    } catch(SpinXmlDataFormatException e) {
      // expected to fail ;)
    }
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
