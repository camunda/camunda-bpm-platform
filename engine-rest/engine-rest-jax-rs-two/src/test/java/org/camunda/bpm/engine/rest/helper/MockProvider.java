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

import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.calendar.DateTimeUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.StringValue;

import java.util.ArrayList;
import java.util.List;

import static org.camunda.bpm.engine.rest.util.DateTimeUtils.withTimezone;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Provides mocks for the basic engine entities, such as
 * {@link ProcessDefinition}, {@link User}, etc., that are reused across the
 * various kinds of tests.
 */
public abstract class MockProvider {

  public static final String EXAMPLE_USER_ID = "userId";
  public static final String EXAMPLE_USER_PASSWORD = "s3cret";
  public static final String EXAMPLE_VARIABLE_INSTANCE_NAME = "aVariableInstanceName";
  public static final String EXTERNAL_TASK_ID = "anExternalTaskId";
  public static final String EXTERNAL_TASK_TOPIC_NAME = "aTopic";
  public static final String EXTERNAL_TASK_WORKER_ID = "aWorkerId";
  public static final String EXTERNAL_TASK_LOCK_EXPIRATION_TIME = withTimezone("2015-10-05T13:25:00");
  public static final String EXAMPLE_PROCESS_INSTANCE_ID = "aProcInstId";
  public static final String EXAMPLE_EXECUTION_ID = "anExecutionId";
  public static final String EXAMPLE_ACTIVITY_ID = "anActivity";
  public static final String EXAMPLE_ACTIVITY_INSTANCE_ID = "anActivityInstanceId";
  public static final String EXAMPLE_PROCESS_DEFINITION_ID = "aProcDefId";
  public static final String EXAMPLE_PROCESS_DEFINITION_KEY = "aKey";
  public static final String EXAMPLE_TENANT_ID = "aTenantId";
  public static final Integer EXTERNAL_TASK_RETRIES = new Integer(5);
  public static final String EXTERNAL_TASK_ERROR_MESSAGE = "some error";
  public static final long EXTERNAL_TASK_PRIORITY = Integer.MAX_VALUE + 466L;
  public static final StringValue EXAMPLE_PRIMITIVE_VARIABLE_VALUE = Variables.stringValue("aVariableInstanceValue");
  public static final boolean EXTERNAL_TASK_SUSPENDED = true;
  public static final String EXAMPLE_GROUP_ID = "groupId1";
  public static final String EXAMPLE_GROUP_NAME = "group1";
  public static final String EXAMPLE_GROUP_TYPE = "organizational-unit";
  public static final String EXAMPLE_GROUP_NAME_UPDATE = "group1Update";
  public static final String EXAMPLE_USER_FIRST_NAME = "firstName";
  public static final String EXAMPLE_USER_LAST_NAME = "lastName";
  public static final String EXAMPLE_USER_EMAIL = "test@example.org";
  public static final String EXAMPLE_TENANT_NAME = "aTenantName";
  // engine
  public static final String EXAMPLE_PROCESS_ENGINE_NAME = "default";
  public static final String ANOTHER_EXAMPLE_PROCESS_ENGINE_NAME = "anotherEngineName";
  public static final String NON_EXISTING_PROCESS_ENGINE_NAME = "aNonExistingEngineName";

  public static LockedExternalTask createMockLockedExternalTask() {
    return mockExternalTask()
      .variable(EXAMPLE_VARIABLE_INSTANCE_NAME, EXAMPLE_PRIMITIVE_VARIABLE_VALUE)
      .buildLockedExternalTask();
  }

  public static MockExternalTaskBuilder mockExternalTask() {
    return new MockExternalTaskBuilder()
      .id(EXTERNAL_TASK_ID)
      .activityId(EXAMPLE_ACTIVITY_ID)
      .activityInstanceId(EXAMPLE_ACTIVITY_INSTANCE_ID)
      .errorMessage(EXTERNAL_TASK_ERROR_MESSAGE)
      .executionId(EXAMPLE_EXECUTION_ID)
      .lockExpirationTime(DateTimeUtil.parseDate(EXTERNAL_TASK_LOCK_EXPIRATION_TIME))
      .processDefinitionId(EXAMPLE_PROCESS_DEFINITION_ID)
      .processDefinitionKey(EXAMPLE_PROCESS_DEFINITION_KEY)
      .processInstanceId(EXAMPLE_PROCESS_INSTANCE_ID)
      .retries(EXTERNAL_TASK_RETRIES)
      .suspended(EXTERNAL_TASK_SUSPENDED)
      .topicName(EXTERNAL_TASK_TOPIC_NAME)
      .workerId(EXTERNAL_TASK_WORKER_ID)
      .tenantId(EXAMPLE_TENANT_ID)
      .priority(EXTERNAL_TASK_PRIORITY);

  }


  // user, groups and tenants

  public static Group createMockGroup() {
    return mockGroup().build();
  }

  public static MockGroupBuilder mockGroup() {
    return new MockGroupBuilder()
      .id(EXAMPLE_GROUP_ID)
      .name(EXAMPLE_GROUP_NAME)
      .type(EXAMPLE_GROUP_TYPE);
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
    return mockUser().build();
  }

  public static MockUserBuilder mockUser() {
    return new MockUserBuilder()
      .id(EXAMPLE_USER_ID)
      .firstName(EXAMPLE_USER_FIRST_NAME)
      .lastName(EXAMPLE_USER_LAST_NAME)
      .email(EXAMPLE_USER_EMAIL)
      .password(EXAMPLE_USER_PASSWORD);
  }

  public static Tenant createMockTenant() {
    Tenant mockTenant = mock(Tenant.class);
    when(mockTenant.getId()).thenReturn(EXAMPLE_TENANT_ID);
    when(mockTenant.getName()).thenReturn(EXAMPLE_TENANT_NAME);
    return mockTenant;
  }

}
