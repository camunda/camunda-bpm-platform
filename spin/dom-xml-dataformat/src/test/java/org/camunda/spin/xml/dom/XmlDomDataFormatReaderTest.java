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

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.spin.xml.XmlTestConstants.EXAMPLE_XML;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.camunda.spin.DataFormats;
import org.camunda.spin.impl.xml.dom.format.DomXmlDataFormat;
import org.camunda.spin.impl.xml.dom.format.DomXmlDataFormatReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class XmlDomDataFormatReaderTest {

  private DomXmlDataFormatReader reader;
  private Reader inputReader;

  private static final int REWINDING_LIMIT = 256;

  @Before
  public void setUp() {
    DomXmlDataFormat domXmlDataFormat = new DomXmlDataFormat(DataFormats.XML_DATAFORMAT_NAME);
    reader = domXmlDataFormat.getReader();
  }

  @Test
  public void shouldMatchXmlInput() throws IOException {
    inputReader = stringToReader(EXAMPLE_XML);
    assertThat(reader.canRead(inputReader, REWINDING_LIMIT)).isTrue();
    inputReader.close();
  }

  @Test
  public void shouldMatchXmlInputWithWhitespace() throws IOException {
    inputReader = stringToReader("   " + EXAMPLE_XML);
    assertThat(reader.canRead(inputReader, REWINDING_LIMIT)).isTrue();
    inputReader.close();

    inputReader = stringToReader("\r\n\t   " + EXAMPLE_XML);
    assertThat(reader.canRead(inputReader, REWINDING_LIMIT)).isTrue();
  }

  @Test
  public void shouldNotMatchInvalidXml() throws IOException {
    inputReader = stringToReader("prefix " + EXAMPLE_XML);
    assertThat(reader.canRead(inputReader, REWINDING_LIMIT)).isFalse();
  }

  public Reader stringToReader(String input) {
    return new StringReader(input);
  }

  @After
  public void tearDown() throws IOException {
    if (inputReader != null) {
      inputReader.close();
    }
  }
}
