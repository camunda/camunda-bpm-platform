package org.camunda.bpm.cockpit.impl.web;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.camunda.bpm.engine.rest.exception.ExceptionHandler;
import org.camunda.bpm.engine.rest.exception.RestExceptionHandler;
import org.camunda.bpm.engine.rest.impl.ProcessDefinitionRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.ProcessInstanceRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.TaskRestServiceImpl;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

/**
 * The engine rest api exposed by the application.
 *
 * @author nico.rehwaldt
 */
public class EngineRestApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<Class<?>>();

    classes.add(ProcessDefinitionRestServiceImpl.class);
    classes.add(ProcessInstanceRestServiceImpl.class);
    classes.add(TaskRestServiceImpl.class);

    classes.add(JacksonConfigurator.class);
    classes.add(JacksonJsonProvider.class);
    classes.add(ExceptionHandler.class);
    classes.add(RestExceptionHandler.class);

    return classes;
  }
}
