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
package org.camunda.bpm.dmn.engine.impl.transform;

import static org.camunda.commons.utils.EnsureUtil.ensureNotNull;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionRequirementsGraph;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionLiteralExpressionImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionRequirementsGraphImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableInputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableOutputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableRuleImpl;
import org.camunda.bpm.dmn.engine.impl.DmnExpressionImpl;
import org.camunda.bpm.dmn.engine.impl.DmnLogger;
import org.camunda.bpm.dmn.engine.impl.DmnVariableImpl;
import org.camunda.bpm.dmn.engine.impl.spi.hitpolicy.DmnHitPolicyHandlerRegistry;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnElementTransformContext;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnElementTransformHandler;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnElementTransformHandlerRegistry;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnTransform;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnTransformListener;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnTransformer;
import org.camunda.bpm.dmn.engine.impl.spi.type.DmnDataTypeTransformerRegistry;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelException;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.bpm.model.dmn.instance.Expression;
import org.camunda.bpm.model.dmn.instance.InformationRequirement;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.InputExpression;
import org.camunda.bpm.model.dmn.instance.LiteralExpression;
import org.camunda.bpm.model.dmn.instance.Output;
import org.camunda.bpm.model.dmn.instance.OutputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.camunda.bpm.model.dmn.instance.Variable;

public class DefaultDmnTransform implements DmnTransform, DmnElementTransformContext {

  private static final DmnTransformLogger LOG = DmnLogger.TRANSFORM_LOGGER;

  protected DmnTransformer transformer;

  protected List<DmnTransformListener> transformListeners;
  protected DmnElementTransformHandlerRegistry handlerRegistry;

  // context
  protected DmnModelInstance modelInstance;
  protected Object parent;
  protected DmnDecisionImpl decision;
  protected DmnDecisionTableImpl decisionTable;
  protected DmnDataTypeTransformerRegistry dataTypeTransformerRegistry;
  protected DmnHitPolicyHandlerRegistry hitPolicyHandlerRegistry;

  public DefaultDmnTransform(DmnTransformer transformer) {
    this.transformer = transformer;
    transformListeners = transformer.getTransformListeners();
    handlerRegistry = transformer.getElementTransformHandlerRegistry();
    dataTypeTransformerRegistry = transformer.getDataTypeTransformerRegistry();
    hitPolicyHandlerRegistry = transformer.getHitPolicyHandlerRegistry();
  }

  public void setModelInstance(File file) {
    ensureNotNull("file", file);
    try {
      modelInstance = Dmn.readModelFromFile(file);
    }
    catch (DmnModelException e) {
      throw LOG.unableToTransformDecisionsFromFile(file, e);
    }
  }

  public DmnTransform modelInstance(File file) {
    setModelInstance(file);
    return this;
  }

  public void setModelInstance(InputStream inputStream) {
    ensureNotNull("inputStream", inputStream);
    try {
      modelInstance = Dmn.readModelFromStream(inputStream);
    }
    catch (DmnModelException e) {
      throw LOG.unableToTransformDecisionsFromInputStream(e);
    }
  }

  public DmnTransform modelInstance(InputStream inputStream) {
    setModelInstance(inputStream);
    return this;
  }

  public void setModelInstance(DmnModelInstance modelInstance) {
    ensureNotNull("dmnModelInstance", modelInstance);
    this.modelInstance = modelInstance;
  }

  public DmnTransform modelInstance(DmnModelInstance modelInstance) {
    setModelInstance(modelInstance);
    return this;
  }

  // transform ////////////////////////////////////////////////////////////////

  @SuppressWarnings("unchecked")
  public <T extends DmnDecisionRequirementsGraph> T transformDecisionRequirementsGraph() {
    try {
      Definitions definitions = modelInstance.getDefinitions();
      return (T) transformDefinitions(definitions);
    }
    catch (Exception e) {
      throw LOG.errorWhileTransformingDefinitions(e);
    }
  }

  protected DmnDecisionRequirementsGraph transformDefinitions(Definitions definitions) {
    DmnElementTransformHandler<Definitions, DmnDecisionRequirementsGraphImpl> handler = handlerRegistry.getHandler(Definitions.class);
    DmnDecisionRequirementsGraphImpl dmnDrg = handler.handleElement(this, definitions);

    // validate id of drd
    if (dmnDrg.getKey() == null) {
      throw LOG.drdIdIsMissing(dmnDrg);
    }

    Collection<Decision> decisions = definitions.getChildElementsByType(Decision.class);
    List<DmnDecision> dmnDecisions = transformDecisions(decisions);
    for (DmnDecision dmnDecision : dmnDecisions) {
      dmnDrg.addDecision(dmnDecision);
    }

    notifyTransformListeners(definitions, dmnDrg);
    return dmnDrg;
  }

  @SuppressWarnings("unchecked")
  public <T extends DmnDecision> List<T> transformDecisions() {
    try {
      Definitions definitions = modelInstance.getDefinitions();
      Collection<Decision> decisions = definitions.getChildElementsByType(Decision.class);
      return (List<T>) transformDecisions(decisions);
    }
    catch (Exception e) {
      throw LOG.errorWhileTransformingDecisions(e);
    }
  }

  protected List<DmnDecision> transformDecisions(Collection<Decision> decisions) {
    Map<String,DmnDecisionImpl> dmnDecisions = transformIndividualDecisions(decisions);
    buildDecisionRequirements(decisions, dmnDecisions);
    List<DmnDecision> dmnDecisionList = new ArrayList<DmnDecision>(dmnDecisions.values());

    for(Decision decision: decisions) {
      DmnDecision dmnDecision = dmnDecisions.get(decision.getId());
      notifyTransformListeners(decision, dmnDecision);
    }
    ensureNoLoopInDecisions(dmnDecisionList);

    return dmnDecisionList;
  }

  protected Map<String,DmnDecisionImpl> transformIndividualDecisions(Collection<Decision> decisions) {
    Map<String, DmnDecisionImpl> dmnDecisions = new HashMap<String, DmnDecisionImpl>();

    for (Decision decision : decisions) {
      DmnDecisionImpl dmnDecision = transformDecision(decision);
      if (dmnDecision != null) {
        dmnDecisions.put(dmnDecision.getKey(), dmnDecision);
      }
    }
    return dmnDecisions;
  }

  protected void buildDecisionRequirements(Collection<Decision> decisions, Map<String, DmnDecisionImpl> dmnDecisions) {
    for(Decision decision: decisions) {
      List<DmnDecision> requiredDmnDecisions = getRequiredDmnDecisions(decision, dmnDecisions);
      DmnDecisionImpl dmnDecision = dmnDecisions.get(decision.getId());

      if(requiredDmnDecisions.size() > 0) {
        dmnDecision.setRequiredDecision(requiredDmnDecisions);
      }
    }
  }

  protected void ensureNoLoopInDecisions(List<DmnDecision> dmnDecisionList) {
    List<String> visitedDecisions = new ArrayList<String>();

    for(DmnDecision decision: dmnDecisionList) {
      ensureNoLoopInDecision(decision, new ArrayList<String>(), visitedDecisions);
    }
  }

  protected void ensureNoLoopInDecision(DmnDecision decision, List<String> parentDecisionList, List<String> visitedDecisions) {

    if (visitedDecisions.contains(decision.getKey())) {
      return;
    }

    parentDecisionList.add(decision.getKey());

    for(DmnDecision requiredDecision : decision.getRequiredDecisions()){

      if (parentDecisionList.contains(requiredDecision.getKey())) {
        throw LOG.requiredDecisionLoopDetected(requiredDecision.getKey());
      }

      ensureNoLoopInDecision(requiredDecision, new ArrayList<String>(parentDecisionList), visitedDecisions);
    }
    visitedDecisions.add(decision.getKey());
  }

  protected List<DmnDecision> getRequiredDmnDecisions(Decision decision, Map<String, DmnDecisionImpl> dmnDecisions) {
    List<DmnDecision> requiredDecisionList = new ArrayList<DmnDecision>();
    for(InformationRequirement informationRequirement: decision.getInformationRequirements()) {

      Decision requiredDecision = informationRequirement.getRequiredDecision();
      if(requiredDecision != null) {
        DmnDecision requiredDmnDecision = dmnDecisions.get(requiredDecision.getId());
        requiredDecisionList.add(requiredDmnDecision);
      }
    }
    return requiredDecisionList;
  }

  protected DmnDecisionImpl transformDecision(Decision decision) {

    DmnElementTransformHandler<Decision, DmnDecisionImpl> handler = handlerRegistry.getHandler(Decision.class);
    DmnDecisionImpl dmnDecision = handler.handleElement(this, decision);
    this.decision = dmnDecision;
    // validate decision id
    if (dmnDecision.getKey() == null) {
      throw LOG.decisionIdIsMissing(dmnDecision);
    }

    Expression expression = decision.getExpression();
    if (expression == null) {
      LOG.decisionWithoutExpression(decision);
      return null;
    }

    if (expression instanceof DecisionTable) {
      DmnDecisionTableImpl dmnDecisionTable = transformDecisionTable((DecisionTable) expression);
      dmnDecision.setDecisionLogic(dmnDecisionTable);

    } else if (expression instanceof LiteralExpression) {
      DmnDecisionLiteralExpressionImpl dmnDecisionLiteralExpression = transformDecisionLiteralExpression(decision, (LiteralExpression) expression);
      dmnDecision.setDecisionLogic(dmnDecisionLiteralExpression);

    } else {
      LOG.decisionTypeNotSupported(expression, decision);
      return null;
    }

    return dmnDecision;
  }

  protected DmnDecisionTableImpl transformDecisionTable(DecisionTable decisionTable) {
    DmnElementTransformHandler<DecisionTable, DmnDecisionTableImpl> handler = handlerRegistry.getHandler(DecisionTable.class);
    DmnDecisionTableImpl dmnDecisionTable = handler.handleElement(this, decisionTable);

    for (Input input : decisionTable.getInputs()) {
      parent = dmnDecisionTable;
      this.decisionTable = dmnDecisionTable;
      DmnDecisionTableInputImpl dmnInput = transformDecisionTableInput(input);
      if (dmnInput != null) {
        dmnDecisionTable.getInputs().add(dmnInput);
        notifyTransformListeners(input, dmnInput);
      }
    }

    boolean needsName = decisionTable.getOutputs().size() > 1;
    Set<String> usedNames = new HashSet<String>();
    for (Output output : decisionTable.getOutputs()) {
      parent = dmnDecisionTable;
      this.decisionTable = dmnDecisionTable;
      DmnDecisionTableOutputImpl dmnOutput = transformDecisionTableOutput(output);
      if (dmnOutput != null) {
        // validate output name
        String outputName = dmnOutput.getOutputName();
        if (needsName && outputName == null) {
          throw LOG.compoundOutputsShouldHaveAnOutputName(dmnDecisionTable, dmnOutput);
        }
        if (usedNames.contains(outputName)) {
          throw LOG.compoundOutputWithDuplicateName(dmnDecisionTable, dmnOutput);
        }
        usedNames.add(outputName);

        dmnDecisionTable.getOutputs().add(dmnOutput);
        notifyTransformListeners(output, dmnOutput);
      }
    }

    for (Rule rule : decisionTable.getRules()) {
      parent = dmnDecisionTable;
      this.decisionTable = dmnDecisionTable;
      DmnDecisionTableRuleImpl dmnRule = transformDecisionTableRule(rule);
      if (dmnRule != null) {
        dmnDecisionTable.getRules().add(dmnRule);
        notifyTransformListeners(rule, dmnRule);
      }
    }

    return dmnDecisionTable;
  }

  protected DmnDecisionTableInputImpl transformDecisionTableInput(Input input) {
    DmnElementTransformHandler<Input, DmnDecisionTableInputImpl> handler = handlerRegistry.getHandler(Input.class);
    DmnDecisionTableInputImpl dmnInput = handler.handleElement(this, input);

    // validate input id
    if (dmnInput.getId() == null) {
      throw LOG.decisionTableInputIdIsMissing(decision, dmnInput);
    }

    InputExpression inputExpression = input.getInputExpression();
    if (inputExpression != null) {
      parent = dmnInput;
      DmnExpressionImpl dmnExpression = transformInputExpression(inputExpression);
      if (dmnExpression != null) {
        dmnInput.setExpression(dmnExpression);
      }
    }

    return dmnInput;
  }

  protected DmnDecisionTableOutputImpl transformDecisionTableOutput(Output output) {
    DmnElementTransformHandler<Output, DmnDecisionTableOutputImpl> handler = handlerRegistry.getHandler(Output.class);
    DmnDecisionTableOutputImpl dmnOutput = handler.handleElement(this, output);

    // validate output id
    if (dmnOutput.getId() == null) {
      throw LOG.decisionTableOutputIdIsMissing(decision, dmnOutput);
    }

    return dmnOutput;
  }

  protected DmnDecisionTableRuleImpl transformDecisionTableRule(Rule rule) {
    DmnElementTransformHandler<Rule, DmnDecisionTableRuleImpl> handler = handlerRegistry.getHandler(Rule.class);
    DmnDecisionTableRuleImpl dmnRule = handler.handleElement(this, rule);

    // validate rule id
    if (dmnRule.getId() == null) {
      throw LOG.decisionTableRuleIdIsMissing(decision, dmnRule);
    }

    List<DmnDecisionTableInputImpl> inputs = this.decisionTable.getInputs();
    List<InputEntry> inputEntries = new ArrayList<InputEntry>(rule.getInputEntries());
    if (inputs.size() != inputEntries.size()) {
      throw LOG.differentNumberOfInputsAndInputEntries(inputs.size(), inputEntries.size(), dmnRule);
    }

    for (InputEntry inputEntry : inputEntries) {
      parent = dmnRule;

      DmnExpressionImpl condition = transformInputEntry(inputEntry);
      dmnRule.getConditions().add(condition);
    }

    List<DmnDecisionTableOutputImpl> outputs = this.decisionTable.getOutputs();
    List<OutputEntry> outputEntries = new ArrayList<OutputEntry>(rule.getOutputEntries());
    if (outputs.size() != outputEntries.size()) {
      throw LOG.differentNumberOfOutputsAndOutputEntries(outputs.size(), outputEntries.size(), dmnRule);
    }

    for (OutputEntry outputEntry : outputEntries) {
      parent = dmnRule;
      DmnExpressionImpl conclusion = transformOutputEntry(outputEntry);
      dmnRule.getConclusions().add(conclusion);
    }

    return dmnRule;
  }

  protected DmnExpressionImpl transformInputExpression(InputExpression inputExpression) {
    DmnElementTransformHandler<InputExpression, DmnExpressionImpl> handler = handlerRegistry.getHandler(InputExpression.class);
    return handler.handleElement(this, inputExpression);
  }

  protected DmnExpressionImpl transformInputEntry(InputEntry inputEntry) {
    DmnElementTransformHandler<InputEntry, DmnExpressionImpl> handler = handlerRegistry.getHandler(InputEntry.class);
    return handler.handleElement(this, inputEntry);
  }

  protected DmnExpressionImpl transformOutputEntry(OutputEntry outputEntry) {
    DmnElementTransformHandler<OutputEntry, DmnExpressionImpl> handler = handlerRegistry.getHandler(OutputEntry.class);
    return handler.handleElement(this, outputEntry);
  }

  protected DmnDecisionLiteralExpressionImpl transformDecisionLiteralExpression(Decision decision, LiteralExpression literalExpression) {
    DmnDecisionLiteralExpressionImpl dmnDecisionLiteralExpression = new DmnDecisionLiteralExpressionImpl();

    Variable variable = decision.getVariable();
    if (variable == null) {
      throw LOG.decisionVariableIsMissing(decision.getId());
    }

    DmnVariableImpl dmnVariable = transformVariable(variable);
    dmnDecisionLiteralExpression.setVariable(dmnVariable);

    DmnExpressionImpl dmnLiteralExpression = transformLiteralExpression(literalExpression);
    dmnDecisionLiteralExpression.setExpression(dmnLiteralExpression);

    return dmnDecisionLiteralExpression;
  }

  protected DmnExpressionImpl transformLiteralExpression(LiteralExpression literalExpression) {
    DmnElementTransformHandler<LiteralExpression, DmnExpressionImpl> handler = handlerRegistry.getHandler(LiteralExpression.class);
    return handler.handleElement(this, literalExpression);
  }

  protected DmnVariableImpl transformVariable(Variable variable) {
    DmnElementTransformHandler<Variable, DmnVariableImpl> handler = handlerRegistry.getHandler(Variable.class);
    return handler.handleElement(this, variable);
  }

  // listeners ////////////////////////////////////////////////////////////////

  protected void notifyTransformListeners(Decision decision, DmnDecision dmnDecision) {
    for (DmnTransformListener transformListener : transformListeners) {
      transformListener.transformDecision(decision, dmnDecision);
    }
  }

  protected void notifyTransformListeners(Input input, DmnDecisionTableInputImpl dmnInput) {
    for (DmnTransformListener transformListener : transformListeners) {
      transformListener.transformDecisionTableInput(input, dmnInput);
    }
  }

  protected void notifyTransformListeners(Definitions definitions, DmnDecisionRequirementsGraphImpl dmnDecisionRequirementsGraph) {
    for (DmnTransformListener transformListener : transformListeners) {
      transformListener.transformDecisionRequirementsGraph(definitions, dmnDecisionRequirementsGraph);
    }
  }

  protected void notifyTransformListeners(Output output, DmnDecisionTableOutputImpl dmnOutput) {
    for (DmnTransformListener transformListener : transformListeners) {
      transformListener.transformDecisionTableOutput(output, dmnOutput);
    }
  }

  protected void notifyTransformListeners(Rule rule, DmnDecisionTableRuleImpl dmnRule) {
    for (DmnTransformListener transformListener : transformListeners) {
      transformListener.transformDecisionTableRule(rule, dmnRule);
    }
  }

  // context //////////////////////////////////////////////////////////////////

  public DmnModelInstance getModelInstance() {
    return modelInstance;
  }

  public Object getParent() {
    return parent;
  }

  public DmnDecision getDecision() {
    return decision;
  }

  public DmnDataTypeTransformerRegistry getDataTypeTransformerRegistry() {
    return dataTypeTransformerRegistry;
  }

  public DmnHitPolicyHandlerRegistry getHitPolicyHandlerRegistry() {
    return hitPolicyHandlerRegistry;
  }

}
