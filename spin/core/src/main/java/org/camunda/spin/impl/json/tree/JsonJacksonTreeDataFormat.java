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

import java.util.ArrayList;
import java.util.List;

import org.camunda.spin.impl.json.tree.type.DefaultJsonJacksonTypeDetector;
import org.camunda.spin.impl.json.tree.type.ListJsonJacksonTypeDetector;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.logging.SpinLogger;
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.spi.DataFormatReader;
import org.camunda.spin.spi.TypeDetector;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Spin data format that can wrap Json content and uses 
 * <a href="http://wiki.fasterxml.com/JacksonHome">Jackson</a> as its implementation.
 * Caches an instance of {@link ObjectMapper} as long as configuration does not change
 * according to the advice given in the 
 * <a href="http://wiki.fasterxml.com/JacksonBestPracticesPerformance">Jackson documentation</a>.
 * 
 * 
 * @author Thorben Lindhauer
 * @author Stefan Hentschel
 */
public class JsonJacksonTreeDataFormat implements DataFormat<SpinJsonNode>, JsonJacksonTreeConfigurable {

  public static final JsonJacksonTreeDataFormat INSTANCE = new JsonJacksonTreeDataFormat();
  private static final JsonJacksonTreeLogger LOG = SpinLogger.JSON_TREE_LOGGER;
  
  protected JsonJacksonParserConfiguration parserConfiguration;
  protected JsonJacksonGeneratorConfiguration generatorConfiguration;
  protected JsonJacksonMapperConfiguration mapperConfiguration;
  protected ObjectMapper cachedObjectMapper;
  protected List<TypeDetector> typeDetectors;
  
  public JsonJacksonTreeDataFormat() {
    this.parserConfiguration = new JsonJacksonParserConfiguration(this);
    this.generatorConfiguration = new JsonJacksonGeneratorConfiguration(this);
    this.mapperConfiguration = new JsonJacksonMapperConfiguration(this);
    
    typeDetectors = new ArrayList<TypeDetector>();
    typeDetectors.add(new ListJsonJacksonTypeDetector());
    typeDetectors.add(new DefaultJsonJacksonTypeDetector());
  }
  
  public Class<? extends SpinJsonNode> getWrapperType() {
    return SpinJsonJacksonTreeNode.class;
  }

  public SpinJsonNode createWrapperInstance(Object parameter) {
    return new SpinJsonJacksonTreeNode((JsonNode) parameter, this);
  }

  public String getName() {
    return "application/json; implementation=tree";
  }
  
  // configuration
  public JsonJacksonTreeDataFormat newInstance() {
    JsonJacksonTreeDataFormat instance = new JsonJacksonTreeDataFormat();
    
    instance.cachedObjectMapper = cachedObjectMapper;
    
    instance.parserConfiguration = 
        new JsonJacksonParserConfiguration(instance, parserConfiguration);
    instance.generatorConfiguration = 
        new JsonJacksonGeneratorConfiguration(instance, generatorConfiguration);
    instance.mapperConfiguration =
        new JsonJacksonMapperConfiguration(instance, mapperConfiguration);
    
    instance.typeDetectors = new ArrayList<TypeDetector>(typeDetectors);
    
    return instance;
  }
  
  public DataFormatReader getReader() {
    return new JsonJacksonTreeDataFormatReader(this);
  }

  public JsonJacksonParserConfiguration reader() {
    return parserConfiguration;
  }

  public JsonJacksonGeneratorConfiguration writer() {
    return generatorConfiguration;
  }
  
  public JsonJacksonMapperConfiguration mapper() {
    return mapperConfiguration;
  }

  public JsonJacksonTreeDataFormat done() {
    return this;
  }

  public void applyTo(ObjectMapper mapper) {
    parserConfiguration.applyTo(mapper);
    generatorConfiguration.applyTo(mapper);
    mapperConfiguration.applyTo(mapper);
  }
  
  public ObjectMapper getConfiguredObjectMapper() {
    if (cachedObjectMapper == null) {
      synchronized(this) {
        if (cachedObjectMapper == null) {
          cachedObjectMapper = new ObjectMapper();
          applyTo(cachedObjectMapper);
        }
      }
    }
    
    return cachedObjectMapper;
  }
  
  public synchronized void invalidateCachedObjectMapper() {
    cachedObjectMapper = null;
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
      if (typeDetector.appliesTo(this) && typeDetector.canHandle(object)) {
        return typeDetector.detectType(object);
      }
    }
    
    throw LOG.unableToDetectCanonicalType(object);
  }
  
  /**
   * Constructs a {@link JavaType} object based on the parameter, which
   * has to follow Jackson's canonical type string format.
   * 
   * @param canonicalString
   * @return
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
}
