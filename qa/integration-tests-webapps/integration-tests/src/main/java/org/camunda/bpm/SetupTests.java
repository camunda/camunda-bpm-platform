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

import kong.unirest.Unirest;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class SetupTests {

  protected static Properties properties = new Properties();

  public static void main(String[] args) {
    List<String> cookieValues = Unirest.get(getUrl() + "app/admin/default/setup/")
        .asString()
        .getHeaders()
        .get("Set-Cookie");

    String csrfCookieValue = null;
    for (String cookie : cookieValues) {
      if (cookie.startsWith("XSRF-TOKEN=")) {
        csrfCookieValue = cookie;
        break;
      }
    }

    if (csrfCookieValue == null) {
      throw new RuntimeException("CSRF Cookie does not exists!");
    }
    
    String csrfToken = csrfCookieValue.substring(11, 43);

    Unirest.post(getUrl() + "api/admin/setup/default/user/create")
        .header("X-XSRF-TOKEN", csrfToken)
        .header("Content-Type", "application/json")
        .body("{\"profile\":{\"id\":\"demo\",\"firstName\":\"demo\",\"lastName\":\"demo\",\"email\":\"\"}," +
            "\"credentials\":{\"password\":\"demo\"}}")
        .asEmpty();
  }

  protected static String getUrl() {
    try {
      properties.load(SetupTests.class.getResourceAsStream("/testconfig.properties"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return "http://localhost:" + properties.getProperty("http.port") + "/" +
        properties.getProperty("http.ctx-path.webapp");
  }

}