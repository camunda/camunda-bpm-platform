package org.camunda.bpm.engine.rest.helper;

import org.camunda.bpm.engine.task.DelegationState;

public abstract class ExampleDataProvider {

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
  public static final String EXAMPLE_PROCESS_INSTANCE_ID = "anId";
  public static final boolean EXAMPLE_PROCESS_INSTANCE_IS_SUSPENDED = false;
  public static final boolean EXAMPLE_PROCESS_INSTANCE_IS_ENDED = false;
  
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
 
  public static final int ANOTHER_EXAMPLE_FAILED_JOBS = 43;
  public static final int ANOTHER_EXAMPLE_INSTANCES = 124;
 
  // user & groups
  public static final String EXAMPLE_GROUP_ID = "group1Id";
  public static final String EXAMPLE_GROUP_NAME = "group1";
 
  public static final String EXAMPLE_USER_ID = "userId";
  public static final String EXAMPLE_USER_FIRST_NAME = "firstName";
  public static final String EXAMPLE_USER_LAST_NAME = "lastName";
}
