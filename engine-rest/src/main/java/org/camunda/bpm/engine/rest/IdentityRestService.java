package org.camunda.bpm.engine.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.task.GroupInfoDto;

@Path(IdentityRestService.PATH)
@Produces(MediaType.APPLICATION_JSON)
public interface IdentityRestService {

  public static final String PATH = "/identity";
  
  @GET
  @Path("/groups")
  GroupInfoDto getGroupInfo(@QueryParam("userId") String userId);
}
