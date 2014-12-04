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

import org.camunda.spin.impl.test.Script;
import org.camunda.spin.impl.test.ScriptTest;
import org.camunda.spin.impl.test.ScriptVariable;
import org.camunda.spin.xml.SpinXmlDataFormatException;
import org.camunda.spin.xml.mapping.Order;
import org.junit.Test;

import static org.camunda.spin.xml.XmlTestConstants.*;

/**
 * @author Stefan Hentschel.
 */
public abstract class XmlDomMapXmlToJavaScriptTest extends ScriptTest {

  @Test
  @Script
  @ScriptVariable(name = "input", file = EXAMPLE_VALIDATION_XML_FILE_NAME)
  public void shouldMapXmlToJava() {
    Order order = script.getVariable("order");
    assertIsExampleOrder(order);
  }

  @Test(expected = SpinXmlDataFormatException.class)
  @Script(execute = false)
  @ScriptVariable(name = "input", file = EXAMPLE_VALIDATION_XML_FILE_NAME)
  public void shouldFailMappingMalformedTypeString() throws Throwable {
    failingWithException();
  }

}