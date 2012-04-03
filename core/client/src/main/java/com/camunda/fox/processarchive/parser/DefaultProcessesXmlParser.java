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
package com.camunda.fox.processarchive.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.activiti.engine.impl.util.IoUtil;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.processarchive.parser.spi.ProcessesXmlParser;
import com.camunda.fox.processarchive.schema.ProcessesXml;

/**
 * <p>A JAXB-Parser for the META-INF/processes.xml file</p>
 * 
 * @author Daniel Meyer
 */
public class DefaultProcessesXmlParser implements ProcessesXmlParser {
  
  private static final String MARKER_FILE_LOCATION = "META-INF/processes.xml";
  
  private static Logger log = Logger.getLogger(DefaultProcessesXmlParser.class.getName());
  
  public ProcessesXml parseProcessesXml() {
    InputStream processesXmlStream = getProcessesXmlAsStream();  
    try {      
      if(processesXmlStream == null) { // markerfile not found        
        return null;        
      } else if(isEmptyStream(processesXmlStream)) { // markerfile empty
        return handleEmptyMarkerfile();
      } else {         
        return parseStream(processesXmlStream);
      }
    } finally {
      if(processesXmlStream != null) {
        IoUtil.closeSilently(processesXmlStream);
      }
    }
  }

  protected boolean isEmptyStream(InputStream processesXmlStream) {
    try {
      return processesXmlStream.available() == 0;
    } catch (IOException e) {
      log.log(Level.INFO, "Exception while reading '"+MARKER_FILE_LOCATION+"': "+e.getMessage(),e);
      return false;
    }
  }

  protected ProcessesXml parseStream(InputStream processesXmlStream) {
    try {
      JAXBContext context = JAXBContext.newInstance(ProcessesXml.class);    
      Unmarshaller unmarshaller = context.createUnmarshaller();
      ProcessesXml processesXml = (ProcessesXml) unmarshaller.unmarshal(processesXmlStream);       
      return processesXml;
    }catch (Exception e) {
      throw new FoxPlatformException("Exception while parsing '"+MARKER_FILE_LOCATION+"': "+e.getMessage(),e);
    }
  }

  protected ProcessesXml handleEmptyMarkerfile() {
    log.info(""+MARKER_FILE_LOCATION+" is empty: using default values for process archive.");
    return new ProcessesXml();
  }

  protected InputStream getProcessesXmlAsStream() {
    return getClass().getClassLoader().getResourceAsStream("META-INF/processes.xml");
  }
  
}
