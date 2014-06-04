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
package org.camunda.bpm.engine.test.cmmn.deployment;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.camunda.bpm.engine.repository.DeploymentQuery;

/**
 * @author Roman Smirnov
 *
 */
public class CmmnDeployerTest extends PluggableProcessEngineTestCase {

  public void testCmmnDeployment() {
    ProcessEngineConfigurationImpl config = (ProcessEngineConfigurationImpl) ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("camunda.cfg.xml");
    ProcessEngine engine = config.buildProcessEngine();

    String deploymentId = engine
        .getRepositoryService()
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testSimpleDeployment.cmmn")
        .deploy()
        .getId();

    // there should be one deployment
    RepositoryService repositoryService = engine.getRepositoryService();
    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();

    assertEquals(1, deploymentQuery.count());

    // there should be one case definition
    CaseDefinitionQuery query = engine.getRepositoryService().createCaseDefinitionQuery();
    assertEquals(1, query.count());

    CaseDefinition caseDefinition = query.singleResult();
    assertEquals("Case_1", caseDefinition.getKey());

    engine.getRepositoryService().deleteDeployment(deploymentId);
  }

  public void testDeployTwoCasesWithDuplicateIdAtTheSameTime() {
    try {
      String cmmnResourceName1 = "org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testSimpleDeployment.cmmn";
      String cmmnResourceName2 = "org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testSimpleDeployment2.cmmn";
      repositoryService.createDeployment()
              .addClasspathResource(cmmnResourceName1)
              .addClasspathResource(cmmnResourceName2)
              .name("duplicateAtTheSameTime")
              .deploy();
      fail();
    } catch (Exception e) {
      // Verify that nothing is deployed
      assertEquals(0, repositoryService.createDeploymentQuery().count());
    }
  }

}
