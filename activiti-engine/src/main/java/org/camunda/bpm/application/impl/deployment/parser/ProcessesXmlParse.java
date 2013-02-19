package org.camunda.bpm.application.impl.deployment.parser;

import static org.camunda.bpm.application.impl.deployment.parser.DeploymentMetadataConstants.NAME;
import static org.camunda.bpm.application.impl.deployment.parser.DeploymentMetadataConstants.PROCESS;
import static org.camunda.bpm.application.impl.deployment.parser.DeploymentMetadataConstants.PROCESS_ARCHIVE;
import static org.camunda.bpm.application.impl.deployment.parser.DeploymentMetadataConstants.PROCESS_ENGINE;
import static org.camunda.bpm.application.impl.deployment.parser.DeploymentMetadataConstants.PROPERTIES;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.bpmn.parser.BpmnParser;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.util.xml.Element;
import org.activiti.engine.impl.util.xml.Parse;
import org.activiti.engine.impl.util.xml.Parser;
import org.camunda.bpm.application.impl.deployment.spi.ProcessArchiveXml;
import org.camunda.bpm.application.impl.deployment.spi.ProcessEngineXml;
import org.camunda.bpm.application.impl.deployment.spi.ProcessesXml;

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
  
  public ProcessesXmlParse execute() {
    super.execute();
    return this;
  }

  /**
   * we know this is a <code>&lt;process-application ... /&gt;</code> structure.
   */
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
    
    // set name
    processArchive.setName(element.attribute(NAME));
    
    List<String> processResourceNames = new ArrayList<String>();
   
    Map<String, String> properties = new HashMap<String, String>();
    for (Element childElement : element.elements()) {
      if(PROCESS_ENGINE.equals(childElement.getTagName())) {
        processArchive.setProcessEngineName(childElement.getText());
        
      } else if(PROCESS.equals(childElement.getTagName())) {
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
  
  public ProcessesXmlParse sourceUrl(URL url) {
    super.sourceUrl(url);
    return this;
  }

}
