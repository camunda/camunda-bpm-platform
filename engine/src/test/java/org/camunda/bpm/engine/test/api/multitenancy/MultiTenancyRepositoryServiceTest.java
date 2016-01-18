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
import org.camunda.bpm.engine.repository.ProcessDefinition;

public class MultiTenancyRepositoryServiceTest extends PluggableProcessEngineTestCase {

  protected static final String TENANT_ONE = "tenant 1";
  protected static final String TENANT_TWO = "tenant 2";

  protected String deploymentOneId;
  protected String deploymentTwoId;

  @Override
  protected void setUp() throws Exception {
    deploymentOneId = repositoryService
        .createDeployment()
        .tenantId(TENANT_ONE)
        .addClasspathResource("org/camunda/bpm/engine/test/repository/one.bpmn20.xml")
        .addClasspathResource("org/camunda/bpm/engine/test/repository/two.bpmn20.xml")
        .deploy()
        .getId();

    deploymentTwoId = repositoryService
        .createDeployment()
        .tenantId(TENANT_TWO)
        .addClasspathResource("org/camunda/bpm/engine/test/repository/one.bpmn20.xml")
        .deploy()
        .getId();

    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    repositoryService.deleteDeployment(deploymentOneId);
    repositoryService.deleteDeployment(deploymentTwoId);
  }

  public void testDeploymentWithTenantId() {
    List<Deployment> deployments = repositoryService
        .createDeploymentQuery()
        .orderByTenantId()
        .asc()
        .list();

    assertEquals(2, deployments.size());
    assertEquals(TENANT_ONE, deployments.get(0).getTenantId());
    assertEquals(TENANT_TWO, deployments.get(1).getTenantId());
  }

  public void testDeployProcessDefinitionWithTenantId() {
    List<ProcessDefinition> processDefinitions = repositoryService
        .createProcessDefinitionQuery()
        .orderByTenantId()
        .asc()
        .list();

    assertEquals(3, processDefinitions.size());
    // inherit the tenant id from deployment
    assertEquals(TENANT_ONE, processDefinitions.get(0).getTenantId());
    assertEquals(TENANT_ONE, processDefinitions.get(1).getTenantId());
    assertEquals(TENANT_TWO, processDefinitions.get(2).getTenantId());
  }

  public void testProcessDefinitionVersionWithTenantId() {
    String deploymentId = repositoryService
        .createDeployment()
        .tenantId(TENANT_ONE)
        .addClasspathResource("org/camunda/bpm/engine/test/repository/one.bpmn20.xml")
        .deploy()
        .getId();

    List<ProcessDefinition> processDefinitions = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey("one")
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

    repositoryService.deleteDeployment(deploymentId);
  }

}
