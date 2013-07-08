package org.camunda.bpm.tasklist.resources;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.spi.impl.AbstractProcessEngineAware;
import org.camunda.bpm.tasklist.AuthenticationFilter;
import org.camunda.bpm.tasklist.TasklistDemoData;
import org.camunda.bpm.tasklist.dto.AuthenticationResponseDto;
import org.camunda.bpm.tasklist.dto.LoginDto;

/**
 *
 * @author drobisch
 */
@Path("/auth")
public class AuthenticationResource extends AbstractProcessEngineAware {

  @Context
  private HttpServletRequest httpRequest;

  @POST
  @Path("login")
  @Produces(MediaType.APPLICATION_JSON)
  public AuthenticationResponseDto login(LoginDto loginDto) {

    // generate demo data if necessary
    new TasklistDemoData().createDemoData();

    String user = loginDto.getUsername();
    String password = loginDto.getPassword();

    boolean validLogin = processEngine.getIdentityService().checkPassword(user, password);
    if (validLogin) {
      httpRequest.getSession(true).setAttribute(AuthenticationFilter.AUTH_USER, user);
    }

    return new AuthenticationResponseDto(validLogin, user);
  }

  @GET
  @Path("logout")
  public String logout() {
    httpRequest.getSession(true).setAttribute(AuthenticationFilter.AUTH_USER, null);

    return "logged out";
  }

  @GET
  @Path("user")
  @Produces(MediaType.APPLICATION_JSON)
  public String getCurrentUser() {
    return (String) httpRequest.getSession().getAttribute(AuthenticationFilter.AUTH_USER);
  }

}
