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
package org.camunda.bpm.engine.test.api.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.application.ProcessApplicationManager;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.persistence.deploy.cache.DeploymentCache;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentHandlerFactory;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.repository.ResumePreviousBy;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.bpmn.deployment.VersionedDeploymentHandlerFactory;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationDeploymentTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RepositoryService repositoryService;
  protected ManagementService managementService;
  protected ProcessEngine processEngine;

  private EmbeddedProcessApplication processApplication;
  protected DeploymentHandlerFactory defaultDeploymentHandlerFactory;
  protected DeploymentHandlerFactory customDeploymentHandlerFactory;

  protected ProcessApplicationManager processApplicationManager;
  protected DeploymentCache deploymentCache;
  Set<String> registeredDeployments;

  @Before
  public void setUp() throws Exception {
    processEngine = engineRule.getProcessEngine();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    repositoryService = engineRule.getRepositoryService();
    managementService = engineRule.getManagementService();

    defaultDeploymentHandlerFactory = processEngineConfiguration.getDeploymentHandlerFactory();
    customDeploymentHandlerFactory = new VersionedDeploymentHandlerFactory();
    processApplication = new EmbeddedProcessApplication();

    processApplicationManager = processEngineConfiguration.getProcessApplicationManager();
    deploymentCache = processEngineConfiguration.getDeploymentCache();
    registeredDeployments = processEngineConfiguration.getRegisteredDeployments();
  }

  @After
  public void tearDown() throws Exception {
    clearProcessApplicationDeployments();
    processApplication.undeploy();
    processEngineConfiguration.setDeploymentHandlerFactory(defaultDeploymentHandlerFactory);
    ClockUtil.reset();
  }

  @Test
  public void testEmptyDeployment() {
    try {
      repositoryService
        .createDeployment(processApplication.getReference())
        .deploy();
      fail("it should not be possible to deploy without deployment resources");
    } catch (NotValidException e) {
      // expected
    }

    try {
      repositoryService
        .createDeployment()
        .deploy();
      fail("it should not be possible to deploy without deployment resources");
    } catch (NotValidException e) {
      // expected
    }
  }

  @Test
  public void testSimpleProcessApplicationDeployment() {
    // given
    ProcessApplicationDeployment deployment = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml"));

    // process is deployed:
    assertThatOneProcessIsDeployed();

    // when registration was performed:
    ProcessApplicationRegistration registration = deployment.getProcessApplicationRegistration();

    // then
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(1, deploymentIds.size());
    assertEquals(processEngine.getName(), registration.getProcessEngineName());
  }

  @Test
  public void testProcessApplicationDeploymentNoChanges() {
    // given: create initial deployment
    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml"));

    assertThatOneProcessIsDeployed();

    // when
    // deploy update with no changes:
    ProcessApplicationDeployment deployment = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .enableDuplicateFiltering(false)
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml"));

    // no changes
    assertThatOneProcessIsDeployed();
    ProcessApplicationRegistration registration = deployment.getProcessApplicationRegistration();

    // then
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(1, deploymentIds.size());
    assertEquals(processEngine.getName(), registration.getProcessEngineName());
  }

  @Test
  public void testPartialChangesDeployAll() {
    // given
    BpmnModelInstance model1 = createEmptyModel("process1");
    BpmnModelInstance model2 = createEmptyModel("process2");

    // create initial deployment
    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .addModelInstance("process1.bpmn20.xml", model1)
        .addModelInstance("process2.bpmn20.xml", model2));

    BpmnModelInstance changedModel2 = Bpmn.createExecutableProcess("process2")
        .startEvent()
        .done();

    // when
    // second deployment with partial changes:
    ProcessApplicationDeployment deployment2 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .enableDuplicateFiltering(false)
        .resumePreviousVersions()
        .addModelInstance("process1.bpmn20.xml", model1)
        .addModelInstance("process2.bpmn20.xml", changedModel2));

    // then
    assertEquals(4, repositoryService.createProcessDefinitionQuery().count());

    List<ProcessDefinition> processDefinitionsModel1 =
      repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("process1")
        .orderByProcessDefinitionVersion().asc().list();

    // now there are two versions of process1 deployed
    assertEquals(2, processDefinitionsModel1.size());
    assertEquals(1, processDefinitionsModel1.get(0).getVersion());
    assertEquals(2, processDefinitionsModel1.get(1).getVersion());

    // now there are two versions of process2 deployed
    List<ProcessDefinition> processDefinitionsModel2 =
        repositoryService
          .createProcessDefinitionQuery()
          .processDefinitionKey("process1")
          .orderByProcessDefinitionVersion().asc().list();

    assertEquals(2, processDefinitionsModel2.size());
    assertEquals(1, processDefinitionsModel2.get(0).getVersion());
    assertEquals(2, processDefinitionsModel2.get(1).getVersion());

    // old deployment was resumed
    ProcessApplicationRegistration registration = deployment2.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(2, deploymentIds.size());
    assertEquals(processEngine.getName(), registration.getProcessEngineName());
  }

  /**
   * Test re-deployment of only those resources that have actually changed
   */
  @Test
  public void testPartialChangesDeployChangedOnly() {
    BpmnModelInstance model1 = createEmptyModel("process1");
    BpmnModelInstance model2 = createEmptyModel("process2");

    // create initial deployment
    ProcessApplicationDeployment deployment1 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .addModelInstance("process1.bpmn20.xml", model1)
        .addModelInstance("process2.bpmn20.xml", model2));

    BpmnModelInstance changedModel2 = Bpmn.createExecutableProcess("process2")
        .startEvent()
        .done();

    // second deployment with partial changes:
    ProcessApplicationDeployment deployment2 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .enableDuplicateFiltering(true)
        .resumePreviousVersions()
        .addModelInstance("process1.bpmn20.xml", model1)
        .addModelInstance("process2.bpmn20.xml", changedModel2));

    assertEquals(3, repositoryService.createProcessDefinitionQuery().count());

    // there is one version of process1 deployed
    ProcessDefinition processDefinitionModel1 =
        repositoryService
          .createProcessDefinitionQuery()
          .processDefinitionKey("process1")
          .singleResult();

    assertNotNull(processDefinitionModel1);
    assertEquals(1, processDefinitionModel1.getVersion());
    assertEquals(deployment1.getId(), processDefinitionModel1.getDeploymentId());

    // there are two versions of process2 deployed
    List<ProcessDefinition> processDefinitionsModel2 =
        repositoryService
          .createProcessDefinitionQuery()
          .processDefinitionKey("process2")
          .orderByProcessDefinitionVersion().asc().list();

    assertEquals(2, processDefinitionsModel2.size());
    assertEquals(1, processDefinitionsModel2.get(0).getVersion());
    assertEquals(2, processDefinitionsModel2.get(1).getVersion());

    // old deployment was resumed
    ProcessApplicationRegistration registration = deployment2.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(2, deploymentIds.size());

    BpmnModelInstance anotherChangedModel2 = Bpmn.createExecutableProcess("process2")
        .startEvent()
        .endEvent()
        .done();

    // testing with a third deployment to ensure the change check is not only performed against
    // the last version of the deployment
    ProcessApplicationDeployment deployment3 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .enableDuplicateFiltering(true)
        .resumePreviousVersions()
        .addModelInstance("process1.bpmn20.xml", model1)
        .addModelInstance("process2.bpmn20.xml", anotherChangedModel2)
        .name("deployment"));

    // there should still be one version of process 1
    assertEquals(1, repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("process1")
        .count());

    // there should be three versions of process 2
    assertEquals(3, repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("process2")
        .count());

    // old deployments are resumed
    registration = deployment3.getProcessApplicationRegistration();
    deploymentIds = registration.getDeploymentIds();
    assertEquals(3, deploymentIds.size());
  }

  @Test
  public void testDuplicateFilteringDefaultBehavior() {
    // given
    BpmnModelInstance oldModel = Bpmn.createExecutableProcess("versionedProcess")
      .camundaVersionTag("3").done();
    BpmnModelInstance newModel = Bpmn.createExecutableProcess("versionedProcess")
      .camundaVersionTag("1").done();

    testRule.deploy(repositoryService.createDeployment(processApplication.getReference())
      .enableDuplicateFiltering(true)
      .addModelInstance("model", oldModel)
      .name("defaultDeploymentHandling"));

    // when
    testRule.deploy(repositoryService.createDeployment(processApplication.getReference())
      .enableDuplicateFiltering(true)
      .addModelInstance("model", newModel)
      .name("defaultDeploymentHandling"));

    // then
    long deploymentCount = repositoryService.createDeploymentQuery().count();
    assertEquals(2, deploymentCount);
  }

  @Test
  public void testDuplicateFilteringCustomBehavior() {
    // given
    processEngineConfiguration.setDeploymentHandlerFactory( customDeploymentHandlerFactory);
    BpmnModelInstance oldModel = Bpmn.createExecutableProcess("versionedProcess")
      .camundaVersionTag("1").startEvent().done();
    BpmnModelInstance newModel = Bpmn.createExecutableProcess("versionedProcess")
      .camundaVersionTag("3").startEvent().done();

    Deployment deployment1 = testRule.deploy(
        repositoryService
            .createDeployment(processApplication.getReference())
            .enableDuplicateFiltering(true)
            .addModelInstance("model.bpmn", oldModel)
            .name("customDeploymentHandling"));

    // when
    testRule.deploy(repositoryService.createDeployment(processApplication.getReference())
      .enableDuplicateFiltering(true)
      .addModelInstance("model.bpmn", newModel)
      .name("customDeploymentHandling"));

    Deployment deployment3 = testRule.deploy(
        repositoryService
            .createDeployment(processApplication.getReference())
            .enableDuplicateFiltering(true)
            .addModelInstance("model.bpmn", oldModel)
            .name("customDeploymentHandling"));

    // then
    long deploymentCount = repositoryService.createDeploymentQuery().count();
    assertEquals(2, deploymentCount);
    assertEquals(deployment1.getId(), deployment3.getId());
  }

  @Test
  public void testPartialChangesResumePreviousVersion() {
    BpmnModelInstance model1 = createEmptyModel("process1");
    BpmnModelInstance model2 = createEmptyModel("process2");

    // create initial deployment
    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .addModelInstance("process1.bpmn20.xml", model1));

    ProcessApplicationDeployment deployment2 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .enableDuplicateFiltering(true)
        .resumePreviousVersions()
        .addModelInstance("process1.bpmn20.xml", model1)
        .addModelInstance("process2.bpmn20.xml", model2));

    ProcessApplicationRegistration registration = deployment2.getProcessApplicationRegistration();
    assertEquals(2, registration.getDeploymentIds().size());
  }

  @Test
  public void testProcessApplicationDeploymentResumePreviousVersions() {
    // create initial deployment
    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml"));

    assertThatOneProcessIsDeployed();

    // deploy update with changes:
    ProcessApplicationDeployment deployment2 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .enableDuplicateFiltering(false)
        .resumePreviousVersions()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version2.bpmn20.xml"));

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
        .orderByProcessDefinitionVersion()
        .asc()
        .list();
    // now there are 2 process definitions deployed
    assertEquals(1, processDefinitions.get(0).getVersion());
    assertEquals(2, processDefinitions.get(1).getVersion());

    // old deployment was resumed
    ProcessApplicationRegistration registration = deployment2.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(2, deploymentIds.size());
    assertEquals(processEngine.getName(), registration.getProcessEngineName());
  }

  @Test
  public void testProcessApplicationDeploymentResumePreviousVersionsDifferentKeys() {
    // create initial deployment
    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml"));

    assertThatOneProcessIsDeployed();

    // deploy update with changes:
    ProcessApplicationDeployment deployment2 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .resumePreviousVersions()
        .addClasspathResource("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml"));

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
        .orderByProcessDefinitionVersion()
        .asc()
        .list();
    // now there are 2 process definitions deployed
    assertEquals(1, processDefinitions.get(0).getVersion());
    assertEquals(1, processDefinitions.get(1).getVersion());

    // and the old deployment was not resumed
    ProcessApplicationRegistration registration = deployment2.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(1, deploymentIds.size());
    assertEquals(deployment2.getId(), deploymentIds.iterator().next());
    assertEquals(processEngine.getName(), registration.getProcessEngineName());
  }

  @Test
  public void testProcessApplicationDeploymentResumePreviousVersionsDefaultBehavior() {
    // given
    BpmnModelInstance model1 = createEmptyModel("process1");
    BpmnModelInstance model2 = createEmptyModel("process2");

    // create initial deployment
    testRule.deploy(repositoryService.createDeployment(processApplication.getReference())
      .name("defaultDeploymentHandling")
      .addModelInstance("process1.bpmn20.xml", model1));

    // when
    ProcessApplicationDeployment deployment2 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("defaultDeploymentHandling")
        .enableDuplicateFiltering(true)
        .resumePreviousVersions()
        .addModelInstance("process1.bpmn20.xml", model1)
        .addModelInstance("process2.bpmn20.xml", model2));

    // then
    ProcessApplicationRegistration registration = deployment2.getProcessApplicationRegistration();
    assertEquals(2, registration.getDeploymentIds().size());
  }

  @Test
  public void testProcessApplicationDeploymentResumePreviousVersionsCustomBehavior() {
    // given
    processEngineConfiguration.setDeploymentHandlerFactory(customDeploymentHandlerFactory);
    BpmnModelInstance oldModel = Bpmn.createExecutableProcess("process")
        .camundaVersionTag("1")
        .startEvent()
        .done();
    BpmnModelInstance newModel = Bpmn.createExecutableProcess("process")
        .camundaVersionTag("3")
        .startEvent()
        .done();

    // create initial deployment
    ProcessApplicationDeployment deployment1 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("customDeploymentHandling")
        .addModelInstance("process1.bpmn20.xml", oldModel));

    // when
    ProcessApplicationDeployment deployment2 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("customDeploymentHandling")
        .enableDuplicateFiltering(true)
        .resumePreviousVersions()
        .addModelInstance("process1.bpmn20.xml", newModel));

    ProcessApplicationDeployment deployment3 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("customDeploymentHandling")
        .enableDuplicateFiltering(true)
        .resumePreviousVersions()
        .addModelInstance("process1.bpmn20.xml", oldModel));

    // then
    // PA2 registers only it's own (new) version of the model
    ProcessApplicationRegistration registration2 = deployment2.getProcessApplicationRegistration();
    assertEquals(1, registration2.getDeploymentIds().size());

    // PA3 deploys a duplicate version of the process. The duplicate deployment needs to be found
    // and registered (deployment1)
    ProcessApplicationRegistration registration3 = deployment3.getProcessApplicationRegistration();
    assertEquals(1, registration3.getDeploymentIds().size());
    assertEquals(deployment1.getId(), registration3.getDeploymentIds().iterator().next());
  }

  @Test
  public void testProcessApplicationDeploymentNoResume() {
    // create initial deployment
    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml"));

    assertThatOneProcessIsDeployed();

    // deploy update with changes:
    ProcessApplicationDeployment deployment2 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .enableDuplicateFiltering(false)
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version2.bpmn20.xml"));

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
        .orderByProcessDefinitionVersion()
        .asc()
        .list();
    // now there are 2 process definitions deployed
    assertEquals(1, processDefinitions.get(0).getVersion());
    assertEquals(2, processDefinitions.get(1).getVersion());

    // old deployment was NOT resumed
    ProcessApplicationRegistration registration = deployment2.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(1, deploymentIds.size());
    assertEquals(processEngine.getName(), registration.getProcessEngineName());
  }

  @Test
  public void testProcessApplicationDeploymentResumePreviousVersionsByDeploymentNameDefaultBehavior() {
    // create initial deployment
    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml"));

    assertThatOneProcessIsDeployed();

    // deploy update with changes:
    ProcessApplicationDeployment deployment2 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .enableDuplicateFiltering(false)
        .resumePreviousVersions()
        .resumePreviousVersionsBy(ResumePreviousBy.RESUME_BY_DEPLOYMENT_NAME)
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version2.bpmn20.xml"));

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
        .orderByProcessDefinitionVersion()
        .asc()
        .list();
    // now there are 2 process definitions deployed
    assertEquals(1, processDefinitions.get(0).getVersion());
    assertEquals(2, processDefinitions.get(1).getVersion());

    // old deployment was resumed
    ProcessApplicationRegistration registration = deployment2.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(2, deploymentIds.size());
    assertEquals(processEngine.getName(), registration.getProcessEngineName());
  }

  @Test
  public void testProcessApplicationDeploymentResumePreviousVersionsByDeploymentNameCustomBehavior() {
    // given
    BpmnModelInstance oldProcess =
        Bpmn.createExecutableProcess("process").camundaVersionTag("1").startEvent().done();
    BpmnModelInstance newProcess =
        Bpmn.createExecutableProcess("process").camundaVersionTag("2").startEvent().done();

    // set custom deployment handler
    processEngineConfiguration.setDeploymentHandlerFactory(customDeploymentHandlerFactory);

    // initial deployment is created
    testRule.deploy(
        repositoryService
            .createDeployment(processApplication.getReference())
            .name("deployment")
            .addModelInstance("version1.bpmn20.xml", oldProcess));

    // when
    // update with changes is deployed
    ProcessApplicationDeployment deployment2 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .enableDuplicateFiltering(false)
        .resumePreviousVersions()
        .resumePreviousVersionsBy(ResumePreviousBy.RESUME_BY_DEPLOYMENT_NAME)
        .addModelInstance("version2.bpmn20.xml", newProcess));

    // then
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
        .orderByProcessDefinitionVersion()
        .asc()
        .list();
    // now there are 2 process definitions deployed
    assertEquals(1, processDefinitions.get(0).getVersion());
    assertEquals(2, processDefinitions.get(1).getVersion());

    // old deployment was resumed
    ProcessApplicationRegistration registration = deployment2.getProcessApplicationRegistration();
    Set<String> paDeploymentIds = registration.getDeploymentIds();
    assertEquals(1, paDeploymentIds.size());
    assertTrue(paDeploymentIds.contains(deployment2.getId()));
    assertEquals(processEngine.getName(), registration.getProcessEngineName());
  }

  @Test
  public void testProcessApplicationDeploymentResumePreviousVersionsByDeploymentNameDeployDifferentProcesses(){
    BpmnModelInstance process1 = createEmptyModel("process1");
    BpmnModelInstance process2 = createEmptyModel("process2");
    testRule.deploy(repositoryService
            .createDeployment(processApplication.getReference())
            .name("deployment")
            .addModelInstance("process1.bpmn", process1));

    assertThatOneProcessIsDeployed();

    ProcessApplicationDeployment deployment2 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .resumePreviousVersions()
        .resumePreviousVersionsBy(ResumePreviousBy.RESUME_BY_DEPLOYMENT_NAME)
        .addModelInstance("process2.bpmn", process2));

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
        .orderByProcessDefinitionVersion()
        .asc()
        .list();
    // now there are 2 process definitions deployed but both with version 1
    assertEquals(1, processDefinitions.get(0).getVersion());
    assertEquals(1, processDefinitions.get(1).getVersion());

    // old deployment was resumed
    ProcessApplicationRegistration registration = deployment2.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(2, deploymentIds.size());
    assertEquals(processEngine.getName(), registration.getProcessEngineName());
  }

  @Test
  public void testProcessApplicationDeploymentResumePreviousVersionsByDeploymentNameNoResume(){
    BpmnModelInstance process1 = createEmptyModel("process1");
    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .addModelInstance("process1.bpmn", process1));

    assertThatOneProcessIsDeployed();

    ProcessApplicationDeployment deployment2 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("anotherDeployment")
        .resumePreviousVersions()
        .resumePreviousVersionsBy(ResumePreviousBy.RESUME_BY_DEPLOYMENT_NAME)
        .addModelInstance("process2.bpmn", process1));

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
        .orderByProcessDefinitionVersion()
        .asc()
        .list();
    // there is a new version of the process
    assertEquals(1, processDefinitions.get(0).getVersion());
    assertEquals(2, processDefinitions.get(1).getVersion());

    // but the old deployment was not resumed
    ProcessApplicationRegistration registration = deployment2.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(1, deploymentIds.size());
    assertEquals(deployment2.getId(), deploymentIds.iterator().next());
    assertEquals(processEngine.getName(), registration.getProcessEngineName());
  }

  @Test
  public void testPartialChangesResumePreviousVersionByDeploymentName() {
    BpmnModelInstance model1 = createEmptyModel("process1");
    BpmnModelInstance model2 = createEmptyModel("process2");

    // create initial deployment
    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .addModelInstance("process1.bpmn20.xml", model1));

    ProcessApplicationDeployment deployment2 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .enableDuplicateFiltering(true)
        .resumePreviousVersions()
        .resumePreviousVersionsBy(ResumePreviousBy.RESUME_BY_DEPLOYMENT_NAME)
        .addModelInstance("process1.bpmn20.xml", model1)
        .addModelInstance("process2.bpmn20.xml", model2));

    ProcessApplicationRegistration registration = deployment2.getProcessApplicationRegistration();
    assertEquals(2, registration.getDeploymentIds().size());
  }

  @Test
  public void testProcessApplicationDeploymentResumptionDoesNotCachePreviousBpmnModelInstance() {
    // given an initial deployment
    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml"));

    deploymentCache.discardProcessDefinitionCache();

    // when an update with changes is deployed
    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("deployment")
        .enableDuplicateFiltering(false)
        .resumePreviousVersions()
        .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version2.bpmn20.xml"));

    // then the cache is still empty
    assertTrue(deploymentCache.getBpmnModelInstanceCache().isEmpty());
  }

  @Test
  public void testDeploymentSourceShouldBeNull() {
    // given
    String key = "process";
    BpmnModelInstance model = createEmptyModel(key);
    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();
    testRule.deploy(repositoryService
        .createDeployment()
        .name("first-deployment-without-a-source")
        .addModelInstance("process.bpmn", model));

    assertNull(deploymentQuery.deploymentName("first-deployment-without-a-source")
                   .singleResult()
                   .getSource());

    // when
    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("second-deployment-with-a-source")
        .source(null)
        .addModelInstance("process.bpmn", model));

    // then
    assertNull(deploymentQuery.deploymentName("second-deployment-with-a-source")
                   .singleResult()
                   .getSource());
  }

  @Test
  public void testDeploymentSourceShouldNotBeNull() {
    // given
    String key = "process";
    BpmnModelInstance model = createEmptyModel(key);
    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();
    testRule.deploy(repositoryService
        .createDeployment()
        .name("first-deployment-without-a-source")
        .source("my-first-deployment-source")
        .addModelInstance("process.bpmn", model));

    assertEquals("my-first-deployment-source",
                 deploymentQuery.deploymentName("first-deployment-without-a-source")
                     .singleResult()
                     .getSource());

    // when
    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("second-deployment-with-a-source")
        .source("my-second-deployment-source")
        .addModelInstance("process.bpmn", model));

    // then
    assertEquals("my-second-deployment-source",
                 deploymentQuery.deploymentName("second-deployment-with-a-source")
                     .singleResult()
                     .getSource());
  }

  @Test
  public void testDefaultDeploymentSource() {
    // given
    String key = "process";
    BpmnModelInstance model = createEmptyModel(key);
    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();

    // when
    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("first-deployment-with-a-source")
        .addModelInstance("process.bpmn", model));

    // then
    assertEquals(ProcessApplicationDeployment.PROCESS_APPLICATION_DEPLOYMENT_SOURCE,
                 deploymentQuery.deploymentName("first-deployment-with-a-source")
                     .singleResult()
                     .getSource());
  }

  @Test
  public void testOverwriteDeploymentSource() {
    // given
    String key = "process";
    BpmnModelInstance model = createEmptyModel(key);
    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();

    // when
    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("first-deployment-with-a-source")
        .source("my-source")
        .addModelInstance("process.bpmn", model));

    // then
    assertEquals("my-source",
                 deploymentQuery.deploymentName("first-deployment-with-a-source")
                     .singleResult()
                     .getSource());
  }

  @Test
  public void testNullDeploymentSourceAwareDuplicateFilter() {
    // given
    String key = "process";
    String name = "my-deployment";

    BpmnModelInstance model = createEmptyModel(key);

    ProcessDefinitionQuery processDefinitionQuery = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(key);

    DeploymentQuery deploymentQuery = repositoryService
        .createDeploymentQuery()
        .deploymentName(name);

    // when

    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .source(null)
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true));

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());

    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .source(null)
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true));

    // then

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());
  }

  @Test
  public void testNullAndProcessApplicationDeploymentSourceAwareDuplicateFilter() {
    // given

    String key = "process";
    String name = "my-deployment";

    BpmnModelInstance model = createEmptyModel(key);

    ProcessDefinitionQuery processDefinitionQuery = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(key);

    DeploymentQuery deploymentQuery = repositoryService
        .createDeploymentQuery()
        .deploymentName(name);

    // when

    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .source(null)
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true));

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());

    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true));

    // then

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());
  }

  @Test
  public void testProcessApplicationAndNullDeploymentSourceAwareDuplicateFilter() {
    // given

    String key = "process";
    String name = "my-deployment";

    BpmnModelInstance model = createEmptyModel(key);

    ProcessDefinitionQuery processDefinitionQuery = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(key);

    DeploymentQuery deploymentQuery = repositoryService
        .createDeploymentQuery()
        .deploymentName(name);

    // when

    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true));

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());

    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .source(null)
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true));

    // then

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());
  }

  @Test
  public void testProcessApplicationDeploymentSourceAwareDuplicateFilter() {
    // given

    String key = "process";
    String name = "my-deployment";

    BpmnModelInstance model = createEmptyModel(key);

    ProcessDefinitionQuery processDefinitionQuery = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(key);

    DeploymentQuery deploymentQuery = repositoryService
        .createDeploymentQuery()
        .deploymentName(name);

    // when

    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true));

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());

    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true));

    // then

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());
  }

  @Test
  public void testSameDeploymentSourceAwareDuplicateFilter() {
    // given

    String key = "process";
    String name = "my-deployment";

    BpmnModelInstance model = createEmptyModel(key);

    ProcessDefinitionQuery processDefinitionQuery = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(key);

    DeploymentQuery deploymentQuery = repositoryService
        .createDeploymentQuery()
        .deploymentName(name);

    // when

    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .source("cockpit")
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true));

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());

    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("my-deployment")
        .source("cockpit")
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true));

    // then

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());
  }

  @Test
  public void testDifferentDeploymentSourceShouldDeployNewVersion() {
    // given

    String key = "process";
    String name = "my-deployment";

    BpmnModelInstance model = createEmptyModel(key);

    ProcessDefinitionQuery processDefinitionQuery = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(key);

    DeploymentQuery deploymentQuery = repositoryService
        .createDeploymentQuery()
        .deploymentName(name);

    // when

    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .source("my-source1")
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true));

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());

    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .source("my-source2")
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true));

    // then

    assertEquals(2, processDefinitionQuery.count());
    assertEquals(2, deploymentQuery.count());
  }

  @Test
  public void testNullAndNotNullDeploymentSourceShouldDeployNewVersion() {
    // given

    String key = "process";
    String name = "my-deployment";

    BpmnModelInstance model = createEmptyModel(key);

    ProcessDefinitionQuery processDefinitionQuery = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(key);

    DeploymentQuery deploymentQuery = repositoryService
        .createDeploymentQuery()
        .deploymentName(name);

    // when

    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .source(null)
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true));

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());

    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .source("my-source2")
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true));

    // then

    assertEquals(2, processDefinitionQuery.count());
    assertEquals(2, deploymentQuery.count());
  }

  @Test
  public void testNotNullAndNullDeploymentSourceShouldDeployNewVersion() {
    // given

    String key = "process";
    String name = "my-deployment";

    BpmnModelInstance model = createEmptyModel(key);

    ProcessDefinitionQuery processDefinitionQuery = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(key);

    DeploymentQuery deploymentQuery = repositoryService
        .createDeploymentQuery()
        .deploymentName(name);

    // when

    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .source("my-source1")
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true));

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());

    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .source(null)
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true));

    // then

    assertEquals(2, processDefinitionQuery.count());
    assertEquals(2, deploymentQuery.count());
  }

  @Test
  public void testUnregisterProcessApplicationOnDeploymentDeletion() {
    // given a deployment with a process application registration
    Deployment deployment = testRule.deploy(repositoryService
        .createDeployment()
        .addModelInstance("process.bpmn", createEmptyModel("foo")));

    // and a process application registration
    managementService.registerProcessApplication(deployment.getId(),
                                                 processApplication.getReference());

    // when deleting the deploymen
    repositoryService.deleteDeployment(deployment.getId(), true);

    // then the registration is removed
    assertNull(managementService.getProcessApplicationForDeployment(deployment.getId()));
  }

  /*
   * A delay is introduced between the two deployments so the test is valid when MySQL
   * is used. See https://jira.camunda.com/browse/CAM-11893 for more details.
   */
  @Test
  public void shouldRegisterExistingDeploymentsOnLatestProcessDefinitionRemoval() {
    // given
    Date timeFreeze = new Date();
    ClockUtil.setCurrentTime(timeFreeze);
    BpmnModelInstance process1 = createEmptyModel("process");
    BpmnModelInstance process2 = Bpmn.createExecutableProcess("process").startEvent().endEvent().done();

    DeploymentWithDefinitions deployment1 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("foo")
        .addModelInstance("process.bpmn", process1));

    // offset second deployment time to detect latest deployment with MySQL timestamps
    ClockUtil.offset(1000L);
    DeploymentWithDefinitions deployment2 = testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .name("foo")
        .addModelInstance("process.bpmn", process2)
        .resumePreviousVersions()
        .enableDuplicateFiltering(true));

    ProcessDefinition latestProcessDefinition = deployment2.getDeployedProcessDefinitions().get(0);

    // assume
    assumeNotNull(managementService.getProcessApplicationForDeployment(deployment1.getId()));
    assumeNotNull(managementService.getProcessApplicationForDeployment(deployment2.getId()));

    // delete latest process definition
    repositoryService.deleteProcessDefinition(latestProcessDefinition.getId());

    // stop process engine by clearing the caches
    clearProcessApplicationDeployments();

    // when
    testRule.deploy(repositoryService
        .createDeployment(processApplication.getReference())
        .addModelInstance("process.bpmn", process2)
        .resumePreviousVersions()
        .enableDuplicateFiltering(true)
        .name("foo"));

    // then
    assertNotNull(managementService.getProcessApplicationForDeployment(deployment1.getId()));
    assertNotNull(managementService.getProcessApplicationForDeployment(deployment2.getId()));
  }

  /*
   * Clears the deployment caches to simulate a stop of the process engine.
   */
  protected void clearProcessApplicationDeployments() {
    processApplicationManager.clearRegistrations();
    registeredDeployments.clear();
    deploymentCache.discardProcessDefinitionCache();
  }

  /**
   * Creates a process definition query and checks that only one process with version 1 is present.
   */
  protected void assertThatOneProcessIsDeployed() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
        .singleResult();
    assertThat(processDefinition).isNotNull();
    assertEquals(1, processDefinition.getVersion());
  }

  protected BpmnModelInstance createEmptyModel(String key) {
    return Bpmn.createExecutableProcess(key).startEvent().done();
  }

}
