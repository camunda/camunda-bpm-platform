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

import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_ACTIVITY_DESCRIPTION;
import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_ACTIVITY_TYPE;
import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_IS_BLOCKING;
import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_MANUAL_ACTIVATION_RULE;
import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_REPETITION_RULE;
import static org.camunda.bpm.engine.impl.cmmn.handler.ItemHandler.PROPERTY_REQUIRED_RULE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.delegate.CaseExecutionListener;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.helper.CmmnProperties;
import org.camunda.bpm.engine.impl.cmmn.CaseControlRule;
import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.HumanTaskActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.handler.CasePlanModelHandler;
import org.camunda.bpm.engine.impl.cmmn.handler.HumanTaskItemHandler;
import org.camunda.bpm.engine.impl.cmmn.handler.SentryHandler;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnCaseDefinition;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnSentryDeclaration;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.task.listener.ClassDelegateTaskListener;
import org.camunda.bpm.engine.impl.task.listener.DelegateExpressionTaskListener;
import org.camunda.bpm.engine.impl.task.listener.ExpressionTaskListener;
import org.camunda.bpm.model.cmmn.Cmmn;
import org.camunda.bpm.model.cmmn.instance.Body;
import org.camunda.bpm.model.cmmn.instance.CaseRole;
import org.camunda.bpm.model.cmmn.instance.ConditionExpression;
import org.camunda.bpm.model.cmmn.instance.DefaultControl;
import org.camunda.bpm.model.cmmn.instance.EntryCriterion;
import org.camunda.bpm.model.cmmn.instance.ExitCriterion;
import org.camunda.bpm.model.cmmn.instance.ExtensionElements;
import org.camunda.bpm.model.cmmn.instance.HumanTask;
import org.camunda.bpm.model.cmmn.instance.IfPart;
import org.camunda.bpm.model.cmmn.instance.ItemControl;
import org.camunda.bpm.model.cmmn.instance.ManualActivationRule;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.PlanItemControl;
import org.camunda.bpm.model.cmmn.instance.RepetitionRule;
import org.camunda.bpm.model.cmmn.instance.RequiredRule;
import org.camunda.bpm.model.cmmn.instance.Sentry;
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
  public void testHumanTaskActivityType() {
    // given

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    String activityType = (String) activity.getProperty(PROPERTY_ACTIVITY_TYPE);
    assertEquals("humanTask", activityType);
  }

  @Test
  public void testHumanTaskDescriptionProperty() {
    // given
    String description = "This is a humanTask";
    humanTask.setDescription(description);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(description, activity.getProperty(PROPERTY_ACTIVITY_DESCRIPTION));
  }

  @Test
  public void testPlanItemDescriptionProperty() {
    // given
    String description = "This is a planItem";
    planItem.setDescription(description);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    assertEquals(description, activity.getProperty(PROPERTY_ACTIVITY_DESCRIPTION));
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
    Boolean isBlocking = (Boolean) activity.getProperty(PROPERTY_IS_BLOCKING);
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
  public void testTaskDefinitionFollowUpDateExpression() {
    // given
    String aFollowUpDate = "aFollowDate";
    humanTask.setCamundaFollowUpDate(aFollowUpDate);

    // when
    CmmnActivity activity = handler.handleElement(planItem, context);

    // then
    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    TaskDefinition taskDefinition = behavior.getTaskDefinition();

    Expression followUpDateExpression = taskDefinition.getFollowUpDateExpression();
    assertNotNull(followUpDateExpression);
    assertEquals(aFollowUpDate, followUpDateExpression.getExpressionText());
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
    CaseRole role = createElement(caseDefinition, "aRole", CaseRole.class);
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

    List<TaskListener> createListeners = taskDefinition.getTaskListeners(event);
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

    List<TaskListener> createListeners = taskDefinition.getTaskListeners(event);
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

    List<TaskListener> createListeners = taskDefinition.getTaskListeners(event);
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

    List<TaskListener> createListeners = taskDefinition.getTaskListeners(event);
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

    List<TaskListener> createListeners = taskDefinition.getTaskListeners(event);
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

    List<TaskListener> createListeners = taskDefinition.getTaskListeners(event);
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

    List<TaskListener> createListeners = taskDefinition.getTaskListeners(event);
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

    List<TaskListener> createListeners = taskDefinition.getTaskListeners(event);
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

    List<TaskListener> createListeners = taskDefinition.getTaskListeners(event);
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

    List<TaskListener> createListeners = taskDefinition.getTaskListeners(event);
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

    List<TaskListener> createListeners = taskDefinition.getTaskListeners(event);
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

    List<TaskListener> createListeners = taskDefinition.getTaskListeners(event);
    assertEquals(1, createListeners.size());
    TaskListener listener = createListeners.get(0);

    assertTrue(listener instanceof ExpressionTaskListener);

    ExpressionTaskListener expressionListener = (ExpressionTaskListener) listener;
    assertEquals(expression, expressionListener.getExpressionText());

  }

  @Test
  public void testExitCriteria() {
    // given

    // create sentry containing ifPart
    Sentry sentry = createElement(casePlanModel, "Sentry_1", Sentry.class);
    IfPart ifPart = createElement(sentry, "abc", IfPart.class);
    ConditionExpression conditionExpression = createElement(ifPart, "def", ConditionExpression.class);
    Body body = createElement(conditionExpression, null, Body.class);
    body.setTextContent("${test}");

    // set exitCriteria
    ExitCriterion criterion = createElement(planItem, ExitCriterion.class);
    criterion.setSentry(sentry);

    // transform casePlanModel as parent
    CmmnActivity parent = new CasePlanModelHandler().handleElement(casePlanModel, context);
    context.setParent(parent);

    // transform Sentry
    CmmnSentryDeclaration sentryDeclaration = new SentryHandler().handleElement(sentry, context);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    assertTrue(newActivity.getEntryCriteria().isEmpty());

    assertFalse(newActivity.getExitCriteria().isEmpty());
    assertEquals(1, newActivity.getExitCriteria().size());

    assertEquals(sentryDeclaration, newActivity.getExitCriteria().get(0));

  }

  @Test
  public void testMultipleExitCriteria() {
    // given

    // create first sentry containing ifPart
    Sentry sentry1 = createElement(casePlanModel, "Sentry_1", Sentry.class);
    IfPart ifPart1 = createElement(sentry1, "abc", IfPart.class);
    ConditionExpression conditionExpression1 = createElement(ifPart1, "def", ConditionExpression.class);
    Body body1 = createElement(conditionExpression1, null, Body.class);
    body1.setTextContent("${test}");

    // set first exitCriteria
    ExitCriterion criterion1 = createElement(planItem, ExitCriterion.class);
    criterion1.setSentry(sentry1);

    // create first sentry containing ifPart
    Sentry sentry2 = createElement(casePlanModel, "Sentry_2", Sentry.class);
    IfPart ifPart2 = createElement(sentry2, "ghi", IfPart.class);
    ConditionExpression conditionExpression2 = createElement(ifPart2, "jkl", ConditionExpression.class);
    Body body2 = createElement(conditionExpression2, null, Body.class);
    body2.setTextContent("${test}");

    // set second exitCriteria
    ExitCriterion criterion2 = createElement(planItem, ExitCriterion.class);
    criterion2.setSentry(sentry2);

    // transform casePlanModel as parent
    CmmnActivity parent = new CasePlanModelHandler().handleElement(casePlanModel, context);
    context.setParent(parent);

    // transform Sentry
    CmmnSentryDeclaration firstSentryDeclaration = new SentryHandler().handleElement(sentry1, context);
    CmmnSentryDeclaration secondSentryDeclaration = new SentryHandler().handleElement(sentry2, context);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    assertTrue(newActivity.getEntryCriteria().isEmpty());

    assertFalse(newActivity.getExitCriteria().isEmpty());
    assertEquals(2, newActivity.getExitCriteria().size());

    assertTrue(newActivity.getExitCriteria().contains(firstSentryDeclaration));
    assertTrue(newActivity.getExitCriteria().contains(secondSentryDeclaration));

  }

  @Test
  public void testEntryCriteria() {
    // given

    // create sentry containing ifPart
    Sentry sentry = createElement(casePlanModel, "Sentry_1", Sentry.class);
    IfPart ifPart = createElement(sentry, "abc", IfPart.class);
    ConditionExpression conditionExpression = createElement(ifPart, "def", ConditionExpression.class);
    Body body = createElement(conditionExpression, null, Body.class);
    body.setTextContent("${test}");

    // set entryCriterion
    EntryCriterion criterion = createElement(planItem, EntryCriterion.class);
    criterion.setSentry(sentry);

    // transform casePlanModel as parent
    CmmnActivity parent = new CasePlanModelHandler().handleElement(casePlanModel, context);
    context.setParent(parent);

    // transform Sentry
    CmmnSentryDeclaration sentryDeclaration = new SentryHandler().handleElement(sentry, context);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    assertTrue(newActivity.getExitCriteria().isEmpty());

    assertFalse(newActivity.getEntryCriteria().isEmpty());
    assertEquals(1, newActivity.getEntryCriteria().size());

    assertEquals(sentryDeclaration, newActivity.getEntryCriteria().get(0));

  }

  @Test
  public void testMultipleEntryCriteria() {
    // given

    // create first sentry containing ifPart
    Sentry sentry1 = createElement(casePlanModel, "Sentry_1", Sentry.class);
    IfPart ifPart1 = createElement(sentry1, "abc", IfPart.class);
    ConditionExpression conditionExpression1 = createElement(ifPart1, "def", ConditionExpression.class);
    Body body1 = createElement(conditionExpression1, null, Body.class);
    body1.setTextContent("${test}");

    // set first entryCriteria
    EntryCriterion criterion1 = createElement(planItem, EntryCriterion.class);
    criterion1.setSentry(sentry1);

    // create first sentry containing ifPart
    Sentry sentry2 = createElement(casePlanModel, "Sentry_2", Sentry.class);
    IfPart ifPart2 = createElement(sentry2, "ghi", IfPart.class);
    ConditionExpression conditionExpression2 = createElement(ifPart2, "jkl", ConditionExpression.class);
    Body body2 = createElement(conditionExpression2, null, Body.class);
    body2.setTextContent("${test}");

    // set second entryCriteria
    EntryCriterion criterion2 = createElement(planItem, EntryCriterion.class);
    criterion2.setSentry(sentry2);

    // transform casePlanModel as parent
    CmmnActivity parent = new CasePlanModelHandler().handleElement(casePlanModel, context);
    context.setParent(parent);

    // transform Sentry
    CmmnSentryDeclaration firstSentryDeclaration = new SentryHandler().handleElement(sentry1, context);
    CmmnSentryDeclaration secondSentryDeclaration = new SentryHandler().handleElement(sentry2, context);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    assertTrue(newActivity.getExitCriteria().isEmpty());

    assertFalse(newActivity.getEntryCriteria().isEmpty());
    assertEquals(2, newActivity.getEntryCriteria().size());

    assertTrue(newActivity.getEntryCriteria().contains(firstSentryDeclaration));
    assertTrue(newActivity.getEntryCriteria().contains(secondSentryDeclaration));

  }

  @Test
  public void testEntryCriteriaAndExitCriteria() {
    // given

    // create sentry containing ifPart
    Sentry sentry = createElement(casePlanModel, "Sentry_1", Sentry.class);
    IfPart ifPart = createElement(sentry, "abc", IfPart.class);
    ConditionExpression conditionExpression = createElement(ifPart, "def", ConditionExpression.class);
    Body body = createElement(conditionExpression, null, Body.class);
    body.setTextContent("${test}");

    // set entryCriteria
    EntryCriterion criterion1 = createElement(planItem, EntryCriterion.class);
    criterion1.setSentry(sentry);

    // set exitCriterion
    ExitCriterion criterion2 = createElement(planItem, ExitCriterion.class);
    criterion2.setSentry(sentry);

    // transform casePlanModel as parent
    CmmnActivity parent = new CasePlanModelHandler().handleElement(casePlanModel, context);
    context.setParent(parent);

    // transform Sentry
    CmmnSentryDeclaration sentryDeclaration = new SentryHandler().handleElement(sentry, context);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    assertFalse(newActivity.getExitCriteria().isEmpty());
    assertEquals(1, newActivity.getExitCriteria().size());
    assertEquals(sentryDeclaration, newActivity.getExitCriteria().get(0));

    assertFalse(newActivity.getEntryCriteria().isEmpty());
    assertEquals(1, newActivity.getEntryCriteria().size());
    assertEquals(sentryDeclaration, newActivity.getEntryCriteria().get(0));

  }

  @Test
  public void testManualActivationRule() {
    // given
    ItemControl itemControl = createElement(planItem, "ItemControl_1", ItemControl.class);
    ManualActivationRule manualActivationRule = createElement(itemControl, "ManualActivationRule_1", ManualActivationRule.class);
    ConditionExpression expression = createElement(manualActivationRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    Object rule = newActivity.getProperty(PROPERTY_MANUAL_ACTIVATION_RULE);
    assertNotNull(rule);
    assertTrue(rule instanceof CaseControlRule);
  }

  @Test
  public void testManualActivationRuleByDefaultPlanItemControl() {
    // given
    PlanItemControl defaultControl = createElement(humanTask, "ItemControl_1", DefaultControl.class);
    ManualActivationRule manualActivationRule = createElement(defaultControl, "ManualActivationRule_1", ManualActivationRule.class);
    ConditionExpression expression = createElement(manualActivationRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    Object rule = newActivity.getProperty(PROPERTY_MANUAL_ACTIVATION_RULE);
    assertNotNull(rule);
    assertTrue(rule instanceof CaseControlRule);
  }

  @Test
  public void testRequiredRule() {
    // given
    ItemControl itemControl = createElement(planItem, "ItemControl_1", ItemControl.class);
    RequiredRule requiredRule = createElement(itemControl, "RequiredRule_1", RequiredRule.class);
    ConditionExpression expression = createElement(requiredRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    Object rule = newActivity.getProperty(PROPERTY_REQUIRED_RULE);
    assertNotNull(rule);
    assertTrue(rule instanceof CaseControlRule);
  }

  @Test
  public void testRequiredRuleByDefaultPlanItemControl() {
    // given
    PlanItemControl defaultControl = createElement(humanTask, "ItemControl_1", DefaultControl.class);
    RequiredRule requiredRule = createElement(defaultControl, "RequiredRule_1", RequiredRule.class);
    ConditionExpression expression = createElement(requiredRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    Object rule = newActivity.getProperty(PROPERTY_REQUIRED_RULE);
    assertNotNull(rule);
    assertTrue(rule instanceof CaseControlRule);
  }

  @Test
  public void testRepetitionRule() {
    // given
    ItemControl itemControl = createElement(planItem, "ItemControl_1", ItemControl.class);
    RepetitionRule repetitionRule = createElement(itemControl, "RepititionRule_1", RepetitionRule.class);
    ConditionExpression expression = createElement(repetitionRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    Object rule = newActivity.getProperty(PROPERTY_REPETITION_RULE);
    assertNotNull(rule);
    assertTrue(rule instanceof CaseControlRule);
  }

  @Test
  public void testRepetitionRuleByDefaultPlanItemControl() {
    // given
    PlanItemControl defaultControl = createElement(humanTask, "DefaultControl_1", DefaultControl.class);
    RepetitionRule repetitionRule = createElement(defaultControl, "RepititionRule_1", RepetitionRule.class);
    ConditionExpression expression = createElement(repetitionRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    Object rule = newActivity.getProperty(PROPERTY_REPETITION_RULE);
    assertNotNull(rule);
    assertTrue(rule instanceof CaseControlRule);
  }

  @Test
  public void testRepetitionRuleStandardEvents() {
    // given
    ItemControl itemControl = createElement(planItem, "ItemControl_1", ItemControl.class);
    RepetitionRule repetitionRule = createElement(itemControl, "RepititionRule_1", RepetitionRule.class);
    ConditionExpression expression = createElement(repetitionRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    List<String> events = newActivity.getProperties().get(CmmnProperties.REPEAT_ON_STANDARD_EVENTS);
    assertNotNull(events);
    assertEquals(2, events.size());
    assertTrue(events.contains(CaseExecutionListener.COMPLETE));
    assertTrue(events.contains(CaseExecutionListener.TERMINATE));
  }

  @Test
  public void testRepetitionRuleStandardEventsByDefaultPlanItemControl() {
    // given
    PlanItemControl defaultControl = createElement(humanTask, "DefaultControl_1", DefaultControl.class);
    RepetitionRule repetitionRule = createElement(defaultControl, "RepititionRule_1", RepetitionRule.class);
    ConditionExpression expression = createElement(repetitionRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    List<String> events = newActivity.getProperties().get(CmmnProperties.REPEAT_ON_STANDARD_EVENTS);
    assertNotNull(events);
    assertEquals(2, events.size());
    assertTrue(events.contains(CaseExecutionListener.COMPLETE));
    assertTrue(events.contains(CaseExecutionListener.TERMINATE));
  }

  @Test
  public void testRepetitionRuleCustomStandardEvents() {
    // given
    ItemControl itemControl = createElement(planItem, "ItemControl_1", ItemControl.class);
    RepetitionRule repetitionRule = createElement(itemControl, "RepititionRule_1", RepetitionRule.class);
    ConditionExpression expression = createElement(repetitionRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    repetitionRule.setCamundaRepeatOnStandardEvent(CaseExecutionListener.DISABLE);

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    List<String> events = newActivity.getProperties().get(CmmnProperties.REPEAT_ON_STANDARD_EVENTS);
    assertNotNull(events);
    assertEquals(1, events.size());
    assertTrue(events.contains(CaseExecutionListener.DISABLE));
  }

  @Test
  public void testRepetitionRuleCustomStandardEventsByDefaultPlanItemControl() {
    // given
    PlanItemControl defaultControl = createElement(humanTask, "DefaultControl_1", DefaultControl.class);
    RepetitionRule repetitionRule = createElement(defaultControl, "RepititionRule_1", RepetitionRule.class);
    ConditionExpression expression = createElement(repetitionRule, "Expression_1", ConditionExpression.class);
    expression.setText("${true}");

    repetitionRule.setCamundaRepeatOnStandardEvent(CaseExecutionListener.DISABLE);

    Cmmn.validateModel(modelInstance);

    // when
    CmmnActivity newActivity = handler.handleElement(planItem, context);

    // then
    List<String> events = newActivity.getProperties().get(CmmnProperties.REPEAT_ON_STANDARD_EVENTS);
    assertNotNull(events);
    assertEquals(1, events.size());
    assertTrue(events.contains(CaseExecutionListener.DISABLE));
  }

}
