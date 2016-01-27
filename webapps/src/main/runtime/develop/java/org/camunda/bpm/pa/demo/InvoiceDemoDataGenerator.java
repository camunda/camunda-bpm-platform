package org.camunda.bpm.pa.demo;

import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Permissions.ACCESS;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Resources.APPLICATION;
import static org.camunda.bpm.engine.authorization.Resources.USER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.FilterService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.task.TaskQuery;

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

      Map<String, Object> filterProperties = new HashMap<String, Object>();
      filterProperties.put("description", "Unfiltered Tasks");
      filterProperties.put("priority", 1);
      Filter filter = filterService.newTaskFilter().setName("All Tasks").setProperties(filterProperties);
      filterService.saveFilter(filter);

      filterProperties.clear();
      filterProperties.put("description", "Tasks assigned to me");
      filterProperties.put("priority", -10);
      List<FilterVariableDefinition> variableDefinitions = new ArrayList<FilterVariableDefinition>();
      variableDefinitions.add(new FilterVariableDefinition("Bytes", "aByteVar"));
      variableDefinitions.add(new FilterVariableDefinition("String", "stringVar"));
      variableDefinitions.add(new FilterVariableDefinition("File", "bytesVar"));
      variableDefinitions.add(new FilterVariableDefinition("Yep", "trueBooleanVar"));
      variableDefinitions.add(new FilterVariableDefinition("Nothing", "nullVar"));
      variableDefinitions.add(new FilterVariableDefinition("Random", "random"));
      variableDefinitions.add(new FilterVariableDefinition("Simple date", "dateVar"));
      variableDefinitions.add(new FilterVariableDefinition("Not short", "longVar"));
      variableDefinitions.add(new FilterVariableDefinition("Double", "doubleVar"));
      variableDefinitions.add(new FilterVariableDefinition("Life jacket", "floatVar"));
      variableDefinitions.add(new FilterVariableDefinition("Pete Cook", "cockpitVar"));
      variableDefinitions.add(new FilterVariableDefinition("Integer", "integerVar"));
      variableDefinitions.add(new FilterVariableDefinition("Serialized variable", "serializableVar"));
      variableDefinitions.add(new FilterVariableDefinition("Serialized collection", "serializableCollection"));
      variableDefinitions.add(new FilterVariableDefinition("Hash", "mapVar"));
      variableDefinitions.add(new FilterVariableDefinition("Broken", "failingVar"));
      variableDefinitions.add(new FilterVariableDefinition("Value 1", "value1"));
      filterProperties.put("variables", variableDefinitions);
      TaskService taskService = engine.getTaskService();
      TaskQuery query = taskService.createTaskQuery().taskAssigneeExpression("${currentUser()}");
      filter = filterService.newTaskFilter().setName("My Tasks").setProperties(filterProperties).setQuery(query);
      filterService.saveFilter(filter);

      filterProperties.clear();
      filterProperties.put("description", "Tasks candidate to my groups");
      filterProperties.put("priority", -5);
      query = taskService.createTaskQuery().taskCandidateGroupInExpression("${currentUserGroups()}");
      filter = filterService.newTaskFilter().setName("My Group Tasks").setProperties(filterProperties).setQuery(query);
      filterService.saveFilter(filter);

      filterProperties.clear();
      filterProperties.put("description", "Tasks assigned to group accounting");
      filterProperties.put("priority", 5);
      filterProperties.put("color", "#9fb4de");
      query = taskService.createTaskQuery().taskCandidateGroup("accounting");
      filter = filterService.newTaskFilter().setName("Accounting Tasks").setProperties(filterProperties).setQuery(query);
      filterService.saveFilter(filter);

      filterProperties.clear();
      filterProperties.put("description", "Task due in the next three days");
      filterProperties.put("priority", 0);
      query = taskService.createTaskQuery().dueBeforeExpression("${dateTime().plusDays(4).withTimeAtStartOfDay()}");
      filter = filterService.newTaskFilter().setName("Soon due tasks").setProperties(filterProperties).setQuery(query);
      filterService.saveFilter(filter);

    }
}
