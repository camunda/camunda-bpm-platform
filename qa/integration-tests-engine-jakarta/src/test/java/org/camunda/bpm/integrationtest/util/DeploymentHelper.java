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

import org.apache.commons.lang.ArrayUtils;
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
    return getWeld(CAMUNDA_ENGINE_CDI);
  }

  public static JavaArchive[] getEngineSpring() {
    return getEngineSpring(CAMUNDA_ENGINE_SPRING);
  }

  protected static JavaArchive[] getWeld(String engineCdiArtifactName) {
    if (CACHED_WELD_ASSETS != null) {
      return CACHED_WELD_ASSETS;
    } else {

      JavaArchive[] archives = resolveDependenciesFromPomXml(engineCdiArtifactName,
              "org.jboss.weld.servlet:weld-servlet-shaded"
      );

      if(archives.length == 0) {
        throw new RuntimeException("could not resolve the weld implementation and jakarta API dependencies");
      } else {
        CACHED_WELD_ASSETS = archives;
        return CACHED_WELD_ASSETS;
      }
    }
  }

  protected static JavaArchive[] resolveDependenciesFromPomXml(String engineCdiArtifactName, String... dependencyNames) {
    JavaArchive[] result = new JavaArchive[0];
    for (String dependencyName : dependencyNames) {
      JavaArchive[] archive = resolveDependenciesFromPomXml(engineCdiArtifactName, dependencyName);
      result = (JavaArchive[]) ArrayUtils.addAll(result, archive);
    }

    return result;
  }

  protected static JavaArchive[] resolveDependenciesFromPomXml(String engineCdiArtifactName, String dependencyName) {
      return Maven.configureResolver()
              .workOffline()
              .loadPomFromFile("pom.xml")
              .resolve(engineCdiArtifactName, dependencyName)
              .withoutTransitivity()
              .as(JavaArchive.class);
  }

}
