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
package org.camunda.bpm.rest.distro.test;

import java.util.Collections;

import org.camunda.bpm.rest.distro.CamundaRestDistro;
import org.camunda.bpm.rest.distro.test.util.LoggingInterceptor;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { CamundaRestDistro.class }, webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = { "test-auth-disabled" })
public abstract class AbstractRestTest {

  @Autowired
  protected TestRestTemplate testRestTemplate;

  @LocalServerPort
  protected int localPort;

  @Before
  public void enableRequestResponseLogging() {
    testRestTemplate.getRestTemplate().setInterceptors(Collections.singletonList(new LoggingInterceptor()));
  }
}
