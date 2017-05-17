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
package org.camunda.bpm.engine.impl;

import org.camunda.bpm.engine.query.QueryProperty;

/**
 * @author Roman Smirnov
 *
 */
public interface HistoricIncidentQueryProperty {

  public static final QueryProperty INCIDENT_ID = new QueryPropertyImpl("ID_");
  public static final QueryProperty INCIDENT_CREATE_TIME = new QueryPropertyImpl("CREATE_TIME_");
  public static final QueryProperty INCIDENT_END_TIME = new QueryPropertyImpl("END_TIME_");
  public static final QueryProperty INCIDENT_TYPE = new QueryPropertyImpl("INCIDENT_TYPE_");
  public static final QueryProperty EXECUTION_ID = new QueryPropertyImpl("EXECUTION_ID_");
  public static final QueryProperty ACTIVITY_ID = new QueryPropertyImpl("ACTIVITY_ID_");
  public static final QueryProperty PROCESS_INSTANCE_ID = new QueryPropertyImpl("PROC_INST_ID_");
  public static final QueryProperty PROCESS_DEFINITION_ID = new QueryPropertyImpl("PROC_DEF_ID_");
  public static final QueryProperty CAUSE_INCIDENT_ID = new QueryPropertyImpl("CAUSE_INCIDENT_ID_");
  public static final QueryProperty ROOT_CAUSE_INCIDENT_ID = new QueryPropertyImpl("ROOT_CAUSE_INCIDENT_ID_");
  public static final QueryProperty CONFIGURATION = new QueryPropertyImpl("CONFIGURATION_");
  public static final QueryProperty TENANT_ID = new QueryPropertyImpl("TENANT_ID_");
  public static final QueryProperty INCIDENT_STATE = new QueryPropertyImpl("INCIDENT_STATE_");

}
