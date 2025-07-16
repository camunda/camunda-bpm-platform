package org.camunda.bpm.engine.test.bpmn.tasklistener.util;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.task.IdentityLinkType;

public class TaskCreateListenerAddAndDelete  implements TaskListener {

  @Override
  public void notify(DelegateTask delegateTask) {
    delegateTask.setDescription("TaskCreateListener is listening!");

    var taskService = delegateTask.getProcessEngineServices().getTaskService();
    taskService.addCandidateUser(delegateTask.getId(), "aCandidateUserId");
    taskService.deleteCandidateUser(delegateTask.getId(), "aCandidateUserId");

    taskService.addCandidateGroup(delegateTask.getId(), "aCandidateGroupId");
    taskService.deleteCandidateGroup(delegateTask.getId(), "aCandidateGroupId");

    taskService.addUserIdentityLink(delegateTask.getId(), "anAssigneeUserId", IdentityLinkType.ASSIGNEE);
    taskService.deleteUserIdentityLink(delegateTask.getId(), "anAssigneeUserId", IdentityLinkType.ASSIGNEE);

    taskService.addGroupIdentityLink(delegateTask.getId(), "anotherCandidateGroupId", IdentityLinkType.CANDIDATE);
    taskService.deleteGroupIdentityLink(delegateTask.getId(), "anotherCandidateGroupId", IdentityLinkType.CANDIDATE);

    var runTimeService = delegateTask.getProcessEngineServices().getRuntimeService();
    runTimeService.setVariable(delegateTask.getExecutionId(), "aVarName", "aVarValue");
    runTimeService.removeVariable(delegateTask.getExecutionId(), "aVarName");

  }
}
