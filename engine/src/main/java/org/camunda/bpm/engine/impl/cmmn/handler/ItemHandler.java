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

import static org.camunda.bpm.engine.delegate.CaseExecutionListener.COMPLETE;
import static org.camunda.bpm.engine.delegate.CaseExecutionListener.TERMINATE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.delegate.CaseVariableListener;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.VariableListener;
import org.camunda.bpm.engine.impl.bpmn.helper.CmmnProperties;
import org.camunda.bpm.engine.impl.bpmn.parser.FieldDeclaration;
import org.camunda.bpm.engine.impl.cmmn.CaseControlRule;
import org.camunda.bpm.engine.impl.cmmn.behavior.CaseControlRuleImpl;
import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.listener.ClassDelegateCaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.listener.DelegateExpressionCaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.listener.ExpressionCaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.listener.ScriptCaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnSentryDeclaration;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.engine.impl.scripting.engine.ScriptingEngines;
import org.camunda.bpm.engine.impl.util.ScriptUtil;
import org.camunda.bpm.engine.impl.variable.listener.ClassDelegateCaseVariableListener;
import org.camunda.bpm.engine.impl.variable.listener.DelegateExpressionCaseVariableListener;
import org.camunda.bpm.engine.impl.variable.listener.ExpressionCaseVariableListener;
import org.camunda.bpm.engine.impl.variable.listener.ScriptCaseVariableListener;
import org.camunda.bpm.model.cmmn.Query;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.cmmn.instance.ConditionExpression;
import org.camunda.bpm.model.cmmn.instance.DiscretionaryItem;
import org.camunda.bpm.model.cmmn.instance.Documentation;
import org.camunda.bpm.model.cmmn.instance.ExtensionElements;
import org.camunda.bpm.model.cmmn.instance.ManualActivationRule;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.PlanItemControl;
import org.camunda.bpm.model.cmmn.instance.PlanItemDefinition;
import org.camunda.bpm.model.cmmn.instance.RepetitionRule;
import org.camunda.bpm.model.cmmn.instance.RequiredRule;
import org.camunda.bpm.model.cmmn.instance.Sentry;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaCaseExecutionListener;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaExpression;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaField;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaScript;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaString;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaVariableListener;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;

/**
 * @author Roman Smirnov
 *
 */
public abstract class ItemHandler extends CmmnElementHandler<CmmnElement, CmmnActivity> {

  public static final String PROPERTY_AUTO_COMPLETE = "autoComplete";
  public static final String PROPERTY_REQUIRED_RULE = "requiredRule";
  public static final String PROPERTY_MANUAL_ACTIVATION_RULE = "manualActivationRule";
  public static final String PROPERTY_REPETITION_RULE = "repetitionRule";
  public static final String PROPERTY_IS_BLOCKING = "isBlocking";
  public static final String PROPERTY_DISCRETIONARY = "discretionary";
  public static final String PROPERTY_ACTIVITY_TYPE = "activityType";
  public static final String PROPERTY_ACTIVITY_DESCRIPTION = "description";

  protected static final String PARENT_COMPLETE = "parentComplete";

  public static List<String> TASK_OR_STAGE_CREATE_EVENTS = Arrays.asList(
      CaseExecutionListener.CREATE
    );

  public static List<String> TASK_OR_STAGE_UPDATE_EVENTS = Arrays.asList(
      CaseExecutionListener.ENABLE,
      CaseExecutionListener.DISABLE,
      CaseExecutionListener.RE_ENABLE,
      CaseExecutionListener.START,
      CaseExecutionListener.MANUAL_START,
      CaseExecutionListener.SUSPEND,
      CaseExecutionListener.PARENT_SUSPEND,
      CaseExecutionListener.RESUME,
      CaseExecutionListener.PARENT_RESUME
    );

  public static List<String> TASK_OR_STAGE_END_EVENTS = Arrays.asList(
      CaseExecutionListener.TERMINATE,
      CaseExecutionListener.EXIT,
      CaseExecutionListener.COMPLETE,
      PARENT_COMPLETE
    );

  public static List<String> TASK_OR_STAGE_EVENTS = new ArrayList<String>();

  public static List<String> EVENT_LISTENER_OR_MILESTONE_CREATE_EVENTS = Arrays.asList(
      CaseExecutionListener.CREATE
    );

  public static List<String> EVENT_LISTENER_OR_MILESTONE_UPDATE_EVENTS = Arrays.asList(
      CaseExecutionListener.SUSPEND,
      CaseExecutionListener.RESUME
    );

  public static List<String> EVENT_LISTENER_OR_MILESTONE_END_EVENTS = Arrays.asList(
      CaseExecutionListener.TERMINATE,
      CaseExecutionListener.PARENT_TERMINATE,
      CaseExecutionListener.OCCUR,
      PARENT_COMPLETE
    );

  public static List<String> EVENT_LISTENER_OR_MILESTONE_EVENTS = new ArrayList<String>();

  public static List<String> CASE_PLAN_MODEL_CREATE_EVENTS = Arrays.asList(
      CaseExecutionListener.CREATE
    );

  public static List<String> CASE_PLAN_MODEL_UPDATE_EVENTS = Arrays.asList(
      CaseExecutionListener.TERMINATE,
      CaseExecutionListener.SUSPEND,
      CaseExecutionListener.COMPLETE,
      CaseExecutionListener.RE_ACTIVATE
    );

  public static List<String> CASE_PLAN_MODEL_CLOSE_EVENTS = Arrays.asList(
      CaseExecutionListener.CLOSE
    );

  public static List<String> CASE_PLAN_MODEL_EVENTS = new ArrayList<String>();

  public static List<String> DEFAULT_VARIABLE_EVENTS = Arrays.asList(
      VariableListener.CREATE,
      VariableListener.DELETE,
      VariableListener.UPDATE
  );

  static {
    TASK_OR_STAGE_EVENTS.addAll(TASK_OR_STAGE_CREATE_EVENTS);
    TASK_OR_STAGE_EVENTS.addAll(TASK_OR_STAGE_UPDATE_EVENTS);
    TASK_OR_STAGE_EVENTS.addAll(TASK_OR_STAGE_END_EVENTS);

    EVENT_LISTENER_OR_MILESTONE_EVENTS.addAll(EVENT_LISTENER_OR_MILESTONE_CREATE_EVENTS);
    EVENT_LISTENER_OR_MILESTONE_EVENTS.addAll(EVENT_LISTENER_OR_MILESTONE_UPDATE_EVENTS);
    EVENT_LISTENER_OR_MILESTONE_EVENTS.addAll(EVENT_LISTENER_OR_MILESTONE_END_EVENTS);

    CASE_PLAN_MODEL_EVENTS.addAll(CASE_PLAN_MODEL_CREATE_EVENTS);
    CASE_PLAN_MODEL_EVENTS.addAll(CASE_PLAN_MODEL_UPDATE_EVENTS);
    CASE_PLAN_MODEL_EVENTS.addAll(CASE_PLAN_MODEL_CLOSE_EVENTS);
  }

  protected CmmnActivity createActivity(CmmnElement element, CmmnHandlerContext context) {
    String id = element.getId();
    CmmnActivity parent = context.getParent();

    CmmnActivity newActivity = null;

    if (parent != null) {
      newActivity = parent.createActivity(id);

    } else {
      CmmnCaseDefinition caseDefinition = context.getCaseDefinition();
      newActivity = new CmmnActivity(id, caseDefinition);
    }

    newActivity.setCmmnElement(element);

    CmmnActivityBehavior behavior = getActivityBehavior();
    newActivity.setActivityBehavior(behavior);

    return newActivity;
  }

  protected CmmnActivityBehavior getActivityBehavior() {
    return null;
  }

  public CmmnActivity handleElement(CmmnElement element, CmmnHandlerContext context) {
    // create a new activity
    CmmnActivity newActivity = createActivity(element, context);

    // initialize activity
    initializeActivity(element, newActivity, context);

    return newActivity;
  }

  protected void initializeActivity(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    if (isDiscretionaryItem(element)) {
      activity.setProperty(PROPERTY_DISCRETIONARY, true);
    }

    String name = getName(element);

    if (name == null) {
      PlanItemDefinition definition = getDefinition(element);
      if (definition != null) {
        name = definition.getName();
      }
    }

    activity.setName(name);

    // activityType
    initializeActivityType(element, activity, context);

    // description
    initializeDescription(element, activity, context);

    // autoComplete
    initializeAutoComplete(element, activity, context);

    // requiredRule
    initializeRequiredRule(element, activity, context);

    // manualActivation
    initializeManualActivationRule(element, activity, context);

    // repetitionRule
    initializeRepetitionRule(element, activity, context);

    // case execution listeners
    initializeCaseExecutionListeners(element, activity, context);

    // variable listeners
    initializeVariableListeners(element, activity, context);

    // initialize entry criteria
    initializeEntryCriterias(element, activity, context);

    // initialize exit criteria
    initializeExitCriterias(element, activity, context);

  }

  protected void initializeActivityType(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    PlanItemDefinition definition = getDefinition(element);

    String activityType = null;
    if (definition != null) {
      ModelElementType elementType = definition.getElementType();
      if (elementType != null) {
        activityType = elementType.getTypeName();
      }
    }

    activity.setProperty(PROPERTY_ACTIVITY_TYPE, activityType);
  }

  protected void initializeDescription(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    String description = getDesciption(element);
    if (description == null) {
      description = getDocumentation(element);
    }
    activity.setProperty(PROPERTY_ACTIVITY_DESCRIPTION, description);
  }

  protected void initializeAutoComplete(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    // noop
  }

  protected void initializeRequiredRule(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    PlanItemControl itemControl = getItemControl(element);
    PlanItemControl defaultControl = getDefaultControl(element);

    RequiredRule requiredRule = null;
    if (itemControl != null) {
      requiredRule = itemControl.getRequiredRule();
    }
    if (requiredRule == null && defaultControl != null) {
      requiredRule = defaultControl.getRequiredRule();
    }

    if (requiredRule != null) {
      CaseControlRule caseRule = initializeCaseControlRule(requiredRule.getCondition(), context);
      activity.setProperty(PROPERTY_REQUIRED_RULE, caseRule);
    }

  }

  protected void initializeManualActivationRule(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    PlanItemControl itemControl = getItemControl(element);
    PlanItemControl defaultControl = getDefaultControl(element);

    ManualActivationRule manualActivationRule = null;
    if (itemControl != null) {
      manualActivationRule = itemControl.getManualActivationRule();
    }
    if (manualActivationRule == null && defaultControl != null) {
      manualActivationRule = defaultControl.getManualActivationRule();
    }

    if (manualActivationRule != null) {
      CaseControlRule caseRule = initializeCaseControlRule(manualActivationRule.getCondition(), context);
      activity.setProperty(PROPERTY_MANUAL_ACTIVATION_RULE, caseRule);
    }

  }

  protected void initializeRepetitionRule(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    PlanItemControl itemControl = getItemControl(element);
    PlanItemControl defaultControl = getDefaultControl(element);

    RepetitionRule repetitionRule = null;
    if (itemControl != null) {
      repetitionRule = itemControl.getRepetitionRule();
    }
    if (repetitionRule == null && defaultControl != null) {
      repetitionRule = defaultControl.getRepetitionRule();
    }

    if (repetitionRule != null) {
      ConditionExpression condition = repetitionRule.getCondition();
      CaseControlRule caseRule = initializeCaseControlRule(condition, context);
      activity.setProperty(PROPERTY_REPETITION_RULE, caseRule);

      List<String> events = Arrays.asList(TERMINATE, COMPLETE);
      String repeatOnStandardEvent = repetitionRule.getCamundaRepeatOnStandardEvent();
      if (repeatOnStandardEvent != null && !repeatOnStandardEvent.isEmpty()) {
        events = Arrays.asList(repeatOnStandardEvent);
      }
      activity.getProperties().set(CmmnProperties.REPEAT_ON_STANDARD_EVENTS, events);
    }
  }

  protected CaseControlRule initializeCaseControlRule(ConditionExpression condition, CmmnHandlerContext context) {
    Expression expression = null;

    if (condition != null) {
      String rule = condition.getText();
      if (rule != null && !rule.isEmpty()) {
        ExpressionManager expressionManager = context.getExpressionManager();
        expression = expressionManager.createExpression(rule);
      }
    }

    return new CaseControlRuleImpl(expression);
  }

  protected void initializeCaseExecutionListeners(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    PlanItemDefinition definition = getDefinition(element);

    List<CamundaCaseExecutionListener> listeners = queryExtensionElementsByClass(definition, CamundaCaseExecutionListener.class);

    for (CamundaCaseExecutionListener listener : listeners) {
      CaseExecutionListener caseExecutionListener = initializeCaseExecutionListener(element, activity, context, listener);

      String eventName = listener.getCamundaEvent();
      if(eventName != null) {
        activity.addListener(eventName, caseExecutionListener);

      } else {
        for (String event : getStandardEvents(element)) {
          activity.addListener(event, caseExecutionListener);
        }
      }
    }
  }

  protected CaseExecutionListener initializeCaseExecutionListener(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context, CamundaCaseExecutionListener listener) {
    Collection<CamundaField> fields = listener.getCamundaFields();
    List<FieldDeclaration> fieldDeclarations = initializeFieldDeclarations(element, activity, context, fields);

    ExpressionManager expressionManager = context.getExpressionManager();

    CaseExecutionListener caseExecutionListener = null;

    String className = listener.getCamundaClass();
    String expression = listener.getCamundaExpression();
    String delegateExpression = listener.getCamundaDelegateExpression();
    CamundaScript scriptElement = listener.getCamundaScript();

    if (className != null) {
      caseExecutionListener = new ClassDelegateCaseExecutionListener(className, fieldDeclarations);

    } else if (expression != null) {
      Expression expressionExp = expressionManager.createExpression(expression);
      caseExecutionListener = new ExpressionCaseExecutionListener(expressionExp);

    } else if (delegateExpression != null) {
      Expression delegateExp = expressionManager.createExpression(delegateExpression);
      caseExecutionListener = new DelegateExpressionCaseExecutionListener(delegateExp, fieldDeclarations);

    } else if (scriptElement != null) {
      ExecutableScript executableScript = initializeScript(element, activity, context, scriptElement);
      if (executableScript != null) {
        caseExecutionListener = new ScriptCaseExecutionListener(executableScript);
      }
    }

    return caseExecutionListener;
  }

  protected void initializeVariableListeners(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    PlanItemDefinition definition = getDefinition(element);

    List<CamundaVariableListener> listeners = queryExtensionElementsByClass(definition, CamundaVariableListener.class);

    for (CamundaVariableListener listener : listeners) {
      CaseVariableListener variableListener = initializeVariableListener(element, activity, context, listener);

      String eventName = listener.getCamundaEvent();
      if(eventName != null) {
        activity.addVariableListener(eventName, variableListener);

      } else {
        for (String event : DEFAULT_VARIABLE_EVENTS) {
          activity.addVariableListener(event, variableListener);
        }
      }
    }
  }

  protected CaseVariableListener initializeVariableListener(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context, CamundaVariableListener listener) {
    Collection<CamundaField> fields = listener.getCamundaFields();
    List<FieldDeclaration> fieldDeclarations = initializeFieldDeclarations(element, activity, context, fields);

    ExpressionManager expressionManager = context.getExpressionManager();

    String className = listener.getCamundaClass();
    String expression = listener.getCamundaExpression();
    String delegateExpression = listener.getCamundaDelegateExpression();
    CamundaScript scriptElement = listener.getCamundaScript();

    CaseVariableListener variableListener = null;
    if (className != null) {
      variableListener = new ClassDelegateCaseVariableListener(className, fieldDeclarations);

    } else if (expression != null) {
      Expression expressionExp = expressionManager.createExpression(expression);
      variableListener = new ExpressionCaseVariableListener(expressionExp);

    } else if (delegateExpression != null) {
      Expression delegateExp = expressionManager.createExpression(delegateExpression);
      variableListener = new DelegateExpressionCaseVariableListener(delegateExp, fieldDeclarations);

    } else if (scriptElement != null) {
      ExecutableScript executableScript = initializeScript(element, activity, context, scriptElement);
      if (executableScript != null) {
        variableListener = new ScriptCaseVariableListener(executableScript);
      }
    }

    return variableListener;
  }

  protected ExecutableScript initializeScript(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context, CamundaScript script) {
    String language = script.getCamundaScriptFormat();
    String resource = script.getCamundaResource();
    String source = script.getTextContent();

    if (language == null) {
      language = ScriptingEngines.DEFAULT_SCRIPTING_LANGUAGE;
    }

    try {
      return ScriptUtil.getScript(language, source, resource, context.getExpressionManager());
    }
    catch (ProcessEngineException e) {
      // ignore
      return null;
    }
  }

  protected List<FieldDeclaration> initializeFieldDeclarations(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context, Collection<CamundaField> fields) {
    List<FieldDeclaration> fieldDeclarations = new ArrayList<FieldDeclaration>();

    for (CamundaField field : fields) {
      FieldDeclaration fieldDeclaration = initializeFieldDeclaration(element, activity, context, field);
      fieldDeclarations.add(fieldDeclaration);
    }

    return fieldDeclarations;
  }

  protected FieldDeclaration initializeFieldDeclaration(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context, CamundaField field) {
    String name = field.getCamundaName();
    String type = Expression.class.getName();

    Object value = getFixedValue(field);

    if (value == null) {
      ExpressionManager expressionManager = context.getExpressionManager();
      value = getExpressionValue(field, expressionManager);
    }

    return new FieldDeclaration(name, type, value);
  }

  protected FixedValue getFixedValue(CamundaField field) {
    CamundaString strg = field.getCamundaString();

    String value = null;
    if (strg != null) {
      value = strg.getTextContent();
    }

    if (value == null) {
      value = field.getCamundaStringValue();
    }

    if (value != null) {
      return new FixedValue(value);
    }

    return null;
  }

  protected Expression getExpressionValue(CamundaField field, ExpressionManager expressionManager) {
    CamundaExpression expression = field.getCamundaExpressionChild();

    String value = null;
    if (expression != null) {
      value = expression.getTextContent();

    }

    if (value == null) {
      value = field.getCamundaExpression();
    }

    if (value != null) {
      return expressionManager.createExpression(value);
    }

    return null;
  }

  protected void initializeEntryCriterias(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    Collection<Sentry> entryCriterias = getEntryCriterias(element);

    if (!entryCriterias.isEmpty()) {
      CmmnActivity parent = activity.getParent();
      if (parent != null) {
        for (Sentry sentry : entryCriterias) {
          String sentryId = sentry.getId();
          CmmnSentryDeclaration sentryDeclaration = parent.getSentry(sentryId);
          if (sentryDeclaration != null) {
            activity.addEntryCriteria(sentryDeclaration);
          }
        }
      }
    }
  }

  protected void initializeExitCriterias(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    Collection<Sentry> exitCriterias = getExitCriterias(element);

    if (!exitCriterias.isEmpty()) {
      CmmnActivity parent = activity.getParent();
      if (parent != null) {
        for (Sentry sentry : exitCriterias) {
          String sentryId = sentry.getId();
          CmmnSentryDeclaration sentryDeclaration = parent.getSentry(sentryId);
          if (sentryDeclaration != null) {
            activity.addExitCriteria(sentryDeclaration);
          }
        }
      }
    }
  }

  protected PlanItemControl getDefaultControl(CmmnElement element) {
    PlanItemDefinition definition = getDefinition(element);

    return definition.getDefaultControl();
  }

  protected <V extends ModelElementInstance> List<V> queryExtensionElementsByClass(CmmnElement element, Class<V> cls) {
    ExtensionElements extensionElements = getExtensionElements(element);

    if (extensionElements != null) {
      Query<ModelElementInstance> query = extensionElements.getElementsQuery();
      return query.filterByType(cls).list();

    } else {
      return new ArrayList<V>();
    }
  }

  protected ExtensionElements getExtensionElements(CmmnElement element) {
    return element.getExtensionElements();
  }

  protected PlanItemControl getItemControl(CmmnElement element) {
    if (isPlanItem(element)) {
      PlanItem planItem = (PlanItem) element;
      return planItem.getItemControl();
    } else
    if (isDiscretionaryItem(element)) {
      DiscretionaryItem discretionaryItem = (DiscretionaryItem) element;
      return discretionaryItem.getItemControl();
    }

    return null;
  }

  protected String getName(CmmnElement element) {
    String name = null;
    if (isPlanItem(element)) {
      PlanItem planItem = (PlanItem) element;
      name = planItem.getName();
    }

    if (name == null || name.isEmpty()) {
      PlanItemDefinition definition = getDefinition(element);
      if (definition != null) {
        name = definition.getName();
      }
    }

    return name;
  }

  protected PlanItemDefinition getDefinition(CmmnElement element) {
    if (isPlanItem(element)) {
      PlanItem planItem = (PlanItem) element;
      return planItem.getDefinition();
    } else
    if (isDiscretionaryItem(element)) {
      DiscretionaryItem discretionaryItem = (DiscretionaryItem) element;
      return discretionaryItem.getDefinition();
    }

    return null;
  }

  protected Collection<Sentry> getEntryCriterias(CmmnElement element) {
    if (isPlanItem(element)) {
      PlanItem planItem = (PlanItem) element;
      return planItem.getEntryCriteria();
    }

    return new ArrayList<Sentry>();
  }

  protected Collection<Sentry> getExitCriterias(CmmnElement element) {
    if (isPlanItem(element)) {
      PlanItem planItem = (PlanItem) element;
      return planItem.getExitCriteria();
    }

    return new ArrayList<Sentry>();
  }

  protected String getDesciption(CmmnElement element) {
    String description = element.getDescription();

    if (description == null) {
      PlanItemDefinition definition = getDefinition(element);
      description = definition.getDescription();
    }

    return description;
  }

  protected String getDocumentation(CmmnElement element) {
    Collection<Documentation> documentations = element.getDocumentations();

    if (documentations.isEmpty()) {
      PlanItemDefinition definition = getDefinition(element);
      documentations = definition.getDocumentations();
    }

    if (documentations.isEmpty()) {
      return null;
    }

    StringBuilder builder = new StringBuilder();
    for (Documentation doc : documentations) {

      String content = doc.getTextContent();
      if (content == null || content.isEmpty()) {
        continue;
      }

      if (builder.length() != 0) {
        builder.append("\n\n");
      }

      builder.append(content.trim());
    }

    return builder.toString();


  }

  protected boolean isPlanItem(CmmnElement element) {
    return element instanceof PlanItem;
  }

  protected boolean isDiscretionaryItem(CmmnElement element) {
    return element instanceof DiscretionaryItem;
  }

  protected abstract List<String> getStandardEvents(CmmnElement element);

}
