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
package org.camunda.bpm.application.impl.metadata;

import static org.camunda.bpm.container.impl.metadata.DeploymentMetadataConstants.NAME;
import static org.camunda.bpm.container.impl.metadata.DeploymentMetadataConstants.PROCESS;
import static org.camunda.bpm.container.impl.metadata.DeploymentMetadataConstants.PROCESS_ARCHIVE;
import static org.camunda.bpm.container.impl.metadata.DeploymentMetadataConstants.PROCESS_ENGINE;
import static org.camunda.bpm.container.impl.metadata.DeploymentMetadataConstants.PROPERTIES;
import static org.camunda.bpm.container.impl.metadata.DeploymentMetadataConstants.RESOURCE;
import static org.camunda.bpm.container.impl.metadata.DeploymentMetadataConstants.TENANT_ID;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.application.impl.metadata.spi.ProcessArchiveXml;
import org.camunda.bpm.application.impl.metadata.spi.ProcessesXml;
import org.camunda.bpm.container.impl.metadata.DeploymentMetadataParse;
import org.camunda.bpm.container.impl.metadata.spi.ProcessEngineXml;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.impl.util.xml.Parse;
import org.camunda.bpm.engine.impl.util.xml.Parser;

/**
 * <p>{@link Parse} object for the <code>processes.xml</code> file.</p>
 *
 * <p>This class is NOT Threadsafe</p>
 *
 * @author Daniel Meyer
 *
 */
public class ProcessesXmlParse extends DeploymentMetadataParse {

  /** the constructed ProcessXml */
  protected ProcessesXml processesXml;

  public ProcessesXmlParse(Parser parser) {
    super(parser);
  }

  @Override
  public ProcessesXmlParse execute() {
    super.execute();
    return this;
  }

  /**
   * we know this is a <code>&lt;process-application ... /&gt;</code> structure.
   */
  @Override
  protected void parseRootElement() {

    List<ProcessEngineXml> processEngines = new ArrayList<ProcessEngineXml>();
    List<ProcessArchiveXml> processArchives = new ArrayList<ProcessArchiveXml>();

    for (Element element : rootElement.elements()) {

      if(PROCESS_ENGINE.equals(element.getTagName())) {
        parseProcessEngine(element, processEngines);

      } else if(PROCESS_ARCHIVE.equals(element.getTagName())) {
        parseProcessArchive(element, processArchives);

      }

    }

    processesXml = new ProcessesXmlImpl(processEngines, processArchives);

  }

  /**
   * parse a <code>&lt;process-archive .../&gt;</code> element and add it to the list of parsed elements
   */
  protected void parseProcessArchive(Element element, List<ProcessArchiveXml> parsedProcessArchives) {

    ProcessArchiveXmlImpl processArchive = new ProcessArchiveXmlImpl();

    processArchive.setName(element.attribute(NAME));
    processArchive.setTenantId(element.attribute(TENANT_ID));

    List<String> processResourceNames = new ArrayList<String>();

    Map<String, String> properties = new HashMap<String, String>();
    for (Element childElement : element.elements()) {
      if(PROCESS_ENGINE.equals(childElement.getTagName())) {
        processArchive.setProcessEngineName(childElement.getText());

      } else if(PROCESS.equals(childElement.getTagName()) || RESOURCE.equals(childElement.getTagName())) {
        processResourceNames.add(childElement.getText());

      } else if(PROPERTIES.equals(childElement.getTagName())) {
        parseProperties(childElement, properties);

      }
    }

    // set properties
    processArchive.setProperties(properties);

    // add collected resource names.
    processArchive.setProcessResourceNames(processResourceNames);

    // add process archive to list of parsed archives.
    parsedProcessArchives.add(processArchive);

  }

  public ProcessesXml getProcessesXml() {
    return processesXml;
  }

  @Override
  public ProcessesXmlParse sourceUrl(URL url) {
    super.sourceUrl(url);
    return this;
  }

}
