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
package org.camunda.spin.xml.dom.format.spi;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.camunda.spin.DataFormats;
import org.camunda.spin.impl.xml.dom.format.DomXmlDataFormat;
import org.camunda.spin.xml.JdkUtil;
import org.camunda.spin.xml.SpinXmlDataFormatException;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

public class DomXmlDataFormatProtectionTest {

  protected static DomXmlDataFormat format;

  @BeforeClass
  public static void setUpMocks() {
    format = (DomXmlDataFormat) DataFormats.xml();
  }

  @Test
  public void shouldThrowExceptionForTooManyAttributes() {
    // IBM JDKs do not check on attribute number limits, skip the test there
    Assume.assumeFalse(JdkUtil.runsOnIbmJDK());

    // given
    String testXml = "org/camunda/spin/xml/dom/format/spi/FeatureSecureProcessing.xml";
    InputStream testXmlAsStream = this.getClass().getClassLoader().getResourceAsStream(testXml);

    // when
    assertThatThrownBy(() -> {
      format.getReader().readInput(new InputStreamReader(testXmlAsStream));
    })
        // then
        .isInstanceOf(SpinXmlDataFormatException.class);
  }

  @Test
  public void shouldThrowExceptionForDoctype() {
    // given
    String testXml = "org/camunda/spin/xml/dom/format/spi/XxeProcessing.xml";
    InputStream testXmlAsStream = this.getClass().getClassLoader().getResourceAsStream(testXml);

    // when
    assertThatThrownBy(() -> {
      format.getReader().readInput(new InputStreamReader(testXmlAsStream));
    })
        // then
        .isInstanceOf(SpinXmlDataFormatException.class)
        .hasMessageContaining("SPIN/DOM-XML-01009 Unable to parse input into DOM document")
        .hasStackTraceContaining("DOCTYPE")
        .hasStackTraceContaining("http://apache.org/xml/features/disallow-doctype-decl");
  }

}
