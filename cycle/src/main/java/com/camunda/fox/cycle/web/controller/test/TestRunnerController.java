package com.camunda.fox.cycle.web.controller.test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.camunda.fox.cycle.web.controller.AbstractController;


/**
 * This is the test controller which provides an interface to run angular end-to-end tests
 * 
 * @author nico.rehwaldt
 */
@Path("test")
public class TestRunnerController extends AbstractController {
  

  @GET
  @Path("runner")
  @Produces(MediaType.TEXT_HTML)
  public Object runner() {
    return "test/runner";
  }
}
