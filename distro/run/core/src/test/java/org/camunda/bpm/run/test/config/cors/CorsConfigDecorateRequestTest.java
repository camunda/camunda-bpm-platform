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
package org.camunda.bpm.run.test.config.cors;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.run.property.CamundaBpmRunCorsProperty;
import org.camunda.bpm.run.property.CamundaBpmRunProperties;
import org.camunda.bpm.run.test.AbstractRestTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * The CORS <code>decorateRequest</code> property populates an <code>HttpServletRequest</code>
 * instance with CORS-related attributes. The functionality is provided and tested by Tomcat.
 * This test case covers the related Camunda Run property.
 */
@ActiveProfiles(profiles = { "test-cors-enabled" }, inheritProfiles = true)
@TestPropertySource(properties = {CamundaBpmRunCorsProperty.PREFIX + ".decorate-request=false"})
public class CorsConfigDecorateRequestTest extends AbstractRestTest {

  @Autowired
  protected CamundaBpmRunProperties camundaBpmRunProperties;

  @Test
  public void shouldSetDecorateRequestProperty() {
    // then
    assertThat(camundaBpmRunProperties.getCors().getDecorateRequest()).isFalse();
  }
}