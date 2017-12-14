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
package org.camunda.bpm.engine.test.cmmn.listener;

import java.util.List;

import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.impl.test.CmmnProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Roman Smirnov
 *
 */
public class CaseExecutionListenerTest extends CmmnProcessEngineTestCase {

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testCreateListenerByClass.cmmn"})
  public void testCreateListenerByClass() {
    // given

    // when
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    // then
    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("create").singleResult().getValue());
    assertEquals(1, query.variableName("createEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("createOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testCreateListenerByDelegateExpression.cmmn"})
  public void testCreateListenerByDelegateExpression() {
    // given

    // when
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .setVariable("myListener", new MySpecialCaseExecutionListener())
      .create()
      .getId();

    // then
    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("create").singleResult().getValue());
    assertEquals(1, query.variableName("createEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("createOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testCreateListenerByExpression.cmmn"})
  public void testCreateListenerByExpression() {
    // given

    // when
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .setVariable("myListener", new MyCaseExecutionListener())
      .create()
      .getId();

    // then
    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("create").singleResult().getValue());
    assertEquals(1, query.variableName("createEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("createOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testCreateListenerByScript.cmmn"})
  public void testCreateListenerByScript() {
    // given

    // when
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    // then
    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("create").singleResult().getValue());
    assertEquals(1, query.variableName("createEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("createOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testEnableListenerByClass.cmmn"})
  public void testEnableListenerByClass() {
    // given

    // when
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    // then
    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("enable").singleResult().getValue());
    assertEquals(1, query.variableName("enableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("enableOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testEnableListenerByDelegateExpression.cmmn"})
  public void testEnableListenerByDelegateExpression() {
    // given

    // when
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .setVariable("myListener", new MySpecialCaseExecutionListener())
      .create()
      .getId();

    // then
    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("enable").singleResult().getValue());
    assertEquals(1, query.variableName("enableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("enableOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testEnableListenerByExpression.cmmn"})
  public void testEnableListenerByExpression() {
    // given

    // when
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .setVariable("myListener", new MyCaseExecutionListener())
      .create()
      .getId();

    // then
    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("enable").singleResult().getValue());
    assertEquals(1, query.variableName("enableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("enableOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testEnableListenerByScript.cmmn"})
  public void testEnableListenerByScript() {
    // given

    // when
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    // then
    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("enable").singleResult().getValue());
    assertEquals(1, query.variableName("enableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("enableOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testDisableListenerByClass.cmmn"})
  public void testDisableListenerByClass() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .disable();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("disable").singleResult().getValue());
    assertEquals(1, query.variableName("disableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("disableOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testDisableListenerByDelegateExpression.cmmn"})
  public void testDisableListenerByDelegateExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MySpecialCaseExecutionListener())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .disable();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("disable").singleResult().getValue());
    assertEquals(1, query.variableName("disableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("disableOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testDisableListenerByExpression.cmmn"})
  public void testDisableListenerByExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MyCaseExecutionListener())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .disable();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("disable").singleResult().getValue());
    assertEquals(1, query.variableName("disableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("disableOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testDisableListenerByScript.cmmn"})
  public void testDisableListenerByScript() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .disable();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("disable").singleResult().getValue());
    assertEquals(1, query.variableName("disableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("disableOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testReEnableListenerByClass.cmmn"})
  public void testReEnableListenerByClass() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(humanTaskId)
      .disable();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .reenable();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("reenable").singleResult().getValue());
    assertEquals(1, query.variableName("reenableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("reenableOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testReEnableListenerByDelegateExpression.cmmn"})
  public void testReEnableListenerByDelegateExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MySpecialCaseExecutionListener())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(humanTaskId)
      .disable();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .reenable();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("reenable").singleResult().getValue());
    assertEquals(1, query.variableName("reenableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("reenableOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testReEnableListenerByExpression.cmmn"})
  public void testReEnableListenerByExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MyCaseExecutionListener())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(humanTaskId)
      .disable();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .reenable();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("reenable").singleResult().getValue());
    assertEquals(1, query.variableName("reenableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("reenableOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testReEnableListenerByScript.cmmn"})
  public void testReEnableListenerByScript() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    caseService
      .withCaseExecution(humanTaskId)
      .disable();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .reenable();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("reenable").singleResult().getValue());
    assertEquals(1, query.variableName("reenableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("reenableOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testStartListenerByClass.cmmn"})
  public void testStartListenerByClass() {
    // given

    // when
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    // then
    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("start").singleResult().getValue());
    assertEquals(1, query.variableName("startEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("startOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testStartListenerByDelegateExpression.cmmn"})
  public void testStartListenerByDelegateExpression() {
    // given

    // when
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .setVariable("myListener", new MySpecialCaseExecutionListener())
      .create()
      .getId();

    // then
    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("start").singleResult().getValue());
    assertEquals(1, query.variableName("startEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("startOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testStartListenerByExpression.cmmn"})
  public void testStartListenerByExpression() {
    // given

    // when
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .setVariable("myListener", new MyCaseExecutionListener())
      .create()
      .getId();

    // then
    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("start").singleResult().getValue());
    assertEquals(1, query.variableName("startEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("startOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testStartListenerByScript.cmmn"})
  public void testStartListenerByScript() {
    // given

    // when
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    // then
    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("start").singleResult().getValue());
    assertEquals(1, query.variableName("startEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("startOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testManualStartListenerByClass.cmmn"})
  public void testManualStartListenerByClass() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("manualStart").singleResult().getValue());
    assertEquals(1, query.variableName("manualStartEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("manualStartOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testManualStartListenerByDelegateExpression.cmmn"})
  public void testManualStartListenerByDelegateExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MySpecialCaseExecutionListener())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("manualStart").singleResult().getValue());
    assertEquals(1, query.variableName("manualStartEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("manualStartOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testManualStartListenerByExpression.cmmn"})
  public void testManualStartListenerByExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MyCaseExecutionListener())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("manualStart").singleResult().getValue());
    assertEquals(1, query.variableName("manualStartEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("manualStartOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testManualStartListenerByScript.cmmn"})
  public void testManualStartListenerByScript() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("manualStart").singleResult().getValue());
    assertEquals(1, query.variableName("manualStartEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("manualStartOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testCompleteListenerByClass.cmmn"})
  public void testCompleteListenerByClass() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("complete").singleResult().getValue());
    assertEquals(1, query.variableName("completeEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("completeOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testCompleteListenerByDelegateExpression.cmmn"})
  public void testCompleteListenerByDelegateExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MySpecialCaseExecutionListener())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("complete").singleResult().getValue());
    assertEquals(1, query.variableName("completeEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("completeOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testCompleteListenerByExpression.cmmn"})
  public void testCompleteListenerByExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MyCaseExecutionListener())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("complete").singleResult().getValue());
    assertEquals(1, query.variableName("completeEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("completeOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testCompleteListenerByScript.cmmn"})
  public void testCompleteListenerByScript() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("complete").singleResult().getValue());
    assertEquals(1, query.variableName("completeEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("completeOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testTerminateListenerByClass.cmmn"})
  public void testTerminateListenerByClass() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    terminate(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("terminate").singleResult().getValue());
    assertEquals(1, query.variableName("terminateEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("terminateOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testTerminateListenerByDelegateExpression.cmmn"})
  public void testTerminateListenerByDelegateExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MySpecialCaseExecutionListener())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    terminate(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("terminate").singleResult().getValue());
    assertEquals(1, query.variableName("terminateEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("terminateOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testTerminateListenerByExpression.cmmn"})
  public void testTerminateListenerByExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MyCaseExecutionListener())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    terminate(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("terminate").singleResult().getValue());
    assertEquals(1, query.variableName("terminateEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("terminateOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testTerminateListenerByScript.cmmn"})
  public void testTerminateListenerByScript() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    terminate(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("terminate").singleResult().getValue());
    assertEquals(1, query.variableName("terminateEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("terminateOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testExitListenerByClass.cmmn"})
  public void testExitListenerByClass() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    exit(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("exit").singleResult().getValue());
    assertEquals(1, query.variableName("exitEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("exitOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testExitListenerByDelegateExpression.cmmn"})
  public void testExitListenerByDelegateExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MySpecialCaseExecutionListener())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    exit(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("exit").singleResult().getValue());
    assertEquals(1, query.variableName("exitEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("exitOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testExitListenerByExpression.cmmn"})
  public void testExitListenerByExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MyCaseExecutionListener())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    exit(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("exit").singleResult().getValue());
    assertEquals(1, query.variableName("exitEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("exitOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testExitListenerByScript.cmmn"})
  public void testExitListenerByScript() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    exit(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("exit").singleResult().getValue());
    assertEquals(1, query.variableName("exitEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("exitOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testParentTerminateListenerByClass.cmmn"})
  public void testParentTerminateListenerByClass() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String milestoneId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Milestone_1")
        .singleResult()
        .getId();

    // when
    parentTerminate(milestoneId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("parentTerminate").singleResult().getValue());
    assertEquals(1, query.variableName("parentTerminateEventCounter").singleResult().getValue());
    assertEquals(milestoneId, query.variableName("parentTerminateOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testParentTerminateListenerByDelegateExpression.cmmn"})
  public void testParentTerminateListenerByDelegateExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MySpecialCaseExecutionListener())
        .create()
        .getId();

    String milestoneId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Milestone_1")
        .singleResult()
        .getId();

    // when
    parentTerminate(milestoneId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("parentTerminate").singleResult().getValue());
    assertEquals(1, query.variableName("parentTerminateEventCounter").singleResult().getValue());
    assertEquals(milestoneId, query.variableName("parentTerminateOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testParentTerminateListenerByExpression.cmmn"})
  public void testParentTerminateListenerByExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MyCaseExecutionListener())
        .create()
        .getId();

    String milestoneId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Milestone_1")
        .singleResult()
        .getId();

    // when
    parentTerminate(milestoneId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("parentTerminate").singleResult().getValue());
    assertEquals(1, query.variableName("parentTerminateEventCounter").singleResult().getValue());
    assertEquals(milestoneId, query.variableName("parentTerminateOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testParentTerminateListenerByScript.cmmn"})
  public void testParentTerminateListenerByScript() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String milestoneId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Milestone_1")
        .singleResult()
        .getId();

    // when
    parentTerminate(milestoneId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("parentTerminate").singleResult().getValue());
    assertEquals(1, query.variableName("parentTerminateEventCounter").singleResult().getValue());
    assertEquals(milestoneId, query.variableName("parentTerminateOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testSuspendListenerByClass.cmmn"})
  public void testSuspendListenerByClass() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    suspend(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("suspend").singleResult().getValue());
    assertEquals(1, query.variableName("suspendEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("suspendOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testSuspendListenerByDelegateExpression.cmmn"})
  public void testSuspendListenerByDelegateExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MySpecialCaseExecutionListener())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    suspend(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("suspend").singleResult().getValue());
    assertEquals(1, query.variableName("suspendEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("suspendOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testSuspendListenerByExpression.cmmn"})
  public void testSuspendListenerByExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MyCaseExecutionListener())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    suspend(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("suspend").singleResult().getValue());
    assertEquals(1, query.variableName("suspendEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("suspendOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testSuspendListenerByScript.cmmn"})
  public void testSuspendListenerByScript() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    suspend(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("suspend").singleResult().getValue());
    assertEquals(1, query.variableName("suspendEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("suspendOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testParentSuspendListenerByClass.cmmn"})
  public void testParentSuspendListenerByClass() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    parentSuspend(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("parentSuspend").singleResult().getValue());
    assertEquals(1, query.variableName("parentSuspendEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("parentSuspendOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testParentSuspendListenerByDelegateExpression.cmmn"})
  public void testParentSuspendListenerByDelegateExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MySpecialCaseExecutionListener())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    parentSuspend(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("parentSuspend").singleResult().getValue());
    assertEquals(1, query.variableName("parentSuspendEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("parentSuspendOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testParentSuspendListenerByExpression.cmmn"})
  public void testParentSuspendListenerByExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MyCaseExecutionListener())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    parentSuspend(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("parentSuspend").singleResult().getValue());
    assertEquals(1, query.variableName("parentSuspendEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("parentSuspendOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testParentSuspendListenerByScript.cmmn"})
  public void testParentSuspendListenerByScript() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    parentSuspend(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("parentSuspend").singleResult().getValue());
    assertEquals(1, query.variableName("parentSuspendEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("parentSuspendOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testResumeListenerByClass.cmmn"})
  public void testResumeListenerByClass() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    suspend(humanTaskId);

    // when
    resume(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("resume").singleResult().getValue());
    assertEquals(1, query.variableName("resumeEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("resumeOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testResumeListenerByDelegateExpression.cmmn"})
  public void testResumeListenerByDelegateExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MySpecialCaseExecutionListener())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    suspend(humanTaskId);

    // when
    resume(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("resume").singleResult().getValue());
    assertEquals(1, query.variableName("resumeEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("resumeOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testResumeListenerByExpression.cmmn"})
  public void testResumeListenerByExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MyCaseExecutionListener())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    suspend(humanTaskId);

    // when
    resume(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("resume").singleResult().getValue());
    assertEquals(1, query.variableName("resumeEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("resumeOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testResumeListenerByScript.cmmn"})
  public void testResumeListenerByScript() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    suspend(humanTaskId);

    // when
    resume(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("resume").singleResult().getValue());
    assertEquals(1, query.variableName("resumeEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("resumeOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testParentResumeListenerByClass.cmmn"})
  public void testParentResumeListenerByClass() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    suspend(humanTaskId);

    // when
    parentResume(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("parentResume").singleResult().getValue());
    assertEquals(1, query.variableName("parentResumeEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("parentResumeOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testParentResumeListenerByDelegateExpression.cmmn"})
  public void testParentResumeListenerByDelegateExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MySpecialCaseExecutionListener())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    suspend(humanTaskId);

    // when
    parentResume(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("parentResume").singleResult().getValue());
    assertEquals(1, query.variableName("parentResumeEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("parentResumeOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testParentResumeListenerByExpression.cmmn"})
  public void testParentResumeListenerByExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MyCaseExecutionListener())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    suspend(humanTaskId);

    // when
    parentResume(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("parentResume").singleResult().getValue());
    assertEquals(1, query.variableName("parentResumeEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("parentResumeOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testParentResumeListenerByScript.cmmn"})
  public void testParentResumeListenerByScript() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    suspend(humanTaskId);

    // when
    parentResume(humanTaskId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("parentResume").singleResult().getValue());
    assertEquals(1, query.variableName("parentResumeEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("parentResumeOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testOccurListenerByClass.cmmn"})
  public void testOccurListenerByClass() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String milestoneId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Milestone_1")
        .singleResult()
        .getId();

    // when
    occur(milestoneId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("occur").singleResult().getValue());
    assertEquals(1, query.variableName("occurEventCounter").singleResult().getValue());
    assertEquals(milestoneId, query.variableName("occurOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testOccurListenerByDelegateExpression.cmmn"})
  public void testOccurListenerByDelegateExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MySpecialCaseExecutionListener())
        .create()
        .getId();

    String milestoneId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Milestone_1")
        .singleResult()
        .getId();

    // when
    occur(milestoneId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("occur").singleResult().getValue());
    assertEquals(1, query.variableName("occurEventCounter").singleResult().getValue());
    assertEquals(milestoneId, query.variableName("occurOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testOccurListenerByExpression.cmmn"})
  public void testOccurListenerByExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MyCaseExecutionListener())
        .create()
        .getId();

    String milestoneId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Milestone_1")
        .singleResult()
        .getId();

    // when
    occur(milestoneId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertTrue((Boolean) query.variableName("occur").singleResult().getValue());
    assertEquals(1, query.variableName("occurEventCounter").singleResult().getValue());
    assertEquals(milestoneId, query.variableName("occurOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testOccurListenerByScript.cmmn"})
  public void testOccurListenerByScript() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String milestoneId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_Milestone_1")
        .singleResult()
        .getId();

    // when
    occur(milestoneId);

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertTrue((Boolean) query.variableName("occur").singleResult().getValue());
    assertEquals(1, query.variableName("occurEventCounter").singleResult().getValue());
    assertEquals(milestoneId, query.variableName("occurOnCaseExecutionId").singleResult().getValue());

    assertEquals(1, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testAllListenerByClass.cmmn"})
  public void testAllListenerByClass() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when

    caseService
      .withCaseExecution(humanTaskId)
      .disable();

    caseService
      .withCaseExecution(humanTaskId)
      .reenable();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    suspend(humanTaskId);

    resume(humanTaskId);

    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(25, query.count());

    assertTrue((Boolean) query.variableName("create").singleResult().getValue());
    assertEquals(1, query.variableName("createEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("createOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("enable").singleResult().getValue());
    assertEquals(1, query.variableName("enableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("enableOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("disable").singleResult().getValue());
    assertEquals(1, query.variableName("disableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("disableOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("reenable").singleResult().getValue());
    assertEquals(1, query.variableName("reenableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("reenableOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("manualStart").singleResult().getValue());
    assertEquals(1, query.variableName("manualStartEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("manualStartOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("suspend").singleResult().getValue());
    assertEquals(1, query.variableName("suspendEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("suspendOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("resume").singleResult().getValue());
    assertEquals(1, query.variableName("resumeEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("resumeOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("complete").singleResult().getValue());
    assertEquals(1, query.variableName("completeEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("completeOnCaseExecutionId").singleResult().getValue());

    assertEquals(8, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testAllListenerByDelegateExpression.cmmn"})
  public void testAllListenerByDelegateExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MySpecialCaseExecutionListener())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when

    caseService
      .withCaseExecution(humanTaskId)
      .disable();

    caseService
      .withCaseExecution(humanTaskId)
      .reenable();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    suspend(humanTaskId);

    resume(humanTaskId);

    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(26, query.count());

    assertTrue((Boolean) query.variableName("create").singleResult().getValue());
    assertEquals(1, query.variableName("createEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("createOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("enable").singleResult().getValue());
    assertEquals(1, query.variableName("enableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("enableOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("disable").singleResult().getValue());
    assertEquals(1, query.variableName("disableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("disableOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("reenable").singleResult().getValue());
    assertEquals(1, query.variableName("reenableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("reenableOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("manualStart").singleResult().getValue());
    assertEquals(1, query.variableName("manualStartEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("manualStartOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("suspend").singleResult().getValue());
    assertEquals(1, query.variableName("suspendEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("suspendOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("resume").singleResult().getValue());
    assertEquals(1, query.variableName("resumeEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("resumeOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("complete").singleResult().getValue());
    assertEquals(1, query.variableName("completeEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("completeOnCaseExecutionId").singleResult().getValue());

    assertEquals(8, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testAllListenerByExpression.cmmn"})
  public void testAllListenerByExpression() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new MyCaseExecutionListener())
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when

    caseService
      .withCaseExecution(humanTaskId)
      .disable();

    caseService
      .withCaseExecution(humanTaskId)
      .reenable();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    suspend(humanTaskId);

    resume(humanTaskId);

    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(26, query.count());

    assertTrue((Boolean) query.variableName("create").singleResult().getValue());
    assertEquals(1, query.variableName("createEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("createOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("enable").singleResult().getValue());
    assertEquals(1, query.variableName("enableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("enableOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("disable").singleResult().getValue());
    assertEquals(1, query.variableName("disableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("disableOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("reenable").singleResult().getValue());
    assertEquals(1, query.variableName("reenableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("reenableOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("manualStart").singleResult().getValue());
    assertEquals(1, query.variableName("manualStartEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("manualStartOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("suspend").singleResult().getValue());
    assertEquals(1, query.variableName("suspendEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("suspendOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("resume").singleResult().getValue());
    assertEquals(1, query.variableName("resumeEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("resumeOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("complete").singleResult().getValue());
    assertEquals(1, query.variableName("completeEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("completeOnCaseExecutionId").singleResult().getValue());

    assertEquals(8, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testAllListenerByScript.cmmn"})
  public void testAllListenerByScript() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when

    caseService
      .withCaseExecution(humanTaskId)
      .disable();

    caseService
      .withCaseExecution(humanTaskId)
      .reenable();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    suspend(humanTaskId);

    resume(humanTaskId);

    caseService
      .withCaseExecution(humanTaskId)
      .complete();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(25, query.count());

    assertTrue((Boolean) query.variableName("create").singleResult().getValue());
    assertEquals(1, query.variableName("createEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("createOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("enable").singleResult().getValue());
    assertEquals(1, query.variableName("enableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("enableOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("disable").singleResult().getValue());
    assertEquals(1, query.variableName("disableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("disableOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("reenable").singleResult().getValue());
    assertEquals(1, query.variableName("reenableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("reenableOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("manualStart").singleResult().getValue());
    assertEquals(1, query.variableName("manualStartEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("manualStartOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("suspend").singleResult().getValue());
    assertEquals(1, query.variableName("suspendEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("suspendOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("resume").singleResult().getValue());
    assertEquals(1, query.variableName("resumeEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("resumeOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("complete").singleResult().getValue());
    assertEquals(1, query.variableName("completeEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("completeOnCaseExecutionId").singleResult().getValue());

    assertEquals(8, query.variableName("eventCounter").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testFieldInjectionByClass.cmmn"})
  public void testFieldInjectionByClass() {
    // given

    // when
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .create()
      .getId();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(4, query.count());

    assertEquals("Hello from The Case", query.variableName("greeting").singleResult().getValue());
    assertEquals("Hello World", query.variableName("helloWorld").singleResult().getValue());
    assertEquals("cam", query.variableName("prefix").singleResult().getValue());
    assertEquals("unda", query.variableName("suffix").singleResult().getValue());

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testFieldInjectionByDelegateExpression.cmmn"})
  public void testFieldInjectionByDelegateExpression() {
    // given

    // when
    String caseInstanceId = caseService
      .withCaseDefinitionByKey("case")
      .setVariable("myListener", new FieldInjectionCaseExecutionListener())
      .create()
      .getId();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(5, query.count());

    assertEquals("Hello from The Case", query.variableName("greeting").singleResult().getValue());
    assertEquals("Hello World", query.variableName("helloWorld").singleResult().getValue());
    assertEquals("cam", query.variableName("prefix").singleResult().getValue());
    assertEquals("unda", query.variableName("suffix").singleResult().getValue());

  }

  @Deployment(resources = {
      "org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testListenerByScriptResource.cmmn",
      "org/camunda/bpm/engine/test/cmmn/listener/caseExecutionListener.groovy"
      })
  public void testListenerByScriptResource() {
    // given
    String caseInstanceId = caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();

    String humanTaskId = caseService
        .createCaseExecutionQuery()
        .activityId("PI_HumanTask_1")
        .singleResult()
        .getId();

    // when
    caseService
      .withCaseExecution(humanTaskId)
      .disable();

    caseService
      .withCaseExecution(humanTaskId)
      .reenable();

    caseService
      .withCaseExecution(humanTaskId)
      .manualStart();

    // then
    VariableInstanceQuery query = runtimeService
        .createVariableInstanceQuery()
        .caseInstanceIdIn(caseInstanceId);

    assertEquals(10, query.count());

    assertTrue((Boolean) query.variableName("disable").singleResult().getValue());
    assertEquals(1, query.variableName("disableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("disableOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("reenable").singleResult().getValue());
    assertEquals(1, query.variableName("reenableEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("reenableOnCaseExecutionId").singleResult().getValue());

    assertTrue((Boolean) query.variableName("manualStart").singleResult().getValue());
    assertEquals(1, query.variableName("manualStartEventCounter").singleResult().getValue());
    assertEquals(humanTaskId, query.variableName("manualStartOnCaseExecutionId").singleResult().getValue());

    assertEquals(3, query.variableName("eventCounter").singleResult().getValue());
  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testDoesNotImplementCaseExecutionListenerInterfaceByClass.cmmn"})
  public void testDoesNotImplementCaseExecutionListenerInterfaceByClass() {
    // given


    try {
      // when
      caseService
        .withCaseDefinitionByKey("case")
        .create();
    } catch (Exception e) {
      // then
      String message = e.getMessage();
      assertTextPresent("ENGINE-05016 Class 'org.camunda.bpm.engine.test.cmmn.listener.NotCaseExecutionListener' doesn't implement '" + CaseExecutionListener.class.getName() + "'", message);
    }

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testDoesNotImplementCaseExecutionListenerInterfaceByDelegateExpression.cmmn"})
  public void testDoesNotImplementCaseExecutionListenerInterfaceByDelegateExpression() {
    // given

    try {
      // when
      caseService
        .withCaseDefinitionByKey("case")
        .setVariable("myListener", new NotCaseExecutionListener())
        .create();
    } catch (Exception e) {
      // then
      String message = e.getMessage();
      assertTextPresent("Delegate expression ${myListener} did not resolve to an implementation of interface "+CaseExecutionListener.class.getName(), message);
    }

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testListenerDoesNotExist.cmmn"})
  public void testListenerDoesNotExist() {
    // given

    try {
      // when
      caseService
        .withCaseDefinitionByKey("case")
        .create()
        .getId();
    } catch (Exception e) {
      // then
      String message = e.getMessage();
      assertTextPresent("Exception while instantiating class 'org.camunda.bpm.engine.test.cmmn.listener.NotExistingCaseExecutionListener'", message);
    }

  }

  @Deployment(resources = {"org/camunda/bpm/engine/test/cmmn/listener/CaseExecutionListenerTest.testBusinessKeyAsCaseBusinessKey.cmmn"})
  public void testBusinessKeyAsCaseBusinessKey() {
    // given

    // when
    caseService.withCaseDefinitionByKey("case")
      .businessKey("myBusinessKey")
      .create()
      .getId();

    // then
    VariableInstance v1 = runtimeService.createVariableInstanceQuery().variableName("businessKey").singleResult();
    VariableInstance v2 = runtimeService.createVariableInstanceQuery().variableName("caseBusinessKey").singleResult();
    assertNotNull(v1);
    assertNotNull(v2);
    assertEquals("myBusinessKey", v1.getValue());
    assertEquals(v1.getValue(), v2.getValue());
  }

}
