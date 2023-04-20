/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.cfg.CommandChecker;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.history.event.HistoricIncidentEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.event.HistoryEventProcessor;
import org.camunda.bpm.engine.impl.history.event.HistoryEventTypes;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.IncidentEntity;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

public class SetAnnotationForIncidentCmd implements Command<Void> {

  protected String incidentId;
  protected String annotation;

  public SetAnnotationForIncidentCmd(String incidentId, String annotation) {
    this.incidentId = incidentId;
    this.annotation = annotation;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    EnsureUtil.ensureNotNull(NotValidException.class, "incident id", incidentId);

    IncidentEntity incident = (IncidentEntity) commandContext.getIncidentManager().findIncidentById(incidentId);
    EnsureUtil.ensureNotNull(BadUserRequestException.class, "incident", incident);
    ExecutionEntity execution = null;
    if (incident.getExecutionId() != null) {
      execution = commandContext.getExecutionManager().findExecutionById(incident.getExecutionId());
      if (execution != null) {
        // check rights for updating an execution-related incident
        for (CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
          checker.checkUpdateProcessInstance(execution);
        }
      }
    }

    incident.setAnnotation(annotation);

    triggerHistoryEvent(commandContext, incident);

    String tenantId = execution != null ? execution.getTenantId() : null;
    if (annotation == null) {
      commandContext.getOperationLogManager()
          .logClearIncidentAnnotationOperation(incidentId, tenantId);

    } else {
      commandContext.getOperationLogManager()
          .logSetIncidentAnnotationOperation(incidentId, tenantId);
    }

    return null;
  }

  protected void triggerHistoryEvent(CommandContext commandContext, IncidentEntity incident) {
    HistoryLevel historyLevel = commandContext.getProcessEngineConfiguration().getHistoryLevel();
    if (historyLevel.isHistoryEventProduced(HistoryEventTypes.INCIDENT_UPDATE, incident)) {
      HistoryEventProcessor.processHistoryEvents(new HistoryEventProcessor.HistoryEventCreator() {
        @Override
        public HistoryEvent createHistoryEvent(HistoryEventProducer producer) {
          HistoricIncidentEventEntity incidentUpdateEvt = (HistoricIncidentEventEntity)
              producer.createHistoricIncidentUpdateEvt(incident);
          incidentUpdateEvt.setAnnotation(annotation);
          return incidentUpdateEvt;
        }
      });
    }
  }

}
