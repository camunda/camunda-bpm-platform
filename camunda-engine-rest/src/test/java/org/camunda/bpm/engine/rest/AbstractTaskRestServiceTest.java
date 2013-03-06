package org.camunda.bpm.engine.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.form.FormProperty;
import org.camunda.bpm.engine.form.FormType;
import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.rest.helper.MockTaskBuilder;
import org.camunda.bpm.engine.task.DelegationState;
import org.camunda.bpm.engine.task.Task;
import org.joda.time.DateTime;

public abstract class AbstractTaskRestServiceTest extends AbstractRestServiceTest {

  // task properties
  protected static final String EXAMPLE_ID = "anId";
  protected static final String EXAMPLE_NAME = "aName";
  protected static final String EXAMPLE_ASSIGNEE_NAME = "anAssignee";
  protected static final String EXAMPLE_CREATE_TIME = "2013-01-23T13:42:42";
  protected static final String EXAMPLE_DUE_DATE = "2013-01-23T13:42:43";
  protected static final DelegationState EXAMPLE_DELEGATION_STATE = DelegationState.RESOLVED;
  protected static final String EXAMPLE_DESCRIPTION = "aDescription";
  protected static final String EXAMPLE_EXECUTION_ID = "anExecution";
  protected static final String EXAMPLE_OWNER = "anOwner";
  protected static final String EXAMPLE_PARENT_TASK_ID = "aParentId";
  protected static final int EXAMPLE_PRIORITY = 42;
  protected static final String EXAMPLE_PROCESS_DEFINITION_ID = "aProcDefId";
  protected static final String EXAMPLE_PROCESS_INSTANCE_ID = "aProcInstId";
  protected static final String EXAMPLE_TASK_DEFINITION_KEY = "aTaskDefinitionKey";
  
  // form data
  protected static final String EXAMPLE_FORM_KEY = "aFormKey";
  protected static final String EXAMPLE_DEPLOYMENT_ID = "aDeploymentId";
  
  // form property data
  protected static final String EXAMPLE_FORM_PROPERTY_ID = "aFormPropertyId";
  protected static final String EXAMPLE_FORM_PROPERTY_NAME = "aFormName";
  protected static final String EXAMPLE_FORM_PROPERTY_TYPE_NAME = "aFormPropertyTypeName";
  protected static final String EXAMPLE_FORM_PROPERTY_VALUE = "aValue";
  protected static final boolean EXAMPLE_FORM_PROPERTY_READABLE = true;
  protected static final boolean EXAMPLE_FORM_PROPERTY_WRITABLE = true;
  protected static final boolean EXAMPLE_FORM_PROPERTY_REQUIRED = true;
  
  protected Task createMockTask() {
    Task mockTask = 
        new MockTaskBuilder().id(EXAMPLE_ID).name(EXAMPLE_NAME).assignee(EXAMPLE_ASSIGNEE_NAME)
        .createTime(DateTime.parse(EXAMPLE_CREATE_TIME).toDate()).dueDate(DateTime.parse(EXAMPLE_DUE_DATE).toDate())
        .delegationState(EXAMPLE_DELEGATION_STATE).description(EXAMPLE_DESCRIPTION).executionId(EXAMPLE_EXECUTION_ID)
        .owner(EXAMPLE_OWNER).parentTaskId(EXAMPLE_PARENT_TASK_ID).priority(EXAMPLE_PRIORITY)
        .processDefinitionId(EXAMPLE_PROCESS_DEFINITION_ID).processInstanceId(EXAMPLE_PROCESS_INSTANCE_ID)
        .taskDefinitionKey(EXAMPLE_TASK_DEFINITION_KEY).build();
    return mockTask;
  }
  
  protected List<Task> createMockTasks() {
    List<Task> mocks = new ArrayList<Task>();
    mocks.add(createMockTask());
    return mocks;
  }
  
  protected TaskFormData createMockTaskFormData() {
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
}
