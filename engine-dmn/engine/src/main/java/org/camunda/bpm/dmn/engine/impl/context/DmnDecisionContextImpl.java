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

package org.camunda.bpm.dmn.engine.impl.context;

import static org.camunda.bpm.engine.variable.impl.context.SingleVariableContext.singleVariable;
import static org.camunda.commons.utils.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTable;
import org.camunda.bpm.dmn.engine.DmnDecisionTableListener;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnDecisionTableRule;
import org.camunda.bpm.dmn.engine.DmnDecisionTableValue;
import org.camunda.bpm.dmn.engine.DmnExpression;
import org.camunda.bpm.dmn.engine.DmnInput;
import org.camunda.bpm.dmn.engine.DmnInputEntry;
import org.camunda.bpm.dmn.engine.DmnOutputEntry;
import org.camunda.bpm.dmn.engine.DmnRule;
import org.camunda.bpm.dmn.engine.DmnScriptEngineResolver;
import org.camunda.bpm.dmn.engine.context.DmnDecisionContext;
import org.camunda.bpm.dmn.engine.el.ElExpression;
import org.camunda.bpm.dmn.engine.el.ElProvider;
import org.camunda.bpm.dmn.engine.hitpolicy.DmnHitPolicyHandler;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionOutputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionResultImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableResultImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableRuleImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableValueImpl;
import org.camunda.bpm.dmn.engine.impl.DmnEngineConfigurationImpl;
import org.camunda.bpm.dmn.engine.impl.DmnEngineLogger;
import org.camunda.bpm.dmn.feel.FeelEngine;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.context.VariableContext;
import org.camunda.bpm.engine.variable.impl.context.CompositeVariableContext;
import org.camunda.bpm.engine.variable.value.TypedValue;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.commons.utils.StringUtil;

public class DmnDecisionContextImpl implements DmnDecisionContext {

  protected static final DmnEngineLogger LOG = DmnEngineLogger.ENGINE_LOGGER;

  protected DmnScriptEngineResolver scriptEngineResolver;
  protected ElProvider elProvider;
  protected FeelEngine feelEngine;
  protected Map<HitPolicy, DmnHitPolicyHandler> hitPolicyHandlers;
  protected List<DmnDecisionTableListener> decisionTableListeners = new ArrayList<DmnDecisionTableListener>();

  protected String defaultAllowedValueExpressionLanguage;
  protected String defaultInputEntryExpressionLanguage;
  protected String defaultInputExpressionExpressionLanguage;
  protected String defaultOutputEntryExpressionLanguage;

  public DmnScriptEngineResolver getScriptEngineResolver() {
    return scriptEngineResolver;
  }

  public void setScriptEngineResolver(DmnScriptEngineResolver scriptEngineResolver) {
    this.scriptEngineResolver = scriptEngineResolver;
  }

  public void setElProvider(ElProvider elProvider) {
    this.elProvider = elProvider;
  }

  public ElProvider getElProvider() {
    return elProvider;
  }

  public FeelEngine getFeelEngine() {
    return feelEngine;
  }

  public void setFeelEngine(FeelEngine feelEngine) {
    this.feelEngine = feelEngine;
  }

  public Map<HitPolicy, DmnHitPolicyHandler> getHitPolicyHandlers() {
    return hitPolicyHandlers;
  }

  public DmnHitPolicyHandler getHitPolicyHandler(HitPolicy hitPolicy) {
    if (hitPolicyHandlers == null) {
      return null;
    } else {
      return hitPolicyHandlers.get(hitPolicy);
    }
  }

  public DmnHitPolicyHandler getHitPolicyHandlerChecked(HitPolicy hitPolicy) {
    DmnHitPolicyHandler hitPolicyHandler = getHitPolicyHandler(hitPolicy);
    if (hitPolicyHandler != null) {
      return hitPolicyHandler;
    } else {
      throw LOG.unableToFindHitPolicyHandlerFor(hitPolicy);
    }
  }

  public void setHitPolicyHandlers(Map<HitPolicy, DmnHitPolicyHandler> hitPolicyHandlers) {
    this.hitPolicyHandlers = hitPolicyHandlers;
  }

  public List<DmnDecisionTableListener> getDecisionTableListeners() {
    return decisionTableListeners;
  }

  public void setDecisionTableListeners(List<DmnDecisionTableListener> decisionTableListeners) {
    this.decisionTableListeners = decisionTableListeners;
  }

  public String getDefaultAllowedValueExpressionLanguage() {
    return defaultAllowedValueExpressionLanguage;
  }

  public void setDefaultAllowedValueExpressionLanguage(String defaultAllowedValueExpressionLanguage) {
    this.defaultAllowedValueExpressionLanguage = defaultAllowedValueExpressionLanguage;
  }

  public String getDefaultInputEntryExpressionLanguage() {
    return defaultInputEntryExpressionLanguage;
  }

  public void setDefaultInputEntryExpressionLanguage(String defaultInputEntryExpressionLanguage) {
    this.defaultInputEntryExpressionLanguage = defaultInputEntryExpressionLanguage;
  }

  public String getDefaultInputExpressionExpressionLanguage() {
    return defaultInputExpressionExpressionLanguage;
  }

  public void setDefaultInputExpressionExpressionLanguage(String defaultInputExpressionExpressionLanguage) {
    this.defaultInputExpressionExpressionLanguage = defaultInputExpressionExpressionLanguage;
  }

  public String getDefaultOutputEntryExpressionLanguage() {
    return defaultOutputEntryExpressionLanguage;
  }

  public void setDefaultOutputEntryExpressionLanguage(String defaultOutputEntryExpressionLanguage) {
    this.defaultOutputEntryExpressionLanguage = defaultOutputEntryExpressionLanguage;
  }

  public DmnDecisionResult evaluateDecision(DmnDecision decision, VariableContext variableContext) {
    ensureNotNull("decision", decision);
    if (decision instanceof DmnDecisionTable) {
      return evaluateDecisionTable((DmnDecisionTable) decision, variableContext);
    } else {
      throw LOG.decisionTypeNotSupported(decision);
    }
  }

  protected DmnDecisionResult evaluateDecisionTable(DmnDecisionTable decisionTable, VariableContext variableContext) {
    DmnDecisionTableResultImpl decisionTableResult = new DmnDecisionTableResultImpl();
    decisionTableResult.setExecutedDecisionElements(calculateExecutedDecisionElements(decisionTable));

    // evaluate inputs
    Map<String, DmnDecisionTableValue> inputs = evaluateDecisionTableInputs(decisionTable, variableContext);
    decisionTableResult.setInputs(inputs);

    // evaluate rules
    List<DmnDecisionTableRule> matchingRules = decisionTableResult.getMatchingRules();
    for (DmnRule rule : decisionTable.getRules()) {
      if (isRuleApplicable(rule, variableContext, inputs)) {
        DmnDecisionTableRuleImpl matchingRule = evaluateMatchingRule(rule, variableContext);
        matchingRules.add(matchingRule);
      }
    }

    // generate result
    return generateDecisionTableResult(decisionTable, decisionTableResult);
  }

  protected DmnDecisionResult generateDecisionTableResult(DmnDecisionTable decisionTable, DmnDecisionTableResult decisionTableResult) {
    // call hit policy handler
    DmnHitPolicyHandler hitPolicyHandler = getHitPolicyHandlerChecked(decisionTable.getHitPolicy());
    decisionTableResult = hitPolicyHandler.apply(decisionTable, decisionTableResult);

    // notify listeners
    for (DmnDecisionTableListener decisionTableListener : decisionTableListeners) {
      decisionTableListener.notify(decisionTable, decisionTableResult);
    }

    // generate output
    return generateDecisionResult(decisionTableResult);
  }

  protected DmnDecisionResult generateDecisionResult(DmnDecisionTableResult decisionTableResult) {
    DmnDecisionResultImpl decisionResult = new DmnDecisionResultImpl();
    if (decisionTableResult.getCollectResultName() != null || decisionTableResult.getCollectResultValue() != null) {
      DmnDecisionOutputImpl decisionOutput = new DmnDecisionOutputImpl();
      TypedValue resultValue = decisionTableResult.getCollectResultValue();
      decisionOutput.putValue(decisionTableResult.getCollectResultName(), resultValue);
      decisionResult.addOutput(decisionOutput);
    } else {
      for (DmnDecisionTableRule matchingRule : decisionTableResult.getMatchingRules()) {
        DmnDecisionOutputImpl decisionOutput = new DmnDecisionOutputImpl();
        for (DmnDecisionTableValue outputValue : matchingRule.getOutputs().values()) {
          decisionOutput.putValue(outputValue.getOutputName(), outputValue.getValue());
        }
        decisionResult.addOutput(decisionOutput);
      }
    }
    return decisionResult;
  }

  protected long calculateExecutedDecisionElements(DmnDecisionTable decisionTable) {
    return (decisionTable.getInputs().size() + decisionTable.getOutputs().size()) * decisionTable.getRules().size();
  }

  protected Map<String, DmnDecisionTableValue> evaluateDecisionTableInputs(DmnDecisionTable decisionTable, VariableContext variableContext) {
    Map<String, DmnDecisionTableValue> inputs = new HashMap<String, DmnDecisionTableValue>();
    for (DmnInput dmnInput : decisionTable.getInputs()) {
      if (isNonEmptyExpression(dmnInput.getInputExpression())) {
        DmnDecisionTableValue input = evaluateInputClause(dmnInput, variableContext);
        inputs.put(input.getKey(), input);
      }
    }
    return inputs;
  }

  protected DmnDecisionTableValue evaluateInputClause(DmnInput dmnInput, VariableContext variableContext) {
    DmnDecisionTableValueImpl input = new DmnDecisionTableValueImpl(dmnInput);
    DmnExpression inputExpression = dmnInput.getInputExpression();

    if (inputExpression != null) {
      Object value = evaluateInputExpression(inputExpression, variableContext);
      TypedValue typedValue = inputExpression.getTypeDefinition().transform(value);
      input.setValue(typedValue);

    } else {
      input.setValue(Variables.untypedNullValue());
    }
    return input;
  }

  protected boolean isRuleApplicable(DmnRule rule, VariableContext variableContext, Map<String, DmnDecisionTableValue> inputs) {
    Map<String, Boolean> clauseSatisfied = new HashMap<String, Boolean>();
    List<DmnInputEntry> conditions = rule.getInputEntries();

    for (DmnInputEntry condition : conditions) {
      String clauseKey = condition.getInput().getKey();
      Boolean alreadySatisfied = clauseSatisfied.get(clauseKey);
      if (alreadySatisfied != null && alreadySatisfied) {
        // skip condition if clause already satisfied
        continue;
      }

      // set temporary evaluation variable cache
      VariableContext localVariableContext;

      // set input clause variable
      if (inputs.containsKey(clauseKey)) {
        DmnDecisionTableValue inputValue = inputs.get(clauseKey);

        // compose local variable context out of global variable context enhanced with the value of the current input.
        localVariableContext = CompositeVariableContext.compose(
            singleVariable(inputValue.getOutputName(), inputValue.getValue()),
            variableContext
        );
      }
      else {
        localVariableContext = variableContext;
      }

      boolean applicable = isConditionApplicable(condition, localVariableContext);
      clauseSatisfied.put(clauseKey, applicable);
    }

    // the rule is applicable if all involved clauses are satisfied
    return !clauseSatisfied.containsValue(false);
  }

  protected DmnDecisionTableRuleImpl evaluateMatchingRule(DmnRule rule, VariableContext variableContext) {
    DmnDecisionTableRuleImpl ruleResult = new DmnDecisionTableRuleImpl();
    ruleResult.setKey(rule.getKey());
    evaluateRuleOutput(ruleResult, rule, variableContext);
    return ruleResult;
  }

  protected void evaluateRuleOutput(DmnDecisionTableRuleImpl ruleResult, DmnRule rule, VariableContext variableContext) {
    for (DmnOutputEntry conclusion : rule.getOutputEntries()) {
      if (isNonEmptyExpression(conclusion)) {
        DmnDecisionTableValueImpl output = new DmnDecisionTableValueImpl(conclusion.getOutput());
        Object value = evaluateOutputEntry(conclusion, variableContext);
        TypedValue typedValue = conclusion.getOutput().getTypeDefinition().transform(value);

        output.setValue(typedValue);
        ruleResult.addOutput(output);
      }
    }
  }

  protected boolean isConditionApplicable(DmnInputEntry condition, VariableContext localVariableContext) {
    Object result = evaluateInputEntry(condition, localVariableContext);
    return result != null && result.equals(true);
  }

  protected Object evaluateInputExpression(DmnExpression inputExpression, VariableContext variableContext) {
    String expressionLanguage = inputExpression.getExpressionLanguage();
    if (expressionLanguage == null) {
      expressionLanguage = getDefaultInputExpressionExpressionLanguage();
    }
    if (isFeelExpressionLanguage(expressionLanguage)) {
      return evaluateFeelSimpleExpression(inputExpression, variableContext);
    }
    else {
      return evaluateExpression(expressionLanguage, inputExpression, variableContext);
    }
  }

  protected Object evaluateInputEntry(DmnInputEntry inputEntry, VariableContext variableContext) {
    if (isNonEmptyExpression(inputEntry)) {
      String expressionLanguage = inputEntry.getExpressionLanguage();
      if (expressionLanguage == null) {
        expressionLanguage = getDefaultInputEntryExpressionLanguage();
      }
      if (isFeelExpressionLanguage(expressionLanguage)) {
        return evaluateFeelSimpleUnaryTests(inputEntry, variableContext);
      } else {
        return evaluateExpression(expressionLanguage, inputEntry, variableContext);
      }
    }
    else {
      return true; // input entries without expressions are true
    }
  }

  protected Object evaluateOutputEntry(DmnOutputEntry outputEntry, VariableContext variableContext) {
    String expressionLanguage = outputEntry.getExpressionLanguage();
    if (expressionLanguage == null) {
      expressionLanguage = getDefaultOutputEntryExpressionLanguage();
    }
    if (isFeelExpressionLanguage(expressionLanguage)) {
      return evaluateFeelSimpleExpression(outputEntry, variableContext);
    }
    else {
      return evaluateExpression(expressionLanguage, outputEntry, variableContext);
    }
  }

  protected Object evaluateFeelSimpleExpression(DmnExpression feelExpression, VariableContext variableContext) {
    String feelSimpleExpression = feelExpression.getExpression();
    if (feelSimpleExpression != null) {
      return feelEngine.evaluateSimpleExpression(feelSimpleExpression, variableContext);
    }
    else {
      return null;
    }
  }

  protected Object evaluateFeelSimpleUnaryTests(DmnInputEntry feelExpression, VariableContext variableContext) {
    String feelSimpleUnaryTests = feelExpression.getExpression();
    if (feelSimpleUnaryTests != null) {
      String inputVariableName = feelExpression.getInput().getOutputName();
      return feelEngine.evaluateSimpleUnaryTests(feelSimpleUnaryTests, inputVariableName, variableContext);
    }
    else {
      return null;
    }
  }

  protected Object evaluateExpression(String expressionLanguage, DmnExpression expression, VariableContext variableContext) {
    String expressionText = getExpressionTextForLanguage(expression, expressionLanguage);
    if (expressionText != null) {
      if(isElExpression(expressionLanguage)) {
        ElExpression elExpression = elProvider.createExpression(expressionText);
        try {
          return elExpression.getValue(variableContext);
        }
        // yes, we catch all exceptions
        catch(Exception e) {
          throw LOG.unableToEvaluateExpression(expressionText, expressionLanguage, e);
        }
      }
      else {
        ScriptEngine scriptEngine = getScriptEngineForName(expressionLanguage);
        // wrap script engine bindings + variable context and pass enhanced
        // bindings to the script engine.
        Bindings bindings = VariableContextScriptBindings.wrap(scriptEngine.createBindings(), variableContext);
        bindings.put("variableContext", variableContext);

        try {
          return scriptEngine.eval(expressionText, bindings);
        }
        catch (ScriptException e) {
          throw LOG.unableToEvaluateExpression(expressionText, scriptEngine.getFactory().getLanguageName(), e);
        }
      }
    } else {
      return null;
    }
  }

  protected boolean isElExpression(String expressionLanguage) {
    return DmnEngineConfigurationImpl.JUEL_EXPRESSION_LANGUAGE.equals(expressionLanguage);
  }

  protected ScriptEngine getScriptEngineForName(String expressionLanguage) {
    ensureNotNull("expressionLanguage", expressionLanguage);
    ScriptEngine scriptEngine = scriptEngineResolver.getScriptEngineForLanguage(expressionLanguage);
    if (scriptEngine != null) {
      return scriptEngine;
    }
    else {
      throw LOG.noScriptEngineFoundForLanguage(expressionLanguage);
    }
  }

  protected String getExpressionTextForLanguage(DmnExpression expression, String expressionLanguage) {
    String expressionText = expression.getExpression();
    if (expressionText != null) {
      if (DmnEngineConfigurationImpl.JUEL_EXPRESSION_LANGUAGE.equals(expressionLanguage) && !StringUtil.isExpression(expressionText)) {
        return "${" + expressionText + "}";
      }
      else {
        return expressionText;
      }
    }
    else {
      return null;
    }
  }

  protected boolean isNonEmptyExpression(DmnExpression expression) {
    return expression != null && expression.getExpression() != null && !expression.getExpression().trim().isEmpty();
  }

  protected boolean isFeelExpressionLanguage(String expressionLanguage) {
    ensureNotNull("expressionLanguage", expressionLanguage);
    return expressionLanguage.equals(DmnEngineConfigurationImpl.FEEL_EXPRESSION_LANGUAGE) ||
      expressionLanguage.toLowerCase().equals(DmnEngineConfigurationImpl.FEEL_EXPRESSION_LANGUAGE_ALTERNATIVE);
  }

}
