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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.camunda.spin.SpinList;
import org.camunda.spin.impl.SpinListImpl;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.logging.SpinLogger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

  
  public SpinJsonJacksonTreeNode(JsonNode jsonNode, JsonJacksonTreeDataFormat dataFormat) {
    this.jsonNode = jsonNode;
    this.dataFormat = dataFormat;
  }
  
  public String getDataFormatName() {
    return dataFormat.getName();
  }

  public JsonNode unwrap() {
    return jsonNode;
  }

  public String toString() {
    // FIXME: should return the string of the wrapped json node
    return jsonNode.toString();
  }

  public OutputStream toStream() {
    OutputStream out = new ByteArrayOutputStream();
    return writeToStream(out);
  }

  public <S extends OutputStream> S writeToStream(S outputStream) {
    ObjectMapper mapper = dataFormat.getConfiguredObjectMapper();
    JsonFactory factory = mapper.getFactory();
    
    try {
      JsonGenerator generator = factory.createGenerator(outputStream);
      mapper.writeTree(generator, jsonNode);
    } catch (IOException e) {
      throw LOG.unableToWriteJsonNode(e);
    }
    
    return outputStream;
  }

  public <W extends Writer> W writeToWriter(W writer) {
    ObjectMapper mapper = dataFormat.getConfiguredObjectMapper();
    JsonFactory factory = mapper.getFactory();
    
    try {
      JsonGenerator generator = factory.createGenerator(writer);
      mapper.writeTree(generator, jsonNode);
    } catch (IOException e) {
      throw LOG.unableToWriteJsonNode(e);
    }
    
    return writer;
  }

  /**
   * fetches a property by name
   *
   * @param name Name of the property
   * @return property SpinJsonNode representation of the property
   */
  public SpinJsonNode prop(String name) {
    // FIXME: should throw exception if propertie does not exist
    // FIXME: should throw exception if name is null
    JsonNode property = jsonNode.get(name);
    return dataFormat.createWrapperInstance(property);
  }

  public Boolean boolValue() {
    if(jsonNode.isBoolean()) {
      return jsonNode.booleanValue();
    } else {
      throw LOG.unableToParseValue(Boolean.class.getSimpleName(), jsonNode.getNodeType());
    }
  }

  public Number numberValue() {
    if(jsonNode.isNumber()) {
      return jsonNode.numberValue();
    } else {
      throw LOG.unableToParseValue(Number.class.getSimpleName(), jsonNode.getNodeType());
    }
  }

  public String value() {
    if(jsonNode.isTextual()) {
      return jsonNode.textValue();
    } else {
      throw LOG.unableToParseValue(String.class.getSimpleName(), jsonNode.getNodeType());
    }
  }

  public SpinList<SpinJsonNode> elements() {
    // FIXME: should throw exception if the property is no container
    Iterator<JsonNode> iterator = jsonNode.elements();
    SpinList<SpinJsonNode> list = new SpinListImpl<SpinJsonNode>();
    while(iterator.hasNext()) {
      SpinJsonNode node = dataFormat.createWrapperInstance(iterator.next());
      list.add(node);
    }

    return list;
  }

  public List<String> fieldNames() {
    Iterator<String> iterator = jsonNode.fieldNames();
    List<String> list = new ArrayList<String>();
    while(iterator.hasNext()) {
      list.add(iterator.next());
    }

    return list;
  }
}
