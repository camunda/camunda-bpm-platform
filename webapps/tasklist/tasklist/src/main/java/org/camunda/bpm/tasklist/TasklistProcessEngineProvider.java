package org.camunda.bpm.tasklist;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.StrongUuidGenerator;
import org.activiti.engine.repository.ProcessDefinition;
import org.camunda.bpm.BpmPlatform;
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
