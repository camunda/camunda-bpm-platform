package org.camunda.bpm.engine.rest.application;

import com.fasterxml.jackson.jaxrs.base.JsonMappingExceptionMapper;
import com.fasterxml.jackson.jaxrs.base.JsonParseExceptionMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.camunda.bpm.engine.rest.exception.ExceptionHandler;
import org.camunda.bpm.engine.rest.exception.ProcessEngineExceptionHandler;
import org.camunda.bpm.engine.rest.exception.RestExceptionHandler;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class TestCustomResourceApplication extends Application {

  private static final Set<Class<?>> RESOURCES = new HashSet<Class<?>>();
  private static final Set<Class<?>> PROVIDERS = new HashSet<Class<?>>();

  static {
    // RESOURCES
    RESOURCES.add(UnannotatedResource.class);

    // PROVIDERS
    PROVIDERS.add(ConflictingProvider.class);

    PROVIDERS.add(JacksonConfigurator.class);

    PROVIDERS.add(JacksonJsonProvider.class);
    PROVIDERS.add(JsonMappingExceptionMapper.class);
    PROVIDERS.add(JsonParseExceptionMapper.class);

    PROVIDERS.add(ProcessEngineExceptionHandler.class);
    PROVIDERS.add(RestExceptionHandler.class);
    PROVIDERS.add(ExceptionHandler.class);
  }

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<Class<?>>();
    classes.addAll(RESOURCES);
    classes.addAll(PROVIDERS);

    return classes;
  }

  public static Set<Class<?>> getResourceClasses() {
    return RESOURCES;
  }

  public static Set<Class<?>> getProviderClasses() {
    return PROVIDERS;
  }
}
