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

import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

public class DeploymentHelper extends AbstractDeploymentHelper {

  protected static final String CAMUNDA_EJB_CLIENT = "org.camunda.bpm.javaee:camunda-ejb-client-jakarta";
  protected static final String CAMUNDA_ENGINE_CDI = "org.camunda.bpm:camunda-engine-cdi-jakarta";
  protected static final String CAMUNDA_ENGINE_SPRING = "org.camunda.bpm:camunda-engine-spring-6";

  public static JavaArchive getEjbClient() {
    return getEjbClient(CAMUNDA_EJB_CLIENT);
  }

  public static JavaArchive getEngineCdi() {
    return getEngineCdi(CAMUNDA_ENGINE_CDI);
  }

  public static JavaArchive[] getWeld() {
    return getWeldShaded(CAMUNDA_ENGINE_CDI);
  }

  public static JavaArchive[] getEngineSpring() {
    return getEngineSpring(CAMUNDA_ENGINE_SPRING);
  }

  protected static JavaArchive[] getWeldShaded(String engineCdiArtifactName) {
    if(CACHED_WELD_ASSETS != null) {
      return CACHED_WELD_ASSETS;
    } else {

      JavaArchive[] resolvedArchives = Maven.configureResolver()
              .workOffline()
              .loadPomFromFile("pom.xml")
              .resolve(engineCdiArtifactName, "org.jboss.weld.servlet:weld-servlet-shaded")
              .withTransitivity()
              .as(JavaArchive.class);

      if(resolvedArchives.length == 0) {
        throw new RuntimeException("could not resolve org.jboss.weld.servlet:weld-servlet-shaded");
      } else {
        CACHED_WELD_ASSETS = resolvedArchives;
        return CACHED_WELD_ASSETS;
      }
    }
  }

}
