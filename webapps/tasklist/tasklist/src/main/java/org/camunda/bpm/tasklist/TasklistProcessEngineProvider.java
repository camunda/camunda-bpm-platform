package org.camunda.bpm.tasklist;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.persistence.StrongUuidGenerator;
import org.activiti.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;

import javax.swing.*;
import java.util.List;

/**
 * @author: drobisch
 */
public class TasklistProcessEngineProvider implements ProcessEngineProvider {

    private static final int numOfProcessesPerDefinition = 10;
    private static ProcessEngine processEngine;

    public static void createProcessEngine() {
      ProcessEngineConfiguration processEngineConfiguration =
          ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration();
      // use UUIDs
      ((ProcessEngineConfigurationImpl)processEngineConfiguration).setIdGenerator(new StrongUuidGenerator());
      processEngine = processEngineConfiguration.buildProcessEngine();
    }


    public static void createDemoData() {
      RepositoryService repositoryService = processEngine.getRepositoryService();

      repositoryService
          .createDeployment()
          .addClasspathResource("processes/fox-invoice_en.bpmn")
          .deploy();

      RuntimeService runtimeService = processEngine.getRuntimeService();
      List<ProcessDefinition> pds = repositoryService.createProcessDefinitionQuery().list();
      for (ProcessDefinition pd : pds) {
        for (int i = 0; i < numOfProcessesPerDefinition; i++) {
          runtimeService.startProcessInstanceById(pd.getId());
        }
      }

      User user = processEngine.getIdentityService().newUser("demo");
      user.setPassword("demo");
      user.setEmail("demo@camunda.org");
      processEngine.getIdentityService().saveUser(user);
    }

    public ProcessEngine getProcessEngine() {
      return processEngine;
    }

    public static ProcessEngine getStaticEngine() {
      return processEngine;
    }
}
