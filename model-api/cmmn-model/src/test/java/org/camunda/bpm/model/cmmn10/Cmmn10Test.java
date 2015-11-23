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
package org.camunda.bpm.model.cmmn10;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Collection;

import org.camunda.bpm.model.cmmn.Cmmn;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.cmmn.impl.CmmnModelConstants;
import org.camunda.bpm.model.cmmn.instance.Case;
import org.camunda.bpm.model.cmmn.instance.CasePlanModel;
import org.camunda.bpm.model.cmmn.instance.CaseRole;
import org.camunda.bpm.model.cmmn.instance.CaseRoles;
import org.camunda.bpm.model.cmmn.instance.ConditionExpression;
import org.camunda.bpm.model.cmmn.instance.Documentation;
import org.camunda.bpm.model.cmmn.instance.EntryCriterion;
import org.camunda.bpm.model.cmmn.instance.Event;
import org.camunda.bpm.model.cmmn.instance.EventListener;
import org.camunda.bpm.model.cmmn.instance.ExitCriterion;
import org.camunda.bpm.model.cmmn.instance.HumanTask;
import org.camunda.bpm.model.cmmn.instance.IfPart;
import org.camunda.bpm.model.cmmn.instance.InputCaseParameter;
import org.camunda.bpm.model.cmmn.instance.InputsCaseParameter;
import org.camunda.bpm.model.cmmn.instance.OutputCaseParameter;
import org.camunda.bpm.model.cmmn.instance.OutputsCaseParameter;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.Sentry;
import org.camunda.bpm.model.cmmn.instance.TimerEvent;
import org.camunda.bpm.model.cmmn.instance.UserEvent;
import org.junit.Test;

/**
 * @author Roman Smirnov
 *
 */
public class Cmmn10Test {

  @Test
  public void shouldGetCasePlanModelExitCriterion() {
    CmmnModelInstance modelInstance = getCmmnModelInstance();
    CasePlanModel casePlanModel = modelInstance.getModelElementsByType(CasePlanModel.class).iterator().next();

    Collection<Sentry> exitCriterias = casePlanModel.getExitCriterias();
    assertThat(exitCriterias).hasSize(1);

    Collection<Sentry> exitCriteria = casePlanModel.getExitCriteria();
    assertThat(exitCriteria).hasSize(1);

    Collection<ExitCriterion> exitCriterions = casePlanModel.getExitCriterions();
    assertThat(exitCriterions).isEmpty();
  }

  @Test
  public void shouldGetPlanItemExitCriterion() {
    CmmnModelInstance modelInstance = getCmmnModelInstance();
    PlanItem planItem = modelInstance.getModelElementsByType(PlanItem.class).iterator().next();

    Collection<Sentry> exitCriterias = planItem.getExitCriterias();
    assertThat(exitCriterias).hasSize(1);

    Collection<Sentry> exitCriteria = planItem.getExitCriteria();
    assertThat(exitCriteria).hasSize(1);

    Collection<ExitCriterion> exitCriterions = planItem.getExitCriterions();
    assertThat(exitCriterions).isEmpty();
  }

  @Test
  public void shouldGetPlanItemEntryCriterion() {
    CmmnModelInstance modelInstance = getCmmnModelInstance();
    PlanItem planItem = modelInstance.getModelElementsByType(PlanItem.class).iterator().next();

    Collection<Sentry> entryCriterias = planItem.getEntryCriterias();
    assertThat(entryCriterias).hasSize(1);

    Collection<Sentry> entryCriteria = planItem.getEntryCriteria();
    assertThat(entryCriteria).hasSize(1);

    Collection<EntryCriterion> entryCriterions = planItem.getEntryCriterions();
    assertThat(entryCriterions).isEmpty();
  }

  @Test
  public void shouldGetTaskInputsOutputs() {
    CmmnModelInstance modelInstance = getCmmnModelInstance();
    HumanTask humanTask = modelInstance.getModelElementsByType(HumanTask.class).iterator().next();

    Collection<InputsCaseParameter> inputs = humanTask.getInputs();
    assertThat(inputs).hasSize(1);

    Collection<InputCaseParameter> inputParameters = humanTask.getInputParameters();
    assertThat(inputParameters).isEmpty();

    Collection<OutputsCaseParameter> outputs = humanTask.getOutputs();
    assertThat(outputs).hasSize(1);

    Collection<OutputCaseParameter> outputParameters = humanTask.getOutputParameters();
    assertThat(outputParameters).isEmpty();
  }

  @Test
  public void shouldGetEvents() {
    CmmnModelInstance modelInstance = getCmmnModelInstance();

    Event event = modelInstance.getModelElementsByType(Event.class).iterator().next();
    assertThat(event).isNotNull();

    UserEvent userEvent = modelInstance.getModelElementsByType(UserEvent.class).iterator().next();
    assertThat(userEvent).isNotNull();

    TimerEvent timerEvent = modelInstance.getModelElementsByType(TimerEvent.class).iterator().next();
    assertThat(timerEvent).isNotNull();
  }

  @Test
  public void shouldGetDescription() {
    CmmnModelInstance modelInstance = getCmmnModelInstance();
    CasePlanModel casePlanModel = modelInstance.getModelElementsByType(CasePlanModel.class).iterator().next();

    String description = casePlanModel.getDescription();
    assertThat(description).isEqualTo("This is a description...");

    Collection<Documentation> documentations = casePlanModel.getDocumentations();
    assertThat(documentations).isEmpty();
  }

  @Test
  public void shouldGetMultipleIfPartConditions() {
    CmmnModelInstance modelInstance = getCmmnModelInstance();
    Sentry sentry = modelInstance.getModelElementsByType(Sentry.class).iterator().next();

    IfPart ifPart = sentry.getIfPart();
    assertThat(ifPart).isNotNull();

    Collection<ConditionExpression> conditions = ifPart.getConditions();
    assertThat(conditions).hasSize(2);

    ConditionExpression condition = ifPart.getCondition();
    assertThat(condition).isNotNull();
  }

  @Test
  public void shouldGetCaseRoles() {
    CmmnModelInstance modelInstance = getCmmnModelInstance();
    Case _case = modelInstance.getModelElementsByType(Case.class).iterator().next();

    Collection<CaseRole> roles = _case.getCaseRoles();
    assertThat(roles).hasSize(2);

    CaseRoles caseRole = _case.getRoles();
    assertThat(caseRole).isNull();
  }

  @Test
  public void shouldGetExpressionTextContent() {
    CmmnModelInstance modelInstance = getCmmnModelInstance();
    ConditionExpression expression = modelInstance.getModelElementsByType(ConditionExpression.class).iterator().next();

    assertThat(expression.getBody()).isEqualTo("${value >= 100}");
    assertThat(expression.getText()).isEqualTo("${value >= 100}");
  }

  @Test
  public void shouldNotAbleToAddNewElement() {
    CmmnModelInstance modelInstance = getCmmnModelInstance();
    CasePlanModel casePlanModel = modelInstance.getModelElementsByType(CasePlanModel.class).iterator().next();

    HumanTask humanTask = modelInstance.newInstance(HumanTask.class);
    casePlanModel.getPlanItemDefinitions().add(humanTask);

    try {
      Cmmn.writeModelToStream(System.out, modelInstance);
      fail("cannot save cmmn 1.0 model");
    }
    catch (Exception e) {
      // expected exception
    }
  }

  @Test
  public void shouldReturnCmmn11Namespace() {
    CmmnModelInstance modelInstance = getCmmnModelInstance();
    CasePlanModel casePlanModel = modelInstance.getModelElementsByType(CasePlanModel.class).iterator().next();

    assertThat(casePlanModel.getElementType().getTypeNamespace()).isEqualTo(CmmnModelConstants.CMMN11_NS);
  }

  @Test
  public void shouldNotAbleToAddCmmn10Element() {
    CmmnModelInstance modelInstance = Cmmn.readModelFromStream(Cmmn10Test.class.getResourceAsStream("Cmmn11Test.cmmn"));
    CasePlanModel casePlanModel = modelInstance.getModelElementsByType(CasePlanModel.class).iterator().next();

    Event event = modelInstance.newInstance(Event.class);
    casePlanModel.getPlanItemDefinitions().add(event);

    try {
      Cmmn.writeModelToStream(System.out, modelInstance);
      fail("cannot save cmmn 1.1 model");
    }
    catch (Exception e) {
      // expected exception
    }
  }

  protected CmmnModelInstance getCmmnModelInstance() {
    return Cmmn.readModelFromStream(Cmmn10Test.class.getResourceAsStream("Cmmn10Test.cmmn"));
  }

}
