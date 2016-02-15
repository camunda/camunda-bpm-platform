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
import static org.junit.Assert.assertThat;

import java.util.List;

import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.impl.repository.ResourceDefinitionEntity;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class MultiTenancyRepositoryServiceTest extends PluggableProcessEngineTestCase {

  protected static final BpmnModelInstance emptyProcess = Bpmn.createExecutableProcess().done();
  protected static final String CMMN = "org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testSimpleDeployment.cmmn";
  protected static final String DMN = "org/camunda/bpm/engine/test/api/multitenancy/simpleDecisionTable.dmn";

  public void testDeploymentWithoutTenantId() {
    createDeploymentBuilder()
      .deploy();

    Deployment deployment = repositoryService
        .createDeploymentQuery()
        .singleResult();

    assertThat(deployment, is(notNullValue()));
    assertThat(deployment.getTenantId(), is(nullValue()));
  }

  public void testDeploymentWithTenantId() {
    createDeploymentBuilder()
      .tenantId("tenant1")
      .deploy();

    Deployment deployment = repositoryService
        .createDeploymentQuery()
        .singleResult();

    assertThat(deployment, is(notNullValue()));
    assertThat(deployment.getTenantId(), is("tenant1"));
  }

  public void testProcessDefinitionVersionWithTenantId() {
    createDeploymentBuilder()
      .tenantId("tenant1")
      .deploy();

    createDeploymentBuilder()
      .tenantId("tenant1")
      .deploy();

    createDeploymentBuilder()
      .tenantId("tenant2")
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

  public void testDeploymentWithDuplicateFilteringForSameTenant() {
    // given: a deployment with tenant ID
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .tenantId("tenant1")
      .deploy();

    // if the same process is deployed with the same tenant ID again
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .tenantId("tenant1")
      .deploy();

    // then it does not create a new deployment
    assertThat(repositoryService.createDeploymentQuery().count(), is(1L));
  }

  public void testDeploymentWithDuplicateFilteringForDifferentTenants() {
    // given: a deployment with tenant ID
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .tenantId("tenant1")
      .deploy();

    // if the same process is deployed with the another tenant ID
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .tenantId("tenant2")
      .deploy();

    // then a new deployment is created
    assertThat(repositoryService.createDeploymentQuery().count(), is(2L));
  }

  public void testDeploymentWithDuplicateFilteringIgnoreDeploymentForNoTenant() {
    // given: a deployment without tenant ID
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .deploy();

    // if the same process is deployed with tenant ID
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .tenantId("tenant1")
      .deploy();

    // then a new deployment is created
    assertThat(repositoryService.createDeploymentQuery().count(), is(2L));
  }

  public void testDeploymentWithDuplicateFilteringIgnoreDeploymentForTenant() {
    // given: a deployment with tenant ID
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .tenantId("tenant1")
      .deploy();

    // if the same process is deployed without tenant ID
    createDeploymentBuilder()
      .enableDuplicateFiltering(false)
      .name("twice")
      .deploy();

    // then a new deployment is created
    assertThat(repositoryService.createDeploymentQuery().count(), is(2L));
  }

  public void testGetPreviousProcessDefinitionWithTenantId() {
    deploymentForTenant("tenant1", emptyProcess);
    deploymentForTenant("tenant1", emptyProcess);
    deploymentForTenant("tenant1", emptyProcess);

    deploymentForTenant("tenant2", emptyProcess);
    deploymentForTenant("tenant2", emptyProcess);

    List<ProcessDefinition> latestProcessDefinitions = repositoryService.createProcessDefinitionQuery()
      .latestVersion()
      .orderByTenantId()
      .asc()
      .list();

    ProcessDefinitionEntity previousDefinitionTenantOne = getPreviousDefinition((ProcessDefinitionEntity) latestProcessDefinitions.get(0));
    ProcessDefinitionEntity previousDefinitionTenantTwo = getPreviousDefinition((ProcessDefinitionEntity) latestProcessDefinitions.get(1));

    assertThat(previousDefinitionTenantOne.getVersion(), is(2));
    assertThat(previousDefinitionTenantOne.getTenantId(), is("tenant1"));

    assertThat(previousDefinitionTenantTwo.getVersion(), is(1));
    assertThat(previousDefinitionTenantTwo.getTenantId(), is("tenant2"));
  }

  public void testGetPreviousCaseDefinitionWithTenantId() {
    deploymentForTenant("tenant1", CMMN);
    deploymentForTenant("tenant1", CMMN);
    deploymentForTenant("tenant1", CMMN);

    deploymentForTenant("tenant2", CMMN);
    deploymentForTenant("tenant2", CMMN);

    List<CaseDefinition> latestCaseDefinitions = repositoryService.createCaseDefinitionQuery()
      .latestVersion()
      .orderByTenantId()
      .asc()
      .list();

    CaseDefinitionEntity previousDefinitionTenantOne = getPreviousDefinition((CaseDefinitionEntity) latestCaseDefinitions.get(0));
    CaseDefinitionEntity previousDefinitionTenantTwo = getPreviousDefinition((CaseDefinitionEntity) latestCaseDefinitions.get(1));

    assertThat(previousDefinitionTenantOne.getVersion(), is(2));
    assertThat(previousDefinitionTenantOne.getTenantId(), is("tenant1"));

    assertThat(previousDefinitionTenantTwo.getVersion(), is(1));
    assertThat(previousDefinitionTenantTwo.getTenantId(), is("tenant2"));
  }

  public void testGetPreviousDecisionDefinitionWithTenantId() {
    deploymentForTenant("tenant1", DMN);
    deploymentForTenant("tenant1", DMN);
    deploymentForTenant("tenant1", DMN);

    deploymentForTenant("tenant2", DMN);
    deploymentForTenant("tenant2", DMN);

    List<DecisionDefinition> latesDefinitions = repositoryService.createDecisionDefinitionQuery()
      .latestVersion()
      .orderByTenantId()
      .asc()
      .list();

    DecisionDefinitionEntity previousDefinitionTenantOne = getPreviousDefinition((DecisionDefinitionEntity) latesDefinitions.get(0));
    DecisionDefinitionEntity previousDefinitionTenantTwo = getPreviousDefinition((DecisionDefinitionEntity) latesDefinitions.get(1));

    assertThat(previousDefinitionTenantOne.getVersion(), is(2));
    assertThat(previousDefinitionTenantOne.getTenantId(), is("tenant1"));

    assertThat(previousDefinitionTenantTwo.getVersion(), is(1));
    assertThat(previousDefinitionTenantTwo.getTenantId(), is("tenant2"));
  }

  protected <T extends ResourceDefinitionEntity> T getPreviousDefinition(final T definitionEntity) {
    return processEngineConfiguration.getCommandExecutorTxRequired().execute(new Command<T>() {

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

  @Override
  protected void tearDown() throws Exception {
    for(Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

}
