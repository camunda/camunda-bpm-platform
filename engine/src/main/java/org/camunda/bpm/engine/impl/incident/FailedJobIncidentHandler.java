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
package org.camunda.bpm.engine.impl.incident;

import org.camunda.bpm.engine.impl.persistence.entity.IncidentEntity;

public class FailedJobIncidentHandler implements IncidentHandler {

  public final static String INCIDENT_HANDLER_TYPE = "failedJob";
  
  public String getIncidentHandlerType() {
    return INCIDENT_HANDLER_TYPE;
  }

  public void handleIncident(String executionId, String configuration) {
    IncidentEntity.createAndInsertIncident(true, INCIDENT_HANDLER_TYPE, executionId, null, null, configuration);
  }

  public void resolveIncident(String executionId, String configuration) {
    // TODO:
  }

}
