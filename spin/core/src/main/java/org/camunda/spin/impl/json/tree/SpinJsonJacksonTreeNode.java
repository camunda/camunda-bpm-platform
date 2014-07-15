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

import com.fasterxml.jackson.databind.JsonNode;
import org.camunda.spin.SpinList;
import org.camunda.spin.impl.SpinListImpl;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.logging.SpinLogger;

import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Wrapper for a Jackson Json Tree Node. 
 * 
 * @author Thorben Lindhauer
 * @author Stefan Hentschel
 */
public class SpinJsonJacksonTreeNode extends SpinJsonNode {

  private final JsonJacksonTreeLogger LOG = SpinLogger.JSON_TREE_LOGGER;

  protected final JsonNode jsonNode;
  protected final JsonJacksonTreeDataFormat dataFormat;

  
  public SpinJsonJacksonTreeNode(JsonNode jsonNode, JsonJacksonTreeDataFormat format) {
    this.jsonNode = jsonNode;
    this.dataFormat = format;
  }
  
  public String getDataFormatName() {
    return dataFormat.getName();
  }

  public JsonNode unwrap() {
    return jsonNode;
  }

  public String toString() {
    return jsonNode.toString();
  }

  public OutputStream toStream() {
    // TODO Auto-generated method stub
    return null;
  }

  public <S extends OutputStream> S writeToStream(S outputStream) {
    // TODO Auto-generated method stub
    return null;
  }

  public <W extends Writer> W writeToWriter(W writer) {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * fetches a property by name
   *
   * @param name Name of the property
   * @return property SpinJsonNode representation of the property
   */
  public SpinJsonNode prop(String name) {
    JsonNode property = jsonNode.get(name);
    return dataFormat.createWrapperInstance(property);
  }

  /**
   * fetch boolean value of a property
   *
   * @return propertyValue value of type Boolean
   */
  public Boolean boolValue() {
    if(jsonNode.isBoolean()) {
      return jsonNode.booleanValue();
    } else {
      throw LOG.unableToParseValue(Boolean.class.getSimpleName(), jsonNode.getNodeType());
    }
  }

  /**
   * fetch number value of a property
   *
   * @return propertyValue value of type Number
   */
  public Number numberValue() {
    if(jsonNode.isNumber()) {
      return jsonNode.numberValue();
    } else {
      throw LOG.unableToParseValue(Number.class.getSimpleName(), jsonNode.getNodeType());
    }
  }

  /**
   * fetch string value of a property
   *
   * @return propertyValue value of type String
   */
  public String value() {
    if(jsonNode.isTextual()) {
      return jsonNode.textValue();
    } else {
      throw LOG.unableToParseValue(String.class.getSimpleName(), jsonNode.getNodeType());
    }
  }

  /**
   * fetch data for json array
   *
   * @return list list of child nodes
   */
  public SpinList elements() {
    Iterator<JsonNode> iterator = jsonNode.elements();
    SpinList<SpinJsonNode> list = new SpinListImpl<SpinJsonNode>();
    while(iterator.hasNext()) {
      SpinJsonNode node = dataFormat.createWrapperInstance(iterator.next());
      list.add(node);
    }

    return (SpinList) list;
  }

  /**
   * fetch a list of field names for all child nodes of a node
   *
   * @return list list of field names
   */
  public ArrayList<String> fieldNames() {
    Iterator<String> iterator = jsonNode.fieldNames();
    ArrayList<String> list = new ArrayList<String>();
    while(iterator.hasNext()) {
      list.add(iterator.next());
    }

    return list;
  }
}
