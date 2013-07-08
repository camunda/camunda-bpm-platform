package org.camunda.bpm.tasklist;

import java.util.logging.Logger;

import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.rest.spi.impl.AbstractProcessEngineAware;

/**
 * @author: drobisch
 */
public class TasklistDemoData extends AbstractProcessEngineAware {

    private final static Logger LOGGER = Logger.getLogger(TasklistDemoData.class.getName());

    public void createDemoData() {

      User singleResult = processEngine.getIdentityService().createUserQuery().userId("demo").singleResult();
      if(singleResult != null) {
        return;
      }

      LOGGER.info("Generating demo data for tasklist");

      User user = processEngine.getIdentityService().newUser("demo");
      user.setPassword("demo");
      user.setEmail("demo@camunda.org");
      processEngine.getIdentityService().saveUser(user);

      User user2 = processEngine.getIdentityService().newUser("John");
      user2.setPassword("John");
      user2.setEmail("john@camunda.org");

      processEngine.getIdentityService().saveUser(user2);

      User user3 = processEngine.getIdentityService().newUser("Mary");
      user3.setPassword("Mary");
      user3.setEmail("mary@camunda.org");

      processEngine.getIdentityService().saveUser(user3);

      User user4 = processEngine.getIdentityService().newUser("Peter");
      user4.setPassword("Peter");
      user4.setEmail("peter@camunda.org");

      processEngine.getIdentityService().saveUser(user4);

      Group salesGroup = processEngine.getIdentityService().newGroup("sales");
      salesGroup.setName("Sales");
      processEngine.getIdentityService().saveGroup(salesGroup);

      Group accountingGroup = processEngine.getIdentityService().newGroup("accounting");
      accountingGroup.setName("Accounting");
      processEngine.getIdentityService().saveGroup(accountingGroup);

      Group managementGroup = processEngine.getIdentityService().newGroup("management");
      managementGroup.setName("Management");
      processEngine.getIdentityService().saveGroup(managementGroup);

      processEngine.getIdentityService().createMembership("demo", "sales");
      processEngine.getIdentityService().createMembership("demo", "accounting");
      processEngine.getIdentityService().createMembership("demo", "management");

      processEngine.getIdentityService().createMembership("John", "sales");
      processEngine.getIdentityService().createMembership("Mary", "accounting");
      processEngine.getIdentityService().createMembership("Peter", "management");
    }

}
