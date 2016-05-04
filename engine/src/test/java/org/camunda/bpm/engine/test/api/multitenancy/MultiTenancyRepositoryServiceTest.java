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

package org.camunda.bpm.engine.test.api.multitenancy;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.repository.ResourceDefinitionEntity;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.Resource;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

public class MultiTenancyRepositoryServiceTest {

  protected static final String TENANT_TWO = "tenant2";
  protected static final String TENANT_ONE = "tenant1";

  protected static final BpmnModelInstance emptyProcess = Bpmn.createExecutableProcess().done();
  protected static final String CMMN = "org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testSimpleDeployment.cmmn";
  protected static final String DMN = "org/camunda/bpm/engine/test/api/multitenancy/simpleDecisionTable.dmn";

  protected ProcessEngineRule engineRule = new ProcessEngineRule(true);

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
  public void deploymentWithoutTenantId() {
    createDeploymentBuilder()
      .deploy();

    Deployment deployment = repositoryService
        .createDeploymentQuery()
        .singleResult();

    assertThat(deployment, is(notNullValue()));
    assertThat(deployment.getTenantId(), is(nullValue()));
  }

  @Test
  public void deploymentWithTenantId() {
    createDeploymentBuilder()
      .tenantId(TENANT_ONE)
      .deploy();

    Deployment deployment = repositoryService
        .createDeploymentQuery()
        .singleResult();

    assertThat(deployment, is(notNullValue()));
    assertThat(deployment.getTenantId(), is(TENANT_ONE));
  }

  @Test
  public void processDefinitionVersionWithTenantId() {
    createDeploymentBuilder()
      .tenantId(TENANT_ONE)
      .deploy();

    createDeploymentBuilder()
      .tenantId(TENANT_ONE)
      .deploy();

    createDeploymentBuilder()
      .tenantId(TENANT_TWO)
      .deploy();

    List<ProcessDefinition> processDefinitions = repositoryService
        .createProcessDefinitionQuery()
        .orderByTenantId()
        .asc()
        .orderByProcessDefinitionVersion()
        .asc()
        .list();

    assertThat(processDefinitions.size(), is(3));
    // process definition was deployed twice for tenant one
    assertThat(processDefinitions.get(0).getVersion(), is(1));
    assertThat(processDefinitions.get(1).getVersion(), is(2));
    // process definition version of tenant two have to be independent from tenant one
    assertThat(processDefinitions.get(2).getVersion(), is(1));
  }

  @Test
  public void deploymentWithDuplicateFilteringForSameTenant() {
    // given: a deployment with tenant ID
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .tenantId(TENANT_ONE)
      .deploy();

    // if the same process is deployed with the same tenant ID again
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .tenantId(TENANT_ONE)
      .deploy();

    // then it does not create a new deployment
    assertThat(repositoryService.createDeploymentQuery().count(), is(1L));
  }

  @Test
  public void deploymentWithDuplicateFilteringForDifferentTenants() {
    // given: a deployment with tenant ID
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .tenantId(TENANT_ONE)
      .deploy();

    // if the same process is deployed with the another tenant ID
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .tenantId(TENANT_TWO)
      .deploy();

    // then a new deployment is created
    assertThat(repositoryService.createDeploymentQuery().count(), is(2L));
  }

  @Test
  public void deploymentWithDuplicateFilteringIgnoreDeploymentForNoTenant() {
    // given: a deployment without tenant ID
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .deploy();

    // if the same process is deployed with tenant ID
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .tenantId(TENANT_ONE)
      .deploy();

    // then a new deployment is created
    assertThat(repositoryService.createDeploymentQuery().count(), is(2L));
  }

  @Test
  public void deploymentWithDuplicateFilteringIgnoreDeploymentForTenant() {
    // given: a deployment with tenant ID
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .tenantId(TENANT_ONE)
      .deploy();

    // if the same process is deployed without tenant ID
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .deploy();

    // then a new deployment is created
    assertThat(repositoryService.createDeploymentQuery().count(), is(2L));
  }

  @Test
  public void getPreviousProcessDefinitionWithTenantId() {
    testRule.deployForTenant(TENANT_ONE, emptyProcess);
    testRule.deployForTenant(TENANT_ONE, emptyProcess);
    testRule.deployForTenant(TENANT_ONE, emptyProcess);

    testRule.deployForTenant(TENANT_TWO, emptyProcess);
    testRule.deployForTenant(TENANT_TWO, emptyProcess);

    List<ProcessDefinition> latestProcessDefinitions = repositoryService.createProcessDefinitionQuery()
      .latestVersion()
      .orderByTenantId()
      .asc()
      .list();

    ProcessDefinitionEntity previousDefinitionTenantOne = getPreviousDefinition((ProcessDefinitionEntity) latestProcessDefinitions.get(0));
    ProcessDefinitionEntity previousDefinitionTenantTwo = getPreviousDefinition((ProcessDefinitionEntity) latestProcessDefinitions.get(1));

    assertThat(previousDefinitionTenantOne.getVersion(), is(2));
    assertThat(previousDefinitionTenantOne.getTenantId(), is(TENANT_ONE));

    assertThat(previousDefinitionTenantTwo.getVersion(), is(1));
    assertThat(previousDefinitionTenantTwo.getTenantId(), is(TENANT_TWO));
  }

  @Test
  public void getPreviousCaseDefinitionWithTenantId() {
    testRule.deployForTenant(TENANT_ONE, CMMN);
    testRule.deployForTenant(TENANT_ONE, CMMN);
    testRule.deployForTenant(TENANT_ONE, CMMN);

    testRule.deployForTenant(TENANT_TWO, CMMN);
    testRule.deployForTenant(TENANT_TWO, CMMN);

    List<CaseDefinition> latestCaseDefinitions = repositoryService.createCaseDefinitionQuery()
      .latestVersion()
      .orderByTenantId()
      .asc()
      .list();

    CaseDefinitionEntity previousDefinitionTenantOne = getPreviousDefinition((CaseDefinitionEntity) latestCaseDefinitions.get(0));
    CaseDefinitionEntity previousDefinitionTenantTwo = getPreviousDefinition((CaseDefinitionEntity) latestCaseDefinitions.get(1));

    assertThat(previousDefinitionTenantOne.getVersion(), is(2));
    assertThat(previousDefinitionTenantOne.getTenantId(), is(TENANT_ONE));

    assertThat(previousDefinitionTenantTwo.getVersion(), is(1));
    assertThat(previousDefinitionTenantTwo.getTenantId(), is(TENANT_TWO));
  }

  @Test
  public void getPreviousDecisionDefinitionWithTenantId() {
    testRule.deployForTenant(TENANT_ONE, DMN);
    testRule.deployForTenant(TENANT_ONE, DMN);
    testRule.deployForTenant(TENANT_ONE, DMN);

    testRule.deployForTenant(TENANT_TWO, DMN);
    testRule.deployForTenant(TENANT_TWO, DMN);

    List<DecisionDefinition> latestDefinitions = repositoryService.createDecisionDefinitionQuery()
      .latestVersion()
      .orderByTenantId()
      .asc()
      .list();

    DecisionDefinitionEntity previousDefinitionTenantOne = getPreviousDefinition((DecisionDefinitionEntity) latestDefinitions.get(0));
    DecisionDefinitionEntity previousDefinitionTenantTwo = getPreviousDefinition((DecisionDefinitionEntity) latestDefinitions.get(1));

    assertThat(previousDefinitionTenantOne.getVersion(), is(2));
    assertThat(previousDefinitionTenantOne.getTenantId(), is(TENANT_ONE));

    assertThat(previousDefinitionTenantTwo.getVersion(), is(1));
    assertThat(previousDefinitionTenantTwo.getTenantId(), is(TENANT_TWO));
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
    repositoryService.createDeployment().addModelInstance("emptyProcessTwo", emptyProcess).tenantId(TENANT_TWO).deploy();

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
    Deployment deploymentTwo = testRule.deployForTenant(TENANT_TWO, emptyProcess);

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
    Deployment deploymentTwo = testRule.deployForTenant(TENANT_TWO, emptyProcess);

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
    Deployment deploymentTwo = testRule.deployForTenant(TENANT_TWO, emptyProcess);

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
    Deployment deploymentTwo = testRule.deployForTenant(TENANT_TWO, emptyProcess);

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
    Deployment deploymentTwo = testRule.deployForTenant(TENANT_TWO, emptyProcess);

    Resource resourceOne = repositoryService.getDeploymentResources(deploymentOne.getId()).get(0);
    Resource resourceTwo = repositoryService.getDeploymentResources(deploymentTwo.getId()).get(0);

    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    InputStream inputStream = repositoryService.getResourceAsStreamById(deploymentOne.getId(), resourceOne.getId());
    assertThat(inputStream, notNullValue());

    inputStream = repositoryService.getResourceAsStreamById(deploymentTwo.getId(), resourceTwo.getId());
    assertThat(inputStream, notNullValue());
  }

  protected <T extends ResourceDefinitionEntity> T getPreviousDefinition(final T definitionEntity) {
    return ((ProcessEngineConfigurationImpl) processEngineConfiguration).getCommandExecutorTxRequired().execute(new Command<T>() {

      @SuppressWarnings("unchecked")
      @Override
      public T execute(CommandContext commandContext) {
        return (T) definitionEntity.getPreviousDefinition();
      }
    });
  }

  protected DeploymentBuilder createDeploymentBuilder() {
    return repositoryService
        .createDeployment()
        .addModelInstance("testProcess.bpmn", emptyProcess);
  }

  @After
  public void tearDown() throws Exception {
    for(Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

}
