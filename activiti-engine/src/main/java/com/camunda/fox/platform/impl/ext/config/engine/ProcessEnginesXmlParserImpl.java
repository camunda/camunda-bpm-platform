package com.camunda.fox.platform.impl.ext.config.engine;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.activiti.engine.impl.util.IoUtil;

import com.camunda.fox.platform.FoxPlatformException;
import com.camunda.fox.platform.impl.ext.config.engine.spi.ProcessEnginesXmlParser;

/**
 * Simple JAXB parser for parsing all META-INF/process-engines.xml found on our classpath.
 * 
 * @author Daniel Meyer
 * 
 */
public class ProcessEnginesXmlParserImpl implements ProcessEnginesXmlParser {

  protected JAXBContext context;

  public List<ProcessEnginesXml> parseProcessEnginesXml(String processEnginesXmlLocation) {
    List<ProcessEnginesXml> result = new ArrayList<ProcessEnginesXml>();
    
    Enumeration<URL> resources = getResourcesAsStream(processEnginesXmlLocation);
    while (resources.hasMoreElements()) {
      URL url = (URL) resources.nextElement();
      InputStream stream = null;
      try {
        stream = url.openStream();
        ProcessEnginesXml parsedResource = parseStream(stream, processEnginesXmlLocation);
        parsedResource.resourceName = url.getFile();
        result.add(parsedResource);        
      }catch (IOException e) {
        throw new FoxPlatformException("Could not load resources '"+url.getFile()+"'");
      } finally {
        if (stream != null) {
          IoUtil.closeSilently(stream);
        }  
      }      
    }  
    
    return result;
  }

  protected ProcessEnginesXml parseStream(InputStream stream, String processEnginesXmlLocation) {
    try {
      if(context == null) {
        context = JAXBContext.newInstance(ProcessEnginesXml.class);
      }
      Unmarshaller unmarshaller = context.createUnmarshaller();
      ProcessEnginesXml processEnginesXml = (ProcessEnginesXml) unmarshaller.unmarshal(stream);
      return processEnginesXml;
    } catch (Exception e) {
      throw new FoxPlatformException("Exception while parsing '" + processEnginesXmlLocation + "': " + e.getMessage(), e);
    }
  }

  protected Enumeration<URL> getResourcesAsStream(String resourceName) {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    if(classLoader == null) {
      classLoader = getClass().getClassLoader();             
    }
    try {
      return  classLoader.getResources(resourceName);
    } catch (IOException e) {
      throw new FoxPlatformException("Could not load resources with name '"+resourceName+"'");
    }
  }

}
