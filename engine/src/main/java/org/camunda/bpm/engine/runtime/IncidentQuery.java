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
}
