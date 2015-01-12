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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.Direction;
import org.camunda.bpm.engine.impl.QueryOperator;
import org.camunda.bpm.engine.impl.TaskQueryImpl;
import org.camunda.bpm.engine.impl.TaskQueryVariableValue;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
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
  public static final String NAME_LIKE = "nameLike";
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
  public static final String DELEGATION_STATE = "delegationState";
  public static final String CANDIDATE_USER = "candidateUser";
  public static final String CANDIDATE_GROUP = "candidateGroup";
  public static final String CANDIDATE_GROUPS = "candidateGroups";
  public static final String INSTANCE_ID = "instanceId";
  public static final String PROCESS_INSTANCE_ID = "processInstanceId";
  public static final String EXECUTION_ID = "executionId";
  public static final String ACTIVITY_INSTANCE_ID_IN = "activityInstanceIdIn";
  public static final String CREATED = "created";
  public static final String CREATED_BEFORE = "createdBefore";
  public static final String CREATED_AFTER = "createdAfter";
  public static final String KEY = "key";
  public static final String KEY_LIKE = "keyLike";
  public static final String PROCESS_DEFINITION_KEY = "processDefinitionKey";
  public static final String PROCESS_DEFINITION_ID = "processDefinitionId";
  public static final String PROCESS_DEFINITION_NAME = "processDefinitionName";
  public static final String PROCESS_DEFINITION_NAME_LIKE = "processDefinitionNameLike";
  public static final String PROCESS_INSTANCE_BUSINESS_KEY = "processInstanceBusinessKey";
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
  public static final String ORDER_BY = "orderBy";
  public static final String ACTIVE = "active";
  public static final String SUSPENDED = "suspended";
  public static final String PROCESS_VARIABLES = "processVariables";
  public static final String TASK_VARIABLES = "taskVariables";
  public static final String CASE_INSTANCE_VARIABLES = "caseInstanceVariables";
  public static final String SORT_BY = "sortBy";
  public static final String SORT_ORDER = "sortOrder";
  protected static JsonTaskQueryVariableValueConverter variableValueConverter = new JsonTaskQueryVariableValueConverter();

  public JSONObject toJsonObject(TaskQuery taskQuery) {
    JSONObject json = new JSONObject();
    TaskQueryImpl query = (TaskQueryImpl) taskQuery;

    addField(json, TASK_ID, query.getTaskId());
    addField(json, NAME, query.getName());
    addField(json, NAME_LIKE, query.getNameLike());
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
    addField(json, DELEGATION_STATE, query.getDelegationStateString());
    addField(json, CANDIDATE_USER, query.getCandidateUser());
    addField(json, CANDIDATE_GROUP, query.getCandidateGroup());
    addListField(json, CANDIDATE_GROUPS, query.getCandidateGroupsInternal());
    addField(json, PROCESS_INSTANCE_ID, query.getProcessInstanceId());
    addField(json, EXECUTION_ID, query.getExecutionId());
    addArrayField(json, ACTIVITY_INSTANCE_ID_IN, query.getActivityInstanceIdIn());
    addDateField(json, CREATED, query.getCreateTime());
    addDateField(json, CREATED_BEFORE, query.getCreateTimeBefore());
    addDateField(json, CREATED_AFTER, query.getCreateTimeAfter());
    addField(json, KEY, query.getKey());
    addField(json, KEY_LIKE, query.getKeyLike());
    addField(json, PROCESS_DEFINITION_KEY, query.getProcessDefinitionKey());
    addField(json, PROCESS_DEFINITION_ID, query.getProcessDefinitionId());
    addField(json, PROCESS_DEFINITION_NAME, query.getProcessDefinitionName());
    addField(json, PROCESS_DEFINITION_NAME_LIKE, query.getProcessDefinitionNameLike());
    addField(json, PROCESS_INSTANCE_BUSINESS_KEY, query.getProcessInstanceBusinessKey());
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
    addSuspensionState(json, query.getSuspensionState());
    addField(json, CASE_DEFINITION_KEY, query.getCaseDefinitionKey());
    addField(json, CASE_DEFINITION_ID, query.getCaseDefinitionId());
    addField(json, CASE_DEFINITION_NAME, query.getCaseDefinitionName());
    addField(json, CASE_DEFINITION_NAME_LIKE, query.getCaseDefinitionNameLike());
    addField(json, CASE_INSTANCE_ID, query.getCaseInstanceId());
    addField(json, CASE_INSTANCE_BUSINESS_KEY, query.getCaseInstanceBusinessKey());
    addField(json, CASE_INSTANCE_BUSINESS_KEY_LIKE, query.getCaseInstanceBusinessKeyLike());
    addField(json, CASE_EXECUTION_ID, query.getCaseExecutionId());
    addDefaultField(json, ORDER_BY, ListQueryParameterObject.DEFAULT_ORDER_BY, query.getOrderBy());

    // expressions
    for (Map.Entry<String, String> expressionEntry : query.getExpressions().entrySet()) {
      json.put(expressionEntry.getKey() + "Expression", expressionEntry.getValue());
    }

    return json;
  }

  protected void addField(JSONObject json, String name, Object value) {
    if (value != null) {
      json.put(name, value);
    }
  }

  protected void addDefaultField(JSONObject json, String name, Object defaultValue, Object value) {
    if (value != null && !value.equals(defaultValue)) {
      json.put(name, value);
    }
  }

  protected void addListField(JSONObject json, String name, List<String> list) {
    if (list != null) {
      json.put(name, new JSONArray(list));
    }
  }

  protected void addArrayField(JSONObject json, String name, String[] array) {
    if (array != null) {
      addListField(json, name, Arrays.asList(array));
    }
  }

  protected void addDateField(JSONObject json, String name, Date date) {
    if (date != null) {
      json.put(name, date.getTime());
    }
  }

  private void addSuspensionState(JSONObject json, SuspensionState suspensionState) {
    if (suspensionState != null) {
      if (suspensionState.equals(SuspensionState.ACTIVE)) {
        json.put(ACTIVE, true);
      }
      else if (suspensionState.equals(SuspensionState.SUSPENDED)) {
        json.put(SUSPENDED, true);
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

  public TaskQuery toObject(JSONObject json) {
    TaskQueryImpl query = new TaskQueryImpl();

    if (json.has(TASK_ID)) {
      query.taskId(json.getString(TASK_ID));
    }
    if (json.has(NAME)) {
      query.taskName(json.getString(NAME));
    }
    if (json.has(NAME_LIKE)) {
      query.taskNameLike(json.getString(NAME_LIKE));
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
    if (json.has(KEY_LIKE)) {
      query.taskDefinitionKeyLike(json.getString(KEY_LIKE));
    }
    if (json.has(PROCESS_DEFINITION_KEY)) {
      query.processDefinitionKey(json.getString(PROCESS_DEFINITION_KEY));
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
    if (json.has(ORDER_BY)) {
      query.setOrderBy(json.getString(ORDER_BY));
    }
    if (json.has(SORT_BY)) {
      setSortBy(query, json.getString(SORT_BY));
    }
    if (json.has(SORT_ORDER)) {
      String sortOrder = json.getString(SORT_ORDER);
      if (Direction.ASCENDING.getName().equals(sortOrder)) {
        query.asc();
      }
      else if (Direction.DESCENDING.getName().equals(sortOrder)) {
        query.desc();
      }
      else {
        throw new NotValidException("Unknown sort ordering '" + sortOrder + "' in query");
      }
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

  protected void setSortBy(TaskQuery query, String sortBy) {
    if (ID.equals(sortBy)) {
      query.orderByTaskId();
    }
    else if (NAME.equals(sortBy)) {
      query.orderByTaskName();
    }
    else if (DESCRIPTION.equals(sortBy)) {
      query.orderByTaskDescription();
    }
    else if (PRIORITY.equals(sortBy)) {
      query.orderByTaskPriority();
    }
    else if (INSTANCE_ID.equals(sortBy)) {
      query.orderByProcessInstanceId();
    }
    else if (CASE_INSTANCE_ID.equals(sortBy)) {
      query.orderByCaseInstanceId();
    }
    else if (EXECUTION_ID.equals(sortBy)) {
      query.orderByExecutionId();
    }
    else if (CASE_EXECUTION_ID.equals(sortBy)) {
      query.orderByCaseExecutionId();
    }
    else if (ASSIGNEE.equals(sortBy)) {
      query.orderByTaskAssignee();
    }
    else if (CREATED.equals(sortBy)) {
      query.orderByTaskCreateTime();
    }
    else if (DUE_DATE.equals(sortBy)) {
      query.orderByDueDate();
    }
    else if (FOLLOW_UP_DATE.equals(sortBy)) {
      query.orderByFollowUpDate();
    }
    else {
      throw new NotValidException("Unknown sort by '" + sortBy + "' in query");
    }
  }

}
