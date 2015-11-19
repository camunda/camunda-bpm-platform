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

import static org.camunda.commons.utils.EnsureUtil.ensureNotNull;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.camunda.spin.SpinList;
import org.camunda.spin.impl.SpinListImpl;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat;
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormatMapper;
import org.camunda.spin.impl.json.jackson.query.JacksonJsonPathQuery;
import org.camunda.spin.json.SpinJsonDataFormatException;
import org.camunda.spin.json.SpinJsonException;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.json.SpinJsonPathQuery;
import org.camunda.spin.spi.DataFormatMapper;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;

/**
 * Wrapper for a Jackson Json Tree Node.
 *
 * @author Thorben Lindhauer
 * @author Stefan Hentschel
 */
public class JacksonJsonNode extends SpinJsonNode {

  private static final JacksonJsonLogger LOG = JacksonJsonLogger.JSON_TREE_LOGGER;

  protected final JsonNode jsonNode;
  protected final JacksonJsonDataFormat dataFormat;


  public JacksonJsonNode(JsonNode jsonNode, JacksonJsonDataFormat dataFormat) {
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
    StringWriter writer = new StringWriter();
    writeToWriter(writer);
    return writer.toString();
  }

  public void writeToWriter(Writer writer) {
    dataFormat.getWriter().writeToWriter(writer, jsonNode);
  }

  /**
   * fetch correct array index if index is less than 0
   *
   * ArrayNode will convert all negative integers into 0...
   *
   * @param index wanted index
   * @return {@link Integer} new index
   */
  protected Integer getCorrectIndex(Integer index) {
    Integer size = jsonNode.size();
    Integer newIndex = index;

    // reverse walking through the array
    if(index < 0) {
      newIndex = size + index;
    }

    // the negative index would be greater than the size a second time!
    if(newIndex < 0) {
      throw LOG.indexOutOfBounds(index, size);
    }

    // the index is greater as the actual size
    if(index > size) {
     throw LOG.indexOutOfBounds(index, size);
    }

    return newIndex;
  }

  public Integer indexOf(Object searchObject) {
    ensureNotNull("searchObject", searchObject);
    if(this.isArray()) {
      Integer i = 0;
      JsonNode node = dataFormat.createJsonNode(searchObject);
      for (Iterator<JsonNode> nodeIterator = jsonNode.elements(); nodeIterator.hasNext(); i++) {
        JsonNode n = nodeIterator.next();
        if (n.equals(node)) {
          return i;
        }
      }

      // when searchObject is not found
      throw LOG.unableToFindProperty(node.asText());
    } else {
      throw LOG.unableToGetIndex(jsonNode.getNodeType().name());
    }
  }

  public Integer lastIndexOf(Object searchObject) {
    ensureNotNull("searchObject", searchObject);
    if(this.isArray()) {
      Integer i = 0;
      Integer j = -1;
      JsonNode node = dataFormat.createJsonNode(searchObject);
      for (Iterator<JsonNode> nodeIterator = jsonNode.elements(); nodeIterator.hasNext(); i++) {
        JsonNode n = nodeIterator.next();
        if (n.equals(node)) {
          j = i;
        }
      }

      // when searchObject is not found
      if(j == -1) {
        throw LOG.unableToFindProperty(node.getNodeType().name());
      }

      return j;
    } else {
      throw LOG.unableToGetIndex(jsonNode.getNodeType().name());
    }
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

    return this;
  }

  public SpinJsonNode prop(String name, Number newProperty) {
    ObjectNode node = (ObjectNode) jsonNode;

    // Numbers magic because Jackson has no native .put(Number value)
    if (newProperty == null) {
      node.putNull(name);
    } else if(newProperty instanceof Long) {
      node.put(name, newProperty.longValue());
    } else if(newProperty instanceof Integer) {
      node.put(name, newProperty.intValue());
    } else if(newProperty instanceof Float) {
        node.put(name, newProperty.floatValue());
    } else {
      // convert any other sub class of Number into Float
      node.put(name, newProperty.floatValue());
    }

    return this;
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

    node.put(name, newProperty);

    return this;
  }

  public SpinJsonNode prop(String name, List<Object> newProperty) {
    ObjectNode node = (ObjectNode) jsonNode;

    node.set(name, dataFormat.createJsonNode(newProperty));

    return this;
  }

  public SpinJsonNode prop(String name, Map<String, Object> newProperty) {
    ObjectNode node = (ObjectNode) jsonNode;

    node.set(name, dataFormat.createJsonNode(newProperty));

    return this;
  }

  public SpinJsonNode prop(String name, SpinJsonNode newProperty) {
    ObjectNode node = (ObjectNode) jsonNode;

    if (newProperty != null) {
      node.set(name, (JsonNode) newProperty.unwrap());
    }
    else {
      node.putNull(name);
    }

    return this;
  }

  public SpinJsonNode deleteProp(String name) {
    ensureNotNull("name", name);

    if(jsonNode.has(name)) {
      ObjectNode node = (ObjectNode) jsonNode;
      node.remove(name);
      return this;
    } else {
      throw LOG.unableToFindProperty(name);
    }
  }

  public SpinJsonNode deleteProp(List<String> names) {
    ensureNotNull("names", names);

    for(String name: names) {
      deleteProp(name);
    }

    return this;
  }

  public SpinJsonNode append(Object property) {
    ensureNotNull("property", property);
    if(jsonNode.isArray()) {
      ArrayNode node = (ArrayNode) jsonNode;

      node.add(dataFormat.createJsonNode(property));

      return this;
    } else {
      throw LOG.unableToModifyNode(jsonNode.getNodeType().name());
    }
  }

  public SpinJsonNode insertAt(int index, Object property) {
    ensureNotNull("property", property);

    if(jsonNode.isArray()) {
      index = getCorrectIndex(index);
      ArrayNode node = (ArrayNode) jsonNode;

      node.insert(index, dataFormat.createJsonNode(property));

      return this;
    } else {
      throw LOG.unableToModifyNode(jsonNode.getNodeType().name());
    }
  }

  public SpinJsonNode insertBefore(Object searchObject, Object insertObject) {
    ensureNotNull("searchObject", searchObject);
    ensureNotNull("insertObject", insertObject);
    if(this.isArray()) {
      Integer i = indexOf(searchObject);

      return insertAt(i, insertObject);

    } else {
      throw LOG.unableToCreateNode(jsonNode.getNodeType().name());
    }
  }

  public SpinJsonNode insertAfter(Object searchObject, Object insertObject) {
    ensureNotNull("searchObject", searchObject);
    ensureNotNull("insertObject", insertObject);
    if(this.isArray()) {
      Integer i = indexOf(searchObject);

      return insertAt(i + 1, insertObject);

    } else {
      throw LOG.unableToCreateNode(jsonNode.getNodeType().name());
    }
  }

  public SpinJsonNode remove(Object property) {
    return removeAt(indexOf(property));
  }

  public SpinJsonNode removeLast(Object property) {
    return removeAt(lastIndexOf(property));
  }

  public SpinJsonNode removeAt(int index) {
    if(this.isArray()) {
      ArrayNode node = (ArrayNode) jsonNode;

      node.remove(getCorrectIndex(index));

      return this;
    } else {
      throw LOG.unableToModifyNode(jsonNode.getNodeType().name());
    }
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

  public Boolean isNull() {
    return jsonNode.isNull();
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

    if (jsonNode.isNull()) {
      return null;
    }

    throw LOG.unableToParseValue("String/Number/Boolean/Null", jsonNode.getNodeType());
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

  public JsonNodeType getNodeType() {
    return jsonNode.getNodeType();
  }

  public SpinJsonPathQuery jsonPath(String expression) {
    ensureNotNull("expression", expression);
    try {
      JsonPath query = JsonPath.compile(expression);
      return new JacksonJsonPathQuery(this, query, dataFormat);
    } catch(InvalidPathException pex) {
      throw LOG.unableToCompileJsonPathExpression(expression, pex);
    } catch(IllegalArgumentException aex) {
      throw LOG.unableToCompileJsonPathExpression(expression, aex);
    }
  }

  /**
   * Maps the json represented by this object to a java object of the given type.
   *
   * @throws SpinJsonException if the json representation cannot be mapped to the specified type
   */
  public <C> C mapTo(Class<C> type) {
    DataFormatMapper mapper = dataFormat.getMapper();
    return mapper.mapInternalToJava(jsonNode, type);
  }

  /**
   * Maps the json represented by this object to a java object of the given type.
   * Argument is to be supplied in Jackson's canonical type string format
   * (see {@link JavaType#toCanonical()}).
   *
   * @throws SpinJsonException if the json representation cannot be mapped to the specified type
   * @throws SpinJsonDataFormatException if the parameter does not match a valid type
   */
  public <C> C mapTo(String type) {
    DataFormatMapper mapper = dataFormat.getMapper();
    return mapper.mapInternalToJava(jsonNode, type);
  }

  /**
   * Maps the json represented by this object to a java object of the given type.
   *
   * @throws SpinJsonException if the json representation cannot be mapped to the specified type
   */
  public <C> C mapTo(JavaType type) {
    JacksonJsonDataFormatMapper mapper = dataFormat.getMapper();
    return mapper.mapInternalToJava(jsonNode, type);
  }
}
