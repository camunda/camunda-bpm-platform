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

import org.camunda.spin.json.SpinJsonTreeNodeException;
import org.camunda.spin.json.SpinJsonTreePropertyException;
import org.camunda.spin.logging.SpinLogger;
import org.camunda.spin.spi.SpinJsonDataFormatException;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

/**
 * @author Thorben Lindhauer
 */
public class JsonJacksonTreeLogger extends SpinLogger {

  public SpinJsonDataFormatException unableToParseInput(Exception e) {
    return new SpinJsonDataFormatException(exceptionMessage("001", "Unable to parse input into json node"), e);
  }

  /**
   * Exception handler if we are unable to parse a json value into a java representation
   *
   * @param expectedType Name of the expected Type
   * @param type Type of the json node
   * @return SpinJsonDataFormatException
   */
  public SpinJsonDataFormatException unableToParseValue(String expectedType, JsonNodeType type) {
    return new SpinJsonDataFormatException(exceptionMessage("002", "Expected '{}', got '{}'", expectedType, type.toString()));
  }

  public SpinJsonDataFormatException unableToWriteJsonNode(Exception cause) {
    return new SpinJsonDataFormatException(exceptionMessage("003", "Unable to write json node"), cause);
  }

  public SpinJsonTreePropertyException unableToFindProperty(String propertyName) {
    return new SpinJsonTreePropertyException(exceptionMessage("004", "Unable to find '{}'", propertyName));
  }

  public SpinJsonTreePropertyException unableToCreateNode(String objectType) {
    return new SpinJsonTreePropertyException(exceptionMessage("005", "Unable to create node for object of type '{}'", objectType));
  }

  public SpinJsonTreeNodeException unableToDeserialize(JsonNode jsonNode, Class<?> type, Exception cause) {
    return new SpinJsonTreeNodeException(
        exceptionMessage("006", "Cannot deserialize '{}...' to java type '{}'",
            jsonNode.toString().substring(0, 10), type.getSimpleName()), cause);
  }

  public SpinJsonTreeNodeException unableToDeserialize(JsonNode jsonNode, JavaType type, Exception cause) {
    return new SpinJsonTreeNodeException(
        exceptionMessage("006", "Cannot deserialize '{}...' to java type '{}'",
            jsonNode.toString().substring(0, 10), type), cause);
  }

  public SpinJsonDataFormatException unableToConstructJavaType(String fromString, Exception cause) {
    return new SpinJsonDataFormatException(
        exceptionMessage("007", "Cannot construct java type from string '{}'", fromString), cause);
  }

  public SpinJsonDataFormatException unableToDetectCanonicalType(Object parameter) {
    return new SpinJsonDataFormatException(exceptionMessage("008", "Cannot detect canonical data type for parameter '{}'", parameter));
  }

  public SpinJsonDataFormatException unableToMapInput(Object input, Exception cause) {
    return new SpinJsonDataFormatException(exceptionMessage("009", "Unable to map object {} to json node", input), cause);
  }
}
