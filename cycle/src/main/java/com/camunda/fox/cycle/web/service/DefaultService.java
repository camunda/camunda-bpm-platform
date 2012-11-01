package com.camunda.fox.cycle.web.service;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.camunda.fox.cycle.repository.UserRepository;
import com.camunda.fox.cycle.web.dto.UserDTO;


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
  
  @Inject
  private UserRepository userRepository;
  
//  @GET
//  @Consumes(MediaType.APPLICATION_JSON)
//  @Path("login")
//  public String loginJSON() {
//    throw new WebApplicationException(Response.Status.UNAUTHORIZED);
//  }

  
  @GET
  @Path("currentUser")
  @Produces(MediaType.APPLICATION_JSON)
  public UserDTO currentUser(@Context HttpServletRequest request) {
    return new UserDTO(userRepository.findAll().get(0));
    
//    Principal principal = request.getUserPrincipal();
//    boolean isAdminRole = request.isUserInRole(ADMIN_ROLE);
//    if (principal != null) {
//      return new CurrentUserDTO(-1, principal.getName(), isAdminRole);
//    } else {
//      return null;
//    }
  }
}
