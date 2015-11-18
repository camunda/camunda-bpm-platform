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
package org.camunda.bpm.engine.impl.metrics.parser;

import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.transformer.AbstractCmmnTransformListener;
import org.camunda.bpm.model.cmmn.instance.CaseTask;
import org.camunda.bpm.model.cmmn.instance.DecisionTask;
import org.camunda.bpm.model.cmmn.instance.HumanTask;
import org.camunda.bpm.model.cmmn.instance.Milestone;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.ProcessTask;
import org.camunda.bpm.model.cmmn.instance.Stage;
import org.camunda.bpm.model.cmmn.instance.Task;

/**
 * @author Daniel Meyer
 *
 */
public class MetricsCmmnTransformListener extends AbstractCmmnTransformListener {

  public static MetricsCaseExecutionListener listener = new MetricsCaseExecutionListener();

  protected void addListeners(CmmnActivity activity) {
    if(activity != null) {
      activity.addBuiltInListener(CaseExecutionListener.START, listener);
      activity.addBuiltInListener(CaseExecutionListener.MANUAL_START, listener);
      activity.addBuiltInListener(CaseExecutionListener.OCCUR, listener);
    }
  }

  public void transformHumanTask(PlanItem planItem, HumanTask humanTask, CmmnActivity activity) {
    addListeners(activity);
  }

  public void transformProcessTask(PlanItem planItem, ProcessTask processTask, CmmnActivity activity) {
    addListeners(activity);
  }

  public void transformCaseTask(PlanItem planItem, CaseTask caseTask, CmmnActivity activity) {
    addListeners(activity);
  }

  public void transformDecisionTask(PlanItem planItem, DecisionTask decisionTask, CmmnActivity activity) {
    addListeners(activity);
  }

  public void transformTask(PlanItem planItem, Task task, CmmnActivity activity) {
    addListeners(activity);
  }

  public void transformStage(PlanItem planItem, Stage stage, CmmnActivity activity) {
    addListeners(activity);
  }

  public void transformMilestone(PlanItem planItem, Milestone milestone, CmmnActivity activity) {
    addListeners(activity);
  }

}
