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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import org.camunda.bpm.application.ProcessApplicationExecutionException;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Roman Smirnov
 *
 */
@RunWith(Parameterized.class)
public class RedeploymentProcessApplicationTest {

  protected static final String DEPLOYMENT_NAME = "my-deployment";

  protected static final String BPMN_RESOURCE_1 = "org/camunda/bpm/engine/test/api/repository/processOne.bpmn20.xml";
  protected static final String BPMN_RESOURCE_2 = "org/camunda/bpm/engine/test/api/repository/processTwo.bpmn20.xml";

  protected static final String CMMN_RESOURCE_1 = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";
  protected static final String CMMN_RESOURCE_2 = "org/camunda/bpm/engine/test/api/cmmn/twoTaskCase.cmmn";

  protected static final String DMN_RESOURCE_1 = "org/camunda/bpm/engine/test/dmn/deployment/DecisionDefinitionDeployerTest.testDmnDeployment.dmn11.xml";
  protected static final String DMN_RESOURCE_2 = "org/camunda/bpm/engine/test/dmn/deployment/dmnScore.dmn11.xml";

  protected static final String DRD_RESOURCE_1 = "org/camunda/bpm/engine/test/dmn/deployment/drdScore.dmn11.xml";
  protected static final String DRD_RESOURCE_2 = "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml";

  @Rule
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected static RepositoryService repositoryService;
  protected static RuntimeService runtimeService;
  protected static CaseService caseService;
  protected static DecisionService decisionService;
  protected static ManagementService managementService;

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

  public boolean enforceHistoryTimeToLive;

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
    runtimeService = engineRule.getRuntimeService();
    caseService = engineRule.getCaseService();
    decisionService = engineRule.getDecisionService();
    managementService = engineRule.getManagementService();

    enforceHistoryTimeToLive = engineRule.getProcessEngineConfiguration().isEnforceHistoryTimeToLive();
  }

  @After
  public void tearDown() {
    engineRule.getProcessEngineConfiguration().setEnforceHistoryTimeToLive(enforceHistoryTimeToLive);
  }

  @Test
  public void definitionOnePreviousDeploymentWithPA() {
    // given

    MyEmbeddedProcessApplication application = new MyEmbeddedProcessApplication();

    // first deployment
    Deployment deployment1 = repositoryService
        .createDeployment(application.getReference())
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(resource1)
        .deploy();

    // second deployment
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    assertEquals(2, testProvider.countDefinitionsByKey(definitionKey1));

    // when
    testProvider.createInstanceByDefinitionKey(definitionKey1);

    // then
    assertTrue(application.isCalled());

    deleteDeployments(deployment1, deployment2);
  }

  @Test
  public void deploymentShouldFailOnNullHTTLAndEnforceHistoryTimeToLiveTrue() {
    // given
    Deployment deployment1 = null;
    try {
      MyEmbeddedProcessApplication application = new MyEmbeddedProcessApplication();
      engineRule.getProcessEngineConfiguration().setEnforceHistoryTimeToLive(true);

      // when
      // deployment does not accept null HTTL resources
      deployment1 = repositoryService
          .createDeployment(application.getReference())
          .name(DEPLOYMENT_NAME)
          .addClasspathResource(resource1)
          .deploy();

      fail("The deployment should have thrown an exception due to mandatory enforcement of historyTimeToLive");
    } catch (Exception e) {
      // then
      assertTrue("Deployment1 should fail due to mandatory historyTimeToLive", e instanceof ProcessEngineException);
    } finally {

      // cleanup
      if (deployment1 != null) {
        deleteDeployments(deployment1);
      }
    }
  }

  @Test
  public void redeploymentShouldFailOnNullHTTLAndEnforceHistoryTimeToLiveTrue() {
    // given
    Deployment deployment1 = null;
    Deployment deployment2 = null;
    try {
      MyEmbeddedProcessApplication application = new MyEmbeddedProcessApplication();
      engineRule.getProcessEngineConfiguration().setEnforceHistoryTimeToLive(false);

      // first deployment allows null HTTL
      deployment1 = repositoryService
          .createDeployment(application.getReference())
          .name(DEPLOYMENT_NAME)
          .addClasspathResource(resource1)
          .deploy();

      // enforceHistoryTimeToLive=true should prevent deployment2 from getting deployed
      engineRule.getProcessEngineConfiguration().setEnforceHistoryTimeToLive(true);

      // when - second deployment
      deployment2 = repositoryService
          .createDeployment()
          .name(DEPLOYMENT_NAME)
          .addDeploymentResources(deployment1.getId())
          .deploy();

      fail("The second deployment should have thrown an exception due to mandatory enforcement of historyTimeToLive");
    } catch (Exception e) {
      // then
      assertTrue("Deployment2 should fail due to mandatory historyTimeToLive", e instanceof ProcessEngineException);
    } finally {

      // cleanup
      if (deployment1 != null) {
        deleteDeployments(deployment1);
      }

      if (deployment2 != null) {
        deleteDeployments(deployment1, deployment2);
      }
    }
  }

  @Test
  public void definitionTwoPreviousDeploymentWithPA() {
    // given

    // first deployment
    MyEmbeddedProcessApplication application1 = new MyEmbeddedProcessApplication();
    Deployment deployment1 = repositoryService
        .createDeployment(application1.getReference())
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(resource1)
        .deploy();

    // second deployment
    MyEmbeddedProcessApplication application2 = new MyEmbeddedProcessApplication();
    Deployment deployment2 = repositoryService
        .createDeployment(application2.getReference())
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(resource1)
        .deploy();

    // second deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    assertEquals(3, testProvider.countDefinitionsByKey(definitionKey1));

    // when
    testProvider.createInstanceByDefinitionKey(definitionKey1);

    // then
    assertFalse(application1.isCalled());
    assertTrue(application2.isCalled());

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  @Test
	public void definitionTwoPreviousDeploymentFirstDeploymentWithPA() {
    // given

    // first deployment
    MyEmbeddedProcessApplication application1 = new MyEmbeddedProcessApplication();
    Deployment deployment1 = repositoryService
        .createDeployment(application1.getReference())
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(resource1)
        .deploy();

    // second deployment
    Deployment deployment2 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(resource1)
        .deploy();

    // second deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    assertEquals(3, testProvider.countDefinitionsByKey(definitionKey1));

    // when
    testProvider.createInstanceByDefinitionKey(definitionKey1);

    // then
    assertTrue(application1.isCalled());

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  @Test
	public void definitionTwoPreviousDeploymentDeleteSecondDeployment() {
    // given

    // first deployment
    MyEmbeddedProcessApplication application1 = new MyEmbeddedProcessApplication();
    Deployment deployment1 = repositoryService
        .createDeployment(application1.getReference())
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(resource1)
        .deploy();

    // second deployment
    MyEmbeddedProcessApplication application2 = new MyEmbeddedProcessApplication();
    Deployment deployment2 = repositoryService
        .createDeployment(application2.getReference())
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(resource1)
        .deploy();

    // second deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    assertEquals(3, testProvider.countDefinitionsByKey(definitionKey1));

    // when
    deleteDeployments(deployment2);
    testProvider.createInstanceByDefinitionKey(definitionKey1);

    // then
    assertTrue(application1.isCalled());
    assertFalse(application2.isCalled());

    deleteDeployments(deployment1, deployment3);
  }

  @Test
	public void definitionTwoPreviousDeploymentUnregisterSecondPA() {
    // given

    // first deployment
    MyEmbeddedProcessApplication application1 = new MyEmbeddedProcessApplication();
    Deployment deployment1 = repositoryService
        .createDeployment(application1.getReference())
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(resource1)
        .deploy();

    // second deployment
    MyEmbeddedProcessApplication application2 = new MyEmbeddedProcessApplication();
    Deployment deployment2 = repositoryService
        .createDeployment(application2.getReference())
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(resource1)
        .deploy();

    // second deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    assertEquals(3, testProvider.countDefinitionsByKey(definitionKey1));

    // when
    managementService.unregisterProcessApplication(deployment2.getId(), true);
    testProvider.createInstanceByDefinitionKey(definitionKey1);

    // then
    assertTrue(application1.isCalled());
    assertFalse(application2.isCalled());

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  @Test
	public void definitionTwoDifferentPreviousDeploymentsWithDifferentPA() {
    // given

    // first deployment
    MyEmbeddedProcessApplication application1 = new MyEmbeddedProcessApplication();
    Deployment deployment1 = repositoryService
        .createDeployment(application1.getReference())
        .name(DEPLOYMENT_NAME + "-1")
        .addClasspathResource(resource1)
        .deploy();

    // second deployment
    MyEmbeddedProcessApplication application2 = new MyEmbeddedProcessApplication();
    Deployment deployment2 = repositoryService
        .createDeployment(application2.getReference())
        .name(DEPLOYMENT_NAME + "-2")
        .addClasspathResource(resource2)
        .deploy();

    // second deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME + "-3")
        .addDeploymentResources(deployment1.getId())
        .addDeploymentResources(deployment2.getId())
        .deploy();

    assertEquals(2, testProvider.countDefinitionsByKey(definitionKey1));
    assertEquals(2, testProvider.countDefinitionsByKey(definitionKey2));

    // when (1)
    testProvider.createInstanceByDefinitionKey(definitionKey1);

    // then (1)
    assertTrue(application1.isCalled());
    assertFalse(application2.isCalled());

    // reset flag
    application1.setCalled(false);

    // when (2)
    testProvider.createInstanceByDefinitionKey(definitionKey2);

    // then (2)
    assertFalse(application1.isCalled());
    assertTrue(application2.isCalled());

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  @Test
	public void definitionTwoPreviousDeploymentsWithDifferentPA() {
    // given

    // first deployment
    MyEmbeddedProcessApplication application1 = new MyEmbeddedProcessApplication();
    Deployment deployment1 = repositoryService
      .createDeployment(application1.getReference())
      .name(DEPLOYMENT_NAME)
      .addClasspathResource(resource1)
      .addClasspathResource(resource2)
      .deploy();

    // second deployment
    MyEmbeddedProcessApplication application2 = new MyEmbeddedProcessApplication();
    Deployment deployment2 = repositoryService
        .createDeployment(application2.getReference())
        .name(DEPLOYMENT_NAME)
        .addClasspathResource(resource1)
        .deploy();

    // third deployment
    Deployment deployment3 = repositoryService
        .createDeployment()
        .name(DEPLOYMENT_NAME)
        .addDeploymentResources(deployment1.getId())
        .deploy();

    assertEquals(3, testProvider.countDefinitionsByKey(definitionKey1));
    assertEquals(2, testProvider.countDefinitionsByKey(definitionKey2));

    // when (1)
    testProvider.createInstanceByDefinitionKey(definitionKey1);

    // then (1)
    assertFalse(application1.isCalled());
    assertTrue(application2.isCalled());

    // reset flag
    application2.setCalled(false);

    // when (2)
    testProvider.createInstanceByDefinitionKey(definitionKey2);

    // then (2)
    assertTrue(application1.isCalled());
    assertFalse(application2.isCalled());

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  protected void deleteDeployments(Deployment... deployments){
    for (Deployment deployment : deployments) {
      repositoryService.deleteDeployment(deployment.getId(), true);
      managementService.unregisterProcessApplication(deployment.getId(), false);
    }
  }

  protected interface TestProvider {
    long countDefinitionsByKey(String definitionKey);

    void createInstanceByDefinitionKey(String definitionKey);
  }

  protected static TestProvider processDefinitionTestProvider() {
    return new TestProvider() {

      public long countDefinitionsByKey(String definitionKey) {
        return repositoryService.createProcessDefinitionQuery().processDefinitionKey(definitionKey).count();
      }

      public void createInstanceByDefinitionKey(String definitionKey) {
        runtimeService.startProcessInstanceByKey(definitionKey, Variables.createVariables()
            .putValue("a", 1).putValue("b", 1));
      }

    };
  }

  protected static TestProvider caseDefinitionTestProvider() {
    return new TestProvider() {

      public long countDefinitionsByKey(String definitionKey) {
        return repositoryService.createCaseDefinitionQuery().caseDefinitionKey(definitionKey).count();
      }

      public void createInstanceByDefinitionKey(String definitionKey) {
        caseService.createCaseInstanceByKey(definitionKey);
      }

    };
  }

  protected static TestProvider decisionDefinitionTestProvider() {
    return new TestProvider() {

      public long countDefinitionsByKey(String definitionKey) {
        return repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey(definitionKey).count();
      }

      public void createInstanceByDefinitionKey(String definitionKey) {
        decisionService.evaluateDecisionTableByKey(definitionKey)
          .variables(Variables.createVariables().putValue("input", "john"))
          .evaluate();
      }

    };
  }

  protected static TestProvider decisionRequirementsDefinitionTestProvider() {
    return new TestProvider() {

      public long countDefinitionsByKey(String definitionKey) {
        return repositoryService.createDecisionRequirementsDefinitionQuery().decisionRequirementsDefinitionKey(definitionKey).count();
      }

      public void createInstanceByDefinitionKey(String definitionKey) {
        decisionService.evaluateDecisionTableByKey(definitionKey + "-decision")
          .variables(Variables.createVariables()
              .putValue("temperature", 21)
              .putValue("dayType", "Weekend")
              .putValue("input", "John"))
          .evaluate();
      }

    };
  }

  public class MyEmbeddedProcessApplication extends EmbeddedProcessApplication {

    protected ProcessApplicationReference reference;
    protected boolean called;

    @Override
    public ProcessApplicationReference getReference() {
      if (reference == null) {
        reference = super.getReference();
      }
      return reference;
    }

    @Override
    public <T> T execute(Callable<T> callable) throws ProcessApplicationExecutionException {
      called = true;
      return super.execute(callable);
    }

    public boolean isCalled() {
      return called;
    }

    public void setCalled(boolean called) {
      this.called = called;
    }

  }

}
