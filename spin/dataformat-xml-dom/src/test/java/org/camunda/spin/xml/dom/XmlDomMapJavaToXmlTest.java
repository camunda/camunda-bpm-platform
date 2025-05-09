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
import static org.assertj.core.api.Fail.fail;
import static org.camunda.spin.Spin.XML;
import static org.camunda.spin.xml.XmlTestConstants.EXAMPLE_VALIDATION_XML;
import static org.camunda.spin.xml.XmlTestConstants.createExampleOrder;

import org.camunda.spin.xml.XmlTestUtil;
import org.camunda.spin.xml.mapping.NonXmlRootElementType;
import org.camunda.spin.xml.mapping.Order;
import org.junit.Test;

public class XmlDomMapJavaToXmlTest {

  @Test
  public void shouldMapJavaToXml() {
    Order order = createExampleOrder();
    String orderAsString = XML(order).toString();
    //In EXAMPLE_VALIDATION_XML, expected date is hardcoded in CET timezone, ignoring it so that it passes when ran in
    //different timezone
    String exampleValidationXmlWoTimezone = XmlTestUtil.removeTimeZone(EXAMPLE_VALIDATION_XML);
    orderAsString = XmlTestUtil.removeTimeZone(orderAsString);
    assertThat(orderAsString).isXmlEqualTo(exampleValidationXmlWoTimezone);
  }

  @Test
  public void shouldMapNonXmlRootElementToXml() {
    NonXmlRootElementType nonXmlRootElementType = new NonXmlRootElementType();
    nonXmlRootElementType.setProperty("propValue");

    String xmlString = XML(nonXmlRootElementType).toString();

    NonXmlRootElementType nonXmlRootElementType2 = XML(xmlString).mapTo(NonXmlRootElementType.class);
    assertThat(nonXmlRootElementType).isEqualTo(nonXmlRootElementType2);
  }

  @Test
  @SuppressWarnings("unused")
  public void shouldFailWithNull() {
    try {
      String s = XML(null).toString();
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected!
    }
  }
}
