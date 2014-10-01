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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.delegate.Expression;
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
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.el.FixedValue;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.impl.scripting.DynamicResourceExecutableScript;
import org.camunda.bpm.engine.impl.scripting.DynamicSourceExecutableScript;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.engine.impl.scripting.engine.JuelScriptEngineFactory;
import org.camunda.bpm.engine.impl.util.ResourceUtil;
import org.camunda.bpm.engine.impl.util.StringUtil;
import org.camunda.bpm.model.cmmn.Query;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.cmmn.instance.DiscretionaryItem;
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
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

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

  public static List<String> TASK_OR_STAGE_EVENTS = Arrays.asList(
      CaseExecutionListener.CREATE,
      CaseExecutionListener.ENABLE,
      CaseExecutionListener.DISABLE,
      CaseExecutionListener.RE_ENABLE,
      CaseExecutionListener.START,
      CaseExecutionListener.MANUAL_START,
      CaseExecutionListener.TERMINATE,
      CaseExecutionListener.EXIT,
      CaseExecutionListener.SUSPEND,
      CaseExecutionListener.PARENT_SUSPEND,
      CaseExecutionListener.RESUME,
      CaseExecutionListener.PARENT_RESUME,
      CaseExecutionListener.COMPLETE
    );

  public static List<String> EVENTLISTENER_OR_MILESTONE_EVENTS = Arrays.asList(
      CaseExecutionListener.CREATE,
      CaseExecutionListener.SUSPEND,
      CaseExecutionListener.RESUME,
      CaseExecutionListener.TERMINATE,
      CaseExecutionListener.PARENT_TERMINATE,
      CaseExecutionListener.OCCUR
    );

  public static List<String> CASE_PLAN_MODEL_EVENTS = Arrays.asList(
      CaseExecutionListener.CREATE,
      CaseExecutionListener.TERMINATE,
      CaseExecutionListener.SUSPEND,
      CaseExecutionListener.COMPLETE,
      CaseExecutionListener.RE_ACTIVATE,
      CaseExecutionListener.CLOSE
    );

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
      name = definition.getName();
    }
    activity.setName(name);

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

    // initialize entry criteria
    initializeEntryCriterias(element, activity, context);

    // initialize exit criteria
    initializeExitCriterias(element, activity, context);

  }

  protected void initializeAutoComplete(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    // noop
  }

  protected void initializeRequiredRule(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    PlanItemControl itemControl = getItemControl(element);
    PlanItemControl defaultControl = getDefaultControl(element);

    ExpressionManager expressionManager = context.getExpressionManager();

    RequiredRule requiredRule = null;
    if (itemControl != null) {
      requiredRule = itemControl.getRequiredRule();
    }
    if (requiredRule == null && defaultControl != null) {
      requiredRule = defaultControl.getRequiredRule();
    }

    if (requiredRule != null) {
      String rule = requiredRule.getCondition().getBody();
      Expression requiredRuleExpression = expressionManager.createExpression(rule);
      CaseControlRule caseRule = new CaseControlRuleImpl(requiredRuleExpression);
      activity.setProperty(PROPERTY_REQUIRED_RULE, caseRule);
    }

  }

  protected void initializeManualActivationRule(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    PlanItemControl itemControl = getItemControl(element);
    PlanItemControl defaultControl = getDefaultControl(element);

    ExpressionManager expressionManager = context.getExpressionManager();

    ManualActivationRule manualActivationRule = null;
    if (itemControl != null) {
      manualActivationRule = itemControl.getManualActivationRule();
    }
    if (manualActivationRule == null && defaultControl != null) {
      manualActivationRule = defaultControl.getManualActivationRule();
    }

    if (manualActivationRule != null) {
      String rule = manualActivationRule.getCondition().getBody();
      Expression manualActivationExpression = expressionManager.createExpression(rule);
      CaseControlRule caseRule = new CaseControlRuleImpl(manualActivationExpression);
      activity.setProperty(PROPERTY_MANUAL_ACTIVATION_RULE, caseRule);
    }

  }

  protected void initializeRepetitionRule(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    PlanItemControl itemControl = getItemControl(element);
    PlanItemControl defaultControl = getDefaultControl(element);

    ExpressionManager expressionManager = context.getExpressionManager();

    RepetitionRule repetitionRule = null;
    if (itemControl != null) {
      repetitionRule = itemControl.getRepetitionRule();
    }
    if (repetitionRule == null && defaultControl != null) {
      repetitionRule = defaultControl.getRepetitionRule();
    }

    if (repetitionRule != null) {
      String rule = repetitionRule.getCondition().getBody();
      Expression repetitionRuleExpression = expressionManager.createExpression(rule);
      CaseControlRule caseRule = new CaseControlRuleImpl(repetitionRuleExpression);
      activity.setProperty(PROPERTY_REPETITION_RULE, caseRule);
    }

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

  protected ExecutableScript initializeScript(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context, CamundaScript script) {
    String language = script.getCamundaScriptFormat();
    String resource = script.getCamundaResource();
    String source = script.getTextContent();

    return initializeScriptDefinition(language, resource, source, context);
  }

  public ExecutableScript initializeScriptDefinition(String language, String resource, String source, CmmnHandlerContext context) {
    if (language != null) {
      if (resource != null && !resource.isEmpty()) {
        return parseScriptResource(resource, language, context);
      }
      else if(source != null) {
        return parseScriptSource(source, language, context);
      }
    }
    return null;
  }

  protected ExecutableScript parseScriptSource(String source, String language, CmmnHandlerContext context) {
    if (StringUtil.isExpression(source) && !JuelScriptEngineFactory.names.contains(language)) {
      ExpressionManager expressionManager = context.getExpressionManager();
      Expression scriptExpression = expressionManager.createExpression(source.trim());
      return new DynamicSourceExecutableScript(scriptExpression, language);
    }
    else {
      return parseScript(source, language);
    }
  }

  protected ExecutableScript parseScriptResource(String resource, String language, CmmnHandlerContext context) {
    if (StringUtil.isExpression(resource)) {
      ExpressionManager expressionManager = context.getExpressionManager();
      Expression scriptResourceExpression = expressionManager.createExpression(resource);
      return new DynamicResourceExecutableScript(scriptResourceExpression, language);
    }
    else {
      DeploymentEntity deployment = (DeploymentEntity) context.getDeployment();
      String scriptSource = ResourceUtil.loadResourceContent(resource, deployment);
      return parseScript(scriptSource, language);
    }
  }

  protected ExecutableScript parseScript(String script, String language) {
    return Context.getProcessEngineConfiguration()
      .getScriptFactory()
      .createScript(script, language);
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
          activity.addEntryCriteria(sentryDeclaration);
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
          activity.addExitCriteria(sentryDeclaration);
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
      return planItem.getEntryCriterias();
    }

    return new ArrayList<Sentry>();
  }

  protected Collection<Sentry> getExitCriterias(CmmnElement element) {
    if (isPlanItem(element)) {
      PlanItem planItem = (PlanItem) element;
      return planItem.getExitCriterias();
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

  protected boolean isPlanItem(CmmnElement element) {
    return element instanceof PlanItem;
  }

  protected boolean isDiscretionaryItem(CmmnElement element) {
    return element instanceof DiscretionaryItem;
  }

  protected abstract List<String> getStandardEvents(CmmnElement element);

}
