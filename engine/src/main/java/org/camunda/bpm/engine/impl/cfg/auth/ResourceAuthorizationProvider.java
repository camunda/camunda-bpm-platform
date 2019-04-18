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
package org.camunda.bpm.engine.impl.cfg.auth;

import org.camunda.bpm.engine.filter.Filter;
import org.camunda.bpm.engine.identity.Group;
import org.camunda.bpm.engine.identity.Tenant;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationEntity;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.IdentityLinkType;
import org.camunda.bpm.engine.task.Task;

/**
 * <p>Manages (create/update/delete) default authorization when an entity is
 * changed</p>
 *
 * <p>Implementations should throw an exception when a specific resource's id is <code>*</code>, as
 * <code>*</code> represents access to all resources/by all users.</p>
 *
 *
 * @author Daniel Meyer
 */
public interface ResourceAuthorizationProvider {

  // Users /////////////////////////////////////////////

  /**
   * <p>Invoked whenever a new user is created</p>
   *
   * @param user
   *          a newly created user
   * @return a list of authorizations to be automatically added when a new user
   *         is created.
   */
  public AuthorizationEntity[] newUser(User user);

  /**
   * <p>Invoked whenever a new group is created</p>
   *
   * @param group
   *          a newly created {@link Group}
   * @return a list of authorizations to be automatically added when a new
   *         {@link Group} is created.
   */
  public AuthorizationEntity[] newGroup(Group group);

  /**
   * <p>
   * Invoked whenever a new tenant is created
   * </p>
   *
   * @param tenant
   *          a newly created {@link Tenant}
   * @return a list of authorizations to be automatically added when a new
   *         {@link Tenant} is created.
   */
  public AuthorizationEntity[] newTenant(Tenant tenant);

  /**
   * <p>Invoked whenever a user is added to a group</p>
   *
   * @param userId
   *          the id of the user who is added to a group a newly created
   *          {@link User}
   * @param groupId
   *          the id of the group to which the user is added
   * @return a list of authorizations to be automatically added when a new
   *         {@link User} is created.
   */
  public AuthorizationEntity[] groupMembershipCreated(String groupId, String userId);

  /**
   * <p>Invoked whenever an user is added to a tenant.</p>
   *
   * @param tenant
   *          the id of the tenant
   * @param userId
   *          the id of the user
   * @return a list of authorizations to be automatically added when a new
   *         membership is created.
   */
  public AuthorizationEntity[] tenantMembershipCreated(Tenant tenant, User user);

  /**
   * <p>Invoked whenever a group is added to a tenant.</p>
   *
   * @param tenant
   *          the id of the tenant
   * @param groupId
   *          the id of the group
   * @return a list of authorizations to be automatically added when a new
   *         membership is created.
   */
  public AuthorizationEntity[] tenantMembershipCreated(Tenant tenant, Group group);

  // Filter ////////////////////////////////////////////////

  /**
   * <p>Invoked whenever a new filter is created</p>
   *
   * @param filter the newly created filter
   * @return a list of authorizations to be automatically added when a new
   *         {@link Filter} is created.
   */
  public AuthorizationEntity[] newFilter(Filter filter);

  // Deployment //////////////////////////////////////////////

  /**
   * <p>Invoked whenever a new deployment is created</p>
   *
   * @param deployment the newly created deployment
   * @return a list of authorizations to be automatically added when a new
   *         {@link Deployment} is created.
   */
  public AuthorizationEntity[] newDeployment(Deployment deployment);

  // Process Definition //////////////////////////////////////

  /**
   * <p>Invoked whenever a new process definition is created</p>
   *
   * @param processDefinition the newly created process definition
   * @return a list of authorizations to be automatically added when a new
   *         {@link ProcessDefinition} is created.
   */
  public AuthorizationEntity[] newProcessDefinition(ProcessDefinition processDefinition);

  // Process Instance ///////////////////////////////////////

  /**
   * <p>Invoked whenever a new process instance is started</p>
   *
   * @param processInstance the newly started process instance
   * @return a list of authorizations to be automatically added when a new
   *         {@link ProcessInstance} is started.
   */
  public AuthorizationEntity[] newProcessInstance(ProcessInstance processInstance);

  // Task /////////////////////////////////////////////////

  /**
   * <p>Invoked whenever a new task is created</p>
   *
   * @param task the newly created task
   * @return a list of authorizations to be automatically added when a new
   *         {@link Task} is created.
   */
  public AuthorizationEntity[] newTask(Task task);

  /**
   * <p>Invoked whenever an user has been assigned to a task.</p>
   *
   * @param task the task on which the assignee has been changed
   * @param oldAssignee the old assignee of the task
   * @param newAssignee the new assignee of the task
   *
   * @return a list of authorizations to be automatically added when an
   *          assignee of a task changes.
   */
  public AuthorizationEntity[] newTaskAssignee(Task task, String oldAssignee, String newAssignee);

  /**
   * <p>Invoked whenever an user has been set as the owner of a task.</p>
   *
   * @param task the task on which the owner has been changed
   * @param oldOwner the old owner of the task
   * @param newOwner the new owner of the task
   *
   * @return a list of authorizations to be automatically added when the
   *          owner of a task changes.
   */
  public AuthorizationEntity[] newTaskOwner(Task task, String oldOwner, String newOwner);

  /**
   * <p>Invoked whenever a new user identity link has been added to a task.</p>
   *
   * @param task the task on which a new identity link has been added
   * @param userId the user for which the identity link has been created
   * @param type the type of the identity link (e.g. {@link IdentityLinkType#CANDIDATE})
   *
   * @return a list of authorizations to be automatically added when
   *          a new user identity link has been added.
   */
  public AuthorizationEntity[] newTaskUserIdentityLink(Task task, String userId, String type);

  /**
   * <p>Invoked whenever a new group identity link has been added to a task.</p>
   *
   * @param task the task on which a new identity link has been added
   * @param groupId the group for which the identity link has been created
   * @param type the type of the identity link (e.g. {@link IdentityLinkType#CANDIDATE})
   *
   * @return a list of authorizations to be automatically added when
   *          a new group identity link has been added.
   */
  public AuthorizationEntity[] newTaskGroupIdentityLink(Task task, String groupId, String type);

  /**
   * <p>Invoked whenever a user identity link of a task has been deleted.</p>
   *
   * @param task the task on which the identity link has been deleted
   * @param userId the user for which the identity link has been deleted
   * @param type the type of the identity link (e.g. {@link IdentityLinkType#CANDIDATE})
   *
   * @return a list of authorizations to be automatically deleted when
   *          a user identity link has been deleted.
   */
  public AuthorizationEntity[] deleteTaskUserIdentityLink(Task task, String userId, String type);

  /**
   * <p>Invoked whenever a group identity link of a task has been deleted.</p>
   *
   * @param task the task on which the identity link has been deleted
   * @param groupId the group for which the identity link has been deleted
   * @param type the type of the identity link (e.g. {@link IdentityLinkType#CANDIDATE})
   *
   * @return a list of authorizations to be automatically deleted when
   *          a group identity link has been deleted.
   */
  public AuthorizationEntity[] deleteTaskGroupIdentityLink(Task task, String groupId, String type);

  /**
   * <p>Invoked whenever a new decision definition is created.</p>
   *
   * @param decisionDefinition the newly created decision definition
   * @return a list of authorizations to be automatically added when a new
   *         {@link DecisionDefinition} is created.
   */
  public AuthorizationEntity[] newDecisionDefinition(DecisionDefinition decisionDefinition);

  /**
   * <p>Invoked whenever a new decision requirements definition is created.</p>
   *
   * @param decisionRequirementsDefinition the newly created decision requirements definition
   * @return a list of authorizations to be automatically added when a new
   *         {@link DecisionRequirementsDefinition} is created.
   */
  public AuthorizationEntity[] newDecisionRequirementsDefinition(DecisionRequirementsDefinition decisionRequirementsDefinition);

}
