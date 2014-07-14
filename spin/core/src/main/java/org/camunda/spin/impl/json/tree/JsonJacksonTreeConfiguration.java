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

import com.fasterxml.jackson.core.JsonParser.Feature;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Thorben Lindhauer
 */
public class JsonJacksonTreeConfiguration implements JsonJacksonTreeConfigurable<JsonJacksonTreeConfiguration> {

  protected Map<String, Object> configuration;

  public JsonJacksonTreeConfiguration() {
    this.configuration = new HashMap<String, Object>();
  }
  
  public JsonJacksonTreeConfiguration(JsonJacksonTreeConfiguration other) {
    this.configuration = new HashMap<String, Object>(other.configuration);
  }
  
  public JsonJacksonTreeConfiguration config(String key, Object value) {
    configuration.put(key, value);
    return this;
  }

  public JsonJacksonTreeConfiguration config(Map<String, Object> config) {
    configuration.putAll(config);
    return this;
  }

  public JsonJacksonTreeConfiguration config(Feature feature, Object value) {
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

  public Boolean getValue(Feature feature) {
    Boolean value = (Boolean) configuration.get(feature.name());
    if (value == null) {
      return feature.enabledByDefault();
    }
    else {
      return value;
    }
  }

  public Boolean allowsNumericLeadingZeros() {
    return getValue(Feature.ALLOW_NUMERIC_LEADING_ZEROS);
  }

  public JsonJacksonTreeConfiguration allowNumericLeadingZeros(Boolean value) {
    return config(Feature.ALLOW_NUMERIC_LEADING_ZEROS, value);
  }

  public Map<String, Object> getConfiguration() {
    return configuration;
  }

  public Boolean allowsComments() {
    return getValue(Feature.ALLOW_COMMENTS);
  }

  public JsonJacksonTreeConfiguration allowComments(Boolean value) {
    return config(Feature.ALLOW_COMMENTS, value);
  }

  public Boolean allowYamlComments() {
    return getValue(Feature.ALLOW_YAML_COMMENTS);
  }

  public JsonJacksonTreeConfiguration allowYamlComments(Boolean value) {
    return config(Feature.ALLOW_YAML_COMMENTS, value);
  }

  public Boolean allowsUnquotedFieldNames() {
    return getValue(Feature.ALLOW_UNQUOTED_FIELD_NAMES);
  }

  public JsonJacksonTreeConfiguration allowQuotedFieldNames(Boolean value) {
    return config(Feature.ALLOW_UNQUOTED_FIELD_NAMES, value);
  }

  public Boolean allowsSingleQuotes() {
    return getValue(Feature.ALLOW_SINGLE_QUOTES);
  }

  public JsonJacksonTreeConfiguration allowSingleQuotes(Boolean value) {
    return config(Feature.ALLOW_SINGLE_QUOTES, value);
  }

  public Boolean allowsBackslashEscapingAnyCharacter() {
    return getValue(Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
  }

  public JsonJacksonTreeConfiguration allowBackslashEscapingAnyCharacter(Boolean value) {
    return config(Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, value);
  }

  public Boolean allowsNonNumericNumbers() {
    return getValue(Feature.ALLOW_NON_NUMERIC_NUMBERS);
  }

  public JsonJacksonTreeConfiguration allowNonNumericNumbers(Boolean value) {
    return config(Feature.ALLOW_NON_NUMERIC_NUMBERS, value);
  }

}
