package com.camunda.fox.cycle.web.service;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


/**
 * This controller exposes the views offered by the application 
 * under <code>app/secured/view</code>. 
 * 
 * @author nico.rehwaldt
 */
@Path("secured/view")
public class ViewService extends AbstractRestService {
  
  @GET
  @Path("{name:[\\S]+}")
  @Produces(MediaType.TEXT_HTML)
  public String template(@PathParam("name") String name) {
    if (name.endsWith(".html")) {
      name = name.substring(0, name.lastIndexOf(".html"));
    }
    return "tpl:app/" + name;
  }
}
