package org.camunda.bpm.engine.rest.mapper;

import java.text.SimpleDateFormat;

import javax.ws.rs.ext.ContextResolver;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

public class JacksonConfigurator implements ContextResolver<ObjectMapper> {

  ObjectMapper mapper;
  
  public JacksonConfigurator() {
    mapper = new ObjectMapper();
    mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
    mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
  }
  
  @Override
  public ObjectMapper getContext(Class<?> clazz) {
    return mapper;
  }

}
