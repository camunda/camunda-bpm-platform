package org.camunda.bpm.engine.rest;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(HistoryRestService.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface HistoryRestService {

	public static final String PATH = "/history"; 	 

	  @Path(HistoricProcessInstanceRestService.PATH)
	  HistoricProcessInstanceRestService getProcessInstanceService();
	 
	  @Path(HistoricActivityInstanceRestService.PATH)
	  HistoricActivityInstanceRestService getActivityInstanceService();
	  
	  @Path(HistoricVariableInstanceRestService.PATH)
	  HistoricVariableInstanceRestService getVariableInstanceService();	
}
