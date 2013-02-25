package org.camunda.bpm.tasklist.resources;

import org.camunda.bpm.tasklist.TasklistProcessEngineProvider;
import org.camunda.bpm.tasklist.dto.AuthenticationResponse;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * @author : drobisch
 */

@Path("/auth")
public class AuthenticationResource {

  @Context
  private HttpServletRequest httpRequest;

  @GET
  @Path("login/{user}/{password}")
  @Produces(MediaType.APPLICATION_JSON)
  public AuthenticationResponse login(@PathParam("user") String userId, @PathParam("password") String password) {
    boolean validLogin = TasklistProcessEngineProvider.getStaticEngine().getIdentityService().checkPassword(userId, password);
    if (validLogin) {
      httpRequest.getSession(true).setAttribute("authenticatedUser", userId);
    }
    return new AuthenticationResponse(validLogin, userId);
  }

  @GET
  @Path("user")
  @Produces(MediaType.APPLICATION_JSON)
  public String getCurrentUser() {
    return (String) httpRequest.getSession().getAttribute("authenticatedUser");
  }

}
