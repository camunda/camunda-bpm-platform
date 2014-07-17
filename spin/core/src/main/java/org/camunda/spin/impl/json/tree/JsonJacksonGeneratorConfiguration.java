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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonJacksonGeneratorConfiguration extends AbstractConfiguration<JsonJacksonGeneratorConfiguration> implements JsonJacksonTreeConfigurable {

  protected JsonJacksonTreeDataFormat dataFormat;
  
  public JsonJacksonGeneratorConfiguration(JsonJacksonTreeDataFormat dataFormat) {
    this.dataFormat = dataFormat;
  }
  
  public JsonJacksonGeneratorConfiguration(JsonJacksonTreeDataFormat dataFormat, 
      JsonJacksonGeneratorConfiguration generatorConfiguration) {
    super(generatorConfiguration);
    this.dataFormat = dataFormat;
  }

  public JsonJacksonParserConfiguration reader() {
    return dataFormat.reader();
  }

  public JsonJacksonGeneratorConfiguration writer() {
    return dataFormat.writer();
  }

  public JsonJacksonTreeDataFormat done() {
    return dataFormat;
  }

  protected JsonJacksonGeneratorConfiguration thisConfiguration() {
    return this;
  }
  
  public JsonJacksonGeneratorConfiguration config(Map<String, Object> config) {
    dataFormat.invalidateCachedObjectMapper();
    return super.config(config);
  }
  
  public JsonJacksonGeneratorConfiguration config(String key, Object value) {
    dataFormat.invalidateCachedObjectMapper();
    return super.config(key, value);
  }
  
  public JsonJacksonGeneratorConfiguration config(JsonGenerator.Feature feature, Object value) {
    return config(feature.name(), value);
  }
  
  public Boolean getValue(JsonGenerator.Feature feature) {
    Boolean value = (Boolean) configuration.get(feature.name());
    if (value == null) {
      return feature.enabledByDefault();
    }
    else {
      return value;
    }
  }

  public void applyTo(ObjectMapper mapper) {
    for (JsonGenerator.Feature feature : JsonGenerator.Feature.values()) {
      mapper.configure(feature, getValue(feature));
    }
  }
  
  public JsonJacksonGeneratorConfiguration escapeNonAscii(Boolean value) {
    return config(JsonGenerator.Feature.ESCAPE_NON_ASCII, value);
  }
  
  public Boolean escapesNonAscii() {
    return getValue(JsonGenerator.Feature.ESCAPE_NON_ASCII);
  }
  
  public JsonJacksonGeneratorConfiguration quoteFieldNames(Boolean value) {
    return config(JsonGenerator.Feature.QUOTE_FIELD_NAMES, value);
  }
  
  public Boolean quotesFieldNames() {
    return getValue(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
  }
  
  public JsonJacksonGeneratorConfiguration quoteNonNumericNumbers(Boolean value) {
    return config(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS, value);
  }
  
  public Boolean quotesNonNumericNumbers() {
    return getValue(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS);
  }
  
  public JsonJacksonGeneratorConfiguration writeNumbersAsString(Boolean value) {
    return config(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, value);
  }
  
  public Boolean writesNumbersAsString() {
    return getValue(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
  }
}
