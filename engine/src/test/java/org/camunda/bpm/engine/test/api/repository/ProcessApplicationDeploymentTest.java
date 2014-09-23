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

import java.util.List;
import java.util.Set;

import org.camunda.bpm.application.ProcessApplicationRegistration;
import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessApplicationDeployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
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

  public void testSimpleProcessApplicationDeployment() {

    ProcessApplicationDeployment deployment = repositoryService.createDeployment(processApplication.getReference())
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml")
      .deploy();

    // process is deployed:
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertNotNull(processDefinition);
    assertEquals(1, processDefinition.getVersion());

    // registration was performed:
    ProcessApplicationRegistration registration = deployment.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(1, deploymentIds.size());
    assertEquals(processEngine.getName(), registration.getProcessEngineName());

    repositoryService.deleteDeployment(deployment.getId(), true);
  }

  public void testProcessApplicationDeploymentNoChanges() {
    // create initial deployment
    ProcessApplicationDeployment deployment = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml")
      .deploy();

    // process is deployed:
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertNotNull(processDefinition);
    assertEquals(1, processDefinition.getVersion());

    // deploy update with no changes:
    deployment = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .enableDuplicateFiltering()
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml")
      .deploy();

    // no changes
    processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertNotNull(processDefinition);
    assertEquals(1, processDefinition.getVersion());
    ProcessApplicationRegistration registration = deployment.getProcessApplicationRegistration();
    Set<String> deploymentIds = registration.getDeploymentIds();
    assertEquals(1, deploymentIds.size());
    assertEquals(processEngine.getName(), registration.getProcessEngineName());

    repositoryService.deleteDeployment(deployment.getId(), true);
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
      .enableDuplicateFiltering()
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

    repositoryService.deleteDeployment(deployment1.getId(), true);
    repositoryService.deleteDeployment(deployment2.getId(), true);
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

    repositoryService.deleteDeployment(deployment1.getId(), true);
    repositoryService.deleteDeployment(deployment2.getId(), true);
    repositoryService.deleteDeployment(deployment3.getId(), true);
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

    repositoryService.deleteDeployment(deployment1.getId(), true);
    repositoryService.deleteDeployment(deployment2.getId(), true);
  }

  public void testProcessApplicationDeploymentResumePreviousVersions() {
    // create initial deployment
    ProcessApplicationDeployment deployment1 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml")
      .deploy();

    // process is deployed:
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertNotNull(processDefinition);
    assertEquals(1, processDefinition.getVersion());

    // deploy update with changes:
    ProcessApplicationDeployment deployment2 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .enableDuplicateFiltering()
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

    repositoryService.deleteDeployment(deployment1.getId(), true);
    repositoryService.deleteDeployment(deployment2.getId(), true);
  }

  public void testProcessApplicationDeploymentNoResume() {
    // create initial deployment
    ProcessApplicationDeployment deployment1 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .addClasspathResource("org/camunda/bpm/engine/test/api/repository/version1.bpmn20.xml")
      .deploy();

    // process is deployed:
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    assertNotNull(processDefinition);
    assertEquals(1, processDefinition.getVersion());

    // deploy update with changes:
    ProcessApplicationDeployment deployment2 = repositoryService.createDeployment(processApplication.getReference())
      .name("deployment")
      .enableDuplicateFiltering()
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

    repositoryService.deleteDeployment(deployment1.getId(), true);
    repositoryService.deleteDeployment(deployment2.getId(), true);
  }

}
