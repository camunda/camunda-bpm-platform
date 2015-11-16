/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
 * Abstract base class for implementing a {@link CmmnTransformListener} without being forced to implement
 * all methods provided, which make the implementation more robust to future changes.
 *
 * @author Sebastian Menski
 */
public class AbstractCmmnTransformListener implements CmmnTransformListener {

  public void transformRootElement(Definitions definitions, List<? extends CmmnCaseDefinition> caseDefinitions) {
  }

  public void transformCase(Case element, CmmnCaseDefinition caseDefinition) {
  }

  public void transformCasePlanModel(org.camunda.bpm.model.cmmn.impl.instance.CasePlanModel casePlanModel, CmmnActivity activity) {
    transformCasePlanModel((org.camunda.bpm.model.cmmn.instance.CasePlanModel) casePlanModel, activity);
  }

  public void transformCasePlanModel(CasePlanModel casePlanModel, CmmnActivity activity) {
  }

  public void transformHumanTask(PlanItem planItem, HumanTask humanTask, CmmnActivity activity) {
  }

  public void transformProcessTask(PlanItem planItem, ProcessTask processTask, CmmnActivity activity) {
  }

  public void transformCaseTask(PlanItem planItem, CaseTask caseTask, CmmnActivity activity) {
  }

  public void transformDecisionTask(PlanItem planItem, DecisionTask decisionTask, CmmnActivity activity) {
  }

  public void transformTask(PlanItem planItem, Task task, CmmnActivity activity) {
  }

  public void transformStage(PlanItem planItem, Stage stage, CmmnActivity activity) {
  }

  public void transformMilestone(PlanItem planItem, Milestone milestone, CmmnActivity activity) {
  }

  public void transformEventListener(PlanItem planItem, EventListener eventListener, CmmnActivity activity) {
  }

  public void transformSentry(Sentry sentry, CmmnSentryDeclaration sentryDeclaration) {
  }

}
