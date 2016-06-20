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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinitionQuery;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

public class DmnDeployerTest {

  protected ProcessEngineRule engineRule = new ProcessEngineRule(true);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  protected RepositoryService repositoryService;

  @Before
  public void initServices() {
    repositoryService = engineRule.getRepositoryService();
  }

  @Test
  public void dmnDeployment() {
    String resourceName = "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testDmnDeployment.dmn11.xml";
    String deploymentId = testRule.deploy(resourceName).getId();

    // there should be one deployment
    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();

    assertEquals(1, deploymentQuery.count());

    // there should be one case definition
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();
    assertEquals(1, query.count());

    DecisionDefinition decisionDefinition = query.singleResult();

    assertTrue(decisionDefinition.getId().startsWith("decision:1:"));
    assertEquals("http://camunda.org/schema/1.0/dmn", decisionDefinition.getCategory());
    assertEquals("CheckOrder", decisionDefinition.getName());
    assertEquals("decision", decisionDefinition.getKey());
    assertEquals(1, decisionDefinition.getVersion());
    assertEquals(resourceName, decisionDefinition.getResourceName());
    assertEquals(deploymentId, decisionDefinition.getDeploymentId());
    assertNull(decisionDefinition.getDiagramResourceName());
  }

  @Test
  public void dmnDeploymentWithDmnSuffix() {
    String resourceName = "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testDmnDeployment.dmn";
    String deploymentId = testRule.deploy(resourceName).getId();

    // there should be one deployment
    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();

    assertEquals(1, deploymentQuery.count());

    // there should be one case definition
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();
    assertEquals(1, query.count());

    DecisionDefinition decisionDefinition = query.singleResult();

    assertTrue(decisionDefinition.getId().startsWith("decision:1:"));
    assertEquals("http://camunda.org/schema/1.0/dmn", decisionDefinition.getCategory());
    assertEquals("CheckOrder", decisionDefinition.getName());
    assertEquals("decision", decisionDefinition.getKey());
    assertEquals(1, decisionDefinition.getVersion());
    assertEquals(resourceName, decisionDefinition.getResourceName());
    assertEquals(deploymentId, decisionDefinition.getDeploymentId());
    assertNull(decisionDefinition.getDiagramResourceName());
  }

  @Deployment
  @Test
  public void longDecisionDefinitionKey() {
    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();

    assertFalse(decisionDefinition.getId().startsWith("o123456789"));
    assertEquals("o123456789o123456789o123456789o123456789o123456789o123456789o123456789", decisionDefinition.getKey());
  }

  @Test
  public void duplicateIdInDeployment() {
    String resourceName1 = "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testDuplicateIdInDeployment.dmn11.xml";
    String resourceName2 = "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testDuplicateIdInDeployment2.dmn11.xml";

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("duplicateDecision");

    repositoryService.createDeployment()
            .addClasspathResource(resourceName1)
            .addClasspathResource(resourceName2)
            .name("duplicateIds")
            .deploy();
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testDecisionDiagramResource.dmn11.xml",
    "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testDecisionDiagramResource.png"
  })
  @Test
  public void getDecisionDiagramResource() {
    String resourcePrefix = "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testDecisionDiagramResource";

    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();

    assertEquals(resourcePrefix + ".dmn11.xml", decisionDefinition.getResourceName());
    assertEquals("decision", decisionDefinition.getKey());

    String diagramResourceName = decisionDefinition.getDiagramResourceName();
    assertEquals(resourcePrefix + ".png", diagramResourceName);

    InputStream diagramStream = repositoryService.getResourceAsStream(decisionDefinition.getDeploymentId(), diagramResourceName);
    final byte[] diagramBytes = IoUtil.readInputStream(diagramStream, "diagram stream");
    assertEquals(2540, diagramBytes.length);
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testMultipleDecisionDiagramResource.dmn11.xml",
    "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testMultipleDecisionDiagramResource.decision1.png",
    "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testMultipleDecisionDiagramResource.decision2.png",
    "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testMultipleDecisionDiagramResource.decision3.png"
  })
  @Test
  public void multipleDiagramResourcesProvided() {
    String resourcePrefix = "org/camunda/bpm/engine/test/dmn/deployment/DmnDeployerTest.testMultipleDecisionDiagramResource.";

    DecisionDefinitionQuery decisionDefinitionQuery = repositoryService.createDecisionDefinitionQuery();
    assertEquals(3, decisionDefinitionQuery.count());

    for (DecisionDefinition decisionDefinition : decisionDefinitionQuery.list()) {
      assertEquals(resourcePrefix + decisionDefinition.getKey() + ".png", decisionDefinition.getDiagramResourceName());
    }
  }

  @Deployment
  @Test
  public void decisionWithRequiredDecision() {
    // there should be two definitions
    // - the root decision and the required one
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    assertEquals(2, query.count());
    assertEquals(1, query.decisionDefinitionKey("decision").count());
    assertEquals(1, query.decisionDefinitionKey("score").count());
  }

}
