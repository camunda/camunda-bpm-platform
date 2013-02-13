package com.camunda.fox.cycle.web.service;


import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


/**
 * This controller exposes error pages
 * 
 * @author nico.rehwaldt
 */
@Path("error")
public class ErrorViewService extends AbstractRestService {
  
  @GET
  @Path("{name:[\\S]+}")
  @Produces(MediaType.TEXT_HTML)
  public String template(@PathParam("name") String name) {
    if (name.endsWith(".html")) {
      name = name.substring(0, name.lastIndexOf(".html"));
    }
    return "tpl:error/" + name;
  }
}
