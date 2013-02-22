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
package com.camunda.fox.platform.subsystem.impl.deployment.processor;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.util.IoUtil;
import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessesXml;
import org.camunda.bpm.application.impl.deployment.parser.ProcessesXmlParser;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.vfs.VirtualFile;

import com.camunda.fox.platform.subsystem.impl.deployment.marker.ProcessApplicationAttachments;
import com.camunda.fox.platform.subsystem.impl.util.ProcessesXmlWrapper;

/**
 * <p>Detects and processes the <em>META-INF/processes.xml</em> file and attaches
 * the parsed Information to the {@link DeploymentUnit}.</p>
 * 
 * <p>Marks the deployment unit and the parent of the deployment unit as a process
 * application.</p>
 * 
 * @author Daniel Meyer
 * 
 */
public class ProcessesXmlProcessor implements DeploymentUnitProcessor {

  public static final String PROCESSES_XML_JAR = "META-INF/processes.xml";
  public static final String PROCESSES_XML_WAR = "WEB-INF/classes/META-INF/processes.xml";
  
  public static final int PRIORITY = 0x1050;

  public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
    
    DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
    
    ResourceRoot deploymentRoot = deploymentUnit.getAttachment(Attachments.DEPLOYMENT_ROOT);
    
    VirtualFile processesXmlFile = deploymentRoot.getRoot().getChild(PROCESSES_XML_JAR);
    if(processesXmlFile == null || !processesXmlFile.exists()) {
      processesXmlFile = deploymentRoot.getRoot().getChild(PROCESSES_XML_WAR);
    }
    
    if(processesXmlFile != null && processesXmlFile.exists()) {
      
      // mark the deployment unit as a ProcessApplication
      ProcessApplicationAttachments.mark(deploymentUnit);      
      // mark the parent as well
      if(deploymentUnit.getParent() != null) {
        ProcessApplicationAttachments.mark(deploymentUnit.getParent());        
      }
      
      // get URL for processes XML
      URL url = null;
      try {
        url = processesXmlFile.toURL();
      } catch (MalformedURLException e) {
        throw new ActivitiException("Cannot create URL for "+processesXmlFile);
      }
      
      // parse processes.xml metadata.
      ProcessesXml processesXml = null;
      if(isEmptyFile(url)) {
        processesXml = ProcessesXml.EMPTY_PROCESSES_XML;
      } else {
        processesXml = parseProcessesXml(url);
      }
      
      // mark the deployment unit with the parsed processesXml
      ProcessApplicationAttachments.attachProcessesXml(deploymentUnit, new ProcessesXmlWrapper(processesXml, processesXmlFile));
    }
  }
  
  protected boolean isEmptyFile(URL url) {

    InputStream inputStream = null;

    try {
      inputStream = url.openStream();
      return inputStream.available() == 0;
      
    } catch (IOException e) {
      throw new ActivitiException("Could not open stream for " + url, e); 
      
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

  public void undeploy(DeploymentUnit context) {
    
  }

}
