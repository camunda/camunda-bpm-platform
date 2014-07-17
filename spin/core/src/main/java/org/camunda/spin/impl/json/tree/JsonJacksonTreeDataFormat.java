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
import org.camunda.spin.spi.DataFormat;
import org.camunda.spin.spi.DataFormatReader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
/**
 * @author Thorben Lindhauer
 * @author Stefan Hentschel
 */
public class JsonJacksonTreeDataFormat implements DataFormat<SpinJsonNode>, JsonJacksonTreeConfigurable {

  public static final JsonJacksonTreeDataFormat INSTANCE = new JsonJacksonTreeDataFormat();
  
  protected JsonJacksonParserConfiguration parserConfiguration;
  protected JsonJacksonGeneratorConfiguration generatorConfiguration;
  protected ObjectMapper cachedObjectMapper;
  
  public JsonJacksonTreeDataFormat() {
    this.parserConfiguration = new JsonJacksonParserConfiguration(this);
    this.generatorConfiguration = new JsonJacksonGeneratorConfiguration(this);
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

  public JsonJacksonTreeDataFormat done() {
    return this;
  }

  public void applyTo(ObjectMapper mapper) {
    parserConfiguration.applyTo(mapper);
    generatorConfiguration.applyTo(mapper);
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
}
