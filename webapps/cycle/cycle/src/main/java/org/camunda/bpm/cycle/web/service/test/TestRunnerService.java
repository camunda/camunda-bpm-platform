package org.camunda.bpm.cycle.web.service.test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.cycle.web.service.AbstractRestService;



/**
 * This is the test controller which provides an interface to run angular end-to-end tests
 * 
 * @author nico.rehwaldt
 */
@Path("test")
public class TestRunnerService extends AbstractRestService {
  
  @GET
  @Path("runner")
  @Produces(MediaType.TEXT_HTML)
  public Object runner() {
    return "test/runner";
  }
}
