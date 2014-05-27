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

import org.camunda.spin.spi.SpinDataFormatException;
import org.camunda.spin.xml.tree.SpinXmlTreeElement;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.spin.DataFormats.xmlDom;
import static org.camunda.spin.Spin.S;
import static org.camunda.spin.Spin.XML;
import static org.camunda.spin.impl.util.IoUtil.stringAsInputStream;
import static org.camunda.spin.xml.XmlTestConstants.*;

/**
 * @author Daniel Meyer
 *
 */
public class XmlDomCreateTest {

  @Test
  public void shouldCreateForString() {
    SpinXmlTreeElement xml = XML(EXAMPLE_XML);
    assertThat(xml).isNotNull();

    xml = S(EXAMPLE_XML, xmlDom());
    assertThat(xml).isNotNull();

    xml = S(EXAMPLE_XML);
    assertThat(xml).isNotNull();
  }

  @Test
  public void shouldCreateForInputStream() {
    SpinXmlTreeElement xml = XML(stringAsInputStream(EXAMPLE_XML));
    assertThat(xml).isNotNull();

    xml = S(stringAsInputStream(EXAMPLE_XML), xmlDom());
    assertThat(xml).isNotNull();

    xml = S(stringAsInputStream(EXAMPLE_XML));
    assertThat(xml).isNotNull();
  }

  @Test
  public void shouldBeIdempotent() {
    SpinXmlTreeElement xml = XML(EXAMPLE_XML);
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
