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
      user.setFirstName("Demo");
      user.setLastName("Demo");
      user.setPassword("demo");
      user.setEmail("demo@camunda.org");
      processEngine.getIdentityService().saveUser(user);

      User user2 = processEngine.getIdentityService().newUser("john");
      user2.setFirstName("John");
      user2.setLastName("Doe");
      user2.setPassword("john");
      user2.setEmail("john@camunda.org");

      processEngine.getIdentityService().saveUser(user2);

      User user3 = processEngine.getIdentityService().newUser("mary");
      user3.setFirstName("Mary");
      user3.setLastName("Anne");
      user3.setPassword("mary");
      user3.setEmail("mary@camunda.org");

      processEngine.getIdentityService().saveUser(user3);

      User user4 = processEngine.getIdentityService().newUser("peter");
      user4.setFirstName("Peter");
      user4.setLastName("Meter");
      user4.setPassword("peter");
      user4.setEmail("peter@camunda.org");

      processEngine.getIdentityService().saveUser(user4);

      Group salesGroup = processEngine.getIdentityService().newGroup("sales");
      salesGroup.setName("Sales");
      salesGroup.setType("WORKFLOW");
      processEngine.getIdentityService().saveGroup(salesGroup);

      Group accountingGroup = processEngine.getIdentityService().newGroup("accounting");
      accountingGroup.setName("Accounting");
      accountingGroup.setType("WORKFLOW");
      processEngine.getIdentityService().saveGroup(accountingGroup);

      Group managementGroup = processEngine.getIdentityService().newGroup("management");
      managementGroup.setName("Management");
      managementGroup.setType("WORKFLOW");
      processEngine.getIdentityService().saveGroup(managementGroup);

      processEngine.getIdentityService().createMembership("demo", "sales");
      processEngine.getIdentityService().createMembership("demo", "accounting");
      processEngine.getIdentityService().createMembership("demo", "management");

      processEngine.getIdentityService().createMembership("john", "sales");
      processEngine.getIdentityService().createMembership("mary", "accounting");
      processEngine.getIdentityService().createMembership("peter", "management");
    }

}
