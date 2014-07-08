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
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.spin.DataFormats.jsonTree;
import static org.camunda.spin.Spin.JSON;
import static org.camunda.spin.Spin.S;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JACKSON_CONFIGURATION_JSON;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.camunda.spin.impl.json.tree.JsonJacksonTreeConfiguration;
import org.camunda.spin.impl.json.tree.JsonJacksonTreeDataFormat;
import org.camunda.spin.impl.util.IoUtil;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.spi.SpinJsonDataFormatException;
import org.junit.Before;
import org.junit.Test;

public class JsonTreeConfigureTest {

  private JsonJacksonTreeDataFormat dataFormatInstance;
  private Map<String, Object> configurationMap;
  
  @Before
  public void setUp() {
    dataFormatInstance = 
        jsonTree()
            .allowNumericLeadingZeros(Boolean.TRUE)
            .allowBackslashEscapingAnyCharacter(Boolean.TRUE)
            .allowComments(Boolean.TRUE)
            .allowNonNumericNumbers(Boolean.TRUE)
            .allowQuotedFieldNames(Boolean.TRUE)
            .allowSingleQuotes(Boolean.TRUE);
    
    configurationMap = new HashMap<String, Object>();
    configurationMap.put(JsonJacksonTreeConfiguration.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, Boolean.TRUE);
    configurationMap.put(JsonJacksonTreeConfiguration.ALLOW_COMMENTS, Boolean.TRUE);
    configurationMap.put(JsonJacksonTreeConfiguration.ALLOW_NON_NUMERIC_NUMBERS, Boolean.TRUE);
    configurationMap.put(JsonJacksonTreeConfiguration.ALLOW_NUMERIC_LEADING_ZEROS, Boolean.TRUE);
    configurationMap.put(JsonJacksonTreeConfiguration.ALLOW_SINGLE_QUOTES, Boolean.TRUE);
    configurationMap.put(JsonJacksonTreeConfiguration.ALLOW_UNQUOTED_FIELD_NAMES, Boolean.TRUE);
  }
  
  
  @Test
  public void shouldApplyConfigurationOnCreation() {
    try {
      S(EXAMPLE_JACKSON_CONFIGURATION_JSON, jsonTree());
      fail("Expected SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException e) {
      // happy path
    }
    
    SpinJsonNode json = S(EXAMPLE_JACKSON_CONFIGURATION_JSON, dataFormatInstance);
    assertThat(json).isNotNull();
    
    json = JSON(EXAMPLE_JACKSON_CONFIGURATION_JSON, dataFormatInstance);
    assertThat(json).isNotNull();
    
    json = JSON(EXAMPLE_JACKSON_CONFIGURATION_JSON, configurationMap);
    assertThat(json).isNotNull();
  }
  
  @Test
  public void shouldApplyConfigurationOnCreationFromInputStream() {
    InputStream input = IoUtil.stringAsInputStream(EXAMPLE_JACKSON_CONFIGURATION_JSON);
    
    try {
      S(input, jsonTree());
      fail("Expected SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException e) {
      // happy path
    }
    
    input = IoUtil.stringAsInputStream(EXAMPLE_JACKSON_CONFIGURATION_JSON);
    SpinJsonNode json = S(input, dataFormatInstance);
    assertThat(json).isNotNull();
    
    input = IoUtil.stringAsInputStream(EXAMPLE_JACKSON_CONFIGURATION_JSON);
    json = JSON(input, dataFormatInstance);
    assertThat(json).isNotNull();
    
    input = IoUtil.stringAsInputStream(EXAMPLE_JACKSON_CONFIGURATION_JSON);
    json = JSON(input, configurationMap);
    assertThat(json).isNotNull();
  }
  
  @Test
  public void shouldCreateNewInstanceOnConfiguration() {
    JsonJacksonTreeDataFormat jsonDataFormat = new JsonJacksonTreeDataFormat();
    jsonDataFormat.config("aKey", "aValue");
    
    JsonJacksonTreeDataFormat jsonDataFormatInstance = 
        jsonDataFormat.newInstance().config("anotherKey", "anotherValue");
    
    assertThat(jsonDataFormat.getValue("aKey")).isEqualTo("aValue");
    assertThat(jsonDataFormat.getValue("anotherKey")).isNull();
    
    assertThat(jsonDataFormatInstance.getValue("aKey").equals("aValue"));
    assertThat(jsonDataFormatInstance.getValue("anotherKey").equals("anotherValue"));
    
    JsonJacksonTreeDataFormat nextReturnedDataFormatInstance = 
        jsonDataFormatInstance.config("aThirdKey", "aThirdValue");
    assertThat(nextReturnedDataFormatInstance).isSameAs(jsonDataFormatInstance);
    assertThat(jsonDataFormatInstance.getValue("aThirdKey").equals("aThirdValue"));
  }
}
