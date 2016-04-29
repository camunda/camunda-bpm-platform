package org.camunda.bpm.engine.test.api.multitenancy.cmd;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GLOBAL;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Permissions.ALL;
import static org.camunda.bpm.engine.authorization.Resources.AUTHORIZATION;
import static org.camunda.bpm.engine.authorization.Resources.TENANT;
import static org.camunda.bpm.engine.authorization.Resources.USER;
import static org.camunda.bpm.engine.authorization.Resources.DEPLOYMENT;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.TASK;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import java.util.Arrays;
import java.util.Map;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import junit.framework.AssertionFailedError;

/**
 * 
 * @author Deivarayan Azhagappan
 *
 */
public class MultiTenancyCmdTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String multiTenancyProcessDefinitionKey = "multiTenancyCmdTest"; 

  protected ProcessEngineRule engineRule = new ProcessEngineRule(true);

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ExpectedException thrown= ExpectedException.none();
  
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected RepositoryService repositoryService;
  protected IdentityService identityService;
  protected AuthorizationService authorizationService;
  protected FormService formService;

  String deploymentId;

  ProcessEngine processEngine;
  ProcessEngineConfiguration processEngineConfiguration;

  protected String userId = "test";
  protected User user;
  
  protected static final String PROCESS_KEY = "oneTaskProcess";
  protected static final String START_FORM_RESOURCE = "org/camunda/bpm/engine/test/api/form/FormServiceTest.startFormFields.bpmn20.xml";
  
  @Before
  public void init() {
    taskService = engineRule.getTaskService();
    repositoryService = engineRule.getRepositoryService();
    identityService = engineRule.getIdentityService();
    runtimeService = engineRule.getRuntimeService();
    authorizationService = engineRule.getAuthorizationService();
    formService = engineRule.getFormService();

    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    processEngineConfiguration.setTenantCheckEnabled(true);
    enableAuthorization();
    processEngine = engineRule.getProcessEngine();
    
    disableAuthorization();
    deploymentId = createDeployment(null,
      "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml",
      START_FORM_RESOURCE
      ).getId();

    user = createUser(userId);
    
    enableAuthorization();
  }

  @After
  public void tearDown() {
    processEngineConfiguration.setAuthorizationEnabled(false);
    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
    for (Group group : identityService.createGroupQuery().list()) {
      identityService.deleteGroup(group.getId());
    }
    for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
    deleteDeployment(deploymentId, true);
  }
  protected User createUser(String userId) {
    User user = identityService.newUser(userId);
    identityService.saveUser(user);

    // give user all permission to manipulate authorizations
    Authorization authorization = createGrantAuthorization(AUTHORIZATION, ANY);
    authorization.setUserId(userId);
    authorization.addPermission(ALL);
    saveAuthorization(authorization);

    // give user all permission to manipulate users
    authorization = createGrantAuthorization(USER, ANY);
    authorization.setUserId(userId);
    authorization.addPermission(Permissions.ALL);
    saveAuthorization(authorization);

    // give user all permission to deployment
    authorization = createGrantAuthorization(DEPLOYMENT, ANY);
    authorization.setUserId(userId);
    authorization.addPermission(Permissions.ALL);
    saveAuthorization(authorization);

    // give user all permissions to process instance
    authorization = createGrantAuthorization(PROCESS_INSTANCE, ANY);
    authorization.setUserId(userId);
    authorization.addPermission(Permissions.ALL);
    saveAuthorization(authorization);

    // give user all permissions to task
    authorization = createGrantAuthorization(TASK, ANY);
    authorization.setUserId(userId);
    authorization.addPermission(Permissions.ALL);
    saveAuthorization(authorization);
    
    // give user all permissions to process definition
    authorization = createGrantAuthorization(PROCESS_DEFINITION, ANY);
    authorization.setUserId(userId);
    authorization.addPermission(Permissions.ALL);
    saveAuthorization(authorization);
    
    return user;
  }

  public void createTenantAuthorization() {
    identityService.setAuthentication(userId, null, Arrays.asList(TENANT_ONE));
  }

  public void setUserContext() {
    // set user authorization
    identityService.setAuthentication(userId, null);
  }

  protected Authorization createGrantAuthorization(Resource resource, String resourceId) {
    Authorization authorization = createAuthorization(AUTH_TYPE_GRANT, resource, resourceId);
    return authorization;
  }

  protected void createTenantAuthorization(String tenantId) {
    Authorization basePerms = authorizationService.createNewAuthorization(AUTH_TYPE_GLOBAL);
    basePerms.setResource(TENANT);
    basePerms.setResourceId(tenantId);
    authorizationService.saveAuthorization(basePerms);
  }
  
  protected void saveAuthorization(Authorization authorization) {
    authorizationService.saveAuthorization(authorization);
  }

  protected Authorization createAuthorization(int type, Resource resource, String resourceId) {
    Authorization authorization = authorizationService.createNewAuthorization(type);

    authorization.setResource(resource);
    if (resourceId != null) {
      authorization.setResourceId(resourceId);
    }

    return authorization;
  }

  protected Deployment createDeployment(final String name, final String... resources) {
    DeploymentBuilder builder = repositoryService.createDeployment();
    for (String resource : resources) {
      builder.addClasspathResource(resource);
    }
    return builder.deploy();
  } 

  protected void deleteDeployment(final String deploymentId, final boolean cascade) {
    repositoryService.deleteDeployment(deploymentId, cascade);
  }

  protected ProcessInstance startProcessInstanceByKey(String key) {
    return startProcessInstanceByKey(key, null);
  }

  protected ProcessInstance startProcessInstanceByKey(final String key, final Map<String, Object> variables) {
    return runtimeService.startProcessInstanceByKey(key, variables);
  }

  protected ProcessInstance startProcessInstanceForTenant(String tenant, final Map<String, Object> variables, String processDefinitionKey) {
    return runtimeService.createProcessInstanceByKey(processDefinitionKey)
        .setVariables(variables)
        .processDefinitionTenantId(tenant)
        .execute();
  }

  protected ProcessInstance startProcessInstanceForTenant(String tenant, String processDefinitionKey) {
    return runtimeService.createProcessInstanceByKey(processDefinitionKey)
        .processDefinitionTenantId(tenant)
        .execute();
  }

  protected void disableAuthorization() {
    processEngineConfiguration.setAuthorizationEnabled(false);
  }

  protected void enableAuthorization() {
    processEngineConfiguration.setAuthorizationEnabled(true);
  }

  public void assertProcessEnded(final String processInstanceId) {
    ProcessInstance processInstance = processEngine
      .getRuntimeService()
      .createProcessInstanceQuery()
      .processInstanceId(processInstanceId)
      .singleResult();

    if (processInstance!=null) {
      throw new AssertionFailedError("Expected finished process instance '"+processInstanceId+"' but it was still in the db");
    }
  }
}
