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

import java.util.HashMap;
import java.util.Map;

public class JsonJacksonTreeConfiguration implements JsonJacksonTreeConfigurable<JsonJacksonTreeConfiguration> {

  public static final String ALLOW_COMMENTS = "allowComments";
  public static final String ALLOW_UNQUOTED_FIELD_NAMES = "allowUnquotedFieldNames";
  public static final String ALLOW_SINGLE_QUOTES = "allowSingleQuotes";
  public static final String ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER = "allowBackslashEscapingAnyCharacter";
  public static final String ALLOW_NUMERIC_LEADING_ZEROS = "allowNumericLeadingZeros";
  public static final String ALLOW_NON_NUMERIC_NUMBERS = "allowNonNumericNumbers";
  
  protected Map<String, Object> configuration;

  public JsonJacksonTreeConfiguration() {
    this.configuration = new HashMap<String, Object>();
  }
  
  public JsonJacksonTreeConfiguration(Map<String, Object> configuration) {
    this.configuration = configuration;
  }
  
  public JsonJacksonTreeConfiguration config(String key, Object value) {
    configuration.put(key, value);
    return this;
  }

  public JsonJacksonTreeConfiguration config(Map<String, Object> config) {
    configuration.putAll(config);
    return this;
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

  public Boolean allowsNumericLeadingZeros() {
    return (Boolean) getValue(ALLOW_NUMERIC_LEADING_ZEROS, Boolean.FALSE);
  }

  public JsonJacksonTreeConfiguration allowNumericLeadingZeros(Boolean value) {
    configuration.put(ALLOW_NUMERIC_LEADING_ZEROS, value);
    return this;
  }

  public Map<String, Object> getConfiguration() {
    return configuration;
  }

  public Boolean allowsComments() {
    return (Boolean) getValue(ALLOW_COMMENTS, Boolean.FALSE);
  }

  public JsonJacksonTreeConfiguration allowComments(Boolean value) {
    configuration.put(ALLOW_COMMENTS, value);
    return this;
  }

  public Boolean allowsUnquotedFieldNames() {
    return (Boolean) getValue(ALLOW_UNQUOTED_FIELD_NAMES, Boolean.FALSE);
  }

  public JsonJacksonTreeConfiguration allowQuotedFieldNames(Boolean value) {
    configuration.put(ALLOW_UNQUOTED_FIELD_NAMES, value);
    return this;
  }

  public Boolean allowsSingleQuotes() {
    return (Boolean) getValue(ALLOW_SINGLE_QUOTES, Boolean.FALSE);
  }

  public JsonJacksonTreeConfiguration allowSingleQuotes(Boolean value) {
    configuration.put(ALLOW_SINGLE_QUOTES, value);
    return this;
  }

  public Boolean allowsBackslashEscapingAnyCharacter() {
    return (Boolean) getValue(ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, Boolean.FALSE);
  }

  public JsonJacksonTreeConfiguration allowBackslashEscapingAnyCharacter(Boolean value) {
    configuration.put(ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, value);
    return this;
  }

  public Boolean allowsNonNumericNumbers() {
    return (Boolean) getValue(ALLOW_NON_NUMERIC_NUMBERS, Boolean.FALSE);
  }

  public JsonJacksonTreeConfiguration allowNonNumericNumbers(Boolean value) {
    configuration.put(ALLOW_NON_NUMERIC_NUMBERS, value);
    return this;
  }
  
  
}
