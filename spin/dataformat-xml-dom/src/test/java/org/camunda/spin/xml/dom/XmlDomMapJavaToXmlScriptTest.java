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

import static org.camunda.spin.xml.XmlTestConstants.EXAMPLE_VALIDATION_XML;
import static org.camunda.spin.xml.XmlTestConstants.createExampleOrder;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.IOException;

import org.camunda.spin.impl.test.Script;
import org.camunda.spin.impl.test.ScriptTest;
import org.camunda.spin.xml.mapping.Order;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;
import org.xml.sax.SAXException;

public abstract class XmlDomMapJavaToXmlScriptTest extends ScriptTest{

  @Test
  @Script(execute = false)
  public void shouldMapJavaToXml() throws IOException, SAXException {
    Order order = createExampleOrder();

    script.setVariable("input", order);
    script.execute();
    String xml = script.getVariable("xml");

    assertXMLEqual(EXAMPLE_VALIDATION_XML, xml);
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(execute = false)
  public void shouldFailWithNull() throws Throwable {
    failingWithException();
  }
}
