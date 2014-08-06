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

import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.json.SpinJsonTreeNodeException;
import org.camunda.spin.json.SpinJsonTreePathException;
import org.camunda.spin.json.SpinJsonTreePropertyException;
import org.camunda.spin.logging.SpinLogger;
import org.camunda.spin.spi.SpinJsonDataFormatException;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.jayway.jsonpath.PathNotFoundException;

/**
 * @author Thorben Lindhauer
 * @author Stefan Hentschel
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
    return new SpinJsonDataFormatException(exceptionMessage("009", "Unable to map object '{}' to json node", input), cause);
  }

  public SpinJsonTreeNodeException unableToModifyNode(String nodeName) {
    return new SpinJsonTreeNodeException(exceptionMessage("010", "Unable to modify node of type '{}'. Node is not a list!", nodeName));
  }

  public SpinJsonTreeNodeException unableToGetIndex(String nodeName) {
    return new SpinJsonTreeNodeException(exceptionMessage("011", "Unable to get index from '{}'. Node is not a list!", nodeName));
  }

  public IndexOutOfBoundsException indexOutOfBounds(Integer index, Integer size) {
    return new IndexOutOfBoundsException(exceptionMessage("012", "Index is out of bound! Index: '{}', Size: '{}'", index, size));
  }

  public SpinJsonTreePathException unableToEvaluateJsonPathExpressionOnNode(SpinJsonNode node, Exception cause) {
    return new SpinJsonTreePathException(
      exceptionMessage("013", "Unable to evaluate JsonPath expression on element '{}'", node.getClass().getName()), cause);
  }

  public SpinJsonTreePathException unableToCompileJsonPathExpression(String expression, Exception cause) {
    return new SpinJsonTreePathException(
      exceptionMessage("014", "Unable to compile '{}'!", expression), cause);
  }

  public SpinJsonTreePathException unableToCastJsonPathResultTo(Class<?> castClass, Exception cause) {
    return new SpinJsonTreePathException(
      exceptionMessage("015", "Unable to cast JsonPath expression to '{}'", castClass.getName()), cause);
  }

  public PathNotFoundException unableToFindJsonPath(String path, String json) {
    return new PathNotFoundException(exceptionMessage("016", "Unable to find json path '{}' on json node '{}'", path, json));
  }
}
