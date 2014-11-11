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

import org.camunda.bpm.BpmPlatform;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * @author roman.smirnov
 */
public class TestContainer {
  
  public final static String APP_NAME = "";
  
  public final static String PROCESS_ENGINE_SERVICE_JNDI_NAME = BpmPlatform.PROCESS_ENGINE_SERVICE_JNDI_NAME;
  public final static String PROCESS_APPLICATION_SERVICE_JNDI_NAME = BpmPlatform.PROCESS_APPLICATION_SERVICE_JNDI_NAME;
  
  public static void addContainerSpecificResources(WebArchive archive) {
    archive.addClass(TestProcessApplication.class);
  }

  public static String getAppName() {
    return APP_NAME;
  }
  
  public static void addContainerSpecificResourcesForNonPa(WebArchive webArchive) {
    // nothing to do
  }

  public static void addContainerSpecificProcessEngineConfigurationClass(WebArchive deployment) {
    // nothing to do
  }

}
