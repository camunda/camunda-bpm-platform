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
package org.camunda.spin.impl.json.tree;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.camunda.spin.logging.SpinLogger;
import org.camunda.spin.spi.TextBasedDataFormatReader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Thorben Lindhauer
 */
public class JsonJacksonTreeDataFormatReader extends TextBasedDataFormatReader {

  private static final JsonJacksonTreeLogger JSON_LOGGER = SpinLogger.JSON_TREE_LOGGER;
  private static final Pattern INPUT_MATCHING_PATTERN = Pattern.compile("\\A(\\s)*[{\\[]");
  
  protected JsonJacksonTreeDataFormat format;
  
  public JsonJacksonTreeDataFormatReader(JsonJacksonTreeDataFormat format) {
    this.format = format;
  }

  public Object readInput(InputStream input) {
    ObjectMapper mapper = format.getConfiguredObjectMapper();
    
    try {
      return mapper.readTree(input);
    } catch (JsonProcessingException e) {
      throw JSON_LOGGER.unableToParseInput(e);
    } catch (IOException e) {
      throw JSON_LOGGER.unableToParseInput(e);
    }
  }

  protected Pattern getInputDetectionPattern() {
    return INPUT_MATCHING_PATTERN;
  }

}
