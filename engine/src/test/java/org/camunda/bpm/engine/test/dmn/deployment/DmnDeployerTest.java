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
package org.camunda.bpm.engine.test.dmn.deployment;

import java.io.InputStream;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinitionQuery;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.test.Deployment;

public class DmnDeployerTest extends PluggableProcessEngineTestCase {

  public void testDmnDeployment() {
    String resourceName = "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testDmnDeployment.dmn10.xml";
    String deploymentId = repositoryService
        .createDeployment()
        .addClasspathResource(resourceName)
        .deploy()
        .getId();

    // there should be one deployment
    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();

    assertEquals(1, deploymentQuery.count());

    // there should be one case definition
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();
    assertEquals(1, query.count());

    DecisionDefinition decisionDefinition = query.singleResult();

    assertTrue(decisionDefinition.getId().startsWith("decision:1:"));
    assertEquals("http://camunda.org/dmn", decisionDefinition.getCategory());
    assertEquals("CheckOrder", decisionDefinition.getName());
    assertEquals("decision", decisionDefinition.getKey());
    assertEquals(1, decisionDefinition.getVersion());
    assertEquals(resourceName, decisionDefinition.getResourceName());
    assertEquals(deploymentId, decisionDefinition.getDeploymentId());
    assertNull(decisionDefinition.getDiagramResourceName());

    repositoryService.deleteDeployment(deploymentId);
  }

  public void testDmnDeploymentWithDmnSuffix() {
    String resourceName = "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testDmnDeployment.dmn";
    String deploymentId = repositoryService
      .createDeployment()
      .addClasspathResource(resourceName)
      .deploy()
      .getId();

    // there should be one deployment
    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();

    assertEquals(1, deploymentQuery.count());

    // there should be one case definition
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();
    assertEquals(1, query.count());

    DecisionDefinition decisionDefinition = query.singleResult();

    assertTrue(decisionDefinition.getId().startsWith("decision:1:"));
    assertEquals("http://camunda.org/dmn", decisionDefinition.getCategory());
    assertEquals("CheckOrder", decisionDefinition.getName());
    assertEquals("decision", decisionDefinition.getKey());
    assertEquals(1, decisionDefinition.getVersion());
    assertEquals(resourceName, decisionDefinition.getResourceName());
    assertEquals(deploymentId, decisionDefinition.getDeploymentId());
    assertNull(decisionDefinition.getDiagramResourceName());

    repositoryService.deleteDeployment(deploymentId);
  }

  @Deployment
  public void testLongDecisionDefinitionKey() {
    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();

    assertFalse(decisionDefinition.getId().startsWith("o123456789"));
    assertEquals("o123456789o123456789o123456789o123456789o123456789o123456789o123456789", decisionDefinition.getKey());
  }

  public void testDuplicateIdInDeployment() {
    try {
      String resourceName1 = "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testDuplicateIdInDeployment.dmn10.xml";
      String resourceName2 = "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testDuplicateIdInDeployment2.dmn10.xml";
      repositoryService.createDeployment()
              .addClasspathResource(resourceName1)
              .addClasspathResource(resourceName2)
              .name("duplicateIds")
              .deploy();
      fail();
    } catch (ProcessEngineException e) {
      assertTextPresent("duplicateDecision", e.getMessage());
      // Verify that nothing is deployed
      assertEquals(0, repositoryService.createDeploymentQuery().count());
    }
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testDecisionDiagramResource.dmn10.xml",
    "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testDecisionDiagramResource.png"
  })
  public void testDecisionDiagramResource() {
    String resourcePrefix = "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testDecisionDiagramResource";

    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();

    assertEquals(resourcePrefix + ".dmn10.xml", decisionDefinition.getResourceName());
    assertEquals("decision", decisionDefinition.getKey());

    String diagramResourceName = decisionDefinition.getDiagramResourceName();
    assertEquals(resourcePrefix + ".png", diagramResourceName);

    InputStream diagramStream = repositoryService.getResourceAsStream(deploymentId, diagramResourceName);
    final byte[] diagramBytes = IoUtil.readInputStream(diagramStream, "diagram stream");
    assertEquals(2540, diagramBytes.length);
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testMultipleDecisionDiagramResource.dmn10.xml",
    "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testMultipleDecisionDiagramResource.decision1.png",
    "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testMultipleDecisionDiagramResource.decision2.png",
    "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testMultipleDecisionDiagramResource.decision3.png"
  })
  public void testMultipleDiagramResourcesProvided() {
    String resourcePrefix = "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testMultipleDecisionDiagramResource.";

    DecisionDefinitionQuery decisionDefinitionQuery = repositoryService.createDecisionDefinitionQuery();
    assertEquals(3, decisionDefinitionQuery.count());

    for (DecisionDefinition decisionDefinition : decisionDefinitionQuery.list()) {
      assertEquals(resourcePrefix + decisionDefinition.getKey() + ".png", decisionDefinition.getDiagramResourceName());
    }
  }

}
