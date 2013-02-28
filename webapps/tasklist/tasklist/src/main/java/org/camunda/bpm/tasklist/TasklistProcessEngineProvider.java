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

      User user2 = processEngine.getIdentityService().newUser("sales");
      user2.setPassword("sales");
      user2.setEmail("sales@camunda.org");

      processEngine.getIdentityService().saveUser(user2);

      User user3 = processEngine.getIdentityService().newUser("accounting");
      user3.setPassword("accounting");
      user3.setEmail("accounting@camunda.org");

      processEngine.getIdentityService().saveUser(user3);

      User user4 = processEngine.getIdentityService().newUser("management");
      user4.setPassword("management");
      user4.setEmail("management@camunda.org");

      processEngine.getIdentityService().saveUser(user4);

      Group salesGroup = processEngine.getIdentityService().newGroup("sales");;
      processEngine.getIdentityService().saveGroup(salesGroup);

      Group accountingGroup = processEngine.getIdentityService().newGroup("accounting");;
      processEngine.getIdentityService().saveGroup(accountingGroup);

      Group managementGroup = processEngine.getIdentityService().newGroup("management");;
      processEngine.getIdentityService().saveGroup(managementGroup);

      processEngine.getIdentityService().createMembership("demo", "sales");
      processEngine.getIdentityService().createMembership("demo", "accounting");
      processEngine.getIdentityService().createMembership("demo", "management");

      processEngine.getIdentityService().createMembership("sales", "sales");
      processEngine.getIdentityService().createMembership("accounting", "accounting");
      processEngine.getIdentityService().createMembership("management", "management");
    }

    public ProcessEngine getProcessEngine() {
      return processEngine;
    }

    public static ProcessEngine getStaticEngine() {
      return processEngine;
    }
}
