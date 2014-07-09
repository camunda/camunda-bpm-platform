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

import java.io.IOException;
import java.io.InputStream;

import org.camunda.spin.DataFormats;
import org.camunda.spin.impl.json.tree.JsonJacksonTreeDataFormatReader;
import org.camunda.spin.impl.util.IoUtil;
import org.camunda.spin.impl.util.RewindableInputStream;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON_COLLECTION;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class JsonJacksonTreeDataFormatReaderTest {

  private JsonJacksonTreeDataFormatReader reader;
  private RewindableInputStream stream;
  
  private static final int REWINDING_LIMIT = 256;
  
  @Before
  public void setUp() {
    reader = new JsonJacksonTreeDataFormatReader(DataFormats.jsonTree());
  }
  
  @Test
  public void shouldMatchJsonInput() throws IOException {
    stream = stringToStream(EXAMPLE_JSON);
    assertThat(reader.canRead(stream)).isTrue();
    stream.close();
    
    stream = stringToStream(EXAMPLE_JSON_COLLECTION);
    assertThat(reader.canRead(stream)).isTrue();
  }
  
  @Test
  public void shouldMatchJsonInputWithWhitespace() throws IOException {
    stream = stringToStream("   " + EXAMPLE_JSON);
    assertThat(reader.canRead(stream)).isTrue();
    stream.close();
    
    stream = stringToStream("\r\n\t   " + EXAMPLE_JSON);
    assertThat(reader.canRead(stream)).isTrue();
  }
  
  @Test
  public void shouldNotMatchInvalidJson() throws IOException {
    stream = stringToStream("prefix " + EXAMPLE_JSON);
    assertThat(reader.canRead(stream)).isFalse();
  }
  
  public RewindableInputStream stringToStream(String input) {
    InputStream stream = IoUtil.stringAsInputStream(input);
    return new RewindableInputStream(stream, REWINDING_LIMIT);
  }
  
  @After
  public void tearDown() throws IOException {
    if (stream != null) {
      stream.close();
    }
  }
}
