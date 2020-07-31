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
package org.camunda.bpm.engine.test.cmmn.operation;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.camunda.bpm.engine.impl.cmmn.behavior.MilestoneActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.execution.CaseExecutionImpl;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityExecution;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnCaseInstance;
import org.camunda.bpm.engine.impl.cmmn.model.CaseDefinitionBuilder;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnOnPartDeclaration;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnSentryDeclaration;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class CaseExecutionOccurTest {

  @Test
  public void testOccurMilestone() {

    // given
    // a case definition
    CmmnCaseDefinition caseDefinition = new CaseDefinitionBuilder("Case1")
      .createActivity("A")
        .behavior(new MilestoneActivityBehavior())
      .endActivity()
      .buildCaseDefinition();

    CmmnActivity activity = caseDefinition.findActivity("A");

    // a pseudo sentry
    CmmnSentryDeclaration sentryDeclaration = new CmmnSentryDeclaration("X");
    caseDefinition.findActivity("Case1").addSentry(sentryDeclaration);
    activity.addEntryCriteria(sentryDeclaration);

    CmmnOnPartDeclaration onPartDeclaration = new CmmnOnPartDeclaration();
    onPartDeclaration.setSource(new CmmnActivity("B", caseDefinition));
    onPartDeclaration.setStandardEvent("complete");
    sentryDeclaration.addOnPart(onPartDeclaration);

    // an active case instance
    CmmnCaseInstance caseInstance = caseDefinition.createCaseInstance();
    caseInstance.create();

    // task A as a child of the case instance
    CmmnActivityExecution milestoneA = caseInstance.findCaseExecution("A");

    // when

    // completing
    milestoneA.occur();

    // then
    // task A is completed ...
    assertTrue(milestoneA.isCompleted());
    // ... and the case instance is also completed
    assertTrue(caseInstance.isCompleted());

    // task A is not part of the case instance anymore
    assertNull(caseInstance.findCaseExecution("A"));
    // the case instance has no children
    assertTrue(((CaseExecutionImpl) caseInstance).getCaseExecutions().isEmpty());
  }

}
