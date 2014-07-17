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

import java.util.Map;

import org.camunda.spin.spi.AbstractConfiguration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonJacksonParserConfiguration extends AbstractConfiguration<JsonJacksonParserConfiguration>  implements JsonJacksonTreeConfigurable {

  private JsonJacksonTreeDataFormat dataFormat;
  
  public JsonJacksonParserConfiguration(JsonJacksonTreeDataFormat dataFormat) {
    this.dataFormat = dataFormat;
  }
  
  public JsonJacksonParserConfiguration(JsonJacksonTreeDataFormat dataFormat, 
      JsonJacksonParserConfiguration configuration) {
    super(configuration);
    this.dataFormat = dataFormat;
  }

  public JsonJacksonParserConfiguration reader() {
    return this;
  }

  public JsonJacksonGeneratorConfiguration writer() {
    return dataFormat.writer();
  }

  public JsonJacksonTreeDataFormat done() {
    return dataFormat;
  }

  protected JsonJacksonParserConfiguration thisConfiguration() {
    return this;
  }
  
  public JsonJacksonParserConfiguration config(Map<String, Object> config) {
    dataFormat.invalidateCachedObjectMapper();
    return super.config(config);
  }
  
  public JsonJacksonParserConfiguration config(String key, Object value) {
    dataFormat.invalidateCachedObjectMapper();
    return super.config(key, value);
  }
  
  public JsonJacksonParserConfiguration config(JsonParser.Feature feature, Object value) {
    return config(feature.name(), value);
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

  public JsonJacksonParserConfiguration allowNumericLeadingZeros(Boolean value) {
    return config(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, value);
  }

  public Map<String, Object> getConfiguration() {
    return configuration;
  }

  public Boolean allowsComments() {
    return getValue(JsonParser.Feature.ALLOW_COMMENTS);
  }

  public JsonJacksonParserConfiguration allowComments(Boolean value) {
    return config(JsonParser.Feature.ALLOW_COMMENTS, value);
  }

  public Boolean allowYamlComments() {
    return getValue(JsonParser.Feature.ALLOW_YAML_COMMENTS);
  }

  public JsonJacksonParserConfiguration allowYamlComments(Boolean value) {
    return config(JsonParser.Feature.ALLOW_YAML_COMMENTS, value);
  }

  public Boolean allowsUnquotedFieldNames() {
    return getValue(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
  }

  public JsonJacksonParserConfiguration allowQuotedFieldNames(Boolean value) {
    return config(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, value);
  }

  public Boolean allowsSingleQuotes() {
    return getValue(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
  }

  public JsonJacksonParserConfiguration allowSingleQuotes(Boolean value) {
    return config(JsonParser.Feature.ALLOW_SINGLE_QUOTES, value);
  }

  public Boolean allowsBackslashEscapingAnyCharacter() {
    return getValue(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
  }

  public JsonJacksonParserConfiguration allowBackslashEscapingAnyCharacter(Boolean value) {
    return config(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, value);
  }

  public Boolean allowsNonNumericNumbers() {
    return getValue(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
  }

  public JsonJacksonParserConfiguration allowNonNumericNumbers(Boolean value) {
    return config(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS, value);
  }

  public void applyTo(ObjectMapper mapper) {
    for (JsonParser.Feature feature : JsonParser.Feature.values()) {
      mapper.configure(feature, getValue(feature));
    }
  }

}
