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
package org.camunda.bpm.engine.rest.dto.task;

import static java.lang.Boolean.TRUE;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.QueryEntityRelationCondition;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.TaskQueryProperty;
import org.camunda.bpm.engine.impl.TaskQueryVariableValue;
import org.camunda.bpm.engine.impl.VariableInstanceQueryProperty;
import org.camunda.bpm.engine.impl.VariableOrderProperty;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.query.QueryProperty;
import org.camunda.bpm.engine.rest.dto.AbstractQueryDto;
import org.camunda.bpm.engine.rest.dto.CamundaQueryParam;
import org.camunda.bpm.engine.rest.dto.SortingDto;
import org.camunda.bpm.engine.rest.dto.VariableQueryParameterDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.converter.BooleanConverter;
import org.camunda.bpm.engine.rest.dto.converter.DateConverter;
import org.camunda.bpm.engine.rest.dto.converter.DelegationStateConverter;
import org.camunda.bpm.engine.rest.dto.converter.IntegerConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringArrayConverter;
import org.camunda.bpm.engine.rest.dto.converter.StringListConverter;
import org.camunda.bpm.engine.rest.dto.converter.VariableListConverter;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.engine.variable.type.ValueType;
import org.camunda.bpm.engine.variable.type.ValueTypeResolver;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_NULL)
public class TaskQueryDto extends AbstractQueryDto<TaskQuery> {

  public static final String SORT_BY_PROCESS_INSTANCE_ID_VALUE = "instanceId";
  public static final String SORT_BY_CASE_INSTANCE_ID_VALUE = "caseInstanceId";
  public static final String SORT_BY_DUE_DATE_VALUE = "dueDate";
  public static final String SORT_BY_FOLLOW_UP_VALUE = "followUpDate";
  public static final String SORT_BY_EXECUTION_ID_VALUE = "executionId";
  public static final String SORT_BY_CASE_EXECUTION_ID_VALUE = "caseExecutionId";
  public static final String SORT_BY_ASSIGNEE_VALUE = "assignee";
  public static final String SORT_BY_CREATE_TIME_VALUE = "created";
  public static final String SORT_BY_DESCRIPTION_VALUE = "description";
  public static final String SORT_BY_ID_VALUE = "id";
  public static final String SORT_BY_NAME_VALUE = "name";
  public static final String SORT_BY_NAME_CASE_INSENSITIVE_VALUE = "nameCaseInsensitive";
  public static final String SORT_BY_PRIORITY_VALUE = "priority";
  public static final String SORT_BY_TENANT_ID_VALUE = "tenantId";

  public static final String SORT_BY_PROCESS_VARIABLE = "processVariable";
  public static final String SORT_BY_EXECUTION_VARIABLE = "executionVariable";
  public static final String SORT_BY_TASK_VARIABLE = "taskVariable";
  public static final String SORT_BY_CASE_INSTANCE_VARIABLE = "caseInstanceVariable";
  public static final String SORT_BY_CASE_EXECUTION_VARIABLE = "caseExecutionVariable";

  public static final List<String> VALID_SORT_BY_VALUES;
  static {
    VALID_SORT_BY_VALUES = new ArrayList<String>();
    VALID_SORT_BY_VALUES.add(SORT_BY_PROCESS_INSTANCE_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_CASE_INSTANCE_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_DUE_DATE_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_FOLLOW_UP_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_EXECUTION_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_CASE_EXECUTION_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_ASSIGNEE_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_CREATE_TIME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_DESCRIPTION_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_ID_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_NAME_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_NAME_CASE_INSENSITIVE_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_PRIORITY_VALUE);
    VALID_SORT_BY_VALUES.add(SORT_BY_TENANT_ID_VALUE);
  }

  public static final String SORT_PARAMETERS_VARIABLE_NAME = "variable";
  public static final String SORT_PARAMETERS_VALUE_TYPE = "type";

  private String processInstanceBusinessKey;
  private String processInstanceBusinessKeyExpression;
  private String[] processInstanceBusinessKeyIn;
  private String processInstanceBusinessKeyLike;
  private String processInstanceBusinessKeyLikeExpression;
  private String processDefinitionKey;
  private String[] processDefinitionKeyIn;
  private String processDefinitionId;
  private String executionId;
  private String[] activityInstanceIdIn;
  private String processDefinitionName;
  private String processDefinitionNameLike;
  private String processInstanceId;
  private String assignee;
  private String assigneeExpression;
  private String assigneeLike;
  private String assigneeLikeExpression;
  private String candidateGroup;
  private String candidateGroupExpression;
  private String candidateUser;
  private String candidateUserExpression;
  private Boolean includeAssignedTasks;
  private String taskDefinitionKey;
  private String[] taskDefinitionKeyIn;
  private String taskDefinitionKeyLike;
  private String description;
  private String descriptionLike;
  private String involvedUser;
  private String involvedUserExpression;
  private Integer maxPriority;
  private Integer minPriority;
  private String name;
  private String nameNotEqual;
  private String nameLike;
  private String nameNotLike;
  private String owner;
  private String ownerExpression;
  private Integer priority;
  private String parentTaskId;
  protected Boolean assigned;
  private Boolean unassigned;
  private Boolean active;
  private Boolean suspended;

  private String caseDefinitionKey;
  private String caseDefinitionId;
  private String caseDefinitionName;
  private String caseDefinitionNameLike;
  private String caseInstanceId;
  private String caseInstanceBusinessKey;
  private String caseInstanceBusinessKeyLike;
  private String caseExecutionId;

  private Date dueAfter;
  private String dueAfterExpression;
  private Date dueBefore;
  private String dueBeforeExpression;
  private Date dueDate;
  private String dueDateExpression;
  private Date followUpAfter;
  private String followUpAfterExpression;
  private Date followUpBefore;
  private String followUpBeforeExpression;
  private Date followUpBeforeOrNotExistent;
  private String followUpBeforeOrNotExistentExpression;
  private Date followUpDate;
  private String followUpDateExpression;
  private Date createdAfter;
  private String createdAfterExpression;
  private Date createdBefore;
  private String createdBeforeExpression;
  private Date createdOn;
  private String createdOnExpression;

  private String delegationState;

  private String[] tenantIdIn;
  private Boolean withoutTenantId;

  private List<String> candidateGroups;
  private String candidateGroupsExpression;
  protected Boolean withCandidateGroups;
  protected Boolean withoutCandidateGroups;
  protected Boolean withCandidateUsers;
  protected Boolean withoutCandidateUsers;

  private List<VariableQueryParameterDto> taskVariables;
  private List<VariableQueryParameterDto> processVariables;
  private List<VariableQueryParameterDto> caseInstanceVariables;

  private List<TaskQueryDto> orQueries;

  public TaskQueryDto() {

  }

  public TaskQueryDto(ObjectMapper objectMapper, MultivaluedMap<String, String> queryParameters) {
    super(objectMapper, queryParameters);
  }

  @CamundaQueryParam("orQueries")
  public void setOrQueries(List<TaskQueryDto> orQueries) {
    this.orQueries = orQueries;
  }

  @CamundaQueryParam("processInstanceBusinessKey")
  public void setProcessInstanceBusinessKey(String businessKey) {
    this.processInstanceBusinessKey = businessKey;
  }

  @CamundaQueryParam("processInstanceBusinessKeyExpression")
  public void setProcessInstanceBusinessKeyExpression(String businessKeyExpression) {
    this.processInstanceBusinessKeyExpression = businessKeyExpression;
  }

  @CamundaQueryParam(value = "processInstanceBusinessKeyIn", converter = StringArrayConverter.class)
  public void setProcessInstanceBusinessKeyIn(String[] processInstanceBusinessKeyIn) {
    this.processInstanceBusinessKeyIn = processInstanceBusinessKeyIn;
  }

  @CamundaQueryParam("processInstanceBusinessKeyLike")
  public void setProcessInstanceBusinessKeyLike(String businessKeyLike) {
    this.processInstanceBusinessKeyLike = businessKeyLike;
  }

  @CamundaQueryParam("processInstanceBusinessKeyLikeExpression")
  public void setProcessInstanceBusinessKeyLikeExpression(String businessKeyLikeExpression) {
    this.processInstanceBusinessKeyLikeExpression = businessKeyLikeExpression;
  }

  @CamundaQueryParam("processDefinitionKey")
  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionKey = processDefinitionKey;
  }

  @CamundaQueryParam(value = "processDefinitionKeyIn", converter = StringArrayConverter.class)
  public void setProcessDefinitionKeyIn(String[] processDefinitionKeyIn) {
    this.processDefinitionKeyIn = processDefinitionKeyIn;
  }

  @CamundaQueryParam("processDefinitionId")
  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  @CamundaQueryParam("executionId")
  public void setExecutionId(String executionId) {
    this.executionId = executionId;
  }

  @CamundaQueryParam(value="activityInstanceIdIn", converter = StringArrayConverter.class)
  public void setActivityInstanceIdIn(String[] activityInstanceIdIn) {
    this.activityInstanceIdIn = activityInstanceIdIn;
  }

  @CamundaQueryParam(value="tenantIdIn", converter = StringArrayConverter.class)
  public void setTenantIdIn(String[] tenantIdIn) {
    this.tenantIdIn = tenantIdIn;
  }

  @CamundaQueryParam(value = "withoutTenantId", converter = BooleanConverter.class)
  public void setWithoutTenantId(Boolean withoutTenantId) {
    this.withoutTenantId = withoutTenantId;
  }

  @CamundaQueryParam("processDefinitionName")
  public void setProcessDefinitionName(String processDefinitionName) {
    this.processDefinitionName = processDefinitionName;
  }

  @CamundaQueryParam("processDefinitionNameLike")
  public void setProcessDefinitionNameLike(String processDefinitionNameLike) {
    this.processDefinitionNameLike = processDefinitionNameLike;
  }

  @CamundaQueryParam("processInstanceId")
  public void setProcessInstanceId(String processInstanceId) {
    this.processInstanceId = processInstanceId;
  }

  @CamundaQueryParam("assignee")
  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  @CamundaQueryParam("assigneeExpression")
  public void setAssigneeExpression(String assigneeExpression) {
    this.assigneeExpression = assigneeExpression;
  }

  @CamundaQueryParam("assigneeLike")
  public void setAssigneeLike(String assigneeLike) {
    this.assigneeLike = assigneeLike;
  }

  @CamundaQueryParam("assigneeLikeExpression")
  public void setAssigneeLikeExpression(String assigneeLikeExpression) {
    this.assigneeLikeExpression = assigneeLikeExpression;
  }

  @CamundaQueryParam("candidateGroup")
  public void setCandidateGroup(String candidateGroup) {
    this.candidateGroup = candidateGroup;
  }

  @CamundaQueryParam("candidateGroupExpression")
  public void setCandidateGroupExpression(String candidateGroupExpression) {
    this.candidateGroupExpression = candidateGroupExpression;
  }

  @CamundaQueryParam(value = "withCandidateGroups", converter = BooleanConverter.class)
  public void setWithCandidateGroups(Boolean withCandidateGroups) {
    this.withCandidateGroups = withCandidateGroups;
  }

  @CamundaQueryParam(value = "withoutCandidateGroups", converter = BooleanConverter.class)
  public void setWithoutCandidateGroups(Boolean withoutCandidateGroups) {
    this.withoutCandidateGroups = withoutCandidateGroups;
  }

  @CamundaQueryParam(value = "withCandidateUsers", converter = BooleanConverter.class)
  public void setWithCandidateUsers(Boolean withCandidateUsers) {
    this.withCandidateUsers = withCandidateUsers;
  }

  @CamundaQueryParam(value = "withoutCandidateUsers", converter = BooleanConverter.class)
  public void setWithoutCandidateUsers(Boolean withoutCandidateUsers) {
    this.withoutCandidateUsers = withoutCandidateUsers;
  }

  @CamundaQueryParam("candidateUser")
  public void setCandidateUser(String candidateUser) {
    this.candidateUser = candidateUser;
  }

  @CamundaQueryParam("candidateUserExpression")
  public void setCandidateUserExpression(String candidateUserExpression) {
    this.candidateUserExpression = candidateUserExpression;
  }

  @CamundaQueryParam(value = "includeAssignedTasks", converter = BooleanConverter.class)
  public void setIncludeAssignedTasks(Boolean includeAssignedTasks){
    this.includeAssignedTasks = includeAssignedTasks;
  }

  @CamundaQueryParam("taskDefinitionKey")
  public void setTaskDefinitionKey(String taskDefinitionKey) {
    this.taskDefinitionKey = taskDefinitionKey;
  }

  @CamundaQueryParam(value = "taskDefinitionKeyIn", converter= StringArrayConverter.class)
  public void setTaskDefinitionKeyIn(String[] taskDefinitionKeyIn) {
    this.taskDefinitionKeyIn = taskDefinitionKeyIn;
  }

  @CamundaQueryParam("taskDefinitionKeyLike")
  public void setTaskDefinitionKeyLike(String taskDefinitionKeyLike) {
    this.taskDefinitionKeyLike = taskDefinitionKeyLike;
  }

  @CamundaQueryParam("description")
  public void setDescription(String description) {
    this.description = description;
  }

  @CamundaQueryParam("descriptionLike")
  public void setDescriptionLike(String descriptionLike) {
    this.descriptionLike = descriptionLike;
  }

  @CamundaQueryParam("involvedUser")
  public void setInvolvedUser(String involvedUser) {
    this.involvedUser = involvedUser;
  }

  @CamundaQueryParam("involvedUserExpression")
  public void setInvolvedUserExpression(String involvedUserExpression) {
    this.involvedUserExpression = involvedUserExpression;
  }

  @CamundaQueryParam(value = "maxPriority", converter = IntegerConverter.class)
  public void setMaxPriority(Integer maxPriority) {
    this.maxPriority = maxPriority;
  }

  @CamundaQueryParam(value = "minPriority", converter = IntegerConverter.class)
  public void setMinPriority(Integer minPriority) {
    this.minPriority = minPriority;
  }

  @CamundaQueryParam("name")
  public void setName(String name) {
    this.name = name;
  }

  @CamundaQueryParam("nameNotEqual")
  public void setNameNotEqual(String nameNotEqual) {
    this.nameNotEqual = nameNotEqual;
  }

  @CamundaQueryParam("nameLike")
  public void setNameLike(String nameLike) {
    this.nameLike = nameLike;
  }

  @CamundaQueryParam("nameNotLike")
  public void setNameNotLike(String nameNotLike) {
    this.nameNotLike = nameNotLike;
  }

  @CamundaQueryParam("owner")
  public void setOwner(String owner) {
    this.owner = owner;
  }

  @CamundaQueryParam("ownerExpression")
  public void setOwnerExpression(String ownerExpression) {
    this.ownerExpression = ownerExpression;
  }

  @CamundaQueryParam(value = "priority", converter = IntegerConverter.class)
  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  @CamundaQueryParam("parentTaskId")
  public void setParentTaskId(String parentTaskId) {
    this.parentTaskId = parentTaskId;
  }

  @CamundaQueryParam(value = "assigned", converter = BooleanConverter.class)
  public void setAssigned(Boolean assigned) {
    this.assigned = assigned;
  }

  @CamundaQueryParam(value = "unassigned", converter = BooleanConverter.class)
  public void setUnassigned(Boolean unassigned) {
    this.unassigned = unassigned;
  }

  @CamundaQueryParam(value = "active", converter = BooleanConverter.class)
  public void setActive(Boolean active) {
    this.active = active;
  }

  @CamundaQueryParam(value = "suspended", converter = BooleanConverter.class)
  public void setSuspended(Boolean suspended) {
    this.suspended = suspended;
  }

  @CamundaQueryParam(value = "dueAfter", converter = DateConverter.class)
  public void setDueAfter(Date dueAfter) {
    this.dueAfter = dueAfter;
  }

  @CamundaQueryParam(value = "dueAfterExpression")
  public void setDueAfterExpression(String dueAfterExpression) {
    this.dueAfterExpression = dueAfterExpression;
  }

  @CamundaQueryParam(value = "dueBefore", converter = DateConverter.class)
  public void setDueBefore(Date dueBefore) {
    this.dueBefore = dueBefore;
  }

  @CamundaQueryParam(value = "dueBeforeExpression")
  public void setDueBeforeExpression(String dueBeforeExpression) {
    this.dueBeforeExpression = dueBeforeExpression;
  }

  @CamundaQueryParam(value = "dueDate", converter = DateConverter.class)
  public void setDueDate(Date dueDate) {
    this.dueDate = dueDate;
  }

  @Deprecated
  @CamundaQueryParam(value = "due", converter = DateConverter.class)
  public void setDue(Date dueDate) {
    this.dueDate = dueDate;
  }

  @CamundaQueryParam(value = "dueDateExpression")
  public void setDueDateExpression(String dueDateExpression) {
    this.dueDateExpression = dueDateExpression;
  }

  @CamundaQueryParam(value = "followUpAfter", converter = DateConverter.class)
  public void setFollowUpAfter(Date followUpAfter) {
    this.followUpAfter = followUpAfter;
  }

  @CamundaQueryParam(value = "followUpAfterExpression")
  public void setFollowUpAfterExpression(String followUpAfterExpression) {
    this.followUpAfterExpression = followUpAfterExpression;
  }

  @CamundaQueryParam(value = "followUpBefore", converter = DateConverter.class)
  public void setFollowUpBefore(Date followUpBefore) {
    this.followUpBefore = followUpBefore;
  }

  @CamundaQueryParam(value = "followUpBeforeOrNotExistentExpression")
  public void setFollowUpBeforeOrNotExistentExpression(String followUpBeforeExpression) {
    this.followUpBeforeOrNotExistentExpression = followUpBeforeExpression;
  }

  @CamundaQueryParam(value = "followUpBeforeOrNotExistent", converter = DateConverter.class)
  public void setFollowUpBeforeOrNotExistent(Date followUpBefore) {
    this.followUpBeforeOrNotExistent = followUpBefore;
  }

  @CamundaQueryParam(value = "followUpBeforeExpression")
  public void setFollowUpBeforeExpression(String followUpBeforeExpression) {
    this.followUpBeforeExpression = followUpBeforeExpression;
  }

  @CamundaQueryParam(value = "followUpDate", converter = DateConverter.class)
  public void setFollowUpDate(Date followUpDate) {
    this.followUpDate = followUpDate;
  }

  @Deprecated
  @CamundaQueryParam(value = "followUp", converter = DateConverter.class)
  public void setFollowUp(Date followUpDate) {
    this.followUpDate = followUpDate;
  }

  @CamundaQueryParam(value = "followUpDateExpression")
  public void setFollowUpDateExpression(String followUpDateExpression) {
    this.followUpDateExpression = followUpDateExpression;
  }

  @CamundaQueryParam(value = "createdAfter", converter = DateConverter.class)
  public void setCreatedAfter(Date createdAfter) {
    this.createdAfter = createdAfter;
  }

  @CamundaQueryParam(value = "createdAfterExpression")
  public void setCreatedAfterExpression(String createdAfterExpression) {
    this.createdAfterExpression = createdAfterExpression;
  }

  @CamundaQueryParam(value = "createdBefore", converter = DateConverter.class)
  public void setCreatedBefore(Date createdBefore) {
    this.createdBefore = createdBefore;
  }

  @CamundaQueryParam(value = "createdBeforeExpression")
  public void setCreatedBeforeExpression(String createdBeforeExpression) {
    this.createdBeforeExpression = createdBeforeExpression;
  }

  @CamundaQueryParam(value = "createdOn", converter = DateConverter.class)
  public void setCreatedOn(Date createdOn) {
    this.createdOn = createdOn;
  }

  @Deprecated
  @CamundaQueryParam(value = "created", converter = DateConverter.class)
  public void setCreated(Date createdOn) {
    this.createdOn = createdOn;
  }

  @CamundaQueryParam(value = "createdOnExpression")
  public void setCreatedOnExpression(String createdOnExpression) {
    this.createdOnExpression = createdOnExpression;
  }

  @CamundaQueryParam(value = "delegationState")
  public void setDelegationState(String taskDelegationState) {
    this.delegationState = taskDelegationState;
  }

  @CamundaQueryParam(value = "candidateGroups", converter = StringListConverter.class)
  public void setCandidateGroups(List<String> candidateGroups) {
    this.candidateGroups = candidateGroups;
  }

  @CamundaQueryParam(value = "candidateGroupsExpression")
  public void setCandidateGroupsExpression(String candidateGroupsExpression) {
    this.candidateGroupsExpression = candidateGroupsExpression;
  }

  @CamundaQueryParam(value = "taskVariables", converter = VariableListConverter.class)
  public void setTaskVariables(List<VariableQueryParameterDto> taskVariables) {
    this.taskVariables = taskVariables;
  }

  @CamundaQueryParam(value = "processVariables", converter = VariableListConverter.class)
  public void setProcessVariables(List<VariableQueryParameterDto> processVariables) {
    this.processVariables = processVariables;
  }

  @CamundaQueryParam("caseDefinitionId")
  public void setCaseDefinitionId(String caseDefinitionId) {
    this.caseDefinitionId = caseDefinitionId;
  }

  @CamundaQueryParam("caseDefinitionKey")
  public void setCaseDefinitionKey(String caseDefinitionKey) {
    this.caseDefinitionKey = caseDefinitionKey;
  }

  @CamundaQueryParam("caseDefinitionName")
  public void setCaseDefinitionName(String caseDefinitionName) {
    this.caseDefinitionName = caseDefinitionName;
  }

  @CamundaQueryParam("caseDefinitionNameLike")
  public void setCaseDefinitionNameLike(String caseDefinitionNameLike) {
    this.caseDefinitionNameLike = caseDefinitionNameLike;
  }

  @CamundaQueryParam("caseExecutionId")
  public void setCaseExecutionId(String caseExecutionId) {
    this.caseExecutionId = caseExecutionId;
  }

  @CamundaQueryParam("caseInstanceBusinessKey")
  public void setCaseInstanceBusinessKey(String caseInstanceBusinessKey) {
    this.caseInstanceBusinessKey = caseInstanceBusinessKey;
  }

  @CamundaQueryParam("caseInstanceBusinessKeyLike")
  public void setCaseInstanceBusinessKeyLike(String caseInstanceBusinessKeyLike) {
    this.caseInstanceBusinessKeyLike = caseInstanceBusinessKeyLike;
  }

  @CamundaQueryParam("caseInstanceId")
  public void setCaseInstanceId(String caseInstanceId) {
    this.caseInstanceId = caseInstanceId;
  }

  @CamundaQueryParam(value = "caseInstanceVariables", converter = VariableListConverter.class)
  public void setCaseInstanceVariables(List<VariableQueryParameterDto> caseInstanceVariables) {
    this.caseInstanceVariables = caseInstanceVariables;
  }

  @Override
  protected boolean isValidSortByValue(String value) {
    return VALID_SORT_BY_VALUES.contains(value);
  }

  @Override
  protected TaskQuery createNewQuery(ProcessEngine engine) {
    return engine.getTaskService().createTaskQuery();
  }

  public String getProcessInstanceBusinessKey() {
    return processInstanceBusinessKey;
  }

  public String getProcessInstanceBusinessKeyExpression() {
    return processInstanceBusinessKeyExpression;
  }

  public String[] getProcessInstanceBusinessKeyIn() {
    return processInstanceBusinessKeyIn;
  }

  public String getProcessInstanceBusinessKeyLike() {
    return processInstanceBusinessKeyLike;
  }

  public String getProcessInstanceBusinessKeyLikeExpression() {
    return processInstanceBusinessKeyLikeExpression;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionKey;
  }

  public String[] getProcessDefinitionKeyIn() {
    return processDefinitionKeyIn;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getExecutionId() {
    return executionId;
  }

  public String[] getActivityInstanceIdIn() {
    return activityInstanceIdIn;
  }

  public String[] getTenantIdIn() {
    return tenantIdIn;
  }

  public Boolean getWithoutTenantId() {
    return withoutTenantId;
  }

  public String getProcessDefinitionName() {
    return processDefinitionName;
  }

  public String getProcessDefinitionNameLike() {
    return processDefinitionNameLike;
  }

  public String getProcessInstanceId() {
    return processInstanceId;
  }

  public String getAssignee() {
    return assignee;
  }

  public String getAssigneeExpression() {
    return assigneeExpression;
  }

  public String getAssigneeLike() {
    return assigneeLike;
  }

  public String getAssigneeLikeExpression() {
    return assigneeLikeExpression;
  }

  public String getCandidateGroup() {
    return candidateGroup;
  }

  public String getCandidateGroupExpression() {
    return candidateGroupExpression;
  }

  public String getCandidateUser() {
    return candidateUser;
  }

  public String getCandidateUserExpression() {
    return candidateUserExpression;
  }

  public Boolean getIncludeAssignedTasks(){
    return includeAssignedTasks;
  }

  public String[] getTaskDefinitionKeyIn() {
    return taskDefinitionKeyIn;
  }

  public String getTaskDefinitionKey() {
    return taskDefinitionKey;
  }

  public String getTaskDefinitionKeyLike() {
    return taskDefinitionKeyLike;
  }

  public String getDescription() {
    return description;
  }

  public String getDescriptionLike() {
    return descriptionLike;
  }

  public String getInvolvedUser() {
    return involvedUser;
  }

  public String getInvolvedUserExpression() {
    return involvedUserExpression;
  }

  public Integer getMaxPriority() {
    return maxPriority;
  }

  public Integer getMinPriority() {
    return minPriority;
  }

  public String getName() {
    return name;
  }

  public String getNameNotEqual() {
    return nameNotEqual;
  }

  public String getNameLike() {
    return nameLike;
  }

  public String getNameNotLike() {
    return nameNotLike;
  }

  public String getOwner() {
    return owner;
  }

  public String getOwnerExpression() {
    return ownerExpression;
  }

  public Integer getPriority() {
    return priority;
  }

  public String getParentTaskId() {
    return parentTaskId;
  }

  public Boolean getUnassigned() {
    return unassigned;
  }

  public Boolean getActive() {
    return active;
  }

  public Boolean getSuspended() {
    return suspended;
  }

  public String getCaseDefinitionKey() {
    return caseDefinitionKey;
  }

  public String getCaseDefinitionId() {
    return caseDefinitionId;
  }

  public String getCaseDefinitionName() {
    return caseDefinitionName;
  }

  public String getCaseDefinitionNameLike() {
    return caseDefinitionNameLike;
  }

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  public String getCaseInstanceBusinessKey() {
    return caseInstanceBusinessKey;
  }

  public String getCaseInstanceBusinessKeyLike() {
    return caseInstanceBusinessKeyLike;
  }

  public String getCaseExecutionId() {
    return caseExecutionId;
  }

  public Date getDueAfter() {
    return dueAfter;
  }

  public String getDueAfterExpression() {
    return dueAfterExpression;
  }

  public Date getDueBefore() {
    return dueBefore;
  }

  public String getDueBeforeExpression() {
    return dueBeforeExpression;
  }

  public Date getDueDate() {
    return dueDate;
  }

  public String getDueDateExpression() {
    return dueDateExpression;
  }

  public Date getFollowUpAfter() {
    return followUpAfter;
  }

  public String getFollowUpAfterExpression() {
    return followUpAfterExpression;
  }

  public Date getFollowUpBefore() {
    return followUpBefore;
  }

  public String getFollowUpBeforeExpression() {
    return followUpBeforeExpression;
  }

  public Date getFollowUpBeforeOrNotExistent() {
    return followUpBeforeOrNotExistent;
  }

  public String getFollowUpBeforeOrNotExistentExpression() {
    return followUpBeforeOrNotExistentExpression;
  }

  public Date getFollowUpDate() {
    return followUpDate;
  }

  public String getFollowUpDateExpression() {
    return followUpDateExpression;
  }

  public Date getCreatedAfter() {
    return createdAfter;
  }

  public String getCreatedAfterExpression() {
    return createdAfterExpression;
  }

  public Date getCreatedBefore() {
    return createdBefore;
  }

  public String getCreatedBeforeExpression() {
    return createdBeforeExpression;
  }

  public Date getCreatedOn() {
    return createdOn;
  }

  public String getCreatedOnExpression() {
    return createdOnExpression;
  }

  public String getDelegationState() {
    return delegationState;
  }

  public List<String> getCandidateGroups() {
    return candidateGroups;
  }

  public String getCandidateGroupsExpression() {
    return candidateGroupsExpression;
  }

  public List<VariableQueryParameterDto> getTaskVariables() {
    return taskVariables;
  }

  public List<VariableQueryParameterDto> getProcessVariables() {
    return processVariables;
  }

  public List<VariableQueryParameterDto> getCaseInstanceVariables() {
    return caseInstanceVariables;
  }

  public List<TaskQueryDto> getOrQueries() {
    return orQueries;
  }

  @Override
  protected void applyFilters(TaskQuery query) {
    if (orQueries != null) {
      for (TaskQueryDto orQueryDto: orQueries) {
        TaskQueryImpl orQuery = new TaskQueryImpl();
        orQuery.setOrQueryActive();
        orQueryDto.applyFilters(orQuery);
        ((TaskQueryImpl) query).addOrQuery(orQuery);
      }
    }
    if (processInstanceBusinessKey != null) {
      query.processInstanceBusinessKey(processInstanceBusinessKey);
    }
    if (processInstanceBusinessKeyExpression != null) {
      query.processInstanceBusinessKeyExpression(processInstanceBusinessKeyExpression);
    }
    if (processInstanceBusinessKeyIn != null && processInstanceBusinessKeyIn.length > 0) {
      query.processInstanceBusinessKeyIn(processInstanceBusinessKeyIn);
    }
    if (processInstanceBusinessKeyLike != null) {
      query.processInstanceBusinessKeyLike(processInstanceBusinessKeyLike);
    }
    if (processInstanceBusinessKeyLikeExpression != null) {
      query.processInstanceBusinessKeyLikeExpression(processInstanceBusinessKeyLikeExpression);
    }
    if (processDefinitionKey != null) {
      query.processDefinitionKey(processDefinitionKey);
    }
    if (processDefinitionKeyIn != null && processDefinitionKeyIn.length > 0) {
      query.processDefinitionKeyIn(processDefinitionKeyIn);
    }
    if (processDefinitionId != null) {
      query.processDefinitionId(processDefinitionId);
    }
    if (executionId != null) {
      query.executionId(executionId);
    }
    if (activityInstanceIdIn != null && activityInstanceIdIn.length > 0) {
      query.activityInstanceIdIn(activityInstanceIdIn);
    }
    if (tenantIdIn != null && tenantIdIn.length > 0) {
      query.tenantIdIn(tenantIdIn);
    }
    if (TRUE.equals(withoutTenantId)) {
      query.withoutTenantId();
    }
    if (processDefinitionName != null) {
      query.processDefinitionName(processDefinitionName);
    }
    if (processDefinitionNameLike != null) {
      query.processDefinitionNameLike(processDefinitionNameLike);
    }
    if (processInstanceId != null) {
      query.processInstanceId(processInstanceId);
    }
    if (assignee != null) {
      query.taskAssignee(assignee);
    }
    if (assigneeExpression != null) {
      query.taskAssigneeExpression(assigneeExpression);
    }
    if (assigneeLike != null) {
      query.taskAssigneeLike(assigneeLike);
    }
    if (assigneeLikeExpression != null) {
      query.taskAssigneeLikeExpression(assigneeLikeExpression);
    }
    if (candidateGroup != null) {
      query.taskCandidateGroup(candidateGroup);
    }
    if (candidateGroupExpression != null) {
      query.taskCandidateGroupExpression(candidateGroupExpression);
    }
    if (withCandidateGroups != null && withCandidateGroups) {
      query.withCandidateGroups();
    }
    if (withoutCandidateGroups != null && withoutCandidateGroups) {
      query.withoutCandidateGroups();
    }
    if (withCandidateUsers != null && withCandidateUsers) {
      query.withCandidateUsers();
    }
    if (withoutCandidateUsers != null && withoutCandidateUsers) {
      query.withoutCandidateUsers();
    }
    if (candidateUser != null) {
      query.taskCandidateUser(candidateUser);
    }
    if (candidateUserExpression != null) {
      query.taskCandidateUserExpression(candidateUserExpression);
    }
    if (taskDefinitionKeyIn != null && taskDefinitionKeyIn.length > 0) {
      query.taskDefinitionKeyIn(taskDefinitionKeyIn);
    }
    if (taskDefinitionKey != null) {
      query.taskDefinitionKey(taskDefinitionKey);
    }
    if (taskDefinitionKeyLike != null) {
      query.taskDefinitionKeyLike(taskDefinitionKeyLike);
    }
    if (description != null) {
      query.taskDescription(description);
    }
    if (descriptionLike != null) {
      query.taskDescriptionLike(descriptionLike);
    }
    if (involvedUser != null) {
      query.taskInvolvedUser(involvedUser);
    }
    if (involvedUserExpression != null) {
      query.taskInvolvedUserExpression(involvedUserExpression);
    }
    if (maxPriority != null) {
      query.taskMaxPriority(maxPriority);
    }
    if (minPriority != null) {
      query.taskMinPriority(minPriority);
    }
    if (name != null) {
      query.taskName(name);
    }
    if (nameNotEqual != null) {
      query.taskNameNotEqual(nameNotEqual);
    }
    if (nameLike != null) {
      query.taskNameLike(nameLike);
    }
    if (nameNotLike != null) {
      query.taskNameNotLike(nameNotLike);
    }
    if (owner != null) {
      query.taskOwner(owner);
    }
    if (ownerExpression != null) {
      query.taskOwnerExpression(ownerExpression);
    }
    if (priority != null) {
      query.taskPriority(priority);
    }
    if (parentTaskId != null) {
      query.taskParentTaskId(parentTaskId);
    }
    if (assigned != null && assigned) {
      query.taskAssigned();
    }
    if (unassigned != null && unassigned) {
      query.taskUnassigned();
    }
    if (dueAfter != null) {
      query.dueAfter(dueAfter);
    }
    if (dueAfterExpression != null) {
      query.dueAfterExpression(dueAfterExpression);
    }
    if (dueBefore != null) {
      query.dueBefore(dueBefore);
    }
    if (dueBeforeExpression != null) {
      query.dueBeforeExpression(dueBeforeExpression);
    }
    if (dueDate != null) {
      query.dueDate(dueDate);
    }
    if (dueDateExpression != null) {
      query.dueDateExpression(dueDateExpression);
    }
    if (followUpAfter != null) {
      query.followUpAfter(followUpAfter);
    }
    if (followUpAfterExpression != null) {
      query.followUpAfterExpression(followUpAfterExpression);
    }
    if (followUpBefore != null) {
      query.followUpBefore(followUpBefore);
    }
    if (followUpBeforeExpression != null) {
      query.followUpBeforeExpression(followUpBeforeExpression);
    }
    if (followUpBeforeOrNotExistent != null) {
      query.followUpBeforeOrNotExistent(followUpBeforeOrNotExistent);
    }
    if (followUpBeforeOrNotExistentExpression != null) {
      query.followUpBeforeOrNotExistentExpression(followUpBeforeOrNotExistentExpression);
    }
    if (followUpDate != null) {
      query.followUpDate(followUpDate);
    }
    if (followUpDateExpression != null) {
      query.followUpDateExpression(followUpDateExpression);
    }
    if (createdAfter != null) {
      query.taskCreatedAfter(createdAfter);
    }
    if (createdAfterExpression != null) {
      query.taskCreatedAfterExpression(createdAfterExpression);
    }
    if (createdBefore != null) {
      query.taskCreatedBefore(createdBefore);
    }
    if (createdBeforeExpression != null) {
      query.taskCreatedBeforeExpression(createdBeforeExpression);
    }
    if (createdOn != null) {
      query.taskCreatedOn(createdOn);
    }
    if (createdOnExpression != null) {
      query.taskCreatedOnExpression(createdOnExpression);
    }
    if (delegationState != null) {
      DelegationStateConverter converter = new DelegationStateConverter();
      DelegationState state = converter.convertQueryParameterToType(delegationState);
      query.taskDelegationState(state);
    }
    if (candidateGroups != null) {
      query.taskCandidateGroupIn(candidateGroups);
    }
    if (candidateGroupsExpression != null) {
      query.taskCandidateGroupInExpression(candidateGroupsExpression);
    }
    if (includeAssignedTasks != null && includeAssignedTasks){
      query.includeAssignedTasks();
    }
    if (active != null && active) {
      query.active();
    }
    if (suspended != null && suspended) {
      query.suspended();
    }
    if (caseDefinitionId != null) {
      query.caseDefinitionId(caseDefinitionId);
    }
    if (caseDefinitionKey != null) {
      query.caseDefinitionKey(caseDefinitionKey);
    }
    if (caseDefinitionName != null) {
      query.caseDefinitionName(caseDefinitionName);
    }
    if (caseDefinitionNameLike != null) {
      query.caseDefinitionNameLike(caseDefinitionNameLike);
    }
    if (caseExecutionId != null) {
      query.caseExecutionId(caseExecutionId);
    }
    if (caseInstanceBusinessKey != null) {
      query.caseInstanceBusinessKey(caseInstanceBusinessKey);
    }
    if (caseInstanceBusinessKeyLike != null) {
      query.caseInstanceBusinessKeyLike(caseInstanceBusinessKeyLike);
    }
    if (caseInstanceId != null) {
      query.caseInstanceId(caseInstanceId);
    }

    if (taskVariables != null) {
      for (VariableQueryParameterDto variableQueryParam : taskVariables) {
        String variableName = variableQueryParam.getName();
        String op = variableQueryParam.getOperator();
        Object variableValue = variableQueryParam.resolveValue(objectMapper);

        if (op.equals(VariableQueryParameterDto.EQUALS_OPERATOR_NAME)) {
          query.taskVariableValueEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.NOT_EQUALS_OPERATOR_NAME)) {
          query.taskVariableValueNotEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OPERATOR_NAME)) {
          query.taskVariableValueGreaterThan(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OR_EQUALS_OPERATOR_NAME)) {
          query.taskVariableValueGreaterThanOrEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OPERATOR_NAME)) {
          query.taskVariableValueLessThan(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OR_EQUALS_OPERATOR_NAME)) {
          query.taskVariableValueLessThanOrEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LIKE_OPERATOR_NAME)) {
          query.taskVariableValueLike(variableName, String.valueOf(variableValue));
        } else {
          throw new InvalidRequestException(Status.BAD_REQUEST, "Invalid task variable comparator specified: " + op);
        }

      }
    }

    if (processVariables != null) {
      for (VariableQueryParameterDto variableQueryParam : processVariables) {
        String variableName = variableQueryParam.getName();
        String op = variableQueryParam.getOperator();
        Object variableValue = variableQueryParam.resolveValue(objectMapper);

        if (op.equals(VariableQueryParameterDto.EQUALS_OPERATOR_NAME)) {
          query.processVariableValueEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.NOT_EQUALS_OPERATOR_NAME)) {
          query.processVariableValueNotEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OPERATOR_NAME)) {
          query.processVariableValueGreaterThan(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OR_EQUALS_OPERATOR_NAME)) {
          query.processVariableValueGreaterThanOrEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OPERATOR_NAME)) {
          query.processVariableValueLessThan(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OR_EQUALS_OPERATOR_NAME)) {
          query.processVariableValueLessThanOrEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LIKE_OPERATOR_NAME)) {
          query.processVariableValueLike(variableName, String.valueOf(variableValue));
        } else {
          throw new InvalidRequestException(Status.BAD_REQUEST, "Invalid process variable comparator specified: " + op);
        }

      }
    }

    if (caseInstanceVariables != null) {
      for (VariableQueryParameterDto variableQueryParam : caseInstanceVariables) {
        String variableName = variableQueryParam.getName();
        String op = variableQueryParam.getOperator();
        Object variableValue = variableQueryParam.resolveValue(objectMapper);

        if (op.equals(VariableQueryParameterDto.EQUALS_OPERATOR_NAME)) {
          query.caseInstanceVariableValueEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.NOT_EQUALS_OPERATOR_NAME)) {
          query.caseInstanceVariableValueNotEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OPERATOR_NAME)) {
          query.caseInstanceVariableValueGreaterThan(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.GREATER_THAN_OR_EQUALS_OPERATOR_NAME)) {
          query.caseInstanceVariableValueGreaterThanOrEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OPERATOR_NAME)) {
          query.caseInstanceVariableValueLessThan(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LESS_THAN_OR_EQUALS_OPERATOR_NAME)) {
          query.caseInstanceVariableValueLessThanOrEquals(variableName, variableValue);
        } else if (op.equals(VariableQueryParameterDto.LIKE_OPERATOR_NAME)) {
          query.caseInstanceVariableValueLike(variableName, String.valueOf(variableValue));
        } else {
          throw new InvalidRequestException(Status.BAD_REQUEST, "Invalid case variable comparator specified: " + op);
        }

      }
    }
  }

  @Override
  protected void applySortBy(TaskQuery query, String sortBy, Map<String, Object> parameters, ProcessEngine engine) {
    if (sortBy.equals(SORT_BY_PROCESS_INSTANCE_ID_VALUE)) {
      query.orderByProcessInstanceId();
    } else if (sortBy.equals(SORT_BY_CASE_INSTANCE_ID_VALUE)) {
      query.orderByCaseInstanceId();
    } else if (sortBy.equals(SORT_BY_DUE_DATE_VALUE)) {
      query.orderByDueDate();
    } else if (sortBy.equals(SORT_BY_FOLLOW_UP_VALUE)) {
      query.orderByFollowUpDate();
    } else if (sortBy.equals(SORT_BY_EXECUTION_ID_VALUE)) {
      query.orderByExecutionId();
    } else if (sortBy.equals(SORT_BY_CASE_EXECUTION_ID_VALUE)) {
      query.orderByCaseExecutionId();
    } else if (sortBy.equals(SORT_BY_ASSIGNEE_VALUE)) {
      query.orderByTaskAssignee();
    } else if (sortBy.equals(SORT_BY_CREATE_TIME_VALUE)) {
      query.orderByTaskCreateTime();
    } else if (sortBy.equals(SORT_BY_DESCRIPTION_VALUE)) {
      query.orderByTaskDescription();
    } else if (sortBy.equals(SORT_BY_ID_VALUE)) {
      query.orderByTaskId();
    } else if (sortBy.equals(SORT_BY_NAME_VALUE)) {
      query.orderByTaskName();
    } else if (sortBy.equals(SORT_BY_TENANT_ID_VALUE)) {
      query.orderByTenantId();
    } else if (sortBy.equals(SORT_BY_NAME_CASE_INSENSITIVE_VALUE)) {
      query.orderByTaskNameCaseInsensitive();
    } else if (sortBy.equals(SORT_BY_PRIORITY_VALUE)) {
      query.orderByTaskPriority();

    } else if (sortBy.equals(SORT_BY_PROCESS_VARIABLE)) {
      String variableName = getVariableName(parameters);
      String valueTypeName = getValueTypeName(parameters);
      query.orderByProcessVariable(variableName, getValueTypeByName(valueTypeName, engine));

    } else if (sortBy.equals(SORT_BY_EXECUTION_VARIABLE)) {
      String variableName = getVariableName(parameters);
      String valueTypeName = getValueTypeName(parameters);
      query.orderByExecutionVariable(variableName, getValueTypeByName(valueTypeName, engine));

    } else if (sortBy.equals(SORT_BY_TASK_VARIABLE)) {
      String variableName = getVariableName(parameters);
      String valueTypeName = getValueTypeName(parameters);
      query.orderByTaskVariable(variableName, getValueTypeByName(valueTypeName, engine));

    } else if (sortBy.equals(SORT_BY_CASE_INSTANCE_VARIABLE)) {
      String variableName = getVariableName(parameters);
      String valueTypeName = getValueTypeName(parameters);
      query.orderByCaseInstanceVariable(variableName, getValueTypeByName(valueTypeName, engine));

    } else if (sortBy.equals(SORT_BY_CASE_EXECUTION_VARIABLE)) {
      String variableName = getVariableName(parameters);
      String valueTypeName = getValueTypeName(parameters);
      query.orderByCaseExecutionVariable(variableName, getValueTypeByName(valueTypeName, engine));
    }
  }

  protected String getValueTypeName(Map<String, Object> parameters) {
    String valueTypeName = (String) getValue(parameters, SORT_PARAMETERS_VALUE_TYPE);
    if (valueTypeName != null) {
      valueTypeName = VariableValueDto.fromRestApiTypeName(valueTypeName);
    }
    return valueTypeName;
  }

  protected String getVariableName(Map<String, Object> parameters) {
    return (String) getValue(parameters, SORT_PARAMETERS_VARIABLE_NAME);
  }

  protected Object getValue(Map<String, Object> map, String key) {
    if (map != null) {
      return map.get(key);
    }
    return null;
  }

  protected ValueType getValueTypeByName(String name, ProcessEngine engine) {
    ValueTypeResolver valueTypeResolver = engine.getProcessEngineConfiguration().getValueTypeResolver();
    return valueTypeResolver.typeForName(name);
  }

  public static TaskQueryDto fromQuery(Query<?, ?> query) {
    return fromQuery(query, false);
  }

  public static TaskQueryDto fromQuery(Query<?, ?> query, boolean isOrQueryActive) {
    TaskQueryImpl taskQuery = (TaskQueryImpl) query;

    TaskQueryDto dto = new TaskQueryDto();

    if (!isOrQueryActive) {
      dto.orQueries = new ArrayList<TaskQueryDto>();
      for (TaskQueryImpl orQuery: taskQuery.getQueries()) {
        if (orQuery.isOrQueryActive()) {
          dto.orQueries.add(fromQuery(orQuery, true));
        }
      }
    }

    dto.activityInstanceIdIn = taskQuery.getActivityInstanceIdIn();
    dto.caseDefinitionId = taskQuery.getCaseDefinitionId();
    dto.caseDefinitionKey = taskQuery.getCaseDefinitionKey();
    dto.caseDefinitionName = taskQuery.getCaseDefinitionName();
    dto.caseDefinitionNameLike = taskQuery.getCaseDefinitionNameLike();
    dto.caseExecutionId = taskQuery.getCaseExecutionId();
    dto.caseInstanceBusinessKey = taskQuery.getCaseInstanceBusinessKey();
    dto.caseInstanceBusinessKeyLike = taskQuery.getCaseInstanceBusinessKeyLike();
    dto.caseInstanceId = taskQuery.getCaseInstanceId();

    dto.candidateUser = taskQuery.getCandidateUser();
    dto.candidateGroup = taskQuery.getCandidateGroup();
    dto.candidateGroups = taskQuery.getCandidateGroupsInternal();
    dto.includeAssignedTasks = taskQuery.isIncludeAssignedTasksInternal();
    dto.withCandidateGroups = taskQuery.isWithCandidateGroups();
    dto.withoutCandidateGroups = taskQuery.isWithoutCandidateGroups();
    dto.withCandidateUsers = taskQuery.isWithCandidateUsers();
    dto.withoutCandidateUsers = taskQuery.isWithoutCandidateUsers();

    dto.processInstanceBusinessKey = taskQuery.getProcessInstanceBusinessKey();
    dto.processInstanceBusinessKeyLike = taskQuery.getProcessInstanceBusinessKeyLike();
    dto.processDefinitionKey = taskQuery.getProcessDefinitionKey();
    dto.processDefinitionKeyIn = taskQuery.getProcessDefinitionKeys();
    dto.processDefinitionId = taskQuery.getProcessDefinitionId();
    dto.executionId = taskQuery.getExecutionId();

    dto.processDefinitionName = taskQuery.getProcessDefinitionName();
    dto.processDefinitionNameLike = taskQuery.getProcessDefinitionNameLike();
    dto.processInstanceId = taskQuery.getProcessInstanceId();
    dto.assignee = taskQuery.getAssignee();
    dto.assigneeLike = taskQuery.getAssigneeLike();
    dto.taskDefinitionKey = taskQuery.getKey();
    dto.taskDefinitionKeyIn = taskQuery.getKeys();
    dto.taskDefinitionKeyLike = taskQuery.getKeyLike();
    dto.description = taskQuery.getDescription();
    dto.descriptionLike = taskQuery.getDescriptionLike();
    dto.involvedUser = taskQuery.getInvolvedUser();
    dto.maxPriority = taskQuery.getMaxPriority();
    dto.minPriority = taskQuery.getMinPriority();
    dto.name = taskQuery.getName();
    dto.nameNotEqual = taskQuery.getNameNotEqual();
    dto.nameLike = taskQuery.getNameLike();
    dto.nameNotLike = taskQuery.getNameNotLike();
    dto.owner = taskQuery.getOwner();
    dto.priority = taskQuery.getPriority();
    dto.assigned = taskQuery.isAssignedInternal();
    dto.unassigned = taskQuery.isUnassignedInternal();
    dto.parentTaskId = taskQuery.getParentTaskId();

    dto.dueAfter = taskQuery.getDueAfter();
    dto.dueBefore = taskQuery.getDueBefore();
    dto.dueDate = taskQuery.getDueDate();
    dto.followUpAfter = taskQuery.getFollowUpAfter();

    if (taskQuery.isFollowUpNullAccepted()) {
      dto.followUpBeforeOrNotExistent = taskQuery.getFollowUpBefore();
    } else {
      dto.followUpBefore = taskQuery.getFollowUpBefore();
    }
    dto.followUpDate = taskQuery.getFollowUpDate();
    dto.createdAfter = taskQuery.getCreateTimeAfter();
    dto.createdBefore = taskQuery.getCreateTimeBefore();
    dto.createdOn = taskQuery.getCreateTime();

    if (taskQuery.getDelegationState() != null) {
      dto.delegationState = taskQuery.getDelegationState().toString();
    }

    if (taskQuery.isTenantIdSet()) {
      if (taskQuery.getTenantIds() != null) {
        dto.tenantIdIn = taskQuery.getTenantIds();
      } else {
        dto.withoutTenantId = true;
      }
    }

    dto.processVariables = new ArrayList<VariableQueryParameterDto>();
    dto.taskVariables = new ArrayList<VariableQueryParameterDto>();
    dto.caseInstanceVariables = new ArrayList<VariableQueryParameterDto>();
    for (TaskQueryVariableValue variableValue : taskQuery.getVariables()) {
      VariableQueryParameterDto variableValueDto = new VariableQueryParameterDto(variableValue);

      if (variableValue.isProcessInstanceVariable()) {
        dto.processVariables.add(variableValueDto);
      } else if (variableValue.isLocal()) {
        dto.taskVariables.add(variableValueDto);
      } else {
        dto.caseInstanceVariables.add(variableValueDto);
      }
    }

    if (taskQuery.getSuspensionState() == SuspensionState.ACTIVE) {
      dto.active = true;
    }
    if (taskQuery.getSuspensionState() == SuspensionState.SUSPENDED) {
      dto.suspended = true;
    }

    // sorting
    List<QueryOrderingProperty> orderingProperties = taskQuery.getOrderingProperties();
    if (!orderingProperties.isEmpty()) {
      dto.setSorting(convertQueryOrderingPropertiesToSortingDtos(orderingProperties));
    }

    // expressions
    Map<String, String> expressions = taskQuery.getExpressions();
    if (expressions.containsKey("taskAssignee")) {
      dto.setAssigneeExpression(expressions.get("taskAssignee"));
    }
    if (expressions.containsKey("taskAssigneeLike")) {
      dto.setAssigneeLikeExpression(expressions.get("taskAssigneeLike"));
    }
    if (expressions.containsKey("taskOwner")) {
      dto.setOwnerExpression(expressions.get("taskOwner"));
    }
    if (expressions.containsKey("taskCandidateUser")) {
      dto.setCandidateUserExpression(expressions.get("taskCandidateUser"));
    }
    if (expressions.containsKey("taskInvolvedUser")) {
      dto.setInvolvedUserExpression(expressions.get("taskInvolvedUser"));
    }
    if (expressions.containsKey("taskCandidateGroup")) {
      dto.setCandidateGroupExpression(expressions.get("taskCandidateGroup"));
    }
    if (expressions.containsKey("taskCandidateGroupIn")) {
      dto.setCandidateGroupsExpression(expressions.get("taskCandidateGroupIn"));
    }
    if (expressions.containsKey("taskCreatedOne")) {
      dto.setCreatedOnExpression(expressions.get("taskCreatedOne"));
    }
    if (expressions.containsKey("taskCreatedBefore")) {
      dto.setCreatedBeforeExpression(expressions.get("taskCreatedBefore"));
    }
    if (expressions.containsKey("taskCreatedAfter")) {
      dto.setCreatedAfterExpression(expressions.get("taskCreatedAfter"));
    }
    if (expressions.containsKey("dueDate")) {
      dto.setDueDateExpression(expressions.get("dueDate"));
    }
    if (expressions.containsKey("dueBefore")) {
      dto.setDueBeforeExpression(expressions.get("dueBefore"));
    }
    if (expressions.containsKey("dueAfter")) {
      dto.setDueAfterExpression(expressions.get("dueAfter"));
    }
    if (expressions.containsKey("followUpDate")) {
      dto.setFollowUpDateExpression(expressions.get("followUpDate"));
    }
    if (expressions.containsKey("followUpBefore")) {
      dto.setFollowUpBeforeExpression(expressions.get("followUpBefore"));
    }
    if (expressions.containsKey("followUpBeforeOrNotExistent")) {
      dto.setFollowUpBeforeOrNotExistentExpression(expressions.get("followUpBeforeOrNotExistent"));
    }
    if (expressions.containsKey("followUpAfter")) {
      dto.setFollowUpAfterExpression(expressions.get("followUpAfter"));
    }
    if (expressions.containsKey("processInstanceBusinessKey")) {
      dto.setProcessInstanceBusinessKeyExpression(expressions.get("processInstanceBusinessKey"));
    }
    if (expressions.containsKey("processInstanceBusinessKeyLike")) {
      dto.setProcessInstanceBusinessKeyLikeExpression(expressions.get("processInstanceBusinessKeyLike"));
    }

    return dto;
  }

  public static List<SortingDto> convertQueryOrderingPropertiesToSortingDtos(List<QueryOrderingProperty> orderingProperties) {
    List<SortingDto> sortingDtos = new ArrayList<SortingDto>();
    for (QueryOrderingProperty orderingProperty : orderingProperties) {
      SortingDto sortingDto;
      if (orderingProperty instanceof VariableOrderProperty) {
        sortingDto = convertVariableOrderPropertyToSortingDto((VariableOrderProperty) orderingProperty);
      }
      else {
        sortingDto = convertQueryOrderingPropertyToSortingDto(orderingProperty);
      }
      sortingDtos.add(sortingDto);
    }
    return sortingDtos;
  }

  public static SortingDto convertVariableOrderPropertyToSortingDto(VariableOrderProperty variableOrderProperty) {
    SortingDto sortingDto = new SortingDto();
    sortingDto.setSortBy(sortByValueForVariableOrderProperty(variableOrderProperty));
    sortingDto.setSortOrder(sortOrderValueForDirection(variableOrderProperty.getDirection()));
    sortingDto.setParameters(sortParametersForVariableOrderProperty(variableOrderProperty));
    return sortingDto;
  }

  public static SortingDto convertQueryOrderingPropertyToSortingDto(QueryOrderingProperty orderingProperty) {
    SortingDto sortingDto = new SortingDto();
    sortingDto.setSortBy(sortByValueForQueryProperty(orderingProperty.getQueryProperty()));
    sortingDto.setSortOrder(sortOrderValueForDirection(orderingProperty.getDirection()));
    return sortingDto;
  }

  public static String sortByValueForQueryProperty(QueryProperty queryProperty) {
    if (TaskQueryProperty.ASSIGNEE.equals(queryProperty)) {
      return SORT_BY_ASSIGNEE_VALUE;
    }
    else if (TaskQueryProperty.CASE_EXECUTION_ID.equals(queryProperty)) {
      return SORT_BY_CASE_EXECUTION_ID_VALUE;
    }
    else if (TaskQueryProperty.CASE_INSTANCE_ID.equals(queryProperty)) {
      return SORT_BY_CASE_INSTANCE_ID_VALUE;
    }
    else if (TaskQueryProperty.CREATE_TIME.equals(queryProperty)) {
      return SORT_BY_CREATE_TIME_VALUE;
    }
    else if (TaskQueryProperty.DESCRIPTION.equals(queryProperty)) {
      return SORT_BY_DESCRIPTION_VALUE;
    }
    else if (TaskQueryProperty.DUE_DATE.equals(queryProperty)) {
      return SORT_BY_DUE_DATE_VALUE;
    }
    else if (TaskQueryProperty.EXECUTION_ID.equals(queryProperty)) {
      return SORT_BY_EXECUTION_ID_VALUE;
    }
    else if (TaskQueryProperty.FOLLOW_UP_DATE.equals(queryProperty)) {
      return SORT_BY_FOLLOW_UP_VALUE;
    }
    else if (TaskQueryProperty.NAME.equals(queryProperty)) {
      return SORT_BY_NAME_VALUE;
    }
    else if (TaskQueryProperty.NAME_CASE_INSENSITIVE.equals(queryProperty)) {
      return SORT_BY_NAME_CASE_INSENSITIVE_VALUE;
    }
    else if (TaskQueryProperty.PRIORITY.equals(queryProperty)) {
      return SORT_BY_PRIORITY_VALUE;
    }
    else if (TaskQueryProperty.PROCESS_INSTANCE_ID.equals(queryProperty)) {
      return SORT_BY_PROCESS_INSTANCE_ID_VALUE;
    }
    else if (TaskQueryProperty.TASK_ID.equals(queryProperty)) {
      return SORT_BY_ID_VALUE;
    }
    else if (TaskQueryProperty.TENANT_ID.equals(queryProperty)) {
      return SORT_BY_TENANT_ID_VALUE;
    }
    else {
      throw new RestException("Unknown query property for task query " + queryProperty);
    }
  }

  public static String sortByValueForVariableOrderProperty(VariableOrderProperty variableOrderProperty) {
    for (QueryEntityRelationCondition relationCondition : variableOrderProperty.getRelationConditions()) {
      if (relationCondition.isPropertyComparison()) {
        return sortByValueForQueryEntityRelationCondition(relationCondition);
      }
    }

    // if no property comparison was found throw an exception
    throw new RestException("Unknown variable order property for task query " + variableOrderProperty);
  }

  public static String sortByValueForQueryEntityRelationCondition(QueryEntityRelationCondition relationCondition) {
    QueryProperty property = relationCondition.getProperty();
    QueryProperty comparisonProperty = relationCondition.getComparisonProperty();
    if (VariableInstanceQueryProperty.EXECUTION_ID.equals(property) && TaskQueryProperty.PROCESS_INSTANCE_ID.equals(comparisonProperty)) {
        return SORT_BY_PROCESS_VARIABLE;
    }
    else if (VariableInstanceQueryProperty.EXECUTION_ID.equals(property) && TaskQueryProperty.EXECUTION_ID.equals(comparisonProperty)) {
      return SORT_BY_EXECUTION_VARIABLE;
    }
    else if (VariableInstanceQueryProperty.TASK_ID.equals(property) && TaskQueryProperty.TASK_ID.equals(comparisonProperty)) {
      return SORT_BY_TASK_VARIABLE;
    }
    else if (VariableInstanceQueryProperty.CASE_EXECUTION_ID.equals(property) && TaskQueryProperty.CASE_INSTANCE_ID.equals(comparisonProperty)) {
      return SORT_BY_CASE_INSTANCE_VARIABLE;
    }
    else if (VariableInstanceQueryProperty.CASE_EXECUTION_ID.equals(property) && TaskQueryProperty.CASE_EXECUTION_ID.equals(comparisonProperty))  {
      return SORT_BY_CASE_EXECUTION_VARIABLE;
    }
    else {
      throw new RestException("Unknown relation condition for task query  with query property " + property + " and comparison property " + comparisonProperty);
    }
  }

  public static Map<String,Object> sortParametersForVariableOrderProperty(VariableOrderProperty variableOrderProperty) {
    Map<String, Object> parameters = new HashMap<String, Object>();
    for (QueryEntityRelationCondition relationCondition : variableOrderProperty.getRelationConditions()) {
      QueryProperty property = relationCondition.getProperty();
      if (VariableInstanceQueryProperty.VARIABLE_NAME.equals(property)) {
        parameters.put(SORT_PARAMETERS_VARIABLE_NAME, relationCondition.getScalarValue());
      }
      else if (VariableInstanceQueryProperty.VARIABLE_TYPE.equals(property)) {
        String type = VariableValueDto.toRestApiTypeName((String) relationCondition.getScalarValue());
        parameters.put(SORT_PARAMETERS_VALUE_TYPE, type);
      }
    }
    return parameters;
  }

}
