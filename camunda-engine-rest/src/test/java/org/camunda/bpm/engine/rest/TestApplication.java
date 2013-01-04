package org.camunda.bpm.engine.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.camunda.bpm.engine.rest.impl.ProcessDefinitionServiceImpl;

@ApplicationPath("/testapp")
public class TestApplication extends Application {

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> classes = new HashSet<Class<?>>();
    
    classes.add(ProcessDefinitionServiceImpl.class);
    
    return classes;
  }
}
