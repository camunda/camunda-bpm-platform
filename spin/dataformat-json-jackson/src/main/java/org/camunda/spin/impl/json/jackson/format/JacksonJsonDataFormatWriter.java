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
package org.camunda.spin.impl.json.jackson.format;

import java.io.IOException;
import java.io.Writer;

import org.camunda.spin.impl.json.jackson.JacksonJsonLogger;
import org.camunda.spin.spi.DataFormatWriter;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Daniel Meyer
 *
 */
public class JacksonJsonDataFormatWriter implements DataFormatWriter {

  private static final JacksonJsonLogger LOG = JacksonJsonLogger.JSON_TREE_LOGGER;

  protected JacksonJsonDataFormat dataFormat;

  public JacksonJsonDataFormatWriter(JacksonJsonDataFormat dataFormat) {
    this.dataFormat = dataFormat;
  }

  public void writeToWriter(Writer writer, Object input) {
    final ObjectMapper objectMapper = dataFormat.getObjectMapper();
    final JsonFactory factory = objectMapper.getFactory();

    try {
      JsonGenerator generator = factory.createGenerator(writer);
      objectMapper.writeTree(generator, (JsonNode) input);
    }
    catch (IOException e) {
      throw LOG.unableToWriteJsonNode(e);
    }

  }

}
