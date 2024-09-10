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
 *
 * @author christian.lipphardt
 */
public class TestContainer {

  public static void addContainerSpecificResourcesEmbedCdiLib(WebArchive webArchive) {
    addContainerSpecificResources(webArchive);
  }

  public static void addContainerSpecificResources(WebArchive webArchive) {
    addContainerSpecificResourcesWithoutWeld(webArchive);
  }

  public static void addContainerSpecificResourcesWithoutWeld(WebArchive webArchive) {
    webArchive.addAsLibraries(DeploymentHelper.getEjbClient());
  }

  public static void addContainerSpecificResourcesForNonPaEmbedCdiLib(WebArchive webArchive) {
    addContainerSpecificResourcesForNonPa(webArchive);
  }

  public static void addContainerSpecificResourcesForNonPa(WebArchive webArchive) {
    addContainerSpecificResourcesForNonPaWithoutWeld(webArchive);
  }

  public static void addContainerSpecificResourcesForNonPaWithoutWeld(WebArchive webArchive) {
    webArchive.addAsManifestResource("jboss-deployment-structure.xml");
  }

  public static void addContainerSpecificProcessEngineConfigurationClass(WebArchive deployment) {
    // nothing to do
  }

  public static void addSpinJacksonJsonDataFormat(WebArchive webArchive) {
    webArchive.addAsManifestResource("jboss-deployment-structure-spin-json.xml", "jboss-deployment-structure.xml");
  }

  public static void addJodaTimeJacksonModule(WebArchive webArchive) {
    webArchive.addAsLibraries(DeploymentHelper.getJodaTimeModuleForServer("jboss"));
  }

  public static void addCommonLoggingDependency(WebArchive webArchive) {
    webArchive.addAsManifestResource("jboss-deployment-structure-with-commons-logging.xml", "jboss-deployment-structure.xml");
  }
}
