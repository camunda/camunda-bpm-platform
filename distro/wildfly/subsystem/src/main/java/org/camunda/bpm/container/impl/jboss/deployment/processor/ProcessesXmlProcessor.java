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
package org.camunda.bpm.container.impl.jboss.deployment.processor;

import static org.jboss.as.server.deployment.Attachments.MODULE;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.metadata.ProcessesXmlParser;
import org.camunda.bpm.application.impl.metadata.spi.ProcessesXml;
import org.camunda.bpm.container.impl.jboss.deployment.marker.ProcessApplicationAttachments;
import org.camunda.bpm.container.impl.jboss.util.ProcessesXmlWrapper;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.jboss.as.ee.component.ComponentDescription;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.modules.Module;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;


/**
 * <p>Detects and processes all <em>META-INF/processes.xml</em> files that are visible from the module
 * classloader of the {@link DeploymentUnit}.</p>
 *
 * <p>This is POST_MODULE so we can take into account module visibility in case of composite deployments
 * (EARs)</p>
 *
 * @author Daniel Meyer
 *
 */
public class ProcessesXmlProcessor implements DeploymentUnitProcessor {

  public static final String PROCESSES_XML = "META-INF/processes.xml";

  public static final int PRIORITY = 0x0000; // this can happen ASAP in the POST_MODULE Phase

  @Override
  public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

    DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();

    if(!ProcessApplicationAttachments.isProcessApplication(deploymentUnit)) {
      return;
    }

    final Module module = deploymentUnit.getAttachment(MODULE);

    // read @ProcessApplication annotation of PA-component
    String[] deploymentDescriptors = getDeploymentDescriptors(deploymentUnit);

    // load all processes.xml files
    List<URL> deploymentDescriptorURLs = getDeploymentDescriptorUrls(module, deploymentDescriptors);

    for (URL processesXmlResource : deploymentDescriptorURLs) {
      VirtualFile processesXmlFile = getFile(processesXmlResource);

      // parse processes.xml metadata.
      ProcessesXml processesXml = null;
      if(isEmptyFile(processesXmlResource)) {
        processesXml = ProcessesXml.EMPTY_PROCESSES_XML;
      } else {
        processesXml = parseProcessesXml(processesXmlResource);
      }

      // add the parsed metadata to the attachment list
      ProcessApplicationAttachments.addProcessesXml(deploymentUnit, new ProcessesXmlWrapper(processesXml, processesXmlFile));
    }
  }

  protected List<URL> getDeploymentDescriptorUrls(final Module module, String[] deploymentDescriptors) throws DeploymentUnitProcessingException {
    List<URL> deploymentDescriptorURLs = new ArrayList<URL>();
    for (String deploymentDescriptor : deploymentDescriptors) {
      Enumeration<URL> resources = null;
      try {
        resources = module.getClassLoader().getResources(deploymentDescriptor);
      } catch (IOException e) {
        throw new DeploymentUnitProcessingException("Could not load processes.xml resource: ", e);
      }
      while (resources.hasMoreElements()) {
        deploymentDescriptorURLs.add(resources.nextElement());
      }
    }
    return deploymentDescriptorURLs;
  }

  protected String[] getDeploymentDescriptors(DeploymentUnit deploymentUnit) throws DeploymentUnitProcessingException {

    final ComponentDescription processApplicationComponent = ProcessApplicationAttachments.getProcessApplicationComponent(deploymentUnit);
    final String paClassName = processApplicationComponent.getComponentClassName();

    String[] deploymentDescriptorResourceNames = null;

    Module module = deploymentUnit.getAttachment(MODULE);

    Class<?> paClass = null;
    try {
      paClass = module.getClassLoader().loadClass(paClassName);
    } catch (ClassNotFoundException e) {
      throw new DeploymentUnitProcessingException("Unable to load process application class '"+paClassName+"'.");
    }

    ProcessApplication annotation = paClass.getAnnotation(ProcessApplication.class);

    if(annotation == null) {
      deploymentDescriptorResourceNames = new String[]{ PROCESSES_XML };

    } else {
      deploymentDescriptorResourceNames = annotation.deploymentDescriptors();

    }
    return deploymentDescriptorResourceNames;
  }

  protected Enumeration<URL> getProcessesXmlResources(Module module, String[] deploymentDescriptors) throws DeploymentUnitProcessingException {
    try {
      return module.getClassLoader().getResources(PROCESSES_XML);
    } catch (IOException e) {
      throw new DeploymentUnitProcessingException(e);
    }
  }

  protected VirtualFile getFile(URL processesXmlResource) throws DeploymentUnitProcessingException {
    try {
      return VFS.getChild(processesXmlResource.toURI());
    } catch(Exception e) {
      throw new DeploymentUnitProcessingException(e);
    }
  }

  protected boolean isEmptyFile(URL url) {

    InputStream inputStream = null;

    try {
      inputStream = url.openStream();
      return inputStream.available() == 0;

    } catch (IOException e) {
      throw new ProcessEngineException("Could not open stream for " + url, e);

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
