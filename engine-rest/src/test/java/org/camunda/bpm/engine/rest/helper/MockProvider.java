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
import java.util.List;

import org.camunda.bpm.engine.form.FormProperty;
import org.camunda.bpm.engine.form.FormType;
import org.camunda.bpm.engine.form.StartFormData;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.management.ActivityStatistics;
import org.camunda.bpm.engine.management.IncidentStatistics;
import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
import org.joda.time.DateTime;

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
  
  
  // execution
  public static final String EXAMPLE_EXECUTION_ID = "anExecutionId";
  public static final boolean EXAMPLE_EXECUTION_IS_ENDED = false;
  
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
 
  public static final String EXAMPLE_USER_ID = "userId";
  public static final String EXAMPLE_USER_FIRST_NAME = "firstName";
  public static final String EXAMPLE_USER_LAST_NAME = "lastName";
  
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
  
  public static Execution createMockExecution() {
    Execution mock = mock(Execution.class);
    
    when(mock.getId()).thenReturn(EXAMPLE_EXECUTION_ID);
    when(mock.getProcessInstanceId()).thenReturn(EXAMPLE_PROCESS_INSTANCE_ID);
    when(mock.isEnded()).thenReturn(EXAMPLE_EXECUTION_IS_ENDED);
    
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
  public static List<Group> createMockGroups() {
    List<Group> mockGroups = new ArrayList<Group>();
    Group mockGroup = mock(Group.class);
    when(mockGroup.getId()).thenReturn(EXAMPLE_GROUP_ID);
    when(mockGroup.getName()).thenReturn(EXAMPLE_GROUP_NAME);
    mockGroups.add(mockGroup);
    
    return mockGroups;
  }
  
  public static User createMockUser() {
    User mockUser = mock(User.class);
    when(mockUser.getId()).thenReturn(EXAMPLE_USER_ID);
    when(mockUser.getFirstName()).thenReturn(EXAMPLE_USER_FIRST_NAME);
    when(mockUser.getLastName()).thenReturn(EXAMPLE_USER_LAST_NAME);
    return mockUser;
  }
}
