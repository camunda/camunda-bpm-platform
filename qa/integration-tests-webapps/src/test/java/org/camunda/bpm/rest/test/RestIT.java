package org.camunda.bpm.rest.test;

import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.junit.Assert;

import org.camunda.bpm.AbstractWebappIntegrationTest;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class RestIT extends AbstractWebappIntegrationTest {

  private static final String PROCESS_DEFINITION_PATH = "engine/default/process-definition";

  private final static Logger log = Logger.getLogger(RestIT.class.getName());

  protected String getApplicationContextPath() {
    return "engine-rest/";
  }

  @Test
  public void testScenario() throws JSONException {
    
    // FIXME: cannot do this on JBoss AS7, see https://app.camunda.com/jira/browse/CAM-787    
    
    // get list of process engines
    // log.info("Checking " + APP_BASE_PATH + ENGINES_PATH);
    // WebResource resource = client.resource(APP_BASE_PATH + ENGINES_PATH);
    // ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);
    //
    // Assert.assertEquals(200, response.getStatus());
    //
    // JSONArray enginesJson = response.getEntity(JSONArray.class);
    // Assert.assertEquals(1, enginesJson.length());
    //
    // JSONObject engineJson = enginesJson.getJSONObject(0);
    // Assert.assertEquals("default", engineJson.getString("name"));
    //
    // response.close();

    // get process definitions for default engine
    log.info("Checking " + APP_BASE_PATH + PROCESS_DEFINITION_PATH);
    WebResource resource = client.resource(APP_BASE_PATH + PROCESS_DEFINITION_PATH);
    ClientResponse response = resource.accept(MediaType.APPLICATION_JSON).get(ClientResponse.class);

    Assert.assertEquals(200, response.getStatus());

    JSONArray definitionsJson = response.getEntity(JSONArray.class);
    // invoice example
    Assert.assertEquals(1, definitionsJson.length());

    JSONObject definitionJson = definitionsJson.getJSONObject(0);

    Assert.assertEquals("invoice", definitionJson.getString("key"));
    Assert.assertEquals("http://www.omg.org/spec/BPMN/20100524/MODEL", definitionJson.getString("category"));
    Assert.assertEquals("invoice receipt", definitionJson.getString("name"));
    Assert.assertTrue(definitionJson.isNull("description"));
    Assert.assertTrue(definitionJson.getString("resource").contains("invoice.bpmn"));
    Assert.assertFalse(definitionJson.getBoolean("suspended"));

    response.close();
  }

}
