package org.camunda.bpm.engine.test.api.authorization;

import static org.junit.Assert.assertEquals;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class DefaultPermissionForTenantMembers {

  protected static final String TENANT_ONE = "tenant1";
  
  protected static final String TENANT_TWO = "tenant2";
  
  protected ProcessEngineRule engineRule = new ProcessEngineRule(true);

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected AuthorizationService authorizationService;

  protected IdentityService identityService;

  protected Tenant tenantOne;

  protected User user;

  protected Group group;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);
  
  @Before
  public void init() {
    identityService = engineRule.getIdentityService();
    authorizationService = engineRule.getAuthorizationService();
    
    tenantOne = createTenant(TENANT_ONE);
    
    user = identityService.newUser("aUserId");
    identityService.saveUser(user);

    group = identityService.newGroup("aGroupId");
    identityService.saveGroup(group);

    engineRule.getProcessEngineConfiguration().setAuthorizationEnabled(true);
  }

  @After
  public void tearDown() {
    
    identityService.deleteUser(user.getId());
    identityService.deleteGroup(group.getId());
    identityService.deleteTenant(tenantOne.getId());
  }

  @Test
  public void testCreateTenantUserMembership() {

    identityService.createTenantUserMembership(tenantOne.getId(), user.getId());
    
    assertEquals(1, authorizationService.createAuthorizationQuery()
      .userIdIn(user.getId())
      .resourceType(Resources.TENANT)
      .resourceId(tenantOne.getId())
      .hasPermission(Permissions.READ).count());

   assertEquals(TENANT_ONE,identityService.createTenantQuery()
     .userMember(user.getId())
     .singleResult()
     .getId());
    
  }

  @Test
  public void testCreateAndDeleteTenantUserMembership() {

    identityService.createTenantUserMembership(tenantOne.getId(), user.getId());
    
    identityService.deleteTenantUserMembership(tenantOne.getId(), user.getId());

    assertEquals(0, authorizationService.createAuthorizationQuery()
      .userIdIn(user.getId())
      .resourceType(Resources.TENANT)
      .hasPermission(Permissions.READ).count());
    
    assertEquals(0,identityService.createTenantQuery()
     .userMember(user.getId())
     .count());

  }

  @Test
  public void testCreateAndDeleteTenantUserMembershipForMultipleTenants() {

    Tenant tenantTwo = createTenant(TENANT_TWO);

    identityService.createTenantUserMembership(tenantOne.getId(), user.getId());
    identityService.createTenantUserMembership(tenantTwo.getId(), user.getId());

    assertEquals(2, authorizationService.createAuthorizationQuery()
      .userIdIn(user.getId())
      .resourceType(Resources.TENANT)
      .hasPermission(Permissions.READ).count());
    
    identityService.deleteTenantUserMembership(tenantOne.getId(), user.getId());

    assertEquals(TENANT_TWO,identityService.createTenantQuery()
     .userMember(user.getId())
     .singleResult()
     .getId());
    
    assertEquals(1, authorizationService.createAuthorizationQuery()
      .userIdIn(user.getId())
      .resourceType(Resources.TENANT)
      .hasPermission(Permissions.READ).count());

    identityService.deleteTenant(tenantTwo.getId());
  }

  @Test
  public void testCreateTenantGroupMembership() {

    identityService.createTenantGroupMembership(tenantOne.getId(), group.getId());
    
    assertEquals(1, authorizationService.createAuthorizationQuery()
      .groupIdIn(group.getId())
      .resourceType(Resources.TENANT)
      .resourceId(tenantOne.getId())
      .hasPermission(Permissions.READ).count());

    assertEquals(TENANT_ONE,identityService.createTenantQuery()
      .groupMember(group.getId())
      .singleResult()
      .getId());

  }

  @Test
  public void testCreateAndDeleteTenantGroupMembership() {

    identityService.createTenantGroupMembership(tenantOne.getId(), group.getId());
    
    identityService.deleteTenantGroupMembership(tenantOne.getId(), group.getId());

    assertEquals(0, authorizationService.createAuthorizationQuery()
      .groupIdIn(group.getId())
      .resourceType(Resources.TENANT)
      .hasPermission(Permissions.READ).count());
    
    assertEquals(0,identityService.createTenantQuery()
     .groupMember(group.getId())
     .count());

  }

  @Test
  public void testCreateAndDeleteTenantGroupMembershipForMultipleTenants() {

    Tenant tenantTwo = createTenant(TENANT_TWO);

    identityService.createTenantGroupMembership(tenantOne.getId(), group.getId());
    identityService.createTenantGroupMembership(tenantTwo.getId(), group.getId());

    assertEquals(2, authorizationService.createAuthorizationQuery()
      .groupIdIn(group.getId())
      .resourceType(Resources.TENANT)
      .hasPermission(Permissions.READ).count());

    identityService.deleteTenantGroupMembership(tenantOne.getId(), group.getId());

    assertEquals(1, authorizationService.createAuthorizationQuery()
      .groupIdIn(group.getId())
      .resourceType(Resources.TENANT)
      .hasPermission(Permissions.READ).count());
    
    assertEquals(TENANT_TWO,identityService.createTenantQuery()
     .groupMember(group.getId())
     .singleResult()
     .getId());

    identityService.deleteTenant(tenantTwo.getId());

  }

  protected Tenant createTenant(String tenantId) {
    Tenant newTenant = identityService.newTenant(tenantId);
    identityService.saveTenant(newTenant);
    return newTenant;
  }
}
