package org.camunda.bpm.qa.upgrade.authorization;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class AuthorizationTest {

  protected AuthorizationService authorizationService;

  protected IdentityService identityService;

  protected ProcessEngineConfiguration processEngineConfiguration;

  protected boolean defaultAuthorizationEnabled;

  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule();

  @Before
  public void init() {
    authorizationService = rule.getAuthorizationService();
    identityService = rule.getIdentityService();
    processEngineConfiguration = rule.getProcessEngineConfiguration();
    defaultAuthorizationEnabled = processEngineConfiguration.isAuthorizationEnabled();
  }

  @After
  public void restoreAuthorization() {
    processEngineConfiguration.setAuthorizationEnabled(defaultAuthorizationEnabled);
  }

  @Test
  public void testDefaultAuthorizationQueryForCamundaAdminOnUpgrade() {

    processEngineConfiguration.setAuthorizationEnabled(true);

    assertEquals(1, authorizationService.createAuthorizationQuery()
      .resourceType(Resources.TENANT)
      .groupIdIn(Groups.CAMUNDA_ADMIN)
      .hasPermission(Permissions.ALL).count());

    assertEquals(1, authorizationService.createAuthorizationQuery()
      .resourceType(Resources.TENANT_MEMBERSHIP)
      .groupIdIn(Groups.CAMUNDA_ADMIN)
      .hasPermission(Permissions.ALL).count());

    assertEquals(1, authorizationService.createAuthorizationQuery()
      .resourceType(Resources.BATCH)
      .groupIdIn(Groups.CAMUNDA_ADMIN)
      .hasPermission(Permissions.ALL).count());

  }

  @Test
  public void testDefaultAuthorizationForCamundaAdminOnUpgrade() {
    processEngineConfiguration.setAuthorizationEnabled(true);
    assertEquals(true,authorizationService.isUserAuthorized(null, Collections.singletonList(Groups.CAMUNDA_ADMIN), Permissions.ALL, Resources.TENANT));
    assertEquals(true,authorizationService.isUserAuthorized(null, Collections.singletonList(Groups.CAMUNDA_ADMIN), Permissions.ALL, Resources.TENANT_MEMBERSHIP));
    assertEquals(true,authorizationService.isUserAuthorized(null, Collections.singletonList(Groups.CAMUNDA_ADMIN), Permissions.ALL, Resources.BATCH));
  }
}
