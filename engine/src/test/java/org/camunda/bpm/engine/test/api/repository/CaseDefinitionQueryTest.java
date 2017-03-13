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
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;

/**
 * @author Roman Smirnov
 *
 */
public class CaseDefinitionQueryTest extends AbstractDefinitionQueryTest {

  private String deploymentThreeId;

  protected String getResourceOnePath() {
    return "org/camunda/bpm/engine/test/repository/one.cmmn";
  }

  protected String getResourceTwoPath() {
    return "org/camunda/bpm/engine/test/repository/two.cmmn";
  }

  protected String getResourceThreePath() {
    return "org/camunda/bpm/engine/test/api/repository/three_.cmmn";
  }

  @Override
  protected void setUp() throws Exception {
    deploymentThreeId = repositoryService.createDeployment().name("thirdDeployment").addClasspathResource(getResourceThreePath()).deploy().getId();
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    repositoryService.deleteDeployment(deploymentThreeId, true);
  }

  public void testCaseDefinitionProperties() {
    List<CaseDefinition> caseDefinitions = repositoryService
      .createCaseDefinitionQuery()
      .orderByCaseDefinitionName().asc()
      .orderByCaseDefinitionVersion().asc()
      .orderByCaseDefinitionCategory()
      .asc()
      .list();

    CaseDefinition caseDefinition = caseDefinitions.get(0);
    assertEquals("one", caseDefinition.getKey());
    assertEquals("One", caseDefinition.getName());
    assertTrue(caseDefinition.getId().startsWith("one:1"));
    assertEquals("Examples", caseDefinition.getCategory());
    assertEquals(1, caseDefinition.getVersion());
    assertEquals("org/camunda/bpm/engine/test/repository/one.cmmn", caseDefinition.getResourceName());
    assertEquals(deploymentOneId, caseDefinition.getDeploymentId());

    caseDefinition = caseDefinitions.get(1);
    assertEquals("one", caseDefinition.getKey());
    assertEquals("One", caseDefinition.getName());
    assertTrue(caseDefinition.getId().startsWith("one:2"));
    assertEquals("Examples", caseDefinition.getCategory());
    assertEquals(2, caseDefinition.getVersion());
    assertEquals("org/camunda/bpm/engine/test/repository/one.cmmn", caseDefinition.getResourceName());
    assertEquals(deploymentTwoId, caseDefinition.getDeploymentId());

    caseDefinition = caseDefinitions.get(2);
    assertEquals("two", caseDefinition.getKey());
    assertEquals("Two", caseDefinition.getName());
    assertTrue(caseDefinition.getId().startsWith("two:1"));
    assertEquals("Examples2", caseDefinition.getCategory());
    assertEquals(1, caseDefinition.getVersion());
    assertEquals("org/camunda/bpm/engine/test/repository/two.cmmn", caseDefinition.getResourceName());
    assertEquals(deploymentOneId, caseDefinition.getDeploymentId());
  }

  public void testQueryByCaseDefinitionIds() {
    // empty list
    assertTrue(repositoryService.createCaseDefinitionQuery().caseDefinitionIdIn("a", "b").list().isEmpty());

    // collect all ids
    List<CaseDefinition> caseDefinitions = repositoryService.createCaseDefinitionQuery().list();
    // no point of the test if the caseDefinitions is empty
    assertFalse(caseDefinitions.isEmpty());
    List<String> ids = new ArrayList<String>();
    for (CaseDefinition caseDefinition : caseDefinitions) {
      ids.add(caseDefinition.getId());
    }

    caseDefinitions = repositoryService.createCaseDefinitionQuery()
      .caseDefinitionIdIn(ids.toArray(new String[ids.size()]))
      .list();

    assertEquals(ids.size(), caseDefinitions.size());
    for (CaseDefinition caseDefinition : caseDefinitions) {
      if (!ids.contains(caseDefinition.getId())) {
        fail("Expected to find case definition "+ caseDefinition);
      }
    }

    assertEquals(0, repositoryService.createCaseDefinitionQuery()
        .caseDefinitionIdIn(ids.toArray(new String[ids.size()]))
        .caseDefinitionId("nonExistent")
        .count());
  }

  public void testQueryByDeploymentId() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .deploymentId(deploymentOneId);

    verifyQueryResults(query, 2);
  }

  public void testQueryByInvalidDeploymentId() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

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
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionName("Two");

    verifyQueryResults(query, 1);

    query
      .caseDefinitionName("One");

    verifyQueryResults(query, 2);
  }

  public void testQueryByInvalidName() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionName("invalid");

    verifyQueryResults(query, 0);

    try {
      query.caseDefinitionName(null);
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }
  }

  public void testQueryByNameLike() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionNameLike("%w%");

    verifyQueryResults(query, 1);

    query.caseDefinitionNameLike("%z\\_");

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidNameLike() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionNameLike("%invalid%");

    verifyQueryResults(query, 0);

    try {
      query.caseDefinitionNameLike(null);
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }
  }

  public void testQueryByResourceNameLike() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
        .caseDefinitionResourceNameLike("%ree%");

    verifyQueryResults(query, 1);

    query.caseDefinitionResourceNameLike("%e\\_%");

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidResourceNameLike() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
        .caseDefinitionResourceNameLike("%invalid%");

    verifyQueryResults(query, 0);

    try {
      query.caseDefinitionNameLike(null);
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }
  }

  public void testQueryByKey() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    // case one
    query
      .caseDefinitionKey("one");

    verifyQueryResults(query, 2);

    // case two
    query
      .caseDefinitionKey("two");

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidKey() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionKey("invalid");

    verifyQueryResults(query, 0);

    try {
      query.caseDefinitionKey(null);
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }
  }

  public void testQueryByKeyLike() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionKeyLike("%o%");

    verifyQueryResults(query, 3);

    query.caseDefinitionKeyLike("%z\\_");

    verifyQueryResults(query, 1);
  }

  public void testQueryByInvalidKeyLike() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionKeyLike("%invalid%");

    verifyQueryResults(query, 0);

    try {
      query.caseDefinitionKeyLike(null);
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }
  }

  public void testQueryByCategory() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionCategory("Examples");

    verifyQueryResults(query, 2);
  }

  public void testQueryByInvalidCategory() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionCategory("invalid");

    verifyQueryResults(query, 0);

    try {
      query.caseDefinitionCategory(null);
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }
  }

  public void testQueryByCategoryLike() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionCategoryLike("%Example%");

    verifyQueryResults(query, 3);

    query
      .caseDefinitionCategoryLike("%amples2");

    verifyQueryResults(query, 1);

    query.caseDefinitionCategoryLike("%z\\_");

    verifyQueryResults(query, 1);

  }

  public void testQueryByInvalidCategoryLike() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionCategoryLike("invalid");

    verifyQueryResults(query, 0);

    try {
      query.caseDefinitionCategoryLike(null);
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }
  }

  public void testQueryByVersion() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionVersion(2);

    verifyQueryResults(query, 1);

    query
      .caseDefinitionVersion(1);

    verifyQueryResults(query, 3);
  }

  public void testQueryByInvalidVersion() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionVersion(3);

    verifyQueryResults(query, 0);

    try {
      query.caseDefinitionVersion(-1);
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }

    try {
      query.caseDefinitionVersion(null);
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }
  }

  public void testQueryByLatest() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .latestVersion();

    verifyQueryResults(query, 3);

    query
      .caseDefinitionKey("one")
      .latestVersion();

    verifyQueryResults(query, 1);

    query
      .caseDefinitionKey("two").latestVersion();
    verifyQueryResults(query, 1);
  }

  public void testInvalidUsageOfLatest() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    try {
      query
        .caseDefinitionId("test")
        .latestVersion()
        .list();
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }

    try {
      query
        .caseDefinitionName("test")
        .latestVersion()
        .list();
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }

    try {
      query
        .caseDefinitionNameLike("test")
        .latestVersion()
        .list();
      fail();
    } catch (NotValidException e) {
      // Expected exception
    }

    try {
      query
        .caseDefinitionVersion(1)
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
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    // asc
    query
      .orderByCaseDefinitionId()
      .asc();
    verifyQueryResults(query, 4);

    query = repositoryService.createCaseDefinitionQuery();

    query
      .orderByDeploymentId()
      .asc();
    verifyQueryResults(query, 4);

    query = repositoryService.createCaseDefinitionQuery();

    query
      .orderByCaseDefinitionKey()
      .asc();
    verifyQueryResults(query, 4);

    query = repositoryService.createCaseDefinitionQuery();

    query
      .orderByCaseDefinitionVersion()
      .asc();
    verifyQueryResults(query, 4);

    // desc

    query = repositoryService.createCaseDefinitionQuery();

    query
      .orderByCaseDefinitionId()
      .desc();
    verifyQueryResults(query, 4);

    query = repositoryService.createCaseDefinitionQuery();

    query
      .orderByDeploymentId()
      .desc();
    verifyQueryResults(query, 4);

    query = repositoryService.createCaseDefinitionQuery();

    query
      .orderByCaseDefinitionKey()
      .desc();
    verifyQueryResults(query, 4);

    query = repositoryService.createCaseDefinitionQuery();

    query
      .orderByCaseDefinitionVersion()
      .desc();
    verifyQueryResults(query, 4);

    query = repositoryService.createCaseDefinitionQuery();

    // Typical use case
    query
      .orderByCaseDefinitionKey()
      .asc()
      .orderByCaseDefinitionVersion()
      .desc();

    List<CaseDefinition> caseDefinitions = query.list();
    assertEquals(4, caseDefinitions.size());

    assertEquals("one", caseDefinitions.get(0).getKey());
    assertEquals(2, caseDefinitions.get(0).getVersion());
    assertEquals("one", caseDefinitions.get(1).getKey());
    assertEquals(1, caseDefinitions.get(1).getVersion());
    assertEquals("two", caseDefinitions.get(2).getKey());
    assertEquals(1, caseDefinitions.get(2).getVersion());
  }

}
