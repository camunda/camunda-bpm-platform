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
package org.camunda.bpm.engine.impl.incident;

import java.util.List;

import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.IncidentEntity;
import org.camunda.bpm.engine.runtime.Incident;

/**
 * <p>
 * An incident handler that logs incidents of a certain type
 * as instances of {@link Incident} to the engine database.</p>
 *
 * <p>
 * By default, the process engine has two default handlers:
 * <ul>
 * <li>type <code>failedJob</code>: Indicates jobs without retries left. This incident handler is active by default and must be disabled
 * via {@link org.camunda.bpm.engine.ProcessEngineConfiguration#setCreateIncidentOnFailedJobEnabled(boolean)}.
 * <li>type <code>failedExternalTask</code>: Indicates external tasks without retries left
 * </p>
 *
 * @see IncidentHandler
 *
 * @author nico.rehwaldt
 * @author roman.smirnov
 * @author Falko Menge
 * @author Thorben Lindhauer
 */
public class DefaultIncidentHandler implements IncidentHandler {

  protected String type;

  public DefaultIncidentHandler(String type) {
    this.type = type;
  }

  public String getIncidentHandlerType() {
    return type;
  }

  public Incident handleIncident(IncidentContext context, String message) {
    return createIncident(context, message);
  }

  public Incident createIncident(IncidentContext context, String message) {
    IncidentEntity newIncident = IncidentEntity.createAndInsertIncident(type, context, message);

    if(context.getExecutionId() != null) {
      newIncident.createRecursiveIncidents();
    }

    return newIncident;
  }

  public void resolveIncident(IncidentContext context) {
    removeIncident(context, true);
  }

  public void deleteIncident(IncidentContext context) {
    removeIncident(context, false);
  }

  protected void removeIncident(IncidentContext context, boolean incidentResolved) {
    List<Incident> incidents = Context
        .getCommandContext()
        .getIncidentManager()
        .findIncidentByConfiguration(context.getConfiguration());

    for (Incident currentIncident : incidents) {
      IncidentEntity incident = (IncidentEntity) currentIncident;
      if (incidentResolved) {
        incident.resolve();
      } else {
        incident.delete();
      }
    }
  }
}
