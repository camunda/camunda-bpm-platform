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
package org.camunda.bpm.engine.impl;

import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.management.ProcessDefinitionStatistics;
import org.camunda.bpm.engine.management.ProcessDefinitionStatisticsQuery;


public class ProcessDefinitionStatisticsQueryImpl extends AbstractQuery<ProcessDefinitionStatisticsQuery, ProcessDefinitionStatistics>
  implements ProcessDefinitionStatisticsQuery {

  protected static final long serialVersionUID = 1L;
  protected boolean includeFailedJobs = false;
  protected boolean includeIncidents = false;
  protected boolean includeRootIncidents = false;
  protected String includeIncidentsForType;

  public ProcessDefinitionStatisticsQueryImpl(CommandExecutor commandExecutor) {
    super(commandExecutor);
  }

  @Override
  public long executeCount(CommandContext commandContext) {
    checkQueryOk();
    return
      commandContext
        .getStatisticsManager()
        .getStatisticsCountGroupedByProcessDefinitionVersion(this);
  }

  @Override
  public List<ProcessDefinitionStatistics> executeList(CommandContext commandContext,
      Page page) {
    checkQueryOk();
    return
      commandContext
        .getStatisticsManager()
        .getStatisticsGroupedByProcessDefinitionVersion(this, page);
  }

  public ProcessDefinitionStatisticsQuery includeFailedJobs() {
    includeFailedJobs = true;
    return this;
  }

  public ProcessDefinitionStatisticsQuery includeIncidents() {
    includeIncidents = true;
    return this;
  }

  public ProcessDefinitionStatisticsQuery includeIncidentsForType(String incidentType) {
    this.includeIncidentsForType = incidentType;
    return this;
  }

  public boolean isFailedJobsToInclude() {
    return includeFailedJobs;
  }

  public boolean isIncidentsToInclude() {
    return includeIncidents || includeRootIncidents || includeIncidentsForType != null;
  }

  protected void checkQueryOk() {
    super.checkQueryOk();
    if (includeIncidents && includeIncidentsForType != null) {
      throw new ProcessEngineException("Invalid query: It is not possible to use includeIncident() and includeIncidentForType() to execute one query.");
    }
    if (includeRootIncidents && includeIncidentsForType != null) {
      throw new ProcessEngineException("Invalid query: It is not possible to use includeRootIncident() and includeIncidentForType() to execute one query.");
    }
    if (includeIncidents && includeRootIncidents) {
      throw new ProcessEngineException("Invalid query: It is not possible to use includeIncident() and includeRootIncidents() to execute one query.");
    }
  }

  @Override
  public ProcessDefinitionStatisticsQuery includeRootIncidents() {
    this.includeRootIncidents = true;
    return this;
  }

}
