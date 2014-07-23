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
package org.camunda.spin.json.tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.spin.DataFormats.jsonTree;
import static org.camunda.spin.Spin.JSON;

import java.util.HashMap;
import java.util.Map;

import org.camunda.spin.impl.json.tree.JsonJacksonTreeDataFormat;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonTreeConfigureWriterTest {

  // support this?
//configurationMap.put(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN.name(), Boolean.TRUE);
  
  @Test
  public void shouldEscapeNonAscii() {
    String input = "{\"prop\" : \"ä\"}";
    
    String json = JSON(input, jsonTree().writer().escapeNonAscii(Boolean.TRUE).done())
      .toString();
    
    assertThat(json).isEqualTo("{\"prop\":\"\\u00E4\"}");
    
    Map<String, Object> config = newMap(JsonGenerator.Feature.ESCAPE_NON_ASCII.name(), Boolean.TRUE);
    json = JSON(input, jsonTree().writer().config(config).done()).toString();
    
    assertThat(json).isEqualTo("{\"prop\":\"\\u00E4\"}");
  }
  
  @Test
  public void shouldNotQuoteFieldNames() {
    String input = "{\"prop\" : \"value\"}";
    
    String json = JSON(input, jsonTree().writer().quoteFieldNames(Boolean.FALSE).done())
      .toString();
    
    assertThat(json).isEqualTo("{prop:\"value\"}");
    
    Map<String, Object> config = newMap(JsonGenerator.Feature.QUOTE_FIELD_NAMES.name(), Boolean.FALSE);
    json = JSON(input, jsonTree().writer().config(config).done()).toString();
    
    assertThat(json).isEqualTo("{prop:\"value\"}");
  }
  
  @Test
  public void shouldNotQuoteNonNumericNumbers() {
    String input = "{}";
    
    String json = JSON(input, jsonTree().writer().quoteNonNumericNumbers(Boolean.FALSE).done())
        .prop("prop", Double.POSITIVE_INFINITY).toString();
    
    assertThat(json).isEqualTo("{\"prop\":Infinity}");
    
    Map<String, Object> config = 
        newMap(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS.name(), Boolean.FALSE);
    json = JSON(input, jsonTree().writer().config(config).done())
        .prop("prop", Double.POSITIVE_INFINITY).toString();
    
    assertThat(json).isEqualTo("{\"prop\":Infinity}");
  }
  
  @Test
  public void shouldWriteNumbersAsStrings() {
    String input = "{\"prop\" : 123}";
    
    String json = JSON(input, jsonTree().writer().writeNumbersAsString(Boolean.TRUE).done())
      .toString();
    
    assertThat(json).isEqualTo("{\"prop\":\"123\"}");
    
    Map<String, Object> config = 
        newMap(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.name(), Boolean.TRUE);
    json = JSON(input, jsonTree().writer().config(config).done()).toString();
    
    assertThat(json).isEqualTo("{\"prop\":\"123\"}");
  }
  
  @Test
  public void shouldWriteWithNullConfig() {
    String input = "{\"prop\" : 123}";
    
    String json = JSON(input, jsonTree().writer().config(null).done())
      .toString();
    
    assertThat(json).isEqualTo("{\"prop\":123}");
  }
  
  @Test
  public void shouldCacheObjectMapper() {
    JsonJacksonTreeDataFormat dataFormatInstance = jsonTree();
    
    // object mapper should be cached when configuration does not change
    ObjectMapper objectMapper1 = dataFormatInstance.getConfiguredObjectMapper();
    ObjectMapper objectMapper2 = dataFormatInstance.getConfiguredObjectMapper();
    
    assertThat(objectMapper1).isSameAs(objectMapper2);
    
    // changing the configuration should create a new object mapper
    dataFormatInstance.writer().escapeNonAscii(Boolean.FALSE);
    
    ObjectMapper objectMapper3 = dataFormatInstance.getConfiguredObjectMapper();
    
    assertThat(objectMapper3).isNotSameAs(objectMapper2);
    
    // a new format should use the same mapper as long as it is not configured
    JsonJacksonTreeDataFormat newFormat = dataFormatInstance.newInstance();
    
    ObjectMapper objectMapper4 = newFormat.getConfiguredObjectMapper();
    
    assertThat(objectMapper4).isSameAs(objectMapper3);
  }
  
  protected Map<String, Object> newMap(String key, Object value) {
    Map<String, Object> result = new HashMap<String, Object>();
    result.put(key, value);
    
    return result;
  }
}
