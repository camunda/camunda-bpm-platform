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
package org.camunda.bpm.spring.boot.starter.webapp.filter.redirect;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.apache.commons.io.IOUtils;
import org.camunda.bpm.spring.boot.starter.webapp.filter.util.HttpClientRule;
import org.camunda.bpm.spring.boot.starter.webapp.filter.util.FilterTestApp;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { FilterTestApp.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "camunda.bpm.webapp.application-path=/",
        "camunda.bpm.webapp.index-redirect-enabled=false" })
@DirtiesContext
public class ResourceLoadingProcessEnginesAppPathRootTest {

  @Rule
  public HttpClientRule rule = new HttpClientRule().followRedirects(true);

  @LocalServerPort
  public int port;

  @Test
  public void shouldRedirectToStaticContent() throws IOException {
    // given
    // send GET request to /
    HttpURLConnection con = rule.performRequest("http://localhost:" + port + "/");

    // when
    // get content returned by the request
    String body = IOUtils.toString(con.getInputStream(), "UTF-8");

    // then
    assertThat(con.getResponseCode()).isEqualTo(200);
    // since index-redirect-enabled=false, Camunda should not redirect to Tasklist
    assertThat(body).doesNotContain("Tasklist").doesNotContain("Camunda");
    // the static index.html from /src/test/resources/static was served instead
    // this is the default Spring Boot behavior that we document for this case
    assertThat(body).contains("Hello World!");
  }

}
