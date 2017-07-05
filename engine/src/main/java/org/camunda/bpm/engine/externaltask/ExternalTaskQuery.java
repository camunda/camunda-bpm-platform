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
package org.camunda.bpm.engine.externaltask;

import java.util.Date;

import org.camunda.bpm.engine.query.Query;

/**
 * @author Thorben Lindhauer
 * @author Christopher Zell
 */
public interface ExternalTaskQuery extends Query<ExternalTaskQuery, ExternalTask> {

  /**
   * Only select the external task with the given id
   */
  ExternalTaskQuery externalTaskId(String externalTaskId);

  /**
   * Only select external tasks that was most recently locked by the given worker
   */
  ExternalTaskQuery workerId(String workerId);

  /**
   * Only select external tasks that have a lock expiring before the given date
   */
  ExternalTaskQuery lockExpirationBefore(Date lockExpirationDate);

  /**
   * Only select external tasks that have a lock expiring after the given date
   */
  ExternalTaskQuery lockExpirationAfter(Date lockExpirationDate);

  /**
   * Only select external tasks of the given topic
   */
  ExternalTaskQuery topicName(String topicName);

  /**
   * Only select external tasks that are currently locked, i.e. that have a lock expiration
   * time that is in the future
   */
  ExternalTaskQuery locked();

  /**
   * Only select external tasks that are not currently locked, i.e. that have no
   * lock expiration time or one that is overdue
   */
  ExternalTaskQuery notLocked();

  /**
   * Only select external tasks created in the context of the given execution
   */
  ExternalTaskQuery executionId(String executionId);

  /**
   * Only select external tasks created in the context of the given process instance
   */
  ExternalTaskQuery processInstanceId(String processInstanceId);

  /**
   * Only select external tasks created in the context of the given process instances
   */
  ExternalTaskQuery processInstanceIdIn(String... processInstanceIdIn);

  /**
   * Only select external tasks that belong to an instance of the given process definition
   */
  ExternalTaskQuery processDefinitionId(String processDefinitionId);

  /**
   * Only select external tasks that belong to an instance of the given activity
   */
  ExternalTaskQuery activityId(String activityId);

  /**
   * Only select external tasks that belong to an instances of the given activities.
   */
  ExternalTaskQuery activityIdIn(String... activityIdIn);
  
  /**
   * Only select external tasks with a priority that is higher than or equal to the given priority.
   *
   * @since 7.5
   * @param priority the priority which is used for the query
   * @return the builded external task query
   */
  ExternalTaskQuery priorityHigherThanOrEquals(long priority);

  /**
   * Only select external tasks with a priority that is lower than or equal to the given priority.
   *
   * @since 7.5
   * @param priority the priority which is used for the query
   * @return the builded external task query
   */
  ExternalTaskQuery priorityLowerThanOrEquals(long priority);
    
  /**
   * Only select external tasks that are currently suspended
   */
  ExternalTaskQuery suspended();

  /**
   * Only select external tasks that are currently not suspended
   */
  ExternalTaskQuery active();

  /**
   * Only select external tasks that have retries > 0
   */
  ExternalTaskQuery withRetriesLeft();

  /**
   * Only select external tasks that have retries = 0
   */
  ExternalTaskQuery noRetriesLeft();
  
  /** Only select external tasks that belong to one of the given tenant ids. */
  ExternalTaskQuery tenantIdIn(String... tenantIds);

  /**
   * Order by external task id (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  ExternalTaskQuery orderById();

  /**
   * Order by lock expiration time (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Ordering of tasks with no lock expiration time is database-dependent.
   */
  ExternalTaskQuery orderByLockExpirationTime();

  /**
   * Order by process instance id (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  ExternalTaskQuery orderByProcessInstanceId();

  /**
   * Order by process definition id (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  ExternalTaskQuery orderByProcessDefinitionId();

  /**
   * Order by process definition key (needs to be followed by {@link #asc()} or {@link #desc()}).
   */
  ExternalTaskQuery orderByProcessDefinitionKey();

  /**
   * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of external tasks without tenant id is database-specific.
   */
  ExternalTaskQuery orderByTenantId();
  
  /**
   * Order by priority (needs to be followed by {@link #asc()} or {@link #desc()}).
   * @since 7.5
   */
  ExternalTaskQuery orderByPriority();

}
