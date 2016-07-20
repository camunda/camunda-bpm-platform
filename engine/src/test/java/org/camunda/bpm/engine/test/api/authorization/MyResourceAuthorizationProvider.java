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
package org.camunda.bpm.engine.test.api.authorization;

import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.cfg.auth.ResourceAuthorizationProvider;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;

/**
 * @author Roman Smirnov
 *
 */
public class MyResourceAuthorizationProvider implements ResourceAuthorizationProvider {

  // assignee
  public static String OLD_ASSIGNEE;
  public static String NEW_ASSIGNEE;

  // owner
  public static String OLD_OWNER;
  public static String NEW_OWNER;

  // add user identity link
  public static String ADD_USER_IDENTITY_LINK_TYPE;
  public static String ADD_USER_IDENTITY_LINK_USER;

  // delete user identity link
  public static String DELETE_USER_IDENTITY_LINK_TYPE = null;
  public static String DELETE_USER_IDENTITY_LINK_USER = null;

  // add group identity link
  public static String ADD_GROUP_IDENTITY_LINK_TYPE;
  public static String ADD_GROUP_IDENTITY_LINK_GROUP;

  // delete group identity link
  public static String DELETE_GROUP_IDENTITY_LINK_TYPE = null;
  public static String DELETE_GROUP_IDENTITY_LINK_GROUP = null;

  public AuthorizationEntity[] newUser(User user) {
    return null;
  }

  public AuthorizationEntity[] newGroup(Group group) {
    return null;
  }

  public AuthorizationEntity[] newTenant(Tenant tenant) {
    return null;
  }

  public AuthorizationEntity[] groupMembershipCreated(String groupId, String userId) {
    return null;
  }

  public AuthorizationEntity[] tenantMembershipCreated(Tenant tenant, User user) {
    return null;
  }

  public AuthorizationEntity[] tenantMembershipCreated(Tenant tenant, Group group) {
    return null;
  }

  public AuthorizationEntity[] newFilter(Filter filter) {
    return null;
  }

  public AuthorizationEntity[] newDeployment(Deployment deployment) {
    return null;
  }

  public AuthorizationEntity[] newProcessDefinition(ProcessDefinition processDefinition) {
    return null;
  }

  public AuthorizationEntity[] newProcessInstance(ProcessInstance processInstance) {
    return null;
  }

  public AuthorizationEntity[] newTask(Task task) {
    return null;
  }

  public AuthorizationEntity[] newTaskAssignee(Task task, String oldAssignee, String newAssignee) {
    OLD_ASSIGNEE = oldAssignee;
    NEW_ASSIGNEE = newAssignee;
    return null;
  }

  public AuthorizationEntity[] newTaskOwner(Task task, String oldOwner, String newOwner) {
    OLD_OWNER = oldOwner;
    NEW_OWNER = newOwner;
    return null;
  }

  public AuthorizationEntity[] newTaskUserIdentityLink(Task task, String userId, String type) {
    ADD_USER_IDENTITY_LINK_TYPE = type;
    ADD_USER_IDENTITY_LINK_USER = userId;
    return null;
  }

  public AuthorizationEntity[] newTaskGroupIdentityLink(Task task, String groupId, String type) {
    ADD_GROUP_IDENTITY_LINK_TYPE = type;
    ADD_GROUP_IDENTITY_LINK_GROUP = groupId;
    return null;
  }

  public AuthorizationEntity[] deleteTaskUserIdentityLink(Task task, String userId, String type) {
    DELETE_USER_IDENTITY_LINK_TYPE = type;
    DELETE_USER_IDENTITY_LINK_USER = userId;
    return null;
  }

  public AuthorizationEntity[] deleteTaskGroupIdentityLink(Task task, String groupId, String type) {
    DELETE_GROUP_IDENTITY_LINK_TYPE = type;
    DELETE_GROUP_IDENTITY_LINK_GROUP = groupId;
    return null;
  }

  public static void clearProperties() {
    OLD_ASSIGNEE = null;
    NEW_ASSIGNEE = null;
    OLD_OWNER = null;
    NEW_OWNER = null;
    ADD_USER_IDENTITY_LINK_TYPE = null;
    ADD_USER_IDENTITY_LINK_USER = null;
    ADD_GROUP_IDENTITY_LINK_TYPE = null;
    ADD_GROUP_IDENTITY_LINK_GROUP = null;
    DELETE_USER_IDENTITY_LINK_TYPE = null;
    DELETE_USER_IDENTITY_LINK_USER = null;
    DELETE_GROUP_IDENTITY_LINK_TYPE = null;
    DELETE_GROUP_IDENTITY_LINK_GROUP = null;
  }

  public AuthorizationEntity[] newDecisionDefinition(DecisionDefinition decisionDefinition) {
    return null;
  }

  public AuthorizationEntity[] newDecisionRequirementsDefinition(DecisionRequirementsDefinition decisionRequirementsDefinition) {
    return null;
  }

}
