package com.camunda.fox.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.activiti.engine.ProcessEngine;

import com.camunda.fox.platform.api.ProcessEngineService;

/**
 *
 * @author nico.rehwaldt
 */
@ApplicationScoped
public class TestProcessEngineLookup {

  
  private final static String PROCESS_ENGINE_SERVICE_NAME =
          "java:global/" +
          "camunda-fox-platform/" +
          "process-engine/" +
          "PlatformService!com.camunda.fox.platform.api.ProcessEngineService";
  
  @Produces
  @ApplicationScoped
  public ProcessEngine getProcessEngine() {
    try {
      ProcessEngineService service = InitialContext.doLookup(PROCESS_ENGINE_SERVICE_NAME);
      return service.getDefaultProcessEngine();
    } catch (NamingException e) {
      throw new RuntimeException("Could not look up process engine service", e);
    }
  }
}
