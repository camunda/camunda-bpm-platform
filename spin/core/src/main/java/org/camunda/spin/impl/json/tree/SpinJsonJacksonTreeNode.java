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

import static org.camunda.spin.impl.util.SpinEnsure.ensureNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.camunda.spin.SpinList;
import org.camunda.spin.impl.SpinListImpl;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.json.SpinJsonTreeNodeException;
import org.camunda.spin.logging.SpinLogger;
import org.camunda.spin.spi.SpinJsonDataFormatException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
    return writeToWriter(new StringWriter()).toString();
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

  @SuppressWarnings("unchecked")
  protected JsonNode createJsonNode(Object parameter) {
    if(parameter instanceof String) {
      return createJsonNode((String) parameter);

    } else if(parameter instanceof Integer) {
      return createJsonNode((Integer) parameter);

    } else if(parameter instanceof Boolean) {
      return createJsonNode((Boolean) parameter);

    } else if(parameter instanceof Float) {
      return createJsonNode((Float) parameter);

    } else if(parameter instanceof Long) {
      return createJsonNode((Long) parameter);

    } else if(parameter instanceof Number) {
      return createJsonNode(((Number) parameter).floatValue());

    } else if(parameter instanceof List) {
      return createJsonNode((List<Object>) parameter);

    } else if(parameter instanceof Map) {
      return createJsonNode((Map<String, Object>) parameter);

    } else {
      throw LOG.unableToCreateNode(parameter.getClass().getSimpleName());
    }
  }

  protected JsonNode createJsonNode(String parameter) {
    return dataFormat.getConfiguredObjectMapper().getNodeFactory().textNode(parameter);
  }

  protected JsonNode createJsonNode(Integer parameter) {
    return dataFormat.getConfiguredObjectMapper().getNodeFactory().numberNode(parameter);
  }

  protected JsonNode createJsonNode(Float parameter) {
    return dataFormat.getConfiguredObjectMapper().getNodeFactory().numberNode(parameter);
  }

  protected JsonNode createJsonNode(Long parameter) {
    return dataFormat.getConfiguredObjectMapper().getNodeFactory().numberNode(parameter);
  }

  protected JsonNode createJsonNode(Boolean parameter) {
    return dataFormat.getConfiguredObjectMapper().getNodeFactory().booleanNode(parameter);
  }

  protected JsonNode createJsonNode(List<Object> parameter) {
    ArrayNode node = dataFormat.getConfiguredObjectMapper().getNodeFactory().arrayNode();
    for(Object entry : parameter) {
      node.add(createJsonNode(entry));
    }
    return node;
  }

  protected JsonNode createJsonNode(Map<String, Object> parameter) {
    ObjectNode node = dataFormat.getConfiguredObjectMapper().getNodeFactory().objectNode();
    for (Map.Entry<String, Object> entry : parameter.entrySet()) {
      node.put(entry.getKey(), createJsonNode(entry.getValue()));
    }
    return node;
  }

  public boolean isObject() {
    return jsonNode.isObject();
  }

  public boolean hasProp(String name) {
    return jsonNode.has(name);
  }

  public SpinJsonNode prop(String name) {
    ensureNotNull("name", name);
    if(jsonNode.has(name)) {
      JsonNode property = jsonNode.get(name);
      return dataFormat.createWrapperInstance(property);
    } else {
      throw LOG.unableToFindProperty(name);
    }
  }

  public SpinJsonNode prop(String name, String newProperty) {
    ObjectNode node = (ObjectNode) jsonNode;

    node.put(name, newProperty);

    return dataFormat.createWrapperInstance(node);
  }

  public SpinJsonNode prop(String name, Number newProperty) {
    ObjectNode node = (ObjectNode) jsonNode;

    // Numbers magic because Jackson has no native .put(Number value)
    if(newProperty instanceof Long) {
      node.put(name, newProperty.longValue());
    } else if(newProperty instanceof Integer) {
      node.put(name, newProperty.intValue());
    } else if(newProperty instanceof Float) {
        node.put(name, newProperty.floatValue());
    } else {
      // convert any other sub class of Number into Float
      node.put(name, newProperty.floatValue());
    }

    return dataFormat.createWrapperInstance(node);
  }

  public SpinJsonNode prop(String name, int newProperty) {
    return prop(name, (Number) newProperty);
  }

  public SpinJsonNode prop(String name, float newProperty) {
    return prop(name, (Number) newProperty);
  }

  public SpinJsonNode prop(String name, long newProperty) {
    return prop(name, (Number) newProperty);
  }

  public SpinJsonNode prop(String name, boolean newProperty) {
    return prop(name, (Boolean) newProperty);
  }

  public SpinJsonNode prop(String name, Boolean newProperty) {
    ObjectNode node = (ObjectNode) jsonNode;

    node.put(name, createJsonNode(newProperty));

    return dataFormat.createWrapperInstance(node);
  }

  public SpinJsonNode prop(String name, List<Object> newProperty) {
    ObjectNode node = (ObjectNode) jsonNode;

    node.put(name, createJsonNode(newProperty));

    return dataFormat.createWrapperInstance(node);
  }

  public SpinJsonNode prop(String name, Map<String, Object> newProperty) {
    ObjectNode node = (ObjectNode) jsonNode;

    node.put(name, createJsonNode(newProperty));

    return dataFormat.createWrapperInstance(node);
  }

  public SpinJsonNode prop(String name, SpinJsonNode newProperty) {
    ObjectNode node = (ObjectNode) jsonNode;

    node.put(name, (JsonNode) newProperty.unwrap());

    return dataFormat.createWrapperInstance(node);
  }

  public SpinJsonNode deleteProp(String name) {
    ensureNotNull("name", name);

    if(jsonNode.has(name)) {
      ObjectNode node = (ObjectNode) jsonNode;
      node.remove(name);
      return dataFormat.createWrapperInstance(node);
    } else {
      throw LOG.unableToFindProperty(name);
    }
  }

  public SpinJsonNode deleteProp(List<String> names) {
    ensureNotNull("names", names);

    for(String name: names) {
      if(!jsonNode.has(name)) {
        throw LOG.unableToFindProperty(name);
      }
    }

    ObjectNode node = (ObjectNode) jsonNode;
    node.remove(names);
    return dataFormat.createWrapperInstance(node);
  }

  public Boolean isBoolean() {
    return jsonNode.isBoolean();
  }

  public Boolean boolValue() {
    if(jsonNode.isBoolean()) {
      return jsonNode.booleanValue();
    } else {
      throw LOG.unableToParseValue(Boolean.class.getSimpleName(), jsonNode.getNodeType());
    }
  }

  public Boolean isNumber() {
    return jsonNode.isNumber();
  }

  public Number numberValue() {
    if(jsonNode.isNumber()) {
      return jsonNode.numberValue();
    } else {
      throw LOG.unableToParseValue(Number.class.getSimpleName(), jsonNode.getNodeType());
    }
  }

  public Boolean isString() {
    return jsonNode.isTextual();
  }

  public String stringValue() {
    if(jsonNode.isTextual()) {
      return jsonNode.textValue();
    } else {
      throw LOG.unableToParseValue(String.class.getSimpleName(), jsonNode.getNodeType());
    }
  }

  public Boolean isValue() {
    return jsonNode.isValueNode();
  }

  public Object value() {
    if(jsonNode.isBoolean()) {
      return jsonNode.booleanValue();
    }

    if(jsonNode.isNumber()) {
      return jsonNode.numberValue();
    }

    if(jsonNode.isTextual()) {
      return jsonNode.textValue();
    }

    throw LOG.unableToParseValue("String/Number/Boolean", jsonNode.getNodeType());
  }

  public Boolean isArray() {
    return jsonNode.isArray();
  }

  public SpinList<SpinJsonNode> elements() {
    if(jsonNode.isArray()) {
      Iterator<JsonNode> iterator = jsonNode.elements();
      SpinList<SpinJsonNode> list = new SpinListImpl<SpinJsonNode>();
      while(iterator.hasNext()) {
        SpinJsonNode node = dataFormat.createWrapperInstance(iterator.next());
        list.add(node);
      }

      return list;
    } else {
      throw LOG.unableToParseValue(SpinList.class.getSimpleName(), jsonNode.getNodeType());
    }
  }

  public List<String> fieldNames() {
    if(jsonNode.isContainerNode()) {
      Iterator<String> iterator = jsonNode.fieldNames();
      List<String> list = new ArrayList<String>();
      while(iterator.hasNext()) {
        list.add(iterator.next());
      }

      return list;
    } else {
      throw LOG.unableToParseValue("Array/Object", jsonNode.getNodeType());
    }
  }

  /**
   * Maps the json represented by this object to a java object of the given type.
   *
   * @throws SpinJsonTreeNodeException if the json representation cannot be mapped to the specified type
   */
  public <C> C mapTo(Class<C> type) {
    JsonJacksonTreeDataFormatMapper mapper = dataFormat.getMapper();
    return mapper.mapInternalToJava(jsonNode, type);
  }

  /**
   * Maps the json represented by this object to a java object of the given type.
   * Argument is to be supplied in Jackson's canonical type string format
   * (see {@link JavaType#toCanonical()}).
   *
   * @throws SpinJsonTreeNodeException if the json representation cannot be mapped to the specified type
   * @throws SpinJsonDataFormatException if the parameter does not match a valid type
   */
  public <C> C mapTo(String type) {
    JsonJacksonTreeDataFormatMapper mapper = dataFormat.getMapper();
    return mapper.mapInternalToJava(jsonNode, type);
  }

  /**
   * Maps the json represented by this object to a java object of the given type.
   *
   * @throws SpinJsonTreeNodeException if the json representation cannot be mapped to the specified type
   */
  public <C> C mapTo(JavaType type) {
    JsonJacksonTreeDataFormatMapper mapper = dataFormat.getMapper();
    return mapper.mapInternalToJava(jsonNode, type);
  }
}
