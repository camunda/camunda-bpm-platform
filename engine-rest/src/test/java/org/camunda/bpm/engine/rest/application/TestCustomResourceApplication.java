package org.camunda.bpm.engine.rest.application;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.fasterxml.jackson.jaxrs.base.JsonMappingExceptionMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.jaxrs.base.JsonParseExceptionMapper;
import org.camunda.bpm.engine.rest.exception.ExceptionHandler;
import org.camunda.bpm.engine.rest.exception.ProcessEngineExceptionHandler;
import org.camunda.bpm.engine.rest.exception.RestExceptionHandler;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;

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
