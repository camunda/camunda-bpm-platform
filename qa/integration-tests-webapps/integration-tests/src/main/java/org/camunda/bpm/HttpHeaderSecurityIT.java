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

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    HttpResponse<String> response = Unirest.get(appBasePath + TASKLIST_PATH).asString();

    // then
    assertEquals(200, response.getStatus());
    assertHeaderPresent("X-XSS-Protection", "1; mode=block", response);
  }

  @Test(timeout=10000)
  public void shouldCheckPresenceOfContentSecurityPolicyHeader() {
    // given

    // when
    HttpResponse<String> response = Unirest.get(appBasePath + TASKLIST_PATH).asString();

    // then
    assertEquals(200, response.getStatus());
    assertHeaderPresent("Content-Security-Policy", CSP_VALUE, response);
  }

  @Test(timeout=10000)
  public void shouldCheckPresenceOfContentTypeOptions() {
    // given

    // when
    HttpResponse<String> response = Unirest.get(appBasePath + TASKLIST_PATH).asString();

    // then
    assertEquals(200, response.getStatus());
    assertHeaderPresent("X-Content-Type-Options", "nosniff", response);
  }

  @Test(timeout=10000)
  public void shouldCheckAbsenceOfHsts() {
    // given

    // when
    HttpResponse<String> response = Unirest.get(appBasePath + TASKLIST_PATH).asString();

    // then
    assertEquals(200, response.getStatus());
    List<String> values = response.getHeaders().get("Strict-Transport-Security");
    assertTrue(values.isEmpty());
  }

  protected void assertHeaderPresent(String expectedName, String expectedValue, HttpResponse<String> response) {
    List<String> values = response.getHeaders().get(expectedName);

    if (values != null) {
      for (String value : values) {
        if (value.matches(expectedValue)) {
          return;
        }
      }
    }

    Assert.fail(String.format("Header '%s' didn't match.\nExpected:\t%s \nActual:\t%s", expectedName, expectedValue, values));
  }

}
