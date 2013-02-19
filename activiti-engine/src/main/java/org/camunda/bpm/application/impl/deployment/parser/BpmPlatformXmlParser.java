package org.camunda.bpm.application.impl.deployment.parser;

import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.impl.util.xml.Parser;


/**
 * <p>A SAX Parser for the bpm-platform.xml file</p>
 * 
 * @author Daniel Meyer
 *
 */
public class BpmPlatformXmlParser extends Parser {
  
  /**
   * The bpm platform namespace
   */
  public static final String BPM_PLATFORM_NS = "http://www.camunda.org/schema/1.0/BpmPlatform";
  
  /**
   * The location of the XSD file in the classpath.
   */
  public static final String BPM_PLATFORM_XSD = "BpmPlatform.xsd";

  /**
   * create an configure the {@link ProcessesXmlParse} object.
   */
  public BpmPlatformXmlParse createParse() {
    BpmPlatformXmlParse parse = new BpmPlatformXmlParse(this);
    parse.setSchemaResource(ReflectUtil.getResource(BPM_PLATFORM_XSD).toString());
    return parse;
  }

}
