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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HttpHeaderSecurityIT extends AbstractWebIntegrationTest {

  public static final String CSP_VALUE = "base-uri 'self';script-src 'nonce-([-_a-zA-Z\\d]*)' 'strict-dynamic' 'unsafe-eval' https: 'self' 'unsafe-inline';style-src 'unsafe-inline' 'self';default-src 'self';img-src 'self' data:;block-all-mixed-content;form-action 'self';frame-ancestors 'none';object-src 'none';sandbox allow-forms allow-scripts allow-same-origin allow-popups allow-downloads";

  @Before
  public void createClient() throws Exception {
    preventRaceConditions();
    createClient(getWebappCtxPath());
  }

  @Test(timeout=10000)
  public void shouldCheckPresenceOfXssProtectionHeader() {
    // given

    // when
    ClientResponse response = client.resource(appBasePath + TASKLIST_PATH)
        .get(ClientResponse.class);

    // then
    assertEquals(200, response.getStatus());
    assertHeaderPresent("X-XSS-Protection", "1; mode=block", response);

    // cleanup
    response.close();
  }

  @Test(timeout=10000)
  public void shouldCheckPresenceOfContentSecurityPolicyHeader() {
    // given

    // when
    ClientResponse response = client.resource(appBasePath + TASKLIST_PATH)
        .get(ClientResponse.class);

    // then
    assertEquals(200, response.getStatus());
    assertHeaderPresent("Content-Security-Policy", CSP_VALUE, response);

    // cleanup
    response.close();
  }

  @Test(timeout=10000)
  public void shouldCheckPresenceOfContentTypeOptions() {
    // given

    // when
    ClientResponse response = client.resource(appBasePath + TASKLIST_PATH)
        .get(ClientResponse.class);

    // then
    assertEquals(200, response.getStatus());
    assertHeaderPresent("X-Content-Type-Options", "nosniff", response);

    // cleanup
    response.close();
  }

  @Test(timeout=10000)
  public void shouldCheckAbsenceOfHsts() {
    // given

    // when
    ClientResponse response = client.resource(appBasePath + TASKLIST_PATH)
        .get(ClientResponse.class);

    // then
    assertEquals(200, response.getStatus());
    MultivaluedMap<String, String> headers = response.getHeaders();
    List<String> values = headers.get("Strict-Transport-Security");
    assertNull(values);

    // cleanup
    response.close();
  }

  protected void assertHeaderPresent(String expectedName, String expectedValue, ClientResponse response) {
    MultivaluedMap<String, String> headers = response.getHeaders();

    List<String> values = headers.get(expectedName);
    for (String value : values) {
      if (value.matches(expectedValue)) {
        return;
      }
    }

    Assert.fail(String.format("Header '%s' didn't match.\nExpected:\t%s \nActual:\t%s", expectedName, expectedValue, values));
  }

}
