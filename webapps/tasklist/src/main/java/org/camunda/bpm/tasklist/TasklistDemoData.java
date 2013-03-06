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
 * @author: drobisch
 */
public class TasklistDemoData {

    private static final int numOfProcessesPerDefinition = 10;

    public static void createDemoData() {
      ProcessEngine processEngine = BpmPlatform.getDefaultProcessEngine();

      RepositoryService repositoryService = processEngine.getRepositoryService();

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

      Group salesGroup = processEngine.getIdentityService().newGroup("sales");
      salesGroup.setName("Sales");
      processEngine.getIdentityService().saveGroup(salesGroup);

      Group accountingGroup = processEngine.getIdentityService().newGroup("accounting");;
      accountingGroup.setName("Accounting");
      processEngine.getIdentityService().saveGroup(accountingGroup);

      Group managementGroup = processEngine.getIdentityService().newGroup("management");;
      managementGroup.setName("Management");
      processEngine.getIdentityService().saveGroup(managementGroup);

      processEngine.getIdentityService().createMembership("demo", "sales");
      processEngine.getIdentityService().createMembership("demo", "accounting");
      processEngine.getIdentityService().createMembership("demo", "management");

      processEngine.getIdentityService().createMembership("sales", "sales");
      processEngine.getIdentityService().createMembership("accounting", "accounting");
      processEngine.getIdentityService().createMembership("management", "management");
    }
}
