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
package org.camunda.bpm.engine.rest.sub.runtime;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.SuspensionStateDto;
import org.camunda.bpm.engine.rest.dto.batch.BatchDto;
import org.camunda.bpm.engine.rest.dto.runtime.ActivityInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.modification.ProcessInstanceModificationDto;
import org.camunda.bpm.engine.rest.sub.VariableResource;

public interface ProcessInstanceResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  ProcessInstanceDto getProcessInstance();

  @DELETE
  void deleteProcessInstance(@QueryParam("skipCustomListeners") @DefaultValue("false") boolean skipCustomListeners,
      @QueryParam("skipIoMappings") @DefaultValue("false") boolean skipIoMappings,
      @QueryParam("skipSubprocesses") @DefaultValue("false") boolean skipSubprocesses, 
      @QueryParam("failIfNotExists") @DefaultValue("true") boolean failIfNotExists);

  @Path("/variables")
  VariableResource getVariablesResource();

  @GET
  @Path("/activity-instances")
  @Produces(MediaType.APPLICATION_JSON)
  ActivityInstanceDto getActivityInstanceTree();

  @PUT
  @Path("/suspended")
  @Consumes(MediaType.APPLICATION_JSON)
  void updateSuspensionState(SuspensionStateDto dto);

  @POST
  @Path("/modification")
  @Consumes(MediaType.APPLICATION_JSON)
  void modifyProcessInstance(ProcessInstanceModificationDto dto);

  @POST
  @Path("/modification-async")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  BatchDto modifyProcessInstanceAsync(ProcessInstanceModificationDto dto);
}
