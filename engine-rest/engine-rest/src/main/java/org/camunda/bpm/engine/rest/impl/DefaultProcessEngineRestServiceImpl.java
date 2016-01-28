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
package org.camunda.bpm.engine.rest.impl;

import org.camunda.bpm.engine.rest.*;
import org.camunda.bpm.engine.rest.history.HistoryRestService;

import javax.ws.rs.Path;
import java.net.URI;

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

  @Path(ExternalTaskRestService.PATH)
  public ExternalTaskRestService getExternalTaskRestService() {
    return super.getExternalTaskRestService(null);
  }

  @Path(MigrationRestService.PATH)
  public MigrationRestService getMigrationRestService() {
    return super.getMigrationRestService(null);
  }

  @Override
  protected URI getRelativeEngineUri(String engineName) {
    // the default engine
    return URI.create("/");
  }
}
