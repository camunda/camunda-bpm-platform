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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Wraps the configuration of Jackson's {@link ObjectMapper}. Caches an instance of {@link ObjectMapper}
 * as long as configuration does not change 
 * (cf. <a href="http://wiki.fasterxml.com/JacksonFAQThreadSafety">Jackson docs</a>).
 * 
 * @author Thorben Lindhauer
 */
public class JsonJacksonTreeConfiguration implements JsonJacksonTreeConfigurable<JsonJacksonTreeConfiguration> {

  protected Map<String, Object> configuration;
  protected ObjectMapper objectMapper;

  public JsonJacksonTreeConfiguration() {
    this.configuration = Collections.synchronizedMap(new HashMap<String, Object>());
  }
  
  public JsonJacksonTreeConfiguration(JsonJacksonTreeConfiguration other) {
    this.configuration = Collections.synchronizedMap(
        new HashMap<String, Object>(other.configuration));
    this.objectMapper = other.objectMapper;
  }

  public ObjectMapper getConfiguredObjectMapper() {
    if (objectMapper == null) {
      synchronized(this) {
        if (objectMapper == null) {
          objectMapper = new ObjectMapper();
          applyTo(objectMapper);
        }
      }
    }
    
    return objectMapper;
  }
  
  protected void applyTo(ObjectMapper mapper) {
    for (JsonParser.Feature feature : JsonParser.Feature.values()) {
      mapper.configure(feature, getValue(feature));
    }
  }
  
  public synchronized JsonJacksonTreeConfiguration config(String key, Object value) {
    configuration.put(key, value);
    resetObjectMapper();
    return this;
  }

  public synchronized JsonJacksonTreeConfiguration config(Map<String, Object> config) {
    configuration.putAll(config);
    resetObjectMapper();
    return this;
  }
  
  protected void resetObjectMapper() {
    objectMapper = null;
  }
  
  public JsonJacksonTreeConfiguration config(JsonParser.Feature feature, Object value) {
    return config(feature.name(), value);
  }

  public Object getValue(String key) {
    return configuration.get(key);
  }

  public Object getValue(String key, Object defaultValue) {
    if (configuration.get(key) == null) {
      return defaultValue;
    }
    
    return configuration.get(key);
  }

  public Boolean getValue(JsonParser.Feature feature) {
    Boolean value = (Boolean) configuration.get(feature.name());
    if (value == null) {
      return feature.enabledByDefault();
    }
    else {
      return value;
    }
  }

  public Boolean allowsNumericLeadingZeros() {
    return getValue(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS);
  }

  public JsonJacksonTreeConfiguration allowNumericLeadingZeros(Boolean value) {
    return config(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, value);
  }

  public Map<String, Object> getConfiguration() {
    return configuration;
  }

  public Boolean allowsComments() {
    return getValue(JsonParser.Feature.ALLOW_COMMENTS);
  }

  public JsonJacksonTreeConfiguration allowComments(Boolean value) {
    return config(JsonParser.Feature.ALLOW_COMMENTS, value);
  }

  public Boolean allowYamlComments() {
    return getValue(JsonParser.Feature.ALLOW_YAML_COMMENTS);
  }

  public JsonJacksonTreeConfiguration allowYamlComments(Boolean value) {
    return config(JsonParser.Feature.ALLOW_YAML_COMMENTS, value);
  }

  public Boolean allowsUnquotedFieldNames() {
    return getValue(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
  }

  public JsonJacksonTreeConfiguration allowQuotedFieldNames(Boolean value) {
    return config(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, value);
  }

  public Boolean allowsSingleQuotes() {
    return getValue(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
  }

  public JsonJacksonTreeConfiguration allowSingleQuotes(Boolean value) {
    return config(JsonParser.Feature.ALLOW_SINGLE_QUOTES, value);
  }

  public Boolean allowsBackslashEscapingAnyCharacter() {
    return getValue(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
  }

  public JsonJacksonTreeConfiguration allowBackslashEscapingAnyCharacter(Boolean value) {
    return config(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, value);
  }

  public Boolean allowsNonNumericNumbers() {
    return getValue(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
  }

  public JsonJacksonTreeConfiguration allowNonNumericNumbers(Boolean value) {
    return config(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, value);
  }

}
