package com.camunda.fox.cycle.web.service;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;


/**
 *
 * @author nico.rehwaldt
 */
@Path("login")
public class LoginService extends AbstractRestService {
  
//  @GET
//  @Consumes(MediaType.APPLICATION_JSON)
//  @Path("login")
//  public String loginJSON() {
//    throw new WebApplicationException(Response.Status.UNAUTHORIZED);
//  }
  
  @GET
  @Produces(MediaType.TEXT_HTML)
  public Object login(@Context HttpServletRequest request) {

    // We do not want to allow direct access to login. 
    // That is why we forward the request to a location which requires a logged in user

    return "tpl:login";
  }

  @POST
  @Path("expired")
  @Produces(MediaType.TEXT_HTML)
  public String loginExpired() {
    return "tpl:error/login-expired";
  }

  @GET
  @Path("error")
  @Produces(MediaType.TEXT_HTML)
  public String loginError() {
    return "tpl:error/invalid-login-data";
  }
  
  @GET
  @Path("error/license")
  @Produces(MediaType.TEXT_HTML)
  public String loginErrorLicense() {
    return "tpl:error/license-error";
  }
  
  @GET
  @Path("error/license/notfound")
  @Produces(MediaType.TEXT_HTML)
  public String loginErrorLicenseNotFound() {
    return "tpl:error/license-not-found";
  }

  @GET
  @Path("logout")
  public String logout(@Context HttpServletRequest request) {
    // destroys the session for this user.
    request.getSession().invalidate();

    return "tpl:logout";
  }

  /**
   * Is the given request the result of a forward?
   *
   * Note to self: Request and session attributes during login forward
   *
   * ==== request attributes ==== 
   * javax.servlet.forward.request_uri: /cycle-spike/app/secured/view/index
   * javax.servlet.forward.context_path: /cycle-spike javax.servlet.forward.servlet_path: /app
   * javax.servlet.forward.path_info: /secured/view/index org.jboss.resteasy.core.ResourceMethod:
   * org.jboss.resteasy.core.ResourceMethod@1e4651bf 
   * ==== session attributes ====
   *
   * @param request
   * @return
   */
  private boolean isForward(HttpServletRequest request) {
    return request.getAttribute("javax.servlet.forward.request_uri") != null;
  }
}
