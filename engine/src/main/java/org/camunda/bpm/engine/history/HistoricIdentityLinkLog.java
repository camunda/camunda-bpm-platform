package org.camunda.bpm.engine.history;

import java.util.Date;

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

import org.camunda.bpm.engine.identity.GroupQuery;
import org.camunda.bpm.engine.identity.UserQuery;

/**
 * An historic identity link stores the association of a task with a certain identity.
 * 
 * For example, historic identity link is logged on the following conditions:
 * - a user can be an assignee/Candidate/Owner (= identity link type) for a task
 * - a group can be a candidate-group (= identity link type) for a task
 * - a user can be an candidate in the scope of process definition
 * - a group can be a candidate-group in the scope of process definition
 * 
 * For every log, an operation type (add/delete) is added to the database
 * based on the identity link operation
 */
public interface HistoricIdentityLinkLog {
  
  /**
   * Returns the id of historic identity link (Candidate or Assignee or Owner).
   */
  String getId();
  /**
   * Returns the type of link (Candidate or Assignee or Owner).
   * See {@link IdentityLinkType} for the native supported types by the process engine.
   *
   * */
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
   * Returns the userId of the user who assigns a task to the user
   * 
   */
  String getAssignerId();
  
  /**
   * Returns the type of identity link history (add or delete identity link)
   */
  String getOperationType();
  
  /**
   * Returns the time of identity link event (Creation/Deletion)
   */
  Date getTime();

  /**
   * Returns the id of the related process definition 
   */
  String getProcessDefinitionId();
  
  /**
   * Returns the key of the related process definition 
   */
  String getProcessDefinitionKey();
  
  /**
   * Returns the id of the related tenant 
   */
  String getTenantId();
}
