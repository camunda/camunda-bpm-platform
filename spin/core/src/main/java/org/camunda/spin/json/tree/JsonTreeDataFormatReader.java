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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.camunda.spin.logging.SpinLogger;
import org.camunda.spin.spi.DataFormatReader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonTreeDataFormatReader implements DataFormatReader {

  private static final JsonTreeLogger JSON_LOGGER = SpinLogger.JSON_TREE_LOGGER; 
  
  public boolean canRead(byte[] firstBytes) {
    String firstCharacters = new String(firstBytes, Charset.forName("UTF-8")).trim();
    
    return firstCharacters.startsWith("{") || firstCharacters.startsWith("[");
  }

  public Object readInput(InputStream input) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readTree(input);
    } catch (JsonProcessingException e) {
      throw JSON_LOGGER.unableToParseInput(e);
    } catch (IOException e) {
      throw JSON_LOGGER.unableToParseInput(e);
    }
  }

}
