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
package org.camunda.spin.impl.json.jackson;

import org.camunda.commons.logging.BaseLogger;
import org.camunda.spin.impl.logging.SpinLogger;
import org.camunda.spin.json.SpinJsonDataFormatException;
import org.camunda.spin.json.SpinJsonException;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.json.SpinJsonPathException;
import org.camunda.spin.json.SpinJsonPropertyException;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.jayway.jsonpath.PathNotFoundException;

/**
 * @author Thorben Lindhauer
 * @author Stefan Hentschel
 */
public class JacksonJsonLogger extends SpinLogger {

  public static final String PROJECT_CODE = SpinLogger.PROJECT_CODE + "/JACKSON-JSON";
  public static final JacksonJsonLogger JSON_TREE_LOGGER = BaseLogger.createLogger(JacksonJsonLogger.class, PROJECT_CODE, "org.camunda.spin.json", "01");

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

  public SpinJsonException unableToFindProperty(String propertyName) {
    return new SpinJsonPropertyException(exceptionMessage("004", "Unable to find '{}'", propertyName));
  }

  public SpinJsonException unableToCreateNode(String objectType) {
    return new SpinJsonPropertyException(exceptionMessage("005", "Unable to create node for object of type '{}'", objectType));
  }

  public SpinJsonException unableToDeserialize(JsonNode jsonNode, JavaType type, Exception cause) {
    return new SpinJsonException(
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

  public SpinJsonException unableToModifyNode(String nodeName) {
    return new SpinJsonException(exceptionMessage("010", "Unable to modify node of type '{}'. Node is not a list.", nodeName));
  }

  public SpinJsonException unableToGetIndex(String nodeName) {
    return new SpinJsonException(exceptionMessage("011", "Unable to get index from '{}'. Node is not a list.", nodeName));
  }

  public IndexOutOfBoundsException indexOutOfBounds(Integer index, Integer size) {
    return new IndexOutOfBoundsException(exceptionMessage("012", "Index is out of bound! Index: '{}', Size: '{}'", index, size));
  }

  public SpinJsonPathException unableToEvaluateJsonPathExpressionOnNode(SpinJsonNode node, Exception cause) {
    return new SpinJsonPathException(
      exceptionMessage("013", "Unable to evaluate JsonPath expression on element '{}'", node.getClass().getName()), cause);
  }

  public SpinJsonPathException unableToCompileJsonPathExpression(String expression, Exception cause) {
    return new SpinJsonPathException(
      exceptionMessage("014", "Unable to compile '{}'!", expression), cause);
  }

  public SpinJsonPathException unableToCastJsonPathResultTo(Class<?> castClass, Exception cause) {
    return new SpinJsonPathException(
      exceptionMessage("015", "Unable to cast JsonPath expression to '{}'", castClass.getName()), cause);
  }

  public SpinJsonPathException invalidJsonPath(Class<?> castClass, Exception cause) {
    return new SpinJsonPathException(
            exceptionMessage("017", "Invalid json path to '{}'", castClass.getName()), cause);
  }
}
