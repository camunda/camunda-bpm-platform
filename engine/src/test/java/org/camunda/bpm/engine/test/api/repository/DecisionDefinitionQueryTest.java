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
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.decisionDefinitionByDeployTime;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.inverted;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.verifySortingAndCount;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinitionQuery;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
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

  @After
  public void tearDown() {
    ClockUtil.resetClock();
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
    assertThat(decisionDefinition.getKey()).isEqualTo("one");
    assertThat(decisionDefinition.getName()).isEqualTo("One");
    assertThat(decisionDefinition.getId()).startsWith("one:1");
    assertThat(decisionDefinition.getCategory()).isEqualTo("Examples");
    assertThat(decisionDefinition.getVersion()).isEqualTo(1);
    assertThat(decisionDefinition.getResourceName()).isEqualTo("org/camunda/bpm/engine/test/repository/one.dmn");
    assertThat(decisionDefinition.getDeploymentId()).isEqualTo(firstDeploymentId);

    decisionDefinition = decisionDefinitions.get(1);
    assertThat(decisionDefinition.getKey()).isEqualTo("one");
    assertThat(decisionDefinition.getName()).isEqualTo("One");
    assertThat(decisionDefinition.getId()).startsWith("one:2");
    assertThat(decisionDefinition.getCategory()).isEqualTo("Examples");
    assertThat(decisionDefinition.getVersion()).isEqualTo(2);
    assertThat(decisionDefinition.getResourceName()).isEqualTo("org/camunda/bpm/engine/test/repository/one.dmn");
    assertThat(decisionDefinition.getDeploymentId()).isEqualTo(secondDeploymentId);

    decisionDefinition = decisionDefinitions.get(2);
    assertThat(decisionDefinition.getKey()).isEqualTo("two");
    assertThat(decisionDefinition.getName()).isEqualTo("Two");
    assertThat(decisionDefinition.getId()).startsWith("two:1");
    assertThat(decisionDefinition.getCategory()).isEqualTo("Examples2");
    assertThat(decisionDefinition.getVersion()).isEqualTo(1);
    assertThat(decisionDefinition.getResourceName()).isEqualTo("org/camunda/bpm/engine/test/repository/two.dmn");
    assertThat(decisionDefinition.getDeploymentId()).isEqualTo(firstDeploymentId);
  }

  @Test
  public void queryByDecisionDefinitionIds() {
    // empty list
    assertThat(repositoryService.createDecisionDefinitionQuery().decisionDefinitionIdIn("a", "b").list()).isEmpty();

    // collect all ids
    List<DecisionDefinition> decisionDefinitions = repositoryService.createDecisionDefinitionQuery().list();
    List<String> ids = new ArrayList<String>();
    for (DecisionDefinition decisionDefinition : decisionDefinitions) {
      ids.add(decisionDefinition.getId());
    }

    decisionDefinitions = repositoryService.createDecisionDefinitionQuery()
      .decisionDefinitionIdIn(ids.toArray(new String[ids.size()]))
      .list();

    assertThat(decisionDefinitions).hasSize(ids.size());
    for (DecisionDefinition decisionDefinition : decisionDefinitions) {
      assertThat(ids).contains(decisionDefinition.getId()).withFailMessage("Expected to find decision definition " + decisionDefinition);
    }
  }

  @Test
  public void queryByDeploymentId() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query.deploymentId(firstDeploymentId);

    verifyQueryResults(query, 2);
  }

  @Test
  public void queryByInvalidDeploymentId() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

   query
     .deploymentId("invalid");

    verifyQueryResults(query, 0);

    // when/then
    assertThatThrownBy(() -> query.deploymentId(null))
      .isInstanceOf(NotValidException.class);

  }

  @Test
  public void testQueryByDeploymentTimeAfter() {
    // given
    Date startTest = DateUtils.addSeconds(ClockUtil.now(), 5);
    ClockUtil.setCurrentTime(DateUtils.addSeconds(startTest, 5));

    Deployment tempDeploymentOne = repositoryService.createDeployment()
        .addClasspathResource(DMN_ONE_RESOURCE).addClasspathResource(DMN_TWO_RESOURCE).deploy();
    engineRule.manageDeployment(tempDeploymentOne);

    Date timeAfterDeploymentOne = DateUtils.addSeconds(ClockUtil.getCurrentTime(), 1);

    ClockUtil.setCurrentTime(DateUtils.addSeconds(timeAfterDeploymentOne, 5));
    Deployment tempDeploymentTwo = repositoryService.createDeployment()
        .addClasspathResource(DMN_ONE_RESOURCE).deploy();
    engineRule.manageDeployment(tempDeploymentTwo);
    Date timeAfterDeploymentTwo = DateUtils.addSeconds(ClockUtil.getCurrentTime(), 1);

    ClockUtil.setCurrentTime(DateUtils.addSeconds(timeAfterDeploymentTwo, 5));
    Deployment tempDeploymentThree = repositoryService.createDeployment()
        .addClasspathResource(DMN_THREE_RESOURCE).deploy();
    engineRule.manageDeployment(tempDeploymentThree);
    Date timeAfterDeploymentThree = DateUtils.addSeconds(ClockUtil.getCurrentTime(), 1);

    // when
    List<DecisionDefinition> decisionDefinitions = repositoryService.createDecisionDefinitionQuery().deployedAfter(startTest).list();
    // then
    assertThat(decisionDefinitions).hasSize(4);

    // when
    decisionDefinitions = repositoryService.createDecisionDefinitionQuery().deployedAfter(timeAfterDeploymentOne).list();
    // then
    assertThat(decisionDefinitions).hasSize(2);
    assertThatDecisionDefinitionsWereDeployedAfter(decisionDefinitions, timeAfterDeploymentOne);

    // when
    decisionDefinitions = repositoryService.createDecisionDefinitionQuery().deployedAfter(timeAfterDeploymentTwo).list();
    // then
    assertThat(decisionDefinitions).hasSize(1);
    assertThatDecisionDefinitionsWereDeployedAfter(decisionDefinitions, timeAfterDeploymentTwo);

    // when
    decisionDefinitions = repositoryService.createDecisionDefinitionQuery().deployedAfter(timeAfterDeploymentThree).list();
    // then
    assertThat(decisionDefinitions).hasSize(0);
  }

  @Test
  public void testQueryByDeploymentTimeAt() throws ParseException {
    // given
    //get rid of the milliseconds because of MySQL datetime precision
    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss");

    Date startTest = formatter.parse(formatter.format(DateUtils.addSeconds(ClockUtil.now(), 5)));
    ClockUtil.setCurrentTime(startTest);

    Date timeAtDeploymentOne = ClockUtil.getCurrentTime();
    Deployment tempDeploymentOne = repositoryService.createDeployment()
        .addClasspathResource(DMN_ONE_RESOURCE).addClasspathResource(DMN_TWO_RESOURCE).deploy();
    engineRule.manageDeployment(tempDeploymentOne);

    Date timeAtDeploymentTwo = DateUtils.addSeconds(timeAtDeploymentOne, 5);
    ClockUtil.setCurrentTime(timeAtDeploymentTwo);
    Deployment tempDeploymentTwo = repositoryService.createDeployment()
        .addClasspathResource(DMN_ONE_RESOURCE).deploy();
    engineRule.manageDeployment(tempDeploymentTwo);

    Date timeAtDeploymentThree = DateUtils.addSeconds(timeAtDeploymentTwo, 5);
    ClockUtil.setCurrentTime(timeAtDeploymentThree);
    Deployment tempDeploymentThree = repositoryService.createDeployment()
        .addClasspathResource(DMN_THREE_RESOURCE).deploy();
    engineRule.manageDeployment(tempDeploymentThree);

    // then
    List<DecisionDefinition> processDefinitions = repositoryService.createDecisionDefinitionQuery().deployedAt(timeAtDeploymentOne).list();
    assertThat(processDefinitions).hasSize(2);
    assertThatDecisionDefinitionsWereDeployedAt(processDefinitions, timeAtDeploymentOne);

    processDefinitions = repositoryService.createDecisionDefinitionQuery().deployedAt(timeAtDeploymentTwo).list();
    assertThat(processDefinitions).hasSize(1);
    assertThatDecisionDefinitionsWereDeployedAt(processDefinitions, timeAtDeploymentTwo);

    processDefinitions = repositoryService.createDecisionDefinitionQuery().deployedAt(timeAtDeploymentThree).list();
    assertThat(processDefinitions).hasSize(1);
    assertThatDecisionDefinitionsWereDeployedAt(processDefinitions, timeAtDeploymentThree);

    processDefinitions = repositoryService.createDecisionDefinitionQuery().deployedAt(DateUtils.addSeconds(ClockUtil.getCurrentTime(), 5)).list();
    assertThat(processDefinitions).hasSize(0);
  }

  @Test
  public void queryByName() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query.decisionDefinitionName("Two");

    verifyQueryResults(query, 1);

    query.decisionDefinitionName("One");

    verifyQueryResults(query, 2);
  }

  @Test
  public void queryByInvalidName() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query.decisionDefinitionName("invalid");

    verifyQueryResults(query, 0);

    // when/then
    assertThatThrownBy(() -> query.decisionDefinitionName(null))
      .isInstanceOf(NotValidException.class);
  }

  @Test
  public void queryByNameLike() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query.decisionDefinitionNameLike("%w%");

    verifyQueryResults(query, 1);

    query.decisionDefinitionNameLike("%z\\_");

    verifyQueryResults(query, 1);
  }

  @Test
  public void queryByInvalidNameLike() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query.decisionDefinitionNameLike("%invalid%");

    verifyQueryResults(query, 0);

    // when/then
    assertThatThrownBy(() -> query.decisionDefinitionNameLike(null))
      .isInstanceOf(NotValidException.class);
  }

  @Test
  public void queryByResourceNameLike() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query.decisionDefinitionResourceNameLike("%ree%");

    verifyQueryResults(query, 1);

    query.decisionDefinitionResourceNameLike("%ee\\_%");

    verifyQueryResults(query, 1);
  }

  @Test
  public void queryByInvalidNResourceNameLike() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query.decisionDefinitionResourceNameLike("%invalid%");

    verifyQueryResults(query, 0);

    // when/then
    assertThatThrownBy(() -> query.decisionDefinitionNameLike(null))
      .isInstanceOf(NotValidException.class);
  }

  @Test
  public void queryByKey() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    // decision one
    query.decisionDefinitionKey("one");

    verifyQueryResults(query, 2);

    // decision two
    query.decisionDefinitionKey("two");

    verifyQueryResults(query, 1);
  }

  @Test
  public void queryByInvalidKey() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query.decisionDefinitionKey("invalid");

    verifyQueryResults(query, 0);

    // when/then
    assertThatThrownBy(() -> query.decisionDefinitionKey(null))
      .isInstanceOf(NotValidException.class);
  }

  @Test
  public void queryByKeyLike() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query.decisionDefinitionKeyLike("%o%");

    verifyQueryResults(query, 3);

    query.decisionDefinitionKeyLike("%z\\_");

    verifyQueryResults(query, 1);
  }

  @Test
  public void queryByInvalidKeyLike() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query.decisionDefinitionKeyLike("%invalid%");

    verifyQueryResults(query, 0);

    // when/then
    assertThatThrownBy(() -> query.decisionDefinitionKeyLike(null))
      .isInstanceOf(NotValidException.class);
  }

  @Test
  public void queryByCategory() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query.decisionDefinitionCategory("Examples");

    verifyQueryResults(query, 2);
  }

  @Test
  public void queryByInvalidCategory() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query.decisionDefinitionCategory("invalid");

    verifyQueryResults(query, 0);

    // when/then
    assertThatThrownBy(() -> query.decisionDefinitionCategory(null))
      .isInstanceOf(NotValidException.class);
  }

  @Test
  public void queryByCategoryLike() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query.decisionDefinitionCategoryLike("%Example%");

    verifyQueryResults(query, 3);

    query.decisionDefinitionCategoryLike("%amples2");

    verifyQueryResults(query, 1);

    query.decisionDefinitionCategoryLike("%z\\_");

    verifyQueryResults(query, 1);
  }

  @Test
  public void queryByInvalidCategoryLike() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query.decisionDefinitionCategoryLike("invalid");

    verifyQueryResults(query, 0);

    // when/then
    assertThatThrownBy(() -> query.decisionDefinitionCategoryLike(null))
      .isInstanceOf(NotValidException.class);
  }

  @Test
  public void queryByVersion() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query.decisionDefinitionVersion(2);

    verifyQueryResults(query, 1);

    query.decisionDefinitionVersion(1);

    verifyQueryResults(query, 3);
  }

  @Test
  public void queryByInvalidVersion() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query.decisionDefinitionVersion(3);

    verifyQueryResults(query, 0);

    // when/then
    assertThatThrownBy(() -> query.decisionDefinitionVersion(-1))
      .isInstanceOf(NotValidException.class);

    assertThatThrownBy(() -> query.decisionDefinitionVersion(null))
    .isInstanceOf(NotValidException.class);

  }

  @Test
  public void queryByLatest() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    query.latestVersion();

    verifyQueryResults(query, 3);

    query
      .decisionDefinitionKey("one")
      .latestVersion();

    verifyQueryResults(query, 1);

    query
      .decisionDefinitionKey("two")
      .latestVersion();

    verifyQueryResults(query, 1);
  }

  @Test
  public void testInvalidUsageOfLatest() {
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    // when/then
    assertThatThrownBy(() -> query
        .decisionDefinitionId("test")
        .latestVersion()
        .list())
      .isInstanceOf(NotValidException.class);

    assertThatThrownBy(() -> query
        .decisionDefinitionName("test")
        .latestVersion()
        .list())
      .isInstanceOf(NotValidException.class);

    assertThatThrownBy(() -> query
        .decisionDefinitionNameLike("test")
        .latestVersion()
        .list())
      .isInstanceOf(NotValidException.class);

    assertThatThrownBy(() -> query
        .decisionDefinitionVersion(1)
        .latestVersion()
        .list())
      .isInstanceOf(NotValidException.class);

    assertThatThrownBy(() -> query
        .deploymentId("test")
        .latestVersion()
        .list())
      .isInstanceOf(NotValidException.class);
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
    assertThat(decisionDefinitions.size()).isEqualTo(4);

    assertThat(decisionDefinitions.get(0).getKey()).isEqualTo("one");
    assertThat(decisionDefinitions.get(0).getVersion()).isEqualTo(2);
    assertThat(decisionDefinitions.get(1).getKey()).isEqualTo("one");
    assertThat(decisionDefinitions.get(1).getVersion()).isEqualTo(1);
    assertThat(decisionDefinitions.get(2).getKey()).isEqualTo("two");
    assertThat(decisionDefinitions.get(2).getVersion()).isEqualTo(1);
  }


  protected void verifyQueryResults(DecisionDefinitionQuery query, int expectedCount) {
    assertThat(query.count()).isEqualTo(expectedCount);
    assertThat(query.list().size()).isEqualTo(expectedCount);
  }

  @org.camunda.bpm.engine.test.Deployment(resources = {
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

    assertThat(decisionDefinitionList.get(1).getVersionTag()).isEqualTo("1.1.0");
  }

  @Test
  public void testQueryOrderByDecisionRequirementsDefinitionKey() {
    // given
    List<DecisionDefinition> scoreDefinitions = testRule.deploy(DRD_SCORE_RESOURCE).getDeployedDecisionDefinitions();
    List<String> scoreDefinitionIds = asIds(scoreDefinitions);

    List<DecisionDefinition> dishDefinitions = testRule.deploy(DRD_DISH_RESOURCE).getDeployedDecisionDefinitions();
    List<String> dishDefinitionIds = asIds(dishDefinitions);

    // when
    List<DecisionDefinition> decisionDefinitionList = repositoryService
      .createDecisionDefinitionQuery()
      .decisionDefinitionIdIn(merge(scoreDefinitionIds, dishDefinitionIds))
      .orderByDecisionRequirementsDefinitionKey()
      .asc()
      .list();

    // then
    List<DecisionDefinition> firstThreeResults = decisionDefinitionList.subList(0, 3);
    List<DecisionDefinition> lastTwoResults = decisionDefinitionList.subList(3, 5);

    assertThat(firstThreeResults).extracting("id").containsExactlyInAnyOrderElementsOf(dishDefinitionIds);
    assertThat(lastTwoResults).extracting("id").containsExactlyInAnyOrderElementsOf(scoreDefinitionIds);
  }

  @Test
  public void testQueryOrderByDeployTime() {
    // when
    DecisionDefinitionQuery decisionDefinitionOrderByDeploymentTimeAscQuery = repositoryService.createDecisionDefinitionQuery().orderByDeploymentTime().asc();
    DecisionDefinitionQuery decisionDefinitionOrderByDeploymentTimeDescQuery = repositoryService.createDecisionDefinitionQuery().orderByDeploymentTime().desc();

    // then
    verifySortingAndCount(decisionDefinitionOrderByDeploymentTimeAscQuery, 4, decisionDefinitionByDeployTime(engineRule.getProcessEngine()));
    verifySortingAndCount(decisionDefinitionOrderByDeploymentTimeDescQuery, 4, inverted(decisionDefinitionByDeployTime(engineRule.getProcessEngine())));
  }

  protected String[] merge(List<String> list1, List<String> list2) {
    int numElements = list1.size() + list2.size();
    List<String> copy = new ArrayList<>(numElements);
    copy.addAll(list1);
    copy.addAll(list2);

    return copy.toArray(new String[numElements]);
  }

  protected List<String> asIds(List<DecisionDefinition> decisions) {
    List<String> ids = new ArrayList<>();
    for (DecisionDefinition decision : decisions) {
      ids.add(decision.getId());
    }

    return ids;
  }

  @org.camunda.bpm.engine.test.Deployment(resources = {
    "org/camunda/bpm/engine/test/api/repository/versionTag.dmn",
    "org/camunda/bpm/engine/test/api/repository/versionTagHigher.dmn" })
  @Test
  public void testQueryByVersionTag() {
    DecisionDefinition decisionDefinition = repositoryService
      .createDecisionDefinitionQuery()
      .versionTag("1.0.0")
      .singleResult();

    assertThat(decisionDefinition.getKey()).isEqualTo("versionTag");
    assertThat(decisionDefinition.getVersionTag()).isEqualTo("1.0.0");
  }

  @org.camunda.bpm.engine.test.Deployment(resources = {
    "org/camunda/bpm/engine/test/api/repository/versionTag.dmn",
    "org/camunda/bpm/engine/test/api/repository/versionTagHigher.dmn" })
  @Test
  public void testQueryByVersionTagLike() {
    List<DecisionDefinition> decisionDefinitionList = repositoryService
    .createDecisionDefinitionQuery()
    .versionTagLike("1%")
    .list();

    assertThat(decisionDefinitionList).hasSize(2);
  }

  protected void assertThatDecisionDefinitionsWereDeployedAfter(List<DecisionDefinition> decisionDefinitions, Date deployedAfter) {
    for (DecisionDefinition decisionDefinition : decisionDefinitions) {
      assertThat(repositoryService.createDeploymentQuery().deploymentId(decisionDefinition.getDeploymentId()).singleResult().getDeploymentTime()).isAfter(deployedAfter);
    }
  }

  protected void assertThatDecisionDefinitionsWereDeployedAt(List<DecisionDefinition> decisionDefinitions, Date deployedAt) {
    for (DecisionDefinition decisionDefinition : decisionDefinitions) {
      assertThat(repositoryService.createDeploymentQuery().deploymentId(decisionDefinition.getDeploymentId()).singleResult().getDeploymentTime()).isEqualTo(deployedAt);
    }
  }
}
