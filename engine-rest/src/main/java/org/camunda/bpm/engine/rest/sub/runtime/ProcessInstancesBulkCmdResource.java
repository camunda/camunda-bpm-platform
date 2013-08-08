package org.camunda.bpm.engine.rest.sub.runtime;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.BulkCommandDto;
import org.camunda.bpm.engine.rest.dto.BulkCommandExceptionDto;

public interface ProcessInstancesBulkCmdResource {

	  @PUT
	  @Path("/suspended")
	  @Consumes(MediaType.APPLICATION_JSON)
	  @Produces(MediaType.APPLICATION_JSON)
	  List<BulkCommandExceptionDto> updateSuspensionStates(BulkCommandDto commandDto);
	  
	  @DELETE
	  @Path("/delete")
	  @Consumes(MediaType.APPLICATION_JSON)
	  @Produces(MediaType.APPLICATION_JSON)
	  List<BulkCommandExceptionDto> deleteProcessInstances(BulkCommandDto commandDto);	
}
