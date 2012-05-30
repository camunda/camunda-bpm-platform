package com.camunda.fox.platform.impl.jobexecutor.ext.config;

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
import com.camunda.fox.platform.impl.jobexecutor.ext.config.spi.JobExecutorXmlParser;


public class JobExecutorXmlParserImpl implements JobExecutorXmlParser {
  
  protected JAXBContext context;

  @Override
  public List<JobExecutorXml> parseJobExecutorXml(String jobExecutorXmlLocation) {
    List<JobExecutorXml> result = new ArrayList<JobExecutorXml>();
    
    Enumeration<URL> resources = this.getResourcesAsStream(jobExecutorXmlLocation);
    while (resources.hasMoreElements()) {
      System.out.println("--------------------------------------------------------------------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> bin in der Schleife");
      URL url = (URL) resources.nextElement();
      InputStream stream = null;
      try {
        stream = url.openStream();
        JobExecutorXml parsedResource = this.parseStream(stream, jobExecutorXmlLocation);
        parsedResource.setResourceName(url.getFile());
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
  
  protected JobExecutorXml parseStream(InputStream stream, String jobExecutorXmlLocation) {
    try {
      if(context == null) {
        context = JAXBContext.newInstance(JobExecutorXml.class);
      }
      Unmarshaller unmarshaller = context.createUnmarshaller();
      JobExecutorXml processEnginesXml = (JobExecutorXml) unmarshaller.unmarshal(stream);
      return processEnginesXml;
    } catch (Exception e) {
      throw new FoxPlatformException("Exception while parsing '" + jobExecutorXmlLocation + "': " + e.getMessage(), e);
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
