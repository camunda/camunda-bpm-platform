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
package org.camunda.bpm.engine.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.ProcessEngineDto;

@Path(ProcessEngineRestService.PATH)
public interface ProcessEngineRestService {
  
  public static final String PATH = "/engine";

  @Path("/{name}" + ProcessDefinitionRestService.PATH)
  ProcessDefinitionRestService getProcessDefinitionService(@PathParam("name") String engineName);
  
  @Path("/{name}" + ProcessInstanceRestService.PATH)
  ProcessInstanceRestService getProcessInstanceService(@PathParam("name") String engineName);
  
  @Path("/{name}" + ExecutionRestService.PATH)
  ExecutionRestService getExecutionService(@PathParam("name") String engineName);
  
  @Path("/{name}" + TaskRestService.PATH)
  TaskRestService getTaskRestService(@PathParam("name") String engineName);
  
  @Path("/{name}" + IdentityRestService.PATH)
  IdentityRestService getIdentityRestService(@PathParam("name") String engineName);
  
  @Path("/{name}" + MessageRestService.PATH)
  MessageRestService getMessageRestService(@PathParam("name") String engineName);

  @Path("/{name}" + JobRestService.PATH)
  JobRestService getJobRestService(@PathParam("name") String engineName);
  
  @Path("/{name}" + VariableInstanceRestService.PATH)
  VariableInstanceRestService getVariableInstanceService(@PathParam("name") String engineName);
  
  @Path("/{name}" + GroupRestService.PATH)
  GroupRestService getGroupRestService(@PathParam("name") String engineName);
  
  @Path("/{name}" + UserRestService.PATH)
  UserRestService getUserRestService(@PathParam("name") String engineName);

  @Path("/{name}" + AuthorizationRestService.PATH)
  AuthorizationRestService getAuthorizationRestService(@PathParam("name") String engineName);

  @Path("/{name}" + HistoryRestService.PATH)
  HistoryRestService getHistoryRestService(@PathParam("name") String engineName);
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<ProcessEngineDto> getProcessEngineNames();
}
