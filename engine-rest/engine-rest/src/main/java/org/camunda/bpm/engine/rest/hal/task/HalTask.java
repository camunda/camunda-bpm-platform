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
package org.camunda.bpm.engine.rest.hal.task;

import java.util.Date;

import javax.ws.rs.core.UriBuilder;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.CaseDefinitionRestService;
import org.camunda.bpm.engine.rest.CaseExecutionRestService;
import org.camunda.bpm.engine.rest.CaseInstanceRestService;
import org.camunda.bpm.engine.rest.ExecutionRestService;
import org.camunda.bpm.engine.rest.IdentityRestService;
import org.camunda.bpm.engine.rest.ProcessDefinitionRestService;
import org.camunda.bpm.engine.rest.ProcessInstanceRestService;
import org.camunda.bpm.engine.rest.TaskRestService;
import org.camunda.bpm.engine.rest.UserRestService;
import org.camunda.bpm.engine.rest.hal.HalRelation;
import org.camunda.bpm.engine.rest.hal.HalResource;
import org.camunda.bpm.engine.rest.hal.identitylink.HalIdentityLink;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;

/**
 * @author Daniel Meyer
 *
 */
public class HalTask extends HalResource<HalTask> {

  public static HalRelation REL_SELF =
    HalRelation.build("self", TaskRestService.class, UriBuilder.fromPath(TaskRestService.PATH).path("{id}"));
  public static HalRelation REL_ASSIGNEE =
    HalRelation.build("assignee", UserRestService.class, UriBuilder.fromPath(UserRestService.PATH).path("{id}"));
  public static HalRelation REL_OWNER =
    HalRelation.build("owner", UserRestService.class, UriBuilder.fromPath(UserRestService.PATH).path("{id}"));
  public static HalRelation REL_EXECUTION =
    HalRelation.build("execution", ExecutionRestService.class, UriBuilder.fromPath(ExecutionRestService.PATH).path("{id}"));
  public static HalRelation REL_PARENT_TASK =
    HalRelation.build("parentTask", TaskRestService.class, UriBuilder.fromPath(TaskRestService.PATH).path("{id}"));
  public static HalRelation REL_PROCESS_DEFINITION =
    HalRelation.build("processDefinition", ProcessDefinitionRestService.class, UriBuilder.fromPath(ProcessDefinitionRestService.PATH).path("{id}"));
  public static HalRelation REL_PROCESS_INSTANCE =
    HalRelation.build("processInstance", ProcessInstanceRestService.class, UriBuilder.fromPath(ProcessInstanceRestService.PATH).path("{id}"));
  public static HalRelation REL_CASE_INSTANCE =
    HalRelation.build("caseInstance", CaseInstanceRestService.class, UriBuilder.fromPath(CaseInstanceRestService.PATH).path("{id}"));
  public static HalRelation REL_CASE_EXECUTION =
    HalRelation.build("caseExecution", CaseExecutionRestService.class, UriBuilder.fromPath(CaseExecutionRestService.PATH).path("{id}"));
  public static HalRelation REL_CASE_DEFINITION =
    HalRelation.build("caseDefinition", CaseDefinitionRestService.class, UriBuilder.fromPath(CaseDefinitionRestService.PATH).path("{id}"));
  public static HalRelation REL_IDENTITY_LINKS =
    HalRelation.build("identityLink", IdentityRestService.class, UriBuilder.fromPath(TaskRestService.PATH).path("{taskId}").path("identity-links"));

  private String id;
  private String name;
  private String assignee;
  private Date created;
  private Date due;
  private Date followUp;
  private DelegationState delegationState;
  private String description;
  private String executionId;
  private String owner;
  private String parentTaskId;
  private int priority;
  private String processDefinitionId;
  private String processInstanceId;
  private String taskDefinitionKey;
  private String caseExecutionId;
  private String caseInstanceId;
  private String caseDefinitionId;
  private boolean suspended;
  private String formKey;
  private String tenantId;

  public static HalTask generate(Task task, ProcessEngine engine) {
    return fromTask(task)
      .embed(HalTask.REL_PROCESS_DEFINITION, engine)
      .embed(HalTask.REL_CASE_DEFINITION, engine)
      .embed(HalTask.REL_IDENTITY_LINKS, engine)
      .embed(HalIdentityLink.REL_USER, engine)
      .embed(HalIdentityLink.REL_GROUP, engine);
  }

  public static HalTask fromTask(Task task) {
    HalTask dto = new HalTask();

    // task state
    dto.id = task.getId();
    dto.name = task.getName();
    dto.assignee = task.getAssignee();
    dto.created = task.getCreateTime();
    dto.due = task.getDueDate();
    dto.followUp = task.getFollowUpDate();
    dto.delegationState = task.getDelegationState();
    dto.description = task.getDescription();
    dto.executionId = task.getExecutionId();
    dto.owner = task.getOwner();
    dto.parentTaskId = task.getParentTaskId();
    dto.priority = task.getPriority();
    dto.processDefinitionId = task.getProcessDefinitionId();
    dto.processInstanceId = task.getProcessInstanceId();
    dto.taskDefinitionKey = task.getTaskDefinitionKey();
    dto.caseDefinitionId = task.getCaseDefinitionId();
    dto.caseExecutionId = task.getCaseExecutionId();
    dto.caseInstanceId = task.getCaseInstanceId();
    dto.suspended = task.isSuspended();
    dto.tenantId = task.getTenantId();
    try {
      dto.formKey = task.getFormKey();
    }
    catch (BadUserRequestException e) {
      // ignore (initializeFormKeys was not called)
    }

    // links
    dto.linker.createLink(REL_SELF, task.getId());
    dto.linker.createLink(REL_ASSIGNEE, task.getAssignee());
    dto.linker.createLink(REL_OWNER, task.getOwner());
    dto.linker.createLink(REL_EXECUTION,task.getExecutionId());
    dto.linker.createLink(REL_PARENT_TASK, task.getParentTaskId());
    dto.linker.createLink(REL_PROCESS_DEFINITION, task.getProcessDefinitionId());
    dto.linker.createLink(REL_PROCESS_INSTANCE, task.getProcessInstanceId());
    dto.linker.createLink(REL_CASE_INSTANCE, task.getCaseInstanceId());
    dto.linker.createLink(REL_CASE_EXECUTION, task.getCaseExecutionId());
    dto.linker.createLink(REL_CASE_DEFINITION, task.getCaseDefinitionId());
    dto.linker.createLink(REL_IDENTITY_LINKS, task.getId());

    return dto;
  }


  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getAssignee() {
    return assignee;
  }

  public Date getCreated() {
    return created;
  }

  public Date getDue() {
    return due;
  }

  public DelegationState getDelegationState() {
    return delegationState;
  }

  public String getDescription() {
    return description;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String getOwner() {
    return owner;
  }

  public String getParentTaskId() {
    return parentTaskId;
  }

  public int getPriority() {
    return priority;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }

  public Date getFollowUp() {
    return followUp;
  }

  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  public String getCaseExecutionId() {
    return caseExecutionId;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public boolean isSuspended() {
    return suspended;
  }

  public String getFormKey() {
    return formKey;
  }

  public String getTenantId() {
    return tenantId;
  }

}
