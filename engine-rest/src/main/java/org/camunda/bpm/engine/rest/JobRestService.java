package org.camunda.bpm.engine.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(JobRestService.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface JobRestService {

	public static final String PATH = "/management/job";

	/**
	 * Sets the number of retries that a job has left.
	 * 
	 * @param id
	 * @param retries
	 * @return
	 */
	@POST
	@Path("/{id}/retries/{retries}")
	void setJobRetries(@PathParam("id") String jobId,
			@PathParam("retries") int retries);

}
