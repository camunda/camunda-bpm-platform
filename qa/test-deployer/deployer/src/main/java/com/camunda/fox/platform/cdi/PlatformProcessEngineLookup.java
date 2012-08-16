package com.camunda.fox.platform.cdi;

import com.camunda.fox.platform.api.ProcessEngineService;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.activiti.cdi.spi.ProcessEngineLookup;
import org.activiti.engine.ProcessEngine;

/**
 *
 * @author nico.rehwaldt@camunda.com
 */
public class PlatformProcessEngineLookup implements ProcessEngineLookup {

  private final static String PROCESS_ENGINE_SERVICE_NAME =
          "java:global/" +
          "camunda-fox-platform/" +
          "process-engine/" +
          "PlatformService!com.camunda.fox.platform.api.ProcessEngineService";
  
  public int getPrecedence() {
    return 100;
  }
  
  public ProcessEngine getProcessEngine() {
    try {
      ProcessEngineService service = InitialContext.doLookup(PROCESS_ENGINE_SERVICE_NAME);
      return service.getDefaultProcessEngine();
    } catch (NamingException e) {
      throw new RuntimeException("Could not look up process engine service", e);
    }
  }

  public void ungetProcessEngine() {
    // Container managed
  }
}
