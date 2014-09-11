package org.camunda.bpm.pa.demo;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.ProcessEngine;
import static org.camunda.bpm.engine.authorization.Authorization.*;
import static org.camunda.bpm.engine.authorization.Permissions.*;
import static org.camunda.bpm.engine.authorization.Resources.*;

import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.task.TaskQuery;

import org.camunda.bpm.engine.filter.Filter;

/**
 * @author drobisch
 */
public class InvoiceDemoDataGenerator {

    private final static Logger LOGGER = Logger.getLogger(InvoiceDemoDataGenerator.class.getName());

    public void createDemoData(ProcessEngine engine) {

      User singleResult = engine.getIdentityService().createUserQuery().userId("demo").singleResult();
      if(singleResult != null) {
        return;
      }

      LOGGER.info("Generating demo data for tasklist");

      User user = engine.getIdentityService().newUser("demo");
      user.setFirstName("Demo");
      user.setLastName("Demo");
      user.setPassword("demo");
      user.setEmail("demo@camunda.org");
      engine.getIdentityService().saveUser(user);

      User user2 = engine.getIdentityService().newUser("john");
      user2.setFirstName("John");
      user2.setLastName("Doe");
      user2.setPassword("john");
      user2.setEmail("john@camunda.org");

      engine.getIdentityService().saveUser(user2);

      User user3 = engine.getIdentityService().newUser("mary");
      user3.setFirstName("Mary");
      user3.setLastName("Anne");
      user3.setPassword("mary");
      user3.setEmail("mary@camunda.org");

      engine.getIdentityService().saveUser(user3);

      User user4 = engine.getIdentityService().newUser("peter");
      user4.setFirstName("Peter");
      user4.setLastName("Meter");
      user4.setPassword("peter");
      user4.setEmail("peter@camunda.org");

      engine.getIdentityService().saveUser(user4);

      Group salesGroup = engine.getIdentityService().newGroup("sales");
      salesGroup.setName("Sales");
      salesGroup.setType("WORKFLOW");
      engine.getIdentityService().saveGroup(salesGroup);

      Group accountingGroup = engine.getIdentityService().newGroup("accounting");
      accountingGroup.setName("Accounting");
      accountingGroup.setType("WORKFLOW");
      engine.getIdentityService().saveGroup(accountingGroup);

      Group managementGroup = engine.getIdentityService().newGroup("management");
      managementGroup.setName("Management");
      managementGroup.setType("WORKFLOW");
      engine.getIdentityService().saveGroup(managementGroup);

      engine.getIdentityService().createMembership("demo", "sales");
      engine.getIdentityService().createMembership("demo", "accounting");
      engine.getIdentityService().createMembership("demo", "management");

      engine.getIdentityService().createMembership("john", "sales");
      engine.getIdentityService().createMembership("mary", "accounting");
      engine.getIdentityService().createMembership("peter", "management");

      // authorize groups for tasklist:

      AuthorizationService authorizationService = engine.getAuthorizationService();

      Authorization salesTasklistAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
      salesTasklistAuth.setGroupId("sales");
      salesTasklistAuth.addPermission(ACCESS);
      salesTasklistAuth.setResourceId("tasklist");
      salesTasklistAuth.setResource(APPLICATION);
      authorizationService.saveAuthorization(salesTasklistAuth);

      Authorization accountingTasklistAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
      accountingTasklistAuth.setGroupId("accounting");
      accountingTasklistAuth.addPermission(ACCESS);
      accountingTasklistAuth.setResourceId("tasklist");
      accountingTasklistAuth.setResource(APPLICATION);
      authorizationService.saveAuthorization(accountingTasklistAuth);

      Authorization managementTasklistAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
      managementTasklistAuth.setGroupId("management");
      managementTasklistAuth.addPermission(ACCESS);
      managementTasklistAuth.setResourceId("tasklist");
      managementTasklistAuth.setResource(APPLICATION);
      authorizationService.saveAuthorization(managementTasklistAuth);

      Authorization salesDemoAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
      salesDemoAuth.setGroupId("sales");
      salesDemoAuth.setResource(USER);
      salesDemoAuth.setResourceId("demo");
      salesDemoAuth.addPermission(READ);
      authorizationService.saveAuthorization(salesDemoAuth);

      Authorization salesJohnAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
      salesJohnAuth.setGroupId("sales");
      salesJohnAuth.setResource(USER);
      salesJohnAuth.setResourceId("john");
      salesJohnAuth.addPermission(READ);
      authorizationService.saveAuthorization(salesJohnAuth);

      Authorization manDemoAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
      manDemoAuth.setGroupId("management");
      manDemoAuth.setResource(USER);
      manDemoAuth.setResourceId("demo");
      manDemoAuth.addPermission(READ);
      authorizationService.saveAuthorization(manDemoAuth);

      Authorization manPeterAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
      manPeterAuth.setGroupId("management");
      manPeterAuth.setResource(USER);
      manPeterAuth.setResourceId("peter");
      manPeterAuth.addPermission(READ);
      authorizationService.saveAuthorization(manPeterAuth);

      Authorization accDemoAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
      accDemoAuth.setGroupId("accounting");
      accDemoAuth.setResource(USER);
      accDemoAuth.setResourceId("demo");
      accDemoAuth.addPermission(READ);
      authorizationService.saveAuthorization(accDemoAuth);

      Authorization accMaryAuth = authorizationService.createNewAuthorization(AUTH_TYPE_GRANT);
      accMaryAuth.setGroupId("accounting");
      accMaryAuth.setResource(USER);
      accMaryAuth.setResourceId("mary");
      accMaryAuth.addPermission(READ);
      authorizationService.saveAuthorization(accMaryAuth);

      // Filter
      FilterService filterService = engine.getFilterService();
      Map<String, Object> properties = new HashMap<String, Object>();
      properties.put("description", "Unfiltered Tasks");
      properties.put("priority", 1);
      Filter filter = filterService.newTaskFilter().setName("All Tasks").setProperties(properties);
      filterService.saveFilter(filter);

      TaskService taskService = engine.getTaskService();
      TaskQuery query = taskService.createTaskQuery().taskAssignee("jonny1");
      properties.clear();
      properties.put("description", "Tasks assigned to me");
      properties.put("priority", -10);
      filter = filterService.newTaskFilter().setName("My Tasks").setProperties(properties).setQuery(query);
      filterService.saveFilter(filter);

      query = taskService.createTaskQuery().taskCandidateGroup("accounting");
      properties.clear();
      properties.put("description", "Tasks assigned to group accounting");
      properties.put("priority", 5);
      properties.put("color", "#3e4d2f");
      filter = filterService.newTaskFilter().setName("Accounting Tasks").setProperties(properties).setQuery(query);
      filterService.saveFilter(filter);

    }
}
