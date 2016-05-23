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

    // The below test cases are skipped for H2 as there is a bug in H2 version 1.3 (Query does not return the expected output)
    // This H2 exclusion check will be removed as part of CAM-6044, when the H2 database is upgraded to the version 1.4 (Bug was fixed)
    if (DbSqlSessionFactory.H2.equals(processEngineConfiguration.getDatabaseType())) {
      return;
    }

    processEngineConfiguration.setAuthorizationEnabled(true);
    assertEquals(true,authorizationService.isUserAuthorized(null, Collections.singletonList(Groups.CAMUNDA_ADMIN), Permissions.ALL, Resources.TENANT));
    assertEquals(true,authorizationService.isUserAuthorized(null, Collections.singletonList(Groups.CAMUNDA_ADMIN), Permissions.ALL, Resources.TENANT_MEMBERSHIP));
    assertEquals(true,authorizationService.isUserAuthorized(null, Collections.singletonList(Groups.CAMUNDA_ADMIN), Permissions.ALL, Resources.BATCH));
  }
}
