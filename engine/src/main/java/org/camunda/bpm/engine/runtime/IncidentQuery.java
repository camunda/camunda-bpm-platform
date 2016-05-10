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
package org.camunda.bpm.engine.runtime;

import org.camunda.bpm.engine.query.Query;

/**
 * @author roman.smirnov
 */
public interface IncidentQuery extends Query<IncidentQuery, Incident> {

  /** Only select incidents which have the given id. **/
  IncidentQuery incidentId(String incidentId);

  /** Only select incidents which have the given incident type. **/
  IncidentQuery incidentType(String incidentType);

  /** Only select incidents which have the given incident message. **/
  IncidentQuery incidentMessage(String incidentMessage);

  /** Only select incidents which have the given process definition id. **/
  IncidentQuery processDefinitionId(String processDefinitionId);

  /** Only select incidents which have the given process instance id. **/
  IncidentQuery processInstanceId(String processInstanceId);

  /** Only select incidents with the given id. **/
  IncidentQuery executionId(String executionId);

  /** Only select incidents which contain an activity with the given id. **/
  IncidentQuery activityId(String activityId);

  /** Only select incidents which contain the id of the cause incident. **/
  IncidentQuery causeIncidentId(String causeIncidentId);

  /** Only select incidents which contain the id of the root cause incident. **/
  IncidentQuery rootCauseIncidentId(String rootCauseIncidentId);

  /** Only select incidents which contain the configuration. **/
  IncidentQuery configuration(String configuration);

  /** Only select incidents that belong to one of the given tenant ids. */
  IncidentQuery tenantIdIn(String... tenantIds);

  /** Only select incidents that belong to one of the given job definition ids. */
  IncidentQuery jobDefinitionIdIn(String... jobDefinitionIds);

  /** Order by id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  IncidentQuery orderByIncidentId();

  /** Order by incidentTimestamp (needs to be followed by {@link #asc()} or {@link #desc()}). */
  IncidentQuery orderByIncidentTimestamp();

  /** Order by incidentType (needs to be followed by {@link #asc()} or {@link #desc()}). */
  IncidentQuery orderByIncidentType();

  /** Order by executionId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  IncidentQuery orderByExecutionId();

  /** Order by activityId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  IncidentQuery orderByActivityId();

  /** Order by processInstanceId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  IncidentQuery orderByProcessInstanceId();

  /** Order by processDefinitionId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  IncidentQuery orderByProcessDefinitionId();

  /** Order by causeIncidentId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  IncidentQuery orderByCauseIncidentId();

  /** Order by rootCauseIncidentId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  IncidentQuery orderByRootCauseIncidentId();

  /** Order by configuration (needs to be followed by {@link #asc()} or {@link #desc()}). */
  IncidentQuery orderByConfiguration();

  /**
   * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of incidents without tenant id is database-specific.
   */
  IncidentQuery orderByTenantId();

}

