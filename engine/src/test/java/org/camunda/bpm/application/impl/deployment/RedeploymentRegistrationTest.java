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
package org.camunda.bpm.application.impl.deployment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;

import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.application.ProcessApplicationManager;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.context.ProcessApplicationContextUtil;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionDefinitionEntity;
import org.camunda.bpm.engine.impl.dmn.entity.repository.DecisionRequirementsDefinitionEntity;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class RedeploymentRegistrationTest {

  protected static final String DEPLOYMENT_NAME = "my-deployment";

  protected static final String BPMN_RESOURCE_1 = "org/camunda/bpm/engine/test/api/repository/processOne.bpmn20.xml";
  protected static final String BPMN_RESOURCE_2 = "org/camunda/bpm/engine/test/api/repository/processTwo.bpmn20.xml";

  protected static final String CMMN_RESOURCE_1 = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";
  protected static final String CMMN_RESOURCE_2 = "org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn";

  protected static final String DMN_RESOURCE_1 = "org/camunda/bpm/engine/test/dmn/deployment/DecisionDefinitionDeployerTest.testDmnDeployment.dmn11.xml";
  protected static final String DMN_RESOURCE_2 = "org/camunda/bpm/engine/test/dmn/deployment/dmnScore.dmn11.xml";

  protected static final String DRD_RESOURCE_1 = "org/camunda/bpm/engine/test/dmn/deployment/drdScore.dmn11.xml";
  protected static final String DRD_RESOURCE_2 = "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml";

  protected EmbeddedProcessApplication processApplication;

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RepositoryService repositoryService;
  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  @Parameter(0)
  public String resource1;

  @Parameter(1)
  public String resource2;

  @Parameter(2)
  public String definitionKey1;

  @Parameter(3)
  public String definitionKey2;

  @Parameter(4)
  public TestProvider testProvider;

  @Parameters(name = "scenario {index}")
  public static Collection<Object[]> scenarios() {
    return Arrays.asList(new Object[][] {
      { BPMN_RESOURCE_1, BPMN_RESOURCE_2, "processOne", "processTwo", processDefinitionTestProvider() },
      { CMMN_RESOURCE_1, CMMN_RESOURCE_2, "oneTaskCase", "twoTaskCase", caseDefinitionTestProvider() },
      { DMN_RESOURCE_1, DMN_RESOURCE_2, "decision", "score-decision", decisionDefinitionTestProvider() },
      { DRD_RESOURCE_1, DRD_RESOURCE_2, "score", "dish", decisionRequirementsDefinitionTestProvider() }
    });
  }

  @Before
  public void init() throws Exception {
    repositoryService = engineRule.getRepositoryService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();

    processApplication = new EmbeddedProcessApplication();
  }

  @Test
	public void registrationNotFoundByDeploymentId() {
    // given
    ProcessApplicationReference reference = processApplication.getReference();

    Deployment deployment1 = repositoryService
      .createDeployment(reference)
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(resource1)
      .deploy();

    assertEquals(reference, getProcessApplicationForDeployment(deployment1.getId()));

    // when
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    // then
    assertNull(getProcessApplicationForDeployment(deployment2.getId()));
  }

  @Test
	public void registrationNotFoundByDefinition() {
    // given

    // first deployment
    Deployment deployment1 = repositoryService
      .createDeployment()
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(resource1)
      .deploy();

    // second deployment
    repositoryService
      .createDeployment()
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(resource1)
      .deploy();

    // when
    repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String definitionId = getLatestDefinitionIdByKey(definitionKey1);

    // then
    assertNull(getProcessApplicationForDefinition(definitionId));
  }

  @Test
	public void registrationFoundByDeploymentId() {
    // given
    ProcessApplicationReference reference1 = processApplication.getReference();

    Deployment deployment1 = repositoryService
      .createDeployment(reference1)
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(resource1)
      .deploy();

    assertEquals(reference1, getProcessApplicationForDeployment(deployment1.getId()));

    // when
    ProcessApplicationReference reference2 = processApplication.getReference();

    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    // then
    assertEquals(reference2, getProcessApplicationForDeployment(deployment2.getId()));
  }

  @Test
	public void registrationFoundFromPreviousDefinition() {
    // given
    ProcessApplicationReference reference = processApplication.getReference();
    Deployment deployment1 = repositoryService
      .createDeployment(reference)
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(resource1)
      .deploy();

    // when
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String definitionId = getLatestDefinitionIdByKey(definitionKey1);

    // then
    assertEquals(reference, getProcessApplicationForDefinition(definitionId));

    // and the reference is not cached
    assertNull(getProcessApplicationForDeployment(deployment2.getId()));
  }

  @Test
	public void registrationFoundFromLatestDeployment() {
    // given
    ProcessApplicationReference reference1 = processApplication.getReference();
    Deployment deployment1 = repositoryService
      .createDeployment(reference1)
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(resource1)
      .deploy();

    // when
    ProcessApplicationReference reference2 = processApplication.getReference();
    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String definitionId = getLatestDefinitionIdByKey(definitionKey1);

    // then
    assertEquals(reference2, getProcessApplicationForDefinition(definitionId));
    assertEquals(reference2, getProcessApplicationForDeployment(deployment2.getId()));
  }

  @Test
	public void registrationFoundOnlyForOneProcessDefinition() {
    // given

    // first deployment
    Deployment deployment1 = repositoryService
      .createDeployment()
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(resource1)
      .addClasspathResource(resource2)
      .deploy();

    // second deployment
    ProcessApplicationReference reference2 = processApplication.getReference();
    repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(resource1)
        .deploy();

    // when
    repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = getLatestDefinitionIdByKey(definitionKey1);
    String secondDefinitionId = getLatestDefinitionIdByKey(definitionKey2);

    // then
    assertEquals(reference2, getProcessApplicationForDefinition(firstDefinitionId));
    assertNull(getProcessApplicationForDefinition(secondDefinitionId));
  }

  @Test
	public void registrationFoundFromDifferentDeployment() {
    // given

    // first deployment
    ProcessApplicationReference reference1 = processApplication.getReference();
    Deployment deployment1 = repositoryService
      .createDeployment(reference1)
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(resource1)
      .addClasspathResource(resource2)
      .deploy();

    // second deployment
    ProcessApplicationReference reference2 = processApplication.getReference();
    repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(resource1)
        .deploy();

    // when
    repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = getLatestDefinitionIdByKey(definitionKey1);
    String secondDefinitionId = getLatestDefinitionIdByKey(definitionKey2);

    // then
    assertEquals(reference2, getProcessApplicationForDefinition(firstDefinitionId));
    assertEquals(reference1, getProcessApplicationForDefinition(secondDefinitionId));
  }

  @Test
	public void registrationFoundFromSameDeployment() {
    // given

    // first deployment
    ProcessApplicationReference reference1 = processApplication.getReference();
    Deployment deployment1 = repositoryService
      .createDeployment(reference1)
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(resource1)
      .addClasspathResource(resource2)
      .deploy();

    // second deployment
    repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(resource1)
        .deploy();

    repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(resource2)
        .deploy();

    // when
    repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = getLatestDefinitionIdByKey(definitionKey1);
    String secondDefinitionId = getLatestDefinitionIdByKey(definitionKey1);

    // then
    assertEquals(reference1, getProcessApplicationForDefinition(firstDefinitionId));
    assertEquals(reference1, getProcessApplicationForDefinition(secondDefinitionId));
  }

  @Test
	public void registrationFoundFromDifferentDeployments() {
    // given

    // first deployment
    ProcessApplicationReference reference1 = processApplication.getReference();
    Deployment deployment1 = repositoryService
      .createDeployment(reference1)
      .name(DEPLOYMENT_NAME + "-1")
      .addClasspathResource(resource1)
      .deploy();

    // second deployment
    ProcessApplicationReference reference2 = processApplication.getReference();
    repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME + "-2")
        .addClasspathResource(resource2)
        .deploy();

    // when
    repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = getLatestDefinitionIdByKey(definitionKey1);
    String secondDefinitionId = getLatestDefinitionIdByKey(definitionKey2);

    // then
    assertEquals(reference1, getProcessApplicationForDefinition(firstDefinitionId));
    assertEquals(reference2, getProcessApplicationForDefinition(secondDefinitionId));
  }

  @Test
	public void registrationNotFoundWhenDeletingDeployment() {
    // given

    // first deployment
    Deployment deployment1 = repositoryService
      .createDeployment()
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(resource1)
      .deploy();

    // second deployment
    ProcessApplicationReference reference2 = processApplication.getReference();
    Deployment deployment2 = repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    // when (1)
    // third deployment
    repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = getLatestDefinitionIdByKey(definitionKey1);

    // then (1)
    assertEquals(reference2, getProcessApplicationForDefinition(firstDefinitionId));

    // when (2)
    deleteDeployment(deployment2);

    // then (2)
    assertNull(getProcessApplicationForDefinition(firstDefinitionId));
  }

  @Test
	public void registrationFoundAfterDiscardingDeploymentCache() {
    // given

    // first deployment
    Deployment deployment1 = repositoryService
      .createDeployment()
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(resource1)
      .deploy();

    // second deployment
    ProcessApplicationReference reference2 = processApplication.getReference();
    repositoryService
        .createDeployment(reference2)
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    // when (1)
    // third deployment
    repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    String firstDefinitionId = getLatestDefinitionIdByKey(definitionKey1);

    // then (1)
    assertEquals(reference2, getProcessApplicationForDefinition(firstDefinitionId));

    // when (2)
    discardDefinitionCache();

    // then (2)
    assertEquals(reference2, getProcessApplicationForDefinition(firstDefinitionId));
  }

  // helper ///////////////////////////////////////////

  @After
  public void cleanUp() {
    for (Deployment deployment : repositoryService.createDeploymentQuery().list()) {
      deleteDeployment(deployment);
    }
  }

  protected void deleteDeployment(Deployment deployment) {
    repositoryService.deleteDeployment(deployment.getId(), true);
    engineRule.getManagementService().unregisterProcessApplication(deployment.getId(), false);
  }

  protected ProcessApplicationReference getProcessApplicationForDeployment(String deploymentId) {
    ProcessApplicationManager processApplicationManager = processEngineConfiguration.getProcessApplicationManager();
    return processApplicationManager.getProcessApplicationForDeployment(deploymentId);
  }

  protected void discardDefinitionCache() {
    processEngineConfiguration.getDeploymentCache().discardProcessDefinitionCache();
    processEngineConfiguration.getDeploymentCache().discardCaseDefinitionCache();
    processEngineConfiguration.getDeploymentCache().discardDecisionDefinitionCache();
    processEngineConfiguration.getDeploymentCache().discardDecisionRequirementsDefinitionCache();
  }

  protected String getLatestDefinitionIdByKey(String key) {
    return testProvider.getLatestDefinitionIdByKey(repositoryService, key);
  }

  protected ProcessApplicationReference getProcessApplicationForDefinition(String definitionId) {
    return processEngineConfiguration.getCommandExecutorTxRequired().execute(
        testProvider.createGetProcessApplicationCommand(definitionId));
  }

  private interface TestProvider {
    Command<ProcessApplicationReference> createGetProcessApplicationCommand(String definitionId);

    String getLatestDefinitionIdByKey(RepositoryService repositoryService, String key);
  }

  protected static TestProvider processDefinitionTestProvider() {
    return new TestProvider() {

      @Override
      public Command<ProcessApplicationReference> createGetProcessApplicationCommand(final String definitionId) {
        return new Command<ProcessApplicationReference>() {

          public ProcessApplicationReference execute(CommandContext commandContext) {
            ProcessEngineConfigurationImpl configuration = commandContext.getProcessEngineConfiguration();
            DeploymentCache deploymentCache = configuration.getDeploymentCache();
            ProcessDefinitionEntity definition = deploymentCache.findDeployedProcessDefinitionById(definitionId);
            return ProcessApplicationContextUtil.getTargetProcessApplication(definition);
          }
        };
      }

      @Override
      public String getLatestDefinitionIdByKey(RepositoryService repositoryService, String key) {
        return repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).latestVersion().singleResult().getId();
      }

    };
  }

  protected static TestProvider caseDefinitionTestProvider() {
    return new TestProvider() {

      @Override
      public Command<ProcessApplicationReference> createGetProcessApplicationCommand(final String definitionId) {
        return new Command<ProcessApplicationReference>() {

          public ProcessApplicationReference execute(CommandContext commandContext) {
            ProcessEngineConfigurationImpl configuration = commandContext.getProcessEngineConfiguration();
            DeploymentCache deploymentCache = configuration.getDeploymentCache();
            CaseDefinitionEntity definition = deploymentCache.findDeployedCaseDefinitionById(definitionId);
            return ProcessApplicationContextUtil.getTargetProcessApplication(definition);
          }
        };
      }

      @Override
      public String getLatestDefinitionIdByKey(RepositoryService repositoryService, String key) {
        return repositoryService.createCaseDefinitionQuery().caseDefinitionKey(key).latestVersion().singleResult().getId();
      }

    };
  }

  protected static TestProvider decisionDefinitionTestProvider() {
    return new TestProvider() {

      @Override
      public Command<ProcessApplicationReference> createGetProcessApplicationCommand(final String definitionId) {
        return new Command<ProcessApplicationReference>() {

          public ProcessApplicationReference execute(CommandContext commandContext) {
            ProcessEngineConfigurationImpl configuration = commandContext.getProcessEngineConfiguration();
            DeploymentCache deploymentCache = configuration.getDeploymentCache();
            DecisionDefinitionEntity definition = deploymentCache.findDeployedDecisionDefinitionById(definitionId);
            return ProcessApplicationContextUtil.getTargetProcessApplication(definition);
          }
        };
      }

      @Override
      public String getLatestDefinitionIdByKey(RepositoryService repositoryService, String key) {
        return repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey(key).latestVersion().singleResult().getId();
      }

    };
  }

  protected static TestProvider decisionRequirementsDefinitionTestProvider() {
    return new TestProvider() {

      @Override
      public Command<ProcessApplicationReference> createGetProcessApplicationCommand(final String definitionId) {
        return new Command<ProcessApplicationReference>() {

          public ProcessApplicationReference execute(CommandContext commandContext) {
            ProcessEngineConfigurationImpl configuration = commandContext.getProcessEngineConfiguration();
            DeploymentCache deploymentCache = configuration.getDeploymentCache();
            DecisionRequirementsDefinitionEntity definition = deploymentCache.findDeployedDecisionRequirementsDefinitionById(definitionId);
            return ProcessApplicationContextUtil.getTargetProcessApplication(definition);
          }
        };
      }

      @Override
      public String getLatestDefinitionIdByKey(RepositoryService repositoryService, String key) {
        return repositoryService.createDecisionRequirementsDefinitionQuery().decisionRequirementsDefinitionKey(key).latestVersion().singleResult().getId();
      }

    };
  }

}
