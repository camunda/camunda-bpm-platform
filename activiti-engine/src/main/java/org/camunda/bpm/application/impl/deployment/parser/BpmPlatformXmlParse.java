package org.camunda.bpm.application.impl.deployment.parser;

import static org.camunda.bpm.application.impl.deployment.metadata.DeploymentMetadataConstants.JOB_ACQUISITION;
import static org.camunda.bpm.application.impl.deployment.metadata.DeploymentMetadataConstants.JOB_EXECUTOR;
import static org.camunda.bpm.application.impl.deployment.metadata.DeploymentMetadataConstants.NAME;
import static org.camunda.bpm.application.impl.deployment.metadata.DeploymentMetadataConstants.PROCESS_ENGINE;
import static org.camunda.bpm.application.impl.deployment.metadata.DeploymentMetadataConstants.PROPERTIES;
import static org.camunda.bpm.application.impl.deployment.metadata.DeploymentMetadataConstants.ACQUISITION_STRATEGY;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.impl.util.xml.Element;
import org.activiti.engine.impl.util.xml.Parser;
import org.camunda.bpm.application.impl.deployment.metadata.BpmPlatformXmlImpl;
import org.camunda.bpm.application.impl.deployment.metadata.JobAcquisitionXmlImpl;
import org.camunda.bpm.application.impl.deployment.metadata.JobExecutorXmlImpl;
import org.camunda.bpm.application.impl.deployment.metadata.spi.BpmPlatformXml;
import org.camunda.bpm.application.impl.deployment.metadata.spi.JobAcquisitionXml;
import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessEngineXml;

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
    
    for (Element childElement : element.elements()) {
      
      if(JOB_ACQUISITION.equals(childElement.getTagName())) {
        parseJobAcquisition(childElement, jobAcquisitions);
        
      }
      
    }
    
    jobExecutorXml.setJobAcquisitions(jobAcquisitions);
    
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
      if (ACQUISITION_STRATEGY.equals(childElement.getTagName())) {
        jobAcquisition.setAcquisitionStrategy(childElement.getText());

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
