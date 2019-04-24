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
package org.camunda.bpm.engine.impl.cmmn.handler;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.model.cmmn.instance.Case;
import org.camunda.bpm.model.cmmn.instance.CasePlanModel;
import org.camunda.bpm.model.cmmn.instance.CaseTask;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.cmmn.instance.DecisionTask;
import org.camunda.bpm.model.cmmn.instance.HumanTask;
import org.camunda.bpm.model.cmmn.instance.Milestone;
import org.camunda.bpm.model.cmmn.instance.PlanItemDefinition;
import org.camunda.bpm.model.cmmn.instance.ProcessTask;
import org.camunda.bpm.model.cmmn.instance.Stage;
import org.camunda.bpm.model.cmmn.instance.Task;

/**
 * @author Roman Smirnov
 *
 */
public class DefaultCmmnElementHandlerRegistry {

  protected Map<Class<? extends CmmnElement>, CmmnElementHandler<? extends CmmnElement, ? extends CmmnActivity>> definitionElementHandlers;
  protected Map<Class<? extends PlanItemDefinition>, ItemHandler> planItemElementHandlers;
  protected Map<Class<? extends PlanItemDefinition>, ItemHandler> discretionaryElementHandlers;

  protected CaseHandler caseHandler = new CaseHandler();

  protected StageItemHandler stagePlanItemHandler = new StageItemHandler();
  protected CasePlanModelHandler casePlanModelHandler = new CasePlanModelHandler();
  protected TaskItemHandler taskPlanItemHandler = new TaskItemHandler();
  protected HumanTaskItemHandler humanTaskPlanItemHandler = new HumanTaskItemHandler();
  protected ProcessTaskItemHandler processTaskPlanItemHandler = new ProcessTaskItemHandler();
  protected CaseTaskItemHandler caseTaskPlanItemHandler = new CaseTaskItemHandler();
  protected DecisionTaskItemHandler decisionTaskPlanItemHandler = new DecisionTaskItemHandler();
  protected MilestoneItemHandler milestonePlanItemHandler = new MilestoneItemHandler();
  protected EventListenerItemHandler eventListenerPlanItemHandler = new EventListenerItemHandler();

  protected StageItemHandler stageDiscretionaryItemHandler = new StageItemHandler();
  protected HumanTaskItemHandler humanTaskDiscretionaryItemHandler = new HumanTaskItemHandler();

  protected SentryHandler sentryHandler = new SentryHandler();

  public DefaultCmmnElementHandlerRegistry() {

    // init definition element handler
    definitionElementHandlers = new HashMap<Class<? extends CmmnElement>, CmmnElementHandler<? extends CmmnElement, ? extends CmmnActivity>>();

    definitionElementHandlers.put(Case.class, caseHandler);

    // init plan item element handler
    planItemElementHandlers = new HashMap<Class<? extends PlanItemDefinition>, ItemHandler>();

    planItemElementHandlers.put(Stage.class, stagePlanItemHandler);
    planItemElementHandlers.put(CasePlanModel.class, casePlanModelHandler);
    planItemElementHandlers.put(Task.class, taskPlanItemHandler);
    planItemElementHandlers.put(HumanTask.class, humanTaskPlanItemHandler);
    planItemElementHandlers.put(ProcessTask.class, processTaskPlanItemHandler);
    planItemElementHandlers.put(DecisionTask.class, decisionTaskPlanItemHandler);
    planItemElementHandlers.put(CaseTask.class, caseTaskPlanItemHandler);
    planItemElementHandlers.put(Milestone.class, milestonePlanItemHandler);

    // Note: EventListener is currently not supported!
    // planItemElementHandlers.put(EventListener.class, eventListenerPlanItemHandler);

    // init discretionary element handler
    discretionaryElementHandlers = new HashMap<Class<? extends PlanItemDefinition>, ItemHandler>();

    discretionaryElementHandlers.put(Stage.class, stageDiscretionaryItemHandler);
    discretionaryElementHandlers.put(HumanTask.class, humanTaskDiscretionaryItemHandler);
  }

  public Map<Class<? extends CmmnElement>, CmmnElementHandler<? extends CmmnElement, ? extends CmmnActivity>> getDefinitionElementHandlers() {
    return definitionElementHandlers;
  }

  public void setDefinitionElementHandlers(Map<Class<? extends CmmnElement>, CmmnElementHandler<? extends CmmnElement, ? extends CmmnActivity>> definitionElementHandlers) {
    this.definitionElementHandlers = definitionElementHandlers;
  }

  public Map<Class<? extends PlanItemDefinition>, ItemHandler> getPlanItemElementHandlers() {
    return planItemElementHandlers;
  }

  public void setPlanItemElementHandlers(Map<Class<? extends PlanItemDefinition>, ItemHandler> planItemElementHandlers) {
    this.planItemElementHandlers = planItemElementHandlers;
  }

  public Map<Class<? extends PlanItemDefinition>, ItemHandler> getDiscretionaryElementHandlers() {
    return discretionaryElementHandlers;
  }

  public void setDiscretionaryElementHandlers(Map<Class<? extends PlanItemDefinition>, ItemHandler> discretionaryElementHandlers) {
    this.discretionaryElementHandlers = discretionaryElementHandlers;
  }

  public SentryHandler getSentryHandler() {
    return sentryHandler;
  }

  public void setSentryHandler(SentryHandler sentryHandler) {
    this.sentryHandler = sentryHandler;
  }

}
