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
package org.camunda.bpm.engine.impl;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.cmd.AddCommentCmd;
import org.camunda.bpm.engine.impl.cmd.AddGroupIdentityLinkCmd;
import org.camunda.bpm.engine.impl.cmd.AddUserIdentityLinkCmd;
import org.camunda.bpm.engine.impl.cmd.AssignTaskCmd;
import org.camunda.bpm.engine.impl.cmd.ClaimTaskCmd;
import org.camunda.bpm.engine.impl.cmd.CompleteTaskCmd;
import org.camunda.bpm.engine.impl.cmd.CreateAttachmentCmd;
import org.camunda.bpm.engine.impl.cmd.CreateTaskCmd;
import org.camunda.bpm.engine.impl.cmd.DelegateTaskCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteAttachmentCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteGroupIdentityLinkCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteTaskAttachmentCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteTaskCmd;
import org.camunda.bpm.engine.impl.cmd.DeleteUserIdentityLinkCmd;
import org.camunda.bpm.engine.impl.cmd.GetAttachmentCmd;
import org.camunda.bpm.engine.impl.cmd.GetAttachmentContentCmd;
import org.camunda.bpm.engine.impl.cmd.GetIdentityLinksForTaskCmd;
import org.camunda.bpm.engine.impl.cmd.GetProcessInstanceAttachmentsCmd;
import org.camunda.bpm.engine.impl.cmd.GetProcessInstanceCommentsCmd;
import org.camunda.bpm.engine.impl.cmd.GetSubTasksCmd;
import org.camunda.bpm.engine.impl.cmd.GetTaskAttachmentCmd;
import org.camunda.bpm.engine.impl.cmd.GetTaskAttachmentContentCmd;
import org.camunda.bpm.engine.impl.cmd.GetTaskAttachmentsCmd;
import org.camunda.bpm.engine.impl.cmd.GetTaskCommentCmd;
import org.camunda.bpm.engine.impl.cmd.GetTaskCommentsCmd;
import org.camunda.bpm.engine.impl.cmd.GetTaskEventsCmd;
import org.camunda.bpm.engine.impl.cmd.GetTaskVariableCmd;
import org.camunda.bpm.engine.impl.cmd.GetTaskVariableCmdTyped;
import org.camunda.bpm.engine.impl.cmd.GetTaskVariablesCmd;
import org.camunda.bpm.engine.impl.cmd.PatchTaskVariablesCmd;
import org.camunda.bpm.engine.impl.cmd.RemoveTaskVariablesCmd;
import org.camunda.bpm.engine.impl.cmd.ResolveTaskCmd;
import org.camunda.bpm.engine.impl.cmd.SaveAttachmentCmd;
import org.camunda.bpm.engine.impl.cmd.SaveTaskCmd;
import org.camunda.bpm.engine.impl.cmd.SetTaskOwnerCmd;
import org.camunda.bpm.engine.impl.cmd.SetTaskPriorityCmd;
import org.camunda.bpm.engine.impl.cmd.SetTaskVariablesCmd;
import org.camunda.bpm.engine.impl.util.ExceptionUtil;
import org.camunda.bpm.engine.task.Attachment;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.Event;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.NativeTaskQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.task.TaskReport;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.value.TypedValue;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class TaskServiceImpl extends ServiceImpl implements TaskService {

  public Task newTask() {
    return newTask(null);
  }

  public Task newTask(String taskId) {
    return commandExecutor.execute(new CreateTaskCmd(taskId));
  }

  public void saveTask(Task task) {
    commandExecutor.execute(new SaveTaskCmd(task));
  }

  public void deleteTask(String taskId) {
    commandExecutor.execute(new DeleteTaskCmd(taskId, null, false));
  }

  public void deleteTasks(Collection<String> taskIds) {
    commandExecutor.execute(new DeleteTaskCmd(taskIds, null, false));
  }

  public void deleteTask(String taskId, boolean cascade) {
    commandExecutor.execute(new DeleteTaskCmd(taskId, null, cascade));
  }

  public void deleteTasks(Collection<String> taskIds, boolean cascade) {
    commandExecutor.execute(new DeleteTaskCmd(taskIds, null, cascade));
  }

  public void deleteTask(String taskId, String deleteReason) {
    commandExecutor.execute(new DeleteTaskCmd(taskId, deleteReason, false));
  }

  public void deleteTasks(Collection<String> taskIds, String deleteReason) {
    commandExecutor.execute(new DeleteTaskCmd(taskIds, deleteReason, false));
  }

  public void setAssignee(String taskId, String userId) {
    commandExecutor.execute(new AssignTaskCmd(taskId, userId));
  }

  public void setOwner(String taskId, String userId) {
    commandExecutor.execute(new SetTaskOwnerCmd(taskId, userId));
  }

  public void addCandidateUser(String taskId, String userId) {
    commandExecutor.execute(new AddUserIdentityLinkCmd(taskId, userId, IdentityLinkType.CANDIDATE));
  }

  public void addCandidateGroup(String taskId, String groupId) {
    commandExecutor.execute(new AddGroupIdentityLinkCmd(taskId, groupId, IdentityLinkType.CANDIDATE));
  }

  public void addUserIdentityLink(String taskId, String userId, String identityLinkType) {
    commandExecutor.execute(new AddUserIdentityLinkCmd(taskId, userId, identityLinkType));
  }

  public void addGroupIdentityLink(String taskId, String groupId, String identityLinkType) {
    commandExecutor.execute(new AddGroupIdentityLinkCmd(taskId, groupId, identityLinkType));
  }

  public void deleteCandidateGroup(String taskId, String groupId) {
    commandExecutor.execute(new DeleteGroupIdentityLinkCmd(taskId, groupId, IdentityLinkType.CANDIDATE));
  }

  public void deleteCandidateUser(String taskId, String userId) {
    commandExecutor.execute(new DeleteUserIdentityLinkCmd(taskId, userId, IdentityLinkType.CANDIDATE));
  }

  public void deleteGroupIdentityLink(String taskId, String groupId, String identityLinkType) {
    commandExecutor.execute(new DeleteGroupIdentityLinkCmd(taskId, groupId, identityLinkType));
  }

  public void deleteUserIdentityLink(String taskId, String userId, String identityLinkType) {
    commandExecutor.execute(new DeleteUserIdentityLinkCmd(taskId, userId, identityLinkType));
  }

  public List<IdentityLink> getIdentityLinksForTask(String taskId) {
    return commandExecutor.execute(new GetIdentityLinksForTaskCmd(taskId));
  }

  public void claim(String taskId, String userId) {
    commandExecutor.execute(new ClaimTaskCmd(taskId, userId));
  }

  public void complete(String taskId) {
    commandExecutor.execute(new CompleteTaskCmd(taskId, null));
  }

  public void complete(String taskId, Map<String, Object> variables) {
    commandExecutor.execute(new CompleteTaskCmd(taskId, variables));
  }

  public void delegateTask(String taskId, String userId) {
    commandExecutor.execute(new DelegateTaskCmd(taskId, userId));
  }

  public void resolveTask(String taskId) {
    commandExecutor.execute(new ResolveTaskCmd(taskId, null));
  }

  public void resolveTask(String taskId, Map<String, Object> variables) {
    commandExecutor.execute(new ResolveTaskCmd(taskId, variables));
  }

  public void setPriority(String taskId, int priority) {
    commandExecutor.execute(new SetTaskPriorityCmd(taskId, priority) );
  }

  public TaskQuery createTaskQuery() {
    return new TaskQueryImpl(commandExecutor);
  }

  public NativeTaskQuery createNativeTaskQuery() {
    return new NativeTaskQueryImpl(commandExecutor);
  }

  public VariableMap getVariables(String executionId) {
    return getVariablesTyped(executionId);
  }

  public VariableMap getVariablesTyped(String executionId) {
    return getVariablesTyped(executionId, true);
  }

  public VariableMap getVariablesTyped(String taskId, boolean deserializeValues) {
    return commandExecutor.execute(new GetTaskVariablesCmd(taskId, null, false, deserializeValues));
  }

  public VariableMap getVariablesLocal(String taskId) {
    return getVariablesLocalTyped(taskId);
  }

  public VariableMap getVariablesLocalTyped(String taskId) {
    return getVariablesLocalTyped(taskId, true);
  }

  public VariableMap getVariablesLocalTyped(String taskId, boolean deserializeValues) {
    return commandExecutor.execute(new GetTaskVariablesCmd(taskId, null, true, deserializeValues));
  }

  public VariableMap getVariables(String executionId, Collection<String> variableNames) {
    return getVariablesTyped(executionId, variableNames, true);
  }

  public VariableMap getVariablesTyped(String executionId, Collection<String> variableNames, boolean deserializeValues) {
    return commandExecutor.execute(new GetTaskVariablesCmd(executionId, variableNames, false, deserializeValues));
  }

  public VariableMap getVariablesLocal(String executionId, Collection<String> variableNames) {
    return getVariablesLocalTyped(executionId, variableNames, true);
  }

  public VariableMap getVariablesLocalTyped(String executionId, Collection<String> variableNames, boolean deserializeValues) {
    return commandExecutor.execute(new GetTaskVariablesCmd(executionId, variableNames, true, deserializeValues));
  }

  public Object getVariable(String executionId, String variableName) {
    return commandExecutor.execute(new GetTaskVariableCmd(executionId, variableName, false));
  }

  public Object getVariableLocal(String executionId, String variableName) {
    return commandExecutor.execute(new GetTaskVariableCmd(executionId, variableName, true));
  }

  public <T extends TypedValue> T getVariableTyped(String taskId, String variableName) {
    return getVariableTyped(taskId, variableName, false, true);
  }

  public <T extends TypedValue> T getVariableTyped(String taskId, String variableName, boolean deserializeValue) {
    return getVariableTyped(taskId, variableName, false, deserializeValue);
  }

  public <T extends TypedValue> T getVariableLocalTyped(String taskId, String variableName) {
    return getVariableTyped(taskId, variableName, true, true);
  }

  public <T extends TypedValue> T getVariableLocalTyped(String taskId, String variableName, boolean deserializeValue) {
    return getVariableTyped(taskId, variableName, true, deserializeValue);
  }

  @SuppressWarnings("unchecked")
  protected <T extends TypedValue> T getVariableTyped(String taskId, String variableName, boolean isLocal, boolean deserializeValue) {
    return (T) commandExecutor.execute(new GetTaskVariableCmdTyped(taskId, variableName, isLocal, deserializeValue));
  }

  public void setVariable(String executionId, String variableName, Object value) {
    ensureNotNull("variableName", variableName);
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(variableName, value);
    setVariables(executionId, variables, false);
  }

  public void setVariableLocal(String executionId, String variableName, Object value) {
    ensureNotNull("variableName", variableName);
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put(variableName, value);
    setVariables(executionId, variables, true);
  }

  public void setVariables(String executionId, Map<String, ? extends Object> variables) {
    setVariables(executionId, variables, false);
  }

  public void setVariablesLocal(String executionId, Map<String, ? extends Object> variables) {
    setVariables(executionId, variables, true);
  }

  protected void setVariables(String executionId, Map<String, ? extends Object> variables, boolean local) {
    try {
      commandExecutor.execute(new SetTaskVariablesCmd(executionId, variables, local));
    } catch (ProcessEngineException ex) {
      if (ExceptionUtil.checkValueTooLongException(ex)) {
        throw new BadUserRequestException("Variable value is too long", ex);
      }
      throw ex;
    }
  }

  public void updateVariablesLocal(String taskId, Map<String, ? extends Object> modifications, Collection<String> deletions) {
    updateVariables(taskId, modifications, deletions, true);
  }

  public void updateVariables(String taskId, Map<String, ? extends Object> modifications, Collection<String> deletions) {
    updateVariables(taskId, modifications, deletions, false);
  }

  protected void updateVariables(String taskId, Map<String, ? extends Object> modifications, Collection<String> deletions, boolean local) {
    try {
      commandExecutor.execute(new PatchTaskVariablesCmd(taskId, modifications, deletions, local));
    } catch (ProcessEngineException ex) {
      if (ExceptionUtil.checkValueTooLongException(ex)) {
        throw new BadUserRequestException("Variable value is too long", ex);
      }
      throw ex;
    }
  }

  public void removeVariable(String taskId, String variableName) {
    Collection<String> variableNames = new ArrayList<String>();
    variableNames.add(variableName);
    commandExecutor.execute(new RemoveTaskVariablesCmd(taskId, variableNames, false));
  }

  public void removeVariableLocal(String taskId, String variableName) {
    Collection<String> variableNames = new ArrayList<String>(1);
    variableNames.add(variableName);
    commandExecutor.execute(new RemoveTaskVariablesCmd(taskId, variableNames, true));
  }

  public void removeVariables(String taskId, Collection<String> variableNames) {
    commandExecutor.execute(new RemoveTaskVariablesCmd(taskId, variableNames, false));
  }

  public void removeVariablesLocal(String taskId, Collection<String> variableNames) {
    commandExecutor.execute(new RemoveTaskVariablesCmd(taskId, variableNames, true));
  }

  public void addComment(String taskId, String processInstance, String message) {
    createComment(taskId, processInstance, message);
  }

  public Comment createComment(String taskId, String processInstance, String message) {
    return commandExecutor.execute(new AddCommentCmd(taskId, processInstance, message));
  }

  public List<Comment> getTaskComments(String taskId) {
    return commandExecutor.execute(new GetTaskCommentsCmd(taskId));
  }

  public Comment getTaskComment(String taskId, String commentId) {
    return commandExecutor.execute(new GetTaskCommentCmd(taskId, commentId));
  }

  public List<Event> getTaskEvents(String taskId) {
    return commandExecutor.execute(new GetTaskEventsCmd(taskId));
  }

  public List<Comment> getProcessInstanceComments(String processInstanceId) {
    return commandExecutor.execute(new GetProcessInstanceCommentsCmd(processInstanceId));
  }

  public Attachment createAttachment(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, InputStream content) {
    return commandExecutor.execute(new CreateAttachmentCmd(attachmentType, taskId, processInstanceId, attachmentName, attachmentDescription, content, null));
  }

  public Attachment createAttachment(String attachmentType, String taskId, String processInstanceId, String attachmentName, String attachmentDescription, String url) {
    return commandExecutor.execute(new CreateAttachmentCmd(attachmentType, taskId, processInstanceId, attachmentName, attachmentDescription, null, url));
  }

  public InputStream getAttachmentContent(String attachmentId) {
    return commandExecutor.execute(new GetAttachmentContentCmd(attachmentId));
  }

  public InputStream getTaskAttachmentContent(String taskId, String attachmentId) {
    return commandExecutor.execute(new GetTaskAttachmentContentCmd(taskId, attachmentId));
  }

  public void deleteAttachment(String attachmentId) {
    commandExecutor.execute(new DeleteAttachmentCmd(attachmentId));
  }

  public void deleteTaskAttachment(String taskId, String attachmentId) {
    commandExecutor.execute(new DeleteTaskAttachmentCmd(taskId, attachmentId));
  }

  public Attachment getAttachment(String attachmentId) {
    return commandExecutor.execute(new GetAttachmentCmd(attachmentId));
  }

  public Attachment getTaskAttachment(String taskId, String attachmentId) {
    return commandExecutor.execute(new GetTaskAttachmentCmd(taskId, attachmentId));
  }

  public List<Attachment> getTaskAttachments(String taskId) {
    return commandExecutor.execute(new GetTaskAttachmentsCmd(taskId));
  }

  public List<Attachment> getProcessInstanceAttachments(String processInstanceId) {
    return commandExecutor.execute(new GetProcessInstanceAttachmentsCmd(processInstanceId));
  }

  public void saveAttachment(Attachment attachment) {
    commandExecutor.execute(new SaveAttachmentCmd(attachment));
  }

  public List<Task> getSubTasks(String parentTaskId) {
    return commandExecutor.execute(new GetSubTasksCmd(parentTaskId));
  }

  public TaskReport createTaskReport() {
    return new TaskReportImpl(commandExecutor);
  }

}
