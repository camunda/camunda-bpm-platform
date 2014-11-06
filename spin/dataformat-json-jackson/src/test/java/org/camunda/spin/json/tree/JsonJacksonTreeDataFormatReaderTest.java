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
package org.camunda.spin.json.tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON_COLLECTION;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.camunda.spin.DataFormats;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormatReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JsonJacksonTreeDataFormatReaderTest {

  private JacksonJsonDataFormatReader reader;
  private Reader inputReader;

  private static final int REWINDING_LIMIT = 256;

  @Before
  public void setUp() {
    reader = new JacksonJsonDataFormatReader(new JacksonJsonDataFormat(DataFormats.JSON_DATAFORMAT_NAME));
  }

  @Test
  public void shouldMatchJsonInput() throws IOException {
    inputReader = stringToReader(EXAMPLE_JSON);
    assertThat(reader.canRead(inputReader, REWINDING_LIMIT)).isTrue();
    inputReader.close();

    inputReader = stringToReader(EXAMPLE_JSON_COLLECTION);
    assertThat(reader.canRead(inputReader, REWINDING_LIMIT)).isTrue();
  }

  @Test
  public void shouldMatchJsonInputWithWhitespace() throws IOException {
    inputReader = stringToReader("   " + EXAMPLE_JSON);
    assertThat(reader.canRead(inputReader, REWINDING_LIMIT)).isTrue();
    inputReader.close();

    inputReader = stringToReader("\r\n\t   " + EXAMPLE_JSON);
    assertThat(reader.canRead(inputReader, REWINDING_LIMIT)).isTrue();
  }

  @Test
  public void shouldNotMatchInvalidJson() throws IOException {
    inputReader = stringToReader("prefix " + EXAMPLE_JSON);
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
