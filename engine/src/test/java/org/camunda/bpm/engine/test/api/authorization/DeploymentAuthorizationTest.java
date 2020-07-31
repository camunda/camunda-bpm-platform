/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.authorization;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.CREATE;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.DEPLOYMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.AuthorizationQuery;
import org.camunda.bpm.engine.authorization.Groups;
import org.camunda.bpm.engine.impl.AbstractQuery;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.Resource;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class DeploymentAuthorizationTest extends AuthorizationTest {

  private static final String REQUIRED_ADMIN_AUTH_EXCEPTION = "ENGINE-03029 Required admin authenticated group or user.";
  protected static final String FIRST_RESOURCE = "org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml";
  protected static final String SECOND_RESOURCE = "org/camunda/bpm/engine/test/api/authorization/messageBoundaryEventProcess.bpmn20.xml";

  // query ////////////////////////////////////////////////////////////

  @Test
  public void testSimpleDeploymentQueryWithoutAuthorization() {
    // given
    String deploymentId = createDeployment(null);

    // when
    DeploymentQuery query = repositoryService.createDeploymentQuery();

    // then
    verifyQueryResults(query, 0);

    deleteDeployment(deploymentId);
  }

  @Test
  public void testSimpleDeploymentQueryWithReadPermissionOnDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, deploymentId, userId, READ);

    // when
    DeploymentQuery query = repositoryService.createDeploymentQuery();

    // then
    verifyQueryResults(query, 1);

    deleteDeployment(deploymentId);
  }

  @Test
  public void testSimpleDeploymentQueryWithReadPermissionOnAnyDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    // when
    DeploymentQuery query = repositoryService.createDeploymentQuery();

    // then
    verifyQueryResults(query, 1);

    deleteDeployment(deploymentId);
  }

  @Test
  public void testSimpleDeploymentQueryWithMultiple() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, deploymentId, userId, READ);
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    // when
    DeploymentQuery query = repositoryService.createDeploymentQuery();

    // then
    verifyQueryResults(query, 1);

    deleteDeployment(deploymentId);
  }

  @Test
  public void testDeploymentQueryWithoutAuthorization() {
    // given
    String deploymentId1 = createDeployment("first");
    String deploymentId2 = createDeployment("second");

    // when
    DeploymentQuery query = repositoryService.createDeploymentQuery();

    // then
    verifyQueryResults(query, 0);

    deleteDeployment(deploymentId1);
    deleteDeployment(deploymentId2);
  }

  @Test
  public void testDeploymentQueryWithReadPermissionOnDeployment() {
    // given
    String deploymentId1 = createDeployment("first");
    String deploymentId2 = createDeployment("second");
    createGrantAuthorization(DEPLOYMENT, deploymentId1, userId, READ);

    // when
    DeploymentQuery query = repositoryService.createDeploymentQuery();

    // then
    verifyQueryResults(query, 1);

    deleteDeployment(deploymentId1);
    deleteDeployment(deploymentId2);
  }

  @Test
  public void testDeploymentQueryWithReadPermissionOnAnyDeployment() {
    // given
    String deploymentId1 = createDeployment("first");
    String deploymentId2 = createDeployment("second");
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    // when
    DeploymentQuery query = repositoryService.createDeploymentQuery();

    // then
    verifyQueryResults(query, 2);

    deleteDeployment(deploymentId1);
    deleteDeployment(deploymentId2);
  }

  // create deployment ///////////////////////////////////////////////

  @Test
  public void testCreateDeploymentWithoutAuthoriatzion() {
    // given

    try {
      // when
      repositoryService
          .createDeployment()
          .addClasspathResource(FIRST_RESOURCE)
          .deploy();
      fail("Exception expected: It should not be possible to create a new deployment");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(CREATE.getName(), message);
      testRule.assertTextPresent(DEPLOYMENT.resourceName(), message);
    }
  }

  @Test
  public void testCreateDeployment() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, CREATE);

    // when
    Deployment deployment = repositoryService
      .createDeployment()
      .addClasspathResource(FIRST_RESOURCE)
      .deploy();

    // then
    disableAuthorization();
    DeploymentQuery query = repositoryService.createDeploymentQuery();
    verifyQueryResults(query, 1);
    enableAuthorization();

    deleteDeployment(deployment.getId());
  }

  // delete deployment //////////////////////////////////////////////

  @Test
  public void testDeleteDeploymentWithoutAuthorization() {
    // given
    String deploymentId = createDeployment(null);

    try {
      // when
      repositoryService.deleteDeployment(deploymentId);
      fail("Exception expected: it should not be possible to delete a deployment");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(DELETE.getName(), message);
      testRule.assertTextPresent(DEPLOYMENT.resourceName(), message);
    }

    deleteDeployment(deploymentId);
  }

  @Test
  public void testDeleteDeploymentWithDeletePermissionOnDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, deploymentId, userId, DELETE);

    // when
    repositoryService.deleteDeployment(deploymentId);

    // then
    disableAuthorization();
    DeploymentQuery query = repositoryService.createDeploymentQuery();
    verifyQueryResults(query, 0);
    enableAuthorization();

    deleteDeployment(deploymentId);
  }

  @Test
  public void testDeleteDeploymentWithDeletePermissionOnAnyDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, ANY, userId, DELETE);

    // when
    repositoryService.deleteDeployment(deploymentId);

    // then
    disableAuthorization();
    DeploymentQuery query = repositoryService.createDeploymentQuery();
    verifyQueryResults(query, 0);
    enableAuthorization();

    deleteDeployment(deploymentId);
  }

  // get deployment resource names //////////////////////////////////

  @Test
  public void testGetDeploymentResourceNamesWithoutAuthorization() {
    // given
    String deploymentId = createDeployment(null);

    try {
      // when
      repositoryService.getDeploymentResourceNames(deploymentId);
      fail("Exception expected: it should not be possible to retrieve deployment resource names");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(DEPLOYMENT.resourceName(), message);
    }

    deleteDeployment(deploymentId);
  }

  @Test
  public void testGetDeploymentResourceNamesWithReadPermissionOnDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, deploymentId, userId, READ);

    // when
    List<String> names = repositoryService.getDeploymentResourceNames(deploymentId);

    // then
    assertFalse(names.isEmpty());
    assertEquals(2, names.size());
    assertTrue(names.contains(FIRST_RESOURCE));
    assertTrue(names.contains(SECOND_RESOURCE));

    deleteDeployment(deploymentId);
  }

  @Test
  public void testGetDeploymentResourceNamesWithReadPermissionOnAnyDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    // when
    List<String> names = repositoryService.getDeploymentResourceNames(deploymentId);

    // then
    assertFalse(names.isEmpty());
    assertEquals(2, names.size());
    assertTrue(names.contains(FIRST_RESOURCE));
    assertTrue(names.contains(SECOND_RESOURCE));

    deleteDeployment(deploymentId);
  }

  // get deployment resources //////////////////////////////////

  @Test
  public void testGetDeploymentResourcesWithoutAuthorization() {
    // given
    String deploymentId = createDeployment(null);

    try {
      // when
      repositoryService.getDeploymentResources(deploymentId);
      fail("Exception expected: it should not be possible to retrieve deployment resources");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(DEPLOYMENT.resourceName(), message);
    }

    deleteDeployment(deploymentId);
  }

  @Test
  public void testGetDeploymentResourcesWithReadPermissionOnDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, deploymentId, userId, READ);

    // when
    List<Resource> resources = repositoryService.getDeploymentResources(deploymentId);

    // then
    assertFalse(resources.isEmpty());
    assertEquals(2, resources.size());

    deleteDeployment(deploymentId);
  }

  @Test
  public void testGetDeploymentResourcesWithReadPermissionOnAnyDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    // when
    List<Resource> resources = repositoryService.getDeploymentResources(deploymentId);

    // then
    assertFalse(resources.isEmpty());
    assertEquals(2, resources.size());

    deleteDeployment(deploymentId);
  }

  // get resource as stream //////////////////////////////////

  @Test
  public void testGetResourceAsStreamWithoutAuthorization() {
    // given
    String deploymentId = createDeployment(null);

    try {
      // when
      repositoryService.getResourceAsStream(deploymentId, FIRST_RESOURCE);
      fail("Exception expected: it should not be possible to retrieve a resource as stream");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(DEPLOYMENT.resourceName(), message);
    }

    deleteDeployment(deploymentId);
  }

  @Test
  public void testGetResourceAsStreamWithReadPermissionOnDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, deploymentId, userId, READ);

    // when
    InputStream stream = repositoryService.getResourceAsStream(deploymentId, FIRST_RESOURCE);

    // then
    assertNotNull(stream);

    deleteDeployment(deploymentId);
  }

  @Test
  public void testGetResourceAsStreamWithReadPermissionOnAnyDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    // when
    InputStream stream = repositoryService.getResourceAsStream(deploymentId, FIRST_RESOURCE);

    // then
    assertNotNull(stream);

    deleteDeployment(deploymentId);
  }

  // get resource as stream by id//////////////////////////////////

  @Test
  public void testGetResourceAsStreamByIdWithoutAuthorization() {
    // given
    String deploymentId = createDeployment(null);

    disableAuthorization();
    List<Resource> resources = repositoryService.getDeploymentResources(deploymentId);
    enableAuthorization();
    String resourceId = resources.get(0).getId();

    try {
      // when
      repositoryService.getResourceAsStreamById(deploymentId, resourceId);
      fail("Exception expected: it should not be possible to retrieve a resource as stream");
    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      testRule.assertTextPresent(userId, message);
      testRule.assertTextPresent(READ.getName(), message);
      testRule.assertTextPresent(DEPLOYMENT.resourceName(), message);
    }

    deleteDeployment(deploymentId);
  }

  @Test
  public void testGetResourceAsStreamByIdWithReadPermissionOnDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, deploymentId, userId, READ);

    disableAuthorization();
    List<Resource> resources = repositoryService.getDeploymentResources(deploymentId);
    enableAuthorization();
    String resourceId = resources.get(0).getId();

    // when
    InputStream stream = repositoryService.getResourceAsStreamById(deploymentId, resourceId);

    // then
    assertNotNull(stream);

    deleteDeployment(deploymentId);
  }

  @Test
  public void testGetResourceAsStreamByIdWithReadPermissionOnAnyDeployment() {
    // given
    String deploymentId = createDeployment(null);
    createGrantAuthorization(DEPLOYMENT, ANY, userId, READ);

    disableAuthorization();
    List<Resource> resources = repositoryService.getDeploymentResources(deploymentId);
    enableAuthorization();
    String resourceId = resources.get(0).getId();

    // when
    InputStream stream = repositoryService.getResourceAsStreamById(deploymentId, resourceId);

    // then
    assertNotNull(stream);

    deleteDeployment(deploymentId);
  }

  // should create authorization /////////////////////////////////////

  @Test
  public void testCreateAuthorizationOnDeploy() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, CREATE);
    Deployment deployment = repositoryService
      .createDeployment()
      .addClasspathResource(FIRST_RESOURCE)
      .deploy();

    // when
    Authorization authorization = authorizationService
      .createAuthorizationQuery()
      .userIdIn(userId)
      .resourceId(deployment.getId())
      .singleResult();

    // then
    assertNotNull(authorization);
    assertTrue(authorization.isPermissionGranted(READ));
    assertTrue(authorization.isPermissionGranted(DELETE));
    assertFalse(authorization.isPermissionGranted(UPDATE));

    deleteDeployment(deployment.getId());
  }

  // clear authorization /////////////////////////////////////

  @Test
  public void testClearAuthorizationOnDeleteDeployment() {
    // given
    createGrantAuthorization(DEPLOYMENT, ANY, userId, CREATE);
    Deployment deployment = repositoryService
      .createDeployment()
      .addClasspathResource(FIRST_RESOURCE)
      .deploy();

    String deploymentId = deployment.getId();

    AuthorizationQuery query = authorizationService
      .createAuthorizationQuery()
      .userIdIn(userId)
      .resourceId(deploymentId);

    Authorization authorization = query.singleResult();
    assertNotNull(authorization);

    // when
    repositoryService.deleteDeployment(deploymentId);

    authorization = query.singleResult();
    assertNull(authorization);

    deleteDeployment(deploymentId);
  }

  // register process application ///////////////////////////////////

  @Test
  public void testRegisterProcessApplicationWithoutAuthorization() {
    // given
    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();
    ProcessApplicationReference reference = processApplication.getReference();
    String deploymentId = createDeployment(null, FIRST_RESOURCE).getId();

    try {
      // when
      managementService.registerProcessApplication(deploymentId, reference);
      fail("Exception expected: It should not be possible to register a process application");
    } catch (AuthorizationException e) {
      //then
      String message = e.getMessage();
      testRule.assertTextPresent(REQUIRED_ADMIN_AUTH_EXCEPTION, message);

    }

    deleteDeployment(deploymentId);
  }

  @Test
  public void testRegisterProcessApplicationAsCamundaAdmin() {
    // given
    identityService.setAuthentication(userId, Collections.singletonList(Groups.CAMUNDA_ADMIN));

    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();
    ProcessApplicationReference reference = processApplication.getReference();
    String deploymentId = createDeployment(null, FIRST_RESOURCE).getId();

    // when
    ProcessApplicationRegistration registration = managementService.registerProcessApplication(deploymentId, reference);

    // then
    assertNotNull(registration);
    assertNotNull(getProcessApplicationForDeployment(deploymentId));

    deleteDeployment(deploymentId);
  }

  // unregister process application ///////////////////////////////////

  @Test
  public void testUnregisterProcessApplicationWithoutAuthorization() {
    // given
    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();
    String deploymentId = createDeployment(null, FIRST_RESOURCE).getId();
    ProcessApplicationReference reference = processApplication.getReference();
    registerProcessApplication(deploymentId, reference);

    try {
      // when
      managementService.unregisterProcessApplication(deploymentId, true);
      fail("Exception expected: It should not be possible to unregister a process application");
    } catch (AuthorizationException e) {
      //then
      String message = e.getMessage();
      testRule.assertTextPresent(REQUIRED_ADMIN_AUTH_EXCEPTION, message);

    }

    deleteDeployment(deploymentId);
  }

  @Test
  public void testUnregisterProcessApplicationAsCamundaAdmin() {
    // given
    identityService.setAuthentication(userId, Collections.singletonList(Groups.CAMUNDA_ADMIN));

    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();
    String deploymentId = createDeployment(null, FIRST_RESOURCE).getId();
    ProcessApplicationReference reference = processApplication.getReference();
    registerProcessApplication(deploymentId, reference);

    // when
    managementService.unregisterProcessApplication(deploymentId, true);

    // then
    assertNull(getProcessApplicationForDeployment(deploymentId));

    deleteDeployment(deploymentId);
  }

  // get process application for deployment ///////////////////////////////////

  @Test
  public void testGetProcessApplicationForDeploymentWithoutAuthorization() {
    // given
    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();
    String deploymentId = createDeployment(null, FIRST_RESOURCE).getId();
    ProcessApplicationReference reference = processApplication.getReference();
    registerProcessApplication(deploymentId, reference);

    try {
      // when
      managementService.getProcessApplicationForDeployment(deploymentId);
      fail("Exception expected: It should not be possible to get the process application");
    } catch (AuthorizationException e) {
      //then
      String message = e.getMessage();
      testRule.assertTextPresent(REQUIRED_ADMIN_AUTH_EXCEPTION, message);

    }

    deleteDeployment(deploymentId);
  }

  @Test
  public void testGetProcessApplicationForDeploymentAsCamundaAdmin() {
    // given
    identityService.setAuthentication(userId, Collections.singletonList(Groups.CAMUNDA_ADMIN));

    EmbeddedProcessApplication processApplication = new EmbeddedProcessApplication();
    String deploymentId = createDeployment(null, FIRST_RESOURCE).getId();
    ProcessApplicationReference reference = processApplication.getReference();
    registerProcessApplication(deploymentId, reference);

    // when
    String application = managementService.getProcessApplicationForDeployment(deploymentId);

    // then
    assertNotNull(application);

    deleteDeployment(deploymentId);
  }

  // get registered deployments ///////////////////////////////////

  @Test
  public void testGetRegisteredDeploymentsWithoutAuthorization() {
    // given
    String deploymentId = createDeployment(null, FIRST_RESOURCE).getId();

    try {
      // when
      managementService.getRegisteredDeployments();
      fail("Exception expected: It should not be possible to get the registered deployments");
    } catch (AuthorizationException e) {
      //then
      String message = e.getMessage();
      testRule.assertTextPresent(REQUIRED_ADMIN_AUTH_EXCEPTION, message);

    }

    deleteDeployment(deploymentId);
  }

  @Test
  public void testGetRegisteredDeploymentsAsCamundaAdmin() {
    // given
    identityService.setAuthentication(userId, Collections.singletonList(Groups.CAMUNDA_ADMIN));

    String deploymentId = createDeployment(null, FIRST_RESOURCE).getId();

    // when
    Set<String> deployments = managementService.getRegisteredDeployments();

    // then
    assertTrue(deployments.contains(deploymentId));

    deleteDeployment(deploymentId);
  }

  // register deployment for job executor ///////////////////////////////////

  @Test
  public void testRegisterDeploymentForJobExecutorWithoutAuthorization() {
    // given
    String deploymentId = createDeployment(null, FIRST_RESOURCE).getId();

    try {
      // when
      managementService.registerDeploymentForJobExecutor(deploymentId);
      fail("Exception expected: It should not be possible to register the deployment");
    } catch (AuthorizationException e) {
      //then
      String message = e.getMessage();
      testRule.assertTextPresent(REQUIRED_ADMIN_AUTH_EXCEPTION, message);

    }

    deleteDeployment(deploymentId);
  }

  @Test
  public void testRegisterDeploymentForJobExecutorAsCamundaAdmin() {
    // given
    identityService.setAuthentication(userId, Collections.singletonList(Groups.CAMUNDA_ADMIN));

    String deploymentId = createDeployment(null, FIRST_RESOURCE).getId();

    // when
    managementService.registerDeploymentForJobExecutor(deploymentId);

    // then
    assertTrue(getRegisteredDeployments().contains(deploymentId));

    deleteDeployment(deploymentId);
  }

  // unregister deployment for job executor ///////////////////////////////////

  @Test
  public void testUnregisterDeploymentForJobExecutorWithoutAuthorization() {
    // given
    String deploymentId = createDeployment(null, FIRST_RESOURCE).getId();

    try {
      // when
      managementService.unregisterDeploymentForJobExecutor(deploymentId);
      fail("Exception expected: It should not be possible to unregister the deployment");
    } catch (AuthorizationException e) {
      //then
      String message = e.getMessage();
      testRule.assertTextPresent(REQUIRED_ADMIN_AUTH_EXCEPTION, message);

    }

    deleteDeployment(deploymentId);
  }

  @Test
  public void testUnregisterDeploymentForJobExecutorAsCamundaAdmin() {
    // given
    identityService.setAuthentication(userId, Collections.singletonList(Groups.CAMUNDA_ADMIN));

    String deploymentId = createDeployment(null, FIRST_RESOURCE).getId();

    // when
    managementService.unregisterDeploymentForJobExecutor(deploymentId);

    // then
    assertFalse(getRegisteredDeployments().contains(deploymentId));

    deleteDeployment(deploymentId);
  }

  // helper /////////////////////////////////////////////////////////

  protected void verifyQueryResults(DeploymentQuery query, int countExpected) {
    verifyQueryResults((AbstractQuery<?, ?>) query, countExpected);
  }

  protected String createDeployment(String name) {
    return createDeployment(name, FIRST_RESOURCE, SECOND_RESOURCE).getId();
  }

  protected void registerProcessApplication(String deploymentId, ProcessApplicationReference reference) {
    disableAuthorization();
    managementService.registerProcessApplication(deploymentId, reference);
    enableAuthorization();
  }

  protected String getProcessApplicationForDeployment(String deploymentId) {
    disableAuthorization();
    String applications = managementService.getProcessApplicationForDeployment(deploymentId);
    enableAuthorization();
    return applications;
  }

  protected Set<String> getRegisteredDeployments() {
    disableAuthorization();
    Set<String> deployments = managementService.getRegisteredDeployments();
    enableAuthorization();
    return deployments;
  }

}
