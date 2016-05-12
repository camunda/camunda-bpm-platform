/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl.migration.instance;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventProcessor;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.IncidentEntity;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;

public class MigratingIncident implements MigratingInstance {

  protected IncidentEntity incident;
  protected ScopeImpl targetScope;
  protected String targetJobDefinitionId;

  public MigratingIncident(IncidentEntity incident, ScopeImpl targetScope) {
    this.incident = incident;
    this.targetScope = targetScope;
  }

  public void setTargetJobDefinitionId(String targetJobDefinitionId) {
    this.targetJobDefinitionId = targetJobDefinitionId;
  }

  @Override
  public boolean isDetached() {
    return incident.getExecutionId() == null;
  }

  public void detachState() {
    incident.setExecution(null);
  }

  public void attachState(MigratingScopeInstance newOwningInstance) {
    attachTo(newOwningInstance.resolveRepresentativeExecution());
  }

  @Override
  public void attachState(MigratingTransitionInstance targetTransitionInstance) {
    attachTo(targetTransitionInstance.resolveRepresentativeExecution());
  }

  public void migrateState() {
    incident.setActivityId(targetScope.getId());
    incident.setProcessDefinitionId(targetScope.getProcessDefinition().getId());
    incident.setJobDefinitionId(targetJobDefinitionId);

    migrateHistory();
  }

  protected void migrateHistory() {
    HistoryLevel historyLevel = Context.getProcessEngineConfiguration().getHistoryLevel();

    if (historyLevel.isHistoryEventProduced(HistoryEventTypes.INCIDENT_MIGRATE, this)) {
      HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
        @Override
        public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
          return producer.createHistoricIncidentMigrateEvt(incident);
        }
      });
    }
  }

  public void migrateDependentEntities() {
    // nothing to do
  }

  protected void attachTo(ExecutionEntity execution) {
    incident.setExecution(execution);
  }
}
