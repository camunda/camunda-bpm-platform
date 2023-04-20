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
package org.camunda.bpm.engine.impl.json;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.QueryOperator;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.TaskQueryVariableValue;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.util.JsonUtil;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.TaskQuery;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Sebastian Menski
 */
public class JsonTaskQueryConverter extends JsonObjectConverter<TaskQuery> {

  public static final String ID = "id";
  public static final String TASK_ID = "taskId";
  public static final String TASK_ID_IN = "taskIdIn";
  public static final String NAME = "name";
  public static final String NAME_NOT_EQUAL = "nameNotEqual";
  public static final String NAME_LIKE = "nameLike";
  public static final String NAME_NOT_LIKE = "nameNotLike";
  public static final String DESCRIPTION = "description";
  public static final String DESCRIPTION_LIKE = "descriptionLike";
  public static final String PRIORITY = "priority";
  public static final String MIN_PRIORITY = "minPriority";
  public static final String MAX_PRIORITY = "maxPriority";
  public static final String ASSIGNEE = "assignee";
  public static final String ASSIGNEE_LIKE = "assigneeLike";
  public static final String ASSIGNEE_IN = "assigneeIn";
  public static final String ASSIGNEE_NOT_IN = "assigneeNotIn";
  public static final String INVOLVED_USER = "involvedUser";
  public static final String OWNER = "owner";
  public static final String UNASSIGNED = "unassigned";
  public static final String ASSIGNED = "assigned";
  public static final String DELEGATION_STATE = "delegationState";
  public static final String CANDIDATE_USER = "candidateUser";
  public static final String CANDIDATE_GROUP = "candidateGroup";
  public static final String CANDIDATE_GROUPS = "candidateGroups";
  public static final String WITH_CANDIDATE_GROUPS = "withCandidateGroups";
  public static final String WITHOUT_CANDIDATE_GROUPS = "withoutCandidateGroups";
  public static final String WITH_CANDIDATE_USERS = "withCandidateUsers";
  public static final String WITHOUT_CANDIDATE_USERS = "withoutCandidateUsers";
  public static final String INCLUDE_ASSIGNED_TASKS = "includeAssignedTasks";
  public static final String INSTANCE_ID = "instanceId";
  public static final String PROCESS_INSTANCE_ID = "processInstanceId";
  public static final String PROCESS_INSTANCE_ID_IN = "processInstanceIdIn";
  public static final String EXECUTION_ID = "executionId";
  public static final String ACTIVITY_INSTANCE_ID_IN = "activityInstanceIdIn";
  public static final String CREATED = "created";
  public static final String CREATED_BEFORE = "createdBefore";
  public static final String CREATED_AFTER = "createdAfter";
  public static final String UPDATED_AFTER = "updatedAfter";
  public static final String KEY = "key";
  public static final String KEYS = "keys";
  public static final String KEY_LIKE = "keyLike";
  public static final String PARENT_TASK_ID = "parentTaskId";
  public static final String PROCESS_DEFINITION_KEY = "processDefinitionKey";
  public static final String PROCESS_DEFINITION_KEYS = "processDefinitionKeys";
  public static final String PROCESS_DEFINITION_ID = "processDefinitionId";
  public static final String PROCESS_DEFINITION_NAME = "processDefinitionName";
  public static final String PROCESS_DEFINITION_NAME_LIKE = "processDefinitionNameLike";
  public static final String PROCESS_INSTANCE_BUSINESS_KEY = "processInstanceBusinessKey";
  public static final String PROCESS_INSTANCE_BUSINESS_KEYS ="processInstanceBusinessKeys";
  public static final String PROCESS_INSTANCE_BUSINESS_KEY_LIKE = "processInstanceBusinessKeyLike";
  public static final String DUE = "due";
  public static final String DUE_DATE = "dueDate";
  public static final String DUE_BEFORE = "dueBefore";
  public static final String DUE_AFTER = "dueAfter";
  public static final String WITHOUT_DUE_DATE = "withoutDueDate";
  public static final String FOLLOW_UP = "followUp";
  public static final String FOLLOW_UP_DATE = "followUpDate";
  public static final String FOLLOW_UP_BEFORE = "followUpBefore";
  public static final String FOLLOW_UP_NULL_ACCEPTED = "followUpNullAccepted";
  public static final String FOLLOW_UP_AFTER = "followUpAfter";
  public static final String EXCLUDE_SUBTASKS = "excludeSubtasks";
  public static final String CASE_DEFINITION_KEY = "caseDefinitionKey";
  public static final String CASE_DEFINITION_ID = "caseDefinitionId";
  public static final String CASE_DEFINITION_NAME = "caseDefinitionName";
  public static final String CASE_DEFINITION_NAME_LIKE = "caseDefinitionNameLike";
  public static final String CASE_INSTANCE_ID = "caseInstanceId";
  public static final String CASE_INSTANCE_BUSINESS_KEY = "caseInstanceBusinessKey";
  public static final String CASE_INSTANCE_BUSINESS_KEY_LIKE = "caseInstanceBusinessKeyLike";
  public static final String CASE_EXECUTION_ID = "caseExecutionId";
  public static final String ACTIVE = "active";
  public static final String SUSPENDED = "suspended";
  public static final String PROCESS_VARIABLES = "processVariables";
  public static final String TASK_VARIABLES = "taskVariables";
  public static final String CASE_INSTANCE_VARIABLES = "caseInstanceVariables";
  public static final String TENANT_IDS = "tenantIds";
  public static final String WITHOUT_TENANT_ID = "withoutTenantId";
  public static final String ORDERING_PROPERTIES = "orderingProperties";
  public static final String OR_QUERIES = "orQueries";

  /**
   * Exists for backwards compatibility with 7.2; deprecated since 7.3
   */
  @Deprecated
  public static final String ORDER_BY = "orderBy";

  protected static JsonTaskQueryVariableValueConverter variableValueConverter = new JsonTaskQueryVariableValueConverter();

  @Override
  public JsonObject toJsonObject(TaskQuery taskQuery) {
    return toJsonObject(taskQuery, false);
  }

  public JsonObject toJsonObject(TaskQuery taskQuery, boolean isOrQueryActive) {
    JsonObject json = JsonUtil.createObject();
    TaskQueryImpl query = (TaskQueryImpl) taskQuery;

    JsonUtil.addField(json, TASK_ID, query.getTaskId());
    JsonUtil.addArrayField(json, TASK_ID_IN, query.getTaskIdIn());
    JsonUtil.addField(json, NAME, query.getName());
    JsonUtil.addField(json, NAME_NOT_EQUAL, query.getNameNotEqual());
    JsonUtil.addField(json, NAME_LIKE, query.getNameLike());
    JsonUtil.addField(json, NAME_NOT_LIKE, query.getNameNotLike());
    JsonUtil.addField(json, DESCRIPTION, query.getDescription());
    JsonUtil.addField(json, DESCRIPTION_LIKE, query.getDescriptionLike());
    JsonUtil.addField(json, PRIORITY, query.getPriority());
    JsonUtil.addField(json, MIN_PRIORITY, query.getMinPriority());
    JsonUtil.addField(json, MAX_PRIORITY, query.getMaxPriority());
    JsonUtil.addField(json, ASSIGNEE, query.getAssignee());

    if (query.getAssigneeIn() != null) {
      JsonUtil.addArrayField(json, ASSIGNEE_IN,
          query.getAssigneeIn().toArray(new String[query.getAssigneeIn().size()]));
    }

    if (query.getAssigneeNotIn() != null) {
      JsonUtil.addArrayField(json, ASSIGNEE_NOT_IN,
              query.getAssigneeNotIn().toArray(new String[query.getAssigneeNotIn().size()]));
    }

    JsonUtil.addField(json, ASSIGNEE_LIKE, query.getAssigneeLike());
    JsonUtil.addField(json, INVOLVED_USER, query.getInvolvedUser());
    JsonUtil.addField(json, OWNER, query.getOwner());
    JsonUtil.addDefaultField(json, UNASSIGNED, false, query.isUnassigned());
    JsonUtil.addDefaultField(json, ASSIGNED, false, query.isAssigned());
    JsonUtil.addField(json, DELEGATION_STATE, query.getDelegationStateString());
    JsonUtil.addField(json, CANDIDATE_USER, query.getCandidateUser());
    JsonUtil.addField(json, CANDIDATE_GROUP, query.getCandidateGroup());
    JsonUtil.addListField(json, CANDIDATE_GROUPS, query.getCandidateGroupsInternal());
    JsonUtil.addDefaultField(json, WITH_CANDIDATE_GROUPS, false, query.isWithCandidateGroups());
    JsonUtil.addDefaultField(json, WITHOUT_CANDIDATE_GROUPS, false, query.isWithoutCandidateGroups());
    JsonUtil.addDefaultField(json, WITH_CANDIDATE_USERS, false, query.isWithCandidateUsers());
    JsonUtil.addDefaultField(json, WITHOUT_CANDIDATE_USERS, false, query.isWithoutCandidateUsers());
    JsonUtil.addField(json, INCLUDE_ASSIGNED_TASKS, query.isIncludeAssignedTasksInternal());
    JsonUtil.addField(json, PROCESS_INSTANCE_ID, query.getProcessInstanceId());
    if (query.getProcessInstanceIdIn() != null) {
      JsonUtil.addArrayField(json, PROCESS_INSTANCE_ID_IN, query.getProcessInstanceIdIn());
    }
    JsonUtil.addField(json, EXECUTION_ID, query.getExecutionId());
    JsonUtil.addArrayField(json, ACTIVITY_INSTANCE_ID_IN, query.getActivityInstanceIdIn());
    JsonUtil.addDateField(json, CREATED, query.getCreateTime());
    JsonUtil.addDateField(json, CREATED_BEFORE, query.getCreateTimeBefore());
    JsonUtil.addDateField(json, CREATED_AFTER, query.getCreateTimeAfter());
    JsonUtil.addDateField(json, UPDATED_AFTER, query.getUpdatedAfter());
    JsonUtil.addField(json, KEY, query.getKey());
    JsonUtil.addArrayField(json, KEYS, query.getKeys());
    JsonUtil.addField(json, KEY_LIKE, query.getKeyLike());
    JsonUtil.addField(json, PARENT_TASK_ID, query.getParentTaskId());
    JsonUtil.addField(json, PROCESS_DEFINITION_KEY, query.getProcessDefinitionKey());
    JsonUtil.addArrayField(json, PROCESS_DEFINITION_KEYS, query.getProcessDefinitionKeys());
    JsonUtil.addField(json, PROCESS_DEFINITION_ID, query.getProcessDefinitionId());
    JsonUtil.addField(json, PROCESS_DEFINITION_NAME, query.getProcessDefinitionName());
    JsonUtil.addField(json, PROCESS_DEFINITION_NAME_LIKE, query.getProcessDefinitionNameLike());
    JsonUtil.addField(json, PROCESS_INSTANCE_BUSINESS_KEY, query.getProcessInstanceBusinessKey());
    JsonUtil.addArrayField(json, PROCESS_INSTANCE_BUSINESS_KEYS, query.getProcessInstanceBusinessKeys());
    JsonUtil.addField(json, PROCESS_INSTANCE_BUSINESS_KEY_LIKE, query.getProcessInstanceBusinessKeyLike());
    addVariablesFields(json, query.getVariables());
    JsonUtil.addDateField(json, DUE, query.getDueDate());
    JsonUtil.addDateField(json, DUE_BEFORE, query.getDueBefore());
    JsonUtil.addDateField(json, DUE_AFTER, query.getDueAfter());
    JsonUtil.addDefaultField(json, WITHOUT_DUE_DATE, false, query.isWithoutDueDate());
    JsonUtil.addDateField(json, FOLLOW_UP, query.getFollowUpDate());
    JsonUtil.addDateField(json, FOLLOW_UP_BEFORE, query.getFollowUpBefore());
    JsonUtil.addDefaultField(json, FOLLOW_UP_NULL_ACCEPTED, false, query.isFollowUpNullAccepted());
    JsonUtil.addDateField(json, FOLLOW_UP_AFTER, query.getFollowUpAfter());
    JsonUtil.addDefaultField(json, EXCLUDE_SUBTASKS, false, query.isExcludeSubtasks());
    addSuspensionStateField(json, query.getSuspensionState());
    JsonUtil.addField(json, CASE_DEFINITION_KEY, query.getCaseDefinitionKey());
    JsonUtil.addField(json, CASE_DEFINITION_ID, query.getCaseDefinitionId());
    JsonUtil.addField(json, CASE_DEFINITION_NAME, query.getCaseDefinitionName());
    JsonUtil.addField(json, CASE_DEFINITION_NAME_LIKE, query.getCaseDefinitionNameLike());
    JsonUtil.addField(json, CASE_INSTANCE_ID, query.getCaseInstanceId());
    JsonUtil.addField(json, CASE_INSTANCE_BUSINESS_KEY, query.getCaseInstanceBusinessKey());
    JsonUtil.addField(json, CASE_INSTANCE_BUSINESS_KEY_LIKE, query.getCaseInstanceBusinessKeyLike());
    JsonUtil.addField(json, CASE_EXECUTION_ID, query.getCaseExecutionId());
    addTenantIdFields(json, query);

    if (query.getQueries().size() > 1 && !isOrQueryActive) {
      JsonArray orQueries = JsonUtil.createArray();

      for (TaskQueryImpl orQuery: query.getQueries()) {
        if (orQuery != null && orQuery.isOrQueryActive()) {
          orQueries.add(toJsonObject(orQuery, true));
        }
      }

      JsonUtil.addField(json, OR_QUERIES, orQueries);
    }

    if (query.getOrderingProperties() != null && !query.getOrderingProperties().isEmpty()) {
      JsonUtil.addField(json, ORDERING_PROPERTIES,
          JsonQueryOrderingPropertyConverter.ARRAY_CONVERTER.toJsonArray(query.getOrderingProperties()));
    }


    // expressions
    for (Map.Entry<String, String> expressionEntry : query.getExpressions().entrySet()) {
      JsonUtil.addField(json, expressionEntry.getKey() + "Expression", expressionEntry.getValue());
    }

    return json;
  }

  protected void addSuspensionStateField(JsonObject jsonObject, SuspensionState suspensionState) {
    if (suspensionState != null) {
      if (suspensionState.equals(SuspensionState.ACTIVE)) {
        JsonUtil.addField(jsonObject, ACTIVE, true);
      }
      else if (suspensionState.equals(SuspensionState.SUSPENDED)) {
        JsonUtil.addField(jsonObject, SUSPENDED, true);
      }
    }
  }

  protected void addTenantIdFields(JsonObject jsonObject, TaskQueryImpl query) {
    if (query.getTenantIds() != null) {
      JsonUtil.addArrayField(jsonObject, TENANT_IDS, query.getTenantIds());
    }
    if (query.isWithoutTenantId()) {
      JsonUtil.addField(jsonObject, WITHOUT_TENANT_ID, true);
    }
  }

  protected void addVariablesFields(JsonObject jsonObject, List<TaskQueryVariableValue> variables) {
    for (TaskQueryVariableValue variable : variables) {
      if (variable.isProcessInstanceVariable()) {
        addVariable(jsonObject, PROCESS_VARIABLES, variable);
      }
      else if(variable.isLocal()) {
        addVariable(jsonObject, TASK_VARIABLES, variable);
      }
      else {
        addVariable(jsonObject, CASE_INSTANCE_VARIABLES, variable);
      }
    }
  }

  protected void addVariable(JsonObject jsonObject, String variableType, TaskQueryVariableValue variable) {
    JsonArray variables = JsonUtil.getArray(jsonObject, variableType);

    JsonUtil.addElement(variables, variableValueConverter, variable);
    JsonUtil.addField(jsonObject, variableType, variables);
  }

  @Override
  public TaskQuery toObject(JsonObject json) {
    return toObject(json, false);
  }

  protected TaskQuery toObject(JsonObject json, boolean isOrQuery) {
    TaskQueryImpl query = new TaskQueryImpl();
    if (isOrQuery) {
      query.setOrQueryActive();
    }
    if (json.has(OR_QUERIES)) {
      for (JsonElement jsonElement : JsonUtil.getArray(json, OR_QUERIES)) {
        query.addOrQuery((TaskQueryImpl) toObject(JsonUtil.getObject(jsonElement), true));
      }
    }
    if (json.has(TASK_ID)) {
      query.taskId(JsonUtil.getString(json,TASK_ID));
    }
    if (json.has(TASK_ID_IN)) {
      query.taskIdIn(getArray(JsonUtil.getArray(json, TASK_ID_IN)));
    }
    if (json.has(NAME)) {
      query.taskName(JsonUtil.getString(json, NAME));
    }
    if (json.has(NAME_NOT_EQUAL)) {
      query.taskNameNotEqual(JsonUtil.getString(json, NAME_NOT_EQUAL));
    }
    if (json.has(NAME_LIKE)) {
      query.taskNameLike(JsonUtil.getString(json, NAME_LIKE));
    }
    if (json.has(NAME_NOT_LIKE)) {
      query.taskNameNotLike(JsonUtil.getString(json, NAME_NOT_LIKE));
    }
    if (json.has(DESCRIPTION)) {
      query.taskDescription(JsonUtil.getString(json, DESCRIPTION));
    }
    if (json.has(DESCRIPTION_LIKE)) {
      query.taskDescriptionLike(JsonUtil.getString(json, DESCRIPTION_LIKE));
    }
    if (json.has(PRIORITY)) {
      query.taskPriority(JsonUtil.getInt(json, PRIORITY));
    }
    if (json.has(MIN_PRIORITY)) {
      query.taskMinPriority(JsonUtil.getInt(json, MIN_PRIORITY));
    }
    if (json.has(MAX_PRIORITY)) {
      query.taskMaxPriority(JsonUtil.getInt(json, MAX_PRIORITY));
    }
    if (json.has(ASSIGNEE)) {
      query.taskAssignee(JsonUtil.getString(json, ASSIGNEE));
    }
    if (json.has(ASSIGNEE_LIKE)) {
      query.taskAssigneeLike(JsonUtil.getString(json, ASSIGNEE_LIKE));
    }
    if (json.has(ASSIGNEE_IN)) {
      query.taskAssigneeIn(getArray(JsonUtil.getArray(json, ASSIGNEE_IN)));
    }
    if (json.has(ASSIGNEE_NOT_IN)) {
      query.taskAssigneeNotIn(getArray(JsonUtil.getArray(json, ASSIGNEE_NOT_IN)));
    }
    if (json.has(INVOLVED_USER)) {
      query.taskInvolvedUser(JsonUtil.getString(json, INVOLVED_USER));
    }
    if (json.has(OWNER)) {
      query.taskOwner(JsonUtil.getString(json, OWNER));
    }
    if (json.has(ASSIGNED) && JsonUtil.getBoolean(json, ASSIGNED)) {
      query.taskAssigned();
    }
    if (json.has(UNASSIGNED) && JsonUtil.getBoolean(json, UNASSIGNED)) {
      query.taskUnassigned();
    }
    if (json.has(DELEGATION_STATE)) {
      query.taskDelegationState(DelegationState.valueOf(JsonUtil.getString(json, DELEGATION_STATE)));
    }
    if (json.has(CANDIDATE_USER)) {
      query.taskCandidateUser(JsonUtil.getString(json, CANDIDATE_USER));
    }
    if (json.has(CANDIDATE_GROUP)) {
      query.taskCandidateGroup(JsonUtil.getString(json, CANDIDATE_GROUP));
    }
    if (json.has(CANDIDATE_GROUPS) && !json.has(CANDIDATE_USER) && !json.has(CANDIDATE_GROUP)) {
      query.taskCandidateGroupIn(getList(JsonUtil.getArray(json, CANDIDATE_GROUPS)));
    }
    if (json.has(WITH_CANDIDATE_GROUPS) && JsonUtil.getBoolean(json, WITH_CANDIDATE_GROUPS)) {
      query.withCandidateGroups();
    }
    if (json.has(WITHOUT_CANDIDATE_GROUPS) && JsonUtil.getBoolean(json, WITHOUT_CANDIDATE_GROUPS)) {
      query.withoutCandidateGroups();
    }
    if (json.has(WITH_CANDIDATE_USERS) && JsonUtil.getBoolean(json, WITH_CANDIDATE_USERS)) {
      query.withCandidateUsers();
    }
    if (json.has(WITHOUT_CANDIDATE_USERS) && JsonUtil.getBoolean(json, WITHOUT_CANDIDATE_USERS)) {
      query.withoutCandidateUsers();
    }
    if (json.has(INCLUDE_ASSIGNED_TASKS) && JsonUtil.getBoolean(json, INCLUDE_ASSIGNED_TASKS)) {
      query.includeAssignedTasksInternal();
    }
    if (json.has(PROCESS_INSTANCE_ID)) {
      query.processInstanceId(JsonUtil.getString(json, PROCESS_INSTANCE_ID));
    }
    if (json.has(PROCESS_INSTANCE_ID_IN)) {
      query.processInstanceIdIn(getArray(JsonUtil.getArray(json, PROCESS_INSTANCE_ID_IN)));
    }
    if (json.has(EXECUTION_ID)) {
      query.executionId(JsonUtil.getString(json, EXECUTION_ID));
    }
    if (json.has(ACTIVITY_INSTANCE_ID_IN)) {
      query.activityInstanceIdIn(getArray(JsonUtil.getArray(json, ACTIVITY_INSTANCE_ID_IN)));
    }
    if (json.has(CREATED)) {
      query.taskCreatedOn(new Date(JsonUtil.getLong(json, CREATED)));
    }
    if (json.has(CREATED_BEFORE)) {
      query.taskCreatedBefore(new Date(JsonUtil.getLong(json, CREATED_BEFORE)));
    }
    if (json.has(CREATED_AFTER)) {
      query.taskCreatedAfter(new Date(JsonUtil.getLong(json, CREATED_AFTER)));
    }
    if (json.has(UPDATED_AFTER)) {
      query.taskUpdatedAfter(new Date(JsonUtil.getLong(json, UPDATED_AFTER)));
    }
    if (json.has(KEY)) {
      query.taskDefinitionKey(JsonUtil.getString(json, KEY));
    }
    if (json.has(KEYS)) {
      query.taskDefinitionKeyIn(getArray(JsonUtil.getArray(json, KEYS)));
    }
    if (json.has(KEY_LIKE)) {
      query.taskDefinitionKeyLike(JsonUtil.getString(json, KEY_LIKE));
    }
    if (json.has(PARENT_TASK_ID)) {
      query.taskParentTaskId(JsonUtil.getString(json, PARENT_TASK_ID));
    }
    if (json.has(PROCESS_DEFINITION_KEY)) {
      query.processDefinitionKey(JsonUtil.getString(json, PROCESS_DEFINITION_KEY));
    }
    if (json.has(PROCESS_DEFINITION_KEYS)) {
      query.processDefinitionKeyIn(getArray(JsonUtil.getArray(json, PROCESS_DEFINITION_KEYS)));
    }
    if (json.has(PROCESS_DEFINITION_ID)) {
      query.processDefinitionId(JsonUtil.getString(json, PROCESS_DEFINITION_ID));
    }
    if (json.has(PROCESS_DEFINITION_NAME)) {
      query.processDefinitionName(JsonUtil.getString(json, PROCESS_DEFINITION_NAME));
    }
    if (json.has(PROCESS_DEFINITION_NAME_LIKE)) {
      query.processDefinitionNameLike(JsonUtil.getString(json, PROCESS_DEFINITION_NAME_LIKE));
    }
    if (json.has(PROCESS_INSTANCE_BUSINESS_KEY)) {
      query.processInstanceBusinessKey(JsonUtil.getString(json, PROCESS_INSTANCE_BUSINESS_KEY));
    }
    if (json.has(PROCESS_INSTANCE_BUSINESS_KEYS)) {
      query.processInstanceBusinessKeyIn(getArray(JsonUtil.getArray(json, PROCESS_INSTANCE_BUSINESS_KEYS)));
    }
    if (json.has(PROCESS_INSTANCE_BUSINESS_KEY_LIKE)) {
      query.processInstanceBusinessKeyLike(JsonUtil.getString(json, PROCESS_INSTANCE_BUSINESS_KEY_LIKE));
    }
    if (json.has(TASK_VARIABLES)) {
      addVariables(query, JsonUtil.getArray(json, TASK_VARIABLES), true, false);
    }
    if (json.has(PROCESS_VARIABLES)) {
      addVariables(query, JsonUtil.getArray(json, PROCESS_VARIABLES), false, true);
    }
    if (json.has(CASE_INSTANCE_VARIABLES)) {
      addVariables(query, JsonUtil.getArray(json, CASE_INSTANCE_VARIABLES), false, false);
    }
    if (json.has(DUE)) {
      query.dueDate(new Date(JsonUtil.getLong(json, DUE)));
    }
    if (json.has(DUE_BEFORE)) {
      query.dueBefore(new Date(JsonUtil.getLong(json, DUE_BEFORE)));
    }
    if (json.has(DUE_AFTER)) {
      query.dueAfter(new Date(JsonUtil.getLong(json, DUE_AFTER)));
    }
    if (json.has(WITHOUT_DUE_DATE)) {
      query.withoutDueDate();
    }
    if (json.has(FOLLOW_UP)) {
      query.followUpDate(new Date(JsonUtil.getLong(json, FOLLOW_UP)));
    }
    if (json.has(FOLLOW_UP_BEFORE)) {
      query.followUpBefore(new Date(JsonUtil.getLong(json, FOLLOW_UP_BEFORE)));
    }
    if (json.has(FOLLOW_UP_AFTER)) {
      query.followUpAfter(new Date(JsonUtil.getLong(json, FOLLOW_UP_AFTER)));
    }
    if (json.has(FOLLOW_UP_NULL_ACCEPTED)) {
      query.setFollowUpNullAccepted(JsonUtil.getBoolean(json, FOLLOW_UP_NULL_ACCEPTED));
    }
    if (json.has(EXCLUDE_SUBTASKS) && JsonUtil.getBoolean(json, EXCLUDE_SUBTASKS)) {
      query.excludeSubtasks();
    }
    if (json.has(SUSPENDED) && JsonUtil.getBoolean(json, SUSPENDED)) {
      query.suspended();
    }
    if (json.has(ACTIVE) && JsonUtil.getBoolean(json, ACTIVE)) {
      query.active();
    }
    if (json.has(CASE_DEFINITION_KEY)) {
      query.caseDefinitionKey(JsonUtil.getString(json, CASE_DEFINITION_KEY));
    }
    if (json.has(CASE_DEFINITION_ID)) {
      query.caseDefinitionId(JsonUtil.getString(json, CASE_DEFINITION_ID));
    }
    if (json.has(CASE_DEFINITION_NAME)) {
      query.caseDefinitionName(JsonUtil.getString(json, CASE_DEFINITION_NAME));
    }
    if (json.has(CASE_DEFINITION_NAME_LIKE)) {
      query.caseDefinitionNameLike(JsonUtil.getString(json, CASE_DEFINITION_NAME_LIKE));
    }
    if (json.has(CASE_INSTANCE_ID)) {
      query.caseInstanceId(JsonUtil.getString(json, CASE_INSTANCE_ID));
    }
    if (json.has(CASE_INSTANCE_BUSINESS_KEY)) {
      query.caseInstanceBusinessKey(JsonUtil.getString(json, CASE_INSTANCE_BUSINESS_KEY));
    }
    if (json.has(CASE_INSTANCE_BUSINESS_KEY_LIKE)) {
      query.caseInstanceBusinessKeyLike(JsonUtil.getString(json, CASE_INSTANCE_BUSINESS_KEY_LIKE));
    }
    if (json.has(CASE_EXECUTION_ID)) {
      query.caseExecutionId(JsonUtil.getString(json, CASE_EXECUTION_ID));
    }
    if (json.has(TENANT_IDS)) {
      query.tenantIdIn(getArray(JsonUtil.getArray(json, TENANT_IDS)));
    }
    if (json.has(WITHOUT_TENANT_ID)) {
      query.withoutTenantId();
    }
    if (json.has(ORDER_BY)) {
      List<QueryOrderingProperty> orderingProperties =
          JsonLegacyQueryOrderingPropertyConverter.INSTANCE.fromOrderByString(JsonUtil.getString(json, ORDER_BY));

      query.setOrderingProperties(orderingProperties);
    }
    if (json.has(ORDERING_PROPERTIES)) {
      JsonArray jsonArray = JsonUtil.getArray(json, ORDERING_PROPERTIES);
      query.setOrderingProperties(JsonQueryOrderingPropertyConverter.ARRAY_CONVERTER.toObject(jsonArray));
    }

    // expressions
    for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
      String key = entry.getKey();
      if (key.endsWith("Expression")) {
        String expression = JsonUtil.getString(json, key);
        query.addExpression(key.substring(0, key.length() - "Expression".length()), expression);
      }
    }

    return query;
  }

  protected String[] getArray(JsonArray array) {
    return getList(array).toArray(new String[array.size()]);
  }

  protected List<String> getList(JsonArray array) {
    List<String> list = new ArrayList<>();
    for (JsonElement entry : array) {
      list.add(JsonUtil.getString(entry));
    }
    return list;
  }

  protected void addVariables(TaskQueryImpl query, JsonArray variables, boolean isTaskVariable, boolean isProcessVariable) {
    for (JsonElement variable : variables) {
      JsonObject variableObj = JsonUtil.getObject(variable);
      String name = JsonUtil.getString(variableObj, NAME);
      Object rawValue = JsonUtil.getRawObject(variableObj, "value");
      QueryOperator operator = QueryOperator.valueOf(JsonUtil.getString(variableObj, "operator"));
      query.addVariable(name, rawValue, operator, isTaskVariable, isProcessVariable);
    }
  }

}
