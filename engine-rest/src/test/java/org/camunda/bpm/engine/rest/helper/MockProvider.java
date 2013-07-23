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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.engine.form.FormProperty;
import org.camunda.bpm.engine.form.FormType;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.persistence.entity.VariableInstanceEntity;
import org.camunda.bpm.engine.impl.variable.StringType;
import org.camunda.bpm.engine.management.ActivityStatistics;
import org.camunda.bpm.engine.management.IncidentStatistics;
import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
import org.joda.time.DateTime;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;

/**
 * Provides mocks for the basic engine entities, such as {@link ProcessDefinition}, {@link User}, etc., 
 * that are reused across the various kinds of tests.
 * 
 * @author Thorben Lindhauer
 *
 */
public abstract class MockProvider {

  // engine
  public static final String EXAMPLE_PROCESS_ENGINE_NAME = "default";
  public static final String ANOTHER_EXAMPLE_PROCESS_ENGINE_NAME = "anotherEngineName";
  public static final String NON_EXISTING_PROCESS_ENGINE_NAME = "aNonExistingEngineName";
  
  //task properties
  public static final String EXAMPLE_TASK_ID = "anId";
  public static final String EXAMPLE_TASK_NAME = "aName";
  public static final String EXAMPLE_TASK_ASSIGNEE_NAME = "anAssignee";
  public static final String EXAMPLE_TASK_CREATE_TIME = "2013-01-23T13:42:42";
  public static final String EXAMPLE_TASK_DUE_DATE = "2013-01-23T13:42:43";
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
  public static final String EXAMPLE_PROCESS_INSTANCE_ID = "aProcInstId";
  public static final String ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID = "anotherId";
  public static final boolean EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED = false;
  public static final boolean EXAMPLE_PROCESS_INSTANCE_IS_ENDED = false;
  public static final String EXAMPLE_PROCESS_INSTANCE_ID_LIST = EXAMPLE_PROCESS_INSTANCE_ID + "," + ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID;
  public static final String EXAMPLE_PROCESS_INSTANCE_ID_LIST_WITH_DUP = EXAMPLE_PROCESS_INSTANCE_ID + "," + ANOTHER_EXAMPLE_PROCESS_INSTANCE_ID + "," + EXAMPLE_PROCESS_INSTANCE_ID;
  public static final String EXAMPLE_NON_EXISTENT_PROCESS_INSTANCE_ID = "aNonExistentProcInstId";
  public static final String EXAMPLE_PROCESS_INSTANCE_ID_LIST_WITH_NONEXISTENT_ID = EXAMPLE_PROCESS_INSTANCE_ID + "," + EXAMPLE_NON_EXISTENT_PROCESS_INSTANCE_ID;
  
  // variable instance
  public static final String EXAMPLE_VARIABLE_INSTANCE_NAME = "aVariableInstanceName";
  public static final String EXAMPLE_VARIABLE_INSTANCE_TYPE = "String";
  public static final String EXAMPLE_VARIABLE_INSTANCE_VALUE = "aVariableInstanceValue";
  public static final String EXAMPLE_VARIABLE_INSTANCE_PROC_INST_ID = "aVariableInstanceProcInstId";
  public static final String EXAMPLE_VARIABLE_INSTANCE_EXECUTION_ID = "aVariableInstanceExecutionId";
  public static final String EXAMPLE_VARIABLE_INSTANCE_TASK_ID = "aVariableInstanceTaskId";
  public static final String EXAMPLE_VARIABLE_INSTANCE_ACTIVITY_INSTANCE_ID = "aVariableInstanceVariableInstanceId";
  
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
  public static final String EXAMPLE_PROCESS_DEFINITION_NAME = "aName";
  public static final String EXAMPLE_PROCESS_DEFINITION_KEY = "aKey";
  public static final String EXAMPLE_PROCESS_DEFINITION_CATEGORY = "aCategory";
  public static final String EXAMPLE_PROCESS_DEFINITION_DESCRIPTION = "aDescription";
  public static final int EXAMPLE_PROCESS_DEFINITION_VERSION = 42;
  public static final String EXAMPLE_PROCESS_DEFINITION_RESOURCE_NAME = "aResourceName";
  public static final String EXAMPLE_PROCESS_DEFINITION_DIAGRAM_RESOURCE_NAME = "aResourceName";
  public static final boolean EXAMPLE_PROCESS_DEFINITION_IS_SUSPENDED = true;
 
  public static final String ANOTHER_EXAMPLE_PROCESS_DEFINITION_ID = "aProcessDefinitionId:2";
 
  public static final String EXAMPLE_ACTIVITY_ID = "anActivity";
  public static final String ANOTHER_EXAMPLE_ACTIVITY_ID = "anotherActivity";
  public static final String EXAMPLE_ACTIVITY_NAME = "anActivityName";
  public static final String EXAMPLE_ACTIVITY_TYPE = "anActivityType";
 
  // statistics
  public static final int EXAMPLE_FAILED_JOBS = 42;
  public static final int EXAMPLE_INSTANCES = 123;
  
  public static final String EXAMPLE_INCIDENT_TYPE = "anIncidentType";
  public static final int EXAMPLE_INCIDENT_COUNT = 1;
  
  public static final int ANOTHER_EXAMPLE_FAILED_JOBS = 43;
  public static final int ANOTHER_EXAMPLE_INSTANCES = 124;
  
  public static final String ANOTHER_EXAMPLE_INCIDENT_TYPE = "anotherIncidentType";
  public static final int ANOTHER_EXAMPLE_INCIDENT_COUNT = 2;

  // user & groups
  public static final String EXAMPLE_GROUP_ID = "group1Id";
  public static final String EXAMPLE_GROUP_NAME = "group1";
  public static final String EXAMPLE_GROUP_TYPE = "organizational-unit";
  public static final String EXAMPLE_GROUP_NAME_UPDATE = "group1Update";
 
  public static final String EXAMPLE_USER_ID = "userId";
  public static final String EXAMPLE_USER_FIRST_NAME = "firstName";
  public static final String EXAMPLE_USER_LAST_NAME = "lastName";
  public static final String EXAMPLE_USER_EMAIL = "test@example.org";
  public static final String EXAMPLE_USER_PASSWORD = "s3cret";
  
  public static final String EXAMPLE_USER_FIRST_NAME_UPDATE = "firstNameUpdate";
  public static final String EXAMPLE_USER_LAST_NAME_UPDATE = "lastNameUpdate";
  public static final String EXAMPLE_USER_EMAIL_UPDATE = "testUpdate@example.org";
  // Jobs
  public static final String EXAMPLE_JOB_ID = "aJobId";
  public static final String NON_EXISTING_JOB_ID = "aNonExistingJobId";
  public static final int EXAMPLE_NEGATIVE_JOB_RETRIES = -3; 
  public static final int EXAMPLE_JOB_RETRIES = 3;
  public static final String EXAMPLE_JOB_NO_EXCEPTION_MESSAGE = "";
  public static final String EXAMPLE_EXCEPTION_MESSAGE = "aExceptionMessage";
  public static final String EXAMPLE_EMPTY_JOB_ID = "";
  public static final Date EXAMPLE_DUE_DATE = DateTime.now().toDate();
  public static final Boolean EXAMPLE_WITH_RETRIES_LEFT = true;
  public static final Boolean EXAMPLE_EXECUTABLE = true;
  public static final Boolean EXAMPLE_TIMERS = true;
  public static final Boolean EXAMPLE_MESSAGES = true;
  public static final Boolean EXAMPLE_WITH_EXCEPTION= true;
  // Historic Process Instance
  public static final String EXAMPLE_HIST_PROCESS_DELETE_REASON = "aDeleteReason";
  public static final Date EXAMPLE_END_TIME = DateTime.now().toDate();
  public static final String EXAMPLE_HIST_PROCESS_DURATION_MILLIS_AS_STR = "2000";
  public static final long EXAMPLE_HIST_PROCESS_DURATION_MILLIS_AS_LNG = 2000l;
  public static final Date EXAMPLE_HISTORIC_PROCESS_END_TIME = DateTime.now().toDate();
  public static final Date EXAMPLE_HISTORIC_PROCESS_START_TIME = DateTime.now().toDate();  
  public static final String EXAMPLE_HISTORIC_PROCESS_START_AFTER = "2013-04-23T13:42:43";
  public static final String EXAMPLE_HISTORIC_PROCESS_START_BEFORE ="2013-01-23T13:42:43";
  // Historic Activity Instance
  public static final Date EXAMPLE_ACTIVITY_START_TIME = DateTime.now().toDate();
  public static final Date EXAMPLE_ACTIVITY_END_TIME = DateTime.now().toDate();
  
  // tasks
  public static Task createMockTask() {
    Task mockTask = 
        new MockTaskBuilder().id(EXAMPLE_TASK_ID).name(EXAMPLE_TASK_NAME).assignee(EXAMPLE_TASK_ASSIGNEE_NAME)
        .createTime(DateTime.parse(EXAMPLE_TASK_CREATE_TIME).toDate()).dueDate(DateTime.parse(EXAMPLE_TASK_DUE_DATE).toDate())
        .delegationState(EXAMPLE_TASK_DELEGATION_STATE).description(EXAMPLE_TASK_DESCRIPTION).executionId(EXAMPLE_TASK_EXECUTION_ID)
        .owner(EXAMPLE_TASK_OWNER).parentTaskId(EXAMPLE_TASK_PARENT_TASK_ID).priority(EXAMPLE_TASK_PRIORITY)
        .processDefinitionId(EXAMPLE_PROCESS_DEFINITION_ID).processInstanceId(EXAMPLE_PROCESS_INSTANCE_ID)
        .taskDefinitionKey(EXAMPLE_TASK_DEFINITION_KEY).build();
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
    
    when(mock.getName()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_NAME);
    when(mock.getType()).thenReturn(new StringType());
    when(mock.getTypeName()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_TYPE);
    when(mock.getValue()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_VALUE);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_PROC_INST_ID);
    when(mock.getExecutionId()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_EXECUTION_ID);
    when(mock.getTaskId()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_TASK_ID);
    when(mock.getActivityInstanceId()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_ACTIVITY_INSTANCE_ID);
    
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
    when(mock.getCreated()).thenReturn(DateTime.parse(EXAMPLE_EVENT_SUBSCRIPTION_CREATION_DATE).toDate());
    
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
    ProcessDefinition mockDefinition = 
        builder.id(EXAMPLE_PROCESS_DEFINITION_ID).category(EXAMPLE_PROCESS_DEFINITION_CATEGORY).name(EXAMPLE_PROCESS_DEFINITION_NAME)
          .key(EXAMPLE_PROCESS_DEFINITION_KEY).description(EXAMPLE_PROCESS_DEFINITION_DESCRIPTION)
          .version(EXAMPLE_PROCESS_DEFINITION_VERSION).resource(EXAMPLE_PROCESS_DEFINITION_RESOURCE_NAME)
          .deploymentId(EXAMPLE_DEPLOYMENT_ID).diagram(EXAMPLE_PROCESS_DEFINITION_DIAGRAM_RESOURCE_NAME)
          .suspended(EXAMPLE_PROCESS_DEFINITION_IS_SUSPENDED).build();
    
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
  
  // jobs
  public static Job createMockJob() {
		Job mock = new MockJobBuilder().id(EXAMPLE_JOB_ID)
				.processInstanceId(EXAMPLE_PROCESS_INSTANCE_ID)
				.executionId(EXAMPLE_EXECUTION_ID).retries(EXAMPLE_JOB_RETRIES)
				.exceptionMessage(EXAMPLE_JOB_NO_EXCEPTION_MESSAGE)
				.dueDate(EXAMPLE_DUE_DATE)
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
  
  //History
  public static List<HistoricActivityInstance> createMockHistoricActivityInstances() {
		List<HistoricActivityInstance> mockList = new ArrayList<HistoricActivityInstance>();
			mockList.add(createMockHistoricActivityInstance());
		return mockList;
	 }

	 public static HistoricActivityInstance createMockHistoricActivityInstance() {
		 HistoricActivityInstance mock = mock(HistoricActivityInstance.class);
			when(mock.getActivityId()).thenReturn(EXAMPLE_ACTIVITY_ID);
			when(mock.getActivityName()).thenReturn(EXAMPLE_ACTIVITY_NAME);
			when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
			when(mock.getProcessDefinitionId()).thenReturn(EXAMPLE_PROCESS_DEFINITION_ID);
			when(mock.getActivityType()).thenReturn(EXAMPLE_ACTIVITY_TYPE);	
			when(mock.getStartTime()).thenReturn(EXAMPLE_ACTIVITY_START_TIME);	
			when(mock.getEndTime()).thenReturn(EXAMPLE_ACTIVITY_END_TIME);
			return mock;
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
		    when(mock.getDeleteReason()).thenReturn(EXAMPLE_HIST_PROCESS_DELETE_REASON);
		    when(mock.getEndTime()).thenReturn(EXAMPLE_HISTORIC_PROCESS_END_TIME);
		    when(mock.getStartTime()).thenReturn(EXAMPLE_HISTORIC_PROCESS_START_TIME);
		    when(mock.getDurationInMillis()).thenReturn(EXAMPLE_HIST_PROCESS_DURATION_MILLIS_AS_LNG);
			return mock;
	 }
	 
	 public static List<HistoricVariableInstance> createMockHistoricVariableInstances() {
		List<HistoricVariableInstance> mockList = new ArrayList<HistoricVariableInstance>();
			mockList.add(createMockHistoricVariableInstance());
		return mockList;
	 }

	 public static HistoricVariableInstance createMockHistoricVariableInstance() {
		 HistoricVariableInstance mock = mock(HistoricVariableInstance.class);
		    when(mock.getVariableName()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_NAME);
		    when(mock.getVariableTypeName()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_TYPE);
		    when(mock.getValue()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_VALUE);
		    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_VARIABLE_INSTANCE_PROC_INST_ID);	  
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
}
