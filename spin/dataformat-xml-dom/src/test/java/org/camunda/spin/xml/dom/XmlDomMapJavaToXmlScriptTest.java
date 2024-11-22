/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.spin.xml.dom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.spin.xml.XmlTestConstants.EXAMPLE_VALIDATION_XML;
import static org.camunda.spin.xml.XmlTestConstants.createExampleOrder;

import org.camunda.spin.impl.test.Script;
import org.camunda.spin.impl.test.ScriptTest;
import org.camunda.spin.xml.XmlTestUtil;
import org.camunda.spin.xml.mapping.Order;
import org.junit.Test;

public abstract class XmlDomMapJavaToXmlScriptTest extends ScriptTest{

  @Test
  @Script(execute = false)
  public void shouldMapJavaToXml() throws Throwable {
    Order order = createExampleOrder();

    script.setVariable("input", order);
    script.execute();
    String xml = script.getVariable("xml");

    //In EXAMPLE_VALIDATION_XML, expected date is hardcoded in CET timezone, ignoring it so that it passes when ran in
    //different timezone
    String exampleValidationXmlWoTimezone = XmlTestUtil.removeTimeZone(EXAMPLE_VALIDATION_XML);
    xml = XmlTestUtil.removeTimeZone(xml);
    assertThat(xml).isXmlEqualTo(exampleValidationXmlWoTimezone);
  }

  @Test(expected = IllegalArgumentException.class)
  @Script(execute = false)
  public void shouldFailWithNull() throws Throwable {
    failingWithException();
  }
}
