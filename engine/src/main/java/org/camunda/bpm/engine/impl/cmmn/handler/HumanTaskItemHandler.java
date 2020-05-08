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

import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.bpmn.parser.FieldDeclaration;
import org.camunda.bpm.engine.impl.cmmn.behavior.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.behavior.HumanTaskActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.form.handler.DefaultTaskFormHandler;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.engine.impl.task.TaskDecorator;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.engine.impl.task.listener.ClassDelegateTaskListener;
import org.camunda.bpm.engine.impl.task.listener.DelegateExpressionTaskListener;
import org.camunda.bpm.engine.impl.task.listener.ExpressionTaskListener;
import org.camunda.bpm.engine.impl.task.listener.ScriptTaskListener;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.cmmn.instance.HumanTask;
import org.camunda.bpm.model.cmmn.instance.Role;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaField;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaScript;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaTaskListener;

/**
 * @author Roman Smirnov
 *
 */
public class HumanTaskItemHandler extends TaskItemHandler {

  public CmmnActivity handleElement(CmmnElement element, CmmnHandlerContext context) {
    HumanTask definition = getDefinition(element);

    if (!definition.isBlocking()) {
      // The CMMN 1.0 specification says:
      // When a HumanTask is not 'blocking' (isBlocking is 'false'),
      // it can be considered a 'manual' Task, i.e., the Case management
      // system is not tracking the lifecycle of the HumanTask (instance).
      return null;
    }

    return super.handleElement(element, context);
  }

  @Override
  protected void initializeActivity(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context) {
    // execute standard initialization
    super.initializeActivity(element, activity, context);

    // create a taskDefinition
    TaskDefinition taskDefinition = createTaskDefinition(element, context);

    // get the caseDefinition...
    CaseDefinitionEntity caseDefinition = (CaseDefinitionEntity) context.getCaseDefinition();
    // ... and taskDefinition to caseDefinition
    caseDefinition.getTaskDefinitions().put(taskDefinition.getKey(), taskDefinition);

    ExpressionManager expressionManager = context.getExpressionManager();
    // create decorator
    TaskDecorator taskDecorator = new TaskDecorator(taskDefinition, expressionManager);

    // set taskDecorator on behavior
    HumanTaskActivityBehavior behavior = (HumanTaskActivityBehavior) activity.getActivityBehavior();
    behavior.setTaskDecorator(taskDecorator);

    // task listeners
    initializeTaskListeners(element, activity, context, taskDefinition);

  }

  protected TaskDefinition createTaskDefinition(CmmnElement element, CmmnHandlerContext context) {
    Deployment deployment = context.getDeployment();
    String deploymentId = deployment.getId();

    // at the moment a default task form handler is only supported,
    // custom task form handler are not supported.
    DefaultTaskFormHandler taskFormHandler = new DefaultTaskFormHandler();
    taskFormHandler.setDeploymentId(deploymentId);

    // create new taskDefinition
    TaskDefinition taskDefinition = new TaskDefinition(taskFormHandler);

    // the plan item id will be handled as taskDefinitionKey
    String taskDefinitionKey = element.getId();
    taskDefinition.setKey(taskDefinitionKey);

    // name
    initializeTaskDefinitionName(element, taskDefinition, context);
    // dueDate
    initializeTaskDefinitionDueDate(element, taskDefinition, context);
    // followUp
    initializeTaskDefinitionFollowUpDate(element, taskDefinition, context);
    // priority
    initializeTaskDefinitionPriority(element, taskDefinition, context);
    // assignee
    initializeTaskDefinitionAssignee(element, taskDefinition, context);
    // candidateUsers
    initializeTaskDefinitionCandidateUsers(element, taskDefinition, context);
    // candidateGroups
    initializeTaskDefinitionCandidateGroups(element, taskDefinition, context);
    // formKey
    initializeTaskDefinitionFormKey(element, taskDefinition, context);
    // description
    initializeTaskDescription(element, taskDefinition, context);

    return taskDefinition;
  }

  protected void initializeTaskDefinitionName(CmmnElement element, TaskDefinition taskDefinition, CmmnHandlerContext context) {
    String name = getName(element);
    if (name == null) {
      HumanTask definition = getDefinition(element);
      name = definition.getName();
    }

    if (name != null) {
      ExpressionManager expressionManager = context.getExpressionManager();
      Expression nameExpression = expressionManager.createExpression(name);
      taskDefinition.setNameExpression(nameExpression);
    }

  }

  protected void initializeTaskDefinitionFormKey(CmmnElement element, TaskDefinition taskDefinition, CmmnHandlerContext context) {
    HumanTask definition = getDefinition(element);

    String formKey = definition.getCamundaFormKey();
    if (formKey != null) {
      ExpressionManager expressionManager = context.getExpressionManager();
      Expression formKeyExpression = expressionManager.createExpression(formKey);
      taskDefinition.setFormKey(formKeyExpression);
    }
  }

  protected void initializeTaskDefinitionAssignee(CmmnElement element, TaskDefinition taskDefinition, CmmnHandlerContext context) {
    HumanTask definition = getDefinition(element);
    Role performer = definition.getPerformer();

    String assignee = null;
    if (performer != null) {
      assignee = performer.getName();
    } else {
      assignee = definition.getCamundaAssignee();
    }

    if (assignee != null) {
      ExpressionManager expressionManager = context.getExpressionManager();
      Expression assigneeExpression = expressionManager.createExpression(assignee);
      taskDefinition.setAssigneeExpression(assigneeExpression);
    }
  }

  protected void initializeTaskDefinitionCandidateUsers(CmmnElement element, TaskDefinition taskDefinition, CmmnHandlerContext context) {
    HumanTask definition = getDefinition(element);
    ExpressionManager expressionManager = context.getExpressionManager();

    List<String> candidateUsers = definition.getCamundaCandidateUsersList();
    for (String candidateUser : candidateUsers) {
      Expression candidateUserExpression = expressionManager.createExpression(candidateUser);
      taskDefinition.addCandidateUserIdExpression(candidateUserExpression);
    }
  }

  protected void initializeTaskDefinitionCandidateGroups(CmmnElement element, TaskDefinition taskDefinition, CmmnHandlerContext context) {
    HumanTask definition = getDefinition(element);
    ExpressionManager expressionManager = context.getExpressionManager();

    List<String> candidateGroups = definition.getCamundaCandidateGroupsList();
    for (String candidateGroup : candidateGroups) {
      Expression candidateGroupExpression = expressionManager.createExpression(candidateGroup);
      taskDefinition.addCandidateGroupIdExpression(candidateGroupExpression);
    }
  }

  protected void initializeTaskDefinitionDueDate(CmmnElement element, TaskDefinition taskDefinition, CmmnHandlerContext context) {
    HumanTask definition = getDefinition(element);

    String dueDate = definition.getCamundaDueDate();
    if (dueDate != null) {
      ExpressionManager expressionManager = context.getExpressionManager();
      Expression dueDateExpression = expressionManager.createExpression(dueDate);
      taskDefinition.setDueDateExpression(dueDateExpression);
    }
  }

  protected void initializeTaskDefinitionFollowUpDate(CmmnElement element, TaskDefinition taskDefinition, CmmnHandlerContext context) {
    HumanTask definition = getDefinition(element);

    String followUpDate = definition.getCamundaFollowUpDate();
    if (followUpDate != null) {
      ExpressionManager expressionManager = context.getExpressionManager();
      Expression followUpDateExpression = expressionManager.createExpression(followUpDate);
      taskDefinition.setFollowUpDateExpression(followUpDateExpression);
    }
  }

  protected void initializeTaskDefinitionPriority(CmmnElement element, TaskDefinition taskDefinition, CmmnHandlerContext context) {
    HumanTask definition = getDefinition(element);

    String priority = definition.getCamundaPriority();
    if (priority != null) {
      ExpressionManager expressionManager = context.getExpressionManager();
      Expression priorityExpression = expressionManager.createExpression(priority);
      taskDefinition.setPriorityExpression(priorityExpression);
    }
  }

  protected void initializeTaskDescription(CmmnElement element, TaskDefinition taskDefinition, CmmnHandlerContext context) {
    String description = getDesciption(element);
    if (description != null && ! description.isEmpty()) {
      ExpressionManager expressionManager = context.getExpressionManager();
      Expression descriptionExpression = expressionManager.createExpression(description);
      taskDefinition.setDescriptionExpression(descriptionExpression);
    }
    else {
      String documentation = getDocumentation(element);
      if (documentation != null && !documentation.isEmpty()) {
        ExpressionManager expressionManager = context.getExpressionManager();
        Expression documentationExpression = expressionManager.createExpression(documentation);
        taskDefinition.setDescriptionExpression(documentationExpression);
      }
    }
  }

  protected void initializeTaskListeners(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context, TaskDefinition taskDefinition) {
    HumanTask humanTask = getDefinition(element);

    List<CamundaTaskListener> listeners = queryExtensionElementsByClass(humanTask, CamundaTaskListener.class);

    for (CamundaTaskListener listener : listeners) {
      TaskListener taskListener = initializeTaskListener(element, activity, context, listener);

      String eventName = listener.getCamundaEvent();
      if (eventName != null) {
        taskDefinition.addTaskListener(eventName, taskListener);

      } else {
        taskDefinition.addTaskListener(TaskListener.EVENTNAME_CREATE, taskListener);
        taskDefinition.addTaskListener(TaskListener.EVENTNAME_ASSIGNMENT, taskListener);
        taskDefinition.addTaskListener(TaskListener.EVENTNAME_COMPLETE, taskListener);
        taskDefinition.addTaskListener(TaskListener.EVENTNAME_UPDATE, taskListener);
        taskDefinition.addTaskListener(TaskListener.EVENTNAME_DELETE, taskListener);
      }
    }
  }

  protected TaskListener initializeTaskListener(CmmnElement element, CmmnActivity activity, CmmnHandlerContext context, CamundaTaskListener listener) {
    Collection<CamundaField> fields = listener.getCamundaFields();
    List<FieldDeclaration> fieldDeclarations = initializeFieldDeclarations(element, activity, context, fields);

    ExpressionManager expressionManager = context.getExpressionManager();

    TaskListener taskListener = null;

    String className = listener.getCamundaClass();
    String expression = listener.getCamundaExpression();
    String delegateExpression = listener.getCamundaDelegateExpression();
    CamundaScript scriptElement = listener.getCamundaScript();

    if (className != null) {
      taskListener = new ClassDelegateTaskListener(className, fieldDeclarations);

    } else if (expression != null) {
      Expression expressionExp = expressionManager.createExpression(expression);
      taskListener = new ExpressionTaskListener(expressionExp);

    } else if (delegateExpression != null) {
      Expression delegateExp = expressionManager.createExpression(delegateExpression);
      taskListener = new DelegateExpressionTaskListener(delegateExp, fieldDeclarations);

    } else if (scriptElement != null) {
      ExecutableScript executableScript = initializeScript(element, activity, context, scriptElement);
      if (executableScript != null) {
        taskListener = new ScriptTaskListener(executableScript);
      }
    }

    return taskListener;
  }

  protected HumanTask getDefinition(CmmnElement element) {
    return (HumanTask) super.getDefinition(element);
  }

  @Override
  protected CmmnActivityBehavior getActivityBehavior() {
    return new HumanTaskActivityBehavior();
  }

}
