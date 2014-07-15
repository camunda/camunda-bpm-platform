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

import com.fasterxml.jackson.databind.node.JsonNodeType;
import org.camunda.spin.logging.SpinLogger;
import org.camunda.spin.spi.SpinJsonDataFormatException;

/**
 * @author Thorben Lindhauer
 */
public class JsonJacksonTreeLogger extends SpinLogger {

  public SpinJsonDataFormatException unableToParseInput(Exception e) {
    return new SpinJsonDataFormatException(exceptionMessage("001", "Unable to parse input into json node"), e);
  }

  public void unableToFind(String mess) {
    logInfo("002", "Unable {}", mess);
  }

  /**
   * Exception handler if we are unable to parse a json value into a java representation
   *
   * @param expectedType Name of the expected Type
   * @param type Type of the json node
   * @return SpinJsonDataFormatException
   */
  public SpinJsonDataFormatException unableToParseValue(String expectedType, JsonNodeType type) {
    return new SpinJsonDataFormatException(exceptionMessage("003", "Expected '{}', got '{}'", expectedType, type.toString()));
  }
}
