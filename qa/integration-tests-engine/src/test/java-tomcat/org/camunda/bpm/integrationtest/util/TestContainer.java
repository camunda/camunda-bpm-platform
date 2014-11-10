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

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * Tomcat test container.
 * 
 * @author Daniel Meyer
 */
public class TestContainer {
  
  public final static String APP_NAME = "";
  public final static String PROCESS_ENGINE_SERVICE_JNDI_NAME = "java:comp/env/ProcessEngineService";
  public final static String PROCESS_APPLICATION_SERVICE_JNDI_NAME = "java:comp/env/ProcessApplicationService";
  
  public static void addContainerSpecificResources(WebArchive archive) {
    
    archive
      .addAsManifestResource("context.xml")
      .addAsLibraries(DeploymentHelper.getWeld())
      .addClass(IntegrationTestProcessApplication.class)
      .addAsWebInfResource("web.xml");
  }

  public static String getAppName() {
    return APP_NAME;
  }

  public static void addContainerSpecificResourcesForNonPa(WebArchive deployment) {
    
    deployment
    .addAsManifestResource("context.xml")
    .addAsLibraries(DeploymentHelper.getWeld())
    .addAsWebInfResource("web.xml");
    
  }

  public static void addContainerSpecificProcessEngineConfigurationClass(WebArchive deployment) {
    // nothing to do
  }

  public static Archive<?> processArchive(Archive<?> archive) {
    return archive;
  }
  
}
