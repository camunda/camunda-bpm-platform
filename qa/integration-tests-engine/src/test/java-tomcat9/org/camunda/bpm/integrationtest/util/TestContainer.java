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
package org.camunda.bpm.integrationtest.util;

import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Tomcat test container.
 *
 * @author Daniel Meyer
 */
public class TestContainer {

  /**
   * In some scenarios, Tomcat 10 and Weld 5 have issues when the Weld library is embedded into the WAR.
   * To solve these issues, Weld is added to the Tomcat server libs folder.
   */
  public static void addContainerSpecificResourcesEmbedCdiLib(WebArchive webArchive) {
    addContainerSpecificResources(webArchive);
  }

  public static void addContainerSpecificResources(WebArchive webArchive) {
    webArchive
        .addAsManifestResource("context.xml")
        .addClass(TestProcessApplication.class)
        .addAsWebInfResource("web.xml")
        .addAsLibraries(DeploymentHelper.getWeld());
  }

  public static void addContainerSpecificResourcesWithoutWeld(WebArchive webArchive) {
    webArchive
      .addAsManifestResource("context.xml")
      .addClass(TestProcessApplication.class)
      .addAsWebInfResource("web-without-weld.xml", "web.xml");
  }

  /**
   * In some scenarios, Tomcat 10 and Weld 5 have issues when the Weld library is embedded into the WAR.
   * To solve these issues, Weld is added to the Tomcat server libs folder.
   */
  public static void addContainerSpecificResourcesForNonPaEmbedCdiLib(WebArchive webArchive) {
    addContainerSpecificResourcesForNonPa(webArchive);
  }

  public static void addContainerSpecificResourcesForNonPa(WebArchive webArchive) {
    webArchive
        .addAsManifestResource("context.xml")
        .addAsWebInfResource("web.xml")
        .addAsLibraries(DeploymentHelper.getWeld());
  }

  public static void addContainerSpecificResourcesForNonPaWithoutWeld(WebArchive webArchive) {
    webArchive
      .addAsManifestResource("context.xml")
      .addAsWebInfResource("web-without-weld.xml", "web.xml");
  }

  public static void addContainerSpecificProcessEngineConfigurationClass(WebArchive deployment) {
    // nothing to do
  }

  public static void addSpinJacksonJsonDataFormat(WebArchive webArchive) {
    webArchive.addAsLibraries(DeploymentHelper.getSpinJacksonJsonDataFormatForServer("tomcat"));
  }

  public static void addJodaTimeJacksonModule(WebArchive webArchive) {
    webArchive.addAsLibraries(DeploymentHelper.getJodaTimeModuleForServer("tomcat"));
  }

  public static void addCommonLoggingDependency(WebArchive webArchive) {
    // nothing to do
  }

}
