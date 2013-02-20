package org.camunda.bpm.container.impl.tomcat.deployment;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.activiti.engine.ActivitiException;
import org.camunda.bpm.container.impl.jmx.deployment.AbstractParseBpmPlatformXmlStep;

/**
 * <p>This deployment operation step is responsible for parsing and attaching the bpm-platform.xml file on tomcat.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class TomcatParseBpmPlatformXmlStep extends AbstractParseBpmPlatformXmlStep {

  protected URL getBpmPlatformXmlStream() {
    
    // read file from CATALINA_HOME directory.
    String catalinaHome = System.getProperty("catalina.home");
    String bpmPlatformFileLocation = catalinaHome + File.separator + "conf" + File.separator + "bpm-platform.xml";
    
    File bpmPlatformFile = new File(bpmPlatformFileLocation);
    
    if(bpmPlatformFile.exists()) {
      try {
        return bpmPlatformFile.toURI().toURL();
      } catch (MalformedURLException e) {
        throw new ActivitiException("Cannot construct URL for "+bpmPlatformFile, e);
      }
      
    } else {
      throw new ActivitiException(bpmPlatformFileLocation + " does not exist. This file is necessary for deploying the camunda BPM platform");
      
    }
  }


}
