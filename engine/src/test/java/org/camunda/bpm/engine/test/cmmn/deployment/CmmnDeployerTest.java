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

import java.io.InputStream;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Roman Smirnov
 *
 */
public class CmmnDeployerTest extends PluggableProcessEngineTestCase {

  public void testCmmnDeployment() {
    String deploymentId = processEngine
        .getRepositoryService()
        .createDeployment()
        .addClasspathResource("org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testSimpleDeployment.cmmn")
        .deploy()
        .getId();

    // there should be one deployment
    RepositoryService repositoryService = processEngine.getRepositoryService();
    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();

    assertEquals(1, deploymentQuery.count());

    // there should be one case definition
    CaseDefinitionQuery query = processEngine.getRepositoryService().createCaseDefinitionQuery();
    assertEquals(1, query.count());

    CaseDefinition caseDefinition = query.singleResult();
    assertEquals("Case_1", caseDefinition.getKey());

    processEngine.getRepositoryService().deleteDeployment(deploymentId);
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

  @Deployment(resources = { "org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testCaseDiagramResource.cmmn",
      "org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testCaseDiagramResource.png" })
  public void testCaseDiagramResource() {
    final CaseDefinition caseDefinition = repositoryService.createCaseDefinitionQuery().singleResult();

    assertEquals("org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testCaseDiagramResource.cmmn", caseDefinition.getResourceName());
    assertEquals("Case_1", caseDefinition.getKey());

    final String diagramResourceName = caseDefinition.getDiagramResourceName();
    assertEquals("org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testCaseDiagramResource.png", diagramResourceName);

    final InputStream diagramStream = repositoryService.getResourceAsStream(deploymentId,
        "org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testCaseDiagramResource.png");
    final byte[] diagramBytes = IoUtil.readInputStream(diagramStream, "diagram stream");
    assertEquals(2540, diagramBytes.length);
  }

  @Deployment(resources = { "org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testMultipleDiagramResourcesProvided.cmmn",
      "org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testMultipleDiagramResourcesProvided.a.png",
      "org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testMultipleDiagramResourcesProvided.b.png",
      "org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testMultipleDiagramResourcesProvided.c.png" })
  public void testMultipleDiagramResourcesProvided() {
    final CaseDefinition caseA = repositoryService.createCaseDefinitionQuery().caseDefinitionKey("a").singleResult();
    final CaseDefinition caseB = repositoryService.createCaseDefinitionQuery().caseDefinitionKey("b").singleResult();
    final CaseDefinition caseC = repositoryService.createCaseDefinitionQuery().caseDefinitionKey("c").singleResult();

    assertEquals("org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testMultipleDiagramResourcesProvided.a.png", caseA.getDiagramResourceName());
    assertEquals("org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testMultipleDiagramResourcesProvided.b.png", caseB.getDiagramResourceName());
    assertEquals("org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testMultipleDiagramResourcesProvided.c.png", caseC.getDiagramResourceName());
  }

  public void testDeployCmmn10XmlFile() {
    verifyCmmnResourceDeployed("org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testDeployCmmn10XmlFile.cmmn10.xml");

  }

  public void testDeployCmmn11XmlFile() {
    verifyCmmnResourceDeployed("org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testDeployCmmn11XmlFile.cmmn11.xml");
  }

  protected void verifyCmmnResourceDeployed(String resourcePath) {
    String deploymentId = processEngine
        .getRepositoryService()
        .createDeployment()
        .addClasspathResource(resourcePath)
        .deploy()
        .getId();

    // there should be one deployment
    RepositoryService repositoryService = processEngine.getRepositoryService();
    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();

    assertEquals(1, deploymentQuery.count());

    // there should be one case definition
    CaseDefinitionQuery query = processEngine.getRepositoryService().createCaseDefinitionQuery();
    assertEquals(1, query.count());

    CaseDefinition caseDefinition = query.singleResult();
    assertEquals("Case_1", caseDefinition.getKey());

    processEngine.getRepositoryService().deleteDeployment(deploymentId);

  }

}
