/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.spin.impl.json.jackson.query;

import com.jayway.jsonpath.InvalidPathException;
import org.camunda.spin.SpinList;
import org.camunda.spin.impl.json.jackson.JacksonJsonLogger;
import org.camunda.spin.impl.json.jackson.JacksonJsonNode;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.json.SpinJsonPathQuery;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

/**
 * @author Stefan Hentschel
 */
public class JacksonJsonPathQuery implements SpinJsonPathQuery {

  private static final JacksonJsonLogger LOG = JacksonJsonLogger.JSON_TREE_LOGGER;

  protected final SpinJsonNode spinJsonNode;
  protected final JsonPath query;
  protected final JacksonJsonDataFormat dataFormat;

  public JacksonJsonPathQuery(JacksonJsonNode jacksonJsonNode, JsonPath query, JacksonJsonDataFormat dataFormat) {
    this.spinJsonNode = jacksonJsonNode;
    this.query = query;
    this.dataFormat = dataFormat;
  }

  public SpinJsonNode element() {
    try {
      Object result = query.read(spinJsonNode.toString(), dataFormat.getJsonPathConfiguration());
      JsonNode node;
      if (result != null) {
        node = dataFormat.createJsonNode(result);
      } else {
        node = dataFormat.createNullJsonNode();
      }
      return dataFormat.createWrapperInstance(node);
    } catch(PathNotFoundException pex) {
      throw LOG.unableToEvaluateJsonPathExpressionOnNode(spinJsonNode, pex);
    } catch (ClassCastException cex) {
      throw LOG.unableToCastJsonPathResultTo(SpinJsonNode.class, cex);
    } catch(InvalidPathException iex) {
      throw LOG.invalidJsonPath(SpinJsonNode.class, iex);
    }
  }

  public SpinList<SpinJsonNode> elementList() {
    JacksonJsonNode node = (JacksonJsonNode) element();
    if(node.isArray()) {
      return node.elements();
    } else {
      throw LOG.unableToParseValue(SpinList.class.getSimpleName(), node.getNodeType());
    }
  }

  public String stringValue() {
    JacksonJsonNode node = (JacksonJsonNode) element();
    if(node.isString()) {
      return node.stringValue();
    } else {
      throw LOG.unableToParseValue(String.class.getSimpleName(), node.getNodeType());
    }
  }

  public Number numberValue() {
    JacksonJsonNode node = (JacksonJsonNode) element();
    if(node.isNumber()) {
      return node.numberValue();
    } else {
      throw LOG.unableToParseValue(Number.class.getSimpleName(), node.getNodeType());
    }
  }

  public Boolean boolValue() {
    JacksonJsonNode node = (JacksonJsonNode) element();
    if(node.isBoolean()) {
      return node.boolValue();
    } else {
      throw LOG.unableToParseValue(Boolean.class.getSimpleName(), node.getNodeType());
    }
  }
}
