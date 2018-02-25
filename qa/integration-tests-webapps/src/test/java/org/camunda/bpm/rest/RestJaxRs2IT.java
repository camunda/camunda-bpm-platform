package org.camunda.bpm.rest;

import com.sun.jersey.api.client.ClientResponse;
import org.camunda.bpm.AbstractWebappIntegrationTest;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RestJaxRs2IT extends AbstractWebappIntegrationTest {

  private static final String ENGINE_DEFAULT_PATH = "engine/default";
  private static final String FETCH_AND_LOCK_PATH = ENGINE_DEFAULT_PATH + "/external-task/fetchAndLock";

  protected String getApplicationContextPath() {
    return "engine-rest/";
  }

  @BeforeClass
  public static void setup() throws InterruptedException {
    // just wait some seconds before starting because of Wildfly / Cargo race conditions
    Thread.sleep(5 * 1000);
  }

  @Test
  public void testJaxRsTwoArtifactIsUsed() throws JSONException {
    Map<String, Object> payload = new HashMap<String, Object>();
    payload.put("workerId", "aWorkerId");
    payload.put("asyncResponseTimeout", 1000 * 60 * 30 + 1);

    ClientResponse response = client.resource(APP_BASE_PATH + FETCH_AND_LOCK_PATH).accept(MediaType.APPLICATION_JSON)
      .entity(payload, MediaType.APPLICATION_JSON_TYPE)
      .post(ClientResponse.class);

    assertEquals(400, response.getStatus());
    String responseMessage = response.getEntity(JSONObject.class).get("message").toString();
    assertTrue(responseMessage.equals("The asynchronous response timeout cannot be set to a value greater than 1800000 milliseconds"));
    response.close();
  }

}
