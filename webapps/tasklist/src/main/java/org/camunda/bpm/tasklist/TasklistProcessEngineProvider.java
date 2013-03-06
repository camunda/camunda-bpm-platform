package org.camunda.bpm.tasklist;

import org.camunda.bpm.BpmPlatform;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.StrongUuidGenerator;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;

import javax.swing.*;
import java.util.List;

/**
 * @author drobisch
 */
public class TasklistProcessEngineProvider implements ProcessEngineProvider {

    public ProcessEngine getProcessEngine() {
      return getStaticProcessEngine();
    }

    public static ProcessEngine getStaticProcessEngine() {
      return BpmPlatform.getDefaultProcessEngine();
    }
}
