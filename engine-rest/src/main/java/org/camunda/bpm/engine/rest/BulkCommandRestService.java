package org.camunda.bpm.engine.rest;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(BulkCommandRestService.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface BulkCommandRestService {

	  public static final String PATH = "/command";
	
}
