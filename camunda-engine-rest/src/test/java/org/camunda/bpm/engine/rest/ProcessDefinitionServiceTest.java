package org.camunda.bpm.engine.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Assert;
import org.junit.Test;

public class ProcessDefinitionServiceTest extends AbstractRestServiceTest {
  
  @Test
  public void testProcessDefinitionRetrieval() {
    WebClient client = WebClient.create("http://localhost:8080");
    client.accept("application/json");
    client.path("/process-definition/query");
    client.query("pid", "0");
    
    String content = client.get(String.class);
    
    System.out.println(content);
    
    Response response = client.getResponse();
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());
  }
}
