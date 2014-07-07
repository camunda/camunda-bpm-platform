package org.camunda.spin.json.tree;

import java.util.HashMap;
import java.util.Map;

import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.spi.DataFormatInstance;
import org.camunda.spin.spi.DataFormatReader;

public class JsonTreeDataFormatInstance implements DataFormatInstance<SpinJsonNode>, JsonTreeConfigurable<JsonTreeDataFormatInstance> {

  protected JsonTreeConfiguration configuration;
  protected JsonTreeDataFormat dataFormat;
  
  public JsonTreeDataFormatInstance(Map<String, Object> configuration, JsonTreeDataFormat format) {
    this.configuration = new JsonTreeConfiguration(new HashMap<String, Object>(configuration));
    this.dataFormat = format;
  }
  
  public JsonTreeDataFormatInstance config(String key, Object value) {
    configuration.config(key, value);
    return this;
  }
  
  public JsonTreeDataFormatInstance config(Map<String, Object> config) {
    configuration.config(config);
    return this;
  }

  public Object getValue(String key) {
    configuration.getValue(key);
    return this;
  }

  public Object getValue(String key, Object defaultValue) {
    configuration.getValue(key, defaultValue);
    return this;
  }

  public Boolean allowsNumericLeadingZeros() {
    return configuration.allowsNumericLeadingZeros();
  }

  public JsonTreeDataFormatInstance allowNumericLeadingZeros(Boolean value) {
    configuration.allowNumericLeadingZeros(value);
    return this;
  }

  public JsonTreeDataFormat getDataFormat() {
    return dataFormat;
  }

  public Map<String, Object> getConfiguration() {
    return configuration.getConfiguration();
  }
  
  public DataFormatReader getReader() {
    return new JsonTreeDataFormatReader(this);
  }

}
