/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.cockpit.plugin.base.authorization;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Authorization.AUTH_TYPE_GRANT;
import static org.camunda.bpm.engine.authorization.Permissions.ALL;
import static org.camunda.bpm.engine.authorization.Resources.AUTHORIZATION;
import static org.camunda.bpm.engine.authorization.Resources.USER;

import java.util.Arrays;

import org.camunda.bpm.cockpit.plugin.test.AbstractCockpitPluginTest;
import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resource;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;

/**
 * @author Roman Smirnov
 *
 */
public abstract class AuthorizationTest extends AbstractCockpitPluginTest {

  protected ProcessEngine processEngine;
  protected String engineName;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  protected IdentityService identityService;
  protected AuthorizationService authorizationService;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;

  protected String userId = "test";
  protected String groupId = "accounting";
  protected User user;
  protected Group group;

  @Before
  public void setUp() throws Exception {
    super.before();

    processEngine = getProcessEngine();
    engineName = getProcessEngine().getName();
    processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();

    identityService = processEngine.getIdentityService();
    authorizationService = processEngine.getAuthorizationService();
    repositoryService = processEngine.getRepositoryService();
    runtimeService = processEngine.getRuntimeService();

    user = createUser(userId);
    group = createGroup(groupId);

    identityService.createMembership(userId, groupId);

    identityService.setAuthentication(userId, Arrays.asList(groupId));
    enableAuthorization();
  }

  @After
  public void tearDown() {
    disableAuthorization();
    for (User user : identityService.createUserQuery().list()) {
      identityService.deleteUser(user.getId());
    }
    for (Group group : identityService.createGroupQuery().list()) {
      identityService.deleteGroup(group.getId());
    }
    for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
  }

  // user ////////////////////////////////////////////////////////////////

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

    return user;
  }

  // group //////////////////////////////////////////////////////////////

  protected Group createGroup(String groupId) {
    Group group = identityService.newGroup(groupId);
    identityService.saveGroup(group);;
    return group;
  }

  // authorization ///////////////////////////////////////////////////////

  protected void createGrantAuthorization(Resource resource, String resourceId, String userId, Permission... permissions) {
    Authorization authorization = createGrantAuthorization(resource, resourceId);
    authorization.setUserId(userId);
    for (Permission permission : permissions) {
      authorization.addPermission(permission);
    }
    saveAuthorization(authorization);
  }

  protected Authorization createGrantAuthorization(Resource resource, String resourceId) {
    Authorization authorization = createAuthorization(AUTH_TYPE_GRANT, resource, resourceId);
    return authorization;
  }

  protected Authorization createAuthorization(int type, Resource resource, String resourceId) {
    Authorization authorization = authorizationService.createNewAuthorization(type);

    authorization.setResource(resource);
    if (resourceId != null) {
      authorization.setResourceId(resourceId);
    }

    return authorization;
  }

  protected void saveAuthorization(Authorization authorization) {
    authorizationService.saveAuthorization(authorization);
  }

  // enable/disable authorization //////////////////////////////////////////////

  protected void enableAuthorization() {
    processEngineConfiguration.setAuthorizationEnabled(true);
  }

  protected void disableAuthorization() {
    processEngineConfiguration.setAuthorizationEnabled(false);
  }

  // actions (executed without authorization) ///////////////////////////////////

  protected ProcessDefinition selectProcessDefinitionByKey(String processDefinitionKey) {
    disableAuthorization();
    ProcessDefinition definition = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(processDefinitionKey)
        .singleResult();
    enableAuthorization();
    return definition;
  }

  protected ProcessInstance selectAnyProcessInstanceByKey(String processDefinitionKey) {
    disableAuthorization();
    ProcessInstance instance = runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).listPage(0, 1).get(0);
    enableAuthorization();
    return instance;
  }

  protected void startProcessInstances(String processDefinitionKey, int numOfInstances) {
    disableAuthorization();
    for (int i = 0; i < numOfInstances; i++) {
      runtimeService.startProcessInstanceByKey(processDefinitionKey, "businessKey_" + i);
    }
    enableAuthorization();
  }

  protected Deployment createDeployment(String name, String... resources) {
    disableAuthorization();
    DeploymentBuilder builder = repositoryService.createDeployment();
    for (String resource : resources) {
      builder.addClasspathResource(resource);
    }
    Deployment deployment = builder.deploy();
    enableAuthorization();
    return deployment;
  }

  protected void deleteDeployment(String deploymentId) {
    disableAuthorization();
    repositoryService.deleteDeployment(deploymentId, true);
    enableAuthorization();
  }

}
