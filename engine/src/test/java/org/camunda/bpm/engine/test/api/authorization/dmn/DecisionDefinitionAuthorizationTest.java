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

package org.camunda.bpm.engine.test.api.authorization.dmn;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.UPDATE;
import static org.camunda.bpm.engine.authorization.Resources.DECISION_DEFINITION;

import java.io.InputStream;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinitionQuery;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;
import org.camunda.bpm.model.dmn.DmnModelInstance;

/**
 * @author Philipp Ossler
 */
public class DecisionDefinitionAuthorizationTest extends AuthorizationTest {

  protected static final String PROCESS_KEY = "testProcess";
  protected static final String DECISION_DEFINITION_KEY = "sampleDecision";

  @Override
  public void setUp() throws Exception {
    deploymentId = createDeployment(null,
        "org/camunda/bpm/engine/test/api/authorization/singleDecision.dmn11.xml",
        "org/camunda/bpm/engine/test/api/authorization/anotherDecision.dmn11.xml")
        .getId();
    super.setUp();
  }

  @Override
  public void tearDown() {
    super.tearDown();
    deleteDeployment(deploymentId);
  }

  public void testQueryWithoutAuthorization() {
    // given user is not authorized to read any decision definition

    // when
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testQueryWithReadPermissionOnAnyDecisionDefinition() {
    // given user gets read permission on any decision definition
    createGrantAuthorization(DECISION_DEFINITION, ANY, userId, READ);

    // when
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    // then
    verifyQueryResults(query, 2);
  }

  public void testQueryWithReadPermissionOnOneDecisionDefinition() {
    // given user gets read permission on the decision definition
    createGrantAuthorization(DECISION_DEFINITION, DECISION_DEFINITION_KEY, userId, READ);

    // when
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    // then
    verifyQueryResults(query, 1);

    DecisionDefinition definition = query.singleResult();
    assertNotNull(definition);
    assertEquals(DECISION_DEFINITION_KEY, definition.getKey());
  }

  public void testQueryWithMultiple() {
    createGrantAuthorization(DECISION_DEFINITION, DECISION_DEFINITION_KEY, userId, READ);
    createGrantAuthorization(DECISION_DEFINITION, ANY, userId, READ);

    // when
    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    // then
    verifyQueryResults(query, 2);
  }

  public void testGetDecisionDefinitionWithoutAuthorizations() {
    // given
    String decisionDefinitionId = selectDecisionDefinitionByKey(DECISION_DEFINITION_KEY).getId();

    try {
      // when
      repositoryService.getDecisionDefinition(decisionDefinitionId);
      fail("Exception expected");

    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(DECISION_DEFINITION_KEY, message);
      assertTextPresent(DECISION_DEFINITION.resourceName(), message);
    }
  }

  public void testGetDecisionDefinition() {
    // given
    String decisionDefinitionId = selectDecisionDefinitionByKey(DECISION_DEFINITION_KEY).getId();
    createGrantAuthorization(DECISION_DEFINITION, DECISION_DEFINITION_KEY, userId, READ);

    // when
    DecisionDefinition decisionDefinition = repositoryService.getDecisionDefinition(decisionDefinitionId);

    // then
    assertNotNull(decisionDefinition);
  }

  public void testGetDecisionDiagramWithoutAuthorizations() {
    // given
    String decisionDefinitionId = selectDecisionDefinitionByKey(DECISION_DEFINITION_KEY).getId();

    try {
      // when
      repositoryService.getDecisionDiagram(decisionDefinitionId);
      fail("Exception expected");

    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(DECISION_DEFINITION_KEY, message);
      assertTextPresent(DECISION_DEFINITION.resourceName(), message);
    }
  }

  public void testGetDecisionDiagram() {
    // given
    String decisionDefinitionId = selectDecisionDefinitionByKey(DECISION_DEFINITION_KEY).getId();
    createGrantAuthorization(DECISION_DEFINITION, DECISION_DEFINITION_KEY, userId, READ);

    // when
    InputStream stream = repositoryService.getDecisionDiagram(decisionDefinitionId);

    // then
    // no decision diagram deployed
    assertNull(stream);
  }

  public void testGetDecisionModelWithoutAuthorizations() {
    // given
    String decisionDefinitionId = selectDecisionDefinitionByKey(DECISION_DEFINITION_KEY).getId();

    try {
      // when
      repositoryService.getDecisionModel(decisionDefinitionId);
      fail("Exception expected");

    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(DECISION_DEFINITION_KEY, message);
      assertTextPresent(DECISION_DEFINITION.resourceName(), message);
    }
  }

  public void testGetDecisionModel() {
    // given
    String decisionDefinitionId = selectDecisionDefinitionByKey(DECISION_DEFINITION_KEY).getId();
    createGrantAuthorization(DECISION_DEFINITION, DECISION_DEFINITION_KEY, userId, READ);

    // when
    InputStream stream = repositoryService.getDecisionModel(decisionDefinitionId);

    // then
    assertNotNull(stream);
  }

  public void testGetDmnModelInstanceWithoutAuthorizations() {
    // given
    String decisionDefinitionId = selectDecisionDefinitionByKey(DECISION_DEFINITION_KEY).getId();

    try {
      // when
      repositoryService.getDmnModelInstance(decisionDefinitionId);
      fail("Exception expected");

    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(READ.getName(), message);
      assertTextPresent(DECISION_DEFINITION_KEY, message);
      assertTextPresent(DECISION_DEFINITION.resourceName(), message);
    }
  }

  public void testGetDmnModelInstance() {
    // given
    String decisionDefinitionId = selectDecisionDefinitionByKey(DECISION_DEFINITION_KEY).getId();
    createGrantAuthorization(DECISION_DEFINITION, DECISION_DEFINITION_KEY, userId, READ);

    // when
    DmnModelInstance modelInstance = repositoryService.getDmnModelInstance(decisionDefinitionId);

    // then
    assertNotNull(modelInstance);
  }

  public void testDecisionDefinitionUpdateTimeToLive() {
    //given
    String decisionDefinitionId = selectDecisionDefinitionByKey(DECISION_DEFINITION_KEY).getId();
    createGrantAuthorization(DECISION_DEFINITION, DECISION_DEFINITION_KEY, userId, UPDATE);

    //when
    repositoryService.updateDecisionDefinitionHistoryTimeToLive(decisionDefinitionId, 6);

    //then
    assertEquals(6, selectDecisionDefinitionByKey(DECISION_DEFINITION_KEY).getHistoryTimeToLive().intValue());

  }

  public void testDecisionDefinitionUpdateTimeToLiveWithoutAuthorizations() {
    //given
    String decisionDefinitionId = selectDecisionDefinitionByKey(DECISION_DEFINITION_KEY).getId();
    try {
      //when
      repositoryService.updateDecisionDefinitionHistoryTimeToLive(decisionDefinitionId, 6);
      fail("Exception expected");

    } catch (AuthorizationException e) {
      // then
      String message = e.getMessage();
      assertTextPresent(userId, message);
      assertTextPresent(UPDATE.getName(), message);
      assertTextPresent(DECISION_DEFINITION_KEY, message);
      assertTextPresent(DECISION_DEFINITION.resourceName(), message);
    }

  }

}
