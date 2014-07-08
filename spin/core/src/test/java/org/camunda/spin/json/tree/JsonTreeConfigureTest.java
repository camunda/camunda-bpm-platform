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
import org.camunda.spin.impl.json.tree.JsonJacksonTreeDataFormatInstance;
import org.camunda.spin.impl.util.IoUtil;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.spi.SpinJsonDataFormatException;
import org.junit.Before;
import org.junit.Test;

public class JsonTreeConfigureTest {

  private JsonJacksonTreeDataFormatInstance dataFormatInstance;
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
    jsonDataFormat.getConfiguration().config("aKey", "aValue");
    
    JsonJacksonTreeDataFormatInstance jsonDataFormatInstance = 
        jsonDataFormat.newInstance().config("anotherKey", "anotherValue");
    
    assertThat(jsonDataFormat.getConfiguration().getValue("aKey")).isEqualTo("aValue");
    assertThat(jsonDataFormat.getConfiguration().getValue("anotherKey")).isNull();
    
    assertThat(jsonDataFormatInstance.getValue("aKey").equals("aValue"));
    assertThat(jsonDataFormatInstance.getValue("anotherKey").equals("anotherValue"));
    
    JsonJacksonTreeDataFormatInstance nextReturnedDataFormatInstance = 
        jsonDataFormatInstance.config("aThirdKey", "aThirdValue");
    assertThat(nextReturnedDataFormatInstance).isSameAs(jsonDataFormatInstance);
    assertThat(jsonDataFormatInstance.getValue("aThirdKey").equals("aThirdValue"));
  }
}
