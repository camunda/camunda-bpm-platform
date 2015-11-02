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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionModel;
import org.camunda.bpm.dmn.engine.DmnDecisionTable;
import org.camunda.bpm.dmn.engine.DmnExpression;
import org.camunda.bpm.dmn.engine.DmnInput;
import org.camunda.bpm.dmn.engine.DmnItemDefinition;
import org.camunda.bpm.dmn.engine.DmnOutput;
import org.camunda.bpm.dmn.engine.DmnRule;
import org.camunda.bpm.dmn.engine.handler.DmnElementHandler;
import org.camunda.bpm.dmn.engine.handler.DmnElementHandlerContext;
import org.camunda.bpm.dmn.engine.handler.DmnElementHandlerRegistry;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionModelImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.dmn.engine.impl.DmnExpressionImpl;
import org.camunda.bpm.dmn.engine.impl.DmnInputEntryImpl;
import org.camunda.bpm.dmn.engine.impl.DmnInputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnLogger;
import org.camunda.bpm.dmn.engine.impl.DmnOutputEntryImpl;
import org.camunda.bpm.dmn.engine.impl.DmnOutputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnRuleImpl;
import org.camunda.bpm.dmn.engine.impl.DmnTransformLogger;
import org.camunda.bpm.dmn.engine.impl.DmnTypeDefinitionImpl;
import org.camunda.bpm.dmn.engine.transform.DmnTransform;
import org.camunda.bpm.dmn.engine.transform.DmnTransformListener;
import org.camunda.bpm.dmn.engine.transform.DmnTransformer;
import org.camunda.bpm.dmn.engine.type.DataTypeTransformer;
import org.camunda.bpm.dmn.engine.type.DataTypeTransformerFactory;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelException;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.bpm.model.dmn.instance.DmnModelElementInstance;
import org.camunda.bpm.model.dmn.instance.Expression;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.InputExpression;
import org.camunda.bpm.model.dmn.instance.ItemDefinition;
import org.camunda.bpm.model.dmn.instance.Output;
import org.camunda.bpm.model.dmn.instance.OutputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;

public class DmnTransformImpl implements DmnTransform, DmnElementHandlerContext {

  private static final DmnTransformLogger LOG = DmnLogger.TRANSFORM_LOGGER;

  protected DmnTransformer transformer;

  protected DmnElementHandlerRegistry elementHandlerRegistry;
  protected List<DmnTransformListener> transformListeners;

  protected DataTypeTransformerFactory dataTypeTransformerFactory;

  // context
  protected DmnModelInstance modelInstance;
  protected DmnDecisionModelImpl decisionModel;
  protected DmnDecisionTableImpl decision;
  protected Object parent;

  public DmnTransformImpl(DmnTransformer transformer) {
    this.transformer = transformer;
    elementHandlerRegistry = transformer.getElementHandlerRegistry();
    transformListeners = transformer.getTransformListeners();
    dataTypeTransformerFactory = transformer.getDataTypeTransformerFactory();
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

    transformDecisions();

    notifyTransformListeners(definitions, decisionModel);

    return decisionModel;
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
    DmnDecisionTableImpl dmnDecisionTable = decisionTableHandler.handleElement(this, decisionTable);
    decision = dmnDecisionTable;

    for (Input input : decisionTable.getInputs()) {
      parent = dmnDecisionTable;
      DmnInputImpl dmnInput = transformInput(input);
      dmnDecisionTable.addInput(dmnInput);
      notifyTransformListeners(input, dmnInput);
    }

    for (Output output : decisionTable.getOutputs()) {
      parent = dmnDecisionTable;
      DmnOutput dmnOutput = transformOutput(output);
      dmnDecisionTable.addOutput(dmnOutput);
      notifyTransformListeners(output, dmnOutput);
    }

    for (Rule rule : decisionTable.getRules()) {
      parent = dmnDecisionTable;
      transformRule(rule);
    }

    return dmnDecisionTable;
  }

  protected DmnInputImpl transformInput(Input input) {
    DmnElementHandler<Input, DmnInputImpl> inputHandler = getElementHandler(Input.class);
    DmnInputImpl dmnInput = inputHandler.handleElement(this, input);


    InputExpression inputExpression = input.getInputExpression();
    if (inputExpression != null) {
      parent = dmnInput;
      DmnExpression dmnInputExpression = transformInputExpression(inputExpression);
      dmnInput.setInputExpression(dmnInputExpression);
    }

    return dmnInput;
  }

  protected DmnExpression transformInputExpression(InputExpression inputExpression) {
    DmnElementHandler<InputExpression, DmnExpressionImpl> inputExpressionHandler = getElementHandler(InputExpression.class);

    DmnExpressionImpl dmnInputExpression = inputExpressionHandler.handleElement(this, inputExpression);

    String typeRef = inputExpression.getTypeRef();
    if (typeRef != null) {
      DmnTypeDefinitionImpl dmnTypeDefinition = new DmnTypeDefinitionImpl();
      dmnTypeDefinition.setTypeName(typeRef);
      DataTypeTransformer dataTypeTransformer = getDataTypeTransformerFactory().getTransformerForType(typeRef);
      dmnTypeDefinition.setTransformer(dataTypeTransformer);
      dmnInputExpression.setTypeDefinition(dmnTypeDefinition);
    }

    return dmnInputExpression;
  }

  protected DmnOutput transformOutput(Output output) {
    DmnElementHandler<Output, DmnOutputImpl> outputHandler = getElementHandler(Output.class);
    DmnOutputImpl dmnOutput = outputHandler.handleElement(this, output);

    String typeRef = output.getTypeRef();
    if (typeRef != null) {
      DmnTypeDefinitionImpl dmnTypeDefinition = new DmnTypeDefinitionImpl();
      dmnTypeDefinition.setTypeName(typeRef);
      DataTypeTransformer dataTypeTransformer = getDataTypeTransformerFactory().getTransformerForType(typeRef);
      dmnTypeDefinition.setTransformer(dataTypeTransformer);
      dmnOutput.setTypeDefinition(dmnTypeDefinition);
    }

    return dmnOutput;
  }

  protected void transformRule(Rule rule) {
    DmnElementHandler<Rule, DmnRuleImpl> ruleHandler = getElementHandler(Rule.class);

    DmnDecisionTable dmnDecisionTable = (DmnDecisionTable) parent;
    DmnRuleImpl dmnRule = ruleHandler.handleElement(this, rule);

    List<DmnInput> inputs = dmnDecisionTable.getInputs();
    List<InputEntry> inputEntries = new ArrayList<InputEntry>(rule.getInputEntries());
    if (inputs.size() != inputEntries.size()) {
      throw LOG.differentNumberOfInputsAndInputEntries(inputs.size(), inputEntries.size(), dmnRule);
    }
    for (int i = 0; i < inputEntries.size(); i++) {
      InputEntry inputEntry = inputEntries.get(i);
      parent = dmnRule;
      DmnInputEntryImpl dmnInputEntry = transformInputEntry(inputEntry);

      DmnInput dmnInput = inputs.get(i);
      dmnInputEntry.setInput(dmnInput);

      dmnRule.addInputEntry(dmnInputEntry);
    }

    List<DmnOutput> outputs = dmnDecisionTable.getOutputs();
    ArrayList<OutputEntry> outputEntries = new ArrayList<OutputEntry>(rule.getOutputEntries());
    if (outputs.size() != outputEntries.size()) {
      throw LOG.differentNumberOfOutputsAndOutputEntries(outputs.size(), outputEntries.size(), dmnRule);
    }
    for (int i = 0; i < outputEntries.size(); i++) {
      OutputEntry outputEntry = outputEntries.get(i);
      parent = dmnRule;
      DmnOutputEntryImpl dmnOutputEntry = transformOutputEntry(outputEntry);

      DmnOutput dmnOutput = outputs.get(i);
      dmnOutputEntry.setOutput(dmnOutput);

      dmnRule.addOutputEntry(dmnOutputEntry);
    }

    notifyTransformListeners(rule, dmnRule);

    decision.addRule(dmnRule);
  }

  protected DmnInputEntryImpl transformInputEntry(InputEntry inputEntry) {
    DmnElementHandler<InputEntry, DmnInputEntryImpl> inputEntryHandler = getElementHandler(InputEntry.class);
    return inputEntryHandler.handleElement(this, inputEntry);
  }

  protected DmnOutputEntryImpl transformOutputEntry(OutputEntry outputEntry) {
    DmnElementHandler<OutputEntry, DmnOutputEntryImpl> outputEntryHandler = getElementHandler(OutputEntry.class);
    return outputEntryHandler.handleElement(this, outputEntry);
  }

  // Helper /////////////////////////////////////////////////////////////////////////////////////////////////////////

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

  protected void notifyTransformListeners(Input input, DmnInput dmnInput) {
    for (DmnTransformListener transformListener : transformListeners) {
      transformListener.transformInput(input, dmnInput);
    }
  }

  protected void notifyTransformListeners(Output output, DmnOutput dmnOutput) {
    for (DmnTransformListener transformListener : transformListeners) {
      transformListener.transformOutput(output, dmnOutput);
    }
  }

  protected void notifyTransformListeners(Rule rule, DmnRule dmnRule) {
    for (DmnTransformListener transformListener : transformListeners) {
      transformListener.transformRule(rule, dmnRule);
    }
  }

  // Context methods //////////////////////////////////////////////////////////////////////////////////////////////////////


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

  public DataTypeTransformerFactory getDataTypeTransformerFactory() {
    return dataTypeTransformerFactory;
  }

}


