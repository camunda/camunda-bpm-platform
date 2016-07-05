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
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinitionQuery;
import org.camunda.bpm.engine.repository.DecisionRequirementsGraph;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinitionQuery;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

public class DecisionDefinitionDeployerTest {

  protected static final String DMN_CHECK_ORDER_RESOURCE = "org/camunda/bpm/engine/test/dmn/deployment/DecisionDefinitionDeployerTest.testDmnDeployment.dmn11.xml";
  protected static final String DMN_CHECK_ORDER_RESOURCE_DMN_SUFFIX = "org/camunda/bpm/engine/test/dmn/deployment/DecisionDefinitionDeployerTest.testDmnDeployment.dmn";
  protected static final String DMN_SCORE_RESOURCE = "org/camunda/bpm/engine/test/dmn/deployment/dmnScore.dmn11.xml";

  protected static final String DRD_SCORE_RESOURCE = "org/camunda/bpm/engine/test/dmn/deployment/drdScore.dmn11.xml";
  protected static final String DRD_SCORE_V2_RESOURCE = "org/camunda/bpm/engine/test/dmn/deployment/drdScore_v2.dmn11.xml";
  protected static final String DRD_DISH_RESOURCE = "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
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
    String deploymentId = testRule.deploy(DMN_CHECK_ORDER_RESOURCE).getId();

    // there should be decision deployment
    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();

    assertEquals(1, deploymentQuery.count());

    // there should be one decision definition
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();
    assertEquals(1, query.count());

    DecisionDefinition decisionDefinition = query.singleResult();

    assertTrue(decisionDefinition.getId().startsWith("decision:1:"));
    assertEquals("http://camunda.org/schema/1.0/dmn", decisionDefinition.getCategory());
    assertEquals("CheckOrder", decisionDefinition.getName());
    assertEquals("decision", decisionDefinition.getKey());
    assertEquals(1, decisionDefinition.getVersion());
    assertEquals(DMN_CHECK_ORDER_RESOURCE, decisionDefinition.getResourceName());
    assertEquals(deploymentId, decisionDefinition.getDeploymentId());
    assertNull(decisionDefinition.getDiagramResourceName());
  }

  @Test
  public void dmnDeploymentWithDmnSuffix() {
    String deploymentId = testRule.deploy(DMN_CHECK_ORDER_RESOURCE_DMN_SUFFIX).getId();

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
    assertEquals(DMN_CHECK_ORDER_RESOURCE_DMN_SUFFIX, decisionDefinition.getResourceName());
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
    String resourceName1 = "org/camunda/bpm/engine/test/dmn/deployment/DecisionDefinitionDeployerTest.testDuplicateIdInDeployment.dmn11.xml";
    String resourceName2 = "org/camunda/bpm/engine/test/dmn/deployment/DecisionDefinitionDeployerTest.testDuplicateIdInDeployment2.dmn11.xml";

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("duplicateDecision");

    repositoryService.createDeployment()
            .addClasspathResource(resourceName1)
            .addClasspathResource(resourceName2)
            .name("duplicateIds")
            .deploy();
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/dmn/deployment/DecisionDefinitionDeployerTest.testDecisionDiagramResource.dmn11.xml",
    "org/camunda/bpm/engine/test/dmn/deployment/DecisionDefinitionDeployerTest.testDecisionDiagramResource.png"
  })
  @Test
  public void getDecisionDiagramResource() {
    String resourcePrefix = "org/camunda/bpm/engine/test/dmn/deployment/DecisionDefinitionDeployerTest.testDecisionDiagramResource";

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
    "org/camunda/bpm/engine/test/dmn/deployment/DecisionDefinitionDeployerTest.testMultipleDecisionDiagramResource.dmn11.xml",
    "org/camunda/bpm/engine/test/dmn/deployment/DecisionDefinitionDeployerTest.testMultipleDecisionDiagramResource.decision1.png",
    "org/camunda/bpm/engine/test/dmn/deployment/DecisionDefinitionDeployerTest.testMultipleDecisionDiagramResource.decision2.png",
    "org/camunda/bpm/engine/test/dmn/deployment/DecisionDefinitionDeployerTest.testMultipleDecisionDiagramResource.decision3.png"
  })
  @Test
  public void multipleDiagramResourcesProvided() {
    String resourcePrefix = "org/camunda/bpm/engine/test/dmn/deployment/DecisionDefinitionDeployerTest.testMultipleDecisionDiagramResource.";

    DecisionDefinitionQuery decisionDefinitionQuery = repositoryService.createDecisionDefinitionQuery();
    assertEquals(3, decisionDefinitionQuery.count());

    for (DecisionDefinition decisionDefinition : decisionDefinitionQuery.list()) {
      assertEquals(resourcePrefix + decisionDefinition.getKey() + ".png", decisionDefinition.getDiagramResourceName());
    }
  }

  @Test
  public void drdDeployment() {
    String deploymentId = testRule.deploy(DRD_SCORE_RESOURCE).getId();

    // there should be one decision requirements definition
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();
    assertEquals(1, query.count());

    DecisionRequirementsGraph decisionRequirementsDefinition = query.singleResult();

    assertTrue(decisionRequirementsDefinition.getId().startsWith("score:1:"));
    assertEquals("score", decisionRequirementsDefinition.getKey());
    assertEquals("Score", decisionRequirementsDefinition.getName());
    assertEquals("test-drd-1", decisionRequirementsDefinition.getCategory());
    assertEquals(1, decisionRequirementsDefinition.getVersion());
    assertEquals(DRD_SCORE_RESOURCE, decisionRequirementsDefinition.getResourceName());
    assertEquals(deploymentId, decisionRequirementsDefinition.getDeploymentId());
    assertNull(decisionRequirementsDefinition.getDiagramResourceName());

    // both decisions should have a reference to the decision requirements definition
    List<DecisionDefinition> decisions = repositoryService.createDecisionDefinitionQuery().orderByDecisionDefinitionKey().asc().list();
    assertEquals(2, decisions.size());

    DecisionDefinition firstDecision = decisions.get(0);
    assertEquals("score-decision", firstDecision.getKey());
    assertEquals(decisionRequirementsDefinition.getId(), firstDecision.getDecisionRequirementsDefinitionId());

    DecisionDefinition secondDecision = decisions.get(1);
    assertEquals("score-result", secondDecision.getKey());
    assertEquals(decisionRequirementsDefinition.getId(), secondDecision.getDecisionRequirementsDefinitionId());
  }

  @Deployment( resources = DMN_CHECK_ORDER_RESOURCE )
  @Test
  public void noDrdForSingleDecisionDeployment() {
    // when the DMN file contains only a single decision definition
    assertEquals(1, repositoryService.createDecisionDefinitionQuery().count());

    // then create no decision requirements definition
    assertEquals(0, repositoryService.createDecisionRequirementsDefinitionQuery().count());
    // and don't link the decision to a decision requirements definition
    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();
    assertNull(decisionDefinition.getDecisionRequirementsDefinitionId());
  }

  @Deployment( resources = { DRD_SCORE_RESOURCE, DRD_DISH_RESOURCE })
  @Test
  public void multipleDrdDeployment() {
    // there should be two decision requirements definitions
    List<DecisionRequirementsGraph> decisionRequirementsDefinitions = repositoryService
        .createDecisionRequirementsDefinitionQuery()
        .orderByDecisionRequirementsDefinitionCategory()
        .asc()
        .list();

    assertEquals(2, decisionRequirementsDefinitions.size());
    assertEquals("score", decisionRequirementsDefinitions.get(0).getKey());
    assertEquals("dish", decisionRequirementsDefinitions.get(1).getKey());

    // the decisions should have a reference to the decision requirements definition
    List<DecisionDefinition> decisions = repositoryService.createDecisionDefinitionQuery().orderByDecisionDefinitionCategory().asc().list();
    assertEquals(5, decisions.size());
    assertEquals(decisionRequirementsDefinitions.get(0).getId(), decisions.get(0).getDecisionRequirementsDefinitionId());
    assertEquals(decisionRequirementsDefinitions.get(0).getId(), decisions.get(1).getDecisionRequirementsDefinitionId());
    assertEquals(decisionRequirementsDefinitions.get(1).getId(), decisions.get(2).getDecisionRequirementsDefinitionId());
    assertEquals(decisionRequirementsDefinitions.get(1).getId(), decisions.get(3).getDecisionRequirementsDefinitionId());
    assertEquals(decisionRequirementsDefinitions.get(1).getId(), decisions.get(4).getDecisionRequirementsDefinitionId());
  }

  @Test
  public void duplicateDrdIdInDeployment() {

    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("definitions");

    repositoryService.createDeployment()
            .addClasspathResource(DRD_SCORE_RESOURCE)
            .addClasspathResource(DRD_SCORE_V2_RESOURCE)
            .name("duplicateIds")
            .deploy();
  }

  @Test
  public void deployMultipleDecisionsWithSameDrdId() {
    // when deploying two decision with the same drd id `definitions`
    testRule.deploy(DMN_SCORE_RESOURCE, DMN_CHECK_ORDER_RESOURCE);

    // then create two decision definitions and
    // ignore the duplicated drd id since no drd is created
    assertEquals(2, repositoryService.createDecisionDefinitionQuery().count());
    assertEquals(0, repositoryService.createDecisionRequirementsDefinitionQuery().count());
  }

  @Test
  public void deployDecisionIndependentFromDrd() {
    String deploymentIdDecision = testRule.deploy(DMN_SCORE_RESOURCE).getId();
    String deploymentIdDrd = testRule.deploy(DRD_SCORE_RESOURCE).getId();

    // there should be one decision requirements definition
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();
    assertEquals(1, query.count());

    DecisionRequirementsGraph decisionRequirementsDefinition = query.singleResult();
    assertEquals(1, decisionRequirementsDefinition.getVersion());
    assertEquals(deploymentIdDrd, decisionRequirementsDefinition.getDeploymentId());

    // and two deployed decisions with different versions
    List<DecisionDefinition> decisions = repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey("score-decision")
        .orderByDecisionDefinitionVersion().asc()
        .list();

    assertEquals(2, decisions.size());

    DecisionDefinition firstDecision = decisions.get(0);
    assertEquals(1, firstDecision.getVersion());
    assertEquals(deploymentIdDecision, firstDecision.getDeploymentId());
    assertNull(firstDecision.getDecisionRequirementsDefinitionId());

    DecisionDefinition secondDecision = decisions.get(1);
    assertEquals(2, secondDecision.getVersion());
    assertEquals(deploymentIdDrd, secondDecision.getDeploymentId());
    assertEquals(decisionRequirementsDefinition.getId(),secondDecision.getDecisionRequirementsDefinitionId());
  }

}
