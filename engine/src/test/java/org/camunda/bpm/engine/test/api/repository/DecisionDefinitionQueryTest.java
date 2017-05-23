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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinitionQuery;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class DecisionDefinitionQueryTest {

  protected static final String DMN_ONE_RESOURCE = "org/camunda/bpm/engine/test/repository/one.dmn";
  protected static final String DMN_TWO_RESOURCE = "org/camunda/bpm/engine/test/repository/two.dmn";
  protected static final String DMN_THREE_RESOURCE = "org/camunda/bpm/engine/test/api/repository/three_.dmn";

  protected static final String DRD_SCORE_RESOURCE = "org/camunda/bpm/engine/test/dmn/deployment/drdScore.dmn11.xml";
  protected static final String DRD_DISH_RESOURCE = "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml";

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

    firstDeploymentId = testRule.deploy(DMN_ONE_RESOURCE, DMN_TWO_RESOURCE).getId();
    secondDeploymentId = testRule.deploy(DMN_ONE_RESOURCE).getId();
    thirdDeploymentId = testRule.deploy(DMN_THREE_RESOURCE).getId();
  }

  @Test
  public void decisionDefinitionProperties() {
    List<DecisionDefinition> decisionDefinitions = repositoryService
      .createDecisionDefinitionQuery()
      .orderByDecisionDefinitionName().asc()
      .orderByDecisionDefinitionVersion().asc()
      .orderByDecisionDefinitionCategory()
      .asc()
      .list();

    DecisionDefinition decisionDefinition = decisionDefinitions.get(0);
    assertEquals("one", decisionDefinition.getKey());
    assertEquals("One", decisionDefinition.getName());
    assertTrue(decisionDefinition.getId().startsWith("one:1"));
    assertEquals("Examples", decisionDefinition.getCategory());
    assertEquals(1, decisionDefinition.getVersion());
    assertEquals("org/camunda/bpm/engine/test/repository/one.dmn", decisionDefinition.getResourceName());
    assertEquals(firstDeploymentId, decisionDefinition.getDeploymentId());

    decisionDefinition = decisionDefinitions.get(1);
    assertEquals("one", decisionDefinition.getKey());
    assertEquals("One", decisionDefinition.getName());
    assertTrue(decisionDefinition.getId().startsWith("one:2"));
    assertEquals("Examples", decisionDefinition.getCategory());
    assertEquals(2, decisionDefinition.getVersion());
    assertEquals("org/camunda/bpm/engine/test/repository/one.dmn", decisionDefinition.getResourceName());
    assertEquals(secondDeploymentId, decisionDefinition.getDeploymentId());

    decisionDefinition = decisionDefinitions.get(2);
    assertEquals("two", decisionDefinition.getKey());
    assertEquals("Two", decisionDefinition.getName());
    assertTrue(decisionDefinition.getId().startsWith("two:1"));
    assertEquals("Examples2", decisionDefinition.getCategory());
    assertEquals(1, decisionDefinition.getVersion());
    assertEquals("org/camunda/bpm/engine/test/repository/two.dmn", decisionDefinition.getResourceName());
    assertEquals(firstDeploymentId, decisionDefinition.getDeploymentId());
  }

  @Test
	public void queryByDecisionDefinitionIds() {
    // empty list
    assertTrue(repositoryService.createDecisionDefinitionQuery().decisionDefinitionIdIn("a", "b").list().isEmpty());

    // collect all ids
    List<DecisionDefinition> decisionDefinitions = repositoryService.createDecisionDefinitionQuery().list();
    List<String> ids = new ArrayList<String>();
    for (DecisionDefinition decisionDefinition : decisionDefinitions) {
      ids.add(decisionDefinition.getId());
    }

    decisionDefinitions = repositoryService.createDecisionDefinitionQuery()
      .decisionDefinitionIdIn(ids.toArray(new String[ids.size()]))
      .list();

    assertEquals(ids.size(), decisionDefinitions.size());
    for (DecisionDefinition decisionDefinition : decisionDefinitions) {
      if (!ids.contains(decisionDefinition.getId())) {
        fail("Expected to find decision definition "+ decisionDefinition);
      }
    }
  }

  @Test
	public void queryByDeploymentId() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .deploymentId(firstDeploymentId);

    verifyQueryResults(query, 2);
  }

  @Test
	public void queryByInvalidDeploymentId() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

   query
     .deploymentId("invalid");

    verifyQueryResults(query, 0);

    try {
      query.deploymentId(null);
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }
  }

  @Test
	public void queryByName() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .decisionDefinitionName("Two");

    verifyQueryResults(query, 1);

    query
      .decisionDefinitionName("One");

    verifyQueryResults(query, 2);
  }

  @Test
	public void queryByInvalidName() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .decisionDefinitionName("invalid");

    verifyQueryResults(query, 0);

    try {
      query.decisionDefinitionName(null);
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }
  }

  @Test
	public void queryByNameLike() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .decisionDefinitionNameLike("%w%");

    verifyQueryResults(query, 1);

    query.decisionDefinitionNameLike("%z\\_");

    verifyQueryResults(query, 1);
  }

  @Test
	public void queryByInvalidNameLike() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .decisionDefinitionNameLike("%invalid%");

    verifyQueryResults(query, 0);

    try {
      query.decisionDefinitionNameLike(null);
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }
  }

  @Test
  public void queryByResourceNameLike() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
        .decisionDefinitionResourceNameLike("%ree%");

    verifyQueryResults(query, 1);

    query.decisionDefinitionResourceNameLike("%ee\\_%");

    verifyQueryResults(query, 1);
  }

  @Test
  public void queryByInvalidNResourceNameLike() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
        .decisionDefinitionResourceNameLike("%invalid%");

    verifyQueryResults(query, 0);

    try {
      query.decisionDefinitionNameLike(null);
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }
  }

  @Test
	public void queryByKey() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    // decision one
    query
      .decisionDefinitionKey("one");

    verifyQueryResults(query, 2);

    // decision two
    query
      .decisionDefinitionKey("two");

    verifyQueryResults(query, 1);
  }

  @Test
	public void queryByInvalidKey() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .decisionDefinitionKey("invalid");

    verifyQueryResults(query, 0);

    try {
      query.decisionDefinitionKey(null);
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }
  }

  @Test
	public void queryByKeyLike() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .decisionDefinitionKeyLike("%o%");

    verifyQueryResults(query, 3);

    query.decisionDefinitionKeyLike("%z\\_");

    verifyQueryResults(query, 1);
  }

  @Test
	public void queryByInvalidKeyLike() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .decisionDefinitionKeyLike("%invalid%");

    verifyQueryResults(query, 0);

    try {
      query.decisionDefinitionKeyLike(null);
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }
  }

  @Test
	public void queryByCategory() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .decisionDefinitionCategory("Examples");

    verifyQueryResults(query, 2);
  }

  @Test
	public void queryByInvalidCategory() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .decisionDefinitionCategory("invalid");

    verifyQueryResults(query, 0);

    try {
      query.decisionDefinitionCategory(null);
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }
  }

  @Test
	public void queryByCategoryLike() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .decisionDefinitionCategoryLike("%Example%");

    verifyQueryResults(query, 3);

    query
      .decisionDefinitionCategoryLike("%amples2");

    verifyQueryResults(query, 1);

    query
        .decisionDefinitionCategoryLike("%z\\_");

    verifyQueryResults(query, 1);
  }

  @Test
	public void queryByInvalidCategoryLike() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .decisionDefinitionCategoryLike("invalid");

    verifyQueryResults(query, 0);

    try {
      query.decisionDefinitionCategoryLike(null);
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }
  }

  @Test
	public void queryByVersion() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .decisionDefinitionVersion(2);

    verifyQueryResults(query, 1);

    query
      .decisionDefinitionVersion(1);

    verifyQueryResults(query, 3);
  }

  @Test
	public void queryByInvalidVersion() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .decisionDefinitionVersion(3);

    verifyQueryResults(query, 0);

    try {
      query.decisionDefinitionVersion(-1);
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }

    try {
      query.decisionDefinitionVersion(null);
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }
  }

  @Test
	public void queryByLatest() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .latestVersion();

    verifyQueryResults(query, 3);

    query
      .decisionDefinitionKey("one")
      .latestVersion();

    verifyQueryResults(query, 1);

    query
      .decisionDefinitionKey("two").latestVersion();
    verifyQueryResults(query, 1);
  }

  public void testInvalidUsageOfLatest() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    try {
      query
        .decisionDefinitionId("test")
        .latestVersion()
        .list();
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }

    try {
      query
        .decisionDefinitionName("test")
        .latestVersion()
        .list();
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }

    try {
      query
        .decisionDefinitionNameLike("test")
        .latestVersion()
        .list();
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }

    try {
      query
        .decisionDefinitionVersion(1)
        .latestVersion()
        .list();
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }

    try {
      query
        .deploymentId("test")
        .latestVersion()
        .list();
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }
  }

  @Test
  public void queryByDecisionRequirementsDefinitionId() {
    testRule.deploy(DRD_DISH_RESOURCE, DRD_SCORE_RESOURCE);

    List<DecisionRequirementsDefinition> drds = repositoryService.createDecisionRequirementsDefinitionQuery()
        .orderByDecisionRequirementsDefinitionName().asc().list();

    String dishDrdId = drds.get(0).getId();
    String scoreDrdId = drds.get(1).getId();

    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    verifyQueryResults(query.decisionRequirementsDefinitionId("non existing"), 0);
    verifyQueryResults(query.decisionRequirementsDefinitionId(dishDrdId), 3);
    verifyQueryResults(query.decisionRequirementsDefinitionId(scoreDrdId), 2);
  }

  @Test
  public void queryByDecisionRequirementsDefinitionKey() {
    testRule.deploy(DRD_DISH_RESOURCE, DRD_SCORE_RESOURCE);

    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    verifyQueryResults(query.decisionRequirementsDefinitionKey("non existing"), 0);
    verifyQueryResults(query.decisionRequirementsDefinitionKey("dish"), 3);
    verifyQueryResults(query.decisionRequirementsDefinitionKey("score"), 2);
  }

  @Test
  public void queryByWithoutDecisionRequirementsDefinition() {
    testRule.deploy(DRD_DISH_RESOURCE, DRD_SCORE_RESOURCE);

    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    verifyQueryResults(query, 9);
    verifyQueryResults(query.withoutDecisionRequirementsDefinition(), 4);
  }

  @Test
	public void querySorting() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    // asc
    query
      .orderByDecisionDefinitionId()
      .asc();
    verifyQueryResults(query, 4);

    query = repositoryService.createDecisionDefinitionQuery();

    query
      .orderByDeploymentId()
      .asc();
    verifyQueryResults(query, 4);

    query = repositoryService.createDecisionDefinitionQuery();

    query
      .orderByDecisionDefinitionKey()
      .asc();
    verifyQueryResults(query, 4);

    query = repositoryService.createDecisionDefinitionQuery();

    query
      .orderByDecisionDefinitionVersion()
      .asc();
    verifyQueryResults(query, 4);

    // desc

    query = repositoryService.createDecisionDefinitionQuery();

    query
      .orderByDecisionDefinitionId()
      .desc();
    verifyQueryResults(query, 4);

    query = repositoryService.createDecisionDefinitionQuery();

    query
      .orderByDeploymentId()
      .desc();
    verifyQueryResults(query, 4);

    query = repositoryService.createDecisionDefinitionQuery();

    query
      .orderByDecisionDefinitionKey()
      .desc();
    verifyQueryResults(query, 4);

    query = repositoryService.createDecisionDefinitionQuery();

    query
      .orderByDecisionDefinitionVersion()
      .desc();
    verifyQueryResults(query, 4);

    query = repositoryService.createDecisionDefinitionQuery();

    // Typical use decision
    query
      .orderByDecisionDefinitionKey()
      .asc()
      .orderByDecisionDefinitionVersion()
      .desc();

    List<DecisionDefinition> decisionDefinitions = query.list();
    assertEquals(4, decisionDefinitions.size());

    assertEquals("one", decisionDefinitions.get(0).getKey());
    assertEquals(2, decisionDefinitions.get(0).getVersion());
    assertEquals("one", decisionDefinitions.get(1).getKey());
    assertEquals(1, decisionDefinitions.get(1).getVersion());
    assertEquals("two", decisionDefinitions.get(2).getKey());
    assertEquals(1, decisionDefinitions.get(2).getVersion());
  }


  protected void verifyQueryResults(DecisionDefinitionQuery query, int expectedCount) {
    assertEquals(expectedCount, query.count());
    assertEquals(expectedCount, query.list().size());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/api/repository/versionTag.dmn",
    "org/camunda/bpm/engine/test/api/repository/versionTagHigher.dmn" })
  @Test
  public void testQueryOrderByVersionTag() {
    List<DecisionDefinition> decisionDefinitionList = repositoryService
      .createDecisionDefinitionQuery()
      .versionTagLike("1%")
      .orderByVersionTag()
      .asc()
      .list();

    assertEquals("1.1.0", decisionDefinitionList.get(1).getVersionTag());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/api/repository/versionTag.dmn",
    "org/camunda/bpm/engine/test/api/repository/versionTagHigher.dmn" })
  @Test
  public void testQueryByVersionTag() {
    DecisionDefinition decisionDefinition = repositoryService
      .createDecisionDefinitionQuery()
      .versionTag("1.0.0")
      .singleResult();

    assertEquals("versionTag", decisionDefinition.getKey());
    assertEquals("1.0.0", decisionDefinition.getVersionTag());
  }

  @Deployment(resources = {
    "org/camunda/bpm/engine/test/api/repository/versionTag.dmn",
    "org/camunda/bpm/engine/test/api/repository/versionTagHigher.dmn" })
  @Test
  public void testQueryByVersionTagLike() {
    List<DecisionDefinition> decisionDefinitionList = repositoryService
    .createDecisionDefinitionQuery()
    .versionTagLike("1%")
    .list();

    assertEquals(2, decisionDefinitionList.size());
  }
}
