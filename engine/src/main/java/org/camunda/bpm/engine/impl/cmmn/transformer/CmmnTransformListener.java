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
package org.camunda.bpm.engine.impl.cmmn.transformer;

import java.util.List;

import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnSentryDeclaration;
import org.camunda.bpm.model.cmmn.instance.Case;
import org.camunda.bpm.model.cmmn.instance.CasePlanModel;
import org.camunda.bpm.model.cmmn.instance.CaseTask;
import org.camunda.bpm.model.cmmn.instance.DecisionTask;
import org.camunda.bpm.model.cmmn.instance.Definitions;
import org.camunda.bpm.model.cmmn.instance.EventListener;
import org.camunda.bpm.model.cmmn.instance.HumanTask;
import org.camunda.bpm.model.cmmn.instance.Milestone;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.ProcessTask;
import org.camunda.bpm.model.cmmn.instance.Sentry;
import org.camunda.bpm.model.cmmn.instance.Stage;
import org.camunda.bpm.model.cmmn.instance.Task;

/**
 * Listener which can be registered within the engine to receive events during transforming (and
 * maybe influence it). Instead of implementing this interface you might consider to extend
 * the {@link AbstractCmmnTransformListener}, which contains an empty implementation for all methods
 * and makes your implementation easier and more robust to future changes.
 *
 * @author Sebastian Menski
 *
 */
public interface CmmnTransformListener {

  void transformRootElement(Definitions definitions, List<? extends CmmnCaseDefinition> caseDefinitions);

  void transformCase(Case element, CmmnCaseDefinition caseDefinition);

  /**
   * @deprecated use {@link #transformCasePlanModel(org.camunda.bpm.model.cmmn.instance.CasePlanModel, CmmnActivity)}
   */
  @Deprecated
  void transformCasePlanModel(org.camunda.bpm.model.cmmn.impl.instance.CasePlanModel casePlanModel, CmmnActivity caseActivity);

  void transformCasePlanModel(CasePlanModel casePlanModel, CmmnActivity caseActivity);

  void transformHumanTask(PlanItem planItem, HumanTask humanTask, CmmnActivity caseActivity);

  void transformProcessTask(PlanItem planItem, ProcessTask processTask, CmmnActivity caseActivity);

  void transformCaseTask(PlanItem planItem, CaseTask caseTask, CmmnActivity caseActivity);

  void transformDecisionTask(PlanItem planItem, DecisionTask decisionTask, CmmnActivity caseActivity);

  void transformTask(PlanItem planItem, Task task, CmmnActivity caseActivity);

  void transformStage(PlanItem planItem, Stage stage, CmmnActivity caseActivity);

  void transformMilestone(PlanItem planItem, Milestone milestone, CmmnActivity caseActivity);

  void transformEventListener(PlanItem planItem, EventListener eventListener, CmmnActivity caseActivity);

  void transformSentry(Sentry sentry, CmmnSentryDeclaration sentryDeclaration);

}
