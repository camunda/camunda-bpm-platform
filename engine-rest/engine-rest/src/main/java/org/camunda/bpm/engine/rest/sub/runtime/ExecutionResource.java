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

import org.camunda.bpm.engine.rest.dto.CreateIncidentDto;
import org.camunda.bpm.engine.rest.dto.runtime.ExecutionDto;
import org.camunda.bpm.engine.rest.dto.runtime.ExecutionTriggerDto;
import org.camunda.bpm.engine.rest.dto.runtime.IncidentDto;
import org.camunda.bpm.engine.rest.sub.VariableResource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

public interface ExecutionResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  ExecutionDto getExecution();
  
  @POST
  @Path("/signal")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  void signalExecution(ExecutionTriggerDto triggerDto);
  
  @Path("/localVariables")
  VariableResource getLocalVariables();
  
  @Path("/messageSubscriptions/{messageName}")
  EventSubscriptionResource getMessageEventSubscription(@PathParam("messageName") String messageName);

  @POST
  @Path("/create-incident")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  IncidentDto createIncident(CreateIncidentDto createIncidentDto);
}
