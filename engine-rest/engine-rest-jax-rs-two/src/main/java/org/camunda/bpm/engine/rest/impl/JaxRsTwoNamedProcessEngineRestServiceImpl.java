package org.camunda.bpm.engine.rest.impl;

import org.camunda.bpm.engine.rest.impl.fetchAndLock.FetchAndLockRestService;
import org.camunda.bpm.engine.rest.impl.fetchAndLock.FetchAndLockRestServiceImpl;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Providers;
import java.net.URI;

/**
 * @author Tassilo Weidner
 */
@Path(JaxRsTwoNamedProcessEngineRestServiceImpl.PATH)
public class JaxRsTwoNamedProcessEngineRestServiceImpl extends JaxRsTwoAbstractProcessEngineRestServiceImpl {

  static final String PATH = "/engine";

  @Context
  protected Providers providers;

  @Path("/{name}" + FetchAndLockRestService.PATH)
  public FetchAndLockRestService fetchAndLock(@PathParam("name") String engineName) {
    String rootResourcePath = getRelativeEngineUri(engineName).toASCIIString();
    FetchAndLockRestServiceImpl subResource = new FetchAndLockRestServiceImpl(engineName, getObjectMapper(), getFetchAndLockHandler());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

  private URI getRelativeEngineUri(String engineName) {
    return UriBuilder.fromResource(JaxRsTwoNamedProcessEngineRestServiceImpl.class).path("{name}").build(engineName);
  }

}
