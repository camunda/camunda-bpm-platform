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
package org.camunda.bpm.engine.management;

import org.camunda.bpm.engine.query.Query;

/**
 * Allows programmatic querying of {@link JobDefinition}s.
 *
 * @author roman.smirnov
 */
public interface JobDefinitionQuery extends Query<JobDefinitionQuery, JobDefinition> {

  /** Only select job definitions with the given id */
  JobDefinitionQuery jobDefinitionId(String jobDefinitionId);

  /** Only select job definitions which exist for the listed activity ids */
  JobDefinitionQuery activityIdIn(String... activityIds);

  /** Only select job definitions which exist for the given process definition id. **/
  JobDefinitionQuery processDefinitionId(String processDefinitionId);

  /** Only select job definitions which exist for the given process definition key. **/
  JobDefinitionQuery processDefinitionKey(String processDefinitionKey);

  /** Only select job definitions which have the given job type. **/
  JobDefinitionQuery jobType(String jobType);

  /** Only select job definitions which contain the configuration. **/
  JobDefinitionQuery jobConfiguration(String jobConfiguration);

  /** Only selects job definitions which are active **/
  JobDefinitionQuery active();

  /** Only selects job definitions which are suspended **/
  JobDefinitionQuery suspended();

  /** Order by id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  JobDefinitionQuery orderByJobDefinitionId();

  /** Order by activty id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  JobDefinitionQuery orderByActivityId();

  /** Order by process defintion id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  JobDefinitionQuery orderByProcessDefinitionId();

  /** Order by process definition key (needs to be followed by {@link #asc()} or {@link #desc()}). */
  JobDefinitionQuery orderByProcessDefinitionKey();

  /** Order by job type (needs to be followed by {@link #asc()} or {@link #desc()}). */
  JobDefinitionQuery orderByJobType();

  /** Order by job configuration (needs to be followed by {@link #asc()} or {@link #desc()}). */
  JobDefinitionQuery orderByJobConfiguration();

}
