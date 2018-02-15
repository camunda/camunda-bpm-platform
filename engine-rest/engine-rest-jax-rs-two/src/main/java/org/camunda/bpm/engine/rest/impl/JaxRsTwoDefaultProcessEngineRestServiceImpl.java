package org.camunda.bpm.engine.rest.impl;

import org.camunda.bpm.engine.rest.impl.fetchAndLock.FetchAndLockRestService;
import org.camunda.bpm.engine.rest.impl.fetchAndLock.FetchAndLockRestServiceImpl;

import javax.ws.rs.Path;
import java.net.URI;

/**
 * @author Tassilo Weidner
 */
@Path(JaxRsTwoDefaultProcessEngineRestServiceImpl.PATH)
public class JaxRsTwoDefaultProcessEngineRestServiceImpl extends JaxRsTwoAbstractProcessEngineRestServiceImpl {

  static final String PATH = "";
  private String rootResourcePath = URI.create("/").toASCIIString();

  @Path(FetchAndLockRestService.PATH)
  public FetchAndLockRestService fetchAndLock() {
    FetchAndLockRestServiceImpl subResource = new FetchAndLockRestServiceImpl(null, getObjectMapper(), getFetchAndLockHandler());
    subResource.setRelativeRootResourceUri(rootResourcePath);
    return subResource;
  }

}
