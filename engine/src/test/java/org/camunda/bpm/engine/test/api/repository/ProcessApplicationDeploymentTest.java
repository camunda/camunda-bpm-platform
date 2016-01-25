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
package org.camunda.bpm.engine.test.api.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Set;

import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.repository.ResumePreviousBy;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationDeploymentTest extends PluggableProcessEngineTestCase {

  private EmbeddedProcessApplication processApplication;

  protected void setUp() throws Exception {
    processApplication = new EmbeddedProcessApplication();
  }

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

  public void testSimpleProcessApplicationDeployment() {

    ProcessApplicationDeployment deployment = repositoryService.createDeployment(processApplication.getReference())
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml")
      .deploy();

    // process is deployed:
    assertThatOneProcessIsDeployed();

    // registration was performed:
    ProcessApplicationRegistration registration = deployment.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(1, deploymentIds.size());
    assertEquals(processEngine.getName(), registration.getProcessEngineName());

    deleteDeployments(deployment);
  }

  public void testProcessApplicationDeploymentNoChanges() {
    // create initial deployment
    ProcessApplicationDeployment deployment = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml")
      .deploy();

    assertThatOneProcessIsDeployed();

    // deploy update with no changes:
    deployment = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .enableDuplicateFiltering(false)
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml")
      .deploy();

    // no changes
    assertThatOneProcessIsDeployed();
    ProcessApplicationRegistration registration = deployment.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(1, deploymentIds.size());
    assertEquals(processEngine.getName(), registration.getProcessEngineName());

    deleteDeployments(deployment);
  }

  public void testPartialChangesDeployAll() {
    BpmnModelInstance model1 = Bpmn.createExecutableProcess("process1").done();
    BpmnModelInstance model2 = Bpmn.createExecutableProcess("process2").done();

    // create initial deployment
    ProcessApplicationDeployment deployment1 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .addModelInstance("process1.bpmn20.xml", model1)
      .addModelInstance("process2.bpmn20.xml", model2)
      .deploy();

    BpmnModelInstance changedModel2 = Bpmn.createExecutableProcess("process2").startEvent().done();

    // second deployment with partial changes:
    ProcessApplicationDeployment deployment2 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .enableDuplicateFiltering(false)
      .resumePreviousVersions()
      .addModelInstance("process1.bpmn20.xml", model1)
      .addModelInstance("process2.bpmn20.xml", changedModel2)
      .deploy();

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

    deleteDeployments(deployment1, deployment2);
  }

  /**
   * Test re-deployment of only those resources that have actually changed
   */
  public void testPartialChangesDeployChangedOnly() {
    BpmnModelInstance model1 = Bpmn.createExecutableProcess("process1").done();
    BpmnModelInstance model2 = Bpmn.createExecutableProcess("process2").done();

    // create initial deployment
    ProcessApplicationDeployment deployment1 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .addModelInstance("process1.bpmn20.xml", model1)
      .addModelInstance("process2.bpmn20.xml", model2)
      .deploy();

    BpmnModelInstance changedModel2 = Bpmn.createExecutableProcess("process2").startEvent().done();

    // second deployment with partial changes:
    ProcessApplicationDeployment deployment2 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .enableDuplicateFiltering(true)
      .resumePreviousVersions()
      .addModelInstance("process1.bpmn20.xml", model1)
      .addModelInstance("process2.bpmn20.xml", changedModel2)
      .deploy();

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

    BpmnModelInstance anotherChangedModel2 = Bpmn.createExecutableProcess("process2").startEvent().endEvent().done();

    // testing with a third deployment to ensure the change check is not only performed against
    // the last version of the deployment
    ProcessApplicationDeployment deployment3 = repositoryService.createDeployment(processApplication.getReference())
        .enableDuplicateFiltering(true)
        .resumePreviousVersions()
        .addModelInstance("process1.bpmn20.xml", model1)
        .addModelInstance("process2.bpmn20.xml", anotherChangedModel2)
        .name("deployment")
        .deploy();

    // there should still be one version of process 1
    assertEquals(1, repositoryService.createProcessDefinitionQuery().processDefinitionKey("process1").count());

    // there should be three versions of process 2
    assertEquals(3, repositoryService.createProcessDefinitionQuery().processDefinitionKey("process2").count());

    // old deployments are resumed
    registration = deployment3.getProcessApplicationRegistration();
    deploymentIds = registration.getDeploymentIds();
    assertEquals(3, deploymentIds.size());

    deleteDeployments(deployment1, deployment2, deployment3);
  }

  public void testPartialChangesResumePreviousVersion() {
    BpmnModelInstance model1 = Bpmn.createExecutableProcess("process1").done();
    BpmnModelInstance model2 = Bpmn.createExecutableProcess("process2").done();

    // create initial deployment
    ProcessApplicationDeployment deployment1 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .addModelInstance("process1.bpmn20.xml", model1)
      .deploy();

    ProcessApplicationDeployment deployment2 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .enableDuplicateFiltering(true)
      .resumePreviousVersions()
      .addModelInstance("process1.bpmn20.xml", model1)
      .addModelInstance("process2.bpmn20.xml", model2)
      .deploy();

    ProcessApplicationRegistration registration = deployment2.getProcessApplicationRegistration();
    assertEquals(2, registration.getDeploymentIds().size());

    deleteDeployments(deployment1, deployment2);
  }

  public void testProcessApplicationDeploymentResumePreviousVersions() {
    // create initial deployment
    ProcessApplicationDeployment deployment1 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml")
      .deploy();

    assertThatOneProcessIsDeployed();

    // deploy update with changes:
    ProcessApplicationDeployment deployment2 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .enableDuplicateFiltering(false)
      .resumePreviousVersions()
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version2.bpmn20.xml")
      .deploy();

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionVersion().asc().list();
    // now there are 2 process definitions deployed
    assertEquals(1, processDefinitions.get(0).getVersion());
    assertEquals(2, processDefinitions.get(1).getVersion());

    // old deployment was resumed
    ProcessApplicationRegistration registration = deployment2.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(2, deploymentIds.size());
    assertEquals(processEngine.getName(), registration.getProcessEngineName());

    deleteDeployments(deployment1, deployment2);
  }

  public void testProcessApplicationDeploymentResumePreviousVersionsDifferentKeys() {
    // create initial deployment
    ProcessApplicationDeployment deployment1 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml")
      .deploy();

    assertThatOneProcessIsDeployed();

    // deploy update with changes:
    ProcessApplicationDeployment deployment2 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .resumePreviousVersions()
      .addClasspathResource("org/camunda/bpm/engine/test/api/oneTaskProcess.bpmn20.xml")
      .deploy();

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionVersion().asc().list();
    // now there are 2 process definitions deployed
    assertEquals(1, processDefinitions.get(0).getVersion());
    assertEquals(1, processDefinitions.get(1).getVersion());

    // and the old deployment was not resumed
    ProcessApplicationRegistration registration = deployment2.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(1, deploymentIds.size());
    assertEquals(deployment2.getId(), deploymentIds.iterator().next());
    assertEquals(processEngine.getName(), registration.getProcessEngineName());

    deleteDeployments(deployment1, deployment2);
  }

  public void testProcessApplicationDeploymentNoResume() {
    // create initial deployment
    ProcessApplicationDeployment deployment1 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml")
      .deploy();

    assertThatOneProcessIsDeployed();

    // deploy update with changes:
    ProcessApplicationDeployment deployment2 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .enableDuplicateFiltering(false)
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version2.bpmn20.xml")
      .deploy();

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionVersion().asc().list();
    // now there are 2 process definitions deployed
    assertEquals(1, processDefinitions.get(0).getVersion());
    assertEquals(2, processDefinitions.get(1).getVersion());

    // old deployment was NOT resumed
    ProcessApplicationRegistration registration = deployment2.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(1, deploymentIds.size());
    assertEquals(processEngine.getName(), registration.getProcessEngineName());

    deleteDeployments(deployment1, deployment2);
  }

  public void testProcessApplicationDeploymentResumePreviousVersionsByDeploymentName() {
    // create initial deployment
    ProcessApplicationDeployment deployment1 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml")
      .deploy();

    assertThatOneProcessIsDeployed();

    // deploy update with changes:
    ProcessApplicationDeployment deployment2 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .enableDuplicateFiltering(false)
      .resumePreviousVersions()
      .resumePreviousVersionsBy(ResumePreviousBy.RESUME_BY_DEPLOYMENT_NAME)
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version2.bpmn20.xml")
      .deploy();

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionVersion().asc().list();
    // now there are 2 process definitions deployed
    assertEquals(1, processDefinitions.get(0).getVersion());
    assertEquals(2, processDefinitions.get(1).getVersion());

    // old deployment was resumed
    ProcessApplicationRegistration registration = deployment2.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(2, deploymentIds.size());
    assertEquals(processEngine.getName(), registration.getProcessEngineName());

    deleteDeployments(deployment1, deployment2);
  }

  public void testProcessApplicationDeploymentResumePreviousVersionsByDeploymentNameDeployDifferentProcesses(){
    BpmnModelInstance process1 = Bpmn.createExecutableProcess("process1").done();
    BpmnModelInstance process2 = Bpmn.createExecutableProcess("process2").done();
    ProcessApplicationDeployment deployment = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .addModelInstance("process1.bpmn", process1)
      .deploy();

    assertThatOneProcessIsDeployed();

    ProcessApplicationDeployment deployment2 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .resumePreviousVersions()
      .resumePreviousVersionsBy(ResumePreviousBy.RESUME_BY_DEPLOYMENT_NAME)
      .addModelInstance("process2.bpmn", process2)
      .deploy();

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionVersion().asc().list();
    // now there are 2 process definitions deployed but both with version 1
    assertEquals(1, processDefinitions.get(0).getVersion());
    assertEquals(1, processDefinitions.get(1).getVersion());

    // old deployment was resumed
    ProcessApplicationRegistration registration = deployment2.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(2, deploymentIds.size());
    assertEquals(processEngine.getName(), registration.getProcessEngineName());

    deleteDeployments(deployment, deployment2);
  }

  public void testProcessApplicationDeploymentResumePreviousVersionsByDeploymentNameNoResume(){
    BpmnModelInstance process1 = Bpmn.createExecutableProcess("process1").done();
    ProcessApplicationDeployment deployment = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .addModelInstance("process1.bpmn", process1)
      .deploy();

    assertThatOneProcessIsDeployed();

    ProcessApplicationDeployment deployment2 = repositoryService.createDeployment(processApplication.getReference())
      .name("anotherDeployment")
      .resumePreviousVersions()
      .resumePreviousVersionsBy(ResumePreviousBy.RESUME_BY_DEPLOYMENT_NAME)
      .addModelInstance("process2.bpmn", process1)
      .deploy();

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionVersion().asc().list();
    // there is a new version of the process
    assertEquals(1, processDefinitions.get(0).getVersion());
    assertEquals(2, processDefinitions.get(1).getVersion());

    // but the old deployment was not resumed
    ProcessApplicationRegistration registration = deployment2.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(1, deploymentIds.size());
    assertEquals(deployment2.getId(), deploymentIds.iterator().next());
    assertEquals(processEngine.getName(), registration.getProcessEngineName());

    deleteDeployments(deployment, deployment2);
  }

  public void testPartialChangesResumePreviousVersionByDeploymentName() {
    BpmnModelInstance model1 = Bpmn.createExecutableProcess("process1").done();
    BpmnModelInstance model2 = Bpmn.createExecutableProcess("process2").done();

    // create initial deployment
    ProcessApplicationDeployment deployment1 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .addModelInstance("process1.bpmn20.xml", model1)
      .deploy();

    ProcessApplicationDeployment deployment2 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .enableDuplicateFiltering(true)
      .resumePreviousVersions()
      .resumePreviousVersionsBy(ResumePreviousBy.RESUME_BY_DEPLOYMENT_NAME)
      .addModelInstance("process1.bpmn20.xml", model1)
      .addModelInstance("process2.bpmn20.xml", model2)
      .deploy();

    ProcessApplicationRegistration registration = deployment2.getProcessApplicationRegistration();
    assertEquals(2, registration.getDeploymentIds().size());

    deleteDeployments(deployment1, deployment2);
  }

  public void testDeploymentSourceShouldBeNull() {
    String key = "process";

    BpmnModelInstance model = Bpmn.createExecutableProcess(key).done();

    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();

    Deployment deployment1 = repositoryService
        .createDeployment()
        .name("first-deployment-without-a-source")
        .addModelInstance("process.bpmn", model)
        .deploy();

    assertNull(deploymentQuery.deploymentName("first-deployment-without-a-source").singleResult().getSource());

    Deployment deployment2 = repositoryService
        .createDeployment(processApplication.getReference())
        .name("second-deployment-with-a-source")
        .source(null)
        .addModelInstance("process.bpmn", model)
        .deploy();

    assertNull(deploymentQuery.deploymentName("second-deployment-with-a-source").singleResult().getSource());

    deleteDeployments(deployment1, deployment2);
  }

  public void testDeploymentSourceShouldNotBeNull() {
    String key = "process";

    BpmnModelInstance model = Bpmn.createExecutableProcess(key).done();

    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();

    Deployment deployment1 = repositoryService
        .createDeployment()
        .name("first-deployment-without-a-source")
        .source("my-first-deployment-source")
        .addModelInstance("process.bpmn", model)
        .deploy();

    assertEquals("my-first-deployment-source", deploymentQuery.deploymentName("first-deployment-without-a-source").singleResult().getSource());

    Deployment deployment2 = repositoryService
        .createDeployment(processApplication.getReference())
        .name("second-deployment-with-a-source")
        .source("my-second-deployment-source")
        .addModelInstance("process.bpmn", model)
        .deploy();

    assertEquals("my-second-deployment-source", deploymentQuery.deploymentName("second-deployment-with-a-source").singleResult().getSource());

    deleteDeployments(deployment1, deployment2);
  }

  public void testDefaultDeploymentSource() {
    String key = "process";

    BpmnModelInstance model = Bpmn.createExecutableProcess(key).done();

    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();

    Deployment deployment = repositoryService
        .createDeployment(processApplication.getReference())
        .name("first-deployment-with-a-source")
        .addModelInstance("process.bpmn", model)
        .deploy();

    assertEquals(ProcessApplicationDeployment.PROCESS_APPLICATION_DEPLOYMENT_SOURCE, deploymentQuery.deploymentName("first-deployment-with-a-source").singleResult().getSource());

    deleteDeployments(deployment);
  }

  public void testOverwriteDeploymentSource() {
    String key = "process";

    BpmnModelInstance model = Bpmn.createExecutableProcess(key).done();

    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();

    Deployment deployment = repositoryService
        .createDeployment(processApplication.getReference())
        .name("first-deployment-with-a-source")
        .source("my-source")
        .addModelInstance("process.bpmn", model)
        .deploy();

    assertEquals("my-source", deploymentQuery.deploymentName("first-deployment-with-a-source").singleResult().getSource());

    deleteDeployments(deployment);
  }

  public void testNullDeploymentSourceAwareDuplicateFilter() {
    // given
    String key = "process";
    String name = "my-deployment";

    BpmnModelInstance model = Bpmn.createExecutableProcess(key).done();

    ProcessDefinitionQuery processDefinitionQuery = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(key);

    DeploymentQuery deploymentQuery = repositoryService
        .createDeploymentQuery()
        .deploymentName(name);

    // when

    ProcessApplicationDeployment deployment1 = repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .source(null)
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true)
        .deploy();

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());

    ProcessApplicationDeployment deployment2 = repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .source(null)
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true)
        .deploy();

    // then

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());

    deleteDeployments(deployment1, deployment2);
  }

  public void testNullAndProcessApplicationDeploymentSourceAwareDuplicateFilter() {
    // given

    String key = "process";
    String name = "my-deployment";

    BpmnModelInstance model = Bpmn.createExecutableProcess(key).done();

    ProcessDefinitionQuery processDefinitionQuery = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(key);

    DeploymentQuery deploymentQuery = repositoryService
        .createDeploymentQuery()
        .deploymentName(name);

    // when

    ProcessApplicationDeployment deployment1 = repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .source(null)
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true)
        .deploy();

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());

    ProcessApplicationDeployment deployment2 = repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true)
        .deploy();

    // then

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());

    deleteDeployments(deployment1, deployment2);
  }

  public void testProcessApplicationAndNullDeploymentSourceAwareDuplicateFilter() {
    // given

    String key = "process";
    String name = "my-deployment";

    BpmnModelInstance model = Bpmn.createExecutableProcess(key).done();

    ProcessDefinitionQuery processDefinitionQuery = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(key);

    DeploymentQuery deploymentQuery = repositoryService
        .createDeploymentQuery()
        .deploymentName(name);

    // when

    ProcessApplicationDeployment deployment1 = repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true)
        .deploy();

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());

    ProcessApplicationDeployment deployment2 = repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .source(null)
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true)
        .deploy();

    // then

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());

    deleteDeployments(deployment1, deployment2);
  }

  public void testProcessApplicationDeploymentSourceAwareDuplicateFilter() {
    // given

    String key = "process";
    String name = "my-deployment";

    BpmnModelInstance model = Bpmn.createExecutableProcess(key).done();

    ProcessDefinitionQuery processDefinitionQuery = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(key);

    DeploymentQuery deploymentQuery = repositoryService
        .createDeploymentQuery()
        .deploymentName(name);

    // when

    ProcessApplicationDeployment deployment1 = repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true)
        .deploy();

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());

    ProcessApplicationDeployment deployment2 = repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true)
        .deploy();

    // then

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());

    deleteDeployments(deployment1, deployment2);
  }

  public void testSameDeploymentSourceAwareDuplicateFilter() {
    // given

    String key = "process";
    String name = "my-deployment";

    BpmnModelInstance model = Bpmn.createExecutableProcess(key).done();

    ProcessDefinitionQuery processDefinitionQuery = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(key);

    DeploymentQuery deploymentQuery = repositoryService
        .createDeploymentQuery()
        .deploymentName(name);

    // when

    ProcessApplicationDeployment deployment1 = repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .source("cockpit")
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true)
        .deploy();

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());

    ProcessApplicationDeployment deployment2 = repositoryService
        .createDeployment(processApplication.getReference())
        .name("my-deployment")
        .source("cockpit")
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true)
        .deploy();

    // then

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());

    deleteDeployments(deployment1, deployment2);
  }

  public void testDifferentDeploymentSourceShouldDeployNewVersion() {
    // given

    String key = "process";
    String name = "my-deployment";

    BpmnModelInstance model = Bpmn.createExecutableProcess(key).done();

    ProcessDefinitionQuery processDefinitionQuery = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(key);

    DeploymentQuery deploymentQuery = repositoryService
        .createDeploymentQuery()
        .deploymentName(name);

    // when

    ProcessApplicationDeployment deployment1 = repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .source("my-source1")
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true)
        .deploy();

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());

    ProcessApplicationDeployment deployment2 = repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .source("my-source2")
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true)
        .deploy();

    // then

    assertEquals(2, processDefinitionQuery.count());
    assertEquals(2, deploymentQuery.count());

    deleteDeployments(deployment1, deployment2);
  }

  public void testNullAndNotNullDeploymentSourceShouldDeployNewVersion() {
    // given

    String key = "process";
    String name = "my-deployment";

    BpmnModelInstance model = Bpmn.createExecutableProcess(key).done();

    ProcessDefinitionQuery processDefinitionQuery = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(key);

    DeploymentQuery deploymentQuery = repositoryService
        .createDeploymentQuery()
        .deploymentName(name);

    // when

    ProcessApplicationDeployment deployment1 = repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .source(null)
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true)
        .deploy();

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());

    ProcessApplicationDeployment deployment2 = repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .source("my-source2")
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true)
        .deploy();

    // then

    assertEquals(2, processDefinitionQuery.count());
    assertEquals(2, deploymentQuery.count());

    deleteDeployments(deployment1, deployment2);
  }

  public void testNotNullAndNullDeploymentSourceShouldDeployNewVersion() {
    // given

    String key = "process";
    String name = "my-deployment";

    BpmnModelInstance model = Bpmn.createExecutableProcess(key).done();

    ProcessDefinitionQuery processDefinitionQuery = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(key);

    DeploymentQuery deploymentQuery = repositoryService
        .createDeploymentQuery()
        .deploymentName(name);

    // when

    ProcessApplicationDeployment deployment1 = repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .source("my-source1")
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true)
        .deploy();

    assertEquals(1, processDefinitionQuery.count());
    assertEquals(1, deploymentQuery.count());

    ProcessApplicationDeployment deployment2 = repositoryService
        .createDeployment(processApplication.getReference())
        .name(name)
        .source(null)
        .addModelInstance("process.bpmn", model)
        .enableDuplicateFiltering(true)
        .deploy();

    // then

    assertEquals(2, processDefinitionQuery.count());
    assertEquals(2, deploymentQuery.count());

    deleteDeployments(deployment1, deployment2);
  }

  public void testUnregisterProcessApplicationOnDeploymentDeletion() {
    // given a deployment with a process application registration
    Deployment deployment = repositoryService
      .createDeployment()
      .addModelInstance("process.bpmn", Bpmn.createExecutableProcess("foo").done())
      .deploy();

    // and a process application registration
    managementService.registerProcessApplication(deployment.getId(), processApplication.getReference());

    // when deleting the deploymen
    repositoryService.deleteDeployment(deployment.getId(), true);

    // then the registration is removed
    assertNull(managementService.getProcessApplicationForDeployment(deployment.getId()));



  }

  /**
   * Deletes the deployments cascading.
   */
  private void deleteDeployments(Deployment... deployments){
    for (Deployment deployment : deployments) {
      repositoryService.deleteDeployment(deployment.getId(), true);
    }
  }

  /**
   * Creates a process definition query and checks that only one process with version 1 is present.
   */
  private void assertThatOneProcessIsDeployed() {
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertThat(processDefinition, is(notNullValue()));
    assertEquals(1, processDefinition.getVersion());
  }

}
