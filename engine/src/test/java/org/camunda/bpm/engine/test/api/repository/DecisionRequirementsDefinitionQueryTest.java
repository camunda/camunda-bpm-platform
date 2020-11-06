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
package org.camunda.bpm.engine.test.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinitionQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class DecisionRequirementsDefinitionQueryTest {

  protected static final String DRD_SCORE_RESOURCE = "org/camunda/bpm/engine/test/dmn/deployment/drdScore.dmn11.xml";
  protected static final String DRD_DISH_RESOURCE = "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml";
  protected static final String DRD_XYZ_RESOURCE = "org/camunda/bpm/engine/test/api/repository/drdXyz_.dmn11.xml";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RepositoryService repositoryService;

  protected String decisionRequirementsDefinitionId;
  protected String firstDeploymentId;
  protected String secondDeploymentId;
  protected String thirdDeploymentId;

  @Before
  public void init() {
    repositoryService = engineRule.getRepositoryService();

    firstDeploymentId = testRule.deploy(DRD_DISH_RESOURCE, DRD_SCORE_RESOURCE).getId();
    secondDeploymentId = testRule.deploy(DRD_DISH_RESOURCE).getId();
    thirdDeploymentId = testRule.deploy(DRD_XYZ_RESOURCE).getId();

    decisionRequirementsDefinitionId = repositoryService
        .createDecisionRequirementsDefinitionQuery()
        .decisionRequirementsDefinitionKey("score")
        .singleResult()
        .getId();
  }

  @Test
  public void queryByDecisionRequirementsDefinitionId() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionId("notExisting").count()).isEqualTo(0L);

    assertThat(query.decisionRequirementsDefinitionId(decisionRequirementsDefinitionId).count()).isEqualTo(1L);
    assertThat(query.singleResult().getKey()).isEqualTo("score");
  }

  @Test
  public void queryByDecisionRequirementsDefinitionIds() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionIdIn("not", "existing").count()).isEqualTo(0L);

    assertThat(query.decisionRequirementsDefinitionIdIn(decisionRequirementsDefinitionId, "notExisting").count()).isEqualTo(1L);
    assertThat(query.singleResult().getKey()).isEqualTo("score");
  }

  @Test
  public void queryByDecisionRequirementsDefinitionKey() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionKey("notExisting").count()).isEqualTo(0L);

    assertThat(query.decisionRequirementsDefinitionKey("score").count()).isEqualTo(1L);
    assertThat(query.singleResult().getKey()).isEqualTo("score");
  }

  @Test
  public void queryByDecisionRequirementsDefinitionKeyLike() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionKeyLike("%notExisting%").count()).isEqualTo(0L);

    assertThat(query.decisionRequirementsDefinitionKeyLike("%sco%").count()).isEqualTo(1L);
    assertThat(query.decisionRequirementsDefinitionKeyLike("%dis%").count()).isEqualTo(2L);
    assertThat(query.decisionRequirementsDefinitionKeyLike("%s%").count()).isEqualTo(3L);
  }

  @Test
  public void queryByDecisionRequirementsDefinitionName() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionName("notExisting").count()).isEqualTo(0L);

    assertThat(query.decisionRequirementsDefinitionName("Score").count()).isEqualTo(1L);
    assertThat(query.singleResult().getKey()).isEqualTo("score");
  }

  @Test
  public void queryByDecisionRequirementsDefinitionNameLike() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionNameLike("%notExisting%").count()).isEqualTo(0L);

    assertThat(query.decisionRequirementsDefinitionNameLike("%Sco%").count()).isEqualTo(1L);
    assertThat(query.decisionRequirementsDefinitionNameLike("%ish%").count()).isEqualTo(2L);
  }

  @Test
  public void queryByDecisionRequirementsDefinitionCategory() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionCategory("notExisting").count()).isEqualTo(0L);

    assertThat(query.decisionRequirementsDefinitionCategory("test-drd-1").count()).isEqualTo(1L);
    assertThat(query.singleResult().getKey()).isEqualTo("score");
  }

  @Test
  public void queryByDecisionRequirementsDefinitionCategoryLike() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionCategoryLike("%notExisting%").count()).isEqualTo(0L);

    assertThat(query.decisionRequirementsDefinitionCategoryLike("%test%").count()).isEqualTo(3L);

    assertThat(query.decisionRequirementsDefinitionCategoryLike("%z\\_").count()).isEqualTo(1L);
  }

  @Test
  public void queryByResourceName() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionResourceName("notExisting").count()).isEqualTo(0L);

    assertThat(query.decisionRequirementsDefinitionResourceName(DRD_SCORE_RESOURCE).count()).isEqualTo(1L);
    assertThat(query.singleResult().getKey()).isEqualTo("score");
  }

  @Test
  public void queryByResourceNameLike() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionResourceNameLike("%notExisting%").count()).isEqualTo(0L);

    assertThat(query.decisionRequirementsDefinitionResourceNameLike("%.dmn11.xml%").count()).isEqualTo(4L);
  }

  @Test
  public void queryByResourceNameLikeEscape() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionResourceNameLike("%z\\_.%").count()).isEqualTo(1L);
  }

  @Test
  public void queryByVersion() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionVersion(1).count()).isEqualTo(3L);
    assertThat(query.decisionRequirementsDefinitionVersion(2).count()).isEqualTo(1L);
    assertThat(query.decisionRequirementsDefinitionVersion(3).count()).isEqualTo(0L);
  }

  @Test
  public void queryByLatest() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.latestVersion().count()).isEqualTo(3L);
    assertThat(query.decisionRequirementsDefinitionKey("score").latestVersion().count()).isEqualTo(1L);
  }

  @Test
  public void queryByDeploymentId() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.deploymentId("notExisting").count()).isEqualTo(0L);

    assertThat(query.deploymentId(firstDeploymentId).count()).isEqualTo(2L);
    assertThat(query.deploymentId(secondDeploymentId).count()).isEqualTo(1L);
  }

  @Test
  public void orderByDecisionRequirementsDefinitionId() {
    List<DecisionRequirementsDefinition> decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDecisionRequirementsDefinitionId().asc().list();

    assertThat(decisionRequirementsDefinitions.size()).isEqualTo(4);
    assertThat(decisionRequirementsDefinitions.get(0).getId()).startsWith("dish:1");
    assertThat(decisionRequirementsDefinitions.get(1).getId()).startsWith("dish:2");
    assertThat(decisionRequirementsDefinitions.get(2).getId()).startsWith("score:1");
    assertThat(decisionRequirementsDefinitions.get(3).getId()).startsWith("xyz:1");

    decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDecisionRequirementsDefinitionId().desc().list();

    assertThat(decisionRequirementsDefinitions.get(0).getId()).startsWith("xyz:1");
    assertThat(decisionRequirementsDefinitions.get(1).getId()).startsWith("score:1");
    assertThat(decisionRequirementsDefinitions.get(2).getId()).startsWith("dish:2");
    assertThat(decisionRequirementsDefinitions.get(3).getId()).startsWith("dish:1");
  }

  @Test
  public void orderByDecisionRequirementsDefinitionKey() {
    List<DecisionRequirementsDefinition> decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDecisionRequirementsDefinitionKey().asc().list();

    assertThat(decisionRequirementsDefinitions.size()).isEqualTo(4);
    assertThat(decisionRequirementsDefinitions.get(0).getKey()).isEqualTo("dish");
    assertThat(decisionRequirementsDefinitions.get(1).getKey()).isEqualTo("dish");
    assertThat(decisionRequirementsDefinitions.get(2).getKey()).isEqualTo("score");
    assertThat(decisionRequirementsDefinitions.get(3).getKey()).isEqualTo("xyz");

    decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDecisionRequirementsDefinitionKey().desc().list();

    assertThat(decisionRequirementsDefinitions.get(0).getKey()).isEqualTo("xyz");
    assertThat(decisionRequirementsDefinitions.get(1).getKey()).isEqualTo("score");
    assertThat(decisionRequirementsDefinitions.get(2).getKey()).isEqualTo("dish");
    assertThat(decisionRequirementsDefinitions.get(3).getKey()).isEqualTo("dish");
  }

  @Test
  public void orderByDecisionRequirementsDefinitionName() {
    List<DecisionRequirementsDefinition> decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDecisionRequirementsDefinitionName().asc().list();

    assertThat(decisionRequirementsDefinitions.size()).isEqualTo(4);
    assertThat(decisionRequirementsDefinitions.get(0).getName()).isEqualTo("Dish");
    assertThat(decisionRequirementsDefinitions.get(1).getName()).isEqualTo("Dish");
    assertThat(decisionRequirementsDefinitions.get(2).getName()).isEqualTo("Score");
    assertThat(decisionRequirementsDefinitions.get(3).getName()).isEqualTo("Xyz");

    decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDecisionRequirementsDefinitionName().desc().list();

    assertThat(decisionRequirementsDefinitions.get(0).getName()).isEqualTo("Xyz");
    assertThat(decisionRequirementsDefinitions.get(1).getName()).isEqualTo("Score");
    assertThat(decisionRequirementsDefinitions.get(2).getName()).isEqualTo("Dish");
    assertThat(decisionRequirementsDefinitions.get(3).getName()).isEqualTo("Dish");
  }

  @Test
  public void orderByDecisionRequirementsDefinitionCategory() {
    List<DecisionRequirementsDefinition> decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDecisionRequirementsDefinitionCategory().asc().list();

    assertThat(decisionRequirementsDefinitions.size()).isEqualTo(4);
    assertThat(decisionRequirementsDefinitions.get(0).getCategory()).isEqualTo("test-drd-1");
    assertThat(decisionRequirementsDefinitions.get(1).getCategory()).isEqualTo("test-drd-2");
    assertThat(decisionRequirementsDefinitions.get(2).getCategory()).isEqualTo("test-drd-2");
    assertThat(decisionRequirementsDefinitions.get(3).getCategory()).isEqualTo("xyz_");

    decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDecisionRequirementsDefinitionCategory().desc().list();

    assertThat(decisionRequirementsDefinitions.get(0).getCategory()).isEqualTo("xyz_");
    assertThat(decisionRequirementsDefinitions.get(1).getCategory()).isEqualTo("test-drd-2");
    assertThat(decisionRequirementsDefinitions.get(2).getCategory()).isEqualTo("test-drd-2");
    assertThat(decisionRequirementsDefinitions.get(3).getCategory()).isEqualTo("test-drd-1");
  }

  @Test
  public void orderByDecisionRequirementsDefinitionVersion() {
    List<DecisionRequirementsDefinition> decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDecisionRequirementsDefinitionVersion().asc().list();

    assertThat(decisionRequirementsDefinitions.size()).isEqualTo(4);
    assertThat(decisionRequirementsDefinitions.get(0).getVersion()).isEqualTo(1);
    assertThat(decisionRequirementsDefinitions.get(1).getVersion()).isEqualTo(1);
    assertThat(decisionRequirementsDefinitions.get(2).getVersion()).isEqualTo(1);
    assertThat(decisionRequirementsDefinitions.get(3).getVersion()).isEqualTo(2);

    decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDecisionRequirementsDefinitionVersion().desc().list();

    assertThat(decisionRequirementsDefinitions.get(0).getVersion()).isEqualTo(2);
    assertThat(decisionRequirementsDefinitions.get(1).getVersion()).isEqualTo(1);
    assertThat(decisionRequirementsDefinitions.get(2).getVersion()).isEqualTo(1);
  }

  @Test
  public void orderByDeploymentId() {
    List<DecisionRequirementsDefinition> decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDeploymentId().asc().list();

    assertThat(decisionRequirementsDefinitions.size()).isEqualTo(4);
    assertThat(decisionRequirementsDefinitions.get(0).getDeploymentId()).isEqualTo(firstDeploymentId);
    assertThat(decisionRequirementsDefinitions.get(1).getDeploymentId()).isEqualTo(firstDeploymentId);
    assertThat(decisionRequirementsDefinitions.get(2).getDeploymentId()).isEqualTo(secondDeploymentId);
    assertThat(decisionRequirementsDefinitions.get(3).getDeploymentId()).isEqualTo(thirdDeploymentId);

    decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDeploymentId().desc().list();

    assertThat(decisionRequirementsDefinitions.get(0).getDeploymentId()).isEqualTo(thirdDeploymentId);
    assertThat(decisionRequirementsDefinitions.get(1).getDeploymentId()).isEqualTo(secondDeploymentId);
    assertThat(decisionRequirementsDefinitions.get(2).getDeploymentId()).isEqualTo(firstDeploymentId);
    assertThat(decisionRequirementsDefinitions.get(3).getDeploymentId()).isEqualTo(firstDeploymentId);
  }

}
