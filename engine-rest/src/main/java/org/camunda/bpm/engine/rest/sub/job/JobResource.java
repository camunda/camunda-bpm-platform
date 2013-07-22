package org.camunda.bpm.engine.rest.sub.job;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.runtime.JobDto;
import org.camunda.bpm.engine.rest.dto.runtime.JobRetriesDto;

public interface JobResource {
	
	  @GET
	  @Produces(MediaType.APPLICATION_JSON)
	  JobDto getJob();
		  
	  @PUT
	  @Path("/retries")
	  @Consumes(MediaType.APPLICATION_JSON)
	  void setJobRetries(JobRetriesDto dto);
	  
	  @POST
	  @Path("/execute")	
	  void executeJob();
}
