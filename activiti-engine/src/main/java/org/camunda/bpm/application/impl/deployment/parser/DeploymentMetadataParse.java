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
package org.camunda.bpm.application.impl.deployment.parser;

import static org.camunda.bpm.application.impl.deployment.metadata.DeploymentMetadataConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.util.xml.Element;
import org.activiti.engine.impl.util.xml.Parse;
import org.activiti.engine.impl.util.xml.Parser;
import org.camunda.bpm.application.impl.deployment.metadata.ProcessEngineXmlImpl;
import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessEngineXml;

/**
 * <p>{@link Parse} implementation for Deployment Metadata.</p>
 * 
 * <p>This class is NOT Threadsafe</p>
 *  
 * @author Daniel Meyer
 *
 */
public abstract class DeploymentMetadataParse extends Parse {
  
  private final static Logger LOGGER = Logger.getLogger(DeploymentMetadataParse.class.getName());

  public DeploymentMetadataParse(Parser parser) {
    super(parser);
  }
  
  public Parse execute() {
    super.execute();
    
    try {
      parseRootElement();

    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unknown exception", e);

      throw new ActivitiException("Error while parsing deployment descriptor: " + e.getMessage(), e);
      
    } finally {
      if (hasWarnings()) {
        logWarnings();
      }
      if (hasErrors()) {
        throwActivitiExceptionForErrors();
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
    
    for (Element childElement : element.elements()) {
      if(CONFIGURATION.equals(childElement.getTagName())) {
        processEngine.setConfigurationClass(childElement.getText());
        
      } else if(DATASOURCE.equals(childElement.getTagName())) {
        processEngine.setDatasource(childElement.getText());
        
      } else if(JOB_ACQUISITION.equals(childElement.getTagName())) {
        processEngine.setJobAcquisitionName(childElement.getText());
        
      } else if(PROPERTIES.equals(childElement.getTagName())) {
        parseProperties(childElement, properties);
        
      }
    }
    
    // set collected properties
    processEngine.setProperties(properties);
    // add the process engine to the list of parsed engines. 
    parsedProcessEngines.add(processEngine);
            
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
   */
  protected void parseProperties(Element element, Map<String, String> properties) {
    
    for (Element childElement : element.elements()) {
      if(PROPERTY.equals(childElement.getTagName())) {
        properties.put(childElement.attribute(NAME), childElement.getText());
      }
    }
    
  }

}
