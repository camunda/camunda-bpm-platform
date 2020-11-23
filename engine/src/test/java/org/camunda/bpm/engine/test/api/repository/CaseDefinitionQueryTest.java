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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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

  @Before
  public void setUp() throws Exception {
    deploymentThreeId = repositoryService.createDeployment().name("thirdDeployment").addClasspathResource(getResourceThreePath()).deploy().getId();
  }

  @After
  public void tearDown() throws Exception {
    repositoryService.deleteDeployment(deploymentThreeId, true);
  }

  @Test
  public void testCaseDefinitionProperties() {
    List<CaseDefinition> caseDefinitions = repositoryService
      .createCaseDefinitionQuery()
      .orderByCaseDefinitionName().asc()
      .orderByCaseDefinitionVersion().asc()
      .orderByCaseDefinitionCategory()
      .asc()
      .list();

    CaseDefinition caseDefinition = caseDefinitions.get(0);
    assertThat(caseDefinition.getKey()).isEqualTo("one");
    assertThat(caseDefinition.getName()).isEqualTo("One");
    assertThat(caseDefinition.getId()).startsWith("one:1");
    assertThat(caseDefinition.getCategory()).isEqualTo("Examples");
    assertThat(caseDefinition.getVersion()).isEqualTo(1);
    assertThat(caseDefinition.getResourceName()).isEqualTo("org/camunda/bpm/engine/test/repository/one.cmmn");
    assertThat(caseDefinition.getDeploymentId()).isEqualTo(deploymentOneId);

    caseDefinition = caseDefinitions.get(1);
    assertThat(caseDefinition.getKey()).isEqualTo("one");
    assertThat(caseDefinition.getName()).isEqualTo("One");
    assertThat(caseDefinition.getId()).startsWith("one:2");
    assertThat(caseDefinition.getCategory()).isEqualTo("Examples");
    assertThat(caseDefinition.getVersion()).isEqualTo(2);
    assertThat(caseDefinition.getResourceName()).isEqualTo("org/camunda/bpm/engine/test/repository/one.cmmn");
    assertThat(caseDefinition.getDeploymentId()).isEqualTo(deploymentTwoId);

    caseDefinition = caseDefinitions.get(2);
    assertThat(caseDefinition.getKey()).isEqualTo("two");
    assertThat(caseDefinition.getName()).isEqualTo("Two");
    assertThat(caseDefinition.getId()).startsWith("two:1");
    assertThat(caseDefinition.getCategory()).isEqualTo("Examples2");
    assertThat(caseDefinition.getVersion()).isEqualTo(1);
    assertThat(caseDefinition.getResourceName()).isEqualTo("org/camunda/bpm/engine/test/repository/two.cmmn");
    assertThat(caseDefinition.getDeploymentId()).isEqualTo(deploymentOneId);
  }

  @Test
  public void testQueryByCaseDefinitionIds() {
    // empty list
    assertThat(repositoryService.createCaseDefinitionQuery().caseDefinitionIdIn("a", "b").list()).isEmpty();

    // collect all ids
    List<CaseDefinition> caseDefinitions = repositoryService.createCaseDefinitionQuery().list();
    // no point of the test if the caseDefinitions is empty
    assertThat(caseDefinitions).isNotEmpty();
    List<String> ids = new ArrayList<String>();
    for (CaseDefinition caseDefinition : caseDefinitions) {
      ids.add(caseDefinition.getId());
    }

    caseDefinitions = repositoryService.createCaseDefinitionQuery()
      .caseDefinitionIdIn(ids.toArray(new String[ids.size()]))
      .list();

    assertThat(ids.size()).isEqualTo(caseDefinitions.size());
    for (CaseDefinition caseDefinition : caseDefinitions) {
      assertThat(ids).contains(caseDefinition.getId()).withFailMessage("Expected to find case definition " + caseDefinition);
    }

    assertThat(repositoryService.createCaseDefinitionQuery()
        .caseDefinitionIdIn(ids.toArray(new String[ids.size()]))
        .caseDefinitionId("nonExistent")
        .count()).isEqualTo(0);
  }

  @Test
  public void testQueryByDeploymentId() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .deploymentId(deploymentOneId);

    verifyQueryResults(query, 2);
  }

  @Test
  public void testQueryByInvalidDeploymentId() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

   query
     .deploymentId("invalid");

    verifyQueryResults(query, 0);


    // when/then
    assertThatThrownBy(() -> query.deploymentId(null))
      .isInstanceOf(NotValidException.class);
  }

  @Test
  public void testQueryByName() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionName("Two");

    verifyQueryResults(query, 1);

    query
      .caseDefinitionName("One");

    verifyQueryResults(query, 2);
  }

  @Test
  public void testQueryByInvalidName() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionName("invalid");

    verifyQueryResults(query, 0);

    // when/then
    assertThatThrownBy(() -> query.caseDefinitionName(null))
      .isInstanceOf(NotValidException.class);
  }

  @Test
  public void testQueryByNameLike() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionNameLike("%w%");

    verifyQueryResults(query, 1);

    query.caseDefinitionNameLike("%z\\_");

    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryByInvalidNameLike() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionNameLike("%invalid%");

    verifyQueryResults(query, 0);

    // when/then
    assertThatThrownBy(() -> query.caseDefinitionNameLike(null))
      .isInstanceOf(NotValidException.class);
  }

  @Test
  public void testQueryByResourceNameLike() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
        .caseDefinitionResourceNameLike("%ree%");

    verifyQueryResults(query, 1);

    query.caseDefinitionResourceNameLike("%e\\_%");

    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryByInvalidResourceNameLike() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
        .caseDefinitionResourceNameLike("%invalid%");

    verifyQueryResults(query, 0);

    // when/then
    assertThatThrownBy(() -> query.caseDefinitionNameLike(null))
      .isInstanceOf(NotValidException.class);
  }

  @Test
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

  @Test
  public void testQueryByInvalidKey() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionKey("invalid");

    verifyQueryResults(query, 0);

    // when/then
    assertThatThrownBy(() -> query.caseDefinitionKey(null))
      .isInstanceOf(NotValidException.class);
  }

  @Test
  public void testQueryByKeyLike() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionKeyLike("%o%");

    verifyQueryResults(query, 3);

    query.caseDefinitionKeyLike("%z\\_");

    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryByInvalidKeyLike() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionKeyLike("%invalid%");

    verifyQueryResults(query, 0);

    // when/then
    assertThatThrownBy(() -> query.caseDefinitionKeyLike(null))
      .isInstanceOf(NotValidException.class);
  }

  @Test
  public void testQueryByCategory() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionCategory("Examples");

    verifyQueryResults(query, 2);
  }

  @Test
  public void testQueryByInvalidCategory() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionCategory("invalid");

    verifyQueryResults(query, 0);

    // when/then
    assertThatThrownBy(() -> query.caseDefinitionCategory(null))
      .isInstanceOf(NotValidException.class);
  }

  @Test
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

  @Test
  public void testQueryByInvalidCategoryLike() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionCategoryLike("invalid");

    verifyQueryResults(query, 0);

    // when/then
    assertThatThrownBy(() -> query.caseDefinitionCategoryLike(null))
      .isInstanceOf(NotValidException.class);
  }

  @Test
  public void testQueryByVersion() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionVersion(2);

    verifyQueryResults(query, 1);

    query
      .caseDefinitionVersion(1);

    verifyQueryResults(query, 3);
  }

  @Test
  public void testQueryByInvalidVersion() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    query
      .caseDefinitionVersion(3);

    verifyQueryResults(query, 0);

    // when/then
    assertThatThrownBy(() -> query.caseDefinitionVersion(-1))
      .isInstanceOf(NotValidException.class);

    // and
    assertThatThrownBy(() -> query.caseDefinitionVersion(null))
      .isInstanceOf(NotValidException.class);
  }

  @Test
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

  @Test
  public void testInvalidUsageOfLatest() {
    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    // when/then
    assertThatThrownBy(() -> query
        .caseDefinitionId("test")
        .latestVersion()
        .list())
      .isInstanceOf(NotValidException.class);

    // and
    assertThatThrownBy(() -> query
        .caseDefinitionName("test")
        .latestVersion()
        .list())
      .isInstanceOf(NotValidException.class);

    // and
    assertThatThrownBy(() -> query
        .caseDefinitionNameLike("test")
        .latestVersion()
        .list())
      .isInstanceOf(NotValidException.class);

    // and
    assertThatThrownBy(() -> query
        .caseDefinitionVersion(1)
        .latestVersion()
        .list())
      .isInstanceOf(NotValidException.class);

    // and
    assertThatThrownBy(() -> query
        .deploymentId("test")
        .latestVersion()
        .list())
      .isInstanceOf(NotValidException.class);
  }

  @Test
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
    assertThat(caseDefinitions.size()).isEqualTo(4);

    assertThat(caseDefinitions.get(0).getKey()).isEqualTo("one");
    assertThat(caseDefinitions.get(0).getVersion()).isEqualTo(2);
    assertThat(caseDefinitions.get(1).getKey()).isEqualTo("one");
    assertThat(caseDefinitions.get(1).getVersion()).isEqualTo(1);
    assertThat(caseDefinitions.get(2).getKey()).isEqualTo("two");
    assertThat(caseDefinitions.get(2).getVersion()).isEqualTo(1);
  }

}
