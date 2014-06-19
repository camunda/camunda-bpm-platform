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

import java.util.List;

import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.cmmn.behavior.HumanTaskActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.entity.repository.CaseDefinitionEntity;
import org.camunda.bpm.engine.impl.cmmn.execution.CmmnActivityBehavior;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.form.handler.DefaultTaskFormHandler;
import org.camunda.bpm.engine.impl.form.handler.TaskFormHandler;
import org.camunda.bpm.engine.impl.task.TaskDecorator;
import org.camunda.bpm.engine.impl.task.TaskDefinition;
import org.camunda.bpm.model.cmmn.instance.HumanTask;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.PlanItemDefinition;
import org.camunda.bpm.model.cmmn.instance.Role;

/**
 * @author Roman Smirnov
 *
 */
public class HumanTaskPlanItemHandler extends TaskPlanItemHandler {

  @Override
  protected void initializeActivity(PlanItem planItem, CmmnActivity activity, CmmnHandlerContext context) {
    // execute standard initialization
    super.initializeActivity(planItem, activity, context);

    // create a taskDefinition
    TaskDefinition taskDefinition = createTaskDefinition(planItem, context);

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

  }

  protected TaskDefinition createTaskDefinition(PlanItem planItem, CmmnHandlerContext context) {
    // at the moment a default task form handler is only supported,
    // custom task form handler are not supported.
    TaskFormHandler taskFormHandler = new DefaultTaskFormHandler();

    // create new taskDefinition
    TaskDefinition taskDefinition = new TaskDefinition(taskFormHandler);

    // the plan item id will be handled as taskDefinitionKey
    String taskDefinitionKey = planItem.getId();
    taskDefinition.setKey(taskDefinitionKey);

    // name
    initializeTaskDefinitionName(planItem, taskDefinition, context);
    // dueDate
    initializeTaskDefinitionDueDate(planItem, taskDefinition, context);
    // priority
    initializeTaskDefinitionPriority(planItem, taskDefinition, context);
    // assignee
    initializeTaskDefinitionAssignee(planItem, taskDefinition, context);
    // candidateUsers
    initializeTaskDefinitionCandidateUsers(planItem, taskDefinition, context);
    // candidateGroups
    initializeTaskDefinitionCandidateGroups(planItem, taskDefinition, context);
    // formKey
    initializeTaskDefinitionFormKey(planItem, taskDefinition, context);

    return taskDefinition;
  }


  protected void initializeTaskDefinitionName(PlanItem planItem, TaskDefinition taskDefinition, CmmnHandlerContext context) {
    String name = planItem.getName();
    if (name == null) {
      PlanItemDefinition definition = planItem.getDefinition();
      name = definition.getName();
    }

    if (name != null) {
      ExpressionManager expressionManager = context.getExpressionManager();
      Expression nameExpression = expressionManager.createExpression(name);
      taskDefinition.setNameExpression(nameExpression);
    }

  }

  protected void initializeTaskDefinitionFormKey(PlanItem planItem, TaskDefinition taskDefinition, CmmnHandlerContext context) {
    HumanTask definition = (HumanTask) planItem.getDefinition();

    String formKey = definition.getCamundaFormKey();
    if (formKey != null) {
      ExpressionManager expressionManager = context.getExpressionManager();
      Expression formKeyExpression = expressionManager.createExpression(formKey);
      taskDefinition.setFormKey(formKeyExpression);
    }
  }

  protected void initializeTaskDefinitionAssignee(PlanItem planItem, TaskDefinition taskDefinition, CmmnHandlerContext context) {
    HumanTask definition = (HumanTask) planItem.getDefinition();
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

  protected void initializeTaskDefinitionCandidateUsers(PlanItem planItem, TaskDefinition taskDefinition, CmmnHandlerContext context) {
    HumanTask definition = (HumanTask) planItem.getDefinition();
    ExpressionManager expressionManager = context.getExpressionManager();

    List<String> candidateUsers = definition.getCamundaCandidateUsersList();
    for (String candidateUser : candidateUsers) {
      Expression candidateUserExpression = expressionManager.createExpression(candidateUser);
      taskDefinition.addCandidateUserIdExpression(candidateUserExpression);
    }
  }

  protected void initializeTaskDefinitionCandidateGroups(PlanItem planItem, TaskDefinition taskDefinition, CmmnHandlerContext context) {
    HumanTask definition = (HumanTask) planItem.getDefinition();
    ExpressionManager expressionManager = context.getExpressionManager();

    List<String> candidateGroups = definition.getCamundaCandidateGroupsList();
    for (String candidateGroup : candidateGroups) {
      Expression candidateGroupExpression = expressionManager.createExpression(candidateGroup);
      taskDefinition.addCandidateGroupIdExpression(candidateGroupExpression);
    }
  }

  protected void initializeTaskDefinitionDueDate(PlanItem planItem, TaskDefinition taskDefinition, CmmnHandlerContext context) {
    HumanTask definition = (HumanTask) planItem.getDefinition();

    String dueDate = definition.getCamundaDueDate();
    if (dueDate != null) {
      ExpressionManager expressionManager = context.getExpressionManager();
      Expression dueDateExpression = expressionManager.createExpression(dueDate);
      taskDefinition.setDueDateExpression(dueDateExpression);
    }
  }

  protected void initializeTaskDefinitionPriority(PlanItem planItem, TaskDefinition taskDefinition, CmmnHandlerContext context) {
    HumanTask definition = (HumanTask) planItem.getDefinition();

    String priority = definition.getCamundaPriority();
    if (priority != null) {
      ExpressionManager expressionManager = context.getExpressionManager();
      Expression priorityExpression = expressionManager.createExpression(priority);
      taskDefinition.setPriorityExpression(priorityExpression);
    }
  }

  @Override
  protected CmmnActivityBehavior getActivityBehavior() {
    return new HumanTaskActivityBehavior();
  }

}
