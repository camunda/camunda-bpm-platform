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
package org.camunda.bpm.engine.rest.impl;

import java.net.URI;

import javax.ws.rs.Path;

import org.camunda.bpm.engine.rest.AuthorizationRestService;
import org.camunda.bpm.engine.rest.BatchRestService;
import org.camunda.bpm.engine.rest.CaseDefinitionRestService;
import org.camunda.bpm.engine.rest.CaseExecutionRestService;
import org.camunda.bpm.engine.rest.CaseInstanceRestService;
import org.camunda.bpm.engine.rest.ConditionRestService;
import org.camunda.bpm.engine.rest.DecisionDefinitionRestService;
import org.camunda.bpm.engine.rest.DecisionRequirementsDefinitionRestService;
import org.camunda.bpm.engine.rest.DeploymentRestService;
import org.camunda.bpm.engine.rest.EventSubscriptionRestService;
import org.camunda.bpm.engine.rest.ExecutionRestService;
import org.camunda.bpm.engine.rest.ExternalTaskRestService;
import org.camunda.bpm.engine.rest.FilterRestService;
import org.camunda.bpm.engine.rest.GroupRestService;
import org.camunda.bpm.engine.rest.IdentityRestService;
import org.camunda.bpm.engine.rest.IncidentRestService;
import org.camunda.bpm.engine.rest.JobDefinitionRestService;
import org.camunda.bpm.engine.rest.JobRestService;
import org.camunda.bpm.engine.rest.MessageRestService;
import org.camunda.bpm.engine.rest.MetricsRestService;
import org.camunda.bpm.engine.rest.MigrationRestService;
import org.camunda.bpm.engine.rest.ModificationRestService;
import org.camunda.bpm.engine.rest.ProcessDefinitionRestService;
import org.camunda.bpm.engine.rest.ProcessInstanceRestService;
import org.camunda.bpm.engine.rest.SchemaLogRestService;
import org.camunda.bpm.engine.rest.SignalRestService;
import org.camunda.bpm.engine.rest.TaskRestService;
import org.camunda.bpm.engine.rest.TelemetryRestService;
import org.camunda.bpm.engine.rest.TenantRestService;
import org.camunda.bpm.engine.rest.UserRestService;
import org.camunda.bpm.engine.rest.VariableInstanceRestService;
import org.camunda.bpm.engine.rest.history.HistoryRestService;
import org.camunda.bpm.engine.rest.impl.optimize.OptimizeRestService;

@Path(DefaultProcessEngineRestServiceImpl.PATH)
public class DefaultProcessEngineRestServiceImpl extends AbstractProcessEngineRestServiceImpl {

  public static final String PATH = "";

  @Path(ProcessDefinitionRestService.PATH)
  public ProcessDefinitionRestService getProcessDefinitionService() {
    return super.getProcessDefinitionService(null);
  }

  @Path(ProcessInstanceRestService.PATH)
  public ProcessInstanceRestService getProcessInstanceService() {
    return super.getProcessInstanceService(null);
  }

  @Path(ExecutionRestService.PATH)
  public ExecutionRestService getExecutionService() {
    return super.getExecutionService(null);
  }

  @Path(TaskRestService.PATH)
  public TaskRestService getTaskRestService() {
    return super.getTaskRestService(null);
  }

  @Path(IdentityRestService.PATH)
  public IdentityRestService getIdentityRestService() {
    return super.getIdentityRestService(null);
  }

  @Path(MessageRestService.PATH)
  public MessageRestService getMessageRestService() {
    return super.getMessageRestService(null);
  }

  @Path(VariableInstanceRestService.PATH)
  public VariableInstanceRestService getVariableInstanceService() {
    return super.getVariableInstanceService(null);
  }

  @Path(JobDefinitionRestService.PATH)
  public JobDefinitionRestService getJobDefinitionRestService() {
    return super.getJobDefinitionRestService(null);
  }

  @Path(JobRestService.PATH)
  public JobRestService getJobRestService() {
    return super.getJobRestService(null);
  }

  @Path(GroupRestService.PATH)
  public GroupRestService getGroupRestService() {
    return super.getGroupRestService(null);
  }

  @Path(UserRestService.PATH)
  public UserRestService getUserRestService() {
    return super.getUserRestService(null);
  }

  @Path(AuthorizationRestService.PATH)
  public AuthorizationRestService getAuthorizationRestService() {
    return super.getAuthorizationRestService(null);
  }

  @Path(IncidentRestService.PATH)
  public IncidentRestService getIncidentService() {
    return super.getIncidentService(null);
  }

  @Path(HistoryRestService.PATH)
  public HistoryRestService getHistoryRestService() {
    return super.getHistoryRestService(null);
  }

  @Path(DeploymentRestService.PATH)
  public DeploymentRestService getDeploymentRestService() {
    return super.getDeploymentRestService(null);
  }

  @Path(CaseDefinitionRestService.PATH)
  public CaseDefinitionRestService getCaseDefinitionRestService() {
    return super.getCaseDefinitionRestService(null);
  }

  @Path(CaseInstanceRestService.PATH)
  public CaseInstanceRestService getCaseInstanceRestService() {
    return super.getCaseInstanceRestService(null);
  }

  @Path(CaseExecutionRestService.PATH)
  public CaseExecutionRestService getCaseExecutionRestService() {
    return super.getCaseExecutionRestService(null);
  }

  @Path(FilterRestService.PATH)
  public FilterRestService getFilterRestService() {
    return super.getFilterRestService(null);
  }

  @Path(MetricsRestService.PATH)
  public MetricsRestService getMetricsRestService() {
    return super.getMetricsRestService(null);
  }

  @Path(DecisionDefinitionRestService.PATH)
  public DecisionDefinitionRestService getDecisionDefinitionRestService() {
    return super.getDecisionDefinitionRestService(null);
  }

  @Path(DecisionRequirementsDefinitionRestService.PATH)
  public DecisionRequirementsDefinitionRestService getDecisionRequirementsDefinitionRestService() {
    return super.getDecisionRequirementsDefinitionRestService(null);
  }

  @Path(ExternalTaskRestService.PATH)
  public ExternalTaskRestService getExternalTaskRestService() {
    return super.getExternalTaskRestService(null);
  }

  @Path(MigrationRestService.PATH)
  public MigrationRestService getMigrationRestService() {
    return super.getMigrationRestService(null);
  }

  @Path(ModificationRestService.PATH)
  public ModificationRestService getModificationRestService() {
    return super.getModificationRestService(null);
  }

  @Path(BatchRestService.PATH)
  public BatchRestService getBatchRestService() {
    return super.getBatchRestService(null);
  }

  @Path(TenantRestService.PATH)
  public TenantRestService getTenantRestService() {
    return super.getTenantRestService(null);
  }

  @Path(SignalRestService.PATH)
  public SignalRestService getSignalRestService() {
    return super.getSignalRestService(null);
  }

  @Path(ConditionRestService.PATH)
  public ConditionRestService getConditionRestService() {
    return super.getConditionRestService(null);
  }

  @Path(OptimizeRestService.PATH)
  public OptimizeRestService getOptimizeRestService() {
    return super.getOptimizeRestService(null);
  }

  @Path(VersionRestService.PATH)
  public VersionRestService getVersionRestService() {
    return super.getVersionRestService(null);
  }

  @Path(SchemaLogRestService.PATH)
  public SchemaLogRestService getSchemaLogRestService() {
    return super.getSchemaLogRestService(null);
  }

  @Path(EventSubscriptionRestService.PATH)
  public EventSubscriptionRestService getEventSubscriptionRestService() {
    return super.getEventSubscriptionRestService(null);
  }

  @Path(TelemetryRestService.PATH)
  public TelemetryRestService getTelemetryRestService() {
    return super.getTelemetryRestService(null);
  }

  @Override
  protected URI getRelativeEngineUri(String engineName) {
    // the default engine
    return URI.create("/");
  }
}
