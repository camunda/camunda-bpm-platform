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

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Providers;

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
import org.camunda.bpm.engine.rest.impl.history.HistoryRestServiceImpl;
import org.camunda.bpm.engine.rest.impl.optimize.OptimizeRestService;
import org.camunda.bpm.engine.rest.util.ProvidersUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>Abstract process engine resource that provides instantiations of all REST resources.</p>
 *
 * <p>Subclasses can add JAX-RS to methods as required annotations. For example, if only
 * the process definition resource should be exposed, it is sufficient to add JAX-RS annotations to that
 * resource. The <code>engineName</code> parameter of all the provided methods may be <code>null</code>
 * to instantiate a resource for the default engine.</p>
 *
 * @author Thorben Lindhauer
 */
public abstract class AbstractProcessEngineRestServiceImpl {

  @Context
  protected Providers providers;

  public ProcessDefinitionRestService getProcessDefinitionService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    ProcessDefinitionRestServiceImpl subResource = new ProcessDefinitionRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public ProcessInstanceRestService getProcessInstanceService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    ProcessInstanceRestServiceImpl subResource = new ProcessInstanceRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public ExecutionRestService getExecutionService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    ExecutionRestServiceImpl subResource = new ExecutionRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public TaskRestService getTaskRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    TaskRestServiceImpl subResource = new TaskRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);

    return subResource;
  }

  public IdentityRestService getIdentityRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    IdentityRestServiceImpl subResource = new IdentityRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public MessageRestService getMessageRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    MessageRestServiceImpl subResource = new MessageRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public VariableInstanceRestService getVariableInstanceService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    VariableInstanceRestServiceImpl subResource = new VariableInstanceRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public JobDefinitionRestService getJobDefinitionRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    JobDefinitionRestServiceImpl subResource = new JobDefinitionRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public JobRestService getJobRestService(String engineName) {
  	String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
  	JobRestServiceImpl subResource = new JobRestServiceImpl(engineName, getObjectMapper());
  	subResource.setRelativeRootResourceUri(rootResourcePath);
  	return subResource;
  }

  public GroupRestService getGroupRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    GroupRestServiceImpl subResource = new GroupRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public UserRestService getUserRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    UserRestServiceImpl subResource = new UserRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public AuthorizationRestService getAuthorizationRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    AuthorizationRestServiceImpl subResource = new AuthorizationRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public IncidentRestService getIncidentService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    IncidentRestServiceImpl subResource = new IncidentRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public HistoryRestService getHistoryRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    HistoryRestServiceImpl subResource = new HistoryRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public DeploymentRestService getDeploymentRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    DeploymentRestServiceImpl subResource = new DeploymentRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public CaseDefinitionRestService getCaseDefinitionRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    CaseDefinitionRestServiceImpl subResource = new CaseDefinitionRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public CaseInstanceRestService getCaseInstanceRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    CaseInstanceRestServiceImpl subResource = new CaseInstanceRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public CaseExecutionRestService getCaseExecutionRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    CaseExecutionRestServiceImpl subResource = new CaseExecutionRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public FilterRestService getFilterRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    FilterRestServiceImpl subResource = new FilterRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public MetricsRestService getMetricsRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    MetricsRestServiceImpl subResource = new MetricsRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public DecisionDefinitionRestService getDecisionDefinitionRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    DecisionDefinitionRestServiceImpl subResource = new DecisionDefinitionRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public DecisionRequirementsDefinitionRestService getDecisionRequirementsDefinitionRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    DecisionRequirementsDefinitionRestServiceImpl subResource = new DecisionRequirementsDefinitionRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public ExternalTaskRestService getExternalTaskRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    ExternalTaskRestServiceImpl subResource = new ExternalTaskRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public MigrationRestService getMigrationRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    MigrationRestServiceImpl subResource = new MigrationRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public ModificationRestService getModificationRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    ModificationRestServiceImpl subResource = new ModificationRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public BatchRestService getBatchRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    BatchRestServiceImpl subResource = new BatchRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public TenantRestService getTenantRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    TenantRestServiceImpl subResource = new TenantRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public SignalRestService getSignalRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    SignalRestServiceImpl subResource = new SignalRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public ConditionRestService getConditionRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    ConditionRestServiceImpl subResource = new ConditionRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public OptimizeRestService getOptimizeRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    OptimizeRestService subResource = new OptimizeRestService(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public VersionRestService getVersionRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    VersionRestService subResource = new VersionRestService(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public SchemaLogRestService getSchemaLogRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    SchemaLogRestServiceImpl subResource = new SchemaLogRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public EventSubscriptionRestService getEventSubscriptionRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    EventSubscriptionRestServiceImpl subResource = new EventSubscriptionRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  public TelemetryRestService getTelemetryRestService(String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    TelemetryRestServiceImpl subResource = new TelemetryRestServiceImpl(engineName, getObjectMapper());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  protected abstract URI getRelativeEngineUri(String engineName);

  protected ObjectMapper getObjectMapper() {
    return ProvidersUtil
        .resolveFromContext(providers, ObjectMapper.class, MediaType.APPLICATION_JSON_TYPE, this.getClass());
  }

}
