package org.camunda.bpm.application.impl.deployment.parser;

import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.util.xml.Parser;


/**
 * <p>A SAX Parser for the processes.xml file</p>
 * 
 * @author Daniel Meyer
 *
 */
public class ProcessesXmlParser extends Parser {
  
  /**
   * The process application namespace
   */
  public static final String PROCESS_APP_NS = "http://www.camunda.org/schema/1.0/ProcessApplication";
  
  /**
   * The location of the XSD file in the classpath.
   */
  public static final String PROCESS_APPLICATION_XSD = "ProcessApplication.xsd";

  /**
   * create an configure the {@link ProcessesXmlParse} object.
   */
  public ProcessesXmlParse createParse() {
    ProcessesXmlParse processesXmlParse = new ProcessesXmlParse(this);
    processesXmlParse.setSchemaResource(ReflectUtil.getResource(PROCESS_APPLICATION_XSD).toString());
    return processesXmlParse;
  }

}
