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
package org.camunda.bpm.engine.task;

import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.UserQuery;


/**
 * An identity link is used to associate a task with a certain identity.
 * 
 * For example:
 * - a user can be an assignee (= identity link type) for a task
 * - a group can be a candidate-group (= identity link type) for a task
 * 
 * @author Joram Barrez
 */
public interface IdentityLink {
  
  /**
   * Get the Id of identityLink 
   */
   String getId();	
  /**
   * Returns the type of link.
   * See {@link IdentityLinkType} for the native supported types by the process engine.
   */
  String getType();
  
  /**
   * If the identity link involves a user, then this will be a non-null id of a user.
   * That userId can be used to query for user information through the {@link UserQuery} API.
   */
  String getUserId();
  
  /**
   * If the identity link involves a group, then this will be a non-null id of a group.
   * That groupId can be used to query for user information through the {@link GroupQuery} API.
   */
  String getGroupId();
  
  /**
   * The id of the task associated with this identity link.
   */
  String getTaskId();

  /**
   * Get the process definition id
   */
  public String getProcessDefId();
  
  /**
   * The id of the tenant associated with this identity link.
   *
   * @since 7.5
   * 
   */
  public String getTenantId();
 
}
