/**
 * Provides a fluent API to configure Jackson's json writing options implementing the options of
 * {@link JsonGenerator.Feature}.
 * 
 * @author Thorben Lindhauer
 */
package org.camunda.spin.impl.json.tree;

import java.util.Map;

import org.camunda.spin.spi.AbstractConfiguration;

public abstract class AbstractJsonJacksonDataFormatConfiguration<R extends AbstractConfiguration<R>> 
  extends AbstractConfiguration<R> implements JsonJacksonTreeConfigurable {

  protected JsonJacksonTreeDataFormat dataFormat;
  
  public AbstractJsonJacksonDataFormatConfiguration(JsonJacksonTreeDataFormat dataFormat) {
    super();
    this.dataFormat = dataFormat;
  }
  
  public AbstractJsonJacksonDataFormatConfiguration(JsonJacksonTreeDataFormat dataFormat, 
      R configuration) {
    super(configuration);
    this.dataFormat = dataFormat;
  }
  
  public JsonJacksonParserConfiguration reader() {
    return dataFormat.reader();
  }

  public JsonJacksonGeneratorConfiguration writer() {
    return dataFormat.writer();
  }

  public JsonJacksonMapperConfiguration mapper() {
    return dataFormat.mapper();
  }

  public JsonJacksonTreeDataFormat done() {
    return dataFormat;
  }
  
  public R config(Map<String, Object> config) {
    dataFormat.invalidateCachedObjectMapper();
    return super.config(config);
  }
  
  public R config(String key, Object value) {
    dataFormat.invalidateCachedObjectMapper();
    return super.config(key, value);
  }

}
