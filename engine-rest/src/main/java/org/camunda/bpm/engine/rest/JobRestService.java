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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.rest.dto.CountResultDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobQueryDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobSuspensionStateDto;
import org.camunda.bpm.engine.rest.sub.runtime.JobResource;

@Path(JobRestService.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface JobRestService {

	static final String PATH = "/job";

  @Path("/{id}")
  JobResource getJob(@PathParam("id") String jobId);

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  List<JobDto> getJobs(@Context UriInfo uriInfo,
      @QueryParam("firstResult") Integer firstResult,
      @QueryParam("maxResults") Integer maxResults);


  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  List<JobDto> queryJobs(JobQueryDto queryDto,
      @QueryParam("firstResult") Integer firstResult,
      @QueryParam("maxResults") Integer maxResults);

  @GET
  @Path("/count")
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto getJobsCount(@Context UriInfo uriInfo);

  @POST
  @Path("/count")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  CountResultDto queryJobsCount(JobQueryDto queryDto);

  @PUT
  @Path("/suspended")
  @Consumes(MediaType.APPLICATION_JSON)
  void updateSuspensionState(JobSuspensionStateDto dto);
}
