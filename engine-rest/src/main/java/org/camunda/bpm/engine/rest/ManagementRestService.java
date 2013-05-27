package org.camunda.bpm.engine.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.DeleteEngineEntityDto;

@Path(ManagementRestService.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface ManagementRestService {

	public static final String PATH = "/management/";

	/**
	 * Sets the number of retries that a job has left.
	 * 
	 * @param id
	 * @param retries
	 * @return
	 */
	@POST
	@Path("job/{id}/retries/{retries}")
	void setJobRetries(@PathParam("id") String jobId,
			@PathParam("retries") int retries);

	/**
	 * Deletes all jobs belonging to a given process instance.
	 * 
	 * @param id
	 * @return
	 */
	@DELETE
	@Path("jobs/process-instance/{id}")
	void deleteJobs(@PathParam("id") String processInstanceId);

}
