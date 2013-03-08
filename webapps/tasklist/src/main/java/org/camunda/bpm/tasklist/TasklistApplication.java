package org.camunda.bpm.tasklist;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.camunda.bpm.engine.rest.exception.ExceptionHandler;
import org.camunda.bpm.engine.rest.mapper.EngineQueryDtoGetReader;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.camunda.bpm.tasklist.resources.AuthenticationResource;
import org.camunda.bpm.tasklist.resources.TaskFormResource;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

/**
 * @author: drobisch
 */
public class TasklistApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<Class<?>>();

    classes.add(AuthenticationResource.class);
    classes.add(TaskFormResource.class);

    classes.add(EngineQueryDtoGetReader.class);
    classes.add(JacksonConfigurator.class);
    classes.add(JacksonJsonProvider.class);
    classes.add(ExceptionHandler.class);
    
    return classes;
  }
  
}
