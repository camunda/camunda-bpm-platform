package com.camunda.fox.platform.impl.ext.config.engine;

import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.camunda.fox.platform.impl.ext.config.engine.spi.ProcessEnginesXmlParser;
import com.camunda.fox.platform.spi.ProcessEngineConfiguration;

/**
 * 
 * @author Daniel Meyer
 * 
 */
public class TestProcessEnginesXmlParser {

  private ProcessEnginesXmlParser parser;

  @Before
  public void init() {
    parser = new ProcessEnginesXmlParserImpl();
  }
  
  @Test
  public void testEmptyRootTag() {
    List<ProcessEnginesXml> processEnginesXml = parser.parseProcessEnginesXml("com/camunda/fox/platform/impl/ext/config/engine/emptyRootTag.xml");
    Assert.assertNotNull(processEnginesXml);
    Assert.assertEquals(0, processEnginesXml.get(0).processEngines.size());
  }
  
  @Test
  public void testSingleEngine() {
    List<ProcessEnginesXml> processEnginesXmls = parser.parseProcessEnginesXml("com/camunda/fox/platform/impl/ext/config/engine/singleEngine.xml");
    Assert.assertNotNull(processEnginesXmls);
    Assert.assertEquals(1, processEnginesXmls.size());
    ProcessEnginesXml processEnginesXml = processEnginesXmls.get(0);
    Assert.assertEquals(1, processEnginesXml.processEngines.size());
    ProcessEngineConfiguration processEngineConfiguration = processEnginesXml.processEngines.get(0);
                
    Assert.assertEquals(true, processEngineConfiguration.isDefault());
    Assert.assertEquals("default", processEngineConfiguration.getProcessEngineName());
    Assert.assertEquals("jdbc/FoxEngine", processEngineConfiguration.getDatasourceJndiName());
    Assert.assertEquals("audit", processEngineConfiguration.getHistoryLevel());
    Map<String, Object> properties = processEngineConfiguration.getProperties();
    Assert.assertEquals("prefix", properties.get(ProcessEngineConfiguration.PROP_DB_TABLE_PREFIX));
    Assert.assertEquals(false, properties.get(ProcessEngineConfiguration.PROP_IS_ACTIVATE_JOB_EXECUTOR));
    Assert.assertEquals(true, properties.get(ProcessEngineConfiguration.PROP_IS_AUTO_SCHEMA_UPDATE));
    Assert.assertEquals("default", properties.get(ProcessEngineConfiguration.PROP_JOB_EXECUTOR_ACQUISITION_NAME));
  }
  
  @Test
  public void testMultipleEngines() {
    List<ProcessEnginesXml> processEnginesXmls = parser.parseProcessEnginesXml("com/camunda/fox/platform/impl/ext/config/engine/multipleEngines.xml");
    Assert.assertNotNull(processEnginesXmls);
    Assert.assertEquals(1, processEnginesXmls.size());
    ProcessEnginesXml processEnginesXml = processEnginesXmls.get(0);
    Assert.assertEquals(2, processEnginesXml.processEngines.size());           
  }
}

