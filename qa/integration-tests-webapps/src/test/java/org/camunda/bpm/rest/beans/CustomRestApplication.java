package org.camunda.bpm.rest.beans;

import org.camunda.bpm.engine.rest.impl.CamundaRestResources;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class CustomRestApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<Class<?>>();

    // add all camunda engine rest resources (or just add those that you actually need).
    classes.addAll(CamundaRestResources.getResourceClasses());

    // mandatory
    classes.addAll(CamundaRestResources.getConfigurationClasses());

    return classes;
  }

}
