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
package org.camunda.bpm.cockpit.impl.plugin.base.sub.resources;

import static org.camunda.bpm.engine.authorization.Permissions.READ;
import static org.camunda.bpm.engine.authorization.Permissions.READ_INSTANCE;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_DEFINITION;
import static org.camunda.bpm.engine.authorization.Resources.PROCESS_INSTANCE;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.cockpit.impl.plugin.base.dto.CalledProcessInstanceDto;
import org.camunda.bpm.cockpit.impl.plugin.base.dto.query.CalledProcessInstanceQueryDto;
import org.camunda.bpm.cockpit.plugin.resource.AbstractPluginResource;

public class ProcessInstanceResource extends AbstractPluginResource {

  protected String id;

  public ProcessInstanceResource(String engineName, String id) {
    super(engineName);
    this.id = id;
  }

  @GET
  @Path("/called-process-instances")
  @Produces(MediaType.APPLICATION_JSON)
  public List<CalledProcessInstanceDto> getCalledProcessInstances(@Context UriInfo uriInfo) {
    CalledProcessInstanceQueryDto queryParameter = new CalledProcessInstanceQueryDto(uriInfo.getQueryParameters());
    return queryCalledProcessInstances(queryParameter);
  }

  @POST
  @Path("/called-process-instances")
  @Produces(MediaType.APPLICATION_JSON)
  public List<CalledProcessInstanceDto> queryCalledProcessInstances(CalledProcessInstanceQueryDto queryParameter) {
    queryParameter.setParentProcessInstanceId(id);
    configureExecutionQuery(queryParameter);
    queryParameter.disableMaxResultsLimit();
    return getQueryService().executeQuery("selectCalledProcessInstances", queryParameter);
  }

  protected void configureExecutionQuery(CalledProcessInstanceQueryDto query) {
    configureAuthorizationCheck(query);
    configureTenantCheck(query);
    addPermissionCheck(query, PROCESS_INSTANCE, "EXEC1.PROC_INST_ID_", READ);
    addPermissionCheck(query, PROCESS_DEFINITION, "PROCDEF.KEY_", READ_INSTANCE);
  }

}
