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
package org.camunda.bpm.container.impl.deployment;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.metadata.ProcessesXmlParser;
import org.camunda.bpm.application.impl.metadata.spi.ProcessesXml;
import org.camunda.bpm.container.impl.ContainerIntegrationLogger;
import org.camunda.bpm.container.impl.spi.DeploymentOperation;
import org.camunda.bpm.container.impl.spi.DeploymentOperationStep;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.util.IoUtil;

import static org.camunda.bpm.container.impl.deployment.Attachments.PROCESSES_XML_RESOURCES;
import static org.camunda.bpm.container.impl.deployment.Attachments.PROCESS_APPLICATION;


/**
 * <p>Detects and parses all META-INF/processes.xml files within the process application
 * and attaches the parsed Metadata to the operation context.</p>
 *
 * @author Daniel Meyer
 *
 */
public class ParseProcessesXmlStep extends DeploymentOperationStep {

  private final static ContainerIntegrationLogger LOG = ProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER;

  public String getName() {
    return "Parse processes.xml deployment descriptor files.";
  }

  public void performOperationStep(DeploymentOperation operationContext) {

    final AbstractProcessApplication processApplication = operationContext.getAttachment(PROCESS_APPLICATION);

    Map<URL, ProcessesXml> parsedFiles = parseProcessesXmlFiles(processApplication);

    // attach parsed metadata
    operationContext.addAttachment(PROCESSES_XML_RESOURCES, parsedFiles);
  }

  protected Map<URL, ProcessesXml> parseProcessesXmlFiles(final AbstractProcessApplication processApplication) {

    String[] deploymentDescriptors = getDeploymentDescriptorLocations(processApplication);
    List<URL> processesXmlUrls = getProcessesXmlUrls(deploymentDescriptors, processApplication);

    Map<URL, ProcessesXml> parsedFiles = new HashMap<URL, ProcessesXml>();

    // perform parsing
    for (URL url : processesXmlUrls) {

      LOG.foundProcessesXmlFile(url.toString());

      if(isEmptyFile(url)) {
        parsedFiles.put(url, ProcessesXml.EMPTY_PROCESSES_XML);
        LOG.emptyProcessesXml();

      } else {
        parsedFiles.put(url, parseProcessesXml(url));
      }
    }

    if(parsedFiles.isEmpty()) {
      LOG.noProcessesXmlForPa(processApplication.getName());
    }

    return parsedFiles;
  }

  protected List<URL> getProcessesXmlUrls(String[] deploymentDescriptors, AbstractProcessApplication processApplication) {
    ClassLoader processApplicationClassloader = processApplication.getProcessApplicationClassloader();

    List<URL> result = new ArrayList<URL>();

    // load all deployment descriptor files using the classloader of the process application
    for (String deploymentDescriptor : deploymentDescriptors) {

      Enumeration<URL> processesXmlFileLocations = null;
      try {
        processesXmlFileLocations = processApplicationClassloader.getResources(deploymentDescriptor);
      }
      catch (IOException e) {
        throw LOG.exceptionWhileReadingProcessesXml(deploymentDescriptor, e);
      }

      while (processesXmlFileLocations.hasMoreElements()) {
        result.add(processesXmlFileLocations.nextElement());
      }

    }

    return result;
  }

  protected String[] getDeploymentDescriptorLocations(AbstractProcessApplication processApplication) {
    ProcessApplication annotation = processApplication.getClass().getAnnotation(ProcessApplication.class);
    if(annotation == null) {
      return new String[] {ProcessApplication.DEFAULT_META_INF_PROCESSES_XML};

    } else {
      return annotation.deploymentDescriptors();

    }
  }

  protected boolean isEmptyFile(URL url) {

    InputStream inputStream = null;

    try {
      inputStream = url.openStream();
      return inputStream.available() == 0;

    }
    catch (IOException e) {
      throw LOG.exceptionWhileReadingProcessesXml(url.toString(), e);
    }
    finally {
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
