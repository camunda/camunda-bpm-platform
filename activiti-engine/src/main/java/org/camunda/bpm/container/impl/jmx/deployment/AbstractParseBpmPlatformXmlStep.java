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
package org.camunda.bpm.container.impl.jmx.deployment;

import java.net.URL;

import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;
import org.camunda.bpm.container.impl.metadata.BpmPlatformXmlParser;
import org.camunda.bpm.container.impl.metadata.spi.BpmPlatformXml;

/**
 * <p>Deployment operation step responsible for parsing and attaching the bpm-platform.xml file.</p>
 * 
 * @author Daniel Meyer
 *
 */
public abstract class AbstractParseBpmPlatformXmlStep extends MBeanDeploymentOperationStep {

  public String getName() {
    return "Parsing bpm-platform.xml file";
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {
    
    URL bpmPlatformXmlSource = getBpmPlatformXmlStream(operationContext);
    
    // parse the bpm platform xml
    BpmPlatformXml bpmPlatformXml = new BpmPlatformXmlParser().createParse()
      .sourceUrl(bpmPlatformXmlSource)
      .execute()
      .getBpmPlatformXml();
    
    // attach to operation context
    operationContext.addAttachment(Attachments.BPM_PLATFORM_XML, bpmPlatformXml);     
    
  }

  protected abstract URL getBpmPlatformXmlStream(MBeanDeploymentOperation operationContext);

}
