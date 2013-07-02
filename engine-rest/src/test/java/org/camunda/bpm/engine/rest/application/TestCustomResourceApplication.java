package org.camunda.bpm.engine.rest.application;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.camunda.bpm.engine.rest.exception.ExceptionHandler;
import org.camunda.bpm.engine.rest.exception.ProcessEngineExceptionHandler;
import org.camunda.bpm.engine.rest.exception.RestExceptionHandler;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.jaxrs.JsonMappingExceptionMapper;
import org.codehaus.jackson.jaxrs.JsonParseExceptionMapper;

@ApplicationPath("/")
public class TestCustomResourceApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<Class<?>>();
    
    classes.add(UnannotatedResource.class);
    classes.add(ConflictingProvider.class);
    
    classes.add(JacksonConfigurator.class);
    
    classes.add(JacksonJsonProvider.class);
    classes.add(JsonMappingExceptionMapper.class);
    classes.add(JsonParseExceptionMapper.class);
    
    classes.add(ProcessEngineExceptionHandler.class);
    classes.add(RestExceptionHandler.class);
    classes.add(ExceptionHandler.class);
    
    return classes;
  }
}
