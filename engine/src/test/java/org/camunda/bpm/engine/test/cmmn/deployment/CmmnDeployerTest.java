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
package org.camunda.bpm.engine.test.cmmn.deployment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.camunda.bpm.engine.repository.DeploymentQuery;
import org.camunda.bpm.engine.repository.DeploymentWithDefinitions;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.model.cmmn.Cmmn;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.cmmn.instance.Case;
import org.camunda.bpm.model.cmmn.instance.CasePlanModel;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class CmmnDeployerTest extends PluggableProcessEngineTest {

  @Test
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

  @Test
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
  @Test
  public void testCaseDiagramResource() {
    String deploymentId = repositoryService.createDeploymentQuery().singleResult().getId();
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
  @Test
  public void testMultipleDiagramResourcesProvided() {
    final CaseDefinition caseA = repositoryService.createCaseDefinitionQuery().caseDefinitionKey("a").singleResult();
    final CaseDefinition caseB = repositoryService.createCaseDefinitionQuery().caseDefinitionKey("b").singleResult();
    final CaseDefinition caseC = repositoryService.createCaseDefinitionQuery().caseDefinitionKey("c").singleResult();

    assertEquals("org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testMultipleDiagramResourcesProvided.a.png", caseA.getDiagramResourceName());
    assertEquals("org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testMultipleDiagramResourcesProvided.b.png", caseB.getDiagramResourceName());
    assertEquals("org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testMultipleDiagramResourcesProvided.c.png", caseC.getDiagramResourceName());
  }

  @Test
  public void testDeployCmmn10XmlFile() {
    verifyCmmnResourceDeployed("org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testDeployCmmn10XmlFile.cmmn10.xml");

  }

  @Test
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

  @Test
  public void testDeployCmmnModelInstance() throws Exception {
    // given
    CmmnModelInstance modelInstance = createCmmnModelInstance();

    // when
    testRule.deploy(repositoryService.createDeployment().addModelInstance("foo.cmmn", modelInstance));

    // then
    assertNotNull(repositoryService.createCaseDefinitionQuery().caseDefinitionResourceName("foo.cmmn").singleResult());
  }

  protected static CmmnModelInstance createCmmnModelInstance() {
    final CmmnModelInstance modelInstance = Cmmn.createEmptyModel();
    org.camunda.bpm.model.cmmn.instance.Definitions definitions = modelInstance.newInstance(org.camunda.bpm.model.cmmn.instance.Definitions.class);
    definitions.setTargetNamespace("http://camunda.org/examples");
    modelInstance.setDefinitions(definitions);

    Case caseElement = modelInstance.newInstance(Case.class);
    caseElement.setId("a-case");
    definitions.addChildElement(caseElement);

    CasePlanModel casePlanModel = modelInstance.newInstance(CasePlanModel.class);
    caseElement.setCasePlanModel(casePlanModel);

    Cmmn.writeModelToStream(System.out, modelInstance);

    return modelInstance;
  }

  @Test
  public void testDeployAndGetCaseDefinition() throws Exception {
    // given case model
    final CmmnModelInstance modelInstance = createCmmnModelInstance();

    // when case model is deployed
    DeploymentWithDefinitions deployment = testRule.deploy(repositoryService.createDeployment()
      .addModelInstance("foo.cmmn", modelInstance));

    // then deployment contains deployed case definition
    List<CaseDefinition> deployedCaseDefinitions = deployment.getDeployedCaseDefinitions();
    assertEquals(1, deployedCaseDefinitions.size());
    assertNull(deployment.getDeployedProcessDefinitions());
    assertNull(deployment.getDeployedDecisionDefinitions());
    assertNull(deployment.getDeployedDecisionRequirementsDefinitions());

    // and persisted case definition is equal to deployed case definition
    CaseDefinition persistedCaseDefinition = repositoryService.createCaseDefinitionQuery().caseDefinitionResourceName("foo.cmmn").singleResult();
    assertEquals(persistedCaseDefinition.getId(), deployedCaseDefinitions.get(0).getId());
  }

  @Test
  public void testDeployEmptyCaseDefinition() throws Exception {

    // given empty case model
    final CmmnModelInstance modelInstance = Cmmn.createEmptyModel();
    org.camunda.bpm.model.cmmn.instance.Definitions definitions = modelInstance.newInstance(org.camunda.bpm.model.cmmn.instance.Definitions.class);
    definitions.setTargetNamespace("http://camunda.org/examples");
    modelInstance.setDefinitions(definitions);

    // when case model is deployed
    DeploymentWithDefinitions deployment = testRule.deploy(repositoryService.createDeployment()
      .addModelInstance("foo.cmmn", modelInstance));

    // then no case definition is deployed
    assertNull(deployment.getDeployedCaseDefinitions());

    // and there exist not persisted case definition
    assertNull(repositoryService.createCaseDefinitionQuery().caseDefinitionResourceName("foo.cmmn").singleResult());
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testDeployCaseDefinitionWithIntegerHistoryTimeToLive.cmmn")
  @Test
  public void testDeployCaseDefinitionWithIntegerHistoryTimeToLive() {
    CaseDefinition caseDefinition = repositoryService.createCaseDefinitionQuery().singleResult();
    Integer historyTimeToLive = caseDefinition.getHistoryTimeToLive();
    assertNotNull(historyTimeToLive);
    assertEquals((int) historyTimeToLive, 5);
  }

  @Deployment(resources = "org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testDeployCaseDefinitionWithStringHistoryTimeToLive.cmmn")
  @Test
  public void testDeployCaseDefinitionWithStringHistoryTimeToLive() {
    CaseDefinition caseDefinition = repositoryService.createCaseDefinitionQuery().singleResult();
    Integer historyTimeToLive = caseDefinition.getHistoryTimeToLive();
    assertNotNull(historyTimeToLive);
    assertEquals((int) historyTimeToLive, 5);
  }

  @Test
  public void testDeployCaseDefinitionWithMalformedHistoryTimeToLive() {
    try {
     testRule.deploy("org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testDeployCaseDefinitionWithMalformedHistoryTimeToLive.cmmn");
      fail("Exception expected");
    } catch (ProcessEngineException e) {
      assertTrue(e.getCause().getMessage().contains("Cannot parse historyTimeToLive"));
    }
  }
}
