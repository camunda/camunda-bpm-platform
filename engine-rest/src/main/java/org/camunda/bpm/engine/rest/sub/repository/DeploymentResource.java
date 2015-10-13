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
package org.camunda.bpm.engine.rest.sub.repository;

import org.camunda.bpm.engine.rest.dto.repository.DeploymentDto;
import org.camunda.bpm.engine.rest.dto.repository.RedeploymentDto;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

public interface DeploymentResource {

  public static final String CASCADE = "cascade";

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  DeploymentDto getDeployment();

  @Path("/resources")
  DeploymentResourcesResource getDeploymentResources();

  @POST
  @Path("/redeploy")
  @Produces(MediaType.APPLICATION_JSON)
  DeploymentDto redeploy(@Context UriInfo uriInfo, RedeploymentDto redeployment);

  @DELETE
  void deleteDeployment(@PathParam("id") String deploymentId, @Context UriInfo uriInfo);
}
