/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.dmn.deployment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinitionQuery;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinitionQuery;
import org.camunda.bpm.engine.repository.DeploymentBuilder;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.impl.DmnModelConstants;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.InputExpression;
import org.camunda.bpm.model.dmn.instance.Output;
import org.camunda.bpm.model.dmn.instance.Text;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class DecisionDefinitionDeployerTest {

  protected static final String DMN_CHECK_ORDER_RESOURCE = "org/camunda/bpm/engine/test/dmn/deployment/DecisionDefinitionDeployerTest.testDmnDeployment.dmn11.xml";
  protected static final String DMN_CHECK_ORDER_RESOURCE_DMN_SUFFIX = "org/camunda/bpm/engine/test/dmn/deployment/DecisionDefinitionDeployerTest.testDmnDeployment.dmn";
  protected static final String DMN_SCORE_RESOURCE = "org/camunda/bpm/engine/test/dmn/deployment/dmnScore.dmn11.xml";

  protected static final String DMN_DECISION_LITERAL_EXPRESSION = "org/camunda/bpm/engine/test/dmn/deployment/DecisionWithLiteralExpression.dmn";

  protected static final String DRD_SCORE_RESOURCE = "org/camunda/bpm/engine/test/dmn/deployment/drdScore.dmn11.xml";
  protected static final String DRD_SCORE_V2_RESOURCE = "org/camunda/bpm/engine/test/dmn/deployment/drdScore_v2.dmn11.xml";
  protected static final String DRD_DISH_RESOURCE = "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

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

  @Test
  public void dmnDeploymentWithDecisionLiteralExpression() {
    String deploymentId = testRule.deploy(DMN_DECISION_LITERAL_EXPRESSION).getId();

    // there should be decision deployment
    DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();
    assertEquals(1, deploymentQuery.count());

    // there should be one decision definition
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();
    assertEquals(1, query.count());

    DecisionDefinition decisionDefinition = query.singleResult();

    assertTrue(decisionDefinition.getId().startsWith("decisionLiteralExpression:1:"));
    assertEquals("http://camunda.org/schema/1.0/dmn", decisionDefinition.getCategory());
    assertEquals("decisionLiteralExpression", decisionDefinition.getKey());
    assertEquals("Decision with Literal Expression", decisionDefinition.getName());
    assertEquals(1, decisionDefinition.getVersion());
    assertEquals(DMN_DECISION_LITERAL_EXPRESSION, decisionDefinition.getResourceName());
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

    // when/then
    assertThatThrownBy(() -> repositoryService.createDeployment()
        .addClasspathResource(resourceName1)
        .addClasspathResource(resourceName2)
        .name("duplicateIds")
        .deploy())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("duplicateDecision");
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

    DecisionRequirementsDefinition decisionRequirementsDefinition = query.singleResult();

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
    assertEquals("score", firstDecision.getDecisionRequirementsDefinitionKey());

    DecisionDefinition secondDecision = decisions.get(1);
    assertEquals("score-result", secondDecision.getKey());
    assertEquals(decisionRequirementsDefinition.getId(), secondDecision.getDecisionRequirementsDefinitionId());
    assertEquals("score", secondDecision.getDecisionRequirementsDefinitionKey());
  }

  @Deployment( resources = DMN_CHECK_ORDER_RESOURCE )
  @Test
  public void noDrdForSingleDecisionDeployment() {
    // when the DMN file contains only a single decision definition
    assertEquals(1, repositoryService.createDecisionDefinitionQuery().count());

    // then no decision requirements definition should be created
    assertEquals(0, repositoryService.createDecisionRequirementsDefinitionQuery().count());
    // and the decision should not be linked to a decision requirements definition
    DecisionDefinition decisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();
    assertNull(decisionDefinition.getDecisionRequirementsDefinitionId());
    assertNull(decisionDefinition.getDecisionRequirementsDefinitionKey());
  }

  @Deployment( resources = { DRD_SCORE_RESOURCE, DRD_DISH_RESOURCE })
  @Test
  public void multipleDrdDeployment() {
    // there should be two decision requirements definitions
    List<DecisionRequirementsDefinition> decisionRequirementsDefinitions = repositoryService
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

    // when/then
    assertThatThrownBy(() -> repositoryService.createDeployment()
        .addClasspathResource(DRD_SCORE_RESOURCE)
        .addClasspathResource(DRD_SCORE_V2_RESOURCE)
        .name("duplicateIds")
        .deploy())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("definitions");
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

    DecisionRequirementsDefinition decisionRequirementsDefinition = query.singleResult();
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

  @Test
  public void testDeployDmnModelInstance() throws Exception {
    // given
    DmnModelInstance dmnModelInstance = createDmnModelInstance();

    // when
    testRule.deploy(repositoryService.createDeployment().addModelInstance("foo.dmn", dmnModelInstance));

    // then
    assertNotNull(repositoryService.createDecisionDefinitionQuery()
        .decisionDefinitionResourceName("foo.dmn").singleResult());
  }

  @Test
  public void testDeployDmnModelInstanceNegativeHistoryTimeToLive() throws Exception {
    // given
    DmnModelInstance dmnModelInstance = createDmnModelInstanceNegativeHistoryTimeToLive();

    try {
      testRule.deploy(repositoryService.createDeployment().addModelInstance("foo.dmn", dmnModelInstance));
      fail("Exception for negative time to live value is expected.");
    } catch (ProcessEngineException ex) {
      assertTrue(ex.getCause().getMessage().contains("negative value is not allowed"));
    }
  }

  protected static DmnModelInstance createDmnModelInstanceNegativeHistoryTimeToLive() {
    DmnModelInstance modelInstance = Dmn.createEmptyModel();
    Definitions definitions = modelInstance.newInstance(Definitions.class);
    definitions.setId(DmnModelConstants.DMN_ELEMENT_DEFINITIONS);
    definitions.setName(DmnModelConstants.DMN_ELEMENT_DEFINITIONS);
    definitions.setNamespace(DmnModelConstants.CAMUNDA_NS);
    modelInstance.setDefinitions(definitions);

    Decision decision = modelInstance.newInstance(Decision.class);
    decision.setId("Decision-1");
    decision.setName("foo");
    decision.setCamundaHistoryTimeToLive(-5);
    modelInstance.getDefinitions().addChildElement(decision);

    return modelInstance;
  }

  protected static DmnModelInstance createDmnModelInstance() {
    DmnModelInstance modelInstance = Dmn.createEmptyModel();
    Definitions definitions = modelInstance.newInstance(Definitions.class);
    definitions.setId(DmnModelConstants.DMN_ELEMENT_DEFINITIONS);
    definitions.setName(DmnModelConstants.DMN_ELEMENT_DEFINITIONS);
    definitions.setNamespace(DmnModelConstants.CAMUNDA_NS);
    modelInstance.setDefinitions(definitions);

    Decision decision = modelInstance.newInstance(Decision.class);
    decision.setId("Decision-1");
    decision.setName("foo");
    decision.setCamundaHistoryTimeToLive(5);
    modelInstance.getDefinitions().addChildElement(decision);

    DecisionTable decisionTable = modelInstance.newInstance(DecisionTable.class);
    decisionTable.setId(DmnModelConstants.DMN_ELEMENT_DECISION_TABLE);
    decisionTable.setHitPolicy(HitPolicy.FIRST);
    decision.addChildElement(decisionTable);

    Input input = modelInstance.newInstance(Input.class);
    input.setId("Input-1");
    input.setLabel("Input");
    decisionTable.addChildElement(input);

    InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
    inputExpression.setId("InputExpression-1");
    Text inputExpressionText = modelInstance.newInstance(Text.class);
    inputExpressionText.setTextContent("input");
    inputExpression.setText(inputExpressionText);
    inputExpression.setTypeRef("string");
    input.setInputExpression(inputExpression);

    Output output = modelInstance.newInstance(Output.class);
    output.setName("output");
    output.setLabel("Output");
    output.setTypeRef("string");
    decisionTable.addChildElement(output);

    return modelInstance;
  }

  @Test
  public void testDeployAndGetDecisionDefinition() throws Exception {

    // given decision model
    DmnModelInstance dmnModelInstance = createDmnModelInstance();

    // when decision model is deployed
    DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().addModelInstance("foo.dmn", dmnModelInstance);
    DeploymentWithDefinitions deployment = testRule.deploy(deploymentBuilder);

    // then deployment contains definition
    List<DecisionDefinition> deployedDecisionDefinitions = deployment.getDeployedDecisionDefinitions();
    assertEquals(1, deployedDecisionDefinitions.size());
    assertNull(deployment.getDeployedDecisionRequirementsDefinitions());
    assertNull(deployment.getDeployedProcessDefinitions());
    assertNull(deployment.getDeployedCaseDefinitions());

    // and persisted definition are equal to deployed definition
    DecisionDefinition persistedDecisionDef = repositoryService.createDecisionDefinitionQuery()
      .decisionDefinitionResourceName("foo.dmn").singleResult();
    assertEquals(persistedDecisionDef.getId(), deployedDecisionDefinitions.get(0).getId());
  }

  @Test
  public void testDeployEmptyDecisionDefinition() throws Exception {

    // given empty decision model
    DmnModelInstance modelInstance = Dmn.createEmptyModel();
    Definitions definitions = modelInstance.newInstance(Definitions.class);
    definitions.setId(DmnModelConstants.DMN_ELEMENT_DEFINITIONS);
    definitions.setName(DmnModelConstants.DMN_ELEMENT_DEFINITIONS);
    definitions.setNamespace(DmnModelConstants.CAMUNDA_NS);
    modelInstance.setDefinitions(definitions);

    // when decision model is deployed
    DeploymentBuilder deploymentBuilder = repositoryService.createDeployment().addModelInstance("foo.dmn", modelInstance);
    DeploymentWithDefinitions deployment = testRule.deploy(deploymentBuilder);

    // then deployment contains no definitions
    assertNull(deployment.getDeployedDecisionDefinitions());
    assertNull(deployment.getDeployedDecisionRequirementsDefinitions());

    // and there are no persisted definitions
    assertNull(repositoryService.createDecisionDefinitionQuery()
      .decisionDefinitionResourceName("foo.dmn").singleResult());
  }


  @Test
  public void testDeployAndGetDRDDefinition() throws Exception {

    // when decision requirement graph is deployed
    DeploymentWithDefinitions deployment = testRule.deploy(DRD_SCORE_RESOURCE);

    // then deployment contains definitions
    List<DecisionDefinition> deployedDecisionDefinitions = deployment.getDeployedDecisionDefinitions();
    assertEquals(2, deployedDecisionDefinitions.size());

    List<DecisionRequirementsDefinition> deployedDecisionRequirementsDefinitions = deployment.getDeployedDecisionRequirementsDefinitions();
    assertEquals(1, deployedDecisionRequirementsDefinitions.size());

    assertNull(deployment.getDeployedProcessDefinitions());
    assertNull(deployment.getDeployedCaseDefinitions());

    // and persisted definitions are equal to deployed definitions
    DecisionRequirementsDefinition persistedDecisionRequirementsDefinition = repositoryService.createDecisionRequirementsDefinitionQuery()
      .decisionRequirementsDefinitionResourceName(DRD_SCORE_RESOURCE).singleResult();
    assertEquals(persistedDecisionRequirementsDefinition.getId(), deployedDecisionRequirementsDefinitions.get(0).getId());

    List<DecisionDefinition> persistedDecisionDefinitions = repositoryService.createDecisionDefinitionQuery().decisionDefinitionResourceName(DRD_SCORE_RESOURCE).list();
    assertEquals(deployedDecisionDefinitions.size(), persistedDecisionDefinitions.size());
  }

  @Test
  public void testDeployDecisionDefinitionWithIntegerHistoryTimeToLive() {
    // when
    DeploymentWithDefinitions deployment = testRule.deploy("org/camunda/bpm/engine/test/dmn/deployment/DecisionDefinitionDeployerTest.testDecisionDefinitionWithIntegerHistoryTimeToLive.dmn11.xml");

    // then
    List<DecisionDefinition> deployedDecisionDefinitions = deployment.getDeployedDecisionDefinitions();
    assertEquals(deployedDecisionDefinitions.size(), 1);
    Integer historyTimeToLive = deployedDecisionDefinitions.get(0).getHistoryTimeToLive();
    assertNotNull(historyTimeToLive);
    assertEquals((int) historyTimeToLive, 5);
  }

  @Test
  public void testDeployDecisionDefinitionWithStringHistoryTimeToLive() {
    // when
    DeploymentWithDefinitions deployment = testRule.deploy("org/camunda/bpm/engine/test/dmn/deployment/DecisionDefinitionDeployerTest.testDecisionDefinitionWithStringHistoryTimeToLive.dmn11.xml");

    // then
    List<DecisionDefinition> deployedDecisionDefinitions = deployment.getDeployedDecisionDefinitions();
    assertEquals(deployedDecisionDefinitions.size(), 1);
    Integer historyTimeToLive = deployedDecisionDefinitions.get(0).getHistoryTimeToLive();
    assertNotNull(historyTimeToLive);
    assertEquals((int) historyTimeToLive, 5);
  }

  @Test
  public void testDeployDecisionDefinitionWithMalformedStringHistoryTimeToLive() {
    try {
      testRule.deploy("org/camunda/bpm/engine/test/dmn/deployment/DecisionDefinitionDeployerTest.testDecisionDefinitionWithMalformedHistoryTimeToLive.dmn11.xml");
      fail("Exception expected");
    } catch (ProcessEngineException e) {
      assertTrue(e.getCause().getMessage().contains("Cannot parse historyTimeToLive"));
    }
  }

  @Test
  public void testDeployDecisionDefinitionWithEmptyHistoryTimeToLive() {
      DeploymentWithDefinitions deployment = testRule.deploy("org/camunda/bpm/engine/test/dmn/deployment/DecisionDefinitionDeployerTest.testDecisionDefinitionWithEmptyHistoryTimeToLive.dmn11.xml");

      // then
      List<DecisionDefinition> deployedDecisionDefinitions = deployment.getDeployedDecisionDefinitions();
      assertEquals(deployedDecisionDefinitions.size(), 1);
      Integer historyTimeToLive = deployedDecisionDefinitions.get(0).getHistoryTimeToLive();
      assertNull(historyTimeToLive);
  }

}
