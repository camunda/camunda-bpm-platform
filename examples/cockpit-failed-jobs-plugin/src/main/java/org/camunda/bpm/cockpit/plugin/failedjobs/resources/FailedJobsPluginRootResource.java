package org.camunda.bpm.cockpit.plugin.failedjobs.resources;

import javax.ws.rs.Path;
//import javax.ws.rs.PathParam;

import org.camunda.bpm.cockpit.plugin.resource.AbstractPluginRootResource;
import org.camunda.bpm.cockpit.plugin.failedjobs.FailedJobsPlugin;

@Path("plugin/" + FailedJobsPlugin.ID)
public class FailedJobsPluginRootResource extends AbstractPluginRootResource {

	public FailedJobsPluginRootResource() {
		super(FailedJobsPlugin.ID);
	}

	/*@Path("{engineName}/process-instance")
	public ProcessInstanceResource getProcessInstanceResource(
			@PathParam("engineName") String engineName) {
		return subResource(new ProcessInstanceResource(engineName), engineName);
	}*/
}
