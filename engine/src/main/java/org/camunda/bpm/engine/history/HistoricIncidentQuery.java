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
package org.camunda.bpm.engine.history;

import org.camunda.bpm.engine.query.Query;

/**
 * @author Roman Smirnov
 *
 */
public interface HistoricIncidentQuery extends Query<HistoricIncidentQuery, HistoricIncident> {

  /** Only select historic incidents which have the given id. **/
  HistoricIncidentQuery incidentId(String incidentId);

  /** Only select historic incidents which have the given incident type. **/
  HistoricIncidentQuery incidentType(String incidentType);

  /** Only select historic incidents which have the given incident message. **/
  HistoricIncidentQuery incidentMessage(String incidentMessage);

  /** Only select historic incidents which have the given process definition id. **/
  HistoricIncidentQuery processDefinitionId(String processDefinitionId);

  /** Only select historic incidents which have the given process instance id. **/
  HistoricIncidentQuery processInstanceId(String processInstanceId);

  /** Only select historic incidents with the given id. **/
  HistoricIncidentQuery executionId(String executionId);

  /** Only select historic incidents which contain an activity with the given id. **/
  HistoricIncidentQuery activityId(String activityId);

  /** Only select historic incidents which contain the id of the cause incident. **/
  HistoricIncidentQuery causeIncidentId(String causeIncidentId);

  /** Only select historic incidents which contain the id of the root cause incident. **/
  HistoricIncidentQuery rootCauseIncidentId(String rootCauseIncidentId);

  /** Only select historic incidents that belong to one of the given tenant ids. */
  HistoricIncidentQuery tenantIdIn(String... tenantIds);

  /** Only select incidents which contain the configuration. **/
  HistoricIncidentQuery configuration(String configuration);

  /** Only select incidents that belong to one of the given job definition ids. */
  HistoricIncidentQuery jobDefinitionIdIn(String... jobDefinitionIds);

  /** Only select historic incidents which are open. **/
  HistoricIncidentQuery open();

  /** Only select historic incidents which are resolved. **/
  HistoricIncidentQuery resolved();

  /** Only select historic incidents which are deleted. **/
  HistoricIncidentQuery deleted();

  /** Order by id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByIncidentId();

  /** Order by create time (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByCreateTime();

  /** Order by end time (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByEndTime();

  /** Order by incidentType (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByIncidentType();

  /** Order by executionId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByExecutionId();

  /** Order by activityId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByActivityId();

  /** Order by processInstanceId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByProcessInstanceId();

  /** Order by processDefinitionId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByProcessDefinitionId();

  /** Order by causeIncidentId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByCauseIncidentId();

  /** Order by rootCauseIncidentId (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByRootCauseIncidentId();

  /** Order by configuration (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByConfiguration();

  /** Order by incidentState (needs to be followed by {@link #asc()} or {@link #desc()}). */
  HistoricIncidentQuery orderByIncidentState();

  /**
   * Order by tenant id (needs to be followed by {@link #asc()} or {@link #desc()}).
   * Note that the ordering of incidents without tenant id is database-specific.
   */
  HistoricIncidentQuery orderByTenantId();

}
