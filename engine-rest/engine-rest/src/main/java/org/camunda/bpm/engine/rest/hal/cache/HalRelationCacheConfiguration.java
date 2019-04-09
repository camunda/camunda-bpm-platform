/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.rest.hal.cache;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.camunda.bpm.engine.rest.cache.Cache;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HalRelationCacheConfiguration {

  public final static String CONFIG_CACHE_IMPLEMENTATION = "cacheImplementation";
  public static final String CONFIG_CACHES = "caches";

  protected ObjectMapper objectMapper = new ObjectMapper();
  protected Class<? extends Cache> cacheImplementationClass;
  protected Map<Class<?>, Map<String, Object>> cacheConfigurations;

  public HalRelationCacheConfiguration() {
    cacheConfigurations = new HashMap<Class<?>, Map<String, Object>>();
  }

  public HalRelationCacheConfiguration(String configuration) {
    this();
    parseConfiguration(configuration);
  }

  public Class<? extends Cache> getCacheImplementationClass() {
    return cacheImplementationClass;
  }

  @SuppressWarnings("unchecked")
  public void setCacheImplementationClass(Class<?> cacheImplementationClass) {
    if (Cache.class.isAssignableFrom(cacheImplementationClass)) {
      this.cacheImplementationClass = (Class<? extends Cache>) cacheImplementationClass;
    }
    else {
      throw new HalRelationCacheConfigurationException("Cache implementation class " + cacheImplementationClass.getName() + " does not implement the interface " + Cache.class.getName());
    }
  }

  public Map<Class<?>, Map<String, Object>> getCacheConfigurations() {
    return cacheConfigurations;
  }

  public void setCacheConfigurations(Map<Class<?>, Map<String, Object>> cacheConfigurations) {
    this.cacheConfigurations = cacheConfigurations;
  }

  public void addCacheConfiguration(Class<?> halResourceClass, Map<String, Object> cacheConfiguration) {
    this.cacheConfigurations.put(halResourceClass, cacheConfiguration);
  }

  protected void parseConfiguration(String configuration) {
    try {
      JsonNode jsonConfiguration = objectMapper.readTree(configuration);
      parseConfiguration(jsonConfiguration);
    } catch (IOException e) {
      throw new HalRelationCacheConfigurationException("Unable to parse cache configuration", e);
    }
  }

  protected void parseConfiguration(JsonNode jsonConfiguration) {
    parseCacheImplementationClass(jsonConfiguration);
    parseCacheConfigurations(jsonConfiguration);
  }

  protected void parseCacheImplementationClass(JsonNode jsonConfiguration) {
    JsonNode jsonNode = jsonConfiguration.get(CONFIG_CACHE_IMPLEMENTATION);
    if (jsonNode != null) {
      String cacheImplementationClassName = jsonNode.textValue();
      Class<?> cacheImplementationClass = loadClass(cacheImplementationClassName);
      setCacheImplementationClass(cacheImplementationClass);
    }
    else {
      throw new HalRelationCacheConfigurationException("Unable to find the " + CONFIG_CACHE_IMPLEMENTATION + " parameter");
    }
  }

  protected void parseCacheConfigurations(JsonNode jsonConfiguration) {
    JsonNode jsonNode = jsonConfiguration.get(CONFIG_CACHES);
    if (jsonNode != null) {
      Iterator<Entry<String, JsonNode>> cacheConfigurations = jsonNode.fields();
      while (cacheConfigurations.hasNext()) {
        Entry<String, JsonNode> cacheConfiguration = cacheConfigurations.next();
        parseCacheConfiguration(cacheConfiguration.getKey(), cacheConfiguration.getValue());
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected void parseCacheConfiguration(String halResourceClassName, JsonNode jsonConfiguration) {
    try {
      Class<?> halResourceClass = loadClass(halResourceClassName);
      Map<String, Object> configuration = objectMapper.treeToValue(jsonConfiguration, Map.class);
      addCacheConfiguration(halResourceClass, configuration);
    } catch (IOException e) {
      throw new HalRelationCacheConfigurationException("Unable to parse cache configuration for HAL resource " + halResourceClassName);
    }
  }

  protected Class<?> loadClass(String className) {
    try {
      // use classloader which loaded the REST api classes
      return Class.forName(className, true, HalRelationCacheConfiguration.class.getClassLoader());
    }
    catch (ClassNotFoundException e) {
      throw new HalRelationCacheConfigurationException("Unable to load class of cache configuration " + className, e);
    }
  }

}
