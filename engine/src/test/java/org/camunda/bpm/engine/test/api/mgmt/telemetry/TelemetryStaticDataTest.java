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
package org.camunda.bpm.engine.test.api.mgmt.telemetry;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.impl.telemetry.dto.ApplicationServerImpl;
import org.junit.Test;

public class TelemetryStaticDataTest {

  @Test
  public void shouldValidateWildFlyVendor() {
    // given
    ApplicationServerImpl server = new ApplicationServerImpl("WildFly Full 19.0.0.Final (WildFly Core 11.0.0.Final) - 2.0.30.Final");
    assertThat(server.getVendor()).isEqualTo("WildFly");
  }

  @Test
  public void shouldValidateJbossVendor() {
    // given
    ApplicationServerImpl server = new ApplicationServerImpl("JBoss EAP 7.2.0.GA (WildFly Core 6.0.11.Final-redhat-00001) - 2.0.15.Final-redhat-00001");
    assertThat(server.getVendor()).isEqualTo("JBoss EAP");
  }

  @Test
  public void shouldValidateTomcatVendor() {
    // given
    ApplicationServerImpl server = new ApplicationServerImpl("Apache Tomcat/9.0.36");
    assertThat(server.getVendor()).isEqualTo("Apache Tomcat");
  }

  @Test
  public void shouldValidateWebLogicVendor() {
    // given
    ApplicationServerImpl server = new ApplicationServerImpl("WebLogic Server 12.2.1.0.0 Tue Oct 6 10:05:47 PDT 2015 1721936 WebLogic JAX-RS 2.0 Portable Server / Jersey 2.x integration module");
    assertThat(server.getVendor()).isEqualTo("WebLogic Server");
  }

  @Test
  public void shouldValidateWebSphereVendor() {
    // given
    ApplicationServerImpl server = new ApplicationServerImpl("IBM WebSphere Application Server/8.5");
    assertThat(server.getVendor()).isEqualTo("IBM WebSphere Application Server");
  }

}
