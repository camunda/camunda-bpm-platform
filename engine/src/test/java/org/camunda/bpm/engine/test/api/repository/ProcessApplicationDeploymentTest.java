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
