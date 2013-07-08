package org.camunda.bpm.engine.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.engine.rest.dto.job.JobDto;
import org.camunda.bpm.engine.rest.dto.job.JobQueryDto;
import org.camunda.bpm.engine.rest.sub.job.JobResource;

@Path(JobRestService.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface JobRestService {

	static final String PATH = "/job";
	
	 
	  @Path("/{id}")
	  JobResource getJob(@PathParam("id") String jobId);
	  
	  @GET
	  @Produces(MediaType.APPLICATION_JSON)
	  List<JobDto> getJobs(@Context UriInfo uriInfo,
	      @QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults);


	  @POST
	  @Consumes(MediaType.APPLICATION_JSON)
	  @Produces(MediaType.APPLICATION_JSON)
	  List<JobDto> queryJobs(JobQueryDto queryDto,
	      @QueryParam("firstResult") Integer firstResult, @QueryParam("maxResults") Integer maxResults);

}
