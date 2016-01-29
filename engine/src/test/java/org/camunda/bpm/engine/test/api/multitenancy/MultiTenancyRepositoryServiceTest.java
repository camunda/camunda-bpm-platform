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

import java.util.List;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class MultiTenancyRepositoryServiceTest extends PluggableProcessEngineTestCase {

  protected static final BpmnModelInstance emptyProcess = Bpmn.createExecutableProcess().done();

  public void testDeploymentWithoutTenantId() {
    createDeploymentBuilder()
      .deploy();

    Deployment deployment = repositoryService
        .createDeploymentQuery()
        .singleResult();

    assertNotNull(deployment);
    assertNull(deployment.getTenantId());
  }

  public void testDeploymentWithTenantId() {
    createDeploymentBuilder()
      .tenantId("tenant 1")
      .deploy();

    Deployment deployment = repositoryService
        .createDeploymentQuery()
        .singleResult();

    assertNotNull(deployment);
    assertEquals("tenant 1", deployment.getTenantId());
  }

  public void testProcessDefinitionVersionWithTenantId() {
    createDeploymentBuilder()
      .tenantId("tenant 1")
      .deploy();

    createDeploymentBuilder()
      .tenantId("tenant 1")
      .deploy();

    createDeploymentBuilder()
      .tenantId("tenant 2")
      .deploy();

    List<ProcessDefinition> processDefinitions = repositoryService
        .createProcessDefinitionQuery()
        .orderByTenantId()
        .asc()
        .orderByProcessDefinitionVersion()
        .asc()
        .list();

    assertEquals(3, processDefinitions.size());
    // process definition was deployed twice for tenant one
    assertEquals(1, processDefinitions.get(0).getVersion());
    assertEquals(2, processDefinitions.get(1).getVersion());
    // process definition version of tenant two have to be independent from tenant one
    assertEquals(1, processDefinitions.get(2).getVersion());
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
