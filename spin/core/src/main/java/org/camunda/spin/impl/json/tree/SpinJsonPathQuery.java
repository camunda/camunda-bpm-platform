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

package org.camunda.spin.impl.json.tree;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.camunda.spin.SpinList;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.json.SpinJsonTreePathQuery;
import org.camunda.spin.logging.SpinLogger;

/**
 * @author Stefan Hentschel
 */
public class SpinJsonPathQuery extends SpinJsonTreePathQuery {

  private final static JsonJacksonTreeLogger LOG = SpinLogger.JSON_TREE_LOGGER;

  protected final SpinJsonNode spinJsonNode;
  protected final JsonPath query;
  protected final JsonJacksonTreeDataFormat dataFormat;

  public SpinJsonPathQuery(SpinJsonNode spinJsonNode, JsonPath query, JsonJacksonTreeDataFormat dataFormat) {
    this.spinJsonNode = spinJsonNode;
    this.query = query;
    this.dataFormat = dataFormat;
  }

  public SpinJsonNode element() {
    try {
      JsonNode node = dataFormat.createJsonNode(query.read(spinJsonNode.toString()));
      return dataFormat.createWrapperInstance(node);
    } catch(PathNotFoundException pex) {
      throw LOG.unableToEvaluateJsonPathExpressionOnNode(spinJsonNode, pex);
    } catch (ClassCastException cex) {
      throw LOG.unableToCastJsonPathResultTo(SpinJsonNode.class, cex);
    }
  }

  public SpinList<SpinJsonNode> elementList() {
    SpinJsonNode node = element();
    if(node.isArray()) {
      return node.elements();
    } else {
      throw LOG.unableToParseValue(SpinList.class.getSimpleName(), node.getNodeType());
    }
  }

  public String string() {
    SpinJsonNode node = element();
    if(node.isString()) {
      return node.stringValue();
    } else {
      throw LOG.unableToParseValue(String.class.getSimpleName(), node.getNodeType());
    }
  }

  public Number number() {
    SpinJsonNode node = element();
    if(node.isNumber()) {
      return node.numberValue();
    } else {
      throw LOG.unableToParseValue(Number.class.getSimpleName(), node.getNodeType());
    }
  }

  public Boolean bool() {
    SpinJsonNode node = element();
    if(node.isBoolean()) {
      return node.boolValue();
    } else {
      throw LOG.unableToParseValue(Boolean.class.getSimpleName(), node.getNodeType());
    }
  }
}
