package org.camunda.bpm.application.impl.deployment.parser;

import java.net.URL;
import java.util.Map;

import junit.framework.TestCase;

import org.camunda.bpm.application.impl.deployment.metadata.spi.BpmPlatformXml;
import org.camunda.bpm.application.impl.deployment.metadata.spi.JobAcquisitionXml;
import org.camunda.bpm.application.impl.deployment.metadata.spi.JobExecutorXml;
import org.camunda.bpm.application.impl.deployment.metadata.spi.ProcessEngineXml;

/**
 * <p>The testcases for the {@link BpmPlatformXmlParser}</p>
 * 
 * @author Daniel Meyer
 *
 */
public class BpmPlatformXmlParserTest extends TestCase {
  
  private BpmPlatformXmlParser parser; 
  
  protected void setUp() throws Exception {
    parser = new BpmPlatformXmlParser();
    super.setUp();
  }
  
  protected URL getStreamUrl(String filename) {
    return BpmPlatformXmlParserTest.class.getResource(filename);
  }
  
  public void testParseBpmPlatformXmlNoEngine() {
    
    BpmPlatformXml bpmPlatformXml = parser.createParse()
      .sourceUrl(getStreamUrl("bpmplatform_xml_no_engine.xml"))
      .execute()
      .getBpmPlatformXml();
    
    assertNotNull(bpmPlatformXml);
    assertNotNull(bpmPlatformXml.getJobExecutor());
    assertEquals(0, bpmPlatformXml.getProcessEngines().size());
    
    JobExecutorXml jobExecutorXml = bpmPlatformXml.getJobExecutor();
    assertEquals(1, jobExecutorXml.getJobAcquisitions().size());
    
    JobAcquisitionXml jobAcquisitionXml = jobExecutorXml.getJobAcquisitions().get(0);
    assertEquals("default", jobAcquisitionXml.getName());
    assertEquals("SEQUENTIAL", jobAcquisitionXml.getAcquisitionStrategy());
    
    assertEquals(2, jobAcquisitionXml.getProperties().size());
    
  }
  
  public void testParseBpmPlatformXmlOneEngine() {
    
    BpmPlatformXml bpmPlatformXml = parser.createParse()
      .sourceUrl(getStreamUrl("bpmplatform_xml_one_engine.xml"))
      .execute()
      .getBpmPlatformXml();
    
    assertNotNull(bpmPlatformXml);
    assertNotNull(bpmPlatformXml.getJobExecutor());
    assertEquals(1, bpmPlatformXml.getProcessEngines().size());
    
    JobExecutorXml jobExecutorXml = bpmPlatformXml.getJobExecutor();
    assertEquals(1, jobExecutorXml.getJobAcquisitions().size());
    
    JobAcquisitionXml jobAcquisitionXml = jobExecutorXml.getJobAcquisitions().get(0);
    assertEquals("default", jobAcquisitionXml.getName());
    assertEquals("SEQUENTIAL", jobAcquisitionXml.getAcquisitionStrategy());
    
    assertEquals(2, jobAcquisitionXml.getProperties().size());
    
    ProcessEngineXml engineXml = bpmPlatformXml.getProcessEngines().get(0);
    assertEquals("engine1", engineXml.getName());
    assertEquals("default", engineXml.getJobAcquisitionName());
    
    Map<String, String> properties = engineXml.getProperties();
    assertNotNull(properties);
    assertEquals(0, properties.size());
    
  }

}
