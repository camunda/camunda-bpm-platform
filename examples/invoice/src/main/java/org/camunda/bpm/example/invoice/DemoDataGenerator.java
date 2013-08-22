package org.camunda.bpm.example.invoice;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;

import java.util.logging.Logger;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Permissions.ACCESS;
import static org.camunda.bpm.engine.authorization.Permissions.ALL;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Resources.APPLICATION;
import static org.camunda.bpm.engine.authorization.Resources.USER;

/**
 * Creates demo credentials to be used in the invoice showcase.
 *
 * @author drobisch
 */
public class DemoDataGenerator {

    private final static Logger LOGGER = Logger.getLogger(DemoDataGenerator.class.getName());

    public void createUsers(ProcessEngine engine) {

      final IdentityService identityService = engine.getIdentityService();

      if(identityService.isReadOnly()) {
        LOGGER.info("Identity service provider is Read Only, not creating any demo users.");
        return;
      }

      User singleResult = identityService.createUserQuery().userId("demo").singleResult();
      if (singleResult != null) {
        return;
      }

      LOGGER.info("Generating demo data for invoice showcase");

      User user = identityService.newUser("demo");
      user.setFirstName("Demo");
      user.setLastName("Demo");
      user.setPassword("demo");
      user.setEmail("demo@camunda.org");
      identityService.saveUser(user);

      User user2 = identityService.newUser("john");
      user2.setFirstName("John");
      user2.setLastName("Doe");
      user2.setPassword("john");
      user2.setEmail("john@camunda.org");

      identityService.saveUser(user2);

      User user3 = identityService.newUser("mary");
      user3.setFirstName("Mary");
      user3.setLastName("Anne");
      user3.setPassword("mary");
      user3.setEmail("mary@camunda.org");

      identityService.saveUser(user3);

      User user4 = identityService.newUser("peter");
      user4.setFirstName("Peter");
      user4.setLastName("Meter");
      user4.setPassword("peter");
      user4.setEmail("peter@camunda.org");

      identityService.saveUser(user4);

      Group salesGroup = identityService.newGroup("sales");
      salesGroup.setName("Sales");
      salesGroup.setType("WORKFLOW");
      identityService.saveGroup(salesGroup);

      Group accountingGroup = identityService.newGroup("accounting");
      accountingGroup.setName("Accounting");
      accountingGroup.setType("WORKFLOW");
      identityService.saveGroup(accountingGroup);

      Group managementGroup = identityService.newGroup("management");
      managementGroup.setName("Management");
      managementGroup.setType("WORKFLOW");
      identityService.saveGroup(managementGroup);

      final AuthorizationService authorizationService = engine.getAuthorizationService();

      // create group
      if(identityService.createGroupQuery().groupId(Groups.CAMUNDA_ADMIN).count() == 0) {
        Group camundaAdminGroup = identityService.newGroup(Groups.CAMUNDA_ADMIN);
        camundaAdminGroup.setName("camunda BPM Administrators");
        camundaAdminGroup.setType(Groups.GROUP_TYPE_SYSTEM);
        identityService.saveGroup(camundaAdminGroup);
      }

      // create ADMIN authorizations on all built-in resources
      for (Resource resource : Resources.values()) {
        if(authorizationService.createAuthorizationQuery().groupIdIn(Groups.CAMUNDA_ADMIN).resourceType(resource).resourceId(ANY).count() == 0) {
          AuthorizationEntity userAdminAuth = new AuthorizationEntity(AUTH_TYPE_GRANT);
          userAdminAuth.setGroupId(Groups.CAMUNDA_ADMIN);
          userAdminAuth.setResource(resource);
          userAdminAuth.setResourceId(ANY);
          userAdminAuth.addPermission(ALL);
          authorizationService.saveAuthorization(userAdminAuth);
        }
      }

      identityService.createMembership("demo", "sales");
      identityService.createMembership("demo", "accounting");
      identityService.createMembership("demo", "management");
      identityService.createMembership("demo", "camunda-admin");

      identityService.createMembership("john", "sales");
      identityService.createMembership("mary", "accounting");
      identityService.createMembership("peter", "management");


      // authorize groups for tasklist only:

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

    }
}
