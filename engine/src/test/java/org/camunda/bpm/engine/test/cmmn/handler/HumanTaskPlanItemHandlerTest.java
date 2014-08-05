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
package org.camunda.bpm.engine.test.cmmn.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.delegate.BaseDelegateExecution;
import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.delegate.DelegateListener;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.parser.FieldDeclaration;
import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.HumanTaskActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.handler.HumanTaskItemHandler;
import org.camunda.bpm.engine.impl.cmmn.listener.ClassDelegateCaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.listener.DelegateExpressionCaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.listener.ExpressionCaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.listener.ScriptCaseExecutionListener;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.task.listener.ClassDelegateTaskListener;
import org.camunda.bpm.engine.impl.task.listener.DelegateExpressionTaskListener;
import org.camunda.bpm.engine.impl.task.listener.ExpressionTaskListener;
import org.camunda.bpm.model.cmmn.impl.instance.CaseRoles;
import org.camunda.bpm.model.cmmn.instance.ExtensionElements;
import org.camunda.bpm.model.cmmn.instance.HumanTask;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaCaseExecutionListener;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaExpression;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaField;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaScript;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaString;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaTaskListener;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Roman Smirnov
 *
 */
public class HumanTaskPlanItemHandlerTest extends CmmnElementHandlerTest {

  protected HumanTask humanTask;
  protected PlanItem planItem;
  protected HumanTaskItemHandler handler = new HumanTaskItemHandler();

  @Before
  public void setUp() {
    humanTask = createElement(casePlanModel, "aHumanTask", HumanTask.class);

    planItem = createElement(casePlanModel, "PI_aHumanTask", PlanItem.class);
    planItem.setDefinition(humanTask);

  }

  @Test
  public void testHumanTaskActivityName() {
    // given:
    // the humanTask has a name "A HumanTask"
    String name = "A HumanTask";
    humanTask.setName(name);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(name, activity.getName());
  }

  @Test
  public void testPlanItemActivityName() {
    // given:
    // the humanTask has a name "A HumanTask"
    String humanTaskName = "A HumanTask";
    humanTask.setName(humanTaskName);

    // the planItem has an own name "My LocalName"
    String planItemName = "My LocalName";
    planItem.setName(planItemName);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertNotEquals(humanTaskName, activity.getName());
    assertEquals(planItemName, activity.getName());
  }

  @Test
  public void testActivityBehavior() {
    // given: a planItem

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    CmmnActivityBehavior behavior = activity.getActivityBehavior();
    assertTrue(behavior instanceof HumanTaskActivityBehavior);
  }

  @Test
  public void testIsBlockingEqualsTrueProperty() {
    // given: a humanTask with isBlocking = true (defaultValue)

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    Boolean isBlocking = (Boolean) activity.getProperty("isBlocking");
    assertTrue(isBlocking);
  }

  @Test
  public void testIsBlockingEqualsFalseProperty() {
    // given:
    // a humanTask with isBlocking = false
    humanTask.setIsBlocking(false);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    // According to the specification:
    // When a HumanTask is not 'blocking'
    // (isBlocking is 'false'), it can be
    // considered a 'manual' Task, i.e.,
    // the Case management system is not
    // tracking the lifecycle of the HumanTask (instance).
    assertNull(activity);
  }

  @Test
  public void testWithoutParent() {
    // given: a planItem

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertNull(activity.getParent());
  }

  @Test
  public void testWithParent() {
    // given:
    // a new activity as parent
    CmmnCaseDefinition parent = new CmmnCaseDefinition("aParentActivity");
    context.setParent(parent);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(parent, activity.getParent());
    assertTrue(parent.getActivities().contains(activity));
  }

  @Test
  public void testTaskDecorator() {
    // given: a plan item

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    // there exists a taskDecorator
    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();

    assertNotNull(behavior.getTaskDecorator());
  }

  @Test
  public void testTaskDefinition() {
    // given: a plan item

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    // there exists a taskDefinition
    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();

    assertNotNull(behavior.getTaskDefinition());
  }

  @Test
  public void testExpressionManager() {
    // given: a plan item

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();

    ExpressionManager expressionManager = behavior.getExpressionManager();
    assertNotNull(expressionManager);
    assertEquals(context.getExpressionManager(), expressionManager);
  }

  @Test
  public void testTaskDefinitionHumanTaskNameExpression() {
    // given
    String name = "A HumanTask";
    humanTask.setName(name);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();

    Expression nameExpression = behavior.getTaskDefinition().getNameExpression();
    assertNotNull(nameExpression);
    assertEquals("A HumanTask", nameExpression.getExpressionText());
  }

  @Test
  public void testTaskDefinitionPlanItemNameExpression() {
    // given
    String name = "A HumanTask";
    humanTask.setName(name);

    String planItemName = "My LocalName";
    planItem.setName(planItemName);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    Expression nameExpression = taskDefinition.getNameExpression();
    assertNotNull(nameExpression);
    assertEquals("My LocalName", nameExpression.getExpressionText());
  }

  @Test
  public void testTaskDefinitionDueDateExpression() {
    // given
    String aDueDate = "aDueDate";
    humanTask.setCamundaDueDate(aDueDate);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    Expression dueDateExpression = taskDefinition.getDueDateExpression();
    assertNotNull(dueDateExpression);
    assertEquals(aDueDate, dueDateExpression.getExpressionText());
  }

  @Test
  public void testTaskDefinitionPriorityExpression() {
    // given
    String aPriority = "aPriority";
    humanTask.setCamundaPriority(aPriority);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    Expression priorityExpression = taskDefinition.getPriorityExpression();
    assertNotNull(priorityExpression);
    assertEquals(aPriority, priorityExpression.getExpressionText());
  }

  @Test
  public void testTaskDefinitionPeformerExpression() {
    // given
    CaseRoles role = createElement(caseDefinition, "aRole", CaseRoles.class);
    role.setName("aPerformerRole");

    humanTask.setPerformer(role);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    Expression assigneeExpression = taskDefinition.getAssigneeExpression();
    assertNotNull(assigneeExpression);
    assertEquals("aPerformerRole", assigneeExpression.getExpressionText());
  }

  @Test
  public void testTaskDefinitionAssigneeExpression() {
    // given
    String aPriority = "aPriority";
    humanTask.setCamundaPriority(aPriority);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    Expression priorityExpression = taskDefinition.getPriorityExpression();
    assertNotNull(priorityExpression);
    assertEquals(aPriority, priorityExpression.getExpressionText());
  }

  @Test
  public void testTaskDefinitionCandidateUsers() {
    // given
    String aCandidateUsers = "mary,john,peter";
    humanTask.setCamundaCandidateUsers(aCandidateUsers);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    Set<Expression> candidateUserExpressions = taskDefinition.getCandidateUserIdExpressions();
    assertEquals(3, candidateUserExpressions.size());

    for (Expression candidateUserExpression : candidateUserExpressions) {
      String candidateUser = candidateUserExpression.getExpressionText();
      if ("mary".equals(candidateUser)) {
        assertEquals("mary", candidateUser);
      } else if ("john".equals(candidateUser)) {
        assertEquals("john", candidateUser);
      } else if ("peter".equals(candidateUser)) {
        assertEquals("peter", candidateUser);
      } else {
        fail("Unexpected candidate user: " + candidateUser);
      }
    }
  }

  @Test
  public void testTaskDefinitionCandidateGroups() {
    // given
    String aCandidateGroups = "accounting,management,backoffice";
    humanTask.setCamundaCandidateGroups(aCandidateGroups);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    Set<Expression> candidateGroupExpressions = taskDefinition.getCandidateGroupIdExpressions();
    assertEquals(3, candidateGroupExpressions.size());

    for (Expression candidateGroupExpression : candidateGroupExpressions) {
      String candidateGroup = candidateGroupExpression.getExpressionText();
      if ("accounting".equals(candidateGroup)) {
        assertEquals("accounting", candidateGroup);
      } else if ("management".equals(candidateGroup)) {
        assertEquals("management", candidateGroup);
      } else if ("backoffice".equals(candidateGroup)) {
        assertEquals("backoffice", candidateGroup);
      } else {
        fail("Unexpected candidate group: " + candidateGroup);
      }
    }
  }

  @Test
  public void testTaskDefinitionFormKey() {
    // given
    String aFormKey = "aFormKey";
    humanTask.setCamundaFormKey(aFormKey);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    Expression formKeyExpression = taskDefinition.getFormKey();
    assertNotNull(formKeyExpression);
    assertEquals(aFormKey, formKeyExpression.getExpressionText());
  }

  @Test
  public void testHumanTaskDescription() {
    // given
    String description = "A description";
    humanTask.setDescription(description);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    Expression descriptionExpression = taskDefinition.getDescriptionExpression();
    assertNotNull(descriptionExpression);
    assertEquals(description, descriptionExpression.getExpressionText());
  }

  @Test
  public void testPlanItemDescription() {
    // given
    String description = "A description";
    humanTask.setDescription(description);

    // the planItem has an own description
    String localDescription = "My Local Description";
    planItem.setDescription(localDescription);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    Expression descriptionExpression = taskDefinition.getDescriptionExpression();
    assertNotNull(descriptionExpression);
    assertEquals(localDescription, descriptionExpression.getExpressionText());
  }

  @Test
  public void testCreateCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testCreateCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testCreateCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testCreateCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testEnableCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.ENABLE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testEnableCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.ENABLE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testEnableCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.ENABLE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testEnableCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.ENABLE;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testDisableCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.DISABLE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testDisableCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.DISABLE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testDisableCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.DISABLE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testDisableCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.DISABLE;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testReEnableCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.RE_ENABLE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testReEnableCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.RE_ENABLE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testReEnableCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.RE_ENABLE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testReEnableCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.RE_ENABLE;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testStartCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.START;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testStartCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.START;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testStartCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.START;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testStartCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.START;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testManualStartCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.MANUAL_START;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testManualStartCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.MANUAL_START;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testManualStartCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.MANUAL_START;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testManualStartCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.MANUAL_START;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testCompleteCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.COMPLETE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testCompleteCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.COMPLETE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testCompleteCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.COMPLETE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testCompleteCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.COMPLETE;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testTerminateCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.TERMINATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testTerminateCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.TERMINATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testTerminateCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.TERMINATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testTerminateCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.TERMINATE;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testExitCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.EXIT;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testExitCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.EXIT;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testExitCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.EXIT;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testExitCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.EXIT;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testSuspendCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.SUSPEND;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testSuspendCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.SUSPEND;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testSuspendCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.SUSPEND;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testSuspendCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.SUSPEND;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testParentSuspendCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.PARENT_SUSPEND;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testParentSuspendCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.PARENT_SUSPEND;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testParentSuspendCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.PARENT_SUSPEND;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testParentSuspendCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.PARENT_SUSPEND;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testResumeCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.RESUME;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testResumeCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.RESUME;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testResumeCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.RESUME;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testResumeCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.RESUME;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testParentResumeCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.PARENT_RESUME;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testParentResumeCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.PARENT_RESUME;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testParentResumeCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    String event = CaseExecutionListener.PARENT_RESUME;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ExpressionCaseExecutionListener);

    ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testParentResumeCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String event = CaseExecutionListener.PARENT_RESUME;
    caseExecutionListener.setCamundaEvent(event);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(1, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateListener<? extends BaseDelegateExecution> listener = createListeners.get(0);
    assertTrue(listener instanceof ScriptCaseExecutionListener);

    ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
    ExecutableScript executableScript = scriptListener.getScript();
    assertNotNull(executableScript);
    assertEquals(scriptFormat, executableScript.getLanguage());

  }

  @Test
  public void testAllCaseExecutionListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    caseExecutionListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(13, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> listeners = new ArrayList<DelegateListener<? extends BaseDelegateExecution>>();

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(CaseExecutionListener.CREATE);
    assertEquals(1, createListeners.size());
    listeners.add(createListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> enableListeners = activity.getListeners(CaseExecutionListener.ENABLE);
    assertEquals(1, enableListeners.size());
    listeners.add(enableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> disableListeners = activity.getListeners(CaseExecutionListener.DISABLE);
    assertEquals(1, disableListeners.size());
    listeners.add(disableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> reEnableListeners = activity.getListeners(CaseExecutionListener.RE_ENABLE);
    assertEquals(1, reEnableListeners.size());
    listeners.add(reEnableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> startListeners = activity.getListeners(CaseExecutionListener.START);
    assertEquals(1, startListeners.size());
    listeners.add(startListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> manualStartListeners = activity.getListeners(CaseExecutionListener.MANUAL_START);
    assertEquals(1, manualStartListeners.size());
    listeners.add(manualStartListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> completeListeners = activity.getListeners(CaseExecutionListener.COMPLETE);
    assertEquals(1, completeListeners.size());
    listeners.add(completeListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> terminateListeners = activity.getListeners(CaseExecutionListener.TERMINATE);
    assertEquals(1, terminateListeners.size());
    listeners.add(terminateListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> exitListeners = activity.getListeners(CaseExecutionListener.EXIT);
    assertEquals(1, exitListeners.size());
    listeners.add(exitListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> suspendListeners = activity.getListeners(CaseExecutionListener.SUSPEND);
    assertEquals(1, suspendListeners.size());
    listeners.add(suspendListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> parentSuspendListeners = activity.getListeners(CaseExecutionListener.PARENT_SUSPEND);
    assertEquals(1, parentSuspendListeners.size());
    listeners.add(parentSuspendListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> resumeListeners = activity.getListeners(CaseExecutionListener.RESUME);
    assertEquals(1, resumeListeners.size());
    listeners.add(resumeListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> parentResumeListeners = activity.getListeners(CaseExecutionListener.PARENT_RESUME);
    assertEquals(1, parentResumeListeners.size());
    listeners.add(parentResumeListeners.get(0));


    for (DelegateListener<? extends BaseDelegateExecution> listener : listeners) {
      assertTrue(listener instanceof ClassDelegateCaseExecutionListener);

      ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) listener;
      assertEquals(className, classDelegateListener.getClassName());
      assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());
    }

  }

  @Test
  public void testAllCaseExecutionListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(13, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> listeners = new ArrayList<DelegateListener<? extends BaseDelegateExecution>>();

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(CaseExecutionListener.CREATE);
    assertEquals(1, createListeners.size());
    listeners.add(createListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> enableListeners = activity.getListeners(CaseExecutionListener.ENABLE);
    assertEquals(1, enableListeners.size());
    listeners.add(enableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> disableListeners = activity.getListeners(CaseExecutionListener.DISABLE);
    assertEquals(1, disableListeners.size());
    listeners.add(disableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> reEnableListeners = activity.getListeners(CaseExecutionListener.RE_ENABLE);
    assertEquals(1, reEnableListeners.size());
    listeners.add(reEnableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> startListeners = activity.getListeners(CaseExecutionListener.START);
    assertEquals(1, startListeners.size());
    listeners.add(startListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> manualStartListeners = activity.getListeners(CaseExecutionListener.MANUAL_START);
    assertEquals(1, manualStartListeners.size());
    listeners.add(manualStartListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> completeListeners = activity.getListeners(CaseExecutionListener.COMPLETE);
    assertEquals(1, completeListeners.size());
    listeners.add(completeListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> terminateListeners = activity.getListeners(CaseExecutionListener.TERMINATE);
    assertEquals(1, terminateListeners.size());
    listeners.add(terminateListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> exitListeners = activity.getListeners(CaseExecutionListener.EXIT);
    assertEquals(1, exitListeners.size());
    listeners.add(exitListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> suspendListeners = activity.getListeners(CaseExecutionListener.SUSPEND);
    assertEquals(1, suspendListeners.size());
    listeners.add(suspendListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> parentSuspendListeners = activity.getListeners(CaseExecutionListener.PARENT_SUSPEND);
    assertEquals(1, parentSuspendListeners.size());
    listeners.add(parentSuspendListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> resumeListeners = activity.getListeners(CaseExecutionListener.RESUME);
    assertEquals(1, resumeListeners.size());
    listeners.add(resumeListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> parentResumeListeners = activity.getListeners(CaseExecutionListener.PARENT_RESUME);
    assertEquals(1, parentResumeListeners.size());
    listeners.add(parentResumeListeners.get(0));

    for (DelegateListener<? extends BaseDelegateExecution> listener : listeners) {
      assertTrue(listener instanceof DelegateExpressionCaseExecutionListener);

      DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) listener;
      assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
      assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());
    }

  }

  @Test
  public void testAllCaseExecutionListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String expression = "${myExpression}";
    caseExecutionListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(13, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> listeners = new ArrayList<DelegateListener<? extends BaseDelegateExecution>>();

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(CaseExecutionListener.CREATE);
    assertEquals(1, createListeners.size());
    listeners.add(createListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> enableListeners = activity.getListeners(CaseExecutionListener.ENABLE);
    assertEquals(1, enableListeners.size());
    listeners.add(enableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> disableListeners = activity.getListeners(CaseExecutionListener.DISABLE);
    assertEquals(1, disableListeners.size());
    listeners.add(disableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> reEnableListeners = activity.getListeners(CaseExecutionListener.RE_ENABLE);
    assertEquals(1, reEnableListeners.size());
    listeners.add(reEnableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> startListeners = activity.getListeners(CaseExecutionListener.START);
    assertEquals(1, startListeners.size());
    listeners.add(startListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> manualStartListeners = activity.getListeners(CaseExecutionListener.MANUAL_START);
    assertEquals(1, manualStartListeners.size());
    listeners.add(manualStartListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> completeListeners = activity.getListeners(CaseExecutionListener.COMPLETE);
    assertEquals(1, completeListeners.size());
    listeners.add(completeListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> terminateListeners = activity.getListeners(CaseExecutionListener.TERMINATE);
    assertEquals(1, terminateListeners.size());
    listeners.add(terminateListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> exitListeners = activity.getListeners(CaseExecutionListener.EXIT);
    assertEquals(1, exitListeners.size());
    listeners.add(exitListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> suspendListeners = activity.getListeners(CaseExecutionListener.SUSPEND);
    assertEquals(1, suspendListeners.size());
    listeners.add(suspendListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> parentSuspendListeners = activity.getListeners(CaseExecutionListener.PARENT_SUSPEND);
    assertEquals(1, parentSuspendListeners.size());
    listeners.add(parentSuspendListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> resumeListeners = activity.getListeners(CaseExecutionListener.RESUME);
    assertEquals(1, resumeListeners.size());
    listeners.add(resumeListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> parentResumeListeners = activity.getListeners(CaseExecutionListener.PARENT_RESUME);
    assertEquals(1, parentResumeListeners.size());
    listeners.add(parentResumeListeners.get(0));

    for (DelegateListener<? extends BaseDelegateExecution> listener : listeners) {
      assertTrue(listener instanceof ExpressionCaseExecutionListener);

      ExpressionCaseExecutionListener expressionListener = (ExpressionCaseExecutionListener) listener;
      assertEquals(expression, expressionListener.getExpressionText());
    }

  }

  @Test
  public void testAllCaseExecutionListenerByScript() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    CamundaScript script = createElement(caseExecutionListener, null, CamundaScript.class);
    String scriptFormat = "aScriptFormat";
    String scriptValue = "${myScript}";
    script.setCamundaScriptFormat(scriptFormat);
    script.setTextContent(scriptValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(13, activity.getListeners().size());

    List<DelegateListener<? extends BaseDelegateExecution>> listeners = new ArrayList<DelegateListener<? extends BaseDelegateExecution>>();

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(CaseExecutionListener.CREATE);
    assertEquals(1, createListeners.size());
    listeners.add(createListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> enableListeners = activity.getListeners(CaseExecutionListener.ENABLE);
    assertEquals(1, enableListeners.size());
    listeners.add(enableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> disableListeners = activity.getListeners(CaseExecutionListener.DISABLE);
    assertEquals(1, disableListeners.size());
    listeners.add(disableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> reEnableListeners = activity.getListeners(CaseExecutionListener.RE_ENABLE);
    assertEquals(1, reEnableListeners.size());
    listeners.add(reEnableListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> startListeners = activity.getListeners(CaseExecutionListener.START);
    assertEquals(1, startListeners.size());
    listeners.add(startListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> manualStartListeners = activity.getListeners(CaseExecutionListener.MANUAL_START);
    assertEquals(1, manualStartListeners.size());
    listeners.add(manualStartListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> completeListeners = activity.getListeners(CaseExecutionListener.COMPLETE);
    assertEquals(1, completeListeners.size());
    listeners.add(completeListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> terminateListeners = activity.getListeners(CaseExecutionListener.TERMINATE);
    assertEquals(1, terminateListeners.size());
    listeners.add(terminateListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> exitListeners = activity.getListeners(CaseExecutionListener.EXIT);
    assertEquals(1, exitListeners.size());
    listeners.add(exitListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> suspendListeners = activity.getListeners(CaseExecutionListener.SUSPEND);
    assertEquals(1, suspendListeners.size());
    listeners.add(suspendListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> parentSuspendListeners = activity.getListeners(CaseExecutionListener.PARENT_SUSPEND);
    assertEquals(1, parentSuspendListeners.size());
    listeners.add(parentSuspendListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> resumeListeners = activity.getListeners(CaseExecutionListener.RESUME);
    assertEquals(1, resumeListeners.size());
    listeners.add(resumeListeners.get(0));

    List<DelegateListener<? extends BaseDelegateExecution>> parentResumeListeners = activity.getListeners(CaseExecutionListener.PARENT_RESUME);
    assertEquals(1, parentResumeListeners.size());
    listeners.add(parentResumeListeners.get(0));

    for (DelegateListener<? extends BaseDelegateExecution> listener : listeners) {
      assertTrue(listener instanceof ScriptCaseExecutionListener);

      ScriptCaseExecutionListener scriptListener = (ScriptCaseExecutionListener) listener;
      ExecutableScript executableScript = scriptListener.getScript();
      assertNotNull(executableScript);
      assertEquals(scriptFormat, executableScript.getLanguage());
    }

  }

  @Test
  public void testFieldInjectionExpressionOnClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    CamundaField firstField = createElement(caseExecutionListener, null, CamundaField.class);

    String firstFieldName = "firstField";
    String firstFieldExpression = "${myFirstExpression}";

    firstField.setCamundaName(firstFieldName);
    firstField.setCamundaExpression(firstFieldExpression);

    CamundaField secondField = createElement(caseExecutionListener, null, CamundaField.class);

    String secondFieldName = "secondField";
    String secondFieldExpression = "${mySecondExpression}";

    secondField.setCamundaName(secondFieldName);
    secondField.setCamundaExpression(secondFieldExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) createListeners.get(0);

    assertEquals(className, classDelegateListener.getClassName());

    List<FieldDeclaration> fieldDeclarations = classDelegateListener.getFieldDeclarations();
    assertEquals(2, fieldDeclarations.size());

    FieldDeclaration firstFieldDeclaration = fieldDeclarations.get(0);
    assertEquals(firstFieldName, firstFieldDeclaration.getName());

    Object firstFieldValue = firstFieldDeclaration.getValue();
    assertNotNull(firstFieldValue);
    assertTrue(firstFieldValue instanceof Expression);
    Expression expressionValue = (Expression) firstFieldValue;
    assertEquals(firstFieldExpression, expressionValue.getExpressionText());

    FieldDeclaration secondFieldDeclaration = fieldDeclarations.get(1);
    assertEquals(secondFieldName, secondFieldDeclaration.getName());

    Object secondFieldValue = secondFieldDeclaration.getValue();
    assertNotNull(secondFieldValue);
    assertTrue(secondFieldValue instanceof Expression);
    expressionValue = (Expression) secondFieldValue;
    assertEquals(secondFieldExpression, expressionValue.getExpressionText());

  }

  @Test
  public void testFieldInjectionExpressionChildOnClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    CamundaField firstField = createElement(caseExecutionListener, null, CamundaField.class);

    String firstFieldName = "firstField";
    firstField.setCamundaName(firstFieldName);

    CamundaExpression firstFieldExpressionChild = createElement(firstField, null, CamundaExpression.class);

    String firstFieldExpression = "${myFirstExpression}";
    firstFieldExpressionChild.setTextContent(firstFieldExpression);

    CamundaField secondField = createElement(caseExecutionListener, null, CamundaField.class);

    String secondFieldName = "secondField";
    secondField.setCamundaName(secondFieldName);

    CamundaExpression secondFieldExpressionChild = createElement(secondField, null, CamundaExpression.class);

    String secondFieldExpression = "${mySecondExpression}";
    secondFieldExpressionChild.setTextContent(secondFieldExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) createListeners.get(0);

    assertEquals(className, classDelegateListener.getClassName());

    List<FieldDeclaration> fieldDeclarations = classDelegateListener.getFieldDeclarations();
    assertEquals(2, fieldDeclarations.size());

    FieldDeclaration firstFieldDeclaration = fieldDeclarations.get(0);
    assertEquals(firstFieldName, firstFieldDeclaration.getName());

    Object firstFieldValue = firstFieldDeclaration.getValue();
    assertNotNull(firstFieldValue);
    assertTrue(firstFieldValue instanceof Expression);
    Expression expressionValue = (Expression) firstFieldValue;
    assertEquals(firstFieldExpression, expressionValue.getExpressionText());

    FieldDeclaration secondFieldDeclaration = fieldDeclarations.get(1);
    assertEquals(secondFieldName, secondFieldDeclaration.getName());

    Object secondFieldValue = secondFieldDeclaration.getValue();
    assertNotNull(secondFieldValue);
    assertTrue(secondFieldValue instanceof Expression);
    expressionValue = (Expression) secondFieldValue;
    assertEquals(secondFieldExpression, expressionValue.getExpressionText());

  }

  @Test
  public void testFieldInjectionStringOnClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    CamundaField firstField = createElement(caseExecutionListener, null, CamundaField.class);

    String firstFieldName = "firstField";
    firstField.setCamundaName(firstFieldName);

    CamundaString firstFieldStringChild = createElement(firstField, null, CamundaString.class);

    String firstFieldString = "aFirstFixedValue";
    firstFieldStringChild.setTextContent(firstFieldString);

    CamundaField secondField = createElement(caseExecutionListener, null, CamundaField.class);

    String secondFieldName = "secondField";
    secondField.setCamundaName(secondFieldName);

    CamundaString secondFieldStringChild = createElement(secondField, null, CamundaString.class);

    String secondFieldString = "aSecondFixedValue";
    secondFieldStringChild.setTextContent(secondFieldString);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) createListeners.get(0);

    assertEquals(className, classDelegateListener.getClassName());

    List<FieldDeclaration> fieldDeclarations = classDelegateListener.getFieldDeclarations();
    assertEquals(2, fieldDeclarations.size());

    FieldDeclaration firstFieldDeclaration = fieldDeclarations.get(0);
    assertEquals(firstFieldName, firstFieldDeclaration.getName());

    Object firstFieldValue = firstFieldDeclaration.getValue();
    assertNotNull(firstFieldValue);
    assertTrue(firstFieldValue instanceof Expression);
    Expression expressionValue = (Expression) firstFieldValue;
    assertEquals(firstFieldString, expressionValue.getExpressionText());

    FieldDeclaration secondFieldDeclaration = fieldDeclarations.get(1);
    assertEquals(secondFieldName, secondFieldDeclaration.getName());

    Object secondFieldValue = secondFieldDeclaration.getValue();
    assertNotNull(secondFieldValue);
    assertTrue(secondFieldValue instanceof Expression);
    expressionValue = (Expression) secondFieldValue;
    assertEquals(secondFieldString, expressionValue.getExpressionText());

  }

  @Test
  public void testFieldInjectionStringValueOnClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String className = "org.camunda.bpm.test.caseexecutionlistener.ABC";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaClass(className);

    CamundaField firstField = createElement(caseExecutionListener, null, CamundaField.class);

    String firstFieldName = "firstField";
    String firstFieldStringValue = "aFirstFixedValue";
    firstField.setCamundaName(firstFieldName);
    firstField.setCamundaStringValue(firstFieldStringValue);

    CamundaField secondField = createElement(caseExecutionListener, null, CamundaField.class);

    String secondFieldName = "secondField";
    String secondFieldStringValue = "aSecondFixedValue";
    secondField.setCamundaName(secondFieldName);
    secondField.setCamundaStringValue(secondFieldStringValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    ClassDelegateCaseExecutionListener classDelegateListener = (ClassDelegateCaseExecutionListener) createListeners.get(0);

    assertEquals(className, classDelegateListener.getClassName());

    List<FieldDeclaration> fieldDeclarations = classDelegateListener.getFieldDeclarations();
    assertEquals(2, fieldDeclarations.size());

    FieldDeclaration firstFieldDeclaration = fieldDeclarations.get(0);
    assertEquals(firstFieldName, firstFieldDeclaration.getName());

    Object firstFieldValue = firstFieldDeclaration.getValue();
    assertNotNull(firstFieldValue);
    assertTrue(firstFieldValue instanceof Expression);
    Expression expressionValue = (Expression) firstFieldValue;
    assertEquals(firstFieldStringValue, expressionValue.getExpressionText());

    FieldDeclaration secondFieldDeclaration = fieldDeclarations.get(1);
    assertEquals(secondFieldName, secondFieldDeclaration.getName());

    Object secondFieldValue = secondFieldDeclaration.getValue();
    assertNotNull(secondFieldValue);
    assertTrue(secondFieldValue instanceof Expression);
    expressionValue = (Expression) secondFieldValue;
    assertEquals(secondFieldStringValue, expressionValue.getExpressionText());

  }

  @Test
  public void testFieldInjectionExpressionOnDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    CamundaField firstField = createElement(caseExecutionListener, null, CamundaField.class);

    String firstFieldName = "firstField";
    String firstFieldExpression = "${myFirstExpression}";

    firstField.setCamundaName(firstFieldName);
    firstField.setCamundaExpression(firstFieldExpression);

    CamundaField secondField = createElement(caseExecutionListener, null, CamundaField.class);

    String secondFieldName = "secondField";
    String secondFieldExpression = "${mySecondExpression}";

    secondField.setCamundaName(secondFieldName);
    secondField.setCamundaExpression(secondFieldExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateExpressionCaseExecutionListener delegateExpressionListener = (DelegateExpressionCaseExecutionListener) createListeners.get(0);

    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());

    List<FieldDeclaration> fieldDeclarations = delegateExpressionListener.getFieldDeclarations();
    assertEquals(2, fieldDeclarations.size());

    FieldDeclaration firstFieldDeclaration = fieldDeclarations.get(0);
    assertEquals(firstFieldName, firstFieldDeclaration.getName());

    Object firstFieldValue = firstFieldDeclaration.getValue();
    assertNotNull(firstFieldValue);
    assertTrue(firstFieldValue instanceof Expression);
    Expression expressionValue = (Expression) firstFieldValue;
    assertEquals(firstFieldExpression, expressionValue.getExpressionText());

    FieldDeclaration secondFieldDeclaration = fieldDeclarations.get(1);
    assertEquals(secondFieldName, secondFieldDeclaration.getName());

    Object secondFieldValue = secondFieldDeclaration.getValue();
    assertNotNull(secondFieldValue);
    assertTrue(secondFieldValue instanceof Expression);
    expressionValue = (Expression) secondFieldValue;
    assertEquals(secondFieldExpression, expressionValue.getExpressionText());

  }

  @Test
  public void testFieldInjectionExpressionChildOnDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    CamundaField firstField = createElement(caseExecutionListener, null, CamundaField.class);

    String firstFieldName = "firstField";
    firstField.setCamundaName(firstFieldName);

    CamundaExpression firstFieldExpressionChild = createElement(firstField, null, CamundaExpression.class);

    String firstFieldExpression = "${myFirstExpression}";
    firstFieldExpressionChild.setTextContent(firstFieldExpression);

    CamundaField secondField = createElement(caseExecutionListener, null, CamundaField.class);

    String secondFieldName = "secondField";
    secondField.setCamundaName(secondFieldName);

    CamundaExpression secondFieldExpressionChild = createElement(secondField, null, CamundaExpression.class);

    String secondFieldExpression = "${mySecondExpression}";
    secondFieldExpressionChild.setTextContent(secondFieldExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateExpressionCaseExecutionListener classDelegateListener = (DelegateExpressionCaseExecutionListener) createListeners.get(0);

    assertEquals(delegateExpression, classDelegateListener.getExpressionText());

    List<FieldDeclaration> fieldDeclarations = classDelegateListener.getFieldDeclarations();
    assertEquals(2, fieldDeclarations.size());

    FieldDeclaration firstFieldDeclaration = fieldDeclarations.get(0);
    assertEquals(firstFieldName, firstFieldDeclaration.getName());

    Object firstFieldValue = firstFieldDeclaration.getValue();
    assertNotNull(firstFieldValue);
    assertTrue(firstFieldValue instanceof Expression);
    Expression expressionValue = (Expression) firstFieldValue;
    assertEquals(firstFieldExpression, expressionValue.getExpressionText());

    FieldDeclaration secondFieldDeclaration = fieldDeclarations.get(1);
    assertEquals(secondFieldName, secondFieldDeclaration.getName());

    Object secondFieldValue = secondFieldDeclaration.getValue();
    assertNotNull(secondFieldValue);
    assertTrue(secondFieldValue instanceof Expression);
    expressionValue = (Expression) secondFieldValue;
    assertEquals(secondFieldExpression, expressionValue.getExpressionText());

  }

  @Test
  public void testFieldInjectionStringOnDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    CamundaField firstField = createElement(caseExecutionListener, null, CamundaField.class);

    String firstFieldName = "firstField";
    firstField.setCamundaName(firstFieldName);

    CamundaString firstFieldStringChild = createElement(firstField, null, CamundaString.class);

    String firstFieldString = "aFirstFixedValue";
    firstFieldStringChild.setTextContent(firstFieldString);

    CamundaField secondField = createElement(caseExecutionListener, null, CamundaField.class);

    String secondFieldName = "secondField";
    secondField.setCamundaName(secondFieldName);

    CamundaString secondFieldStringChild = createElement(secondField, null, CamundaString.class);

    String secondFieldString = "aSecondFixedValue";
    secondFieldStringChild.setTextContent(secondFieldString);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateExpressionCaseExecutionListener classDelegateListener = (DelegateExpressionCaseExecutionListener) createListeners.get(0);

    assertEquals(delegateExpression, classDelegateListener.getExpressionText());

    List<FieldDeclaration> fieldDeclarations = classDelegateListener.getFieldDeclarations();
    assertEquals(2, fieldDeclarations.size());

    FieldDeclaration firstFieldDeclaration = fieldDeclarations.get(0);
    assertEquals(firstFieldName, firstFieldDeclaration.getName());

    Object firstFieldValue = firstFieldDeclaration.getValue();
    assertNotNull(firstFieldValue);
    assertTrue(firstFieldValue instanceof Expression);
    Expression expressionValue = (Expression) firstFieldValue;
    assertEquals(firstFieldString, expressionValue.getExpressionText());

    FieldDeclaration secondFieldDeclaration = fieldDeclarations.get(1);
    assertEquals(secondFieldName, secondFieldDeclaration.getName());

    Object secondFieldValue = secondFieldDeclaration.getValue();
    assertNotNull(secondFieldValue);
    assertTrue(secondFieldValue instanceof Expression);
    expressionValue = (Expression) secondFieldValue;
    assertEquals(secondFieldString, expressionValue.getExpressionText());

  }

  @Test
  public void testFieldInjectionStringValueOnDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaCaseExecutionListener caseExecutionListener = createElement(extensionElements, null, CamundaCaseExecutionListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = CaseExecutionListener.CREATE;
    caseExecutionListener.setCamundaEvent(event);
    caseExecutionListener.setCamundaDelegateExpression(delegateExpression);

    CamundaField firstField = createElement(caseExecutionListener, null, CamundaField.class);

    String firstFieldName = "firstField";
    String firstFieldStringValue = "aFirstFixedValue";
    firstField.setCamundaName(firstFieldName);
    firstField.setCamundaStringValue(firstFieldStringValue);

    CamundaField secondField = createElement(caseExecutionListener, null, CamundaField.class);

    String secondFieldName = "secondField";
    String secondFieldStringValue = "aSecondFixedValue";
    secondField.setCamundaName(secondFieldName);
    secondField.setCamundaStringValue(secondFieldStringValue);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then

    List<DelegateListener<? extends BaseDelegateExecution>> createListeners = activity.getListeners(event);
    assertEquals(1, createListeners.size());

    DelegateExpressionCaseExecutionListener classDelegateListener = (DelegateExpressionCaseExecutionListener) createListeners.get(0);

    assertEquals(delegateExpression, classDelegateListener.getExpressionText());

    List<FieldDeclaration> fieldDeclarations = classDelegateListener.getFieldDeclarations();
    assertEquals(2, fieldDeclarations.size());

    FieldDeclaration firstFieldDeclaration = fieldDeclarations.get(0);
    assertEquals(firstFieldName, firstFieldDeclaration.getName());

    Object firstFieldValue = firstFieldDeclaration.getValue();
    assertNotNull(firstFieldValue);
    assertTrue(firstFieldValue instanceof Expression);
    Expression expressionValue = (Expression) firstFieldValue;
    assertEquals(firstFieldStringValue, expressionValue.getExpressionText());

    FieldDeclaration secondFieldDeclaration = fieldDeclarations.get(1);
    assertEquals(secondFieldName, secondFieldDeclaration.getName());

    Object secondFieldValue = secondFieldDeclaration.getValue();
    assertNotNull(secondFieldValue);
    assertTrue(secondFieldValue instanceof Expression);
    expressionValue = (Expression) secondFieldValue;
    assertEquals(secondFieldStringValue, expressionValue.getExpressionText());

  }

  @Test
  public void testCreateTaskListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaTaskListener taskListener = createElement(extensionElements, null, CamundaTaskListener.class);

    String className = "org.camunda.bpm.test.tasklistener.ABC";
    String event = TaskListener.EVENTNAME_CREATE;
    taskListener.setCamundaEvent(event);
    taskListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(0, activity.getListeners().size());

    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    assertNotNull(taskDefinition);

    assertEquals(1, taskDefinition.getTaskListeners().size());

    List<TaskListener> createListeners = taskDefinition.getTaskListener(event);
    assertEquals(1, createListeners.size());
    TaskListener listener = createListeners.get(0);

    assertTrue(listener instanceof ClassDelegateTaskListener);

    ClassDelegateTaskListener classDelegateListener = (ClassDelegateTaskListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testCreateTaskListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaTaskListener taskListener = createElement(extensionElements, null, CamundaTaskListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = TaskListener.EVENTNAME_CREATE;
    taskListener.setCamundaEvent(event);
    taskListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(0, activity.getListeners().size());

    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    assertNotNull(taskDefinition);

    assertEquals(1, taskDefinition.getTaskListeners().size());

    List<TaskListener> createListeners = taskDefinition.getTaskListener(event);
    assertEquals(1, createListeners.size());
    TaskListener listener = createListeners.get(0);

    assertTrue(listener instanceof DelegateExpressionTaskListener);

    DelegateExpressionTaskListener delegateExpressionListener = (DelegateExpressionTaskListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testCreateTaskListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaTaskListener taskListener = createElement(extensionElements, null, CamundaTaskListener.class);

    String expression = "${myExpression}";
    String event = TaskListener.EVENTNAME_CREATE;
    taskListener.setCamundaEvent(event);
    taskListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(0, activity.getListeners().size());

    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    assertNotNull(taskDefinition);

    assertEquals(1, taskDefinition.getTaskListeners().size());

    List<TaskListener> createListeners = taskDefinition.getTaskListener(event);
    assertEquals(1, createListeners.size());
    TaskListener listener = createListeners.get(0);

    assertTrue(listener instanceof ExpressionTaskListener);

    ExpressionTaskListener expressionListener = (ExpressionTaskListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testCompleteTaskListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaTaskListener taskListener = createElement(extensionElements, null, CamundaTaskListener.class);

    String className = "org.camunda.bpm.test.tasklistener.ABC";
    String event = TaskListener.EVENTNAME_COMPLETE;
    taskListener.setCamundaEvent(event);
    taskListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(0, activity.getListeners().size());

    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    assertNotNull(taskDefinition);

    assertEquals(1, taskDefinition.getTaskListeners().size());

    List<TaskListener> createListeners = taskDefinition.getTaskListener(event);
    assertEquals(1, createListeners.size());
    TaskListener listener = createListeners.get(0);

    assertTrue(listener instanceof ClassDelegateTaskListener);

    ClassDelegateTaskListener classDelegateListener = (ClassDelegateTaskListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testCompleteTaskListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaTaskListener taskListener = createElement(extensionElements, null, CamundaTaskListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = TaskListener.EVENTNAME_COMPLETE;
    taskListener.setCamundaEvent(event);
    taskListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(0, activity.getListeners().size());

    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    assertNotNull(taskDefinition);

    assertEquals(1, taskDefinition.getTaskListeners().size());

    List<TaskListener> createListeners = taskDefinition.getTaskListener(event);
    assertEquals(1, createListeners.size());
    TaskListener listener = createListeners.get(0);

    assertTrue(listener instanceof DelegateExpressionTaskListener);

    DelegateExpressionTaskListener delegateExpressionListener = (DelegateExpressionTaskListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testCompleteTaskListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaTaskListener taskListener = createElement(extensionElements, null, CamundaTaskListener.class);

    String expression = "${myExpression}";
    String event = TaskListener.EVENTNAME_COMPLETE;
    taskListener.setCamundaEvent(event);
    taskListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(0, activity.getListeners().size());

    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    assertNotNull(taskDefinition);

    assertEquals(1, taskDefinition.getTaskListeners().size());

    List<TaskListener> createListeners = taskDefinition.getTaskListener(event);
    assertEquals(1, createListeners.size());
    TaskListener listener = createListeners.get(0);

    assertTrue(listener instanceof ExpressionTaskListener);

    ExpressionTaskListener expressionListener = (ExpressionTaskListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testAssignmentTaskListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaTaskListener taskListener = createElement(extensionElements, null, CamundaTaskListener.class);

    String className = "org.camunda.bpm.test.tasklistener.ABC";
    String event = TaskListener.EVENTNAME_ASSIGNMENT;
    taskListener.setCamundaEvent(event);
    taskListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(0, activity.getListeners().size());

    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    assertNotNull(taskDefinition);

    assertEquals(1, taskDefinition.getTaskListeners().size());

    List<TaskListener> createListeners = taskDefinition.getTaskListener(event);
    assertEquals(1, createListeners.size());
    TaskListener listener = createListeners.get(0);

    assertTrue(listener instanceof ClassDelegateTaskListener);

    ClassDelegateTaskListener classDelegateListener = (ClassDelegateTaskListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testAssignmentTaskListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaTaskListener taskListener = createElement(extensionElements, null, CamundaTaskListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = TaskListener.EVENTNAME_ASSIGNMENT;
    taskListener.setCamundaEvent(event);
    taskListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(0, activity.getListeners().size());

    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    assertNotNull(taskDefinition);

    assertEquals(1, taskDefinition.getTaskListeners().size());

    List<TaskListener> createListeners = taskDefinition.getTaskListener(event);
    assertEquals(1, createListeners.size());
    TaskListener listener = createListeners.get(0);

    assertTrue(listener instanceof DelegateExpressionTaskListener);

    DelegateExpressionTaskListener delegateExpressionListener = (DelegateExpressionTaskListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testAssignmentTaskListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaTaskListener taskListener = createElement(extensionElements, null, CamundaTaskListener.class);

    String expression = "${myExpression}";
    String event = TaskListener.EVENTNAME_ASSIGNMENT;
    taskListener.setCamundaEvent(event);
    taskListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(0, activity.getListeners().size());

    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    assertNotNull(taskDefinition);

    assertEquals(1, taskDefinition.getTaskListeners().size());

    List<TaskListener> createListeners = taskDefinition.getTaskListener(event);
    assertEquals(1, createListeners.size());
    TaskListener listener = createListeners.get(0);

    assertTrue(listener instanceof ExpressionTaskListener);

    ExpressionTaskListener expressionListener = (ExpressionTaskListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testDeleteTaskListenerByClass() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaTaskListener taskListener = createElement(extensionElements, null, CamundaTaskListener.class);

    String className = "org.camunda.bpm.test.tasklistener.ABC";
    String event = TaskListener.EVENTNAME_DELETE;
    taskListener.setCamundaEvent(event);
    taskListener.setCamundaClass(className);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(0, activity.getListeners().size());

    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    assertNotNull(taskDefinition);

    assertEquals(1, taskDefinition.getTaskListeners().size());

    List<TaskListener> createListeners = taskDefinition.getTaskListener(event);
    assertEquals(1, createListeners.size());
    TaskListener listener = createListeners.get(0);

    assertTrue(listener instanceof ClassDelegateTaskListener);

    ClassDelegateTaskListener classDelegateListener = (ClassDelegateTaskListener) listener;
    assertEquals(className, classDelegateListener.getClassName());
    assertTrue(classDelegateListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testDeleteTaskListenerByDelegateExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaTaskListener taskListener = createElement(extensionElements, null, CamundaTaskListener.class);

    String delegateExpression = "${myDelegateExpression}";
    String event = TaskListener.EVENTNAME_DELETE;
    taskListener.setCamundaEvent(event);
    taskListener.setCamundaDelegateExpression(delegateExpression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(0, activity.getListeners().size());

    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    assertNotNull(taskDefinition);

    assertEquals(1, taskDefinition.getTaskListeners().size());

    List<TaskListener> createListeners = taskDefinition.getTaskListener(event);
    assertEquals(1, createListeners.size());
    TaskListener listener = createListeners.get(0);

    assertTrue(listener instanceof DelegateExpressionTaskListener);

    DelegateExpressionTaskListener delegateExpressionListener = (DelegateExpressionTaskListener) listener;
    assertEquals(delegateExpression, delegateExpressionListener.getExpressionText());
    assertTrue(delegateExpressionListener.getFieldDeclarations().isEmpty());

  }

  @Test
  public void testDeleteTaskListenerByExpression() {
    // given:
    ExtensionElements extensionElements = addExtensionElements(humanTask);
    CamundaTaskListener taskListener = createElement(extensionElements, null, CamundaTaskListener.class);

    String expression = "${myExpression}";
    String event = TaskListener.EVENTNAME_DELETE;
    taskListener.setCamundaEvent(event);
    taskListener.setCamundaExpression(expression);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(0, activity.getListeners().size());

    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    assertNotNull(taskDefinition);

    assertEquals(1, taskDefinition.getTaskListeners().size());

    List<TaskListener> createListeners = taskDefinition.getTaskListener(event);
    assertEquals(1, createListeners.size());
    TaskListener listener = createListeners.get(0);

    assertTrue(listener instanceof ExpressionTaskListener);

    ExpressionTaskListener expressionListener = (ExpressionTaskListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

}
