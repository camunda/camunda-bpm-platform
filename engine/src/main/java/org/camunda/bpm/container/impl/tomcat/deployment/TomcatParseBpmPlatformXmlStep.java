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
package org.camunda.bpm.container.impl.tomcat.deployment;

import org.camunda.bpm.container.impl.ContainerIntegrationLogger;
import org.camunda.bpm.container.impl.deployment.AbstractParseBpmPlatformXmlStep;
import org.camunda.bpm.container.impl.spi.DeploymentOperation;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * <p>This deployment operation step is responsible for parsing and attaching the bpm-platform.xml file on tomcat.</p>
 *
 * <p>We assume that the bpm-platform.xml file is located under <code>$CATALINA_HOME/conf/bpm-platform.xml</code>.</p>
 *
 * @author Daniel Meyer
 * @author Christian Lipphardt
 *
 */
public class TomcatParseBpmPlatformXmlStep extends AbstractParseBpmPlatformXmlStep {

  private final static ContainerIntegrationLogger LOG = ProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER;

  public static final String CATALINA_BASE = "catalina.base";
  public static final String CATALINA_HOME = "catalina.home";

  public URL getBpmPlatformXmlStream(DeploymentOperation operationcontext) {
    URL fileLocation = lookupBpmPlatformXml();

    if (fileLocation == null) {
      fileLocation = lookupBpmPlatformXmlFromCatalinaConfDirectory();
    }

    return fileLocation;
  }

  public URL lookupBpmPlatformXmlFromCatalinaConfDirectory() {
    // read file from CATALINA_BASE if set, otherwise CATALINA_HOME directory.
    String catalinaHome = System.getProperty(CATALINA_BASE);
    if (catalinaHome == null) {
      catalinaHome = System.getProperty(CATALINA_HOME);
    }

    String bpmPlatformFileLocation = catalinaHome + File.separator + "conf" + File.separator + BPM_PLATFORM_XML_FILE;

    try {
      URL fileLocation = checkValidFileLocation(bpmPlatformFileLocation);

      if (fileLocation != null) {
        LOG.foundTomcatDeploymentDescriptor(bpmPlatformFileLocation, fileLocation.toString());
      }

      return fileLocation;
    }
    catch (MalformedURLException e) {
      throw LOG.invalidDeploymentDescriptorLocation(bpmPlatformFileLocation, e);
    }
  }


}
