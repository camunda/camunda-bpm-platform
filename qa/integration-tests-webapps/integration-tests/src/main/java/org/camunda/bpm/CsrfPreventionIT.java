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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;

public class CsrfPreventionIT extends AbstractWebIntegrationTest {

  @Before
  public void createClient() throws Exception {
    preventRaceConditions();
    createClient(getWebappCtxPath());
  }

  @Test(timeout=10000)
  public void shouldCheckPresenceOfCsrfPreventionCookie() {
    // given

    // when
    ClientResponse response = client.resource(appBasePath + TASKLIST_PATH)
        .get(ClientResponse.class);

    // then
    assertEquals(200, response.getStatus());
    String xsrfTokenHeader = getXsrfTokenHeader(response);
    String xsrfCookieValue = getXsrfCookieValue(response);
    response.close();
    
    assertNotNull(xsrfTokenHeader);
    assertEquals(32, xsrfTokenHeader.length());
    assertNotNull(xsrfCookieValue);
    assertTrue(xsrfCookieValue.contains(";SameSite=Lax"));
  }

  @Test(timeout=10000)
  public void shouldRejectModifyingRequest() {
    // given
    String baseUrl = testProperties.getApplicationPath("/" + getWebappCtxPath());
    String modifyingRequestPath = "api/admin/auth/user/default/login/welcome";

    // when
    ClientResponse response = client.resource(baseUrl + modifyingRequestPath)
        .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_TYPE)
        .post(ClientResponse.class);

    // then
    assertEquals(403, response.getStatus());
    assertTrue(getXsrfTokenHeader(response).equals("Required"));
  }

}
