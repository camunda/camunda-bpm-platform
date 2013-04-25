package org.camunda.bpm.cockpit;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.camunda.bpm.engine.rest.exception.ExceptionHandler;
import org.camunda.bpm.engine.rest.mapper.JacksonConfigurator;
import org.camunda.bpm.cockpit.resources.PluginsResource;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

/**
 * @author: drobisch
 */
public class CockpitApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<Class<?>>();

    classes.add(PluginsResource.class);

    classes.add(JacksonConfigurator.class);
    classes.add(JacksonJsonProvider.class);
    classes.add(ExceptionHandler.class);

    return classes;
  }

}
