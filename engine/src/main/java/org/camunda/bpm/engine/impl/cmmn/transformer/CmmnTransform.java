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

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.handler.CmmnElementHandler;
import org.camunda.bpm.engine.impl.cmmn.handler.CmmnHandlerContext;
import org.camunda.bpm.engine.impl.cmmn.handler.DefaultCmmnElementHandlerRegistry;
import org.camunda.bpm.engine.impl.cmmn.handler.DiscretionaryItemHandler;
import org.camunda.bpm.engine.impl.cmmn.handler.PlanItemHandler;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.core.transformer.Transform;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ResourceEntity;
import org.camunda.bpm.model.cmmn.Cmmn;
import org.camunda.bpm.model.cmmn.CmmnModelException;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.cmmn.impl.instance.CasePlanModel;
import org.camunda.bpm.model.cmmn.instance.Case;
import org.camunda.bpm.model.cmmn.instance.CaseTask;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.cmmn.instance.Definitions;
import org.camunda.bpm.model.cmmn.instance.HumanTask;
import org.camunda.bpm.model.cmmn.instance.PlanFragment;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.PlanItemDefinition;
import org.camunda.bpm.model.cmmn.instance.PlanningTable;
import org.camunda.bpm.model.cmmn.instance.ProcessTask;
import org.camunda.bpm.model.cmmn.instance.Sentry;
import org.camunda.bpm.model.cmmn.instance.Stage;
import org.camunda.bpm.model.cmmn.instance.Task;

/**
 * @author Roman Smirnov
 *
 */
public class CmmnTransform implements Transform<CaseDefinitionEntity> {

  protected CmmnTransformer transformer;

  protected ExpressionManager expressionManager;
  protected DefaultCmmnElementHandlerRegistry handlerRegistry;
  protected List<CmmnTransformListener> parseListeners;

  protected ResourceEntity resource;
  protected DeploymentEntity deployment;

  protected CmmnModelInstance model;
  protected CmmnHandlerContext context = new CmmnHandlerContext();
  protected List<CaseDefinitionEntity> caseDefinitions = new ArrayList<CaseDefinitionEntity>();

  public CmmnTransform(CmmnTransformer transformer) {
    this.transformer = transformer;
    this.expressionManager = transformer.getExpressionManager();
    this.handlerRegistry = transformer.getCmmnElementHandlerRegistry();
    this.parseListeners = transformer.getTransformListener();
  }

  public CmmnTransform deployment(DeploymentEntity deployment) {
    this.deployment = deployment;
    return this;
  }

  public CmmnTransform resource(ResourceEntity resource) {
    this.resource = resource;
    return this;
  }

  public List<CaseDefinitionEntity> transform() {
    // get name of resource
    String resourceName = resource.getName();

    // create an input stream
    byte[] bytes = resource.getBytes();
    ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

    try {
      // read input stream
      model = Cmmn.readModelFromStream(inputStream);

    } catch (CmmnModelException e) {
      throw new ProcessEngineException("Couldn't transform '" + resourceName + "': " + e.getMessage(), e);
    }

    // TODO: use model API to validate (ie.
    // semantic and execution validation) model

    context.setModel(model);
    context.setDeployment(deployment);
    context.setExpressionManager(expressionManager);

    try {

       parseRootElement();

    } catch (Exception e) {
      // ALL unexpected exceptions should bubble up since they are not handled
      // accordingly by underlying parse-methods and the process can't be deployed
      throw new ProcessEngineException("Error while parsing process: " + e.getMessage(), e);
    }

    return caseDefinitions;
  }

  protected void parseRootElement() {

    parseImports();
    parseCaseDefinitions();

  }

  protected void parseImports() {
    // not implemented yet
  }

  protected void parseCaseDefinitions() {
    Definitions definitions = model.getDefinitions();

    Collection<Case> cases = definitions.getCases();

    for (Case currentCase : cases) {
      CmmnCaseDefinition caseDefinition = parseCase(currentCase);
      caseDefinitions.add((CaseDefinitionEntity) caseDefinition);
    }
  }

  protected CaseDefinitionEntity parseCase(Case element) {
    // get CaseTransformer
    CmmnElementHandler<Case> caseTransformer = getDefinitionHandler(Case.class);
    CmmnActivity definition = caseTransformer.handleElement(element, context);

    context.setCaseDefinition((CmmnCaseDefinition) definition);
    context.setParent(definition);

    CasePlanModel casePlanModel = element.getCasePlanModel();
    parseCasePlanModel(casePlanModel);

    return (CaseDefinitionEntity) definition;
  }

  protected void parseCasePlanModel(CasePlanModel casePlanModel) {
    CmmnElementHandler<CasePlanModel> transformer = getDefinitionHandler(CasePlanModel.class);
    CmmnActivity newActivity = transformer.handleElement(casePlanModel, context);
    context.setParent(newActivity);

    parseStage(casePlanModel, newActivity);
  }

  protected void parseStage(Stage stage, CmmnActivity parent) {

    context.setParent(parent);

    parsePlanItems(stage, parent);
    parsePlanningTable(stage.getPlanningTable(), parent);

  }

  protected void parsePlanningTable(PlanningTable planningTable, CmmnActivity parent) {
    // not yet implemented.

    // TODO: think about how to organize the planning tables! A tableItem or planningTable
    // can have "applicabilityRules": If the rule evaluates to "true" the the tableItem or
    // planningTable is applicable for planning otherwise it is not.
  }

  protected void parsePlanItems(PlanFragment planFragment, CmmnActivity parent) {
    Collection<PlanItem> planItems = planFragment.getPlanItems();

    for (PlanItem planItem : planItems) {
      context.setParent(parent);
      parsePlanItem(planItem, parent);
    }

    for (PlanItem planItem : planItems) {
      parseSentry(planItem.getEntryCriterias(), true);
      parseSentry(planItem.getExitCriterias(), false);
    }

  }

  protected void parsePlanItem(PlanItem planItem, CmmnActivity parent) {
    PlanItemDefinition definition = planItem.getDefinition();

    CmmnElementHandler<PlanItem> planItemTransformer = null;

    if (definition instanceof HumanTask) {
      planItemTransformer = getPlanItemHandler(HumanTask.class);
    } else if (definition instanceof ProcessTask) {
      planItemTransformer = getPlanItemHandler(ProcessTask.class);
    } else if (definition instanceof CaseTask) {
      planItemTransformer = getPlanItemHandler(CaseTask.class);
    } else if (definition instanceof Task) {
      planItemTransformer = getPlanItemHandler(Task.class);
    } else if (definition instanceof Stage) {
      planItemTransformer = getPlanItemHandler(Stage.class);
    }

    CmmnActivity newActivity = planItemTransformer.handleElement(planItem, context);

    if (definition instanceof Stage) {
      Stage stage = (Stage) definition;
      parseStage(stage, newActivity);

    } else if (definition instanceof HumanTask) {
      HumanTask humanTask = (HumanTask) definition;

      // According to the specification: A HumanTask can only contain
      // one planningTable, the XSD allows multiple planningTables!
      Collection<PlanningTable> planningTables = humanTask.getPlanningTables();
      for (PlanningTable planningTable : planningTables) {
        parsePlanningTable(planningTable, parent);
      }

    }
  }

  protected void parseSentry(Collection<Sentry> sentry, boolean entryCriterias) {
    // not yet implemented.
  }

  // getter/setter ////////////////////////////////////////////////////////////////////

  public DeploymentEntity getDeployment() {
    return deployment;
  }

  public void setDeployment(DeploymentEntity deployment) {
    this.deployment = deployment;
  }

  public ResourceEntity getResource() {
    return resource;
  }

  public void setResource(ResourceEntity resource) {
    this.resource = resource;
  }

  public DefaultCmmnElementHandlerRegistry getHandlerRegistry() {
    return handlerRegistry;
  }

  public void setHandlerRegistry(DefaultCmmnElementHandlerRegistry handlerRegistry) {
    this.handlerRegistry = handlerRegistry;
  }

  @SuppressWarnings("unchecked")
  protected <V extends CmmnElement> CmmnElementHandler<V> getDefinitionHandler(Class<V> cls) {
    return (CmmnElementHandler<V>) getHandlerRegistry().getDefinitionElementHandlers().get(cls);
  }

  protected PlanItemHandler getPlanItemHandler(Class<? extends PlanItemDefinition> cls) {
    return getHandlerRegistry().getPlanItemElementHandlers().get(cls);
  }

  protected DiscretionaryItemHandler getDiscretionaryItemHandler(Class<? extends PlanItemDefinition> cls) {
    return getHandlerRegistry().getDiscretionaryElementHandlers().get(cls);
  }

  public ExpressionManager getExpressionManager() {
    return expressionManager;
  }

  public void setExpressionManager(ExpressionManager expressionManager) {
    this.expressionManager = expressionManager;
  }

}
