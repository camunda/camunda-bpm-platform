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
package org.camunda.bpm.container.impl.metadata;

import static org.camunda.bpm.container.impl.metadata.DeploymentMetadataConstants.JOB_ACQUISITION;
import static org.camunda.bpm.container.impl.metadata.DeploymentMetadataConstants.JOB_EXECUTOR;
import static org.camunda.bpm.container.impl.metadata.DeploymentMetadataConstants.JOB_EXECUTOR_CLASS_NAME;
import static org.camunda.bpm.container.impl.metadata.DeploymentMetadataConstants.NAME;
import static org.camunda.bpm.container.impl.metadata.DeploymentMetadataConstants.PROCESS_ENGINE;
import static org.camunda.bpm.container.impl.metadata.DeploymentMetadataConstants.PROPERTIES;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.container.impl.metadata.spi.BpmPlatformXml;
import org.camunda.bpm.container.impl.metadata.spi.JobAcquisitionXml;
import org.camunda.bpm.container.impl.metadata.spi.ProcessEngineXml;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.impl.util.xml.Parser;

/**
 * <p>Parse implementation for parsing the {@link BpmPlatformXml}</p>
 *
 * @author Daniel Meyer
 *
 */
public class BpmPlatformXmlParse extends DeploymentMetadataParse {

  /** the parsed {@link BpmPlatformXml} */
  protected BpmPlatformXml bpmPlatformXml;

  public BpmPlatformXmlParse(Parser parser) {
    super(parser);
  }

  public BpmPlatformXmlParse execute() {
    super.execute();
    return this;
  }

  /** We know this is a <code>&lt;bpm-platform ../&gt;</code> element */
  protected void parseRootElement() {

    JobExecutorXmlImpl jobExecutor = new JobExecutorXmlImpl();
    List<ProcessEngineXml> processEngines = new ArrayList<ProcessEngineXml>();

    for (Element element : rootElement.elements()) {

      if(JOB_EXECUTOR.equals(element.getTagName())) {
        parseJobExecutor(element, jobExecutor);

      } else if(PROCESS_ENGINE.equals(element.getTagName())) {
        parseProcessEngine(element, processEngines);

      }

    }

    bpmPlatformXml = new BpmPlatformXmlImpl(jobExecutor, processEngines);
  }

  /**
   * parse a <code>&lt;job-executor .../&gt;</code> element and add it to the list of parsed elements
   */
  protected void parseJobExecutor(Element element, JobExecutorXmlImpl jobExecutorXml) {

    List<JobAcquisitionXml> jobAcquisitions = new ArrayList<JobAcquisitionXml>();
    Map<String, String> properties = new HashMap<String, String>();

    for (Element childElement : element.elements()) {

      if(JOB_ACQUISITION.equals(childElement.getTagName())) {
        parseJobAcquisition(childElement, jobAcquisitions);

      }else if(PROPERTIES.equals(childElement.getTagName())){
        parseProperties(childElement, properties);
      }

    }

    jobExecutorXml.setJobAcquisitions(jobAcquisitions);
    jobExecutorXml.setProperties(properties);

  }

  /**
   * parse a <code>&lt;job-acquisition .../&gt;</code> element and add it to the
   * list of parsed elements
   */
  protected void parseJobAcquisition(Element element, List<JobAcquisitionXml> jobAcquisitions) {

    JobAcquisitionXmlImpl jobAcquisition = new JobAcquisitionXmlImpl();

    // set name
    jobAcquisition.setName(element.attribute(NAME));

    Map<String, String> properties = new HashMap<String, String>();

    for (Element childElement : element.elements()) {
      if (JOB_EXECUTOR_CLASS_NAME.equals(childElement.getTagName())) {
        jobAcquisition.setJobExecutorClassName(childElement.getText());

      } else if (PROPERTIES.equals(childElement.getTagName())) {
        parseProperties(childElement, properties);

      }
    }

    // set collected properties
    jobAcquisition.setProperties(properties);
    // add to list
    jobAcquisitions.add(jobAcquisition);

  }


  public BpmPlatformXml getBpmPlatformXml() {
    return bpmPlatformXml;
  }

  public BpmPlatformXmlParse sourceUrl(URL url) {
    super.sourceUrl(url);
    return this;
  }

}
