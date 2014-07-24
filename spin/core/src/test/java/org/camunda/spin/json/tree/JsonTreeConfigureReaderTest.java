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
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JACKSON_READ_CONFIGURATION_JSON;
import static org.camunda.spin.json.JsonTestConstants.EXAMPLE_JSON;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.camunda.spin.impl.json.tree.JsonJacksonTreeDataFormat;
import org.camunda.spin.impl.util.IoUtil;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.spi.SpinJsonDataFormatException;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonTreeConfigureReaderTest {

  private JsonJacksonTreeDataFormat dataFormatInstance;
  private Map<String, Object> configurationMap;

  @Before
  public void setUp() {
    dataFormatInstance =
        jsonTree().reader()
          .allowNumericLeadingZeros(Boolean.TRUE)
          .allowBackslashEscapingAnyCharacter(Boolean.TRUE)
          .allowComments(Boolean.TRUE)
          .allowYamlComments(Boolean.TRUE)
          .allowNonNumericNumbers(Boolean.TRUE)
          .allowQuotedFieldNames(Boolean.TRUE)
          .allowSingleQuotes(Boolean.TRUE)
          .done();

    configurationMap = new HashMap<String, Object>();
    configurationMap.put(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER.name(), Boolean.TRUE);
    configurationMap.put(JsonParser.Feature.ALLOW_COMMENTS.name(), Boolean.TRUE);
    configurationMap.put(JsonParser.Feature.ALLOW_YAML_COMMENTS.name(), Boolean.TRUE);
    configurationMap.put(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.name(), Boolean.TRUE);
    configurationMap.put(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.name(), Boolean.TRUE);
    configurationMap.put(JsonParser.Feature.ALLOW_SINGLE_QUOTES.name(), Boolean.TRUE);
    configurationMap.put(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.name(), Boolean.TRUE);
  }


  @Test
  public void shouldApplyConfigurationOnCreation() {
    try {
      S(EXAMPLE_JACKSON_READ_CONFIGURATION_JSON, jsonTree());
      fail("Expected SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException e) {
      // happy path
    }

    SpinJsonNode json = S(EXAMPLE_JACKSON_READ_CONFIGURATION_JSON, dataFormatInstance);
    assertThat(json).isNotNull();

    json = JSON(EXAMPLE_JACKSON_READ_CONFIGURATION_JSON, dataFormatInstance);
    assertThat(json).isNotNull();

    json = JSON(EXAMPLE_JACKSON_READ_CONFIGURATION_JSON, configurationMap, null, null);
    assertThat(json).isNotNull();
  }

  @Test
  public void shouldApplyConfigurationOnCreationFromInputStream() {
    InputStream input = IoUtil.stringAsInputStream(EXAMPLE_JACKSON_READ_CONFIGURATION_JSON);

    try {
      S(input, jsonTree());
      fail("Expected SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException e) {
      // happy path
    }

    input = IoUtil.stringAsInputStream(EXAMPLE_JACKSON_READ_CONFIGURATION_JSON);
    SpinJsonNode json = S(input, dataFormatInstance);
    assertThat(json).isNotNull();

    input = IoUtil.stringAsInputStream(EXAMPLE_JACKSON_READ_CONFIGURATION_JSON);
    json = JSON(input, dataFormatInstance);
    assertThat(json).isNotNull();

    input = IoUtil.stringAsInputStream(EXAMPLE_JACKSON_READ_CONFIGURATION_JSON);
    json = JSON(input, configurationMap, null, null);
    assertThat(json).isNotNull();
  }

  @Test
  public void shouldPassConfigurationToNewInstance() {
    JsonJacksonTreeDataFormat jsonDataFormat = new JsonJacksonTreeDataFormat();
    jsonDataFormat.reader().config("aKey", "aValue");

    JsonJacksonTreeDataFormat jsonDataFormatInstance =
        jsonDataFormat.newInstance().reader().config("anotherKey", "anotherValue").done();

    assertThat(jsonDataFormat.reader().getValue("aKey")).isEqualTo("aValue");
    assertThat(jsonDataFormat.reader().getValue("anotherKey")).isNull();

    assertThat(jsonDataFormatInstance.reader().getValue("aKey")).isEqualTo("aValue");
    assertThat(jsonDataFormatInstance.reader().getValue("anotherKey")).isEqualTo("anotherValue");

    JsonJacksonTreeDataFormat nextReturnedDataFormatInstance =
        jsonDataFormatInstance.reader().config("aThirdKey", "aThirdValue").done();
    assertThat(nextReturnedDataFormatInstance).isSameAs(jsonDataFormatInstance);
    assertThat(jsonDataFormatInstance.reader().getValue("aThirdKey")).isEqualTo("aThirdValue");
  }

  @Test
  public void shouldCacheObjectMapper() {
    // object mapper should be cached when configuration does not change
    ObjectMapper objectMapper1 = dataFormatInstance.getConfiguredObjectMapper();
    ObjectMapper objectMapper2 = dataFormatInstance.getConfiguredObjectMapper();

    assertThat(objectMapper1).isSameAs(objectMapper2);

    // changing the configuration should create a new object mapper
    dataFormatInstance.reader().allowBackslashEscapingAnyCharacter(Boolean.FALSE);

    ObjectMapper objectMapper3 = dataFormatInstance.getConfiguredObjectMapper();

    assertThat(objectMapper3).isNotSameAs(objectMapper2);

    // a new format should use the same mapper as long as it is not configured
    JsonJacksonTreeDataFormat newFormat = dataFormatInstance.newInstance();

    ObjectMapper objectMapper4 = newFormat.getConfiguredObjectMapper();

    assertThat(objectMapper4).isSameAs(objectMapper3);
  }

  @Test
  public void shouldReadWithNullConfig() {
    SpinJsonNode json = JSON(EXAMPLE_JSON, null, null, null);
    assertThat(json).isNotNull();
  }
}
