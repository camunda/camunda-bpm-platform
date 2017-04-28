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
package org.camunda.spin.impl.json.jackson.format;

import static org.camunda.commons.utils.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.camunda.spin.DataFormats;
import org.camunda.spin.impl.json.jackson.JacksonJsonLogger;
import org.camunda.spin.impl.json.jackson.JacksonJsonNode;
import org.camunda.spin.json.SpinJsonDataFormatException;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.spi.TypeDetector;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Configuration.ConfigurationBuilder;

/**
 * Spin data format that can wrap Json content and uses
 * <a href="http://wiki.fasterxml.com/JacksonHome">Jackson</a> as its implementation.
 * Caches an instance of {@link ObjectMapper} according to the advice given in the
 * <a href="http://wiki.fasterxml.com/JacksonBestPracticesPerformance">Jackson documentation</a>.
 *
 *
 * @author Thorben Lindhauer
 * @author Stefan Hentschel
 */
public class JacksonJsonDataFormat implements DataFormat<SpinJsonNode> {

  public static final String DATA_FORMAT_NAME = DataFormats.JSON_DATAFORMAT_NAME;

  private static final JacksonJsonLogger LOG = JacksonJsonLogger.JSON_TREE_LOGGER;

  /** The Jackson Object Mapper used by this dataformat */
  protected ObjectMapper objectMapper;

  /** The JsonPath configuration */
  protected Configuration jsonPathConfiguration;

  protected List<TypeDetector> typeDetectors;

  protected JacksonJsonDataFormatReader dataFormatReader;
  protected JacksonJsonDataFormatWriter dataFormatWriter;
  protected JacksonJsonDataFormatMapper dataFormatMapper;

  protected final String name;

  public JacksonJsonDataFormat(String name) {
    this(name, new ObjectMapper());
  }

  public JacksonJsonDataFormat(String name, ObjectMapper objectMapper) {

    this(name, objectMapper,
        new ConfigurationBuilder()
          .jsonProvider(new JacksonJsonProvider(objectMapper))
          .mappingProvider(new JacksonMappingProvider(objectMapper))
          .build());
  }

  public JacksonJsonDataFormat(String name, ObjectMapper objectMapper, Configuration jsonPathConfiguration) {
    this.name = name;
    this.objectMapper = objectMapper;
    this.jsonPathConfiguration = jsonPathConfiguration;
    init();
  }


  // initialization /////////////////////////////////////////////

  protected void init() {
    initReader();
    initWriter();
    initMapper();
    initTypeDetectors();
  }

  protected void initMapper() {
    this.dataFormatMapper = new JacksonJsonDataFormatMapper(this);
  }

  protected void initWriter() {
    this.dataFormatWriter = new JacksonJsonDataFormatWriter(this);
  }

  protected void initReader() {
    this.dataFormatReader = new JacksonJsonDataFormatReader(this);
  }


  protected void initTypeDetectors() {
    typeDetectors = new ArrayList<TypeDetector>();
    typeDetectors.add(new ListJacksonJsonTypeDetector());
    typeDetectors.add(new DefaultJsonJacksonTypeDetector());
  }

  // interface implementation ///////////////////////////////////

  public String getName() {
    return DATA_FORMAT_NAME;
  }

  public Class<? extends SpinJsonNode> getWrapperType() {
    return JacksonJsonNode.class;
  }

  public SpinJsonNode createWrapperInstance(Object parameter) {
    return new JacksonJsonNode((JsonNode) parameter, this);
  }

  /**
   * Identifies the canonical type of an object heuristically.
   *
   * @return the canonical type identifier of the object's class
   * according to Jackson's type format (see {@link TypeFactory#constructFromCanonical(String)})
   */
  public String getCanonicalTypeName(Object object) {
    ensureNotNull("object", object);

    for (TypeDetector typeDetector : typeDetectors) {
      if (typeDetector.canHandle(object)) {
        return typeDetector.detectType(object);
      }
    }

    throw LOG.unableToDetectCanonicalType(object);
  }

  /**
   * Constructs a {@link JavaType} object based on the parameter, which
   * has to follow Jackson's canonical type string format.
   *
   * @param canonicalString canonical string representation of the type
   * @return the constructed java type
   * @throws SpinJsonDataFormatException if no type can be constructed from the given parameter
   */
  public JavaType constructJavaTypeFromCanonicalString(String canonicalString) {
    try {
      return TypeFactory.defaultInstance().constructFromCanonical(canonicalString);
    } catch (IllegalArgumentException e) {
      throw LOG.unableToConstructJavaType(canonicalString, e);
    }
  }

  public void addTypeDetector(TypeDetector typeDetector) {
    typeDetectors.add(0, typeDetector);
  }

  public JacksonJsonDataFormatMapper getMapper() {
    return dataFormatMapper;
  }

  public JacksonJsonDataFormatReader getReader() {
    return dataFormatReader;
  }

  public JacksonJsonDataFormatWriter getWriter() {
    return dataFormatWriter;
  }

  // resources //////////////////////////////////////////////////

  /**
   * Returns a {@link Configuration} object for jayway json path
   * which uses this dataformat's object mapper as {@link JsonProvider}.
   *
   * @return the {@link Configuration} for jsonpath
   */
  public Configuration getJsonPathConfiguration() {
    return jsonPathConfiguration;
  }

  public void setJsonPathConfiguration(Configuration jsonPathConfiguration) {
    this.jsonPathConfiguration = jsonPathConfiguration;
  }

  /**
   * Returns the configured Jackson {@link ObjectMapper} instance.
   * @return the configured object mapper.
   */
  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  // helper functions //////////////////////////////////////////

  @SuppressWarnings("unchecked")
  public JsonNode createJsonNode(Object parameter) {
    if(parameter instanceof SpinJsonNode) {
      return (JsonNode) ((SpinJsonNode) parameter).unwrap();

    } else if(parameter instanceof String) {
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

    } else if (parameter == null) {
      return createNullJsonNode();
    } else {
      throw LOG.unableToCreateNode(parameter.getClass().getSimpleName());
    }
  }

  public JsonNode createJsonNode(String parameter) {
    return objectMapper.getNodeFactory().textNode(parameter);
  }

  public JsonNode createJsonNode(Integer parameter) {
    return objectMapper.getNodeFactory().numberNode(parameter);
  }

  public JsonNode createJsonNode(Float parameter) {
    return objectMapper.getNodeFactory().numberNode(parameter);
  }

  public JsonNode createJsonNode(Long parameter) {
    return objectMapper.getNodeFactory().numberNode(parameter);
  }

  public JsonNode createJsonNode(Boolean parameter) {
    return objectMapper.getNodeFactory().booleanNode(parameter);
  }

  public JsonNode createJsonNode(List<Object> parameter) {
    if (parameter != null) {
      ArrayNode node = objectMapper.getNodeFactory().arrayNode();
      for(Object entry : parameter) {
        node.add(createJsonNode(entry));
      }
      return node;
    }
    else {
      return createNullJsonNode();
    }

  }

  public JsonNode createJsonNode(Map<String, Object> parameter) {
    if (parameter != null) {
      ObjectNode node = objectMapper.getNodeFactory().objectNode();
      for (Map.Entry<String, Object> entry : parameter.entrySet()) {
        node.set(entry.getKey(), createJsonNode(entry.getValue()));
      }
      return node;
    }
    else {
      return createNullJsonNode();
    }
  }

  public JsonNode createNullJsonNode() {
    return objectMapper.getNodeFactory().nullNode();
  }
}
