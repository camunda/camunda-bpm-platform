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

package org.camunda.bpm.engine.impl.history.transformer;

import java.util.List;

import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnSentryDeclaration;
import org.camunda.bpm.engine.impl.cmmn.transformer.CmmnTransformListener;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.producer.CmmnHistoryEventProducer;
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
 * @author Sebastian Menski
 */
public class CmmnHistoryTransformListener implements CmmnTransformListener {

  // Cached listeners
  // listeners can be reused for a given process engine instance but cannot be cached in static fields since
  // different process engine instances on the same Classloader may have different HistoryEventProducer
  // configurations wired
  protected CaseExecutionListener CASE_INSTANCE_CREATE_LISTENER;
  protected CaseExecutionListener CASE_INSTANCE_UPDATE_LISTENER;
  protected CaseExecutionListener CASE_INSTANCE_CLOSE_LISTENER;

  protected CaseExecutionListener CASE_ACTIVITY_INSTANCE_CREATE_LISTENER;
  protected CaseExecutionListener CASE_ACTIVITY_INSTANCE_UPDATE_LISTENER;
  protected CaseExecutionListener CASE_ACTIVITY_INSTANCE_END_LISTENER;

  // The history level set in the process engine configuration
  protected HistoryLevel historyLevel;

  public CmmnHistoryTransformListener(HistoryLevel historyLevel, CmmnHistoryEventProducer historyEventProducer) {
    this.historyLevel = historyLevel;
    initCaseExecutionListeners(historyEventProducer, historyLevel);
  }

  protected void initCaseExecutionListeners(CmmnHistoryEventProducer historyEventProducer, HistoryLevel historyLevel) {
    CASE_INSTANCE_CREATE_LISTENER = new CaseInstanceCreateListener(historyEventProducer, historyLevel);
    CASE_INSTANCE_UPDATE_LISTENER = new CaseInstanceUpdateListener(historyEventProducer, historyLevel);
    CASE_INSTANCE_CLOSE_LISTENER = new CaseInstanceCloseListener(historyEventProducer, historyLevel);

    CASE_ACTIVITY_INSTANCE_CREATE_LISTENER = new CaseActivityInstanceCreateListener(historyEventProducer, historyLevel);
    CASE_ACTIVITY_INSTANCE_UPDATE_LISTENER = new CaseActivityInstanceUpdateListener(historyEventProducer, historyLevel);
    CASE_ACTIVITY_INSTANCE_END_LISTENER = new CaseActivityInstanceEndListener(historyEventProducer, historyLevel);
  }

  public void transformRootElement(Definitions definitions, List<? extends CmmnCaseDefinition> caseDefinitions) {
  }

  public void transformCase(Case element, CmmnCaseDefinition caseDefinition) {
  }

  public void transformCasePlanModel(org.camunda.bpm.model.cmmn.impl.instance.CasePlanModel casePlanModel, CmmnActivity caseActivity) {
    transformCasePlanModel((org.camunda.bpm.model.cmmn.instance.CasePlanModel) casePlanModel, caseActivity);
  }

  public void transformCasePlanModel(CasePlanModel casePlanModel, CmmnActivity caseActivity) {
    addCasePlanModelHandlers(caseActivity);
  }

  public void transformHumanTask(PlanItem planItem, HumanTask humanTask, CmmnActivity caseActivity) {
    addTaskOrStageHandlers(caseActivity);
  }

  public void transformProcessTask(PlanItem planItem, ProcessTask processTask, CmmnActivity caseActivity) {
    addTaskOrStageHandlers(caseActivity);
  }

  public void transformCaseTask(PlanItem planItem, CaseTask caseTask, CmmnActivity caseActivity) {
    addTaskOrStageHandlers(caseActivity);
  }

  public void transformDecisionTask(PlanItem planItem, DecisionTask decisionTask, CmmnActivity caseActivity) {
    addTaskOrStageHandlers(caseActivity);
  }

  public void transformTask(PlanItem planItem, Task task, CmmnActivity caseActivity) {
    addTaskOrStageHandlers(caseActivity);
  }

  public void transformStage(PlanItem planItem, Stage stage, CmmnActivity caseActivity) {
    addTaskOrStageHandlers(caseActivity);
  }

  public void transformMilestone(PlanItem planItem, Milestone milestone, CmmnActivity caseActivity) {
    addEventListenerOrMilestoneHandlers(caseActivity);
  }

  public void transformEventListener(PlanItem planItem, EventListener eventListener, CmmnActivity caseActivity) {
    addEventListenerOrMilestoneHandlers(caseActivity);
  }

  public void transformSentry(Sentry sentry, CmmnSentryDeclaration sentryDeclaration) {
  }

  protected void addCasePlanModelHandlers(CmmnActivity caseActivity) {
    if (caseActivity != null) {
      if (historyLevel.isHistoryEventProduced(HistoryEventTypes.CASE_INSTANCE_CREATE, null)) {
        for (String event : ItemHandler.CASE_PLAN_MODEL_CREATE_EVENTS) {
          caseActivity.addBuiltInListener(event, CASE_INSTANCE_CREATE_LISTENER);
        }
      }
      if (historyLevel.isHistoryEventProduced(HistoryEventTypes.CASE_INSTANCE_UPDATE, null)) {
        for (String event : ItemHandler.CASE_PLAN_MODEL_UPDATE_EVENTS) {
          caseActivity.addBuiltInListener(event, CASE_INSTANCE_UPDATE_LISTENER);
        }
      }
      if (historyLevel.isHistoryEventProduced(HistoryEventTypes.CASE_INSTANCE_CLOSE, null)) {
        for (String event : ItemHandler.CASE_PLAN_MODEL_CLOSE_EVENTS) {
          caseActivity.addBuiltInListener(event, CASE_INSTANCE_CLOSE_LISTENER);
        }
      }
    }
  }

  protected void addTaskOrStageHandlers(CmmnActivity caseActivity) {
    if (caseActivity != null) {
      if (historyLevel.isHistoryEventProduced(HistoryEventTypes.CASE_ACTIVITY_INSTANCE_CREATE, null)) {
        for (String event : ItemHandler.TASK_OR_STAGE_CREATE_EVENTS) {
          caseActivity.addBuiltInListener(event, CASE_ACTIVITY_INSTANCE_CREATE_LISTENER);
        }
      }
      if (historyLevel.isHistoryEventProduced(HistoryEventTypes.CASE_ACTIVITY_INSTANCE_UPDATE, null)) {
        for (String event : ItemHandler.TASK_OR_STAGE_UPDATE_EVENTS) {
          caseActivity.addBuiltInListener(event, CASE_ACTIVITY_INSTANCE_UPDATE_LISTENER);
        }
      }
      if (historyLevel.isHistoryEventProduced(HistoryEventTypes.CASE_ACTIVITY_INSTANCE_END, null)) {
        for (String event : ItemHandler.TASK_OR_STAGE_END_EVENTS) {
          caseActivity.addBuiltInListener(event, CASE_ACTIVITY_INSTANCE_END_LISTENER);
        }
      }
    }
  }

  protected void addEventListenerOrMilestoneHandlers(CmmnActivity caseActivity) {
    if (caseActivity != null) {
      if (historyLevel.isHistoryEventProduced(HistoryEventTypes.CASE_ACTIVITY_INSTANCE_CREATE, null)) {
        for (String event : ItemHandler.EVENT_LISTENER_OR_MILESTONE_CREATE_EVENTS) {
          caseActivity.addBuiltInListener(event, CASE_ACTIVITY_INSTANCE_CREATE_LISTENER);
        }
      }
      if (historyLevel.isHistoryEventProduced(HistoryEventTypes.CASE_ACTIVITY_INSTANCE_UPDATE, null)) {
        for (String event : ItemHandler.EVENT_LISTENER_OR_MILESTONE_UPDATE_EVENTS) {
          caseActivity.addBuiltInListener(event, CASE_ACTIVITY_INSTANCE_UPDATE_LISTENER);
        }
      }
      if (historyLevel.isHistoryEventProduced(HistoryEventTypes.CASE_ACTIVITY_INSTANCE_END, null)) {
        for (String event : ItemHandler.EVENT_LISTENER_OR_MILESTONE_END_EVENTS) {
          caseActivity.addBuiltInListener(event, CASE_ACTIVITY_INSTANCE_END_LISTENER);
        }
      }
    }
  }

}
