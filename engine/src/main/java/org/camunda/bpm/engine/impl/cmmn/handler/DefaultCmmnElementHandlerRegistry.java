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
package org.camunda.bpm.engine.impl.cmmn.handler;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.model.cmmn.impl.instance.CasePlanModel;
import org.camunda.bpm.model.cmmn.instance.Case;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.cmmn.instance.HumanTask;
import org.camunda.bpm.model.cmmn.instance.PlanItemDefinition;
import org.camunda.bpm.model.cmmn.instance.ProcessTask;
import org.camunda.bpm.model.cmmn.instance.Stage;
import org.camunda.bpm.model.cmmn.instance.Task;

/**
 * @author Roman Smirnov
 *
 */
public class DefaultCmmnElementHandlerRegistry {

  protected Map<Class<? extends CmmnElement>, CmmnElementHandler<? extends CmmnElement>> definitionElementHandlers;
  protected Map<Class<? extends PlanItemDefinition>, PlanItemHandler> planItemElementHandlers;
  protected Map<Class<? extends PlanItemDefinition>, DiscretionaryItemHandler> discretionaryElementHandlers;

  protected CaseHandler caseHandler = new CaseHandler();
  protected CasePlanModelHandler casePlanModelHandler = new CasePlanModelHandler();

  protected StagePlanItemHandler stagePlanItemHandler = new StagePlanItemHandler();
  protected TaskPlanItemHandler taskPlanItemHandler = new TaskPlanItemHandler();
  protected HumanTaskPlanItemHandler humanTaskPlanItemHandler = new HumanTaskPlanItemHandler();
  protected ProcessTaskPlanItemHandler processTaskPlanItemHandler = new ProcessTaskPlanItemHandler();

  protected StageDiscretionaryItemHandler stageDiscretionaryItemHandler = new StageDiscretionaryItemHandler();
  protected HumanTaskDiscretionaryItemHandler humanTaskDiscretionaryItemHandler = new HumanTaskDiscretionaryItemHandler();

  public DefaultCmmnElementHandlerRegistry() {

    // init definition element handler
    definitionElementHandlers = new HashMap<Class<? extends CmmnElement>, CmmnElementHandler<? extends CmmnElement>>();

    definitionElementHandlers.put(Case.class, caseHandler);
    definitionElementHandlers.put(CasePlanModel.class, casePlanModelHandler);

    // init plan item element handler
    planItemElementHandlers = new HashMap<Class<? extends PlanItemDefinition>, PlanItemHandler>();

    planItemElementHandlers.put(Stage.class, stagePlanItemHandler);
    planItemElementHandlers.put(Task.class, taskPlanItemHandler);
    planItemElementHandlers.put(HumanTask.class, humanTaskPlanItemHandler);
    planItemElementHandlers.put(ProcessTask.class, processTaskPlanItemHandler);

    // init discretionary element handler
    discretionaryElementHandlers = new HashMap<Class<? extends PlanItemDefinition>, DiscretionaryItemHandler>();

    discretionaryElementHandlers.put(Stage.class, stageDiscretionaryItemHandler);
    discretionaryElementHandlers.put(HumanTask.class, humanTaskDiscretionaryItemHandler);
  }

  public Map<Class<? extends CmmnElement>, CmmnElementHandler<? extends CmmnElement>> getDefinitionElementHandlers() {
    return definitionElementHandlers;
  }

  public void setDefinitionElementHandlers(Map<Class<? extends CmmnElement>, CmmnElementHandler<? extends CmmnElement>> definitionElementHandlers) {
    this.definitionElementHandlers = definitionElementHandlers;
  }

  public Map<Class<? extends PlanItemDefinition>, PlanItemHandler> getPlanItemElementHandlers() {
    return planItemElementHandlers;
  }

  public void setPlanItemElementHandlers(Map<Class<? extends PlanItemDefinition>, PlanItemHandler> planItemElementHandlers) {
    this.planItemElementHandlers = planItemElementHandlers;
  }

  public Map<Class<? extends PlanItemDefinition>, DiscretionaryItemHandler> getDiscretionaryElementHandlers() {
    return discretionaryElementHandlers;
  }

  public void setDiscretionaryElementHandlers(Map<Class<? extends PlanItemDefinition>, DiscretionaryItemHandler> discretionaryElementHandlers) {
    this.discretionaryElementHandlers = discretionaryElementHandlers;
  }

}
