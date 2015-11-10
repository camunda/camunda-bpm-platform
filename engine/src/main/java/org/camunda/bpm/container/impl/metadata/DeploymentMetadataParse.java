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
package org.camunda.bpm.container.impl.metadata;

import static org.camunda.bpm.container.impl.metadata.DeploymentMetadataConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.camunda.bpm.container.impl.ContainerIntegrationLogger;
import org.camunda.bpm.container.impl.metadata.spi.ProcessEnginePluginXml;
import org.camunda.bpm.container.impl.metadata.spi.ProcessEngineXml;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.util.xml.Element;
import org.camunda.bpm.engine.impl.util.xml.Parse;
import org.camunda.bpm.engine.impl.util.xml.Parser;

/**
 * <p>{@link Parse} implementation for Deployment Metadata.</p>
 *
 * <p>This class is NOT Threadsafe</p>
 *
 * @author Daniel Meyer
 *
 */
public abstract class DeploymentMetadataParse extends Parse {

  private final static ContainerIntegrationLogger LOG = ProcessEngineLogger.CONTAINER_INTEGRATION_LOGGER;

  public DeploymentMetadataParse(Parser parser) {
    super(parser);
  }

  public Parse execute() {
    super.execute();

    try {
      parseRootElement();
    }
    catch (Exception e) {
      throw LOG.unknownExceptionWhileParsingDeploymentDescriptor(e);
    }
    finally {
      if (hasWarnings()) {
        logWarnings();
      }
      if (hasErrors()) {
        throwExceptionForErrors();
      }
    }

    return this;
  }

  /**
   * to be overridden by subclasses.
   */
  protected abstract void parseRootElement();

  /**
   * parse a <code>&lt;process-engine .../&gt;</code> element and add it to the list of parsed elements
   */
  protected void parseProcessEngine(Element element, List<ProcessEngineXml> parsedProcessEngines) {

    ProcessEngineXmlImpl processEngine = new ProcessEngineXmlImpl();

    // set name
    processEngine.setName(element.attribute(NAME));

    // set default
    String defaultValue = element.attribute(DEFAULT);
    if(defaultValue == null || defaultValue.isEmpty()) {
      processEngine.setDefault(false);
    } else {
      processEngine.setDefault(Boolean.parseBoolean(defaultValue));
    }

    Map<String, String> properties = new HashMap<String, String>();
    List<ProcessEnginePluginXml> plugins = new ArrayList<ProcessEnginePluginXml>();

    for (Element childElement : element.elements()) {
      if(CONFIGURATION.equals(childElement.getTagName())) {
        processEngine.setConfigurationClass(childElement.getText());

      } else if(DATASOURCE.equals(childElement.getTagName())) {
        processEngine.setDatasource(childElement.getText());

      } else if(JOB_ACQUISITION.equals(childElement.getTagName())) {
        processEngine.setJobAcquisitionName(childElement.getText());

      } else if(PROPERTIES.equals(childElement.getTagName())) {
        parseProperties(childElement, properties);

      } else if(PLUGINS.equals(childElement.getTagName())) {
        parseProcessEnginePlugins(childElement, plugins);

      }
    }

    // set collected properties
    processEngine.setProperties(properties);
    // set plugins
    processEngine.setPlugins(plugins);
    // add the process engine to the list of parsed engines.
    parsedProcessEngines.add(processEngine);

  }

  /**
   * Transform a <code>&lt;plugins ... /&gt;</code> structure.
   */
  protected void parseProcessEnginePlugins(Element element, List<ProcessEnginePluginXml> plugins) {
    for (Element chidElement : element.elements()) {
      if(PLUGIN.equals(chidElement.getTagName())) {
        parseProcessEnginePlugin(chidElement, plugins);
      }
    }
  }

  /**
   * Transform a <code>&lt;plugin ... /&gt;</code> structure.
   */
  protected void parseProcessEnginePlugin(Element element, List<ProcessEnginePluginXml> plugins) {

    ProcessEnginePluginXmlImpl plugin = new ProcessEnginePluginXmlImpl();

    Map<String, String> properties = new HashMap<String, String>();

    for (Element childElement : element.elements()) {
      if(PLUGIN_CLASS.equals(childElement.getTagName())) {
        plugin.setPluginClass(childElement.getText());

      } else if(PROPERTIES.equals(childElement.getTagName())) {
        parseProperties(childElement, properties);

      }

    }

    plugin.setProperties(properties);
    plugins.add(plugin);
  }

  /**
   * Transform a
   * <pre>
   * &lt;properties&gt;
   *   &lt;property name="name"&gt;value&lt;/property&gt;
   * &lt;/properties&gt;
   * </pre>
   * structure into a properties {@link Map}
   *
   * Supports resolution of Ant-style placeholders against system properties.
   *
   */
  protected void parseProperties(Element element, Map<String, String> properties) {

    for (Element childElement : element.elements()) {
      if(PROPERTY.equals(childElement.getTagName())) {
        String resolved = PropertyHelper.resolveProperty(System.getProperties(), childElement.getText());
        properties.put(childElement.attribute(NAME), resolved);
      }
    }

  }

}
