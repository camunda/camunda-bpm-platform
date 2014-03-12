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

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.query.QueryProperty;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricIncidentQueryProperty implements QueryProperty {

  private static final long serialVersionUID = 1L;
  private static final Map<String, HistoricIncidentQueryProperty> properties = new HashMap<String, HistoricIncidentQueryProperty>();

  public static final IncidentQueryProperty INCIDENT_ID = new IncidentQueryProperty("RES.ID_");
  public static final IncidentQueryProperty INCIDENT_CREATE_TIME = new IncidentQueryProperty("RES.CREATE_TIME_");
  public static final IncidentQueryProperty INCIDENT_END_TIME = new IncidentQueryProperty("RES.END_TIME_");
  public static final IncidentQueryProperty INCIDENT_TYPE = new IncidentQueryProperty("RES.INCIDENT_TYPE_");
  public static final IncidentQueryProperty EXECUTION_ID = new IncidentQueryProperty("RES.EXECUTION_ID_");
  public static final IncidentQueryProperty ACTIVITY_ID = new IncidentQueryProperty("RES.ACTIVITY_ID_");
  public static final IncidentQueryProperty PROCESS_INSTANCE_ID = new IncidentQueryProperty("RES.PROC_INST_ID_");
  public static final IncidentQueryProperty PROCESS_DEFINITION_ID = new IncidentQueryProperty("RES.PROC_DEF_ID_");
  public static final IncidentQueryProperty CAUSE_INCIDENT_ID = new IncidentQueryProperty("RES.CAUSE_INCIDENT_ID_");
  public static final IncidentQueryProperty ROOT_CAUSE_INCIDENT_ID = new IncidentQueryProperty("RES.ROOT_CAUSE_INCIDENT_ID_");
  public static final IncidentQueryProperty CONFIGURATION = new IncidentQueryProperty("RES.CONFIGURATION_");

  private String name;

  public HistoricIncidentQueryProperty(String name) {
    this.name = name;
    properties.put(name, this);
  }

  public String getName() {
    return name;
  }

  public static HistoricIncidentQueryProperty findByName(String propertyName) {
    return properties.get(propertyName);
  }

}
