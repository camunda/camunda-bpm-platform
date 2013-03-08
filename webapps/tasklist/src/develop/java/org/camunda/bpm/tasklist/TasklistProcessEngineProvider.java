package org.camunda.bpm.tasklist;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;

/**
 * @author drobisch
 * @author Thorben Lindhauer
 */
public class TasklistProcessEngineProvider implements ProcessEngineProvider {

    public static ProcessEngine getStaticProcessEngine() {
      return BpmPlatform.getDefaultProcessEngine();
    }

    public ProcessEngine getDefaultProcessEngine() {
      return getStaticProcessEngine();
    }

    public ProcessEngine getProcessEngine(String name) {
      return BpmPlatform.getProcessEngineService().getProcessEngine(name);
    }
}
