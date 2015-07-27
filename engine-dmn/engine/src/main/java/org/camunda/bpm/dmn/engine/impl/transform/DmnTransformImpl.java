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

package org.camunda.bpm.dmn.engine.impl.transform;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.impl.DmnRuleImpl;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelException;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.AllowedValue;
import org.camunda.bpm.model.dmn.instance.Clause;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.bpm.model.dmn.instance.DmnModelElementInstance;
import org.camunda.bpm.model.dmn.instance.Expression;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.InputExpression;
import org.camunda.bpm.model.dmn.instance.ItemDefinition;
import org.camunda.bpm.model.dmn.instance.OutputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.bpm.model.dmn.instance.TypeDefinition;
import org.camunda.bpm.dmn.engine.DmnClause;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionModel;
import org.camunda.bpm.dmn.engine.DmnItemDefinition;
import org.camunda.bpm.dmn.engine.DmnRule;
import org.camunda.bpm.dmn.engine.handler.DmnElementHandler;
import org.camunda.bpm.dmn.engine.handler.DmnElementHandlerContext;
import org.camunda.bpm.dmn.engine.handler.DmnElementHandlerRegistry;
import org.camunda.bpm.dmn.engine.impl.DmnClauseEntryImpl;
import org.camunda.bpm.dmn.engine.impl.DmnClauseImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionModelImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.dmn.engine.impl.DmnExpressionImpl;
import org.camunda.bpm.dmn.engine.impl.DmnItemDefinitionImpl;
import org.camunda.bpm.dmn.engine.impl.DmnLogger;
import org.camunda.bpm.dmn.engine.impl.DmnTransformLogger;
import org.camunda.bpm.dmn.engine.impl.DmnTypeDefinitionImpl;
import org.camunda.bpm.dmn.engine.transform.DmnTransform;
import org.camunda.bpm.dmn.engine.transform.DmnTransformListener;
import org.camunda.bpm.dmn.engine.transform.DmnTransformer;

public class DmnTransformImpl implements DmnTransform, DmnElementHandlerContext {

  private static final DmnTransformLogger LOG = DmnLogger.TRANSFORM_LOGGER;

  protected DmnTransformer transformer;

  protected DmnElementHandlerRegistry elementHandlerRegistry;
  protected List<DmnTransformListener> transformListeners;

  // context
  protected DmnModelInstance modelInstance;
  protected DmnDecisionModelImpl decisionModel;
  protected DmnDecisionTableImpl decision;
  protected Object parent;

  protected Map<String, DmnClauseEntryImpl> inputEntries = new HashMap<String, DmnClauseEntryImpl>();
  protected Map<String, DmnClauseEntryImpl> outputEntries = new HashMap<String, DmnClauseEntryImpl>();

  public DmnTransformImpl(DmnTransformer transformer) {
    this.transformer = transformer;
    this.elementHandlerRegistry = transformer.getElementHandlerRegistry();
    this.transformListeners = transformer.getTransformListeners();
  }

  public DmnTransform setModelInstance(File file) {
    try {
      modelInstance = Dmn.readModelFromFile(file);
    }
    catch (DmnModelException e) {
      throw LOG.unableToTransformModelFromFile(file, e);
    }
    return this;
  }

  public DmnTransform setModelInstance(InputStream inputStream) {
    try {
      modelInstance = Dmn.readModelFromStream(inputStream);
    }
    catch (DmnModelException e) {
      throw LOG.unableToTransformModelFromInputStream(e);
    }
    return this;
  }

  public DmnTransform setModelInstance(DmnModelInstance modelInstance) {
    this.modelInstance = modelInstance;
    return this;
  }

  public DmnDecisionModel transform() {
    try {
      return transformDefinitions();
    }
    catch (Exception e) {
      throw LOG.errorWhileTransforming(e);
    }
  }

  protected DmnDecisionModel transformDefinitions() {
    DmnElementHandler<Definitions, DmnDecisionModelImpl> definitionsHandler = getElementHandler(Definitions.class);

    Definitions definitions = modelInstance.getDefinitions();
    decisionModel = definitionsHandler.handleElement(this, definitions);

    transformItemDefinitions();
    transformDecisions();

    notifyTransformListeners(definitions, decisionModel);

    return decisionModel;
  }

  protected void transformItemDefinitions() {
    Collection<ItemDefinition> itemDefinitions = modelInstance.getDefinitions().getItemDefinitions();
    for (ItemDefinition itemDefinition : itemDefinitions) {
      transformItemDefinition(itemDefinition);
    }
  }

  protected void transformItemDefinition(ItemDefinition itemDefinition) {
    DmnElementHandler<ItemDefinition, DmnItemDefinitionImpl> itemDefinitionHandler = getElementHandler(ItemDefinition.class);
    DmnElementHandler<TypeDefinition, DmnTypeDefinitionImpl> typeDefinitionHandler = getElementHandler(TypeDefinition.class);
    DmnElementHandler<AllowedValue, DmnExpressionImpl> allowedValueHandler = getElementHandler(AllowedValue.class);

    DmnItemDefinitionImpl dmnItemDefinition = itemDefinitionHandler.handleElement(this, itemDefinition);

    TypeDefinition typeDefinition = itemDefinition.getTypeDefinition();
    if (typeDefinition != null) {
      parent = dmnItemDefinition;
      DmnTypeDefinitionImpl dmnTypeDefinition = typeDefinitionHandler.handleElement(this, typeDefinition);
      dmnItemDefinition.setTypeDefinition(dmnTypeDefinition);
    }

    for (AllowedValue allowedValue : itemDefinition.getAllowedValues()) {
      parent = itemDefinition;
      DmnExpressionImpl dmnExpression = allowedValueHandler.handleElement(this, allowedValue);
      dmnItemDefinition.addAllowedValue(dmnExpression);
    }

    notifyTransformListeners(itemDefinition, dmnItemDefinition);

    decisionModel.addItemDefinition(dmnItemDefinition);
  }

  protected void transformDecisions() {
    Collection<Decision> decisions = modelInstance.getDefinitions().getChildElementsByType(Decision.class);
    for (Decision decision : decisions) {
      transformDecision(decision);
    }
  }

  protected void transformDecision(Decision decision) {
    DmnDecision dmnDecision = null;

    Expression expression = decision.getExpression();
    if (expression instanceof DecisionTable) {
      dmnDecision = transformDecisionTable((DecisionTable) expression);
    }
    else {
      LOG.decisionTypeNotSupported(decision);
    }

    if (dmnDecision != null) {
      notifyTransformListeners(decision, dmnDecision);
      decisionModel.addDecision(dmnDecision);
    }
  }

  protected DmnDecision transformDecisionTable(DecisionTable decisionTable) {
    DmnElementHandler<DecisionTable, DmnDecisionTableImpl> decisionTableHandler =  getElementHandler(DecisionTable.class);
    DmnDecisionTableImpl dmnDecision = decisionTableHandler.handleElement(this, decisionTable);
    decision = dmnDecision;

    for (Clause clause : decisionTable.getClauses()) {
      parent = dmnDecision;
      transformClause(clause);
    }

    for (Rule rule : decisionTable.getRules()) {
      parent = dmnDecision;
      transformRule(rule);
    }

    return dmnDecision;
  }

  protected void transformClause(Clause clause) {
    DmnClause dmnClause = null;

    if (isInputClause(clause)) {
      dmnClause = transformInputClause(clause);
    }
    else if (isOutputClause(clause)) {
      dmnClause = transformOutputClause(clause);
    }
    else {
      LOG.ignoringClause(clause);
    }

    if (dmnClause != null) {
      notifyTransformListeners(clause, dmnClause);
      decision.addClause(dmnClause);
    }
  }

  protected DmnClause transformInputClause(Clause clause) {
    DmnElementHandler<Clause, DmnClauseImpl> clauseHandler = getElementHandler(Clause.class);
    DmnElementHandler<InputExpression, DmnExpressionImpl> inputExpressionHandler = getElementHandler(InputExpression.class);
    DmnElementHandler<InputEntry, DmnClauseEntryImpl> inputEntryHandler = getElementHandler(InputEntry.class);

    DmnClauseImpl dmnClause = clauseHandler.handleElement(this, clause);

    // set default output name if not specified
    if (dmnClause.getOutputName() == null) {
      dmnClause.setOutputName(DmnClauseImpl.DEFAULT_INPUT_VARIABLE_NAME);
    }

    InputExpression inputExpression = clause.getInputExpression();
    if (inputExpression != null) {
      parent = dmnClause;
      DmnExpressionImpl dmnInputExpression = inputExpressionHandler.handleElement(this, inputExpression);
      dmnClause.setInputExpression(dmnInputExpression);
    }

    for (InputEntry inputEntry : clause.getInputEntries()) {
      parent = dmnClause;
      DmnClauseEntryImpl dmnInputEntry = inputEntryHandler.handleElement(this, inputEntry);
      inputEntries.put(dmnInputEntry.getKey(), dmnInputEntry);
      dmnInputEntry.setClause(dmnClause);
      dmnClause.addInputEntry(dmnInputEntry);
    }

    return dmnClause;
  }

  protected DmnClause transformOutputClause(Clause clause) {
    DmnElementHandler<Clause, DmnClauseImpl> clauseHandler = getElementHandler(Clause.class);
    DmnElementHandler<OutputEntry, DmnClauseEntryImpl> outputEntryHandler = getElementHandler(OutputEntry.class);

    DmnClauseImpl dmnClause = clauseHandler.handleElement(this, clause);

    for (OutputEntry outputEntry : clause.getOutputEntries()) {
      parent = dmnClause;
      DmnClauseEntryImpl dmnOutputEntry = outputEntryHandler.handleElement(this, outputEntry);
      outputEntries.put(dmnOutputEntry.getKey(), dmnOutputEntry);
      dmnOutputEntry.setClause(dmnClause);
      dmnClause.addOutputEntry(dmnOutputEntry);
    }

    return dmnClause;
  }

  protected void transformRule(Rule rule) {
    DmnElementHandler<Rule, DmnRuleImpl> ruleHandler = getElementHandler(Rule.class);

    DmnRuleImpl dmnRule = ruleHandler.handleElement(this, rule);

    for (Expression condition : rule.getConditions()) {
      parent = dmnRule;
      DmnClauseEntryImpl dmnCondition = transformCondition(condition);
      dmnRule.addCondition(dmnCondition);
    }

    for (Expression conclusion : rule.getConclusions()) {
      parent = dmnRule;
      DmnClauseEntryImpl dmnConclusion = transformConclusion(conclusion);
      dmnRule.addConclusion(dmnConclusion);
    }

    notifyTransformListeners(rule, dmnRule);

    decision.addRule(dmnRule);
  }

  protected DmnClauseEntryImpl transformCondition(Expression condition) {
    DmnClauseEntryImpl inputEntry = inputEntries.get(condition.getId());
    if (inputEntry != null) {
      return inputEntry;
    }
    else {
      throw LOG.unableToFindInputEntry(condition.getId());
    }
  }

  protected DmnClauseEntryImpl transformConclusion(Expression conclusion) {
    DmnClauseEntryImpl outputEntry = outputEntries.get(conclusion.getId());
    if (outputEntry != null) {
      return outputEntry;
    }
    else {
      throw LOG.unableToFindOutputEntry(conclusion.getId());
    }
  }

  // Helper /////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected boolean isInputClause(Clause clause) {
    return clause.getInputExpression() != null || !clause.getInputEntries().isEmpty();
  }

  protected boolean isOutputClause(Clause clause) {
    return clause.getOutputDefinition() != null || !clause.getOutputEntries().isEmpty();
  }

  @SuppressWarnings("unchecked")
  protected <Source extends DmnModelElementInstance, Target>  DmnElementHandler<Source, Target> getElementHandler(Class<Source> elementClass) {
    DmnElementHandler<? extends DmnModelElementInstance, Object> elementHandler = elementHandlerRegistry.getElementHandler(elementClass);
    if (elementHandler == null) {
      throw LOG.noElementHandlerForClassRegistered(elementClass);
    }
    return (DmnElementHandler<Source, Target>) elementHandler;
  }

  // Notify Transform Listeners ////////////////////////////////////////////////////////////////////////////////////

  protected void notifyTransformListeners(Definitions definitions, DmnDecisionModel decisionModel) {
    for (DmnTransformListener transformListener : transformListeners) {
      transformListener.transformDefinitions(definitions, decisionModel);
    }
  }

  protected void notifyTransformListeners(ItemDefinition itemDefinition, DmnItemDefinition dmnItemDefinition) {
    for (DmnTransformListener transformListener : transformListeners) {
      transformListener.transformItemDefinition(itemDefinition, dmnItemDefinition);
    }
  }

  protected void notifyTransformListeners(Decision decision, DmnDecision dmnDecision) {
    for (DmnTransformListener transformListener : transformListeners) {
      transformListener.transformDecision(decision, dmnDecision);
    }
  }

  protected void notifyTransformListeners(Clause clause, DmnClause dmnClause) {
    for (DmnTransformListener transformListener : transformListeners) {
      transformListener.transformClause(clause, dmnClause);
    }
  }

  protected void notifyTransformListeners(Rule rule, DmnRule dmnRule) {
    for (DmnTransformListener transformListener : transformListeners) {
      transformListener.transformRule(rule, dmnRule);
    }
  }

  public DmnModelInstance getModelInstance() {
    return modelInstance;
  }

  public DmnDecisionModel getDecisionModel() {
    return decisionModel;
  }

  public Object getParent() {
    return parent;
  }

  public DmnDecision getDecision() {
    return decision;
  }

}
