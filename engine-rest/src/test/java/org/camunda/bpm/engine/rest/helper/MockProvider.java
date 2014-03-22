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
package org.camunda.bpm.engine.rest.helper;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.application.ProcessApplicationInfo;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permission;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.form.FormProperty;
import org.camunda.bpm.engine.form.FormType;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityStatistics;
import org.camunda.bpm.engine.history.HistoricDetail;
import org.camunda.bpm.engine.history.HistoricFormField;
import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableUpdate;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.impl.identity.Authentication;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricDetailVariableInstanceUpdateEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.variable.StringType;
import org.camunda.bpm.engine.management.ActivityStatistics;
import org.camunda.bpm.engine.management.IncidentStatistics;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.IdentityLink;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;

/**
 * Provides mocks for the basic engine entities, such as
 * {@link ProcessDefinition}, {@link User}, etc., that are reused across the
 * various kinds of tests.
 *
 * @author Thorben Lindhauer
 *
 */
public abstract class MockProvider {

  // engine
  public static final String EXAMPLE_PROCESS_ENGINE_NAME = "default";
  public static final String ANOTHER_EXAMPLE_PROCESS_ENGINE_NAME = "anotherEngineName";
  public static final String NON_EXISTING_PROCESS_ENGINE_NAME = "aNonExistingEngineName";

  // task properties
  public static final String EXAMPLE_TASK_ID = "anId";
  public static final String EXAMPLE_TASK_NAME = "aName";
  public static final String EXAMPLE_TASK_ASSIGNEE_NAME = "anAssignee";
  public static final String EXAMPLE_TASK_CREATE_TIME = "2013-01-23T13:42:42";
  public static final String EXAMPLE_TASK_DUE_DATE = "2013-01-23T13:42:43";
  public static final String EXAMPLE_FOLLOW_UP_DATE = "2013-01-23T13:42:44";
  public static final DelegationState EXAMPLE_TASK_DELEGATION_STATE = DelegationState.RESOLVED;
  public static final String EXAMPLE_TASK_DESCRIPTION = "aDescription";
  public static final String EXAMPLE_TASK_EXECUTION_ID = "anExecution";
  public static final String EXAMPLE_TASK_OWNER = "anOwner";
  public static final String EXAMPLE_TASK_PARENT_TASK_ID = "aParentId";
  public static final int EXAMPLE_TASK_PRIORITY = 42;
  public static final String EXAMPLE_TASK_DEFINITION_KEY = "aTaskDefinitionKey";

  // form data
  public static final String EXAMPLE_FORM_KEY = "aFormKey";
  public static final String EXAMPLE_DEPLOYMENT_ID = "aDeploymentId";

  // form property data
  public static final String EXAMPLE_FORM_PROPERTY_ID = "aFormPropertyId";
  public static final String EXAMPLE_FORM_PROPERTY_NAME = "aFormName";
  public static final String EXAMPLE_FORM_PROPERTY_TYPE_NAME = "aFormPropertyTypeName";
  public static final String EXAMPLE_FORM_PROPERTY_VALUE = "aValue";
  public static final boolean EXAMPLE_FORM_PROPERTY_READABLE = true;
  public static final boolean EXAMPLE_FORM_PROPERTY_WRITABLE = true;
  public static final boolean EXAMPLE_FORM_PROPERTY_REQUIRED = true;

  // process instance
  public static final String EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY = "aKey";
  public static final String EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY_LIKE = "aKeyLike";
  public static final String EXAMPLE_PROCESS_INSTANCE_ID = "aProcInstId";
  public static final String ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID = "anotherId";
  public static final boolean EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED = false;
  public static final boolean EXAMPLE_PROCESS_INSTANCE_IS_ENDED = false;
  public static final String EXAMPLE_PROCESS_INSTANCE_ID_LIST = EXAMPLE_PROCESS_INSTANCE_ID + "," + ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID;
  public static final String EXAMPLE_PROCESS_INSTANCE_ID_LIST_WITH_DUP = EXAMPLE_PROCESS_INSTANCE_ID + "," + ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID + "," + EXAMPLE_PROCESS_INSTANCE_ID;
  public static final String EXAMPLE_NON_EXISTENT_PROCESS_INSTANCE_ID = "aNonExistentProcInstId";
  public static final String EXAMPLE_PROCESS_INSTANCE_ID_LIST_WITH_NONEXISTENT_ID = EXAMPLE_PROCESS_INSTANCE_ID + "," + EXAMPLE_NON_EXISTENT_PROCESS_INSTANCE_ID;


  // variable instance
  public static final String EXAMPLE_VARIABLE_INSTANCE_ID = "aVariableInstanceId";
  public static final String EXAMPLE_VARIABLE_INSTANCE_NAME = "aVariableInstanceName";
  public static final String EXAMPLE_VARIABLE_INSTANCE_TYPE = "String";
  public static final String EXAMPLE_VARIABLE_INSTANCE_VALUE = "aVariableInstanceValue";
  public static final String EXAMPLE_VARIABLE_INSTANCE_PROC_INST_ID = "aVariableInstanceProcInstId";
  public static final String EXAMPLE_VARIABLE_INSTANCE_EXECUTION_ID = "aVariableInstanceExecutionId";
  public static final String EXAMPLE_VARIABLE_INSTANCE_TASK_ID = "aVariableInstanceTaskId";
  public static final String EXAMPLE_VARIABLE_INSTANCE_ACTIVITY_INSTANCE_ID = "aVariableInstanceVariableInstanceId";
  public static final String EXAMPLE_VARIABLE_INSTANCE_ERROR_MESSAGE = "aVariableInstanceErrorMessage";

  // execution
  public static final String EXAMPLE_EXECUTION_ID = "anExecutionId";
  public static final boolean EXAMPLE_EXECUTION_IS_ENDED = false;

  // event subscription
  public static final String EXAMPLE_EVENT_SUBSCRIPTION_ID = "anEventSubscriptionId";
  public static final String EXAMPLE_EVENT_SUBSCRIPTION_TYPE = "message";
  public static final String EXAMPLE_EVENT_SUBSCRIPTION_NAME = "anEvent";
  public static final String EXAMPLE_EVENT_SUBSCRIPTION_CREATION_DATE = "2013-01-23T13:59:43";

  // process definition
  public static final String EXAMPLE_PROCESS_DEFINITION_ID = "aProcDefId";
  public static final String NON_EXISTING_PROCESS_DEFINITION_ID = "aNonExistingProcDefId";
  public static final String EXAMPLE_PROCESS_DEFINITION_NAME = "aName";
  public static final String EXAMPLE_PROCESS_DEFINITION_NAME_LIKE = "aNameLike";
  public static final String EXAMPLE_PROCESS_DEFINITION_KEY = "aKey";
  public static final String NON_EXISTING_PROCESS_DEFINITION_KEY = "aNonExistingKey";
  public static final String EXAMPLE_PROCESS_DEFINITION_CATEGORY = "aCategory";
  public static final String EXAMPLE_PROCESS_DEFINITION_DESCRIPTION = "aDescription";
  public static final int EXAMPLE_PROCESS_DEFINITION_VERSION = 42;
  public static final String EXAMPLE_PROCESS_DEFINITION_RESOURCE_NAME = "aResourceName";
  public static final String EXAMPLE_PROCESS_DEFINITION_DIAGRAM_RESOURCE_NAME = "aResourceName.png";
  public static final boolean EXAMPLE_PROCESS_DEFINITION_IS_SUSPENDED = true;

  public static final String ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID = "aProcessDefinitionId:2";

  public static final String EXAMPLE_ACTIVITY_ID = "anActivity";
  public static final String ANOTHER_EXAMPLE_ACTIVITY_ID = "anotherActivity";
  public static final String EXAMPLE_ACTIVITY_NAME = "anActivityName";
  public static final String EXAMPLE_ACTIVITY_TYPE = "anActivityType";
  public static final String EXAMPLE_PROCESS_DEFINITION_DELAYED_EXECUTION = "2013-04-23T13:42:43";

  // statistics
  public static final int EXAMPLE_FAILED_JOBS = 42;
  public static final int EXAMPLE_INSTANCES = 123;

  public static final long EXAMPLE_INSTANCES_LONG = 123;
  public static final long EXAMPLE_FINISHED_LONG = 124;
  public static final long EXAMPLE_CANCELED_LONG = 125;
  public static final long EXAMPLE_COMPLETE_SCOPE_LONG = 126;

  public static final long ANOTHER_EXAMPLE_INSTANCES_LONG = 127;
  public static final long ANOTHER_EXAMPLE_FINISHED_LONG = 128;
  public static final long ANOTHER_EXAMPLE_CANCELED_LONG = 129;
  public static final long ANOTHER_EXAMPLE_COMPLETE_SCOPE_LONG = 130;

  public static final int ANOTHER_EXAMPLE_FAILED_JOBS = 43;
  public static final int ANOTHER_EXAMPLE_INSTANCES = 124;

  public static final String ANOTHER_EXAMPLE_INCIDENT_TYPE = "anotherIncidentType";
  public static final int ANOTHER_EXAMPLE_INCIDENT_COUNT = 2;

  // user & groups
  public static final String EXAMPLE_GROUP_ID = "groupId1";
  public static final String EXAMPLE_GROUP_ID2 = "groupId2";
  public static final String EXAMPLE_GROUP_NAME = "group1";
  public static final String EXAMPLE_GROUP_TYPE = "organizational-unit";
  public static final String EXAMPLE_GROUP_NAME_UPDATE = "group1Update";

  public static final String EXAMPLE_USER_ID = "userId";
  public static final String EXAMPLE_USER_ID2 = "userId2";
  public static final String EXAMPLE_USER_FIRST_NAME = "firstName";
  public static final String EXAMPLE_USER_LAST_NAME = "lastName";
  public static final String EXAMPLE_USER_EMAIL = "test@example.org";
  public static final String EXAMPLE_USER_PASSWORD = "s3cret";

  public static final String EXAMPLE_USER_FIRST_NAME_UPDATE = "firstNameUpdate";
  public static final String EXAMPLE_USER_LAST_NAME_UPDATE = "lastNameUpdate";
  public static final String EXAMPLE_USER_EMAIL_UPDATE = "testUpdate@example.org";

  // Job Definitions
  public static final String EXAMPLE_JOB_DEFINITION_ID = "aJobDefId";
  public static final String NON_EXISTING_JOB_DEFINITION_ID = "aNonExistingJobDefId";
  public static final String EXAMPLE_JOB_TYPE = "aJobType";
  public static final String EXAMPLE_JOB_CONFIG = "aJobConfig";
  public static final boolean EXAMPLE_JOB_DEFINITION_IS_SUSPENDED = true;
  public static final String EXAMPLE_JOB_DEFINITION_DELAYED_EXECUTION = "2013-04-23T13:42:43";

  // Jobs
  public static final String EXAMPLE_JOB_ID = "aJobId";
  public static final String NON_EXISTING_JOB_ID = "aNonExistingJobId";
  public static final int EXAMPLE_NEGATIVE_JOB_RETRIES = -3;
  public static final int EXAMPLE_JOB_RETRIES = 3;
  public static final String EXAMPLE_JOB_NO_EXCEPTION_MESSAGE = "";
  public static final String EXAMPLE_EXCEPTION_MESSAGE = "aExceptionMessage";
  public static final String EXAMPLE_EMPTY_JOB_ID = "";
  public static final String EXAMPLE_DUE_DATE =  "2013-04-23T13:42:43";
  public static final Boolean EXAMPLE_WITH_RETRIES_LEFT = true;
  public static final Boolean EXAMPLE_EXECUTABLE = true;
  public static final Boolean EXAMPLE_TIMERS = true;
  public static final Boolean EXAMPLE_MESSAGES = true;
  public static final Boolean EXAMPLE_WITH_EXCEPTION = true;
  public static final Boolean EXAMPLE_NO_RETRIES_LEFT = true;
  public static final Boolean EXAMPLE_JOB_IS_SUSPENDED = true;

  public static final String EXAMPLE_RESOURCE_TYPE_NAME = "exampleResource";
  public static final int EXAMPLE_RESOURCE_TYPE_ID = 12345678;
  public static final String EXAMPLE_RESOURCE_TYPE_ID_STRING = "12345678";
  public static final String EXAMPLE_RESOURCE_ID = "exampleResourceId";
  public static final String EXAMPLE_PERMISSION_NAME = "READ";
  public static final Permission[] EXAMPLE_PERMISSION_VALUES = new Permission[] { Permissions.READ, Permissions.UPDATE };
  public static final String[] EXAMPLE_PERMISSION_VALUES_STRING = new String[] { "READ", "UPDATE" };

  public static final String EXAMPLE_AUTHORIZATION_ID = "someAuthorizationId";
  public static final int EXAMPLE_AUTHORIZATION_TYPE = 0;
  public static final String EXAMPLE_AUTHORIZATION_TYPE_STRING = "0";

  // process applications
  public static final String EXAMPLE_PROCESS_APPLICATION_NAME = "aProcessApplication";
  public static final String EXAMPLE_PROCESS_APPLICATION_CONTEXT_PATH = "http://camunda.org/someContext";

  // Historic Process Instance
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_DELETE_REASON = "aDeleteReason";
  public static final long EXAMPLE_HISTORIC_PROCESS_INSTANCE_DURATION_MILLIS = 2000l;
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_TIME = "2013-04-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_END_TIME = "2013-04-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_USER_ID = "aStartUserId";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_ACTIVITY_ID = "aStartActivityId";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_SUPER_PROCESS_INSTANCE_ID = "aSuperProcessInstanceId";

  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_AFTER = "2013-04-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_STARTED_BEFORE = "2013-01-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_FINISHED_AFTER = "2013-01-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_PROCESS_INSTANCE_FINISHED_BEFORE = "2013-04-23T13:42:43";

  // Historic Activity Instance
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_ID = "aHistoricActivityInstanceId";
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_PARENT_ACTIVITY_INSTANCE_ID = "aHistoricParentActivityInstanceId";
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_CALLED_PROCESS_INSTANCE_ID = "aHistoricCalledProcessInstanceId";
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_START_TIME = "2013-04-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_END_TIME = "2013-04-23T18:42:43";
  public static final long EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_DURATION = 2000l;
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_STARTED_AFTER = "2013-04-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_STARTED_BEFORE = "2013-01-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_FINISHED_AFTER = "2013-01-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_FINISHED_BEFORE = "2013-04-23T13:42:43";
  public static final boolean EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_IS_CANCELED = true;
  public static final boolean EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_IS_COMPLETE_SCOPE = true;

  // user operation log
  public static final String EXAMPLE_USER_OPERATION_LOG_ID = "userOpLogId";
  public static final String EXAMPLE_USER_OPERATION_ID = "opId";
  public static final String EXAMPLE_USER_OPERATION_TYPE = UserOperationLogEntry.OPERATION_TYPE_CLAIM;
  public static final String EXAMPLE_USER_OPERATION_ENTITY = UserOperationLogEntry.ENTITY_TYPE_TASK;
  public static final String EXAMPLE_USER_OPERATION_PROPERTY = "opProperty";
  public static final String EXAMPLE_USER_OPERATION_ORG_VALUE = "orgValue";
  public static final String EXAMPLE_USER_OPERATION_NEW_VALUE = "newValue";
  public static final String EXAMPLE_USER_OPERATION_TIMESTAMP = "2014-02-20T16:53:37";

  // historic detail
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_ID = "aHistoricVariableUpdateId";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_PROC_INST_ID = "aProcInst";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_ACT_INST_ID = "anActInst";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_EXEC_ID = "anExecutionId";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_TASK_ID = "aTaskId";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_TIME = "2014-01-01T00:00:00";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_NAME = "aVariableName";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_TYPE_NAME = "String";
  public static final String EXAMPLE_HISTORIC_VAR_UPDATE_VALUE = "aValue";
  public static final int EXAMPLE_HISTORIC_VAR_UPDATE_REVISION = 1;

  public static final String EXAMPLE_HISTORIC_FORM_FIELD_ID = "anId";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_PROC_INST_ID = "aProcInst";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_ACT_INST_ID = "anActInst";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_EXEC_ID = "anExecutionId";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_TASK_ID = "aTaskId";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_TIME = "2014-01-01T00:00:00";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_FIELD_ID = "aFormFieldId";
  public static final String EXAMPLE_HISTORIC_FORM_FIELD_VALUE = "aFormFieldValue";

  // historic task instance
  public static final String EXAMPLE_HISTORIC_TASK_INST_ID = "aHistoricTaskInstanceId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_PROC_DEF_ID = "aProcDefId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_PROC_INST_ID = "aProcInstId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_EXEC_ID = "anExecId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_ACT_INST_ID = "anActInstId";
  public static final String EXAMPLE_HISTORIC_TASK_INST_NAME = "aName";
  public static final String EXAMPLE_HISTORIC_TASK_INST_DESCRIPTION = "aDescription";
  public static final String EXAMPLE_HISTORIC_TASK_INST_DELETE_REASON = "aDeleteReason";
  public static final String EXAMPLE_HISTORIC_TASK_INST_OWNER = "anOwner";
  public static final String EXAMPLE_HISTORIC_TASK_INST_ASSIGNEE = "anAssignee";
  public static final String EXAMPLE_HISTORIC_TASK_INST_START_TIME = "2014-01-01T00:00:00";
  public static final String EXAMPLE_HISTORIC_TASK_INST_END_TIME = "2014-01-01T00:00:00";
  public static final Long EXAMPLE_HISTORIC_TASK_INST_DURATION = 5000L;
  public static final String EXAMPLE_HISTORIC_TASK_INST_DEF_KEY = "aTaskDefinitionKey";
  public static final int EXAMPLE_HISTORIC_TASK_INST_PRIORITY = 60;
  public static final String EXAMPLE_HISTORIC_TASK_INST_DUE_DATE = "2014-01-01T00:00:00";
  public static final String EXAMPLE_HISTORIC_TASK_INST_FOLLOW_UP_DATE = "2014-01-01T00:00:00";
  public static final String EXAMPLE_HISTORIC_TASK_INST_PARENT_TASK_ID = "aParentTaskId";

  // Incident
  public static final String EXAMPLE_INCIDENT_ID = "anIncidentId";
  public static final String EXAMPLE_INCIDENT_TIMESTAMP = "2014-01-01T00:00:00";
  public static final String EXAMPLE_INCIDENT_TYPE = "anIncidentType";
  public static final String EXAMPLE_INCIDENT_EXECUTION_ID = "anExecutionId";
  public static final String EXAMPLE_INCIDENT_ACTIVITY_ID = "anActivityId";
  public static final String EXAMPLE_INCIDENT_PROC_INST_ID = "aProcInstId";
  public static final String EXAMPLE_INCIDENT_PROC_DEF_ID = "aProcDefId";
  public static final String EXAMPLE_INCIDENT_CAUSE_INCIDENT_ID = "aCauseIncidentId";
  public static final String EXAMPLE_INCIDENT_ROOT_CAUSE_INCIDENT_ID = "aRootCauseIncidentId";
  public static final String EXAMPLE_INCIDENT_CONFIGURATION = "aConfiguration";
  public static final String EXAMPLE_INCIDENT_MESSAGE = "anIncidentMessage";

  public static final int EXAMPLE_INCIDENT_COUNT = 1;

  // Historic Incident
  public static final String EXAMPLE_HIST_INCIDENT_ID = "anIncidentId";
  public static final String EXAMPLE_HIST_INCIDENT_CREATE_TIME = "2014-01-01T00:00:00";
  public static final String EXAMPLE_HIST_INCIDENT_END_TIME = "2014-01-01T00:00:00";
  public static final String EXAMPLE_HIST_INCIDENT_TYPE = "anIncidentType";
  public static final String EXAMPLE_HIST_INCIDENT_EXECUTION_ID = "anExecutionId";
  public static final String EXAMPLE_HIST_INCIDENT_ACTIVITY_ID = "anActivityId";
  public static final String EXAMPLE_HIST_INCIDENT_PROC_INST_ID = "aProcInstId";
  public static final String EXAMPLE_HIST_INCIDENT_PROC_DEF_ID = "aProcDefId";
  public static final String EXAMPLE_HIST_INCIDENT_CAUSE_INCIDENT_ID = "aCauseIncidentId";
  public static final String EXAMPLE_HIST_INCIDENT_ROOT_CAUSE_INCIDENT_ID = "aRootCauseIncidentId";
  public static final String EXAMPLE_HIST_INCIDENT_CONFIGURATION = "aConfiguration";
  public static final String EXAMPLE_HIST_INCIDENT_MESSAGE = "anIncidentMessage";
  public static final boolean EXAMPLE_HIST_INCIDENT_STATE_OPEN = false;
  public static final boolean EXAMPLE_HIST_INCIDENT_STATE_DELETED = false;
  public static final boolean EXAMPLE_HIST_INCIDENT_STATE_RESOLVED = true;

  // tasks
  public static Task createMockTask() {
    Task mockTask = new MockTaskBuilder().id(EXAMPLE_TASK_ID).name(EXAMPLE_TASK_NAME).assignee(EXAMPLE_TASK_ASSIGNEE_NAME)
        .createTime(DateTimeUtil.parseDateTime(EXAMPLE_TASK_CREATE_TIME).toDate()).dueDate(DateTimeUtil.parseDateTime(EXAMPLE_TASK_DUE_DATE).toDate())
        .followUpDate(DateTimeUtil.parseDateTime(EXAMPLE_FOLLOW_UP_DATE).toDate())
        .delegationState(EXAMPLE_TASK_DELEGATION_STATE).description(EXAMPLE_TASK_DESCRIPTION).executionId(EXAMPLE_TASK_EXECUTION_ID).owner(EXAMPLE_TASK_OWNER)
        .parentTaskId(EXAMPLE_TASK_PARENT_TASK_ID).priority(EXAMPLE_TASK_PRIORITY).processDefinitionId(EXAMPLE_PROCESS_DEFINITION_ID)
        .processInstanceId(EXAMPLE_PROCESS_INSTANCE_ID).taskDefinitionKey(EXAMPLE_TASK_DEFINITION_KEY).build();
    return mockTask;
  }

  public static List<Task> createMockTasks() {
    List<Task> mocks = new ArrayList<Task>();
    mocks.add(createMockTask());
    return mocks;
  }

  public static TaskFormData createMockTaskFormData() {
    FormProperty mockFormProperty = mock(FormProperty.class);
    when(mockFormProperty.getId()).thenReturn(EXAMPLE_FORM_PROPERTY_ID);
    when(mockFormProperty.getName()).thenReturn(EXAMPLE_FORM_PROPERTY_NAME);
    when(mockFormProperty.getValue()).thenReturn(EXAMPLE_FORM_PROPERTY_VALUE);
    when(mockFormProperty.isReadable()).thenReturn(EXAMPLE_FORM_PROPERTY_READABLE);
    when(mockFormProperty.isWritable()).thenReturn(EXAMPLE_FORM_PROPERTY_WRITABLE);
    when(mockFormProperty.isRequired()).thenReturn(EXAMPLE_FORM_PROPERTY_REQUIRED);

    FormType mockFormType = mock(FormType.class);
    when(mockFormType.getName()).thenReturn(EXAMPLE_FORM_PROPERTY_TYPE_NAME);
    when(mockFormProperty.getType()).thenReturn(mockFormType);

    TaskFormData mockFormData = mock(TaskFormData.class);
    when(mockFormData.getFormKey()).thenReturn(EXAMPLE_FORM_KEY);
    when(mockFormData.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);

    List<FormProperty> mockFormProperties = new ArrayList<FormProperty>();
    mockFormProperties.add(mockFormProperty);
    when(mockFormData.getFormProperties()).thenReturn(mockFormProperties);
    return mockFormData;
  }

  public static TaskFormData createMockTaskFormDataUsingFormFieldsWithoutFormKey() {
    FormField mockFormField = mock(FormField.class);
    when(mockFormField.getId()).thenReturn(EXAMPLE_FORM_PROPERTY_ID);
    when(mockFormField.getLabel()).thenReturn(EXAMPLE_FORM_PROPERTY_NAME);
    when(mockFormField.getDefaultValue()).thenReturn(EXAMPLE_FORM_PROPERTY_VALUE);

    FormType mockFormType = mock(FormType.class);
    when(mockFormType.getName()).thenReturn(EXAMPLE_FORM_PROPERTY_TYPE_NAME);
    when(mockFormField.getType()).thenReturn(mockFormType);

    TaskFormData mockFormData = mock(TaskFormData.class);
    when(mockFormData.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);

    List<FormField> mockFormFields = new ArrayList<FormField>();
    mockFormFields.add(mockFormField);
    when(mockFormData.getFormFields()).thenReturn(mockFormFields);
    return mockFormData;
  }

  // form data
  public static StartFormData createMockStartFormData(ProcessDefinition definition) {
    FormProperty mockFormProperty = mock(FormProperty.class);
    when(mockFormProperty.getId()).thenReturn(EXAMPLE_FORM_PROPERTY_ID);
    when(mockFormProperty.getName()).thenReturn(EXAMPLE_FORM_PROPERTY_NAME);
    when(mockFormProperty.getValue()).thenReturn(EXAMPLE_FORM_PROPERTY_VALUE);
    when(mockFormProperty.isReadable()).thenReturn(EXAMPLE_FORM_PROPERTY_READABLE);
    when(mockFormProperty.isWritable()).thenReturn(EXAMPLE_FORM_PROPERTY_WRITABLE);
    when(mockFormProperty.isRequired()).thenReturn(EXAMPLE_FORM_PROPERTY_REQUIRED);

    FormType mockFormType = mock(FormType.class);
    when(mockFormType.getName()).thenReturn(EXAMPLE_FORM_PROPERTY_TYPE_NAME);
    when(mockFormProperty.getType()).thenReturn(mockFormType);

    StartFormData mockFormData = mock(StartFormData.class);
    when(mockFormData.getFormKey()).thenReturn(EXAMPLE_FORM_KEY);
    when(mockFormData.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    when(mockFormData.getProcessDefinition()).thenReturn(definition);

    List<FormProperty> mockFormProperties = new ArrayList<FormProperty>();
    mockFormProperties.add(mockFormProperty);
    when(mockFormData.getFormProperties()).thenReturn(mockFormProperties);
    return mockFormData;
  }

  public static StartFormData createMockStartFormDataUsingFormFieldsWithoutFormKey(ProcessDefinition definition) {
    FormField mockFormField = mock(FormField.class);
    when(mockFormField.getId()).thenReturn(EXAMPLE_FORM_PROPERTY_ID);
    when(mockFormField.getLabel()).thenReturn(EXAMPLE_FORM_PROPERTY_NAME);
    when(mockFormField.getDefaultValue()).thenReturn(EXAMPLE_FORM_PROPERTY_VALUE);

    FormType mockFormType = mock(FormType.class);
    when(mockFormType.getName()).thenReturn(EXAMPLE_FORM_PROPERTY_TYPE_NAME);
    when(mockFormField.getType()).thenReturn(mockFormType);

    StartFormData mockFormData = mock(StartFormData.class);
    when(mockFormData.getDeploymentId()).thenReturn(EXAMPLE_DEPLOYMENT_ID);
    when(mockFormData.getProcessDefinition()).thenReturn(definition);

    List<FormField> mockFormFields = new ArrayList<FormField>();
    mockFormFields.add(mockFormField);
    when(mockFormData.getFormFields()).thenReturn(mockFormFields);

    return mockFormData;
  }

  public static ProcessInstance createMockInstance() {
    ProcessInstance mock = mock(ProcessInstance.class);

    when(mock.getId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getBusinessKey()).thenReturn(EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.isSuspended()).thenReturn(EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED);
    when(mock.isEnded()).thenReturn(EXAMPLE_PROCESS_INSTANCE_IS_ENDED);

    return mock;
  }

  public static VariableInstance createMockVariableInstance() {
    VariableInstanceEntity mock = mock(VariableInstanceEntity.class);

    when(mock.getId()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_ID);
    when(mock.getName()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_NAME);
    when(mock.getType()).thenReturn(new StringType());
    when(mock.getTypeName()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_TYPE);
    when(mock.getValue()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_VALUE);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_PROC_INST_ID);
    when(mock.getExecutionId()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_EXECUTION_ID);
    when(mock.getTaskId()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_TASK_ID);
    when(mock.getActivityInstanceId()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_ACTIVITY_INSTANCE_ID);
    when(mock.getErrorMessage()).thenReturn(null);

    return mock;
  }

  public static Execution createMockExecution() {
    Execution mock = mock(Execution.class);

    when(mock.getId()).thenReturn(EXAMPLE_EXECUTION_ID);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.isEnded()).thenReturn(EXAMPLE_EXECUTION_IS_ENDED);

    return mock;
  }

  public static EventSubscription createMockEventSubscription() {
    EventSubscription mock = mock(EventSubscription.class);

    when(mock.getId()).thenReturn(EXAMPLE_EVENT_SUBSCRIPTION_ID);
    when(mock.getEventType()).thenReturn(EXAMPLE_EVENT_SUBSCRIPTION_TYPE);
    when(mock.getEventName()).thenReturn(EXAMPLE_EVENT_SUBSCRIPTION_NAME);
    when(mock.getExecutionId()).thenReturn(EXAMPLE_EXECUTION_ID);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getActivityId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    when(mock.getCreated()).thenReturn(DateTimeUtil.parseDateTime(EXAMPLE_EVENT_SUBSCRIPTION_CREATION_DATE).toDate());

    return mock;
  }

  // statistics
  public static List<ProcessDefinitionStatistics> createMockProcessDefinitionStatistics() {
    ProcessDefinitionStatistics statistics = mock(ProcessDefinitionStatistics.class);
    when(statistics.getFailedJobs()).thenReturn(EXAMPLE_FAILED_JOBS);
    when(statistics.getInstances()).thenReturn(EXAMPLE_INSTANCES);
    when(statistics.getId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(statistics.getName()).thenReturn(EXAMPLE_PROCESS_DEFINITION_NAME);
    when(statistics.getKey()).thenReturn(EXAMPLE_PROCESS_DEFINITION_KEY);

    IncidentStatistics incidentStaticits = mock(IncidentStatistics.class);
    when(incidentStaticits.getIncidentType()).thenReturn(EXAMPLE_INCIDENT_TYPE);
    when(incidentStaticits.getIncidentCount()).thenReturn(EXAMPLE_INCIDENT_COUNT);

    List<IncidentStatistics> exampleIncidentList = new ArrayList<IncidentStatistics>();
    exampleIncidentList.add(incidentStaticits);
    when(statistics.getIncidentStatistics()).thenReturn(exampleIncidentList);

    ProcessDefinitionStatistics anotherStatistics = mock(ProcessDefinitionStatistics.class);
    when(anotherStatistics.getFailedJobs()).thenReturn(ANOTHER_EXAMPLE_FAILED_JOBS);
    when(anotherStatistics.getInstances()).thenReturn(ANOTHER_EXAMPLE_INSTANCES);
    when(anotherStatistics.getId()).thenReturn(ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID);
    when(anotherStatistics.getName()).thenReturn(EXAMPLE_PROCESS_DEFINITION_NAME);
    when(anotherStatistics.getKey()).thenReturn(EXAMPLE_PROCESS_DEFINITION_KEY);

    IncidentStatistics anotherIncidentStaticits = mock(IncidentStatistics.class);
    when(anotherIncidentStaticits.getIncidentType()).thenReturn(ANOTHER_EXAMPLE_INCIDENT_TYPE);
    when(anotherIncidentStaticits.getIncidentCount()).thenReturn(ANOTHER_EXAMPLE_INCIDENT_COUNT);

    List<IncidentStatistics> anotherExampleIncidentList = new ArrayList<IncidentStatistics>();
    anotherExampleIncidentList.add(anotherIncidentStaticits);
    when(anotherStatistics.getIncidentStatistics()).thenReturn(anotherExampleIncidentList);

    List<ProcessDefinitionStatistics> processDefinitionResults = new ArrayList<ProcessDefinitionStatistics>();
    processDefinitionResults.add(statistics);
    processDefinitionResults.add(anotherStatistics);

    return processDefinitionResults;
  }

  public static List<ActivityStatistics> createMockActivityStatistics() {
    ActivityStatistics statistics = mock(ActivityStatistics.class);
    when(statistics.getFailedJobs()).thenReturn(EXAMPLE_FAILED_JOBS);
    when(statistics.getInstances()).thenReturn(EXAMPLE_INSTANCES);
    when(statistics.getId()).thenReturn(EXAMPLE_ACTIVITY_ID);

    IncidentStatistics incidentStaticits = mock(IncidentStatistics.class);
    when(incidentStaticits.getIncidentType()).thenReturn(EXAMPLE_INCIDENT_TYPE);
    when(incidentStaticits.getIncidentCount()).thenReturn(EXAMPLE_INCIDENT_COUNT);

    List<IncidentStatistics> exampleIncidentList = new ArrayList<IncidentStatistics>();
    exampleIncidentList.add(incidentStaticits);
    when(statistics.getIncidentStatistics()).thenReturn(exampleIncidentList);

    ActivityStatistics anotherStatistics = mock(ActivityStatistics.class);
    when(anotherStatistics.getFailedJobs()).thenReturn(ANOTHER_EXAMPLE_FAILED_JOBS);
    when(anotherStatistics.getInstances()).thenReturn(ANOTHER_EXAMPLE_INSTANCES);
    when(anotherStatistics.getId()).thenReturn(ANOTHER_EXAMPLE_ACTIVITY_ID);

    IncidentStatistics anotherIncidentStaticits = mock(IncidentStatistics.class);
    when(anotherIncidentStaticits.getIncidentType()).thenReturn(ANOTHER_EXAMPLE_INCIDENT_TYPE);
    when(anotherIncidentStaticits.getIncidentCount()).thenReturn(ANOTHER_EXAMPLE_INCIDENT_COUNT);

    List<IncidentStatistics> anotherExampleIncidentList = new ArrayList<IncidentStatistics>();
    anotherExampleIncidentList.add(anotherIncidentStaticits);
    when(anotherStatistics.getIncidentStatistics()).thenReturn(anotherExampleIncidentList);

    List<ActivityStatistics> activityResults = new ArrayList<ActivityStatistics>();
    activityResults.add(statistics);
    activityResults.add(anotherStatistics);

    return activityResults;
  }

  // process definition
  public static List<ProcessDefinition> createMockDefinitions() {
    List<ProcessDefinition> mocks = new ArrayList<ProcessDefinition>();
    mocks.add(createMockDefinition());
    return mocks;
  }

  public static ProcessDefinition createMockDefinition() {
    MockDefinitionBuilder builder = new MockDefinitionBuilder();
    ProcessDefinition mockDefinition = builder.id(EXAMPLE_PROCESS_DEFINITION_ID).category(EXAMPLE_PROCESS_DEFINITION_CATEGORY)
        .name(EXAMPLE_PROCESS_DEFINITION_NAME).key(EXAMPLE_PROCESS_DEFINITION_KEY).description(EXAMPLE_PROCESS_DEFINITION_DESCRIPTION)
        .version(EXAMPLE_PROCESS_DEFINITION_VERSION).resource(EXAMPLE_PROCESS_DEFINITION_RESOURCE_NAME).deploymentId(EXAMPLE_DEPLOYMENT_ID)
        .diagram(EXAMPLE_PROCESS_DEFINITION_DIAGRAM_RESOURCE_NAME).suspended(EXAMPLE_PROCESS_DEFINITION_IS_SUSPENDED).build();

    return mockDefinition;
  }

  // user & groups

  public static Group createMockGroup() {
    Group mockGroup = mock(Group.class);
    when(mockGroup.getId()).thenReturn(EXAMPLE_GROUP_ID);
    when(mockGroup.getName()).thenReturn(EXAMPLE_GROUP_NAME);
    when(mockGroup.getType()).thenReturn(EXAMPLE_GROUP_TYPE);

    return mockGroup;
  }

  public static Group createMockGroupUpdate() {
    Group mockGroup = mock(Group.class);
    when(mockGroup.getId()).thenReturn(EXAMPLE_GROUP_ID);
    when(mockGroup.getName()).thenReturn(EXAMPLE_GROUP_NAME_UPDATE);

    return mockGroup;
  }

  public static List<Group> createMockGroups() {
    List<Group> mockGroups = new ArrayList<Group>();
    mockGroups.add(createMockGroup());
    return mockGroups;
  }

  public static User createMockUser() {
    User mockUser = mock(User.class);
    when(mockUser.getId()).thenReturn(EXAMPLE_USER_ID);
    when(mockUser.getFirstName()).thenReturn(EXAMPLE_USER_FIRST_NAME);
    when(mockUser.getLastName()).thenReturn(EXAMPLE_USER_LAST_NAME);
    when(mockUser.getEmail()).thenReturn(EXAMPLE_USER_EMAIL);
    when(mockUser.getPassword()).thenReturn(EXAMPLE_USER_PASSWORD);
    return mockUser;
  }

  public static Authentication createMockAuthentication() {
    Authentication mockAuthentication = mock(Authentication.class);

    when(mockAuthentication.getUserId()).thenReturn(EXAMPLE_USER_ID);

    return mockAuthentication;
  }

  // jobs
  public static Job createMockJob() {
    Job mock = new MockJobBuilder()
      .id(EXAMPLE_JOB_ID)
      .processInstanceId(EXAMPLE_PROCESS_INSTANCE_ID)
      .executionId(EXAMPLE_EXECUTION_ID)
      .processDefinitionId(EXAMPLE_PROCESS_DEFINITION_ID)
      .processDefinitionKey(EXAMPLE_PROCESS_DEFINITION_KEY)
      .retries(EXAMPLE_JOB_RETRIES)
      .exceptionMessage(EXAMPLE_JOB_NO_EXCEPTION_MESSAGE)
      .dueDate(DateTimeUtil.parseDateTime(EXAMPLE_DUE_DATE).toDate())
      .suspended(EXAMPLE_JOB_IS_SUSPENDED)
      .build();
    return mock;
  }

  public static List<Job> createMockJobs() {
    List<Job> mockList = new ArrayList<Job>();
    mockList.add(createMockJob());
    return mockList;
  }

  public static List<Job> createMockEmptyJobList() {
    List<Job> mockList = new ArrayList<Job>();
    return mockList;
  }

  public static User createMockUserUpdate() {
    User mockUser = mock(User.class);
    when(mockUser.getId()).thenReturn(EXAMPLE_USER_ID);
    when(mockUser.getFirstName()).thenReturn(EXAMPLE_USER_FIRST_NAME_UPDATE);
    when(mockUser.getLastName()).thenReturn(EXAMPLE_USER_LAST_NAME_UPDATE);
    when(mockUser.getEmail()).thenReturn(EXAMPLE_USER_EMAIL_UPDATE);
    when(mockUser.getPassword()).thenReturn(EXAMPLE_USER_PASSWORD);
    return mockUser;
  }

  public static List<User> createMockUsers() {
    ArrayList<User> list = new ArrayList<User>();
    list.add(createMockUser());
    return list;
  }

  public static Authorization createMockGlobalAuthorization() {
    Authorization mockAuthorization = mock(Authorization.class);

    when(mockAuthorization.getId()).thenReturn(EXAMPLE_AUTHORIZATION_ID);
    when(mockAuthorization.getAuthorizationType()).thenReturn(Authorization.AUTH_TYPE_GLOBAL);
    when(mockAuthorization.getUserId()).thenReturn(Authorization.ANY);

    when(mockAuthorization.getResourceType()).thenReturn(EXAMPLE_RESOURCE_TYPE_ID);
    when(mockAuthorization.getResourceId()).thenReturn(EXAMPLE_RESOURCE_ID);
    when(mockAuthorization.getPermissions(Permissions.values())).thenReturn(EXAMPLE_PERMISSION_VALUES);

    return mockAuthorization;
  }

  public static Authorization createMockGrantAuthorization() {
    Authorization mockAuthorization = mock(Authorization.class);

    when(mockAuthorization.getId()).thenReturn(EXAMPLE_AUTHORIZATION_ID);
    when(mockAuthorization.getAuthorizationType()).thenReturn(Authorization.AUTH_TYPE_GRANT);
    when(mockAuthorization.getUserId()).thenReturn(EXAMPLE_USER_ID);

    when(mockAuthorization.getResourceType()).thenReturn(EXAMPLE_RESOURCE_TYPE_ID);
    when(mockAuthorization.getResourceId()).thenReturn(EXAMPLE_RESOURCE_ID);
    when(mockAuthorization.getPermissions(Permissions.values())).thenReturn(EXAMPLE_PERMISSION_VALUES);

    return mockAuthorization;
  }

  public static Authorization createMockRevokeAuthorization() {
    Authorization mockAuthorization = mock(Authorization.class);

    when(mockAuthorization.getId()).thenReturn(EXAMPLE_AUTHORIZATION_ID);
    when(mockAuthorization.getAuthorizationType()).thenReturn(Authorization.AUTH_TYPE_REVOKE);
    when(mockAuthorization.getUserId()).thenReturn(EXAMPLE_USER_ID);

    when(mockAuthorization.getResourceType()).thenReturn(EXAMPLE_RESOURCE_TYPE_ID);
    when(mockAuthorization.getResourceId()).thenReturn(EXAMPLE_RESOURCE_ID);
    when(mockAuthorization.getPermissions(Permissions.values())).thenReturn(EXAMPLE_PERMISSION_VALUES);

    return mockAuthorization;
  }

  public static List<Authorization> createMockAuthorizations() {
    return Arrays.asList(new Authorization[] { createMockGlobalAuthorization(), createMockGrantAuthorization(), createMockRevokeAuthorization() });
  }

  public static List<Authorization> createMockGrantAuthorizations() {
    return Arrays.asList(new Authorization[] { createMockGrantAuthorization() });
  }

  public static List<Authorization> createMockRevokeAuthorizations() {
    return Arrays.asList(new Authorization[] { createMockRevokeAuthorization() });
  }

  public static List<Authorization> createMockGlobalAuthorizations() {
    return Arrays.asList(new Authorization[] { createMockGlobalAuthorization() });
  }

  public static Date createMockDuedate() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.add(Calendar.DATE, 3);
    return cal.getTime();
  } // process application

  public static ProcessApplicationInfo createMockProcessApplicationInfo() {
    ProcessApplicationInfo appInfo = mock(ProcessApplicationInfo.class);
    Map<String, String> mockAppProperties = new HashMap<String, String>();
    String mockServletContextPath = MockProvider.EXAMPLE_PROCESS_APPLICATION_CONTEXT_PATH;
    mockAppProperties.put(ProcessApplicationInfo.PROP_SERVLET_CONTEXT_PATH, mockServletContextPath);
    when(appInfo.getProperties()).thenReturn(mockAppProperties);
    return appInfo;
  }

  // History
  public static List<HistoricActivityInstance> createMockHistoricActivityInstances() {
    List<HistoricActivityInstance> mockList = new ArrayList<HistoricActivityInstance>();
    mockList.add(createMockHistoricActivityInstance());
    return mockList;
  }

  public static HistoricActivityInstance createMockHistoricActivityInstance() {
    HistoricActivityInstance mock = mock(HistoricActivityInstance.class);

    when(mock.getId()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_ID);
    when(mock.getParentActivityInstanceId()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_PARENT_ACTIVITY_INSTANCE_ID);
    when(mock.getActivityId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    when(mock.getActivityName()).thenReturn(EXAMPLE_ACTIVITY_NAME);
    when(mock.getActivityType()).thenReturn(EXAMPLE_ACTIVITY_TYPE);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getExecutionId()).thenReturn(EXAMPLE_EXECUTION_ID);
    when(mock.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(mock.getCalledProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_CALLED_PROCESS_INSTANCE_ID);
    when(mock.getAssignee()).thenReturn(EXAMPLE_TASK_ASSIGNEE_NAME);
    when(mock.getStartTime()).thenReturn(DateTimeUtil.parseDateTime(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_START_TIME).toDate());
    when(mock.getEndTime()).thenReturn(DateTimeUtil.parseDateTime(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_END_TIME).toDate());
    when(mock.getDurationInMillis()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_DURATION);
    when(mock.isCanceled()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_IS_CANCELED);
    when(mock.isCompleteScope()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_IS_COMPLETE_SCOPE);

    return mock;
  }

  public static List<HistoricActivityInstance> createMockRunningHistoricActivityInstances() {
    List<HistoricActivityInstance> mockList = new ArrayList<HistoricActivityInstance>();
    mockList.add(createMockRunningHistoricActivityInstance());
    return mockList;
  }

  public static HistoricActivityInstance createMockRunningHistoricActivityInstance() {
    HistoricActivityInstance mock = mock(HistoricActivityInstance.class);

    when(mock.getId()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_ID);
    when(mock.getParentActivityInstanceId()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_PARENT_ACTIVITY_INSTANCE_ID);
    when(mock.getActivityId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    when(mock.getActivityName()).thenReturn(EXAMPLE_ACTIVITY_NAME);
    when(mock.getActivityType()).thenReturn(EXAMPLE_ACTIVITY_TYPE);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getExecutionId()).thenReturn(EXAMPLE_EXECUTION_ID);
    when(mock.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(mock.getCalledProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_CALLED_PROCESS_INSTANCE_ID);
    when(mock.getAssignee()).thenReturn(EXAMPLE_TASK_ASSIGNEE_NAME);
    when(mock.getStartTime()).thenReturn(DateTimeUtil.parseDateTime(EXAMPLE_HISTORIC_ACTIVITY_INSTANCE_START_TIME).toDate());
    when(mock.getEndTime()).thenReturn(null);
    when(mock.getDurationInMillis()).thenReturn(null);

    return mock;
  }

  public static List<HistoricActivityStatistics> createMockHistoricActivityStatistics() {
    HistoricActivityStatistics statistics = mock(HistoricActivityStatistics.class);

    when(statistics.getId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    when(statistics.getInstances()).thenReturn(EXAMPLE_INSTANCES_LONG);
    when(statistics.getCanceled()).thenReturn(EXAMPLE_CANCELED_LONG);
    when(statistics.getFinished()).thenReturn(EXAMPLE_FINISHED_LONG);
    when(statistics.getCompleteScope()).thenReturn(EXAMPLE_COMPLETE_SCOPE_LONG);

    HistoricActivityStatistics anotherStatistics = mock(HistoricActivityStatistics.class);

    when(anotherStatistics.getId()).thenReturn(ANOTHER_EXAMPLE_ACTIVITY_ID);
    when(anotherStatistics.getInstances()).thenReturn(ANOTHER_EXAMPLE_INSTANCES_LONG);
    when(anotherStatistics.getCanceled()).thenReturn(ANOTHER_EXAMPLE_CANCELED_LONG);
    when(anotherStatistics.getFinished()).thenReturn(ANOTHER_EXAMPLE_FINISHED_LONG);
    when(anotherStatistics.getCompleteScope()).thenReturn(ANOTHER_EXAMPLE_COMPLETE_SCOPE_LONG);

    List<HistoricActivityStatistics> activityResults = new ArrayList<HistoricActivityStatistics>();
    activityResults.add(statistics);
    activityResults.add(anotherStatistics);

    return activityResults;
  }

  public static List<HistoricProcessInstance> createMockHistoricProcessInstances() {
    List<HistoricProcessInstance> mockList = new ArrayList<HistoricProcessInstance>();
    mockList.add(createMockHistoricProcessInstance());
    return mockList;
  }

  public static HistoricProcessInstance createMockHistoricProcessInstance() {
    HistoricProcessInstance mock = mock(HistoricProcessInstance.class);

    when(mock.getId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getBusinessKey()).thenReturn(EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(mock.getDeleteReason()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_DELETE_REASON);
    when(mock.getEndTime()).thenReturn(DateTimeUtil.parseDateTime(EXAMPLE_HISTORIC_PROCESS_INSTANCE_END_TIME).toDate());
    when(mock.getStartTime()).thenReturn(DateTimeUtil.parseDateTime(EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_TIME).toDate());
    when(mock.getDurationInMillis()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_DURATION_MILLIS);
    when(mock.getStartUserId()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_USER_ID);
    when(mock.getStartActivityId()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_ACTIVITY_ID);
    when(mock.getSuperProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_SUPER_PROCESS_INSTANCE_ID);

    return mock;
  }

  public static List<HistoricProcessInstance> createMockRunningHistoricProcessInstances() {
    List<HistoricProcessInstance> mockList = new ArrayList<HistoricProcessInstance>();
    mockList.add(createMockHistoricProcessInstanceUnfinished());
    return mockList;
  }

  public static HistoricProcessInstance createMockHistoricProcessInstanceUnfinished() {
    HistoricProcessInstance mock = mock(HistoricProcessInstance.class);
    when(mock.getId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getBusinessKey()).thenReturn(EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(mock.getDeleteReason()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_DELETE_REASON);
    when(mock.getEndTime()).thenReturn(null);
    when(mock.getStartTime()).thenReturn(DateTimeUtil.parseDateTime(EXAMPLE_HISTORIC_PROCESS_INSTANCE_START_TIME).toDate());
    when(mock.getDurationInMillis()).thenReturn(EXAMPLE_HISTORIC_PROCESS_INSTANCE_DURATION_MILLIS);
    return mock;
  }

  public static List<HistoricVariableInstance> createMockHistoricVariableInstances() {
    List<HistoricVariableInstance> mockList = new ArrayList<HistoricVariableInstance>();
    mockList.add(createMockHistoricVariableInstance());
    return mockList;
  }

  public static HistoricVariableInstance createMockHistoricVariableInstance() {
    HistoricVariableInstanceEntity mock = mock(HistoricVariableInstanceEntity.class);

    when(mock.getId()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_ID);
    when(mock.getVariableName()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_NAME);
    when(mock.getVariableTypeName()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_TYPE);
    when(mock.getVariableType()).thenReturn(new StringType());
    when(mock.getValue()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_VALUE);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_PROC_INST_ID);
    when(mock.getActivtyInstanceId()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_ACTIVITY_INSTANCE_ID);
    when(mock.getErrorMessage()).thenReturn(null);

    return mock;
  }

  public static List<ProcessInstance> createAnotherMockProcessInstanceList() {
    List<ProcessInstance> mockProcessInstanceList = new ArrayList<ProcessInstance>();
    mockProcessInstanceList.add(createMockInstance());
    mockProcessInstanceList.add(createAnotherMockInstance());
    return mockProcessInstanceList;
  }

  public static ProcessInstance createAnotherMockInstance() {
    ProcessInstance mock = mock(ProcessInstance.class);

    when(mock.getId()).thenReturn(ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.getBusinessKey()).thenReturn(EXAMPLE_PROCESS_INSTANCE_BUSINESS_KEY);
    when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(mock.getProcessInstanceId()).thenReturn(ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.isSuspended()).thenReturn(EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED);
    when(mock.isEnded()).thenReturn(EXAMPLE_PROCESS_INSTANCE_IS_ENDED);

    return mock;
  }

  public static Set<String> createMockSetFromList(String list){
    return new HashSet<String>(Arrays.asList(list.split(",")));
  }

  public static IdentityLink createMockUserAssigneeIdentityLink() {
    IdentityLink identityLink = mock(IdentityLink.class);
    when(identityLink.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(identityLink.getType()).thenReturn(IdentityLinkType.ASSIGNEE);
    when(identityLink.getUserId()).thenReturn(EXAMPLE_USER_ID);

    return identityLink;
  }

  public static IdentityLink createMockCandidateGroupIdentityLink() {
    IdentityLink identityLink = mock(IdentityLink.class);
    when(identityLink.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(identityLink.getType()).thenReturn(IdentityLinkType.CANDIDATE);
    when(identityLink.getGroupId()).thenReturn(EXAMPLE_GROUP_ID);

    return identityLink;
  }

  public static IdentityLink createAnotherMockCandidateGroupIdentityLink() {
    IdentityLink identityLink = mock(IdentityLink.class);
    when(identityLink.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(identityLink.getType()).thenReturn(IdentityLinkType.CANDIDATE);
    when(identityLink.getGroupId()).thenReturn(EXAMPLE_GROUP_ID2);

    return identityLink;
  }

  // job definition
  public static List<JobDefinition> createMockJobDefinitions() {
    List<JobDefinition> mocks = new ArrayList<JobDefinition>();
    mocks.add(createMockJobDefinition());
    return mocks;
  }

  public static JobDefinition createMockJobDefinition() {
    JobDefinition jobDefinition = mock(JobDefinition.class);

    when(jobDefinition.getId()).thenReturn(EXAMPLE_JOB_DEFINITION_ID);
    when(jobDefinition.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(jobDefinition.getProcessDefinitionKey()).thenReturn(EXAMPLE_PROCESS_DEFINITION_KEY);
    when(jobDefinition.getJobType()).thenReturn(EXAMPLE_JOB_TYPE);
    when(jobDefinition.getJobConfiguration()).thenReturn(EXAMPLE_JOB_CONFIG);
    when(jobDefinition.getActivityId()).thenReturn(EXAMPLE_ACTIVITY_ID);
    when(jobDefinition.isSuspended()).thenReturn(EXAMPLE_JOB_DEFINITION_IS_SUSPENDED);

    return jobDefinition;
  }

  public static List<UserOperationLogEntry> createUserOperationLogEntries() {
    List<UserOperationLogEntry> entries = new ArrayList<UserOperationLogEntry>();
    entries.add(createUserOperationLogEntry());
    return entries;
  }

  private static UserOperationLogEntry createUserOperationLogEntry() {
    UserOperationLogEntry entry = mock(UserOperationLogEntry.class);
    when(entry.getId()).thenReturn(EXAMPLE_USER_OPERATION_LOG_ID);
    when(entry.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
    when(entry.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(entry.getExecutionId()).thenReturn(EXAMPLE_EXECUTION_ID);
    when(entry.getTaskId()).thenReturn(EXAMPLE_TASK_ID);
    when(entry.getUserId()).thenReturn(EXAMPLE_USER_ID);
    when(entry.getTimestamp()).thenReturn(DateTimeUtil.parseDateTime(EXAMPLE_USER_OPERATION_TIMESTAMP).toDate());
    when(entry.getOperationId()).thenReturn(EXAMPLE_USER_OPERATION_ID);
    when(entry.getOperationType()).thenReturn(EXAMPLE_USER_OPERATION_TYPE);
    when(entry.getEntityType()).thenReturn(EXAMPLE_USER_OPERATION_ENTITY);
    when(entry.getProperty()).thenReturn(EXAMPLE_USER_OPERATION_PROPERTY);
    when(entry.getOrgValue()).thenReturn(EXAMPLE_USER_OPERATION_ORG_VALUE);
    when(entry.getNewValue()).thenReturn(EXAMPLE_USER_OPERATION_NEW_VALUE);
    return entry;
  }

  // historic detail ////////////////////

  public static HistoricVariableUpdate createMockHistoricVariableUpdate() {
    HistoricDetailVariableInstanceUpdateEntity variableUpdate = mock(HistoricDetailVariableInstanceUpdateEntity.class);

    when(variableUpdate.getId()).thenReturn(EXAMPLE_HISTORIC_VAR_UPDATE_ID);
    when(variableUpdate.getProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_VAR_UPDATE_PROC_INST_ID);
    when(variableUpdate.getActivityInstanceId()).thenReturn(EXAMPLE_HISTORIC_VAR_UPDATE_ACT_INST_ID);
    when(variableUpdate.getExecutionId()).thenReturn(EXAMPLE_HISTORIC_VAR_UPDATE_EXEC_ID);
    when(variableUpdate.getTaskId()).thenReturn(EXAMPLE_HISTORIC_VAR_UPDATE_TASK_ID);
    when(variableUpdate.getTime()).thenReturn(DateTimeUtil.parseDateTime(EXAMPLE_HISTORIC_VAR_UPDATE_TIME).toDate());
    when(variableUpdate.getVariableName()).thenReturn(EXAMPLE_HISTORIC_VAR_UPDATE_NAME);
    when(variableUpdate.getVariableTypeName()).thenReturn(EXAMPLE_HISTORIC_VAR_UPDATE_TYPE_NAME);
    when(variableUpdate.getVariableType()).thenReturn(new StringType());
    when(variableUpdate.getValue()).thenReturn(EXAMPLE_HISTORIC_VAR_UPDATE_VALUE);
    when(variableUpdate.getRevision()).thenReturn(EXAMPLE_HISTORIC_VAR_UPDATE_REVISION);

    return variableUpdate;
  }

  public static List<HistoricVariableUpdate> createMockHistoricVariableUpdates() {
    List<HistoricVariableUpdate> entries = new ArrayList<HistoricVariableUpdate>();
    entries.add(createMockHistoricVariableUpdate());
    return entries;
  }

  public static HistoricFormField createMockHistoricFormField() {
    HistoricFormField historicFromField = mock(HistoricFormField.class);

    when(historicFromField.getId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_ID);
    when(historicFromField.getProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_PROC_INST_ID);
    when(historicFromField.getActivityInstanceId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_ACT_INST_ID);
    when(historicFromField.getExecutionId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_EXEC_ID);
    when(historicFromField.getTaskId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_TASK_ID);
    when(historicFromField.getTime()).thenReturn(DateTimeUtil.parseDate(EXAMPLE_HISTORIC_FORM_FIELD_TIME));
    when(historicFromField.getFieldId()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_FIELD_ID);
    when(historicFromField.getFieldValue()).thenReturn(EXAMPLE_HISTORIC_FORM_FIELD_VALUE);

    return historicFromField;
  }

  public static List<HistoricFormField> createMockHistoricFormFields() {
    List<HistoricFormField> entries = new ArrayList<HistoricFormField>();
    entries.add(createMockHistoricFormField());
    return entries;
  }

  public static List<HistoricDetail> createMockHistoricDetails() {
    List<HistoricDetail> entries = new ArrayList<HistoricDetail>();
    entries.add(createMockHistoricVariableUpdate());
    entries.add(createMockHistoricFormField());
    return entries;
  }

  public static HistoricTaskInstance createMockHistoricTaskInstance() {
    HistoricTaskInstance taskInstance = mock(HistoricTaskInstance.class);

    when(taskInstance.getId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_ID);
    when(taskInstance.getProcessInstanceId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_PROC_INST_ID);
    when(taskInstance.getActivityInstanceId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_ACT_INST_ID);
    when(taskInstance.getExecutionId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_EXEC_ID);
    when(taskInstance.getProcessDefinitionId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_PROC_DEF_ID);
    when(taskInstance.getName()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_NAME);
    when(taskInstance.getDescription()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_DESCRIPTION);
    when(taskInstance.getDeleteReason()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_DELETE_REASON);
    when(taskInstance.getOwner()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_OWNER);
    when(taskInstance.getAssignee()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_ASSIGNEE);
    when(taskInstance.getStartTime()).thenReturn(DateTimeUtil.parseDateTime(EXAMPLE_HISTORIC_TASK_INST_START_TIME).toDate());
    when(taskInstance.getEndTime()).thenReturn(DateTimeUtil.parseDateTime(EXAMPLE_HISTORIC_TASK_INST_END_TIME).toDate());
    when(taskInstance.getDurationInMillis()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_DURATION);
    when(taskInstance.getTaskDefinitionKey()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_DEF_KEY);
    when(taskInstance.getPriority()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_PRIORITY);
    when(taskInstance.getDueDate()).thenReturn(DateTimeUtil.parseDateTime(EXAMPLE_HISTORIC_TASK_INST_DUE_DATE).toDate());
    when(taskInstance.getFollowUpDate()).thenReturn(DateTimeUtil.parseDateTime(EXAMPLE_HISTORIC_TASK_INST_FOLLOW_UP_DATE).toDate());
    when(taskInstance.getParentTaskId()).thenReturn(EXAMPLE_HISTORIC_TASK_INST_PARENT_TASK_ID);

    return taskInstance;
  }

  public static List<HistoricTaskInstance> createMockHistoricTaskInstances() {
    List<HistoricTaskInstance> entries = new ArrayList<HistoricTaskInstance>();
    entries.add(createMockHistoricTaskInstance());
    return entries;
  }

  // Incident ///////////////////////////////////////

  public static Incident createMockIncident() {
    Incident incident = mock(Incident.class);

    when(incident.getId()).thenReturn(EXAMPLE_INCIDENT_ID);
    when(incident.getIncidentTimestamp()).thenReturn(DateTimeUtil.parseDateTime(EXAMPLE_INCIDENT_TIMESTAMP).toDate());
    when(incident.getIncidentType()).thenReturn(EXAMPLE_INCIDENT_TYPE);
    when(incident.getExecutionId()).thenReturn(EXAMPLE_INCIDENT_EXECUTION_ID);
    when(incident.getActivityId()).thenReturn(EXAMPLE_INCIDENT_ACTIVITY_ID);
    when(incident.getProcessInstanceId()).thenReturn(EXAMPLE_INCIDENT_PROC_INST_ID);
    when(incident.getProcessDefinitionId()).thenReturn(EXAMPLE_INCIDENT_PROC_DEF_ID);
    when(incident.getCauseIncidentId()).thenReturn(EXAMPLE_INCIDENT_CAUSE_INCIDENT_ID);
    when(incident.getRootCauseIncidentId()).thenReturn(EXAMPLE_INCIDENT_ROOT_CAUSE_INCIDENT_ID);
    when(incident.getConfiguration()).thenReturn(EXAMPLE_INCIDENT_CONFIGURATION);
    when(incident.getIncidentMessage()).thenReturn(EXAMPLE_INCIDENT_MESSAGE);

    return incident;
  }

  public static List<Incident> createMockIncidents() {
    List<Incident> entries = new ArrayList<Incident>();
    entries.add(createMockIncident());
    return entries;
  }

  // Historic Incident ///////////////////////////////////////

  public static HistoricIncident createMockHistoricIncident() {
    HistoricIncident incident = mock(HistoricIncident.class);

    when(incident.getId()).thenReturn(EXAMPLE_HIST_INCIDENT_ID);
    when(incident.getCreateTime()).thenReturn(DateTimeUtil.parseDateTime(EXAMPLE_HIST_INCIDENT_CREATE_TIME).toDate());
    when(incident.getEndTime()).thenReturn(DateTimeUtil.parseDateTime(EXAMPLE_HIST_INCIDENT_END_TIME).toDate());
    when(incident.getIncidentType()).thenReturn(EXAMPLE_HIST_INCIDENT_TYPE);
    when(incident.getExecutionId()).thenReturn(EXAMPLE_HIST_INCIDENT_EXECUTION_ID);
    when(incident.getActivityId()).thenReturn(EXAMPLE_HIST_INCIDENT_ACTIVITY_ID);
    when(incident.getProcessInstanceId()).thenReturn(EXAMPLE_HIST_INCIDENT_PROC_INST_ID);
    when(incident.getProcessDefinitionId()).thenReturn(EXAMPLE_HIST_INCIDENT_PROC_DEF_ID);
    when(incident.getCauseIncidentId()).thenReturn(EXAMPLE_HIST_INCIDENT_CAUSE_INCIDENT_ID);
    when(incident.getRootCauseIncidentId()).thenReturn(EXAMPLE_HIST_INCIDENT_ROOT_CAUSE_INCIDENT_ID);
    when(incident.getConfiguration()).thenReturn(EXAMPLE_HIST_INCIDENT_CONFIGURATION);
    when(incident.getIncidentMessage()).thenReturn(EXAMPLE_HIST_INCIDENT_MESSAGE);
    when(incident.isOpen()).thenReturn(EXAMPLE_HIST_INCIDENT_STATE_OPEN);
    when(incident.isDeleted()).thenReturn(EXAMPLE_HIST_INCIDENT_STATE_DELETED);
    when(incident.isResolved()).thenReturn(EXAMPLE_HIST_INCIDENT_STATE_RESOLVED);

    return incident;
  }

  public static List<HistoricIncident> createMockHistoricIncidents() {
    List<HistoricIncident> entries = new ArrayList<HistoricIncident>();
    entries.add(createMockHistoricIncident());
    return entries;
  }
}
