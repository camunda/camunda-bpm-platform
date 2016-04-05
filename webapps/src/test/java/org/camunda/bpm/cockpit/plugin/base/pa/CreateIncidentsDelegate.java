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
package org.camunda.bpm.cockpit.plugin.base.pa;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.incident.IncidentContext;
import org.camunda.bpm.engine.impl.persistence.entity.IncidentEntity;

public class CreateIncidentsDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) throws Exception {

    IncidentEntity.createAndInsertIncident("anIncident", createIncidentContext(execution, "test1"), "aMessage");
    IncidentEntity.createAndInsertIncident("anIncident", createIncidentContext(execution, "test2"), "aMessage");
    IncidentEntity.createAndInsertIncident("anIncident", createIncidentContext(execution, "test3"), "aMessage");

    IncidentEntity.createAndInsertIncident("anotherIncident", createIncidentContext(execution, "test1"), "anotherMessage");
    IncidentEntity.createAndInsertIncident("anotherIncident", createIncidentContext(execution, "test2"), "anotherMessage");
    IncidentEntity.createAndInsertIncident("anotherIncident", createIncidentContext(execution, "test3"), "anotherMessage");
    IncidentEntity.createAndInsertIncident("anotherIncident", createIncidentContext(execution, "test4"), "anotherMessage");
    IncidentEntity.createAndInsertIncident("anotherIncident", createIncidentContext(execution, "test5"), "anotherMessage");
  }

  private IncidentContext createIncidentContext(DelegateExecution execution, String configuration) {
    IncidentContext incidentContext = new IncidentContext();
    incidentContext.setExecutionId(execution.getId());
    incidentContext.setActivityId(execution.getCurrentActivityId());
    incidentContext.setProcessDefinitionId(execution.getProcessDefinitionId());
    incidentContext.setTenantId(execution.getTenantId());
    incidentContext.setConfiguration(configuration);
    return incidentContext;
  }

}
