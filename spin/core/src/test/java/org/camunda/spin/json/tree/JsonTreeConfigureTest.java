package org.camunda.spin.json.tree;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.spin.DataFormats.jsonTree;
import static org.camunda.spin.Spin.JSON;
import static org.camunda.spin.Spin.S;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.camunda.spin.impl.json.tree.JsonJacksonTreeConfiguration;
import org.camunda.spin.impl.json.tree.JsonJacksonTreeDataFormat;
import org.camunda.spin.impl.json.tree.JsonJacksonTreeDataFormatInstance;
import org.camunda.spin.impl.util.IoUtil;
import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.spi.SpinJsonDataFormatException;
import org.junit.Test;

public class JsonTreeConfigureTest {

  protected final static String EXAMPLE_JSON = "{\"number\": 001}";
  
  @Test
  public void shouldApplyConfigurationOnCreation() {
    try {
      S(EXAMPLE_JSON, jsonTree());
      fail("Expected SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException e) {
      // happy path
    }
    
    SpinJsonNode json = S(EXAMPLE_JSON, jsonTree().allowNumericLeadingZeros(Boolean.TRUE));
    assertThat(json).isNotNull();
    
    json = JSON(EXAMPLE_JSON, jsonTree().allowNumericLeadingZeros(Boolean.TRUE));
    assertThat(json).isNotNull();
    
    Map<String, Object> config = new HashMap<String, Object>();
    config.put(JsonJacksonTreeConfiguration.ALLOW_NUMERIC_LEADING_ZEROS, Boolean.TRUE);
    json = JSON(EXAMPLE_JSON, config);
    assertThat(json).isNotNull();
  }
  
  @Test
  public void shouldApplyConfigurationOnCreationFromInputStream() {
    InputStream input = IoUtil.stringAsInputStream(EXAMPLE_JSON);
    
    try {
      S(input, jsonTree().allowNumericLeadingZeros(Boolean.FALSE));
      fail("Expected SpinJsonDataFormatException");
    } catch (SpinJsonDataFormatException e) {
      // happy path
    }
    
    input = IoUtil.stringAsInputStream(EXAMPLE_JSON);
    SpinJsonNode json = S(input, jsonTree().allowNumericLeadingZeros(Boolean.TRUE));
    assertThat(json).isNotNull();
    
    input = IoUtil.stringAsInputStream(EXAMPLE_JSON);
    json = JSON(input, jsonTree().allowNumericLeadingZeros(Boolean.TRUE));
    assertThat(json).isNotNull();
    
    input = IoUtil.stringAsInputStream(EXAMPLE_JSON);
    Map<String, Object> config = new HashMap<String, Object>();
    config.put(JsonJacksonTreeConfiguration.ALLOW_NUMERIC_LEADING_ZEROS, Boolean.TRUE);
    json = JSON(input, config);
    assertThat(json).isNotNull();
  }
  
  @Test
  public void shouldCreateNewInstanceOnConfiguration() {
    JsonJacksonTreeDataFormat jsonDataFormat = new JsonJacksonTreeDataFormat();
    JsonJacksonTreeDataFormatInstance jsonDataFormatInstance = jsonDataFormat.newInstance().config("aKey", "aValue");
    
    assertThat(jsonDataFormat.getConfiguration().getValue("aKey")).isNull();
    assertThat(jsonDataFormatInstance.getValue("aKey").equals("aValue"));
    
    JsonJacksonTreeDataFormatInstance nextReturnedDataFormatInstance = jsonDataFormatInstance.config("anotherKey", "anotherValue");
    assertThat(nextReturnedDataFormatInstance).isSameAs(jsonDataFormatInstance);
    assertThat(jsonDataFormatInstance.getValue("aKey").equals("aValue"));
    assertThat(jsonDataFormatInstance.getValue("anotherKey").equals("anotherValue"));
  }
}
