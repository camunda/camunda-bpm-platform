/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.integrationtest.util;

import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.strategy.RejectDependenciesStrategy;



/**
 * Note: methods exposed by this class should not be accessed at test runtime in the container
 * since some classes may not be available (e.g. shrinkwrap resolver classes)
 *
 * @author christian.lipphardt
 */
public class TestContainer {

  public static void addContainerSpecificResources(WebArchive webArchive) {
    addContainerSpecificResourcesWithoutWeld(webArchive);
  }

  public static void addContainerSpecificResourcesWithoutWeld(WebArchive webArchive) {
    webArchive.addAsLibraries(DeploymentHelper.getEjbClient());
  }

  public static void addContainerSpecificResourcesForNonPa(WebArchive webArchive) {
    addContainerSpecificResourcesForNonPaWithoutWeld(webArchive);
  }

  public static void addContainerSpecificResourcesForNonPaWithoutWeld(WebArchive webArchive) {
    // nothing to do
  }

  public static void addContainerSpecificProcessEngineConfigurationClass(WebArchive deployment) {
    // nothing to do
  }

  public static void addSpinJacksonJsonDataFormat(WebArchive webArchive) {
    webArchive.addAsLibraries(getSpinJacksonJsonDataFormat());
  }

  protected static JavaArchive[] getSpinJacksonJsonDataFormat() {
    return Maven.resolver()
      .offline()
      .loadPomFromFile("pom.xml")
      .resolve("org.camunda.spin:camunda-spin-dataformat-json-jackson")
      .using(new RejectDependenciesStrategy(false,
          "org.camunda.spin:camunda-spin-core",
          "org.camunda.commons:camunda-commons-logging",
          "org.camunda.commons:camunda-commons-utils"))
      .as(JavaArchive.class);
  }

}
