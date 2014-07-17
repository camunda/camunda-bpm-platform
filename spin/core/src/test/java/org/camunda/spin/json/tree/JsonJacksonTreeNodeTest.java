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

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.spin.DataFormats.jsonTree;
import static org.camunda.spin.Spin.S;
import static org.camunda.spin.Spin.JSON;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import org.camunda.spin.impl.json.tree.SpinJsonJacksonTreeNode;
import org.camunda.spin.impl.util.IoUtil;
import org.camunda.spin.json.SpinJsonNode;
import org.junit.Before;
import org.junit.Test;

public class JsonJacksonTreeNodeTest {

  protected SpinJsonJacksonTreeNode jsonNode;
  
  @Before
  public void parseJson() {
    jsonNode = S(EXAMPLE_JSON);
  }
  
  @Test
  public void canWriteToString() {
    assertThatJson(jsonNode.toString()).isEqualTo(EXAMPLE_JSON);
  }
  
  /**
   * This ensures that Jackson's toString() is not used internally as it does not apply
   * configuration.
   */
  @Test
  public void canWriteToStringWithConfiguration() {
    String input = "{\"prop\" : \"ä\"}";
    
    String result = JSON(input, jsonTree().writer().escapeNonAscii(Boolean.TRUE).done())
      .toString();
    
    assertThat(result).isEqualTo("{\"prop\":\"\\u00E4\"}");
  }

  @Test
  public void canWriteToStream() throws IOException {
    OutputStream outputStream = jsonNode.toStream();
    InputStream inputStream = IoUtil.convertOutputStreamToInputStream(outputStream);
    String value = IoUtil.getStringFromInputStream(inputStream, false);
    assertThatJson(value).isEqualTo(EXAMPLE_JSON);


    outputStream = new ByteArrayOutputStream();
    jsonNode.writeToStream(outputStream);
    inputStream = IoUtil.convertOutputStreamToInputStream(outputStream);
    value = IoUtil.getStringFromInputStream(inputStream, false);
    assertThatJson(value).isEqualTo(EXAMPLE_JSON);
  }

  @Test
  public void canWriteToWriter() {
    StringWriter writer = jsonNode.writeToWriter(new StringWriter());
    String value = writer.toString();
    assertThatJson(value).isEqualTo(EXAMPLE_JSON);
  }

  @Test
  public void canWriteToStreamAndReadAgain() {
    OutputStream outputStream = jsonNode.toStream();
    InputStream inputStream = IoUtil.convertOutputStreamToInputStream(outputStream);
    SpinJsonNode json = JSON(inputStream);
    assertThat(json).isNotNull();
  }
  
}
