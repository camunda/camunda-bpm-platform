/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import static org.camunda.bpm.engine.impl.util.JsonUtil.addArrayField;
import static org.camunda.bpm.engine.impl.util.JsonUtil.addDateField;
import static org.camunda.bpm.engine.impl.util.JsonUtil.addDefaultField;
import static org.camunda.bpm.engine.impl.util.JsonUtil.addField;
import static org.camunda.bpm.engine.impl.util.JsonUtil.addListField;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.QueryOperator;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.TaskQueryVariableValue;
import org.camunda.bpm.engine.impl.persistence.entity.SuspensionState;
import org.camunda.bpm.engine.impl.util.json.JSONArray;
import org.camunda.bpm.engine.impl.util.json.JSONObject;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.TaskQuery;

/**
 * @author Sebastian Menski
 */
public class JsonTaskQueryConverter extends JsonObjectConverter<TaskQuery> {

  public static final String ID = "id";
  public static final String TASK_ID = "taskId";
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
  public static final String EXECUTION_ID = "executionId";
  public static final String ACTIVITY_INSTANCE_ID_IN = "activityInstanceIdIn";
  public static final String CREATED = "created";
  public static final String CREATED_BEFORE = "createdBefore";
  public static final String CREATED_AFTER = "createdAfter";
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
  public JSONObject toJsonObject(TaskQuery taskQuery) {
    return toJsonObject(taskQuery, false);
  }

  public JSONObject toJsonObject(TaskQuery taskQuery, boolean isOrQueryActive) {
    JSONObject json = new JSONObject();
    TaskQueryImpl query = (TaskQueryImpl) taskQuery;

    addField(json, TASK_ID, query.getTaskId());
    addField(json, NAME, query.getName());
    addField(json, NAME_NOT_EQUAL, query.getNameNotEqual());
    addField(json, NAME_LIKE, query.getNameLike());
    addField(json, NAME_NOT_LIKE, query.getNameNotLike());
    addField(json, DESCRIPTION, query.getDescription());
    addField(json, DESCRIPTION_LIKE, query.getDescriptionLike());
    addField(json, PRIORITY, query.getPriority());
    addField(json, MIN_PRIORITY, query.getMinPriority());
    addField(json, MAX_PRIORITY, query.getMaxPriority());
    addField(json, ASSIGNEE, query.getAssignee());
    addField(json, ASSIGNEE_LIKE, query.getAssigneeLike());
    addField(json, INVOLVED_USER, query.getInvolvedUser());
    addField(json, OWNER, query.getOwner());
    addDefaultField(json, UNASSIGNED, false, query.isUnassigned());
    addDefaultField(json, ASSIGNED, false, query.isAssigned());
    addField(json, DELEGATION_STATE, query.getDelegationStateString());
    addField(json, CANDIDATE_USER, query.getCandidateUser());
    addField(json, CANDIDATE_GROUP, query.getCandidateGroup());
    addListField(json, CANDIDATE_GROUPS, query.getCandidateGroupsInternal());
    addDefaultField(json, WITH_CANDIDATE_GROUPS, false, query.isWithCandidateGroups());
    addDefaultField(json, WITHOUT_CANDIDATE_GROUPS, false, query.isWithoutCandidateGroups());
    addDefaultField(json, WITH_CANDIDATE_USERS, false, query.isWithCandidateUsers());
    addDefaultField(json, WITHOUT_CANDIDATE_USERS, false, query.isWithoutCandidateUsers());
    addField(json, INCLUDE_ASSIGNED_TASKS, query.isIncludeAssignedTasksInternal());
    addField(json, PROCESS_INSTANCE_ID, query.getProcessInstanceId());
    addField(json, EXECUTION_ID, query.getExecutionId());
    addArrayField(json, ACTIVITY_INSTANCE_ID_IN, query.getActivityInstanceIdIn());
    addDateField(json, CREATED, query.getCreateTime());
    addDateField(json, CREATED_BEFORE, query.getCreateTimeBefore());
    addDateField(json, CREATED_AFTER, query.getCreateTimeAfter());
    addField(json, KEY, query.getKey());
    addArrayField(json, KEYS, query.getKeys());
    addField(json, KEY_LIKE, query.getKeyLike());
    addField(json, PARENT_TASK_ID, query.getParentTaskId());
    addField(json, PROCESS_DEFINITION_KEY, query.getProcessDefinitionKey());
    addArrayField(json, PROCESS_DEFINITION_KEYS, query.getProcessDefinitionKeys());
    addField(json, PROCESS_DEFINITION_ID, query.getProcessDefinitionId());
    addField(json, PROCESS_DEFINITION_NAME, query.getProcessDefinitionName());
    addField(json, PROCESS_DEFINITION_NAME_LIKE, query.getProcessDefinitionNameLike());
    addField(json, PROCESS_INSTANCE_BUSINESS_KEY, query.getProcessInstanceBusinessKey());
    addArrayField(json, PROCESS_INSTANCE_BUSINESS_KEYS, query.getProcessInstanceBusinessKeys());
    addField(json, PROCESS_INSTANCE_BUSINESS_KEY_LIKE, query.getProcessInstanceBusinessKeyLike());
    addVariablesFields(json, query.getVariables());
    addDateField(json, DUE, query.getDueDate());
    addDateField(json, DUE_BEFORE, query.getDueBefore());
    addDateField(json, DUE_AFTER, query.getDueAfter());
    addDateField(json, FOLLOW_UP, query.getFollowUpDate());
    addDateField(json, FOLLOW_UP_BEFORE, query.getFollowUpBefore());
    addDefaultField(json, FOLLOW_UP_NULL_ACCEPTED, false, query.isFollowUpNullAccepted());
    addDateField(json, FOLLOW_UP_AFTER, query.getFollowUpAfter());
    addDefaultField(json, EXCLUDE_SUBTASKS, false, query.isExcludeSubtasks());
    addSuspensionStateField(json, query.getSuspensionState());
    addField(json, CASE_DEFINITION_KEY, query.getCaseDefinitionKey());
    addField(json, CASE_DEFINITION_ID, query.getCaseDefinitionId());
    addField(json, CASE_DEFINITION_NAME, query.getCaseDefinitionName());
    addField(json, CASE_DEFINITION_NAME_LIKE, query.getCaseDefinitionNameLike());
    addField(json, CASE_INSTANCE_ID, query.getCaseInstanceId());
    addField(json, CASE_INSTANCE_BUSINESS_KEY, query.getCaseInstanceBusinessKey());
    addField(json, CASE_INSTANCE_BUSINESS_KEY_LIKE, query.getCaseInstanceBusinessKeyLike());
    addField(json, CASE_EXECUTION_ID, query.getCaseExecutionId());
    addTenantIdFields(json, query);

    if (query.getQueries().size() > 1 && !isOrQueryActive) {
      JSONArray orQueries = new JSONArray();

      for (TaskQueryImpl orQuery: query.getQueries()) {
        if (orQuery != null && orQuery.isOrQueryActive()) {
          orQueries.put(toJsonObject(orQuery, true));
        }
      }

      addField(json, OR_QUERIES, orQueries);
    }

    if (query.getOrderingProperties() != null && !query.getOrderingProperties().isEmpty()) {
      addField(json, ORDERING_PROPERTIES,
          JsonQueryOrderingPropertyConverter.ARRAY_CONVERTER.toJsonArray(query.getOrderingProperties()));
    }


    // expressions
    for (Map.Entry<String, String> expressionEntry : query.getExpressions().entrySet()) {
      json.put(expressionEntry.getKey() + "Expression", expressionEntry.getValue());
    }

    return json;
  }

  protected void addSuspensionStateField(JSONObject json, SuspensionState suspensionState) {
    if (suspensionState != null) {
      if (suspensionState.equals(SuspensionState.ACTIVE)) {
        json.put(ACTIVE, true);
      }
      else if (suspensionState.equals(SuspensionState.SUSPENDED)) {
        json.put(SUSPENDED, true);
      }
    }
  }

  protected void addTenantIdFields(JSONObject json, TaskQueryImpl query) {
    if (query.isTenantIdSet()) {
      if (query.getTenantIds() != null) {
        addArrayField(json, TENANT_IDS, query.getTenantIds());
      } else {
        addField(json, WITHOUT_TENANT_ID, true);
      }
    }
  }

  protected void addVariablesFields(JSONObject json, List<TaskQueryVariableValue> variables) {
    for (TaskQueryVariableValue variable : variables) {
      if (variable.isProcessInstanceVariable()) {
        addVariable(json, PROCESS_VARIABLES, variable);
      }
      else if(variable.isLocal()) {
        addVariable(json, TASK_VARIABLES, variable);
      }
      else {
        addVariable(json, CASE_INSTANCE_VARIABLES, variable);
      }
    }
  }

  protected void addVariable(JSONObject json, String variableType, TaskQueryVariableValue variable) {
    JSONArray array = json.optJSONArray(variableType);
    if (array == null) {
      array = new JSONArray();
    }
    addVariable(array, variable);
    json.put(variableType, array);
  }

  protected void addVariable(JSONArray array, TaskQueryVariableValue variable) {
    array.put(variableValueConverter.toJsonObject(variable));
  }

  @Override
  public TaskQuery toObject(JSONObject json) {
    TaskQueryImpl query = new TaskQueryImpl();

    if (json.has(OR_QUERIES)) {
      for (int i = 0; i < json.getJSONArray(OR_QUERIES).length(); i++) {
        query.addOrQuery((TaskQueryImpl) toObject(json.getJSONArray(OR_QUERIES).getJSONObject(i)));
      }
    }
    if (json.has(TASK_ID)) {
      query.taskId(json.getString(TASK_ID));
    }
    if (json.has(NAME)) {
      query.taskName(json.getString(NAME));
    }
    if (json.has(NAME_NOT_EQUAL)) {
      query.taskNameNotEqual(json.getString(NAME_NOT_EQUAL));
    }
    if (json.has(NAME_LIKE)) {
      query.taskNameLike(json.getString(NAME_LIKE));
    }
    if (json.has(NAME_NOT_LIKE)) {
      query.taskNameNotLike(json.getString(NAME_NOT_LIKE));
    }
    if (json.has(DESCRIPTION)) {
      query.taskDescription(json.getString(DESCRIPTION));
    }
    if (json.has(DESCRIPTION_LIKE)) {
      query.taskDescriptionLike(json.getString(DESCRIPTION_LIKE));
    }
    if (json.has(PRIORITY)) {
      query.taskPriority(json.getInt(PRIORITY));
    }
    if (json.has(MIN_PRIORITY)) {
      query.taskMinPriority(json.getInt(MIN_PRIORITY));
    }
    if (json.has(MAX_PRIORITY)) {
      query.taskMaxPriority(json.getInt(MAX_PRIORITY));
    }
    if (json.has(ASSIGNEE)) {
      query.taskAssignee(json.getString(ASSIGNEE));
    }
    if (json.has(ASSIGNEE_LIKE)) {
      query.taskAssigneeLike(json.getString(ASSIGNEE_LIKE));
    }
    if (json.has(INVOLVED_USER)) {
      query.taskInvolvedUser(json.getString(INVOLVED_USER));
    }
    if (json.has(OWNER)) {
      query.taskOwner(json.getString(OWNER));
    }
    if (json.has(ASSIGNED) && json.getBoolean(ASSIGNED)) {
      query.taskAssigned();
    }
    if (json.has(UNASSIGNED) && json.getBoolean(UNASSIGNED)) {
      query.taskUnassigned();
    }
    if (json.has(DELEGATION_STATE)) {
      query.taskDelegationState(DelegationState.valueOf(json.getString(DELEGATION_STATE)));
    }
    if (json.has(CANDIDATE_USER)) {
      query.taskCandidateUser(json.getString(CANDIDATE_USER));
    }
    if (json.has(CANDIDATE_GROUP)) {
      query.taskCandidateGroup(json.getString(CANDIDATE_GROUP));
    }
    if (json.has(CANDIDATE_GROUPS) && !json.has(CANDIDATE_USER) && !json.has(CANDIDATE_GROUP)) {
      query.taskCandidateGroupIn(getList(json.getJSONArray(CANDIDATE_GROUPS)));
    }
    if (json.has(WITH_CANDIDATE_GROUPS) && json.getBoolean(WITH_CANDIDATE_GROUPS)) {
      query.withCandidateGroups();
    }
    if (json.has(WITHOUT_CANDIDATE_GROUPS) && json.getBoolean(WITHOUT_CANDIDATE_GROUPS)) {
      query.withoutCandidateGroups();
    }
    if (json.has(WITH_CANDIDATE_USERS) && json.getBoolean(WITH_CANDIDATE_USERS)) {
      query.withCandidateUsers();
    }
    if (json.has(WITHOUT_CANDIDATE_USERS) && json.getBoolean(WITHOUT_CANDIDATE_USERS)) {
      query.withoutCandidateUsers();
    }
    if (json.has(INCLUDE_ASSIGNED_TASKS) && json.getBoolean(INCLUDE_ASSIGNED_TASKS)) {
      query.includeAssignedTasksInternal();
    }
    if (json.has(PROCESS_INSTANCE_ID)) {
      query.processInstanceId(json.getString(PROCESS_INSTANCE_ID));
    }
    if (json.has(EXECUTION_ID)) {
      query.executionId(json.getString(EXECUTION_ID));
    }
    if (json.has(ACTIVITY_INSTANCE_ID_IN)) {
      query.activityInstanceIdIn(getArray(json.getJSONArray(ACTIVITY_INSTANCE_ID_IN)));
    }
    if (json.has(CREATED)) {
      query.taskCreatedOn(new Date(json.getLong(CREATED)));
    }
    if (json.has(CREATED_BEFORE)) {
      query.taskCreatedBefore(new Date(json.getLong(CREATED_BEFORE)));
    }
    if (json.has(CREATED_AFTER)) {
      query.taskCreatedAfter(new Date(json.getLong(CREATED_AFTER)));
    }
    if (json.has(KEY)) {
      query.taskDefinitionKey(json.getString(KEY));
    }
    if (json.has(KEYS)) {
      query.taskDefinitionKeyIn(getArray(json.getJSONArray(KEYS)));
    }
    if (json.has(KEY_LIKE)) {
      query.taskDefinitionKeyLike(json.getString(KEY_LIKE));
    }
    if (json.has(PARENT_TASK_ID)) {
      query.taskParentTaskId(json.getString(PARENT_TASK_ID));
    }
    if (json.has(PROCESS_DEFINITION_KEY)) {
      query.processDefinitionKey(json.getString(PROCESS_DEFINITION_KEY));
    }
    if (json.has(PROCESS_DEFINITION_KEYS)) {
      query.processDefinitionKeyIn(getArray(json.getJSONArray(PROCESS_DEFINITION_KEYS)));
    }
    if (json.has(PROCESS_DEFINITION_ID)) {
      query.processDefinitionId(json.getString(PROCESS_DEFINITION_ID));
    }
    if (json.has(PROCESS_DEFINITION_NAME)) {
      query.processDefinitionName(json.getString(PROCESS_DEFINITION_NAME));
    }
    if (json.has(PROCESS_DEFINITION_NAME_LIKE)) {
      query.processDefinitionNameLike(json.getString(PROCESS_DEFINITION_NAME_LIKE));
    }
    if (json.has(PROCESS_INSTANCE_BUSINESS_KEY)) {
      query.processInstanceBusinessKey(json.getString(PROCESS_INSTANCE_BUSINESS_KEY));
    }
    if (json.has(PROCESS_INSTANCE_BUSINESS_KEYS)) {
      query.processInstanceBusinessKeyIn(getArray(json.getJSONArray(PROCESS_INSTANCE_BUSINESS_KEYS)));
    }
    if (json.has(PROCESS_INSTANCE_BUSINESS_KEY_LIKE)) {
      query.processInstanceBusinessKeyLike(json.getString(PROCESS_INSTANCE_BUSINESS_KEY_LIKE));
    }
    if (json.has(TASK_VARIABLES)) {
      addVariables(query, json.getJSONArray(TASK_VARIABLES), true, false);
    }
    if (json.has(PROCESS_VARIABLES)) {
      addVariables(query, json.getJSONArray(PROCESS_VARIABLES), false, true);
    }
    if (json.has(CASE_INSTANCE_VARIABLES)) {
      addVariables(query, json.getJSONArray(CASE_INSTANCE_VARIABLES), false, false);
    }
    if (json.has(DUE)) {
      query.dueDate(new Date(json.getLong(DUE)));
    }
    if (json.has(DUE_BEFORE)) {
      query.dueBefore(new Date(json.getLong(DUE_BEFORE)));
    }
    if (json.has(DUE_AFTER)) {
      query.dueAfter(new Date(json.getLong(DUE_AFTER)));
    }
    if (json.has(FOLLOW_UP)) {
      query.followUpDate(new Date(json.getLong(FOLLOW_UP)));
    }
    if (json.has(FOLLOW_UP_BEFORE)) {
      query.followUpBefore(new Date(json.getLong(FOLLOW_UP_BEFORE)));
    }
    if (json.has(FOLLOW_UP_AFTER)) {
      query.followUpAfter(new Date(json.getLong(FOLLOW_UP_AFTER)));
    }
    if (json.has(FOLLOW_UP_NULL_ACCEPTED)) {
      query.setFollowUpNullAccepted(json.getBoolean(FOLLOW_UP_NULL_ACCEPTED));
    }
    if (json.has(EXCLUDE_SUBTASKS) && json.getBoolean(EXCLUDE_SUBTASKS)) {
      query.excludeSubtasks();
    }
    if (json.has(SUSPENDED) && json.getBoolean(SUSPENDED)) {
      query.suspended();
    }
    if (json.has(ACTIVE) && json.getBoolean(ACTIVE)) {
      query.active();
    }
    if (json.has(CASE_DEFINITION_KEY)) {
      query.caseDefinitionKey(json.getString(CASE_DEFINITION_KEY));
    }
    if (json.has(CASE_DEFINITION_ID)) {
      query.caseDefinitionId(json.getString(CASE_DEFINITION_ID));
    }
    if (json.has(CASE_DEFINITION_NAME)) {
      query.caseDefinitionName(json.getString(CASE_DEFINITION_NAME));
    }
    if (json.has(CASE_DEFINITION_NAME_LIKE)) {
      query.caseDefinitionNameLike(json.getString(CASE_DEFINITION_NAME_LIKE));
    }
    if (json.has(CASE_INSTANCE_ID)) {
      query.caseInstanceId(json.getString(CASE_INSTANCE_ID));
    }
    if (json.has(CASE_INSTANCE_BUSINESS_KEY)) {
      query.caseInstanceBusinessKey(json.getString(CASE_INSTANCE_BUSINESS_KEY));
    }
    if (json.has(CASE_INSTANCE_BUSINESS_KEY_LIKE)) {
      query.caseInstanceBusinessKeyLike(json.getString(CASE_INSTANCE_BUSINESS_KEY_LIKE));
    }
    if (json.has(CASE_EXECUTION_ID)) {
      query.caseExecutionId(json.getString(CASE_EXECUTION_ID));
    }
    if (json.has(TENANT_IDS)) {
      query.tenantIdIn(getArray(json.getJSONArray(TENANT_IDS)));
    }
    if (json.has(WITHOUT_TENANT_ID)) {
      query.withoutTenantId();
    }
    if (json.has(ORDER_BY)) {
      List<QueryOrderingProperty> orderingProperties =
          JsonLegacyQueryOrderingPropertyConverter.INSTANCE.fromOrderByString(json.getString(ORDER_BY));

      query.setOrderingProperties(orderingProperties);
    }
    if (json.has(ORDERING_PROPERTIES)) {
      JSONArray jsonArray = json.getJSONArray(ORDERING_PROPERTIES);
      query.setOrderingProperties(JsonQueryOrderingPropertyConverter.ARRAY_CONVERTER.toObject(jsonArray));
    }

    // expressions
    Iterator jsonIterator = json.keys();
    while (jsonIterator.hasNext()) {
      String key = (String) jsonIterator.next();
      if (key.endsWith("Expression")) {
        String expression = json.getString(key);
        query.addExpression(key.substring(0, key.length() - "Expression".length()), expression);
      }
    }

    return query;
  }

  private String[] getArray(JSONArray array) {
    return getList(array).toArray(new String[array.length()]);
  }

  protected List<String> getList(JSONArray array) {
    List<String> list = new ArrayList<String>();
    for (int i = 0; i < array.length(); i++) {
      list.add(array.getString(i));
    }
    return list;
  }

  private void addVariables(TaskQueryImpl query, JSONArray variables, boolean isTaskVariable, boolean isProcessVariable) {
    for (int i = 0; i < variables.length(); i++) {
      JSONObject variable = variables.getJSONObject(i);
      String name = variable.getString(NAME);
      Object value = variable.get("value");
      QueryOperator operator = QueryOperator.valueOf(variable.getString("operator"));
      query.addVariable(name, value, operator, isTaskVariable, isProcessVariable);
    }
  }

}
