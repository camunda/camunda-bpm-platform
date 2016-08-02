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
package org.camunda.bpm.engine.test.cmmn.sentry;

import java.util.List;

import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseSentryPartEntity;
import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseSentryPartQueryImpl;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnSentryDeclaration;
import org.camunda.bpm.engine.impl.test.CmmnProcessEngineTestCase;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.model.cmmn.VariableTransition;

/**
 * @author Roman Smirnov
 *
 */
public class SentryInitializationTest extends CmmnProcessEngineTestCase {

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryInitializationTest.testOnPart.cmmn"})
  public void testOnPart() {
    // given
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // when
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    // then
    List<CaseSentryPartEntity> parts = createCaseSentryPartQuery()
      .list();

    assertEquals(1, parts.size());

    CaseSentryPartEntity part = parts.get(0);

    assertEquals(caseInstanceId, part.getCaseExecutionId());
    assertEquals(caseInstanceId, part.getCaseInstanceId());
    assertEquals("Sentry_1", part.getSentryId());
    assertEquals(CmmnSentryDeclaration.PLAN_ITEM_ON_PART, part.getType());
    assertEquals("PI_HumanTask_1", part.getSource());
    assertEquals("complete", part.getStandardEvent());
    assertFalse(part.isSatisfied());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryInitializationTest.testVariableOnPart.cmmn"})
  public void testVariableOnPart() {
    // given
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // when
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    // then
    List<CaseSentryPartEntity> parts = createCaseSentryPartQuery()
      .list();

    assertEquals(1, parts.size());

    CaseSentryPartEntity part = parts.get(0);

    assertEquals(caseInstanceId, part.getCaseExecutionId());
    assertEquals(caseInstanceId, part.getCaseInstanceId());
    assertEquals("Sentry_1", part.getSentryId());
    assertEquals(CmmnSentryDeclaration.VARIABLE_ON_PART, part.getType());
    assertEquals(VariableTransition.create.name(), part.getVariableEvent());
    assertEquals("variable_1", part.getVariableName());
    assertFalse(part.isSatisfied());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryInitializationTest.testIfPart.cmmn"})
  public void testIfPart() {
    // given
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // when
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .setVariable("myVar", 0)
        .create()
        .getId();

    // then
    List<CaseSentryPartEntity> parts = createCaseSentryPartQuery()
      .list();

    assertEquals(1, parts.size());

    CaseSentryPartEntity part = parts.get(0);

    assertEquals(caseInstanceId, part.getCaseExecutionId());
    assertEquals(caseInstanceId, part.getCaseInstanceId());
    assertEquals("Sentry_1", part.getSentryId());
    assertEquals(CmmnSentryDeclaration.IF_PART, part.getType());
    assertNull(part.getSource());
    assertNull(part.getStandardEvent());
    assertFalse(part.isSatisfied());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryInitializationTest.testOnPartIfPartAndVariableOnPart.cmmn"})
  public void testOnPartIfPartAndVariableOnPart() {
    // given
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // when
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .create()
        .getId();

    // then
    CaseSentryPartQueryImpl query = createCaseSentryPartQuery();

    assertEquals(3, query.count());

    CaseSentryPartEntity part = query
        .type(CmmnSentryDeclaration.IF_PART)
        .singleResult();

    assertEquals(caseInstanceId, part.getCaseExecutionId());
    assertEquals(caseInstanceId, part.getCaseInstanceId());
    assertEquals("Sentry_1", part.getSentryId());
    assertEquals(CmmnSentryDeclaration.IF_PART, part.getType());
    assertNull(part.getSource());
    assertNull(part.getStandardEvent());
    assertFalse(part.isSatisfied());

    part = query
        .type(CmmnSentryDeclaration.PLAN_ITEM_ON_PART)
        .singleResult();

    assertEquals(caseInstanceId, part.getCaseExecutionId());
    assertEquals(caseInstanceId, part.getCaseInstanceId());
    assertEquals("Sentry_1", part.getSentryId());
    assertEquals(CmmnSentryDeclaration.PLAN_ITEM_ON_PART, part.getType());
    assertEquals("PI_HumanTask_1", part.getSource());
    assertEquals("complete", part.getStandardEvent());
    assertFalse(part.isSatisfied());

    part = query.type(CmmnSentryDeclaration.VARIABLE_ON_PART).singleResult();
    
    assertEquals(caseInstanceId, part.getCaseExecutionId());
    assertEquals(caseInstanceId, part.getCaseInstanceId());
    assertEquals("Sentry_1", part.getSentryId());
    assertEquals(CmmnSentryDeclaration.VARIABLE_ON_PART, part.getType());
    assertEquals(VariableTransition.delete.name(), part.getVariableEvent());
    assertEquals("variable_1", part.getVariableName());
    assertFalse(part.isSatisfied());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryInitializationTest.testMultipleSentries.cmmn"})
  public void testMultipleSentries() {
    // given
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // when
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .setVariable("myVar", 0)
        .create()
        .getId();

    // then
    CaseSentryPartQueryImpl query = createCaseSentryPartQuery();

    assertEquals(2, query.count());

    CaseSentryPartEntity part = query
        .sentryId("Sentry_1")
        .singleResult();

    assertEquals(caseInstanceId, part.getCaseExecutionId());
    assertEquals(caseInstanceId, part.getCaseInstanceId());
    assertEquals("Sentry_1", part.getSentryId());
    assertEquals(CmmnSentryDeclaration.IF_PART, part.getType());
    assertNull(part.getSource());
    assertNull(part.getStandardEvent());
    assertFalse(part.isSatisfied());

    part = query
        .sentryId("Sentry_2")
        .singleResult();

    assertEquals(caseInstanceId, part.getCaseExecutionId());
    assertEquals(caseInstanceId, part.getCaseInstanceId());
    assertEquals("Sentry_2", part.getSentryId());
    assertEquals(CmmnSentryDeclaration.PLAN_ITEM_ON_PART, part.getType());
    assertEquals("PI_HumanTask_1", part.getSource());
    assertEquals("complete", part.getStandardEvent());
    assertFalse(part.isSatisfied());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/sentry/SentryInitializationTest.testMultipleSentriesWithinStage.cmmn"})
  public void testMultipleSentriesWithinStage() {
    // given
    String caseDefinitionId = repositoryService
        .createCaseDefinitionQuery()
        .singleResult()
        .getId();

    // when
    String caseInstanceId = caseService
        .withCaseDefinition(caseDefinitionId)
        .setVariable("myVar", 0)
        .create()
        .getId();

    // then
    CaseSentryPartQueryImpl query = createCaseSentryPartQuery();

    assertEquals(2, query.count());

    // when
    String stageId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Stage_1")
        .singleResult()
        .getId();

    // then
    assertEquals(2, query.count());

    CaseSentryPartEntity part = query
        .sentryId("Sentry_1")
        .singleResult();

    assertEquals(caseInstanceId, part.getCaseInstanceId());
    assertEquals(stageId, part.getCaseExecutionId());
    assertEquals("Sentry_1", part.getSentryId());
    assertEquals(CmmnSentryDeclaration.IF_PART, part.getType());
    assertNull(part.getSource());
    assertNull(part.getStandardEvent());
    assertFalse(part.isSatisfied());

    part = query
        .sentryId("Sentry_2")
        .singleResult();

    assertEquals(caseInstanceId, part.getCaseInstanceId());
    assertEquals(stageId, part.getCaseExecutionId());
    assertEquals("Sentry_2", part.getSentryId());
    assertEquals(CmmnSentryDeclaration.PLAN_ITEM_ON_PART, part.getType());
    assertEquals("PI_HumanTask_1", part.getSource());
    assertEquals("complete", part.getStandardEvent());
    assertFalse(part.isSatisfied());
  }

}
