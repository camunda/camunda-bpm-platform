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
import java.io.Reader;
import java.util.regex.Pattern;

import org.camunda.spin.impl.json.jackson.JacksonJsonLogger;
import org.camunda.spin.spi.TextBasedDataFormatReader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Thorben Lindhauer
 */
public class JacksonJsonDataFormatReader extends TextBasedDataFormatReader {

  private static final JacksonJsonLogger JSON_LOGGER = JacksonJsonLogger.JSON_TREE_LOGGER;
  private static final Pattern INPUT_MATCHING_PATTERN = Pattern.compile("\\A(\\s)*[{\\[]");

  protected JacksonJsonDataFormat format;

  public JacksonJsonDataFormatReader(JacksonJsonDataFormat format) {
    this.format = format;
  }

  public Object readInput(Reader input) {
    ObjectMapper mapper = format.getObjectMapper();

    try {
      final JsonNode jsonNode = mapper.readTree(input);
      if (jsonNode == null) {
        throw new IOException("Input is empty");
      }
      return jsonNode;
    }
    catch (JsonProcessingException e) {
      throw JSON_LOGGER.unableToParseInput(e);
    }
    catch (IOException e) {
      throw JSON_LOGGER.unableToParseInput(e);
    }
  }

  protected Pattern getInputDetectionPattern() {
    return INPUT_MATCHING_PATTERN;
  }

}
