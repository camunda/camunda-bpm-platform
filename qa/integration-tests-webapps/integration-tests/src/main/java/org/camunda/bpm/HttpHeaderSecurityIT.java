/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm;

import com.sun.jersey.api.client.ClientResponse;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HttpHeaderSecurityIT extends AbstractWebIntegrationTest {

  @Before
  public void createClient() throws Exception {
    preventRaceConditions();
    createClient(getWebappCtxPath());
  }

  @Test(timeout=10000)
  public void shouldCheckPresenceOfXssProtectionHeader() {
    // given

    // when
    ClientResponse response = client.resource(APP_BASE_PATH + TASKLIST_PATH)
        .get(ClientResponse.class);

    // then
    assertEquals(200, response.getStatus());
    assertTrue(isHeaderPresent("X-XSS-Protection", "1; mode=block", response));

    // cleanup
    response.close();
  }

  @Test(timeout=10000)
  public void shouldCheckPresenceOfContentSecurityPolicyHeader() {
    // given

    // when
    ClientResponse response = client.resource(APP_BASE_PATH + TASKLIST_PATH)
        .get(ClientResponse.class);

    // then
    assertEquals(200, response.getStatus());
    assertTrue(isHeaderPresent("Content-Security-Policy", "base-uri 'self'", response));

    // cleanup
    response.close();
  }

  @Test(timeout=10000)
  public void shouldCheckPresenceOfContentTypeOptions() {
    // given

    // when
    ClientResponse response = client.resource(APP_BASE_PATH + TASKLIST_PATH)
        .get(ClientResponse.class);

    // then
    assertEquals(200, response.getStatus());
    assertTrue(isHeaderPresent("X-Content-Type-Options", "nosniff", response));

    // cleanup
    response.close();
  }

  @Test(timeout=10000)
  public void shouldCheckAbsenceOfHsts() {
    // given

    // when
    ClientResponse response = client.resource(APP_BASE_PATH + TASKLIST_PATH)
        .get(ClientResponse.class);

    // then
    assertEquals(200, response.getStatus());
    MultivaluedMap<String, String> headers = response.getHeaders();
    List<String> values = headers.get("Strict-Transport-Security");
    assertNull(values);

    // cleanup
    response.close();
  }

  protected boolean isHeaderPresent(String expectedName, String expectedValue, ClientResponse response) {
    MultivaluedMap<String, String> headers = response.getHeaders();

    List<String> values = headers.get(expectedName);
    for (String value : values) {
      if (value.equals(expectedValue)) {
        return true;
      }
    }

    return false;
  }

}
