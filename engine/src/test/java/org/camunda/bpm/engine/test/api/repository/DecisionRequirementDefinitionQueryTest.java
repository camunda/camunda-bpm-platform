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

package org.camunda.bpm.engine.test.api.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.DecisionRequirementDefinition;
import org.camunda.bpm.engine.repository.DecisionRequirementDefinitionQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class DecisionRequirementDefinitionQueryTest {

  protected static final String DRD_SCORE_RESOURCE = "org/camunda/bpm/engine/test/dmn/deployment/drdScore.dmn11.xml";
  protected static final String DRD_DISH_RESOURCE = "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected RepositoryService repositoryService;

  protected String decisionRequirementDefinitionId;
  protected String firstDeploymentId;
  protected String secondDeploymentId;

  @Before
  public void init() {
    repositoryService = engineRule.getRepositoryService();

    firstDeploymentId = testRule.deploy(DRD_DISH_RESOURCE, DRD_SCORE_RESOURCE).getId();
    secondDeploymentId = testRule.deploy(DRD_DISH_RESOURCE).getId();

    decisionRequirementDefinitionId = repositoryService
        .createDecisionRequirementDefinitionQuery()
        .decisionRequirementDefinitionKey("score")
        .singleResult()
        .getId();
  }

  @Test
  public void queryByDecisionRequirementDefinitionId() {
    DecisionRequirementDefinitionQuery query = repositoryService.createDecisionRequirementDefinitionQuery();

    assertThat(query.decisionRequirementDefinitionId("notExisting").count(), is(0L));

    assertThat(query.decisionRequirementDefinitionId(decisionRequirementDefinitionId).count(), is(1L));
    assertThat(query.singleResult().getKey(), is("score"));
  }

  @Test
  public void queryByDecisionRequirementDefinitionIds() {
    DecisionRequirementDefinitionQuery query = repositoryService.createDecisionRequirementDefinitionQuery();

    assertThat(query.decisionRequirementDefinitionIdIn("not", "existing").count(), is(0L));

    assertThat(query.decisionRequirementDefinitionIdIn(decisionRequirementDefinitionId, "notExisting").count(), is(1L));
    assertThat(query.singleResult().getKey(), is("score"));
  }

  @Test
  public void queryByDecisionRequirementDefinitionKey() {
    DecisionRequirementDefinitionQuery query = repositoryService.createDecisionRequirementDefinitionQuery();

    assertThat(query.decisionRequirementDefinitionKey("notExisting").count(), is(0L));

    assertThat(query.decisionRequirementDefinitionKey("score").count(), is(1L));
    assertThat(query.singleResult().getKey(), is("score"));
  }

  @Test
  public void queryByDecisionRequirementDefinitionKeyLike() {
    DecisionRequirementDefinitionQuery query = repositoryService.createDecisionRequirementDefinitionQuery();

    assertThat(query.decisionRequirementDefinitionKeyLike("%notExisting%").count(), is(0L));

    assertThat(query.decisionRequirementDefinitionKeyLike("%sco%").count(), is(1L));
    assertThat(query.decisionRequirementDefinitionKeyLike("%dis%").count(), is(2L));
    assertThat(query.decisionRequirementDefinitionKeyLike("%s%").count(), is(3L));
  }

  @Test
  public void queryByDecisionRequirementDefinitionName() {
    DecisionRequirementDefinitionQuery query = repositoryService.createDecisionRequirementDefinitionQuery();

    assertThat(query.decisionRequirementDefinitionName("notExisting").count(), is(0L));

    assertThat(query.decisionRequirementDefinitionName("Score").count(), is(1L));
    assertThat(query.singleResult().getKey(), is("score"));
  }

  @Test
  public void queryByDecisionRequirementDefinitionNameLike() {
    DecisionRequirementDefinitionQuery query = repositoryService.createDecisionRequirementDefinitionQuery();

    assertThat(query.decisionRequirementDefinitionNameLike("%notExisting%").count(), is(0L));

    assertThat(query.decisionRequirementDefinitionNameLike("%Sco%").count(), is(1L));
    assertThat(query.decisionRequirementDefinitionNameLike("%ish%").count(), is(2L));
  }

  @Test
  public void queryByDecisionRequirementDefinitionCategory() {
    DecisionRequirementDefinitionQuery query = repositoryService.createDecisionRequirementDefinitionQuery();

    assertThat(query.decisionRequirementDefinitionCategory("notExisting").count(), is(0L));

    assertThat(query.decisionRequirementDefinitionCategory("test-drd-1").count(), is(1L));
    assertThat(query.singleResult().getKey(), is("score"));
  }

  @Test
  public void queryByDecisionRequirementDefinitionCategoryLike() {
    DecisionRequirementDefinitionQuery query = repositoryService.createDecisionRequirementDefinitionQuery();

    assertThat(query.decisionRequirementDefinitionCategoryLike("%notExisting%").count(), is(0L));

    assertThat(query.decisionRequirementDefinitionCategoryLike("%test%").count(), is(3L));
  }

  @Test
  public void queryByResourceName() {
    DecisionRequirementDefinitionQuery query = repositoryService.createDecisionRequirementDefinitionQuery();

    assertThat(query.decisionRequirementDefinitionResourceName("notExisting").count(), is(0L));

    assertThat(query.decisionRequirementDefinitionResourceName(DRD_SCORE_RESOURCE).count(), is(1L));
    assertThat(query.singleResult().getKey(), is("score"));
  }

  @Test
  public void queryByResourceNameLike() {
    DecisionRequirementDefinitionQuery query = repositoryService.createDecisionRequirementDefinitionQuery();

    assertThat(query.decisionRequirementDefinitionResourceNameLike("%notExisting%").count(), is(0L));

    assertThat(query.decisionRequirementDefinitionResourceNameLike("%.dmn11.xml%").count(), is(3L));
  }

  @Test
  public void queryByVersion() {
    DecisionRequirementDefinitionQuery query = repositoryService.createDecisionRequirementDefinitionQuery();

    assertThat(query.decisionRequirementDefinitionVersion(1).count(), is(2L));
    assertThat(query.decisionRequirementDefinitionVersion(2).count(), is(1L));
    assertThat(query.decisionRequirementDefinitionVersion(3).count(), is(0L));
  }

  @Test
  public void queryByLatest() {
    DecisionRequirementDefinitionQuery query = repositoryService.createDecisionRequirementDefinitionQuery();

    assertThat(query.latestVersion().count(), is(2L));
    assertThat(query.decisionRequirementDefinitionKey("score").latestVersion().count(), is(1L));
  }

  @Test
  public void queryByDeploymentId() {
    DecisionRequirementDefinitionQuery query = repositoryService.createDecisionRequirementDefinitionQuery();

    assertThat(query.deploymentId("notExisting").count(), is(0L));

    assertThat(query.deploymentId(firstDeploymentId).count(), is(2L));
    assertThat(query.deploymentId(secondDeploymentId).count(), is(1L));
  }

  @Test
  public void orderByDecisionRequirementDefinitionId() {
    List<DecisionRequirementDefinition> decisionRequirementDefinitions = repositoryService.createDecisionRequirementDefinitionQuery()
        .orderByDecisionRequirementDefinitionId().asc().list();

    assertThat(decisionRequirementDefinitions.size(), is(3));
    assertThat(decisionRequirementDefinitions.get(0).getId(), startsWith("dish:1"));
    assertThat(decisionRequirementDefinitions.get(1).getId(), startsWith("dish:2"));
    assertThat(decisionRequirementDefinitions.get(2).getId(), startsWith("score:1"));

    decisionRequirementDefinitions = repositoryService.createDecisionRequirementDefinitionQuery()
        .orderByDecisionRequirementDefinitionId().desc().list();

    assertThat(decisionRequirementDefinitions.get(0).getId(), startsWith("score:1"));
    assertThat(decisionRequirementDefinitions.get(1).getId(), startsWith("dish:2"));
    assertThat(decisionRequirementDefinitions.get(2).getId(), startsWith("dish:1"));
  }

  @Test
  public void orderByDecisionRequirementDefinitionKey() {
    List<DecisionRequirementDefinition> decisionRequirementDefinitions = repositoryService.createDecisionRequirementDefinitionQuery()
        .orderByDecisionRequirementDefinitionKey().asc().list();

    assertThat(decisionRequirementDefinitions.size(), is(3));
    assertThat(decisionRequirementDefinitions.get(0).getKey(), is("dish"));
    assertThat(decisionRequirementDefinitions.get(1).getKey(), is("dish"));
    assertThat(decisionRequirementDefinitions.get(2).getKey(), is("score"));

    decisionRequirementDefinitions = repositoryService.createDecisionRequirementDefinitionQuery()
        .orderByDecisionRequirementDefinitionKey().desc().list();

    assertThat(decisionRequirementDefinitions.get(0).getKey(), is("score"));
    assertThat(decisionRequirementDefinitions.get(1).getKey(), is("dish"));
    assertThat(decisionRequirementDefinitions.get(2).getKey(), is("dish"));
  }

  @Test
  public void orderByDecisionRequirementDefinitionName() {
    List<DecisionRequirementDefinition> decisionRequirementDefinitions = repositoryService.createDecisionRequirementDefinitionQuery()
        .orderByDecisionRequirementDefinitionName().asc().list();

    assertThat(decisionRequirementDefinitions.size(), is(3));
    assertThat(decisionRequirementDefinitions.get(0).getName(), is("Dish"));
    assertThat(decisionRequirementDefinitions.get(1).getName(), is("Dish"));
    assertThat(decisionRequirementDefinitions.get(2).getName(), is("Score"));

    decisionRequirementDefinitions = repositoryService.createDecisionRequirementDefinitionQuery()
        .orderByDecisionRequirementDefinitionName().desc().list();

    assertThat(decisionRequirementDefinitions.get(0).getName(), is("Score"));
    assertThat(decisionRequirementDefinitions.get(1).getName(), is("Dish"));
    assertThat(decisionRequirementDefinitions.get(2).getName(), is("Dish"));
  }

  @Test
  public void orderByDecisionRequirementDefinitionCategory() {
    List<DecisionRequirementDefinition> decisionRequirementDefinitions = repositoryService.createDecisionRequirementDefinitionQuery()
        .orderByDecisionRequirementDefinitionCategory().asc().list();

    assertThat(decisionRequirementDefinitions.size(), is(3));
    assertThat(decisionRequirementDefinitions.get(0).getCategory(), is("test-drd-1"));
    assertThat(decisionRequirementDefinitions.get(1).getCategory(), is("test-drd-2"));
    assertThat(decisionRequirementDefinitions.get(2).getCategory(), is("test-drd-2"));

    decisionRequirementDefinitions = repositoryService.createDecisionRequirementDefinitionQuery()
        .orderByDecisionRequirementDefinitionCategory().desc().list();

    assertThat(decisionRequirementDefinitions.get(0).getCategory(), is("test-drd-2"));
    assertThat(decisionRequirementDefinitions.get(1).getCategory(), is("test-drd-2"));
    assertThat(decisionRequirementDefinitions.get(2).getCategory(), is("test-drd-1"));
  }

  @Test
  public void orderByDecisionRequirementDefinitionVersion() {
    List<DecisionRequirementDefinition> decisionRequirementDefinitions = repositoryService.createDecisionRequirementDefinitionQuery()
        .orderByDecisionRequirementDefinitionVersion().asc().list();

    assertThat(decisionRequirementDefinitions.size(), is(3));
    assertThat(decisionRequirementDefinitions.get(0).getVersion(), is(1));
    assertThat(decisionRequirementDefinitions.get(1).getVersion(), is(1));
    assertThat(decisionRequirementDefinitions.get(2).getVersion(), is(2));

    decisionRequirementDefinitions = repositoryService.createDecisionRequirementDefinitionQuery()
        .orderByDecisionRequirementDefinitionVersion().desc().list();

    assertThat(decisionRequirementDefinitions.get(0).getVersion(), is(2));
    assertThat(decisionRequirementDefinitions.get(1).getVersion(), is(1));
    assertThat(decisionRequirementDefinitions.get(2).getVersion(), is(1));
  }

  @Test
  public void orderByDeploymentId() {
    List<DecisionRequirementDefinition> decisionRequirementDefinitions = repositoryService.createDecisionRequirementDefinitionQuery()
        .orderByDeploymentId().asc().list();

    assertThat(decisionRequirementDefinitions.size(), is(3));
    assertThat(decisionRequirementDefinitions.get(0).getDeploymentId(), is(firstDeploymentId));
    assertThat(decisionRequirementDefinitions.get(1).getDeploymentId(), is(firstDeploymentId));
    assertThat(decisionRequirementDefinitions.get(2).getDeploymentId(), is(secondDeploymentId));

    decisionRequirementDefinitions = repositoryService.createDecisionRequirementDefinitionQuery()
        .orderByDeploymentId().desc().list();

    assertThat(decisionRequirementDefinitions.get(0).getDeploymentId(), is(secondDeploymentId));
    assertThat(decisionRequirementDefinitions.get(1).getDeploymentId(), is(firstDeploymentId));
    assertThat(decisionRequirementDefinitions.get(2).getDeploymentId(), is(firstDeploymentId));
  }

}
