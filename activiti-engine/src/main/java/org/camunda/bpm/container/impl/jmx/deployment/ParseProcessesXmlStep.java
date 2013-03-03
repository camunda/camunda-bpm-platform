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

import static org.camunda.bpm.container.impl.jmx.deployment.Attachments.PROCESSES_XML_RESOURCES;
import static org.camunda.bpm.container.impl.jmx.deployment.Attachments.PROCESS_APPLICATION;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.util.IoUtil;
import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.impl.metadata.ProcessesXmlParser;
import org.camunda.bpm.application.impl.metadata.spi.ProcessesXml;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperation;
import org.camunda.bpm.container.impl.jmx.kernel.MBeanDeploymentOperationStep;


/**
 * <p>Detects and parses all META-INF/processes.xml files within the process application 
 * and attaches the parsed Metadata to the operation context.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class ParseProcessesXmlStep extends MBeanDeploymentOperationStep {

  private static final String META_INF_PROCESSES_XML = "META-INF/processes.xml";

  public String getName() {
    return "Parse "+META_INF_PROCESSES_XML+" files";
  }

  public void performOperationStep(MBeanDeploymentOperation operationContext) {

    final AbstractProcessApplication processApplication = operationContext.getAttachment(PROCESS_APPLICATION);
    final ClassLoader processApplicationClassloader = processApplication.getProcessApplicationClassloader();
      
    // load all marker files using the classloader of the process application
    Enumeration<URL> processesXmlFileLocations = null;
    try {
      processesXmlFileLocations = processApplicationClassloader.getResources(META_INF_PROCESSES_XML);
            
    } catch (IOException e) {
      throw new ActivitiException("IOException while reading "+META_INF_PROCESSES_XML);
    }

    // perform parsing
    Map<URL, ProcessesXml> parsedFiles = new HashMap<URL, ProcessesXml>();
    while (processesXmlFileLocations.hasMoreElements()) {        
      URL url = (URL) processesXmlFileLocations.nextElement();    
      if(isEmptyFile(url)) {
        parsedFiles.put(url, ProcessesXml.EMPTY_PROCESSES_XML);
        
      } else {
        parsedFiles.put(url, parseProcessesXml(url));
        
      }
    }
    
    // attach parsed metadata
    operationContext.addAttachment(PROCESSES_XML_RESOURCES, parsedFiles);
  }

  protected boolean isEmptyFile(URL url) {

    InputStream inputStream = null;

    try {
      inputStream = url.openStream();
      return inputStream.available() == 0;
      
    } catch (IOException e) {
      throw new ActivitiException("Could not open stream for " + META_INF_PROCESSES_XML, e); 
      
    } finally {
      IoUtil.closeSilently(inputStream);
      
    }
  }

  protected ProcessesXml parseProcessesXml(URL url) {
    
    final ProcessesXmlParser processesXmlParser = new ProcessesXmlParser();
      
    ProcessesXml processesXml = processesXmlParser.createParse()
      .sourceUrl(url)
      .execute()
      .getProcessesXml();
      
    return processesXml;
      
  }

}
