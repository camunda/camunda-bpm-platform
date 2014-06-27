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
package org.camunda.bpm.engine.rest.sub.runtime;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.runtime.CaseExecutionDto;
import org.camunda.bpm.engine.rest.dto.runtime.CaseExecutionTriggerDto;
import org.camunda.bpm.engine.rest.sub.VariableResource;


/**
 *
 * @author Roman Smirnov
 *
 */
public interface CaseExecutionResource {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  CaseExecutionDto getCaseExecution();

  @POST
  @Path("/manual-start")
  @Consumes(MediaType.APPLICATION_JSON)
  void manualStart(CaseExecutionTriggerDto triggerDto);

  @POST
  @Path("/disable")
  @Consumes(MediaType.APPLICATION_JSON)
  void disable(CaseExecutionTriggerDto triggerDto);

  @POST
  @Path("/reenable")
  @Consumes(MediaType.APPLICATION_JSON)
  void reenable(CaseExecutionTriggerDto triggerDto);

  @POST
  @Path("/complete")
  @Consumes(MediaType.APPLICATION_JSON)
  void complete(CaseExecutionTriggerDto triggerDto);

  @Path("/localVariables")
  VariableResource getVariablesLocal();

  @Path("/variables")
  VariableResource getVariables();


}
