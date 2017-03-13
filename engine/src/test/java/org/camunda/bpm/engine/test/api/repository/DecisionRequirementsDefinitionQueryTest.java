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

    assertThat(query.decisionRequirementsDefinitionId("notExisting").count(), is(0L));

    assertThat(query.decisionRequirementsDefinitionId(decisionRequirementsDefinitionId).count(), is(1L));
    assertThat(query.singleResult().getKey(), is("score"));
  }

  @Test
  public void queryByDecisionRequirementsDefinitionIds() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionIdIn("not", "existing").count(), is(0L));

    assertThat(query.decisionRequirementsDefinitionIdIn(decisionRequirementsDefinitionId, "notExisting").count(), is(1L));
    assertThat(query.singleResult().getKey(), is("score"));
  }

  @Test
  public void queryByDecisionRequirementsDefinitionKey() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionKey("notExisting").count(), is(0L));

    assertThat(query.decisionRequirementsDefinitionKey("score").count(), is(1L));
    assertThat(query.singleResult().getKey(), is("score"));
  }

  @Test
  public void queryByDecisionRequirementsDefinitionKeyLike() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionKeyLike("%notExisting%").count(), is(0L));

    assertThat(query.decisionRequirementsDefinitionKeyLike("%sco%").count(), is(1L));
    assertThat(query.decisionRequirementsDefinitionKeyLike("%dis%").count(), is(2L));
    assertThat(query.decisionRequirementsDefinitionKeyLike("%s%").count(), is(3L));
  }

  @Test
  public void queryByDecisionRequirementsDefinitionName() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionName("notExisting").count(), is(0L));

    assertThat(query.decisionRequirementsDefinitionName("Score").count(), is(1L));
    assertThat(query.singleResult().getKey(), is("score"));
  }

  @Test
  public void queryByDecisionRequirementsDefinitionNameLike() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionNameLike("%notExisting%").count(), is(0L));

    assertThat(query.decisionRequirementsDefinitionNameLike("%Sco%").count(), is(1L));
    assertThat(query.decisionRequirementsDefinitionNameLike("%ish%").count(), is(2L));
  }

  @Test
  public void queryByDecisionRequirementsDefinitionCategory() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionCategory("notExisting").count(), is(0L));

    assertThat(query.decisionRequirementsDefinitionCategory("test-drd-1").count(), is(1L));
    assertThat(query.singleResult().getKey(), is("score"));
  }

  @Test
  public void queryByDecisionRequirementsDefinitionCategoryLike() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionCategoryLike("%notExisting%").count(), is(0L));

    assertThat(query.decisionRequirementsDefinitionCategoryLike("%test%").count(), is(3L));

    assertThat(query.decisionRequirementsDefinitionCategoryLike("%z\\_").count(), is(1L));
  }

  @Test
  public void queryByResourceName() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionResourceName("notExisting").count(), is(0L));

    assertThat(query.decisionRequirementsDefinitionResourceName(DRD_SCORE_RESOURCE).count(), is(1L));
    assertThat(query.singleResult().getKey(), is("score"));
  }

  @Test
  public void queryByResourceNameLike() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionResourceNameLike("%notExisting%").count(), is(0L));

    assertThat(query.decisionRequirementsDefinitionResourceNameLike("%.dmn11.xml%").count(), is(4L));
  }

  @Test
  public void queryByResourceNameLikeEscape() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionResourceNameLike("%z\\_.%").count(), is(1L));
  }

  @Test
  public void queryByVersion() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.decisionRequirementsDefinitionVersion(1).count(), is(3L));
    assertThat(query.decisionRequirementsDefinitionVersion(2).count(), is(1L));
    assertThat(query.decisionRequirementsDefinitionVersion(3).count(), is(0L));
  }

  @Test
  public void queryByLatest() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.latestVersion().count(), is(3L));
    assertThat(query.decisionRequirementsDefinitionKey("score").latestVersion().count(), is(1L));
  }

  @Test
  public void queryByDeploymentId() {
    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.deploymentId("notExisting").count(), is(0L));

    assertThat(query.deploymentId(firstDeploymentId).count(), is(2L));
    assertThat(query.deploymentId(secondDeploymentId).count(), is(1L));
  }

  @Test
  public void orderByDecisionRequirementsDefinitionId() {
    List<DecisionRequirementsDefinition> decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDecisionRequirementsDefinitionId().asc().list();

    assertThat(decisionRequirementsDefinitions.size(), is(4));
    assertThat(decisionRequirementsDefinitions.get(0).getId(), startsWith("dish:1"));
    assertThat(decisionRequirementsDefinitions.get(1).getId(), startsWith("dish:2"));
    assertThat(decisionRequirementsDefinitions.get(2).getId(), startsWith("score:1"));
    assertThat(decisionRequirementsDefinitions.get(3).getId(), startsWith("xyz:1"));

    decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDecisionRequirementsDefinitionId().desc().list();

    assertThat(decisionRequirementsDefinitions.get(0).getId(), startsWith("xyz:1"));
    assertThat(decisionRequirementsDefinitions.get(1).getId(), startsWith("score:1"));
    assertThat(decisionRequirementsDefinitions.get(2).getId(), startsWith("dish:2"));
    assertThat(decisionRequirementsDefinitions.get(3).getId(), startsWith("dish:1"));
  }

  @Test
  public void orderByDecisionRequirementsDefinitionKey() {
    List<DecisionRequirementsDefinition> decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDecisionRequirementsDefinitionKey().asc().list();

    assertThat(decisionRequirementsDefinitions.size(), is(4));
    assertThat(decisionRequirementsDefinitions.get(0).getKey(), is("dish"));
    assertThat(decisionRequirementsDefinitions.get(1).getKey(), is("dish"));
    assertThat(decisionRequirementsDefinitions.get(2).getKey(), is("score"));
    assertThat(decisionRequirementsDefinitions.get(3).getKey(), is("xyz"));

    decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDecisionRequirementsDefinitionKey().desc().list();

    assertThat(decisionRequirementsDefinitions.get(0).getKey(), is("xyz"));
    assertThat(decisionRequirementsDefinitions.get(1).getKey(), is("score"));
    assertThat(decisionRequirementsDefinitions.get(2).getKey(), is("dish"));
    assertThat(decisionRequirementsDefinitions.get(3).getKey(), is("dish"));
  }

  @Test
  public void orderByDecisionRequirementsDefinitionName() {
    List<DecisionRequirementsDefinition> decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDecisionRequirementsDefinitionName().asc().list();

    assertThat(decisionRequirementsDefinitions.size(), is(4));
    assertThat(decisionRequirementsDefinitions.get(0).getName(), is("Dish"));
    assertThat(decisionRequirementsDefinitions.get(1).getName(), is("Dish"));
    assertThat(decisionRequirementsDefinitions.get(2).getName(), is("Score"));
    assertThat(decisionRequirementsDefinitions.get(3).getName(), is("Xyz"));

    decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDecisionRequirementsDefinitionName().desc().list();

    assertThat(decisionRequirementsDefinitions.get(0).getName(), is("Xyz"));
    assertThat(decisionRequirementsDefinitions.get(1).getName(), is("Score"));
    assertThat(decisionRequirementsDefinitions.get(2).getName(), is("Dish"));
    assertThat(decisionRequirementsDefinitions.get(3).getName(), is("Dish"));
  }

  @Test
  public void orderByDecisionRequirementsDefinitionCategory() {
    List<DecisionRequirementsDefinition> decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDecisionRequirementsDefinitionCategory().asc().list();

    assertThat(decisionRequirementsDefinitions.size(), is(4));
    assertThat(decisionRequirementsDefinitions.get(0).getCategory(), is("test-drd-1"));
    assertThat(decisionRequirementsDefinitions.get(1).getCategory(), is("test-drd-2"));
    assertThat(decisionRequirementsDefinitions.get(2).getCategory(), is("test-drd-2"));
    assertThat(decisionRequirementsDefinitions.get(3).getCategory(), is("xyz_"));

    decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDecisionRequirementsDefinitionCategory().desc().list();

    assertThat(decisionRequirementsDefinitions.get(0).getCategory(), is("xyz_"));
    assertThat(decisionRequirementsDefinitions.get(1).getCategory(), is("test-drd-2"));
    assertThat(decisionRequirementsDefinitions.get(2).getCategory(), is("test-drd-2"));
    assertThat(decisionRequirementsDefinitions.get(3).getCategory(), is("test-drd-1"));
  }

  @Test
  public void orderByDecisionRequirementsDefinitionVersion() {
    List<DecisionRequirementsDefinition> decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDecisionRequirementsDefinitionVersion().asc().list();

    assertThat(decisionRequirementsDefinitions.size(), is(4));
    assertThat(decisionRequirementsDefinitions.get(0).getVersion(), is(1));
    assertThat(decisionRequirementsDefinitions.get(1).getVersion(), is(1));
    assertThat(decisionRequirementsDefinitions.get(2).getVersion(), is(1));
    assertThat(decisionRequirementsDefinitions.get(3).getVersion(), is(2));

    decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDecisionRequirementsDefinitionVersion().desc().list();

    assertThat(decisionRequirementsDefinitions.get(0).getVersion(), is(2));
    assertThat(decisionRequirementsDefinitions.get(1).getVersion(), is(1));
    assertThat(decisionRequirementsDefinitions.get(2).getVersion(), is(1));
  }

  @Test
  public void orderByDeploymentId() {
    List<DecisionRequirementsDefinition> decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDeploymentId().asc().list();

    assertThat(decisionRequirementsDefinitions.size(), is(4));
    assertThat(decisionRequirementsDefinitions.get(0).getDeploymentId(), is(firstDeploymentId));
    assertThat(decisionRequirementsDefinitions.get(1).getDeploymentId(), is(firstDeploymentId));
    assertThat(decisionRequirementsDefinitions.get(2).getDeploymentId(), is(secondDeploymentId));
    assertThat(decisionRequirementsDefinitions.get(3).getDeploymentId(), is(thirdDeploymentId));

    decisionRequirementsDefinitions = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDeploymentId().desc().list();

    assertThat(decisionRequirementsDefinitions.get(0).getDeploymentId(), is(thirdDeploymentId));
    assertThat(decisionRequirementsDefinitions.get(1).getDeploymentId(), is(secondDeploymentId));
    assertThat(decisionRequirementsDefinitions.get(2).getDeploymentId(), is(firstDeploymentId));
    assertThat(decisionRequirementsDefinitions.get(3).getDeploymentId(), is(firstDeploymentId));
  }

}
