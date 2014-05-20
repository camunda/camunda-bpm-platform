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

import org.camunda.spin.impl.xml.dom.SpinXmlDomElement;
import org.camunda.spin.spi.SpinDataFormatException;
import org.junit.Test;

import static org.camunda.spin.impl.util.IoUtil.*;
import static org.camunda.spin.Spin.*;
import static org.camunda.spin.xml.XmlTestConstants.*;
import static org.assertj.core.api.Assertions.*;
import static org.camunda.spin.DataFormats.*;

/**
 * @author Daniel Meyer
 *
 */
public class XmlDomCreateTest {

  @Test
  public void shouldCreateForString() {
    SpinXmlDomElement xml = XML(EXAMPLE_XML);
    assertThat(xml).isNotNull();

    xml = S(EXAMPLE_XML, xmlDom());
    assertThat(xml).isNotNull();

    xml = S(EXAMPLE_XML);
    assertThat(xml).isNotNull();
  }

  @Test
  public void shouldCreateForInputStream() {
    SpinXmlDomElement xml = XML(stringAsInputStream(EXAMPLE_XML));
    assertThat(xml).isNotNull();

    xml = S(stringAsInputStream(EXAMPLE_XML), xmlDom());
    assertThat(xml).isNotNull();

    xml = S(stringAsInputStream(EXAMPLE_XML));
    assertThat(xml).isNotNull();
  }

  @Test
  public void shouldBeIdempotent() {
    SpinXmlDomElement xml = XML(EXAMPLE_XML);
    assertThat(xml).isEqualTo(XML(xml));
    assertThat(xml).isEqualTo(S(xml, xmlDom()));
    assertThat(xml).isEqualTo(S(xml));
  }

  @Test
  public void shouldFailForNull() {
    try {
      XML(null);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }

    try {
      S(null, xmlDom());
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }

    try {
      S(null);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException e) {
      // expected
    }
  }

  @Test
  public void shouldFailForInvalidXml() {
    try {
      XML(EXAMPLE_INVALID_XML);
      fail("Expected IllegalArgumentException");
    } catch(SpinDataFormatException e) {
      // expected
    }

    try {
      S(EXAMPLE_INVALID_XML, xmlDom());
      fail("Expected IllegalArgumentException");
    } catch(SpinDataFormatException e) {
      // expected
    }

    try {
      S(EXAMPLE_INVALID_XML);
      fail("Expected IllegalArgumentException");
    } catch(SpinDataFormatException e) {
      // expected
    }
  }

  @Test
  public void shouldFailForEmptyString() {
    try {
      XML(EXAMPLE_EMPTY_STRING);
      fail("Expected IllegalArgumentException");
    } catch(SpinDataFormatException e) {
      // expected
    }

    try {
      S(EXAMPLE_EMPTY_STRING, xmlDom());
      fail("Expected IllegalArgumentException");
    } catch(SpinDataFormatException e) {
      // expected
    }

    try {
      S(EXAMPLE_EMPTY_STRING);
      fail("Expected IllegalArgumentException");
    } catch(SpinDataFormatException e) {
      // expected
    }
  }

  @Test
  public void shouldFailForEmptyInputStream() {
    try {
      XML(stringAsInputStream(EXAMPLE_EMPTY_STRING));
      fail("Expected IllegalArgumentException");
    } catch(SpinDataFormatException e) {
      // expected
    }

    try {
      S(stringAsInputStream(EXAMPLE_EMPTY_STRING), xmlDom());
      fail("Expected IllegalArgumentException");
    } catch(SpinDataFormatException e) {
      // expected
    }

    try {
      S(stringAsInputStream(EXAMPLE_EMPTY_STRING));
      fail("Expected IllegalArgumentException");
    } catch(SpinDataFormatException e) {
      // expected
    }
  }
}
