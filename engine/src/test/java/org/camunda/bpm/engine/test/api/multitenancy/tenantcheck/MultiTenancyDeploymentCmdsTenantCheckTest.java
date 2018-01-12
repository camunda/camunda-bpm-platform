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

package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.Resource;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

/**
 * @author kristin.polenz
 */
public class MultiTenancyDeploymentCmdsTenantCheckTest {

  protected static final String TENANT_TWO = "tenant2";
  protected static final String TENANT_ONE = "tenant1";

  protected static final BpmnModelInstance emptyProcess = Bpmn.createExecutableProcess().done();
  protected static final BpmnModelInstance startEndProcess = Bpmn.createExecutableProcess().startEvent().endEvent().done();

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ExpectedException thrown= ExpectedException.none();

  protected RepositoryService repositoryService;
  protected IdentityService identityService;
  protected ProcessEngineConfiguration processEngineConfiguration;

  @Before
  public void init() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    repositoryService = engineRule.getRepositoryService();
    identityService = engineRule.getIdentityService();
  }

  @Test
  public void createDeploymentForAnotherTenant() {
    identityService.setAuthentication("user", null, null);

    repositoryService.createDeployment().addModelInstance("emptyProcess.bpmn", emptyProcess)
      .tenantId(TENANT_ONE).deploy();

    identityService.clearAuthentication();

    DeploymentQuery query = repositoryService.createDeploymentQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  @Test
  public void createDeploymentWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    repositoryService.createDeployment().addModelInstance("emptyProcess.bpmn", emptyProcess)
      .tenantId(TENANT_ONE).deploy();

    identityService.clearAuthentication();

    DeploymentQuery query = repositoryService.createDeploymentQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  @Test
  public void createDeploymentDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    repositoryService.createDeployment().addModelInstance("emptyProcessOne", emptyProcess).tenantId(TENANT_ONE).deploy();
    repositoryService.createDeployment().addModelInstance("emptyProcessTwo", startEndProcess).tenantId(TENANT_TWO).deploy();

    DeploymentQuery query = repositoryService.createDeploymentQuery();
    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  @Test
  public void failToDeleteDeploymentNoAuthenticatedTenant() {
    Deployment deployment = testRule.deployForTenant(TENANT_ONE, emptyProcess);

    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot delete the deployment");

    repositoryService.deleteDeployment(deployment.getId());
  }

  @Test
  public void deleteDeploymentWithAuthenticatedTenant() {
    Deployment deployment = testRule.deployForTenant(TENANT_ONE, emptyProcess);

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    repositoryService.deleteDeployment(deployment.getId());

    identityService.clearAuthentication();

    DeploymentQuery query = repositoryService.createDeploymentQuery();
    assertThat(query.count(), is(0L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(0L));
  }

  @Test
  public void deleteDeploymentDisabledTenantCheck() {
    Deployment deploymentOne = testRule.deployForTenant(TENANT_ONE, emptyProcess);
    Deployment deploymentTwo = testRule.deployForTenant(TENANT_TWO, startEndProcess);

    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    repositoryService.deleteDeployment(deploymentOne.getId());
    repositoryService.deleteDeployment(deploymentTwo.getId());

    DeploymentQuery query = repositoryService.createDeploymentQuery();
    assertThat(query.count(), is(0L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(0L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
  }

  @Test
  public void failToGetDeploymentResourceNamesNoAuthenticatedTenant() {
    Deployment deployment = testRule.deployForTenant(TENANT_ONE, emptyProcess);

    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the deployment");

    repositoryService.getDeploymentResourceNames(deployment.getId());
  }

  @Test
  public void getDeploymentResourceNamesWithAuthenticatedTenant() {
    Deployment deployment = testRule.deployForTenant(TENANT_ONE, emptyProcess);

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    List<String> deploymentResourceNames = repositoryService.getDeploymentResourceNames(deployment.getId());
    assertThat(deploymentResourceNames, hasSize(1));
  }

  @Test
  public void getDeploymentResourceNamesDisabledTenantCheck() {
    Deployment deploymentOne = testRule.deployForTenant(TENANT_ONE, emptyProcess);
    Deployment deploymentTwo = testRule.deployForTenant(TENANT_TWO, startEndProcess);

    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    List<String> deploymentResourceNames = repositoryService.getDeploymentResourceNames(deploymentOne.getId());
    assertThat(deploymentResourceNames, hasSize(1));

    deploymentResourceNames = repositoryService.getDeploymentResourceNames(deploymentTwo.getId());
    assertThat(deploymentResourceNames, hasSize(1));
  }

  @Test
  public void failToGetDeploymentResourcesNoAuthenticatedTenant() {
    Deployment deployment = testRule.deployForTenant(TENANT_ONE, emptyProcess);

    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the deployment");

    repositoryService.getDeploymentResources(deployment.getId());
  }

  @Test
  public void getDeploymentResourcesWithAuthenticatedTenant() {
    Deployment deployment = testRule.deployForTenant(TENANT_ONE, emptyProcess);

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    List<Resource> deploymentResources = repositoryService.getDeploymentResources(deployment.getId());
    assertThat(deploymentResources, hasSize(1));
  }

  @Test
  public void getDeploymentResourcesDisabledTenantCheck() {
    Deployment deploymentOne = testRule.deployForTenant(TENANT_ONE, emptyProcess);
    Deployment deploymentTwo = testRule.deployForTenant(TENANT_TWO, startEndProcess);

    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    List<Resource> deploymentResources = repositoryService.getDeploymentResources(deploymentOne.getId());
    assertThat(deploymentResources, hasSize(1));

    deploymentResources = repositoryService.getDeploymentResources(deploymentTwo.getId());
    assertThat(deploymentResources, hasSize(1));
  }

  @Test
  public void failToGetResourceAsStreamNoAuthenticatedTenant() {
    Deployment deployment = testRule.deployForTenant(TENANT_ONE, emptyProcess);

    Resource resource = repositoryService.getDeploymentResources(deployment.getId()).get(0);

    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the deployment");

    repositoryService.getResourceAsStream(deployment.getId(), resource.getName());
  }

  @Test
  public void getResourceAsStreamWithAuthenticatedTenant() {
    Deployment deployment = testRule.deployForTenant(TENANT_ONE, emptyProcess);

    Resource resource = repositoryService.getDeploymentResources(deployment.getId()).get(0);

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    InputStream inputStream = repositoryService.getResourceAsStream(deployment.getId(), resource.getName());
    assertThat(inputStream, notNullValue());
  }

  @Test
  public void getResourceAsStreamDisabledTenantCheck() {
    Deployment deploymentOne = testRule.deployForTenant(TENANT_ONE, emptyProcess);
    Deployment deploymentTwo = testRule.deployForTenant(TENANT_TWO, startEndProcess);

    Resource resourceOne = repositoryService.getDeploymentResources(deploymentOne.getId()).get(0);
    Resource resourceTwo = repositoryService.getDeploymentResources(deploymentTwo.getId()).get(0);

    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    InputStream inputStream = repositoryService.getResourceAsStream(deploymentOne.getId(), resourceOne.getName());
    assertThat(inputStream, notNullValue());

    inputStream = repositoryService.getResourceAsStream(deploymentTwo.getId(), resourceTwo.getName());
    assertThat(inputStream, notNullValue());
  }

  @Test
  public void failToGetResourceAsStreamByIdNoAuthenticatedTenant() {
    Deployment deployment = testRule.deployForTenant(TENANT_ONE, emptyProcess);

    Resource resource = repositoryService.getDeploymentResources(deployment.getId()).get(0);

    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the deployment");

    repositoryService.getResourceAsStreamById(deployment.getId(), resource.getId());
  }

  @Test
  public void getResourceAsStreamByIdWithAuthenticatedTenant() {
    Deployment deployment = testRule.deployForTenant(TENANT_ONE, emptyProcess);

    Resource resource = repositoryService.getDeploymentResources(deployment.getId()).get(0);

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    InputStream inputStream = repositoryService.getResourceAsStreamById(deployment.getId(), resource.getId());
    assertThat(inputStream, notNullValue());
  }

  @Test
  public void getResourceAsStreamByIdDisabledTenantCheck() {
    Deployment deploymentOne = testRule.deployForTenant(TENANT_ONE, emptyProcess);
    Deployment deploymentTwo = testRule.deployForTenant(TENANT_TWO, startEndProcess);

    Resource resourceOne = repositoryService.getDeploymentResources(deploymentOne.getId()).get(0);
    Resource resourceTwo = repositoryService.getDeploymentResources(deploymentTwo.getId()).get(0);

    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    InputStream inputStream = repositoryService.getResourceAsStreamById(deploymentOne.getId(), resourceOne.getId());
    assertThat(inputStream, notNullValue());

    inputStream = repositoryService.getResourceAsStreamById(deploymentTwo.getId(), resourceTwo.getId());
    assertThat(inputStream, notNullValue());
  }

  @Test
  public void redeployForDifferentAuthenticatedTenants() {
    Deployment deploymentOne = repositoryService.createDeployment()
      .addModelInstance("emptyProcess.bpmn", emptyProcess)
      .addModelInstance("startEndProcess.bpmn", startEndProcess)
      .tenantId(TENANT_ONE)
      .deploy();

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_TWO));

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the deployment");

    repositoryService.createDeployment()
        .addDeploymentResources(deploymentOne.getId())
        .tenantId(TENANT_TWO)
        .deploy();
  }

  @Test
  public void redeployForTheSameAuthenticatedTenant() {
    Deployment deploymentOne = repositoryService.createDeployment()
      .addModelInstance("emptyProcess.bpmn", emptyProcess)
      .addModelInstance("startEndProcess.bpmn", startEndProcess)
      .tenantId(TENANT_ONE)
      .deploy();

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    repositoryService.createDeployment()
        .addDeploymentResources(deploymentOne.getId())
        .tenantId(TENANT_ONE)
        .deploy();

    DeploymentQuery query = repositoryService.createDeploymentQuery();
    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(2L));
  }

  @Test
  public void redeployForDifferentAuthenticatedTenantsDisabledTenantCheck() {
    Deployment deploymentOne = repositoryService.createDeployment()
      .addModelInstance("emptyProcess.bpmn", emptyProcess)
      .addModelInstance("startEndProcess.bpmn", startEndProcess)
      .tenantId(TENANT_ONE)
      .deploy();

    identityService.setAuthentication("user", null, null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    repositoryService.createDeployment()
        .addDeploymentResources(deploymentOne.getId())
        .tenantId(TENANT_TWO)
        .deploy();

    DeploymentQuery query = repositoryService.createDeploymentQuery();
    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  @After
  public void tearDown() throws Exception {
    identityService.clearAuthentication();
    for(Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }
}
