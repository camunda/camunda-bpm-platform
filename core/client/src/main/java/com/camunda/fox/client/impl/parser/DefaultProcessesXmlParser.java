/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.client.impl.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.activiti.engine.impl.util.IoUtil;

import com.camunda.fox.client.impl.parser.spi.ProcessesXmlParser;
import com.camunda.fox.client.impl.schema.ProcessesXml;
import com.camunda.fox.client.impl.schema.ProcessesXml.ProcessArchiveXml;
import com.camunda.fox.client.impl.schema.ProcessesXmlDeprecated;
import com.camunda.fox.client.impl.schema.ProcessesXmlDeprecated.Process;
import com.camunda.fox.platform.FoxPlatformException;

/**
 * <p>A JAXB-Parser for the META-INF/processes.xml file</p>
 * 
 * @author Daniel Meyer
 */
public class DefaultProcessesXmlParser implements ProcessesXmlParser {
  
  private static Logger log = Logger.getLogger(DefaultProcessesXmlParser.class.getName());
  
  public ProcessesXml parseProcessesXml(String processesXmlLocation) {
    InputStream processesXmlStream = getProcessesXmlAsStream(processesXmlLocation);  
    try {      
      if(processesXmlStream == null) { // markerfile not found        
        return null;        
      } else if(isEmptyStream(processesXmlStream, processesXmlLocation)) { // markerfile empty
        return handleEmptyMarkerfile(processesXmlLocation);
      } else {         
        return parseStream(processesXmlStream, processesXmlLocation);
      }
    } finally {
      if(processesXmlStream != null) {
        IoUtil.closeSilently(processesXmlStream);
      }
    }
  }

  protected boolean isEmptyStream(InputStream processesXmlStream, String processesXmlLocation) {
    try {
      return processesXmlStream.available() == 0;
    } catch (IOException e) {
      log.log(Level.INFO, "Exception while reading '"+processesXmlLocation+"': "+e.getMessage(),e);
      return false;
    }
  }

  protected ProcessesXml parseStream(InputStream processesXmlStream, String processesXmlLocation) {
    try {
      JAXBContext context = JAXBContext.newInstance(ProcessesXml.class);    
      Unmarshaller unmarshaller = context.createUnmarshaller();
      ProcessesXml processesXml = (ProcessesXml) unmarshaller.unmarshal(processesXmlStream);       
      return processesXml;
    }catch (Exception e) {
      try {        
        return temporarySupportForDeprecatedSyntax(processesXmlLocation); 
      }catch (Exception ex) {
        // throw original exception:
        throw new FoxPlatformException("Exception while parsing '"+processesXmlLocation+"': "+e.getMessage(),e); 
      }
      
    }
  }

  private ProcessesXml temporarySupportForDeprecatedSyntax(String processesXmlLocation) throws Exception {    
    // try using old syntax:
    JAXBContext context = JAXBContext.newInstance(ProcessesXmlDeprecated.class);    
    Unmarshaller unmarshaller = context.createUnmarshaller();
    InputStream processesXmlAsStream = getProcessesXmlAsStream(processesXmlLocation);
    try {
      ProcessesXmlDeprecated processesXmlDeprecated = (ProcessesXmlDeprecated) unmarshaller.unmarshal(processesXmlAsStream);
      log.warning("Using deprecated syntax for "+processesXmlLocation+" file. Please update to the new syntax.");
      
      ProcessesXml processesXml = new ProcessesXml();        
      ProcessesXml.ProcessArchiveXml processArchive = new ProcessesXml.ProcessArchiveXml();
      processArchive.name = processesXmlDeprecated.name;
      processArchive.configuration.undeployment.delete = processesXmlDeprecated.configuration.undeployment.delete;
      for (Process process : processesXmlDeprecated.processes) {
        com.camunda.fox.client.impl.schema.ProcessesXml.ProcessArchiveXml.ProcessResourceXml process2 = new ProcessesXml.ProcessArchiveXml.ProcessResourceXml();
        process2.resourceName = process.resourceName;
        processArchive.processes.add(process2);
      }
      processesXml.processArchives.add(processArchive);
      
      return processesXml;
    }finally{
      processesXmlAsStream.close();
    }
  }

  protected ProcessesXml handleEmptyMarkerfile(String processesXmlLocation) {
    log.info(""+processesXmlLocation+" is empty: using default values for process archive.");
     ProcessesXml processesXml = new ProcessesXml();
     processesXml.processArchives.add(new ProcessArchiveXml());
     return processesXml;
  }

  protected InputStream getProcessesXmlAsStream(String processesXmlLocation) {
    return getClass().getClassLoader().getResourceAsStream(processesXmlLocation);
  }
  
}
