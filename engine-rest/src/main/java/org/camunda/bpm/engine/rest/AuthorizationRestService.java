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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.authorization.AuthorizationCheckResultDto;

/**
 * @author Daniel Meyer
 *
 */
@Path(AuthorizationRestService.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface AuthorizationRestService {
  
  public static final String PATH = "/authorization";
  
  @GET
  @Path("/check")
  @Produces(MediaType.APPLICATION_JSON)
  public AuthorizationCheckResultDto isUserAuthorized(
      @QueryParam("permissionName") String permissionName,
      @QueryParam("permissionValue") Integer permissionValue,
      @QueryParam("resourceName") String resourceName,
      @QueryParam("resourceType") Integer resourceType,
      @QueryParam("resourceId") String resourceId);
  
}
