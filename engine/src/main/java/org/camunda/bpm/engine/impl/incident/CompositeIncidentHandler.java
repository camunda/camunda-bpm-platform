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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.util.EnsureUtil;
import org.camunda.bpm.engine.runtime.Incident;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * A composite incident handler that handles incidents of a certain type by the multiple handlers.
 * The result of handling depends on main handler.
 *
 * @see #mainIncidentHandler
 * </p>
 * @see IncidentHandler
 */
public class CompositeIncidentHandler implements IncidentHandler {

  protected IncidentHandler mainIncidentHandler;
  protected final List<IncidentHandler> incidentHandlers = new ArrayList<>();

  /**
   * Constructor that takes a list of {@link IncidentHandler} that consume
   * the incident.
   *
   * @param mainIncidentHandler the main incident handler {@link IncidentHandler} that consume the incident and return result.
   * @param incidentHandlers    the list of {@link IncidentHandler} that consume the incident.
   */
  public CompositeIncidentHandler(IncidentHandler mainIncidentHandler, final List<IncidentHandler> incidentHandlers) {
    initializeIncidentsHandlers(mainIncidentHandler, incidentHandlers);
  }

  /**
   * Constructor that takes a varargs parameter {@link IncidentHandler} that
   * consume the incident.
   *
   * @param mainIncidentHandler the main incident handler {@link IncidentHandler} that consume the incident and return result.
   * @param incidentHandlers    the list of {@link IncidentHandler} that consume the incident.
   */
  public CompositeIncidentHandler(IncidentHandler mainIncidentHandler, final IncidentHandler... incidentHandlers) {
    EnsureUtil.ensureNotNull("Incident handlers", (Object[]) incidentHandlers);
    initializeIncidentsHandlers(mainIncidentHandler, Arrays.asList(incidentHandlers));
  }

  /**
   * Initialize {@link #incidentHandlers} with data transfered from constructor
   *
   * @param incidentHandlers
   */
  protected void initializeIncidentsHandlers(IncidentHandler mainIncidentHandler,
                                             final List<IncidentHandler> incidentHandlers) {
    EnsureUtil.ensureNotNull("Incident handler", mainIncidentHandler);
    this.mainIncidentHandler = mainIncidentHandler;

    EnsureUtil.ensureNotNull("Incident handlers", incidentHandlers);
    for (IncidentHandler incidentHandler : incidentHandlers) {
      add(incidentHandler);
    }
  }

  /**
   * Adds the {@link IncidentHandler} to the list of
   * {@link IncidentHandler} that consume the incident.
   *
   * @param incidentHandler the {@link IncidentHandler} that consume the incident.
   */
  public void add(final IncidentHandler incidentHandler) {
    EnsureUtil.ensureNotNull("Incident handler", incidentHandler);
    String incidentHandlerType = getIncidentHandlerType();
    if (!incidentHandlerType.equals(incidentHandler.getIncidentHandlerType())) {
      throw new ProcessEngineException(
          "Incorrect incident type handler in composite handler with type: " + incidentHandlerType);
    }
    this.incidentHandlers.add(incidentHandler);
  }

  @Override
  public String getIncidentHandlerType() {
    return mainIncidentHandler.getIncidentHandlerType();
  }

  @Override
  public Incident handleIncident(IncidentContext context, String message) {
    Incident incident = mainIncidentHandler.handleIncident(context, message);
    for (IncidentHandler incidentHandler : this.incidentHandlers) {
      incidentHandler.handleIncident(context, message);
    }
    return incident;
  }

  @Override
  public void resolveIncident(IncidentContext context) {
    mainIncidentHandler.resolveIncident(context);
    for (IncidentHandler incidentHandler : this.incidentHandlers) {
      incidentHandler.resolveIncident(context);
    }
  }

  @Override
  public void deleteIncident(IncidentContext context) {
    mainIncidentHandler.deleteIncident(context);
    for (IncidentHandler incidentHandler : this.incidentHandlers) {
      incidentHandler.deleteIncident(context);
    }
  }
}
