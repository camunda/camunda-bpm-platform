package org.camunda.spin.impl.json.tree;

import java.util.HashMap;
import java.util.Map;

import org.camunda.spin.json.SpinJsonNode;
import org.camunda.spin.spi.DataFormatInstance;
import org.camunda.spin.spi.DataFormatReader;

public class JsonJacksonTreeDataFormatInstance implements DataFormatInstance<SpinJsonNode>, 
  JsonJacksonTreeConfigurable<JsonJacksonTreeDataFormatInstance> {

  protected JsonJacksonTreeConfiguration configuration;
  protected JsonJacksonTreeDataFormat dataFormat;
  
  public JsonJacksonTreeDataFormatInstance(Map<String, Object> configuration, JsonJacksonTreeDataFormat format) {
    this.configuration = new JsonJacksonTreeConfiguration(new HashMap<String, Object>(configuration));
    this.dataFormat = format;
  }
  
  public JsonJacksonTreeDataFormatInstance config(String key, Object value) {
    configuration.config(key, value);
    return this;
  }
  
  public JsonJacksonTreeDataFormatInstance config(Map<String, Object> config) {
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

  public JsonJacksonTreeDataFormatInstance allowNumericLeadingZeros(Boolean value) {
    configuration.allowNumericLeadingZeros(value);
    return this;
  }

  public JsonJacksonTreeDataFormat getDataFormat() {
    return dataFormat;
  }

  public Map<String, Object> getConfiguration() {
    return configuration.getConfiguration();
  }
  
  public DataFormatReader getReader() {
    return new JsonJacksonTreeDataFormatReader(this);
  }

}
