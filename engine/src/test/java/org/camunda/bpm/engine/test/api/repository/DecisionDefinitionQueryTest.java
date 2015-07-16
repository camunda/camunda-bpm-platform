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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinitionQuery;

public class DecisionDefinitionQueryTest extends AbstractDefinitionQueryTest {

  protected String getResourceOnePath() {
    return "org/camunda/bpm/engine/test/repository/one.dmn";
  }

  protected String getResourceTwoPath() {
    return "org/camunda/bpm/engine/test/repository/two.dmn";
  }

  public void testDecisionDefinitionProperties() {
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
    assertEquals(deploymentOneId, decisionDefinition.getDeploymentId());

    decisionDefinition = decisionDefinitions.get(1);
    assertEquals("one", decisionDefinition.getKey());
    assertEquals("One", decisionDefinition.getName());
    assertTrue(decisionDefinition.getId().startsWith("one:2"));
    assertEquals("Examples", decisionDefinition.getCategory());
    assertEquals(2, decisionDefinition.getVersion());
    assertEquals("org/camunda/bpm/engine/test/repository/one.dmn", decisionDefinition.getResourceName());
    assertEquals(deploymentTwoId, decisionDefinition.getDeploymentId());

    decisionDefinition = decisionDefinitions.get(2);
    assertEquals("two", decisionDefinition.getKey());
    assertEquals("Two", decisionDefinition.getName());
    assertTrue(decisionDefinition.getId().startsWith("two:1"));
    assertEquals("Examples2", decisionDefinition.getCategory());
    assertEquals(1, decisionDefinition.getVersion());
    assertEquals("org/camunda/bpm/engine/test/repository/two.dmn", decisionDefinition.getResourceName());
    assertEquals(deploymentOneId, decisionDefinition.getDeploymentId());
  }

  public void testQueryByDecisionDefinitionIds() {
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

  public void testQueryByDeploymentId() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .deploymentId(deploymentOneId);

    verifyQueryResults(query, 2);
  }

  public void testQueryByInvalidDeploymentId() {
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

  public void testQueryByName() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .decisionDefinitionName("Two");

    verifyQueryResults(query, 1);

    query
      .decisionDefinitionName("One");

    verifyQueryResults(query, 2);
  }

  public void testQueryByInvalidName() {
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

  public void testQueryByNameLike() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .decisionDefinitionNameLike("%w%");

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidNameLike() {
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

  public void testQueryByKey() {
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

  public void testQueryByInvalidKey() {
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

  public void testQueryByKeyLike() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .decisionDefinitionKeyLike("%o%");

    verifyQueryResults(query, 3);
  }

  public void testQueryByInvalidKeyLike() {
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

  public void testQueryByCategory() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .decisionDefinitionCategory("Examples");

    verifyQueryResults(query, 2);
  }

  public void testQueryByInvalidCategory() {
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

  public void testQueryByCategoryLike() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .decisionDefinitionCategoryLike("%Example%");

    verifyQueryResults(query, 3);

    query
      .decisionDefinitionCategoryLike("%amples2");

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidCategoryLike() {
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

  public void testQueryByVersion() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .decisionDefinitionVersion(2);

    verifyQueryResults(query, 1);

    query
      .decisionDefinitionVersion(1);

    verifyQueryResults(query, 2);
  }

  public void testQueryByInvalidVersion() {
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

  public void testQueryByLatest() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query
      .latestVersion();

    verifyQueryResults(query, 2);

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

  public void testQuerySorting() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    // asc
    query
      .orderByDecisionDefinitionId()
      .asc();
    verifyQueryResults(query, 3);

    query = repositoryService.createDecisionDefinitionQuery();

    query
      .orderByDeploymentId()
      .asc();
    verifyQueryResults(query, 3);

    query = repositoryService.createDecisionDefinitionQuery();

    query
      .orderByDecisionDefinitionKey()
      .asc();
    verifyQueryResults(query, 3);

    query = repositoryService.createDecisionDefinitionQuery();

    query
      .orderByDecisionDefinitionVersion()
      .asc();
    verifyQueryResults(query, 3);

    // desc

    query = repositoryService.createDecisionDefinitionQuery();

    query
      .orderByDecisionDefinitionId()
      .desc();
    verifyQueryResults(query, 3);

    query = repositoryService.createDecisionDefinitionQuery();

    query
      .orderByDeploymentId()
      .desc();
    verifyQueryResults(query, 3);

    query = repositoryService.createDecisionDefinitionQuery();

    query
      .orderByDecisionDefinitionKey()
      .desc();
    verifyQueryResults(query, 3);

    query = repositoryService.createDecisionDefinitionQuery();

    query
      .orderByDecisionDefinitionVersion()
      .desc();
    verifyQueryResults(query, 3);

    query = repositoryService.createDecisionDefinitionQuery();

    // Typical use decision
    query
      .orderByDecisionDefinitionKey()
      .asc()
      .orderByDecisionDefinitionVersion()
      .desc();

    List<DecisionDefinition> decisionDefinitions = query.list();
    assertEquals(3, decisionDefinitions.size());

    assertEquals("one", decisionDefinitions.get(0).getKey());
    assertEquals(2, decisionDefinitions.get(0).getVersion());
    assertEquals("one", decisionDefinitions.get(1).getKey());
    assertEquals(1, decisionDefinitions.get(1).getVersion());
    assertEquals("two", decisionDefinitions.get(2).getKey());
    assertEquals(1, decisionDefinitions.get(2).getVersion());
  }

}
