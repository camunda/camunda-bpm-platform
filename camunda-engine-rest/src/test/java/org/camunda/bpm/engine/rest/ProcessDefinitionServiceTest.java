package org.camunda.bpm.engine.rest;

import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Assert;
import org.junit.Test;

public class ProcessDefinitionServiceTest extends AbstractRestServiceTest {

  @Test
  public void testProcessDefinitionRetrieval() {
    WebClient client = WebClient.create(SERVER_ADDRESS);
    client.accept("application/json");
    client.path("/process-definition/query");
    Response response = client.get();
    Assert.fail("Not yet implemented.");
  }
}
