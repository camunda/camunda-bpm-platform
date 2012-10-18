package com.camunda.fox.cycle.web.service;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import com.camunda.fox.cycle.web.dto.CurrentUserDTO;


/**
 * This is the default controller which handles user authentication / authorization.
 * It is invoked via JAAS according to the definitions in the <code>web.xml</code>. 
 * 
 * Note that it offers both means to treat login requests via application/json and application/html.
 * That is needed in order to treat session timeouts in the client application. 
 * 
 * Rather than rendering the presented views directly, the task is delegated to a 
 * {@link com.camunda.fox.cycle.web.jaxrs.ext.TemplateMessageBodyWriter}. 
 * 
 * @author nico.rehwaldt
 */
@Path("/")
public class DefaultService extends AbstractRestService {
  
  private static final String ADMIN_ROLE = "admin";
  
//  @GET
//  @Consumes(MediaType.APPLICATION_JSON)
//  @Path("login")
//  public String loginJSON() {
//    throw new WebApplicationException(Response.Status.UNAUTHORIZED);
//  }
  
  @GET
  @Path("login")
  @Produces(MediaType.TEXT_HTML)
  public Object login(@Context UriInfo info, @Context HttpServletRequest request) {
   
    // We do not want to allow direct access to /login. 
    // That is why we forward the request to a location which requires a logged in user
    if (isForward(request)) {
      return "login";
    } else {
      return redirectTo("secured/view/index/");
    }
  }
  
  @POST
  @Path("login/expired")
  @Produces(MediaType.TEXT_HTML)
  public String loginExpired() {
    return "error/login-expired";
  }
  
  @POST
  @Path("login/error")
  @Produces(MediaType.TEXT_HTML)
  public String loginError() {
    return "error/invalid-login-data";
  }
  
  @GET
  @Path("currentUser")
  @Produces(MediaType.APPLICATION_JSON)
  public CurrentUserDTO currentUser(@Context HttpServletRequest request) {
    Principal principal = request.getUserPrincipal();
    boolean isAdminRole = request.isUserInRole(ADMIN_ROLE);
    if (principal != null) {
      return new CurrentUserDTO(-1, principal.getName(), isAdminRole);
    } else {
      return null;
    }
  }
  
  @GET
  @Path("logout")
  public String logout(@Context HttpServletRequest request) {
    // destroys the session for this user.
		request.getSession().invalidate();
    
    return "logout";
  }

  /**
   * Is the given request the result of a forward?
   * 
   * Note to self: Request and session attributes during login forward
   * 
   * ==== request attributes ====
   * javax.servlet.forward.request_uri: /cycle-spike/app/secured/view/index
   * javax.servlet.forward.context_path: /cycle-spike
   * javax.servlet.forward.servlet_path: /app
   * javax.servlet.forward.path_info: /secured/view/index
   * org.jboss.resteasy.core.ResourceMethod: org.jboss.resteasy.core.ResourceMethod@1e4651bf
   * ==== session attributes ====
   * 
   * @param request
   * @return 
   */
  private boolean isForward(HttpServletRequest request) {
    return request.getAttribute("javax.servlet.forward.request_uri") != null;
  }
}
